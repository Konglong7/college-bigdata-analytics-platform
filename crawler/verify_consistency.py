import requests

test_ids = [1, 2, 3, 9]
for uid in test_ids:
    url = f"http://localhost:8088/api/university/{uid}/detail"
    res = requests.get(url).json()
    data = res.get("data")
    print("=" * 60)
    print(f"校验高校: {data['schoolName']} (ID: {uid})")
    print(f"  - 教育部代码: {data['schoolCode']}")
    print(f"  - 真实主管部门: {data['department']}")
    print(f"  - 办学性质: {data['natureName']}")
    print(f"  - 软科综合排位: 第 {data['ruankeRank']} 名")
    print(f"  - 真实学校官网: {data['schoolSite']}")
    print(f"  - 本科招生官网: {data['site']}")
    print(f"  - 掌上高考档案: {data['gaokaoSite']}")
    print(f"  - 掌上高考分数线: {data['gaokaoScoreSite']}")
    print(f"  - 招生电话: {data['phone']}")
    print(f"  - 招生邮箱: {data['email']}")
    print(f"  - 校区地址: {data['address'][:40]}...")
    print(f"  - 院士/实验室/博士点: {data['numAcademician']}人 / {data['numLab']}个 / {data['numDoctor']}个")
    print(f"  - 真实分数线系列: {data['scoreSeries']}")
