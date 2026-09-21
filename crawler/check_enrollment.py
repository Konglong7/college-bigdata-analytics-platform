import pymysql

conn = pymysql.connect(
    host="localhost", port=3306, user="root", password="123456", database="univ_bigdata_db", charset="utf8mb4"
)
cursor = conn.cursor()
cursor.execute("SELECT COUNT(*) FROM enrollment;")
count = cursor.fetchone()[0]
print("Current total enrollments in DB:", count)

cursor.execute("""
    SELECT e.id, u.school_name, e.year, e.score, e.plan_number 
    FROM enrollment e 
    JOIN university u ON e.university_id = u.id 
    ORDER BY e.id DESC LIMIT 10;
""")
print("\nSample enrollment records:")
for row in cursor.fetchall():
    print(f"ID: {row[0]}, School: {row[1]}, Year: {row[2]}, Score: {row[3]}, Plan: {row[4]}")

conn.close()
