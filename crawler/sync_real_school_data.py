"""
sync_real_school_data.py - 同步并补全全国高校真实爬虫信息与官方网站链接
1. 拉取掌上高考官方索引 name.json 与各校 info.json
2. 提取学校官网 (school_site)、招生网 (site)、电话、邮箱、地址、邮编、真实主管部门 (belong)、真实软科/QS排名、科研博士点等
3. 更新入库至 university 表
"""

import sys
import os
import time
import logging
import requests
from concurrent.futures import ThreadPoolExecutor, as_completed

sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from cleaner import DataCleaner
from db_loader import DatabaseLoader

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("SyncRealData")

NAME_LIST_URL = "https://static-data.gaokao.cn/www/2.0/school/name.json"
INFO_URL_TPL = "https://static-data.gaokao.cn/www/2.0/school/{school_id}/info.json"

def run_sync(max_schools_detail: int = 200):
    logger.info("开始获取全国高校全量索引...")
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
        "Referer": "https://www.gaokao.cn/"
    }
    resp = requests.get(NAME_LIST_URL, headers=headers, timeout=12)
    if resp.status_code != 200:
        logger.error(f"无法拉取 name.json: HTTP {resp.status_code}")
        return

    all_names = resp.json().get("data", [])
    logger.info(f"成功获取全国高校索引，共 {len(all_names)} 所")

    # 创建 school_name -> name_item 映射
    name_map = {item.get("name"): item for item in all_names if item.get("name")}

    db_loader = DatabaseLoader()
    cleaner = DataCleaner()
    conn = db_loader.get_connection()
    cur = conn.cursor()
    cur.execute("SELECT id, school_name FROM university ORDER BY id ASC;")
    db_schools = cur.fetchall()
    cur.close()
    conn.close()

    logger.info(f"数据库中当前已有 {len(db_schools)} 所高校，准备同步官方真实网站与详尽档案...")

    # 优先同步数据库中前 max_schools_detail 所重点院校，以及名称匹配的学校
    targets = []
    for db_id, sname in db_schools[:max_schools_detail]:
        n_item = name_map.get(sname)
        if n_item:
            targets.append((db_id, sname, n_item.get("school_id")))

    logger.info(f"本次选取 {len(targets)} 所核心重点高校进行深度档案与真实网站链接同步...")

    session = requests.Session()
    adapter = requests.adapters.HTTPAdapter(pool_connections=20, pool_maxsize=30, max_retries=2)
    session.mount("https://", adapter)

    def fetch_detail(db_id, sname, sid):
        url = INFO_URL_TPL.format(school_id=sid)
        try:
            r = session.get(url, headers=headers, timeout=8)
            if r.status_code == 200:
                data = r.json().get("data", {})
                if data:
                    data["school_id"] = sid
                    return db_id, sname, data
        except Exception as e:
            pass
        return db_id, sname, None

    cleaned_records = []
    with ThreadPoolExecutor(max_workers=15) as executor:
        futures = [executor.submit(fetch_detail, db_id, sname, sid) for db_id, sname, sid in targets]
        for f in as_completed(futures):
            db_id, sname, raw_detail = f.result()
            if raw_detail:
                cleaned = cleaner.clean_school_record(raw_detail)
                if cleaned:
                    cleaned_records.append(cleaned)

    logger.info(f"成功抓取并清洗 {len(cleaned_records)} 所高校的完整官方档案，准备批量更新入库...")
    if cleaned_records:
        ins, upd, _ = db_loader.load_universities(cleaned_records)
        logger.info(f"入库成功: 更新 {upd} 所重点院校真实档案与官方链接！")
        db_loader.log_crawler_task(
            source_name="掌上高考/全国高校权威名录 (官方门户与链接深度同步)",
            count=len(cleaned_records),
            status="SUCCESS"
        )

    # 另外为数据库中其他高校快速同步 raw_school_id，保证掌上高考直达链接有效
    logger.info("为数据库中其余高校补充官方 raw_school_id...")
    conn = db_loader.get_connection()
    cur = conn.cursor()
    cur.execute("SELECT id, school_name FROM university WHERE raw_school_id IS NULL;")
    missing_id_rows = cur.fetchall()
    update_batch = []
    for uid, sname in missing_id_rows:
        if sname in name_map:
            sid = name_map[sname].get("school_id")
            if sid:
                update_batch.append((sid, uid))

    if update_batch:
        cur.executemany("UPDATE university SET raw_school_id = %s WHERE id = %s;", update_batch)
        logger.info(f"成功为 {len(update_batch)} 所高校补全官方掌上高考 raw_school_id 链接标识！")
    cur.close()
    conn.close()

    print("=" * 60)
    print("★ 高校真实网站链接与官方档案增量同步完成 ★")
    print("=" * 60)

if __name__ == "__main__":
    run_sync(max_schools_detail=200)
