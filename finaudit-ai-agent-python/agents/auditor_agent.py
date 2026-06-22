import os
import json
import base64
from groq import Groq
from config import settings
from prompts.system_prompts import AUDITOR_SYSTEM_PROMPT_TEMPLATE

client = Groq(api_key=settings.GROQ_API_KEY)

def encode_image(image_path):
    with open(image_path, "rb") as image_file:
        return base64.b64encode(image_file.read()).decode("utf-8")

async def analyze_claim_with_ai(claim: dict, retriever, receipt_path: str = None):
    try:
        if retriever is None:
            return {"status": "REJECTED", "reason": "Retriever is not available."}

        # 1. Retrieve relevant policy context
        claim_description = claim.get("description", "")
        claim_amount = claim.get("amount", 0)
        search_query = f"Expense: {claim_description}, Amount: {claim_amount}"
        
        relevant_docs = await retriever.ainvoke(search_query)
        context = "\n\n".join([doc.page_content for doc in relevant_docs])

        # 2. System Prompt 
        system_prompt = AUDITOR_SYSTEM_PROMPT_TEMPLATE.format(
            claim_description=claim_description,
            claim_amount=claim_amount,
            context=context
        )
        content_list = [{"type": "text", "text": f"Audit this claim: Description: {claim_description}, Amount: LKR {claim_amount}"}]

        if receipt_path and os.path.exists(receipt_path):
            base64_image = encode_image(receipt_path)
            content_list.append({
                "type": "image_url",
                "image_url": {"url": f"data:image/jpeg;base64,{base64_image}"}
            })

        # 3. Groq API Call 
        chat_completion = client.chat.completions.create(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": f"Audit this claim: Description: {claim_description}, Amount: LKR {claim_amount}"}
            ],
            model="qwen/qwen3-32b",
            response_format={"type": "json_object"} 
        )

        # 4. JSON parse
        result_text = chat_completion.choices[0].message.content
        return json.loads(result_text)

    except Exception as e:
        print(f"DEBUG: Groq Processing Error Details: {str(e)}")
        return {"status": "PENDING", "reason": "AI Service busy, retrying..."}
