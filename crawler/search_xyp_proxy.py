import re

target_file = r"D:\Desktop\code\校园跑\260915最新xyp.py"
with open(target_file, "r", encoding="utf-8", errors="ignore") as f:
    lines = f.readlines()

print(f"Total lines: {len(lines)}")
matches = []
for idx, line in enumerate(lines):
    if any(k in line.lower() for k in ["proxy_manager", "get_proxy", "rotate_proxy", "switch_proxy", "load_proxies", "test_proxy", "fetch_proxies", "free_proxy"]):
        matches.append((idx + 1, line.strip()))

print(f"Found {len(matches)} matches:")
for lno, text in matches[:30]:
    print(f"L{lno}: {text}")
