from fetcher import GaokaoFetcher
import json

fetcher = GaokaoFetcher()
scores = fetcher.fetch_school_scores(school_id=140, year=2024)
print("Returned scores count:", len(scores))
if scores:
    print("First item keys:", scores[0].keys())
    print("First item sample:", json.dumps(scores[0], ensure_ascii=False, indent=2))
