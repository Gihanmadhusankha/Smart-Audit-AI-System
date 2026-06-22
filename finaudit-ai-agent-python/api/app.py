import sys
from pathlib import Path
from contextlib import asynccontextmanager
from fastapi import FastAPI

# Add root folder to sys.path if run as a script directly
if __package__ is None or __package__ == "":
    sys.path.append(str(Path(__file__).resolve().parents[1]))

import config  # Ensure settings are loaded
from tools.spring_client import get_pending_claims, update_claim_status
from agents.auditor_agent import analyze_claim_with_ai
from memory.vector_store import load_and_split_policy, create_retriever

retriever = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    global retriever

    print("Loading and splitting policy document...")
    chunks = load_and_split_policy()
    retriever = create_retriever(chunks)
    print("Retriever ready.")

    yield

    print("Shutting down FastAPI service...")

app = FastAPI(lifespan=lifespan)

@app.get("/")
def read_root():
    return {"status": "AI Financial Auditor Agent is running successfully!"}
