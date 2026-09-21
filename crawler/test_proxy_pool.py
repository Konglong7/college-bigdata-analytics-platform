import sys
import os

# 引入代理池项目
PROXY_PROJECT_PATH = r"D:\Desktop\code\代理IP"
if PROXY_PROJECT_PATH not in sys.path:
    sys.path.append(PROXY_PROJECT_PATH)

try:
    from proxy_pool import ProxyPool
    print("ProxyPool imported successfully!")
    
    # 快速抓取少量候选代理并验证 HTTPS
    pool = ProxyPool(workers=20, timeout=6, verbose=True)
    cands = pool.fetch_all(max_per_source=20)
    print(f"Fetched candidates: {len(cands)}")
    
    # 验证 HTTPS
    print("Validating with HTTPS check...")
    results = pool.validate(cands[:40], check_https=True)
    https_proxies = [r for r in results if r.get("https_ok")]
    print(f"HTTPS capable proxies: {len(https_proxies)}")
    for p in https_proxies[:5]:
        print(f"  {p['ip_port']} - exit_ip: {p['exit_ip']} - latency: {p['ms']}ms")
        
except Exception as e:
    print("Error:", e)
