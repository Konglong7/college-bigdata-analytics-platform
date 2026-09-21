import requests
import json

base_url = "http://localhost:8088"
endpoints = [
    "/api/dashboard/statistics",
    "/api/dashboard/map-distribution",
    "/api/dashboard/type-ratio",
    "/api/dashboard/province-top10"
]

for ep in endpoints:
    url = base_url + ep
    try:
        r = requests.get(url, timeout=3)
        print(f"GET {ep} => status: {r.status_code}")
        print("Response:", json.dumps(r.json(), ensure_ascii=False))
    except Exception as e:
        print(f"GET {ep} failed: {e}")
