import pymysql

conn = pymysql.connect(
    host="localhost", port=3306, user="root", password="123456", database="univ_bigdata_db", charset="utf8mb4"
)
cursor = conn.cursor()
cursor.execute("SELECT COUNT(*) FROM university;")
total = cursor.fetchone()[0]
print(f"Current total universities in DB: {total}")

cursor.execute("SELECT province, COUNT(1) as c FROM university GROUP BY province ORDER BY c DESC LIMIT 10;")
print("\nTop 10 provinces distribution:")
for row in cursor.fetchall():
    print(f"  {row[0]}: {row[1]}")

cursor.execute("SELECT school_type, COUNT(1) as c FROM university GROUP BY school_type;")
print("\nSchool type distribution:")
for row in cursor.fetchall():
    print(f"  {row[0]}: {row[1]}")

cursor.execute("SELECT school_name, province, school_level, school_type FROM university ORDER BY id DESC LIMIT 5;")
print("\nNewly crawled sample universities:")
for row in cursor.fetchall():
    print(f"  {row[0]} | {row[1]} | {row[2]} | {row[3]}")

conn.close()
