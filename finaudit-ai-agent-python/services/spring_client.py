import httpx
from config import settings

async def get_pending_claims():
    url = f"{settings.SPRINGBOOT_URL}/api/claims/pending"

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(url)
            response.raise_for_status()
            return response.json()
        except httpx.HTTPError as exc:
            print(f"HTTP error occurred for springboot url: {exc}")
            return []
        
async def update_claim_status(claim_id: int, status: str, reason: str):
    url = f"{settings.SPRINGBOOT_URL}/api/claims/{claim_id}/status"
    payload = {"status": status, "reason": reason}

    async with httpx.AsyncClient() as client:
        try:
            response = await client.put(url, json=payload)
            response.raise_for_status()
            return response.json()
        except httpx.HTTPError as exc:
            print(f"HTTP error occurred for springboot url: {exc}")
            return None

async def get_claim_by_id(claim_id: int):
    url = f"{settings.SPRINGBOOT_URL}/api/claims/{claim_id}"

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(url)
            response.raise_for_status()
            return response.json()
        except httpx.HTTPError as exc:
            print(f"HTTP error occurred while fetching claim {claim_id}: {exc}")
            return None
