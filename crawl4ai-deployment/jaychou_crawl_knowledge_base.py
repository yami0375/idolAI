#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Jay Chou 知识库全量采集 · Crawl4AI 0.7.4 单文件版
针对 0.7.4 已修复 timeout 位置错误
"""
import asyncio, json, os, hashlib, time, random, re
from datetime import datetime
from crawl4ai import AsyncWebCrawler, BrowserConfig, CrawlerRunConfig, CacheMode
from crawl4ai.content_filter_strategy import PruningContentFilter
from crawl4ai.markdown_generation_strategy import DefaultMarkdownGenerator
from playwright.async_api import TimeoutError as PWTimeout

# -------------------- 1. 配置 --------------------
CFG = {
    "headless": True,
    "proxy": None,                # {"server": "http://127.0.0.1:7890"}
    "out_dir": "kb_raw",
    "state_db": "kb_raw/.state.json",
    "timeout": 45_000,            # ms → 挂到 arun
    "deep_types": {"basic", "story", "music"},   # 全量展开
    "light_types": {"social"},                   # 只滚 1 次
    "scroll_pause": 2,
}

# 目标清单
TARGETS = [
    {"name": "wiki_zh",   "url": "https://zh.wikipedia.org/wiki/周杰伦",        "type": "basic"},
    {"name": "baidu_bk",  "url": "https://baike.baidu.com/item/周杰伦/129156", "type": "basic"},
    {"name": "douban_music", "url": "https://music.douban.com/subject_search?search_text=周杰伦", "type": "music"},
    {"name": "weibo",     "url": "https://m.weibo.cn/u/1165631310",             "type": "social"},
]

# -------------------- 2. 工具 --------------------
def load_state():
    return json.load(open(CFG["state_db"])) if os.path.exists(CFG["state_db"]) else {}

def save_state(state):
    os.makedirs(CFG["out_dir"], exist_ok=True)
    json.dump(state, open(CFG["state_db"], "w", encoding="utf-8"), ensure_ascii=False, indent=2)

def fp(url):
    return hashlib.md5(url.encode()).hexdigest()

def remove_links(text):
    """移除Markdown中的链接，只保留链接文本"""
    # 移除图片链接 ![alt](url)
    text = re.sub(r'!\[([^\]]*)\]\([^)]+\)', r'\1', text)
    # 移除普通链接 [text](url)，只保留text
    text = re.sub(r'\[([^\]]+)\]\([^)]+\)', r'\1', text)
    # 移除自动链接 <url>
    text = re.sub(r'<[^>]+>', '', text)
    return text

# -------------------- 3. 钩子 --------------------
async def deep_expand(page):
    """百科/故事：展开到尽头"""
    last_height = 0
    while True:
        await page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
        await asyncio.sleep(CFG["scroll_pause"])
        # 点所有「展开」「更多」
        for sel in ["text=展开", "text=更多", "text=阅读全文", ".expand-button", ".toggle"]:
            try:
                for btn in await page.query_selector_all(sel):
                    if await btn.is_visible():
                        await btn.click(timeout=3000)
                        await asyncio.sleep(0.5)
            except Exception:
                continue
        new_height = await page.evaluate("document.body.scrollHeight")
        if new_height == last_height:
            break
        last_height = new_height

async def light_scroll(page):
    """社交：滚 1 次即可"""
    await page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
    await asyncio.sleep(CFG["scroll_pause"])

# -------------------- 4. 采集 --------------------
async def crawl_one(crawler, tar, state):
    url, name, t = tar["url"], tar["name"], tar["type"]
    if state.get(fp(url), {}).get("done") and (time.time() - state[fp(url)]["ts"]) < 86400 * 30:
        print(f"[skip] {name} 30 日内已采")
        return
    print(f"[run] {name}")

    # 0.7.4 把 timeout 从 CrawlerRunConfig 删掉，挂到 arun
    run_cfg = CrawlerRunConfig(
        cache_mode=CacheMode.BYPASS,
        wait_until="networkidle",
        word_count_threshold=10,
        exclude_all_images=False,
        markdown_generator=DefaultMarkdownGenerator(
            content_filter=PruningContentFilter(threshold=0.40, min_word_threshold=5)
        ),
    )

    async def after_goto(page, *args, **kwargs):
        if t in CFG["deep_types"]:
            await deep_expand(page)
        elif t in CFG["light_types"]:
            await light_scroll(page)

    crawler.crawler_strategy.set_hook("after_goto", after_goto)

    try:
        result = await crawler.arun(url=url, config=run_cfg, timeout=CFG["timeout"])
        if not result or not result.markdown:
            raise ValueError("empty markdown")
    except (PWTimeout, ValueError) as e:
        print(f"[fail] {name} : {e}")
        state[fp(url)] = {"ts": int(time.time()), "done": False}
        return

    # 保存纯文本内容（移除链接）
    out_path = os.path.join(CFG["out_dir"], f"{name}.md")
    clean_content = remove_links(result.markdown)
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(clean_content)
    print(f"[save] {out_path}  {len(clean_content)} 字")
    state[fp(url)] = {"ts": int(time.time()), "done": True}

# -------------------- 5. 主入口 --------------------
async def main():
    state = load_state()
    browser_cfg = BrowserConfig(
        headless=CFG["headless"],
        proxy=CFG["proxy"],
        viewport_width=1920,
        viewport_height=1080,
    )
    async with AsyncWebCrawler(config=browser_cfg) as crawler:
        for tar in TARGETS:
            await crawl_one(crawler, tar, state)
            await asyncio.sleep(random.uniform(2, 4))  # 温和速率
    save_state(state)
    print("[all done] 纯文本内容已落盘，已移除所有链接")

if __name__ == "__main__":
    asyncio.run(main())