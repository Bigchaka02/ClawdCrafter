import re, json, sys
from collections import Counter, deque
RUN = "/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run"
CHOSEN = RUN + "/05-vote/chosen.md"
BRIEF = RUN + "/01-brief/brief.md"

def grids_by_y(text, start_pat):
    """Return {y: [16 rows]} for grid blocks that follow a heading matching start_pat."""
    out = {}
    lines = text.split("\n")
    y = None
    in_block = False
    rows = []
    for ln in lines:
        m = re.search(start_pat, ln)
        if m:
            y = int(m.group(1)); continue
        if ln.strip() == "```" :
            if in_block:
                if rows and y is not None and len(rows) == 16:
                    out[y] = rows
                rows = []; in_block = False; y = None if y is None else y
                y = None
            else:
                if y is not None or True:
                    in_block = True; rows = []
            continue
        if in_block:
            m2 = re.match(r'^z=\s*(\d+)\s+(\S{16})\s*$', ln)
            if m2:
                rows.append(m2.group(2))
    return out

def json_block(text, after_pat):
    idx = re.search(after_pat, text).start()
    m = re.search(r"```json\n(.*?)\n```", text[idx:], re.S)
    return json.loads(m.group(1))

chosen = open(CHOSEN, encoding="utf-8").read()
brief = open(BRIEF, encoding="utf-8").read()

# Chosen grids: headings "### y = N"
ch_grids = {}
cur = None; inb = False; rows = []
for ln in chosen.split("\n"):
    m = re.match(r"^### y = (\d+)", ln)
    if m: cur = int(m.group(1)); continue
    if ln.strip().startswith("```"):
        if inb:
            if cur is not None and len(rows) == 16: ch_grids[cur] = rows
            inb = False; rows = []
        else:
            inb = True; rows = []
        continue
    if inb:
        m2 = re.match(r'^z=\s*(\d+)\s+(\S{16})\s*$', ln)
        if m2: rows.append(m2.group(2))

# Brief grids: headings "**y = N**"
br_grids = {}
cur = None; inb = False; rows = []
for ln in brief.split("\n"):
    m = re.match(r"^\*\*y = (\d+)\*\*", ln)
    if m: cur = int(m.group(1)); continue
    if ln.strip().startswith("```"):
        if inb:
            if cur is not None and len(rows) == 16: br_grids[cur] = rows
            inb = False; rows = []; cur = None
        else:
            inb = True; rows = []
        continue
    if inb:
        m2 = re.match(r'^z=\s*(\d+)\s+(\S{16})\s*$', ln)
        if m2: rows.append(m2.group(2))

print("chosen grids:", sorted(ch_grids) == list(range(16)), len(ch_grids))
print("brief grids:", sorted(br_grids) == list(range(16)), len(br_grids))

# Chosen JSON array
ch_json = json_block(chosen, r"## Array file")
br_json = json_block(brief, r"13\.3 Array file")
print("chosen json size:", ch_json["size"])
print("brief json size:", br_json["size"])

def layers_to_grid(js):
    return {y: js["layers"][y] for y in range(16)}
ch_js = layers_to_grid(ch_json)
br_js = layers_to_grid(br_json)

# 1. Format checks on JSON
print("\n== JSON format ==")
for name, js in (("chosen", ch_json), ("brief", br_json)):
    L = js["layers"]
    assert len(L) == 16, (name, len(L))
    bad = []
    for y, layer in enumerate(L):
        if len(layer) != 16: bad.append(("z-count", y, len(layer)))
        for z, row in enumerate(layer):
            if len(row) != 16: bad.append(("x-len", y, z, len(row)))
            for x, c in enumerate(row):
                if c not in js["palette"]: bad.append(("key", y, z, x, c))
    unused = set(js["palette"]) - {c for L2 in L for r in L2 for c in r}
    print(name, "format problems:", bad[:10], "unused keys:", sorted(unused))
    print(name, "palette keys:", len(js["palette"]), "case pairs:", [k for k in js["palette"] if k.lower()!=k and k.lower() in js["palette"]])

# 2. Chosen grids vs chosen JSON
print("\n== chosen grids vs chosen JSON ==")
for y in range(16):
    if ch_grids[y] != ch_json["layers"][y]:
        diffs = [(z, ch_grids[y][z], ch_json["layers"][y][z]) for z in range(16) if ch_grids[y][z] != ch_json["layers"][y][z]]
        print("y", y, "grid != json", diffs)
print("done grid vs json")

# 3. Chosen grids vs brief grids (cell by cell)
print("\n== chosen grids vs brief section 13.2 grids ==")
for y in range(16):
    for z in range(16):
        a = ch_grids[y][z]; b = br_grids[y][z]
        for x in range(16):
            if a[x] != b[x]:
                print(f"  DIFF y={y} z={z} x={x}: chosen={a[x]!r} brief={b[x]!r}")
print("done grids vs brief grids")

# 4. Brief JSON vs brief grids (sanity)
print("\n== brief JSON vs brief grids ==")
for y in range(16):
    if br_grids[y] != br_json["layers"][y]:
        print("brief y", y, "grid != brief json")
print("done")

# 5. Palette in chosen.md vs brief palette
print("\n== palette compare ==")
for k in set(ch_json["palette"]) | set(br_json["palette"]):
    if ch_json["palette"].get(k) != br_json["palette"].get(k):
        print("palette mismatch", k, ch_json["palette"].get(k), br_json["palette"].get(k))
print("palette compare done")

# 6. Cell counts per key and per layer
print("\n== counts ==")
cnt = Counter()
per_layer_air = []
for y in range(16):
    c = Counter(ch for r in ch_json["layers"][y] for ch in r)
    cnt += c
    per_layer_air.append((y, c["."], 256 - c["."]))
print("total keys:", dict(sorted(cnt.items())))
print("per layer (y, air, nonair):", per_layer_air)
print("total air:", cnt["."], "nonair:", 4096 - cnt["."])
