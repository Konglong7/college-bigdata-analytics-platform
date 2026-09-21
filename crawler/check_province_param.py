from fetcher import GaokaoFetcher
import json

fetcher = GaokaoFetcher()
# 传入 province_id="11" (北京)
scores = fetcher.fetch_school_scores(school_id=140, year=2024, province_id="11")
print("Scores count with province_id='11':", len(scores))
if scores:
    print("Sample:", json.dumps(scores[0], ensure_ascii=False))
