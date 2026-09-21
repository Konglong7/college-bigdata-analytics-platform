"""
db_loader.py - 全国高校大数据采集引擎 · 数据持久化与数据库写入器
支持:
  1. 高校主表 (university) 的增量更新与安全入库 (Upsert)
  2. 分数线从表 (enrollment) 的批量挂载写入
  3. 真实采集与清洗监控日志写入 (data_collect_log, data_clean_log)
"""

import pymysql
import logging
from typing import List, Dict, Any, Tuple

logger = logging.getLogger("DbLoader")


class DatabaseLoader:
    def __init__(self, host="localhost", port=3306, user="root", password="123456", db="univ_bigdata_db"):
        self.conn_params = {
            "host": host,
            "port": port,
            "user": user,
            "password": password,
            "database": db,
            "charset": "utf8mb4",
            "autocommit": True
        }

    def get_connection(self):
        return pymysql.connect(**self.conn_params)

    @classmethod
    def setup_demo_database(cls, host="localhost", port=3306, user="root", password="123456", db_name="univ_demo_db", reset=True) -> "DatabaseLoader":
        """
        专为演示设计：自动创建全新的测试演示数据库并初始化 DDL 结构
        :param reset: 若为 True，则先 DROP 再 CREATE，确保演示前库是 100% 纯净空库
        """
        import os
        logger.info(f"正在配置独立演示数据库 [{db_name}] (reset={reset})...")
        conn = pymysql.connect(host=host, port=port, user=user, password=password, charset="utf8mb4", autocommit=True)
        cursor = conn.cursor()
        try:
            if reset:
                cursor.execute(f"DROP DATABASE IF EXISTS `{db_name}`;")
            cursor.execute(f"CREATE DATABASE IF NOT EXISTS `{db_name}` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")
            cursor.execute(f"USE `{db_name}`;")
            
            # 读取项目根目录的 sql/01_schema.sql
            schema_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "sql", "01_schema.sql")
            if os.path.exists(schema_path):
                with open(schema_path, "r", encoding="utf-8") as f:
                    content = f.read()
                statements = [s.strip() for s in content.split(";") if s.strip()]
                for stmt in statements:
                    if stmt.upper().startswith("CREATE DATABASE") or stmt.upper().startswith("USE "):
                        continue
                    try:
                        cursor.execute(stmt)
                    except Exception:
                        pass
            logger.info(f"测试演示数据库 [{db_name}] 初始化完成，DDL 表结构已就绪，当前表中无任何数据！")
        finally:
            cursor.close()
            conn.close()

        return cls(host=host, port=port, user=user, password=password, db=db_name)

    def load_universities(self, school_records: List[Dict[str, Any]]) -> Tuple[int, int, Dict[str, int]]:
        """
        高性能批量写入高校列表至 university 表 (基于内存缓存与批量操作)
        :return: (插入新高校数, 更新高校数, {school_name: db_id} 映射)
        """
        if not school_records:
            return 0, 0, {}

        conn = self.get_connection()
        cursor = conn.cursor()
        inserted_count = 0
        updated_count = 0
        name_id_map = {}

        try:
            # 1. 一次性获取已存在的全部学校映射，消除 N 次单条查询
            cursor.execute("SELECT school_name, id FROM university;")
            for r in cursor.fetchall():
                name_id_map[r[0]] = r[1]

            to_insert = []
            to_update = []

            for s in school_records:
                name = s["school_name"]
                raw_school_id = s.get("raw_school_id")
                belong = s.get("belong")
                nature_name = s.get("nature_name", "公办")
                dual_class_name = s.get("dual_class_name")
                school_site = s.get("school_site")
                site = s.get("site")
                phone = s.get("phone")
                email = s.get("email")
                address = s.get("address")
                postcode = s.get("postcode")
                ruanke_rank = s.get("ruanke_rank")
                qs_rank = s.get("qs_rank")
                xyh_rank = s.get("xyh_rank")
                num_doctor = s.get("num_doctor", 0)
                num_master = s.get("num_master", 0)
                num_academician = s.get("num_academician", 0)
                num_library = s.get("num_library")
                num_lab = s.get("num_lab", 0)

                if name in name_id_map:
                    db_id = name_id_map[name]
                    to_update.append((
                        s["school_code"], s["province"], s["city"],
                        s["school_type"], s["school_level"], s["establish_year"],
                        s["introduction"], raw_school_id, belong, nature_name,
                        dual_class_name, school_site, site, phone, email,
                        address, postcode, ruanke_rank, qs_rank, xyh_rank,
                        num_doctor, num_master, num_academician, num_library, num_lab,
                        db_id
                    ))
                else:
                    to_insert.append((
                        name, s["school_code"], s["province"], s["city"],
                        s["school_type"], s["school_level"], s["establish_year"],
                        s["introduction"], raw_school_id, belong, nature_name,
                        dual_class_name, school_site, site, phone, email,
                        address, postcode, ruanke_rank, qs_rank, xyh_rank,
                        num_doctor, num_master, num_academician, num_library, num_lab
                    ))

            # 批量执行 UPDATE
            if to_update:
                cursor.executemany("""
                    UPDATE university 
                    SET school_code = %s, province = %s, city = %s, 
                        school_type = %s, school_level = %s, establish_year = %s,
                        introduction = %s, raw_school_id = %s, belong = %s, nature_name = %s,
                        dual_class_name = %s, school_site = %s, site = %s, phone = %s, email = %s,
                        address = %s, postcode = %s, ruanke_rank = %s, qs_rank = %s, xyh_rank = %s,
                        num_doctor = %s, num_master = %s, num_academician = %s, num_library = %s, num_lab = %s
                    WHERE id = %s;
                """, to_update)
                updated_count = len(to_update)

            # 批量执行 INSERT
            if to_insert:
                cursor.executemany("""
                    INSERT INTO university (
                        school_name, school_code, province, city,
                        school_type, school_level, establish_year, introduction,
                        raw_school_id, belong, nature_name, dual_class_name,
                        school_site, site, phone, email, address, postcode,
                        ruanke_rank, qs_rank, xyh_rank, num_doctor, num_master,
                        num_academician, num_library, num_lab, create_time
                    )
                    VALUES (
                        %s, %s, %s, %s, %s, %s, %s, %s,
                        %s, %s, %s, %s, %s, %s, %s, %s, %s, %s,
                        %s, %s, %s, %s, %s, %s, %s, %s, NOW()
                    );
                """, to_insert)
                inserted_count = len(to_insert)

            # 重新同步新插入的 ID 映射
            if to_insert:
                cursor.execute("SELECT school_name, id FROM university;")
                for r in cursor.fetchall():
                    name_id_map[r[0]] = r[1]

            logger.info(f"高校表批量同步完成: 新增 {inserted_count} 所, 更新 {updated_count} 所 (当前系统高校总数: {len(name_id_map)})")
            return inserted_count, updated_count, name_id_map

        finally:
            cursor.close()
            conn.close()

    def load_majors_batch(self, major_records: List[Dict[str, Any]]) -> int:
        """批量录入代表性热门专业数据"""
        if not major_records:
            return 0
        conn = self.get_connection()
        cursor = conn.cursor()
        try:
            # 清理或覆盖已有同名专业
            cursor.executemany("""
                INSERT INTO major (university_id, major_name, category, employment_rate)
                VALUES (%s, %s, %s, %s)
                ON DUPLICATE KEY UPDATE employment_rate = VALUES(employment_rate);
            """, [(m["university_id"], m["major_name"], m["category"], m["employment_rate"]) for m in major_records])
            return len(major_records)
        except Exception as e:
            logger.warning(f"专业表批量录入警告: {e}")
            return 0
        finally:
            cursor.close()
            conn.close()

    def load_enrollments(self, score_records: List[Dict[str, Any]]) -> int:
        """
        批量插入或更新录取分数记录至 enrollment 表
        """
        if not score_records:
            return 0

        conn = self.get_connection()
        cursor = conn.cursor()
        saved_count = 0

        try:
            for r in score_records:
                # 检查同一所高校同一年份是否已存在
                cursor.execute("""
                    SELECT id FROM enrollment 
                    WHERE university_id = %s AND year = %s 
                    LIMIT 1;
                """, (r["university_id"], r["year"]))
                row = cursor.fetchone()

                if row:
                    cursor.execute("""
                        UPDATE enrollment 
                        SET score = %s, plan_number = %s, admission_number = %s 
                        WHERE id = %s;
                    """, (r["score"], r["plan_number"], r["admission_number"], row[0]))
                else:
                    cursor.execute("""
                        INSERT INTO enrollment (university_id, year, plan_number, admission_number, score)
                        VALUES (%s, %s, %s, %s, %s);
                    """, (r["university_id"], r["year"], r["plan_number"], r["admission_number"], r["score"]))
                saved_count += 1

            logger.info(f"录取分数线记录同步完成: 共 {saved_count} 条")
            return saved_count

        finally:
            cursor.close()
            conn.close()

    def log_crawler_task(self, source_name: str, count: int, status: str = "SUCCESS"):
        """记录采集调度日志"""
        conn = self.get_connection()
        cursor = conn.cursor()
        try:
            cursor.execute("""
                INSERT INTO data_collect_log (source_name, collect_time, status, data_count)
                VALUES (%s, NOW(), %s, %s);
            """, (source_name, status, count))
        finally:
            cursor.close()
            conn.close()

    def log_clean_task(self, raw_count: int, clean_count: int, error_count: int):
        """记录清洗管道执行日志"""
        conn = self.get_connection()
        cursor = conn.cursor()
        try:
            cursor.execute("""
                INSERT INTO data_clean_log (raw_count, clean_count, error_count, clean_time)
                VALUES (%s, %s, %s, NOW());
            """, (raw_count, clean_count, error_count))
        finally:
            cursor.close()
            conn.close()
