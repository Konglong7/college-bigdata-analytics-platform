import time
import requests
from signer import GaokaoSigner

params = {
    "keyword": "",
    "page": 1,
    "province_id": "",
    "school_type": "",
    "size": 1,
    "type": "",
    "uri": "apidata/api/gkv3/school/lists"
}
api_url = "https://api.zjzw.cn/web/api/"
sign = GaokaoSigner.calculate_sign(params, api_url)
payload = {**params, "signsafe": sign}
headers = GaokaoSigner.get_headers()

print("Waiting for rate limit window reset...")
for i in range(12):
    time.sleep(5)
    r = requests.post(api_url, json=payload, headers=headers)
    print(f"Check at +{(i+1)*5}s: status={r.status_code}, body={r.text[:80]}")
    if '"code":"0000"' in r.text:
        print("Cooling period completed, API restored!")
        break
