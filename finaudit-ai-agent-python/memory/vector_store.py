import os
from pathlib import Path
from pypdf import PdfReader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_google_genai import GoogleGenerativeAIEmbeddings
from langchain_chroma import Chroma
import config  # Ensure settings and .env are loaded

BASE_DIR = Path(__file__).resolve().parents[1]
POLICY_PATH = BASE_DIR / "data" / "company_policy.pdf"
CHROMA_DIR = BASE_DIR / "chroma_db"

def load_and_split_policy():
    try:
        reader = PdfReader(str(POLICY_PATH))
        raw_text = ""

        for page in reader.pages:
            text = page.extract_text()
            if text:
                raw_text += text + "\n"

        text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=800,
            chunk_overlap=100,
        )
        chunks = text_splitter.split_text(raw_text)
        return chunks
    except Exception as e:
        print(f"Error loading and splitting policy: {e}")
        return []

# Initialize embeddings (uses GEMINI_API_KEY from environment)
embeddings = GoogleGenerativeAIEmbeddings(model="gemini-embedding-001")

def create_retriever(chunks):
    try:
        if not chunks:
            print("No policy chunks available; retriever cannot be created.")
            return None

        vectorstore = Chroma.from_texts(
             texts=chunks,
             embedding=embeddings,
             persist_directory=str(CHROMA_DIR)
        )
        
        retriever = vectorstore.as_retriever(search_kwargs={"k": 3})
        return retriever
    
    except Exception as e:
        print(f"Error creating retriever: {e}")
        return None
