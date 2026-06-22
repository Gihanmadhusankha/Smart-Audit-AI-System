import asyncio
import os
import sys
import json
from pathlib import Path
import redis
import dotenv
import aio_pika

dotenv.load_dotenv()

# Add root folder to sys.path if run as a script directly
if __package__ is None or __package__ == "":
    sys.path.append(str(Path(__file__).resolve().parents[1]))

import config  # Ensure settings are loaded
from memory.vector_store import (
    load_and_split_policy,
    create_retriever,
)
from agents.auditor_agent import (
    analyze_claim_with_ai,
)
from tools.spring_client import (
    update_claim_status,
    get_claim_by_id as get,
)

# Load policy once
print("Loading Policy & Initializing Retriever...")
chunks = load_and_split_policy()
retriever = create_retriever(chunks)

r = redis.Redis(
    host=os.getenv("REDIS_HOST"),
    port=int(os.getenv("REDIS_PORT")),
    db=int(os.getenv("REDIS_DB")),
    decode_responses=True
)

# Set up a semaphore to limit concurrent AI calls to 2
semaphore = asyncio.Semaphore(2)

async def safe_ai_call(claim, retriever, receipt_path=None):
    async with semaphore:
        for attempt in range(3): 
            try:
                return await analyze_claim_with_ai(claim, retriever, receipt_path)
            except Exception as e:
                print(f"AI failed attempt {attempt+1}: {e}")
                if "429" in str(e): 
                    await asyncio.sleep(10) 
                else:
                    await asyncio.sleep(2)
        return {"status": "PENDING", "reason": "AI Service busy, retrying later."}
    
async def process_claim(claim_id: str):
    try:
        cached_result = r.get(claim_id)
        if cached_result:
            print(f"Cache hit for claim {claim_id}")
            result = json.loads(cached_result)
        else:
            print(f"Cache miss for claim {claim_id}, fetching from Spring Boot...")
            claim = await get(claim_id)
        
        if not claim:
            print(f"Claim {claim_id} not found!")
            return

        print(f"Analyzing claim: {claim_id} - {claim.get('description')}")

        # 2. AI analysis
        receipt_path = claim.get("receipt_path")  # Assuming the claim has a field for receipt path
        result = await safe_ai_call(claim, retriever, receipt_path)
        print(f"DEBUG: AI Result object: {result}")
        r.set(claim_id, json.dumps(result), ex=3600)  # Cache for 1 hour

        # 3. Update back to Spring Boot
        await update_claim_status(
            claim_id=int(claim_id),
            status=result.get("status"),
            reason=result.get("reason")
        )

        print(f"Claim {claim_id} updated ➜ {result.get('status')}")
        print(f"Reason: {result.get('reason')}")

    except Exception as e:
        print(f"Error processing claim {claim_id}: {e}")

async def main():
    connection = await aio_pika.connect_robust(
        "amqp://guest:guest@localhost/"
    )

    async with connection:
        channel = await connection.channel()

        queue = await channel.declare_queue(
            "expense_queue",
            durable=True
        )

        print(" [*] RabbitMQ Worker is listening for claims...")

        async with queue.iterator() as queue_iter:
            async for message in queue_iter:
                async with message.process():
                    claim_id = message.body.decode()
                    await process_claim(claim_id)

if __name__ == "__main__":
    asyncio.run(main())
