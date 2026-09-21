import time
from fetcher import GaokaoFetcher

fetcher = GaokaoFetcher()
# 测试高校列表接口是否受影响
schools = fetcher.fetch_school_page(page=1, size=2)
print("School list request success, count:", len(schools))
if schools:
    print("School name:", schools[0]["name"])
