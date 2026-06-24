#!/usr/bin/env python3
"""Quick live eval for the app's AI chapter rule-generation protocol."""

from __future__ import annotations

import argparse
import json
import os
import re
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
AI_HELPER = ROOT / "app/src/main/java/io/legado/app/help/ai/AiPurifyHelper.kt"
AI_PROMPT_STORE = ROOT / "app/src/main/java/io/legado/app/help/ai/AiPromptStore.kt"
DEFAULT_BOOK = ROOT / "data/这不是我想要的约会⊙完本.txt"


def main() -> int:
    load_dotenv(ROOT / ".env")
    parser = argparse.ArgumentParser()
    parser.add_argument("--book", default=str(DEFAULT_BOOK.relative_to(ROOT)))
    parser.add_argument("--case", choices=("front", "materials", "ps", "all"), default="all")
    parser.add_argument("--base-url", default=os.getenv("AI_BASE_URL", "https://api.deepseek.com"))
    parser.add_argument("--path", default=os.getenv("AI_CHAT_COMPLETIONS_PATH", "/chat/completions"))
    parser.add_argument("--model", default=os.getenv("AI_MODEL", "deepseek-v4-flash"))
    parser.add_argument("--thinking", choices=("disabled", "enabled", "omit"), default=os.getenv("AI_THINKING", "disabled"))
    parser.add_argument("--api-key", default=os.getenv("AI_API_KEY") or os.getenv("DEEPSEEK_API_KEY"))
    parser.add_argument("--timeout", type=int, default=120)
    parser.add_argument("--save-raw", default=None)
    args = parser.parse_args()

    if not args.api_key:
        print("Missing API key. Set AI_API_KEY or DEEPSEEK_API_KEY.", file=sys.stderr)
        return 2

    book_path = (ROOT / args.book).resolve()
    paragraphs = read_non_empty_lines(book_path)
    cases = build_cases(paragraphs)
    selected = cases if args.case == "all" else [case for case in cases if case["name"] == args.case]
    system_prompt = build_system_prompt()
    raw_runs = []
    failed = False

    for case in selected:
        report, raw = run_case(args, system_prompt, case)
        raw_runs.append(raw)
        print_report(report)
        if report["missing_required_deletes"] or report["forbidden_deletes"] or report["forbidden_replacements"]:
            failed = True

    if args.save_raw:
        save_path = ROOT / args.save_raw
        save_path.parent.mkdir(parents=True, exist_ok=True)
        save_path.write_text(json.dumps(raw_runs, ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"\nraw={save_path}")
    return 1 if failed else 0


def read_non_empty_lines(path: Path) -> list[str]:
    text = path.read_text(encoding="utf-8", errors="replace")
    return [line.strip() for line in text.splitlines() if line.strip()]


def build_cases(paragraphs: list[str]) -> list[dict[str, Any]]:
    front_lines = paragraphs[:24]
    material_start = find_index(paragraphs, "人物卡 两仪梦月")
    ps_start = find_index(paragraphs, "PS：同时会切换对应章节的插图。")
    return [
        {
            "name": "front",
            "inputs": to_inputs(front_lines),
            "required_delete_ids": list(range(1, 12)),
            "protected_ids": list(range(12, len(front_lines) + 1)),
        },
        {
            "name": "materials",
            "inputs": to_inputs(paragraphs[material_start:material_start + 55]),
            "required_delete_ids": [],
            "protected_ids": list(range(1, 56)),
        },
        {
            "name": "ps",
            "inputs": to_inputs(paragraphs[ps_start:ps_start + 36]),
            "required_delete_ids": [],
            "protected_ids": list(range(1, 37)),
        },
    ]


def find_index(paragraphs: list[str], text: str) -> int:
    for index, paragraph in enumerate(paragraphs):
        if paragraph == text or text in paragraph:
            return index
    raise RuntimeError(f"Cannot find paragraph: {text}")


def to_inputs(lines: list[str]) -> list[dict[str, Any]]:
    return [{"id": index + 1, "input": line} for index, line in enumerate(lines)]


def run_case(args: argparse.Namespace, system_prompt: str, case: dict[str, Any]) -> tuple[dict[str, Any], dict[str, Any]]:
    request_body = {
        "model": args.model,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": json.dumps(case["inputs"], ensure_ascii=False, indent=2)},
        ],
        "temperature": 0,
        "max_tokens": 8192,
        "stream": False,
    }
    if args.thinking != "omit":
        request_body["thinking"] = {"type": args.thinking}
    started = time.monotonic()
    response = post_json(
        url=args.base_url.rstrip("/") + ensure_start_slash(args.path),
        api_key=args.api_key,
        body=request_body,
        timeout=args.timeout,
    )
    latency_ms = int((time.monotonic() - started) * 1000)
    content = response["choices"][0]["message"].get("content") or ""
    parsed, parse_error = parse_rule_content(content)
    rules = parsed.get("rules", []) if isinstance(parsed, dict) else []
    source_by_id = {item["id"]: item["input"] for item in case["inputs"]}
    delete_rules = [rule for rule in rules if isinstance(rule, dict) and rule.get("new", None) == ""]
    replacement_rules = [rule for rule in rules if isinstance(rule, dict) and rule.get("new", "") != ""]
    deleted_ids = {
        rule.get("id")
        for rule in delete_rules
        if isinstance(rule.get("id"), int) and rule.get("old") == source_by_id.get(rule.get("id"))
    }
    required_delete_ids = set(case["required_delete_ids"])
    protected_ids = set(case["protected_ids"])
    missing_required_deletes = sorted(required_delete_ids - deleted_ids)
    forbidden_deletes = sorted(protected_ids & deleted_ids)
    forbidden_replacements = sorted(
        {
            rule.get("id")
            for rule in replacement_rules
            if isinstance(rule.get("id"), int) and rule.get("id") in protected_ids
        }
    )
    report = {
        "name": case["name"],
        "model": response.get("model") or args.model,
        "latency_ms": latency_ms,
        "parse_error": parse_error,
        "rules": rules,
        "deleted_ids": sorted(deleted_ids),
        "missing_required_deletes": missing_required_deletes,
        "forbidden_deletes": forbidden_deletes,
        "forbidden_replacements": forbidden_replacements,
    }
    raw = {"case": case, "request": request_body, "response": response, "report": report}
    return report, raw


