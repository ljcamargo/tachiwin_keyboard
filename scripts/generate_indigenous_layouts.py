#!/usr/bin/env python3
"""
Generate indigenous Mexican keyboard layout files and extension manifests
for the Tachiwin Tsokgnan keyboard project.

Creates a NEW standalone extension at assets/ime/keyboard/org.tachiwin.tsokgnan/
that does NOT modify any existing org.florisboard.* files.

Input (single unified JSON):
  data/mexico-indigenous-layouts.json
  ────────────────────────────────────
  {
    "version": "1",
    "languages": {
      "amu": {
        "name": {
          "autonym": "Ñomndaa",          // Native name (first before comma → primary label)
          "inali": "Amuzgo del norte",   // Official INALI name
          "spanish": "Amuzgo de Guerrero",
          "english": "Guerrero Amuzgo"
        },
        "family": "Otomangue",
        "superlanguage": "Amuzgo",
        "authors": ["Tachiwin Tutunakú"],
        "layout": [
          [{"code": 113, "label": "q"}, ...],   // Row 0
          [{"code": 97,  "label": "a"}, ...],   // Row 1
          [{"code": 122, "label": "z"}, ...]    // Row 2
        ],
        "popups": {
          "a": ["á", "à", "ä"],
          "e": ["é", "è"],
          "n": ["ñ"]
        }
      }
    }
  }

Output (all NEW files, never modifies existing):
  - assets/ime/keyboard/org.tachiwin.tsokgnan/layouts/characters/{code}.json
  - assets/ime/keyboard/org.tachiwin.tsokgnan/extension.json
"""

import json
import sys
from pathlib import Path

# ------------------------------------------------------------
# Configuration
# ------------------------------------------------------------
PROJECT_ROOT = Path(__file__).resolve().parent.parent

LAYOUTS_SRC = PROJECT_ROOT / "data" / "mexico-indigenous-layouts.json"

NEW_EXT_ID = "org.tachiwin.tsokgnan"
NEW_EXT_DIR = PROJECT_ROOT / "app" / "src" / "main" / "assets" / "ime" / "keyboard" / NEW_EXT_ID
NEW_LAYOUTS_DIR = NEW_EXT_DIR / "layouts" / "characters"


def load_json(path):
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def save_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print(f"  ✏️  {path.relative_to(PROJECT_ROOT)}")


def get_autonym(raw: str) -> str:
    """Extract the primary autonym (first item before comma)."""
    if not raw:
        return ""
    return raw.split(",")[0].strip()


def titlecase(s: str) -> str:
    """Safe titlecase that handles empty strings."""
    return s.strip().title() if s else ""


def char_to_key_data(char: str) -> dict:
    """Convert a single character to AutoTextKeyData JSON format."""
    return {"$": "auto_text_key", "code": ord(char), "label": char}


def build_display_label(code: str, name: dict) -> str:
    """Build a 3-line display label: autonym / inali name / language code."""
    autonym = get_autonym(name.get("autonym", ""))
    inali = titlecase(name.get("inali", ""))
    language_tag = f"{code}-MX"
    lines = []
    lines.append(autonym if autonym else inali if inali else code.upper())
    if inali:
        lines.append(inali)
    lines.append(language_tag)
    return "\n".join(lines)

def generate_popup_mapping(code: str, popups: dict, output_dir: Path):
    """Generate a popup mapping JSON file for a language with custom popups.
    
    Input format: {"a": ["á", "à", "ä"], "n": ["ñ"], ...}
    Output format: the Android popup mapping JSON ("all" section)
    """
    all_section = {}
    for base_char, variants in popups.items():
        if not variants:
            continue
        entry = {"relevant": [char_to_key_data(v) for v in variants]}
        # First variant becomes the "main" (default on long-press)
        entry["main"] = char_to_key_data(variants[0])
        all_section[base_char] = entry

    mapping = {"all": all_section}
    out_path = output_dir / f"{code}.json"
    save_json(out_path, mapping)
    return code  # return the popup mapping ID (same as language code)
    """Build a 3-line display label: autonym / inali name / language code."""
    autonym = get_autonym(name.get("autonym", ""))
    inali = titlecase(name.get("inali", ""))
    language_tag = f"{code}-MX"
    lines = []
    lines.append(autonym if autonym else inali if inali else code.upper())
    if inali:
        lines.append(inali)
    lines.append(language_tag)
    return "\n".join(lines)


