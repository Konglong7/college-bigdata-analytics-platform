"""
proxy_manager.py - 全国高校大数据采集引擎 · 免费动态代理 IP 调度与管理模块
深度融合:
  1. 底层引擎: 动态集成 D:\Desktop\code\代理IP 中的 ProxyPool (多源聚合与 HTTPS 隧道验证)
  2. 运行策略: 参考 D:\Desktop\code\校园跑\260915最新xyp.py 的健壮设计:
     - 本地健康代理缓存 (快速秒级预热启动)
     - 故障代理黑名单隔离与冷却机制 (Bad Proxy Isolation)
     - 轮换重试机制 (遇到 1069 或网络错误自动切下一个)
     - 探针校验 (针对目标 API 进行连通性把关)
"""

import sys
import os
import time
import json
import logging
from pathlib import Path
from typing import List, Dict, Optional

# 动态引用外部代理池模块 (优先从环境变量读取，若目录不存在则安全降级)
PROXY_POOL_DIR = os.getenv("PROXY_POOL_DIR", r"D:\Desktop\code\代理IP")
if PROXY_POOL_DIR and os.path.isdir(PROXY_POOL_DIR) and PROXY_POOL_DIR not in sys.path:
    sys.path.insert(0, PROXY_POOL_DIR)

try:
    from proxy_pool import ProxyPool
except ImportError:
    ProxyPool = None

logger = logging.getLogger("ProxyManager")


class ProxyManager:
    """动态代理池管理器 (融合预热缓存、黑名单隔离与自动轮换)"""

    def __init__(self, cache_file: Optional[str] = None):
        self.current_dir = Path(__file__).resolve().parent
        self.cache_file = Path(cache_file) if cache_file else (self.current_dir / "proxies_cache.json")
        self.active_proxies: List[Dict] = []
        self.bad_proxies: Dict[str, float] = {}  # {ip_port: fail_timestamp}
        self.bad_cooldown_seconds = 600          # 坏代理冷却 10 分钟
        self.current_index = 0

        # 加载本地缓存
        self._load_cache()

    def _load_cache(self):
        """从本地缓存文件加载上次验证健康的代理"""
        if self.cache_file.exists():
            try:
                with open(self.cache_file, "r", encoding="utf-8") as f:
                    data = json.load(f)
                    now = time.time()
                    # 仅保留在 6 小时有效期内的健康代理
                    valid = [p for p in data if now - p.get("time", 0) < 6 * 3600]
                    self.active_proxies = valid
                    if valid:
                        logger.info(f"成功从本地预热缓存加载 {len(valid)} 个健康代理 IP")
            except Exception as e:
                logger.warning(f"读取代理缓存文件异常: {e}")

    def _save_cache(self):
        """将当前活跃健康代理持久化到本地"""
        try:
            with open(self.cache_file, "w", encoding="utf-8") as f:
                json.dump(self.active_proxies, f, ensure_ascii=False, indent=2)
        except Exception as e:
            logger.warning(f"保存代理缓存失败: {e}")

    def _is_bad(self, ip_port: str) -> bool:
        """检查代理是否在故障冷却黑名单中"""
        fail_time = self.bad_proxies.get(ip_port)
        if not fail_time:
            return False
        if time.time() - fail_time > self.bad_cooldown_seconds:
            del self.bad_proxies[ip_port]  # 冷却期过，解除黑名单
            return False
        return True

    def refresh_from_pool(self, min_ready: int = 5, timeout: int = 6):
        """调用 ProxyPool 模块并发抓取并验证支持 HTTPS 隧道的新鲜代理"""
        if ProxyPool is None:
            logger.error("未找到本地 ProxyPool 模块，无法刷新代理")
            return

        logger.info(f"正在从免费代理池模块并发聚合抓取新鲜代理 (目标预热: {min_ready} 个可用)...")
        pool = ProxyPool(workers=30, timeout=timeout, max_per_source=40, verbose=False)
        raw_candidates = pool.fetch_all()
        
        cands = []
        for items in raw_candidates.values():
            cands.extend(it["ip_port"] for it in items)
        
        # 过滤黑名单
        cands = [ip for ip in dict.fromkeys(cands) if not self._is_bad(ip)]
        logger.info(f"聚合到 {len(cands)} 个待验证候选代理，开始并发验证 HTTPS 隧道能力...")

        # 校验 HTTPS 隧道 (掌上高考 API 必须走 HTTPS CONNECT)
        batch_size = min(len(cands), 60)
        verified = pool.validate(cands[:batch_size], check_https=True)
        https_oks = [r for r in verified if r.get("https_ok") and not self._is_bad(r["ip_port"])]

        now = time.time()
        for p in https_oks:
            p["time"] = now
            if not any(item["ip_port"] == p["ip_port"] for item in self.active_proxies):
                self.active_proxies.append(p)

        logger.info(f"本次探测补充到 {len(https_oks)} 个优质 HTTPS 代理，当前代理池总存量: {len(self.active_proxies)} 个")
        self._save_cache()

    def get_current_proxy(self, auto_fetch: bool = True) -> Optional[Dict]:
        """获取当前正在生效的代理字典 (格式: {http: ..., https: ..., ip_port: ...})"""
        # 移除黑名单中的坏代理
        self.active_proxies = [p for p in self.active_proxies if not self._is_bad(p["ip_port"])]

        if not self.active_proxies and auto_fetch:
            self.refresh_from_pool(min_ready=3)

        if not self.active_proxies:
            return None

        p = self.active_proxies[0]
        ip_port = p["ip_port"]
        return {
            "ip_port": ip_port,
            "exit_ip": p.get("exit_ip", ip_port.split(":")[0]),
            "ms": p.get("ms", 0),
            "proxies": {
                "http": f"http://{ip_port}",
                "https": f"http://{ip_port}"
            }
        }

    def mark_fail(self, ip_port: str, reason: str = "error"):
        """标记某个代理失效 (例如请求超时、连接拒绝或遭遇 1069 频控)，移入隔离黑名单并轮换下一个"""
        logger.warning(f"代理 [{ip_port}] 触发异常 ({reason})，已加入黑名单隔离并自动切换下一个 IP")
        self.bad_proxies[ip_port] = time.time()
        self.active_proxies = [p for p in self.active_proxies if p["ip_port"] != ip_port]
        self._save_cache()

    def mark_success(self, ip_port: str):
        """标记代理请求成功"""
        pass
