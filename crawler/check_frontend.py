import requests

for port in [5173, 3000, 8080]:
    try:
        r = requests.get(f"http://localhost:{port}", timeout=2)
        print(f"Port {port}: status={r.status_code}")
    except Exception as e:
        print(f"Port {port}: not reachable")
