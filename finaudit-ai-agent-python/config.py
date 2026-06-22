import os
from dotenv import load_dotenv

load_dotenv()


class Settings:
  SPRINGBOOT_URL: str = os.getenv("SPRINGBOOT_URL")
  GROQ_API_KEY: str = os.getenv("GROQ_API_KEY")

if not Settings.GROQ_API_KEY:
    raise ValueError("GROQ_API_KEY is not set in the environment variables.")

if not Settings.SPRINGBOOT_URL:
    raise ValueError("SPRINGBOOT_URL is not set in the environment variables.")

settings = Settings()
