from fetcher import GaokaoFetcher
from cleaner import DataCleaner
import json

fetcher = GaokaoFetcher(use_proxy=True)
cleaner = DataCleaner()

# 测试清华大学 (school_id=140)
# 当时在 run_crawler.py 中：province_id = cleaner.PROVINCE_CODE_MAP.get(school["province"], "11")
# 清华大学在 raw 数据里的 province_name 是 "北京"，clean_province_name 之后是 "北京"
# 传 province_id="11" (北京) 时，掌上高考是否有数据？还是省份代码不对？
res = fetcher.fetch_school_scores(school_id=140, year=2024, province_id="11")
print("Response for school_id=140, year=2024, province_id=11:")
print("Count:", len(res))
if res:
    print("Item sample:", json.dumps(res[0], ensure_ascii=False, indent=2))
else:
    # 尝试不传 province_id
    res_all = fetcher.fetch_school_scores(school_id=140, year=2024, province_id="")
    print("Count without province_id:", len(res_all))
    if res_all:
        print("Item sample without province_id:", json.dumps(res_all[0], ensure_ascii=False, indent=2))
