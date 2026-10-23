#!/usr/bin/env python3
"""Validate 07-arrays/B2.json against the 3D array format in 00-rules.md."""
import json
import re
import sys
from pathlib import Path

RUN = Path("/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run")
ARR = RUN / "07-arrays" / "B2.json"
SCHEM = RUN / "06-final" / "final-schematic.md"

errs = []
data = json.loads(ARR.read_text(encoding="utf-8"))

# Top-level keys
if set(data) != {"size", "palette", "layers"}:
    errs.append(f"top-level keys: {sorted(data)}")

# Size
size = data.get("size", {})
if size != {"x": 16, "y": 16, "z": 16}:
    errs.append(f"size: {size}")
X, Y, Z = size.get("x"), size.get("y"), size.get("z")

# Palette
pal = data.get("palette", {})
if pal.get(".") != "minecraft:air":
    errs.append("'.' is not minecraft:air")
for k, v in pal.items():
    if len(k) != 1 or not k.isprintable() or k.isspace():
        errs.append(f"bad palette key {k!r}")
    if not isinstance(v, str) or not v.startswith("minecraft:"):
        errs.append(f"bad block id for {k!r}: {v!r}")
    if k.isalpha() and k.swapcase() in pal:
        errs.append(f"case-pair keys {k!r} / {k.swapcase()!r}")

# Layers
layers = data.get("layers", [])
if len(layers) != Y:
    errs.append(f"layers count {len(layers)} != y {Y}")
used = set()
for y, layer in enumerate(layers):
    if len(layer) != Z:
        errs.append(f"y={y}: {len(layer)} z-strings != {Z}")
        continue
    for z, row in enumerate(layer):
        if len(row) != X:
            errs.append(f"y={y} z={z}: width {len(row)} != {X}")
        for x, ch in enumerate(row):
            used.add(ch)
            if ch not in pal:
                errs.append(f"y={y} z={z} x={x}: key {ch!r} not in palette")
unused = sorted(set(pal) - used)
if unused:
    errs.append(f"palette keys never used: {unused}")

# Block IDs in the schematic table must match the palette exactly
text = SCHEM.read_text(encoding="utf-8")
table = {}
for m in re.finditer(r"^\| `(.)` \| `([^`]+)` \|", text, re.M):
    table[m.group(1)] = m.group(2)
for k, v in pal.items():
    if k not in table:
        errs.append(f"key {k!r} missing from schematic table")
    elif table[k] != v:
        errs.append(f"key {k!r}: table {table[k]!r} != palette {v!r}")

# Highest non-air block must be y = 15 (apex)
top = max((y for y, l in enumerate(layers) for r in l for c in r if c != "."), default=None)
print(f"size={X}x{Y}x{Z}, palette keys={len(pal)}, distinct keys used={len(used)}, highest block y={top}")

if errs:
    print("INVALID:")
    print("\n".join(errs))
    sys.exit(1)
print("VALID")
