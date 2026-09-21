import sys
import os

PROXY_DIR = r"D:\Desktop\code\代理IP"
if PROXY_DIR not in sys.path:
    sys.path.append(PROXY_DIR)

from proxy_pool import ProxyPool
from signer import GaokaoSigner
import requests

print("=== 测试代理池集成与动态 IP 切换 ===")
# 实例化代理池
pool = ProxyPool(workers=30, timeout=5, max_per_source=30, verbose=True)

print("正在聚合免费代理源并验证 HTTPS 隧道能力...")
data = pool.fetch_all()
all_cands = []
for items in data.values():
    all_cands.extend(it["ip_port"] for it in items)

print(f"共获取候选代理: {len(all_cands)} 个，开始测试前 30 个的 HTTPS 连通性...")
results = pool.validate(all_cands[:30], check_https=True)
https_oks = [r for r in results if r.get("https_ok")]

print(f"验证出支持 HTTPS 隧道的可行代理: {len(https_oks)} 个")
if https_oks:
    target_proxy = https_oks[0]
    ip_port = target_proxy["ip_port"]
    print(f"\n[测试] 选用最快代理: {ip_port} (延迟: {target_proxy['ms']}ms, 出口IP: {target_proxy['exit_ip']})")
    
    # 构造掌上高考测试请求
    api_url = "https://api.zjzw.cn/web/api/"
    params = {
        "keyword": "",
        "page": 1,
        "province_id": "",
        "school_type": "",
        "size": 5,
        "type": "",
        "uri": "apidata/api/gkv3/school/lists"
    }
    sign = GaokaoSigner.calculate_sign(params, api_url)
    payload = {**params, "signsafe": sign}
    headers = GaokaoSigner.get_headers()
    proxies = {
        "http": f"http://{ip_port}",
        "https": f"http://{ip_port}"
    }
    
    try:
        print(f"正在通过代理 {ip_port} 穿透请求掌上高考接口...")
        resp = requests.post(api_url, json=payload, headers=headers, proxies=proxies, timeout=10, verify=False)
        print("代理请求状态码:", resp.status_code)
        print("响应前缀:", resp.text[:150])
    except Exception as e:
        print("代理请求异常:", e)
else:
    print("未在当前候选样本中命中可用 HTTPS 代理，建议增加验证样本量")