def main():
    print("=" * 60)
    print("Tachiwin — Indigenous Keyboard Layout Generator")
    print("=" * 60)

    # --------------------------------------------------------
    # 1. Load input data (supports both legacy and unified formats)
    # --------------------------------------------------------
    print(f"\n📂 Loading {LAYOUTS_SRC.relative_to(PROJECT_ROOT)}...")
    if not LAYOUTS_SRC.exists():
        print(f"  ❌ {LAYOUTS_SRC} not found!")
        sys.exit(1)

    raw_data = load_json(LAYOUTS_SRC)

    # Detect format
    is_unified = isinstance(raw_data, dict) and "languages" in raw_data

    if is_unified:
        print(f"  ✅ Detected UNIFIED format (version {raw_data.get('version', '?')})")
        languages = raw_data["languages"]
        # Build metadata + layout from the unified structure
        entries = {}  # code -> {name, layout, family, superlanguage, authors, popups}
        for code, lang in languages.items():
            entries[code] = {
                "name": lang.get("name", {}),
                "layout": lang.get("layout", []),
                "family": lang.get("family", ""),
                "superlanguage": lang.get("superlanguage", ""),
                "authors": lang.get("authors", ["Tachiwin Tutunakú"]),
                "popups": lang.get("popups"),  # None if absent
            }
    else:
        print(f"  ❌ Unknown format: expected a dict with 'languages' key, got {type(raw_data).__name__}")
        sys.exit(1)

    print(f"  📄 Total languages: {len(entries)}")

    # Sort codes alphabetically
    layout_codes = sorted(entries.keys())

    # --------------------------------------------------------
    # 2. Generate individual layout JSON files
    # --------------------------------------------------------
    print(f"\n📝 Generating {len(layout_codes)} layout files...")
    for code in layout_codes:
        layout = entries[code]["layout"]
        if not layout:
            print(f"  ⚠️  No layout data for '{code}', skipping")
            continue
        save_json(NEW_LAYOUTS_DIR / f"{code}.json", layout)

    # --------------------------------------------------------
    # 3. Build the complete extension.json
    # --------------------------------------------------------
    print(f"\n📦 Building extension manifest...")

    # 3a. Characters layout list + popup mapping files
    characters = []
    popup_mappings = []
    NEW_POPUPS_DIR = NEW_EXT_DIR / "popupMappings"
    for code in layout_codes:
        name = entries[code]["name"]
        authors = entries[code].get("authors", ["Tachiwin Tutunakú"])
        label = (titlecase(name.get("inali", "")) or
                 titlecase(name.get("spanish", "")) or
                 titlecase(name.get("english", "")) or
                 code.upper())
        characters.append({
            "id": code,
            "label": label,
            "authors": authors,
            "direction": "ltr",
        })
        # Generate popup mapping if language has custom popups
        popups = entries[code].get("popups")
        if popups:
            generate_popup_mapping(code, popups, NEW_POPUPS_DIR)
            popup_mappings.append({
                "id": code,
                "authors": authors,
            })

    # 3b. Subtype presets
    subtype_presets = []
    for code in layout_codes:
        name = entries[code]["name"]
        language_tag = f"{code}-MX"
        # Use custom popup mapping if the language has popups, else fall back to Spanish
        popups = entries[code].get("popups")
        if popups:
            popup_ref = f"{NEW_EXT_ID}:{code}"
        else:
            popup_ref = "org.florisboard.localization:es"
        subtype_presets.append({
            "languageTag": language_tag,
            "displayLabel": build_display_label(code, name),
            "composer": "org.florisboard.composers:appender",
            "currencySet": f"{NEW_EXT_ID}:mexican_peso",
            "popupMapping": popup_ref,
            "preferred": {
                "characters": f"{NEW_EXT_ID}:{code}",
            },
        })

    # 3c. Mexican Peso currency set
    currency_sets = [
        {
            "id": "mexican_peso",
            "label": "Mexican Peso ($)",
            "slots": [
                {"code": 36, "label": "$"},
                {"code": 162, "label": "¢"},
                {"code": 8364, "label": "€"},
                {"code": 163, "label": "£"},
                {"code": 165, "label": "¥"},
                {"code": 8369, "label": "₱"},
            ],
        },
    ]

    # 3d. Assemble the extension
    extension = {
        "$": "ime.extension.keyboard",
        "meta": {
            "id": NEW_EXT_ID,
            "version": "0.1.0",
            "title": "Lenguas Indígenas de México",
            "description": "Distribuciones de teclado para lenguas indígenas mexicanas.",
            "maintainers": ["Luis J Camargo <lsjcp@yahoo.com>"],
            "license": "apache-2.0",
        },
        "dependencies": [
            "org.florisboard.composers",
            "org.florisboard.currencysets",
            "org.florisboard.layouts",
            "org.florisboard.localization",
        ],
        "currencySets": currency_sets,
        "layouts": {
            "characters": characters,
        },
        "popupMappings": popup_mappings,
        "subtypePresets": subtype_presets,
    }

    save_json(NEW_EXT_DIR / "extension.json", extension)

    # --------------------------------------------------------
    # Summary
    # --------------------------------------------------------
    print("\n" + "=" * 60)
    print("✅ Done! Zero existing files were modified.")
    print("=" * 60)
    print(f"""
📁 {NEW_EXT_DIR.relative_to(PROJECT_ROOT)}/

  ├── extension.json
  │     • {len(characters)} character layouts
  │     • {len(subtype_presets)} subtype presets with displayLabel
  │     • {len(popup_mappings)} custom popup mappings
  │     • mexican_peso currency set
  │
  ├── layouts/characters/
  │     • {len(layout_codes)} layout JSON files
  │
  └── popupMappings/
        • {len(popup_mappings)} popup mapping files (when popups defined in source)
""")


if __name__ == "__main__":
    main()
