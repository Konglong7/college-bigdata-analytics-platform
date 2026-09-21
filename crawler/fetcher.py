"""
fetcher.py - 全国高校大数据采集引擎 · 高可用网络抓取器
包含:
  1. 全国高校名录多维检索分页抓取
  2. 高校历年真实录取分数线 (2023-2024) 批量采集
  3. 请求限频流控与自动重试机制
"""

import time
import random
import logging
import requests
from typing import List, Dict, Any, Optional
from signer import GaokaoSigner
from proxy_manager import ProxyManager

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("CrawlerFetcher")


class GaokaoFetcher:
    API_URL = "https://api.zjzw.cn/web/api/"
    
    def __init__(self, delay_range: tuple = (0.3, 0.8), use_proxy: bool = False):
        self.session = requests.Session()
        self.headers = GaokaoSigner.get_headers()
        self.delay_range = delay_range
        self.use_proxy = use_proxy
        self.proxy_mgr = ProxyManager() if use_proxy else None

    def _post(self, params: Dict[str, Any], max_retries: int = 3) -> Optional[Dict[str, Any]]:
        """计算签名并发送 POST 请求，包含自适应退避与动态代理 IP 自动轮换"""
        sign = GaokaoSigner.calculate_sign(params, self.API_URL)
        payload = {
            **params,
            "signsafe": sign
        }

        for attempt in range(1, max_retries + 1):
            cur_proxy = None
            proxies_arg = None
            if self.use_proxy and self.proxy_mgr:
                cur_proxy = self.proxy_mgr.get_current_proxy(auto_fetch=True)
                if cur_proxy:
                    proxies_arg = cur_proxy["proxies"]
                    logger.info(f"使用动态代理出口: {cur_proxy['ip_port']} (延迟: {cur_proxy.get('ms', 0)}ms)")

            try:
                # 随机友好延时，防高频拦截
                time.sleep(random.uniform(*self.delay_range))
                resp = self.session.post(
                    self.API_URL, 
                    json=payload, 
                    headers=self.headers, 
                    proxies=proxies_arg,
                    timeout=12,
                    verify=False
                )
                
                if resp.status_code == 200:
                    data = resp.json()
                    if data.get("code") == "0000":
                        if cur_proxy:
                            self.proxy_mgr.mark_success(cur_proxy["ip_port"])
                        return data.get("data")
                    elif data.get("code") == "1069":
                        if cur_proxy:
                            # 代理遇到 1069，立即隔离并轮换下一个 IP，无须等待
                            logger.warning(f"代理 [{cur_proxy['ip_port']}] 触发限频 (code 1069)，立即切换下一个动态 IP...")
                            self.proxy_mgr.mark_fail(cur_proxy["ip_port"], reason="1069_rate_limit")
                        else:
                            wait_sec = attempt * 8
                            logger.warning(f"本机直连触发频控 (code 1069)，执行自适应退避休眠 {wait_sec} 秒后重试...")
                            time.sleep(wait_sec)
                    else:
                        logger.warning(f"业务响应非成功码: {data.get('code')} - {data.get('message')}")
                else:
                    logger.warning(f"HTTP 状态码异常: {resp.status_code}, 第 {attempt} 次尝试")
                    if cur_proxy:
                        self.proxy_mgr.mark_fail(cur_proxy["ip_port"], reason=f"http_{resp.status_code}")
            except Exception as e:
                logger.error(f"网络请求发生异常: {e}, 第 {attempt} 次重试")
                if cur_proxy:
                    self.proxy_mgr.mark_fail(cur_proxy["ip_port"], reason=str(e))
                time.sleep(attempt * 1.5)
        
        return None

    def fetch_school_page(self, page: int = 1, size: int = 30, province_id: str = "", school_type: str = "") -> List[Dict[str, Any]]:
        """
        分页获取全国高校列表
        :param page: 页码
        :param size: 每页数量
        :param province_id: 省份代码 (可选)
        :param school_type: 院校类型 (可选)
        :return: 高校记录列表
        """
        params = {
            "keyword": "",
            "page": page,
            "province_id": province_id,
            "school_type": school_type,
            "size": size,
            "type": "",
            "uri": "apidata/api/gkv3/school/lists"
        }
        data = self._post(params)
        if data and "item" in data:
            return data["item"]
        return []

    def fetch_school_scores(self, school_id: int, year: int = 2024, province_id: str = "") -> List[Dict[str, Any]]:
        """
        获取单所高校某年份在各省份的录取投档分数线
        :param school_id: 掌上高考学校唯一 ID
        :param year: 年份 (例如 2024, 2023)
        :param province_id: 招生省份 (留空表示全部)
        :return: 投档分数线列表
        """
        params = {
            "page": 1,
            "province_id": province_id,
            "school_id": school_id,
            "size": 10,
            "uri": "apidata/api/gk/score/province",
            "year": str(year)
        }
        data = self._post(params)
        if isinstance(data, dict) and "item" in data:
            return data["item"]
        elif isinstance(data, list):
            return data
        return []
