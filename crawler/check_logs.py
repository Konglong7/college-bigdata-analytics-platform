import pymysql

conn = pymysql.connect(
    host="localhost", port=3306, user="root", password="123456", database="univ_bigdata_db", charset="utf8mb4"
)
cursor = conn.cursor()
cursor.execute("SELECT id, source_name, collect_time, status, data_count FROM data_collect_log ORDER BY id DESC LIMIT 5;")
for row in cursor.fetchall():
    print(row)
conn.close()
