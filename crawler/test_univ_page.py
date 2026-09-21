import requests
import json

url = "http://localhost:8088/api/university/page?page=1&size=10"
r = requests.get(url)
print("Status:", r.status_code)
data = r.json()
print("Total in page API:", data.get("data", {}).get("total"))
print("First 3 items:")
for item in data.get("data", {}).get("records", [])[:3]:
    print(" ", item.get("id"), item.get("schoolName"), item.get("province"), item.get("schoolLevel"), item.get("schoolType"))
