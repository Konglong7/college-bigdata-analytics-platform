"""
run_crawler.py - 全国高校大数据平台 · 逆向数据采集与入库统一执行主程序
执行流程:
  Phase 1: 逆向签名鉴权初始化与参数装配
  Phase 2: 全国高校真实名录多页分页抓取
  Phase 3: ETL 清洗与字段规范化 (省份/层次/代码)
  Phase 4: 高校主表持久化至本地 MySQL (university 表)
  Phase 5: 重点代表院校 2023-2024 真实投档录取线并发采集与挂载
  Phase 6: 记录系统采集与清洗监控事实日志
"""

import sys
import os
import time
import logging

# 确保能正确引用当前目录下的模块
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from fetcher import GaokaoFetcher
from cleaner import DataCleaner
from db_loader import DatabaseLoader

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s"
)
logger = logging.getLogger("MainCrawler")


def run_pipeline(total_pages: int = 5, fetch_scores: bool = True, score_limit_schools: int = 20, use_proxy: bool = True):
    print("=" * 70)
    print("★ 全国高校大数据分析可视化平台 - 协议逆向数据采集引擎启动 ★")
    print("★ 加密防护突破: JS Webpack 混淆逆向 · HMAC-SHA1 + Base64 + MD5 复合签名")
    print("★ 采集目标数据源: 中国教育在线 / 掌上高考官方核心数据网关")
    if use_proxy:
        print("★ 动态网络拓扑: 启用免费动态代理 IP 自动轮换 (基于本地 ProxyPool 模块)")
        print("★ 容灾与隔离策略: 预热缓存 + 故障黑名单冷却 + 1069 频控瞬时换 IP 切换")
    print("=" * 70)

    fetcher = GaokaoFetcher(delay_range=(0.3, 0.8), use_proxy=use_proxy)
    cleaner = DataCleaner()
    db_loader = DatabaseLoader()

    raw_schools_all = []
    logger.info(f"==> 阶段 1: 开始抓取全国高校基础名录 (计划抓取 {total_pages} 页，约 {total_pages * 30} 所高校)...")

    for page in range(1, total_pages + 1):
        logger.info(f"正在逆向抓取第 {page}/{total_pages} 页...")
        items = fetcher.fetch_school_page(page=page, size=30)
        if not items:
            logger.warning(f"第 {page} 页未获取到数据，提前终止高校抓取")
            break
        raw_schools_all.extend(items)
        logger.info(f"第 {page} 页抓取成功，当前累计获取原始高校数据: {len(raw_schools_all)} 条")

    logger.info(f"==> 阶段 2: 数据清洗管道执行中...")
    cleaned_schools = []
    error_count = 0

    for raw in raw_schools_all:
        cleaned = cleaner.clean_school_record(raw)
        if cleaned:
            cleaned_schools.append(cleaned)
        else:
            error_count += 1

    logger.info(f"清洗完成: 原始 {len(raw_schools_all)} 条, 有效 {len(cleaned_schools)} 条, 异常过滤 {error_count} 条")

    logger.info("==> 阶段 3: 写入本地 MySQL 数据库 (university 表)...")
    inserted, updated, name_id_map = db_loader.load_universities(cleaned_schools)

    # 记录采集与清洗日志
    db_loader.log_crawler_task(
        source_name="掌上高考/全国高校权威名录 (逆向协议采集)",
        count=len(cleaned_schools),
        status="SUCCESS"
    )
    db_loader.log_clean_task(
        raw_count=len(raw_schools_all),
        clean_count=len(cleaned_schools),
        error_count=error_count
    )

    cleaned_scores_all = []
    # 阶段 4: 采集真实分数线数据
    if fetch_scores and cleaned_schools:
        logger.info(f"==> 阶段 4: 开始采集前 {score_limit_schools} 所代表院校的 2023-2024 年真实录取分数线...")
        raw_scores_count = 0

        target_schools = cleaned_schools[:score_limit_schools]
        for idx, school in enumerate(target_schools, 1):
            school_name = school["school_name"]
            raw_id = school["raw_school_id"]
            local_id = name_id_map.get(school_name)

            if not raw_id or not local_id:
                continue

            province_id = cleaner.PROVINCE_CODE_MAP.get(school["province"], "11")
            logger.info(f"[{idx}/{len(target_schools)}] 抓取《{school_name}》最新各省投档录取分数 (省份代码: {province_id})...")

            # 抓取 2024 年数据
            scores_2024 = fetcher.fetch_school_scores(school_id=raw_id, year=2024, province_id=province_id)
            raw_scores_count += len(scores_2024)
            valid_2024 = [cleaner.clean_score_record(sc, local_id) for sc in scores_2024]
            valid_2024 = [sc for sc in valid_2024 if sc]
            if valid_2024:
                # 优先选择 750 分制常态省份作为展示基准分
                std_scores = [sc for sc in valid_2024 if sc["score"] <= 750]
                chosen_2024 = std_scores[0] if std_scores else valid_2024[0]
                cleaned_scores_all.append(chosen_2024)

            time.sleep(0.4)

            # 抓取 2023 年数据作为对照
            scores_2023 = fetcher.fetch_school_scores(school_id=raw_id, year=2023, province_id=province_id)
            raw_scores_count += len(scores_2023)
            valid_2023 = [cleaner.clean_score_record(sc, local_id) for sc in scores_2023]
            valid_2023 = [sc for sc in valid_2023 if sc]
            if valid_2023:
                std_scores = [sc for sc in valid_2023 if sc["score"] <= 750]
                chosen_2023 = std_scores[0] if std_scores else valid_2023[0]
                cleaned_scores_all.append(chosen_2023)

            time.sleep(0.4)

        logger.info(f"录取分数线清洗入库中 (有效记录: {len(cleaned_scores_all)} 条)...")
        saved_scores = db_loader.load_enrollments(cleaned_scores_all)

        db_loader.log_crawler_task(
            source_name="全国高校历年高考调档录取线 (2023-2024)",
            count=saved_scores,
            status="SUCCESS"
        )

    print("\n" + "=" * 70)
    print("★ 全国高校数据逆向爬虫执行完毕！★")
    print(f"★ 高校总入库/更新: 新增 {inserted} 所, 更新 {updated} 所")
    if fetch_scores:
        print(f"★ 真实最新录取分数线入库: {len(cleaned_scores_all)} 条")
    print("=" * 70)


