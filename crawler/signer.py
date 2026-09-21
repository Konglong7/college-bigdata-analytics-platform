"""
signer.py - 全国高校大数据采集引擎 · 逆向签名与加解密核心模块
目标站点: 掌上高考 / 中国教育在线开放数据网关 (https://api.zjzw.cn/web/api/)
加密协议逆向:
  1. 参数按 Key 升序排序 (ASCII Order)
  2. 拼接网关前缀: "api.zjzw.cn/gkcx/api/?"
  3. HMAC-SHA1 哈希摘要 (Secret Key: "D23ABC@#56")
  4. Base64 编码
  5. 32位 MD5 哈希计算生成 signsafe 鉴权签名
"""

import hmac
import hashlib
import base64
import time
from typing import Dict, Any


class GaokaoSigner:
    """掌上高考 API 请求签名计算器与请求头生成器"""
    
    GATEWAY_DOMAIN = "api.zjzw.cn"
    GATEWAY_PREFIX = "api.zjzw.cn/gkcx/api/?"
    HMAC_KEY = b"D23ABC@#56"
    
    # 逆向提取的 AES 对称加密密钥体系 (用于复杂载荷备用通道)
    AES_KEY = b"CernetFIT4206Sun"
    AES_IV = b"XfqauTPSQHSpwJX6"

    @classmethod
    def calculate_sign(cls, params: Dict[str, Any], base_url: str = "https://api.zjzw.cn/web/api/") -> str:
        """
        根据请求参数计算合法的 signsafe 校验码
        :param params: POST 或 GET 参数字典
        :param base_url: 接口基础 URL
        :return: 32位十六进制 MD5 签名字符串
        """
        # 1. 动态获取前缀
        clean_url = base_url.replace("https://", "").replace("http://", "")
        domain = clean_url[:clean_url.find("/")]
        prefix = f"{domain}/gkcx/api/?"
        
        # 2. 对 Key 按字典序排列
        sorted_keys = sorted(params.keys())
        
        # 3. 构造 Query String
        query_parts = []
        for k in sorted_keys:
            v = params[k]
            if isinstance(v, list):
                v_str = ",".join(map(str, v))
            elif v is None:
                v_str = ""
            else:
                v_str = str(v)
            query_parts.append(f"{k}={v_str}")
            
        raw_query = prefix + "&".join(query_parts)
        
        # 4. HMAC-SHA1 签名
        hmac_digest = hmac.new(cls.HMAC_KEY, raw_query.encode("utf-8"), hashlib.sha1).digest()
        
        # 5. Base64 编码
        b64_str = base64.b64encode(hmac_digest).decode("utf-8")
        
        # 6. MD5 哈希
        sign = hashlib.md5(b64_str.encode("utf-8")).hexdigest()
        return sign

    @classmethod
    def get_headers(cls) -> Dict[str, str]:
        """构造符合浏览器特征的合法 HTTP 标头"""
        return {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Accept": "application/json, text/plain, */*",
            "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
            "Origin": "https://www.gaokao.cn",
            "Referer": "https://www.gaokao.cn/school/search",
            "Content-Type": "application/json;charset=UTF-8",
            "Connection": "keep-alive"
        }
