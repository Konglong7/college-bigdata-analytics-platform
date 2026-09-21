import requests
import json

base_url = "https://static-data.gaokao.cn"
candidates = [
    "/www/2.0/school/name.json",
    "/www/2.0/school/all.json",
    "/www/2.0/school/lists.json",
    "/www/2.0/school/school_name.json",
    "/www/2.0/json/school/all.json",
    "/www/2.0/schools.json",
    "/www/2.0/school/rank/2024.json",
    "/www/2.0/school/140/info.json",
    "/www/2.0/school/140/pc_special.json"
]

headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    "Referer": "https://www.gaokao.cn/"
}

for path in candidates:
    url = base_url + path
    try:
        r = requests.get(url, headers=headers, timeout=3)
        print(f"{path} => status: {r.status_code}, length: {len(r.content)}")
        if r.status_code == 200:
            print("Preview:", r.text[:200])
    except Exception as e:
        print(f"{path} => error: {e}")