def build_system_prompt() -> str:
    helper_text = AI_HELPER.read_text(encoding="utf-8")
    prompt_store_text = AI_PROMPT_STORE.read_text(encoding="utf-8")
    protocol = extract_triple_block(helper_text, marker="用户会给你一个 JSON 数组")
    task_prompt = extract_rule_generate_prompt(prompt_store_text)
    return protocol.replace("${AiPromptStore.prompt(AiPromptStore.Prompt.RULE_GENERATE)}", task_prompt)


def extract_rule_generate_prompt(text: str) -> str:
    match = re.search(r"private val RULE_GENERATE_SKILL = \"\"\"(.*?)\"\"\"\.trimIndent\(\)", text, re.S)
    if not match:
        raise RuntimeError("Cannot extract RULE_GENERATE_SKILL")
    return trim_kotlin_indent(match.group(1))


def extract_triple_block(text: str, marker: str) -> str:
    marker_index = text.index(marker)
    start = text.rfind('"""', 0, marker_index)
    end = text.index('""".trimIndent()', marker_index)
    return trim_kotlin_indent(text[start + 3:end])


def trim_kotlin_indent(block: str) -> str:
    lines = block.strip("\n").splitlines()
    non_empty = [line for line in lines if line.strip()]
    indent = min((len(line) - len(line.lstrip())) for line in non_empty) if non_empty else 0
    return "\n".join(line[indent:] for line in lines).strip()


def parse_rule_content(content: str) -> tuple[dict[str, Any] | None, str | None]:
    text = content.strip()
    if text.startswith("```"):
        lines = text.splitlines()
        if lines and lines[0].startswith("```"):
            lines = lines[1:]
        if lines and lines[-1].strip() == "```":
            lines = lines[:-1]
        text = "\n".join(lines).strip()
    start = text.find("{")
    end = text.rfind("}")
    if start < 0 or end <= start:
        return None, "missing_json_object"
    try:
        parsed = json.loads(text[start:end + 1])
    except json.JSONDecodeError as error:
        return None, f"json_parse_error:{error}"
    if not isinstance(parsed, dict) or not isinstance(parsed.get("rules"), list):
        return None, "schema_error"
    return parsed, None


def post_json(url: str, api_key: str, body: dict[str, Any], timeout: int) -> dict[str, Any]:
    data = json.dumps(body, ensure_ascii=False).encode("utf-8")
    request = urllib.request.Request(
        url=url,
        data=data,
        method="POST",
        headers={
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
            "User-Agent": "ReadingNG-AiPurifyRuleEval/1.0",
        },
    )
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            return json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as error:
        detail = error.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"HTTP {error.code}: {detail[:1000]}") from error


def load_dotenv(path: Path) -> None:
    if not path.exists():
        return
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        key = key.strip()
        if not re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*", key) or key in os.environ:
            continue
        value = value.strip()
        if len(value) >= 2 and value[0] == value[-1] and value[0] in ("'", '"'):
            value = value[1:-1]
        os.environ[key] = value


def print_report(report: dict[str, Any]) -> None:
    print(f"\n[{report['name']}] model={report['model']} latency={report['latency_ms']}ms")
    print(f"  parse_error={report['parse_error']}")
    print(f"  deleted_ids={report['deleted_ids']}")
    print(f"  missing_required_deletes={report['missing_required_deletes']}")
    print(f"  forbidden_deletes={report['forbidden_deletes']}")
    print(f"  forbidden_replacements={report['forbidden_replacements']}")
    for rule in report["rules"][:30]:
        old = str(rule.get("old", "")).replace("\n", " ")[:90]
        print(f"    id={rule.get('id')} type={rule.get('type')} new={rule.get('new')!r} old={old}")


def ensure_start_slash(value: str) -> str:
    return value if value.startswith("/") else "/" + value


if __name__ == "__main__":
    raise SystemExit(main())
