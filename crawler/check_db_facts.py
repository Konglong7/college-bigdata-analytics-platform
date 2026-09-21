import pymysql

conn = pymysql.connect(host='localhost', port=3306, user='root', password='123456', database='univ_bigdata_db', charset='utf8mb4')
cur = conn.cursor()

cur.execute('SELECT COUNT(*) FROM university;')
total_univ = cur.fetchone()[0]

cur.execute("SELECT COUNT(*) FROM university WHERE school_level LIKE '%本科%' OR school_level LIKE '%985%' OR school_level LIKE '%211%' OR school_level LIKE '%双一流%';")
total_bk = cur.fetchone()[0]

cur.execute('SELECT COUNT(DISTINCT province) FROM university;')
total_prov = cur.fetchone()[0]

cur.execute('SELECT COUNT(*) FROM major;')
total_major_records = cur.fetchone()[0]

cur.execute('SELECT COUNT(DISTINCT major_name) FROM major;')
distinct_majors = cur.fetchone()[0]

cur.execute('SELECT COUNT(*) FROM enrollment;')
total_enroll = cur.fetchone()[0]

cur.execute('SELECT COUNT(DISTINCT university_id) FROM enrollment;')
enroll_univs = cur.fetchone()[0]

cur.execute('SELECT COUNT(*) FROM data_collect_log;')
total_collect_log = cur.fetchone()[0]

cur.execute('SELECT COUNT(*) FROM data_clean_log;')
total_clean_log = cur.fetchone()[0]

print('=== 数据库真实事实数据全景 ===')
print(f'高校总数: {total_univ} 所')
print(f'本科高校数: {total_bk} 所')
print(f'覆盖省份地区: {total_prov} 个')
print(f'专业开设记录: {total_major_records} 条, 独立专业名称: {distinct_majors} 种')
print(f'录取招生记录: {total_enroll} 条, 覆盖重点高校: {enroll_univs} 所')
print(f'采集日志: {total_collect_log} 条, 清洗日志: {total_clean_log} 条')

print('\nTop 5 开设最广专业:')
cur.execute('SELECT major_name, category, COUNT(DISTINCT university_id) as c, ROUND(AVG(employment_rate), 2) as avg_rate FROM major GROUP BY major_name, category ORDER BY c DESC LIMIT 5;')
for r in cur.fetchall():
    print(f'  {r[0]} ({r[1]}): {r[2]} 所高校开设, 平均就业率: {r[3]}%')

print('\n历年真实招生与录取总额 (enrollment 表):')
cur.execute('SELECT year, SUM(plan_number), SUM(admission_number), ROUND(AVG(score), 1) FROM enrollment GROUP BY year ORDER BY year;')
for r in cur.fetchall():
    print(f'  {r[0]} 年: 计划 {r[1]} 人, 录取 {r[2]} 人, 平均最低调档线: {r[3]} 分')

conn.close()