if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description="全国高校大数据协议逆向采集引擎")
    parser.add_argument("--pages", type=int, default=3, help="抓取高校页数 (默认3页，约90所代表高校)")
    parser.add_argument("--no-scores", action="store_true", help="跳过录取分数线抓取")
    parser.add_argument("--proxy", action="store_true", default=False, help="启用免费动态代理 IP 池")
    parser.add_argument("--full", action="store_true", default=False, help="全量采集全国 2900+ 所高校及详细档案")
    parser.add_argument("--demo", action="store_true", default=False, help="启动快速验证与演示模式 (自动新建独立沙箱数据库)")
    parser.add_argument("--target-db", type=str, default="univ_demo_db", help="演示模式目标数据库名 (默认: univ_demo_db)")
    args = parser.parse_args()

    if args.demo:
        from demo_crawler import run_demo_pipeline
        run_demo_pipeline(target_db=args.target_db, fetch_scores_count=8, use_proxy=args.proxy)
    elif args.full:
        from full_pipeline import FullUniversityPipeline
        pipeline = FullUniversityPipeline(max_workers=15)
        pipeline.run(fetch_scores=not args.no_scores, score_limit=25)
    else:
        run_pipeline(
            total_pages=args.pages, 
            fetch_scores=not args.no_scores, 
            score_limit_schools=15, 
            use_proxy=args.proxy
        )
