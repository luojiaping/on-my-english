#!/usr/bin/env python3
"""Export built-in word decks from ECDICT (MIT, skywind3000/ECDICT).

Filters ecdict.csv by exam tag and emits one compact JSON per deck,
ready for app/src/main/assets/decks/.

AGP transparently decompresses .gz assets at packaging time and strips
the extension, so plain JSON is stored instead; the APK deflates it.

Usage: python3 scripts/ecdict_export.py /path/to/ecdict.csv
"""
import csv
import json
import sys
from pathlib import Path

DECKS = {
    "cet4": {"tag": "cet4", "name": "四级词汇", "badge": "四"},
    "cet6": {"tag": "cet6", "name": "六级词汇", "badge": "六"},
    "kaoyan": {"tag": "ky", "name": "考研词汇", "badge": "研"},
}

POS_MAP = {
    "n": "NOUN", "v": "VERB", "vt": "VERB", "vi": "VERB",
    "a": "ADJECTIVE", "adj": "ADJECTIVE", "ad": "ADVERB", "adv": "ADVERB",
    "prep": "PREPOSITION", "conj": "CONJUNCTION", "pron": "PRONOUN",
    "int": "INTERJECTION", "num": "OTHER", "aux": "VERB",
}


def first_pos(translation: str) -> str:
    head = translation.strip().split(".", 1)[0].strip().lower()
    return POS_MAP.get(head, "OTHER")


def main(csv_path: str) -> None:
    src = Path(csv_path)
    out_dir = Path(__file__).resolve().parent.parent / "app/src/main/assets/decks"
    out_dir.mkdir(parents=True, exist_ok=True)

    pending = {key: [] for key in DECKS}
    with src.open(encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            tags = (row.get("tag") or "").split()
            word = (row.get("word") or "").strip()
            translation = (row.get("translation") or "").strip()
            if not word or not translation or " " in word:
                continue
            payload = {
                "w": word,
                "ph": (row.get("phonetic") or "").strip(),
                "tr": translation.split("\n")[0].strip(),
                "pos": first_pos(translation),
                "frq": int(row["frq"]) if (row.get("frq") or "").isdigit() else 999999,
            }
            for key, meta in DECKS.items():
                if meta["tag"] in tags:
                    pending[key].append(payload)

    manifest = {}
    for key, items in pending.items():
        items.sort(key=lambda x: (x["frq"] == 0, x["frq"] if x["frq"] else 0))
        seen = set()
        unique = []
        for item in items:
            if item["w"].lower() in seen:
                continue
            seen.add(item["w"].lower())
            unique.append(item)
        doc = {"id": key, "name": DECKS[key]["name"], "badge": DECKS[key]["badge"],
               "tag": DECKS[key]["tag"], "count": len(unique), "words": unique}
        target = out_dir / f"{key}.json"
        with target.open("wt", encoding="utf-8") as f:
            json.dump(doc, f, ensure_ascii=False, separators=(",", ":"))
        manifest[key] = len(unique)
        print(f"{key}: {len(unique)} words -> {target} ({target.stat().st_size} bytes)")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "ecdict.csv")
