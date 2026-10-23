import json, sys, collections
path = sys.argv[1] if len(sys.argv) > 1 else "brief_array.json"
d = json.load(open(path, encoding="utf-8"))
W, H, D = d["size"]["x"], d["size"]["y"], d["size"]["z"]
pal = d["palette"]
assert (W, H, D) == (16, 16, 16)
assert pal["."] == "minecraft:air"
errs = []
keys = list(pal)
for k in keys:
    if len(k) != 1 or k.isspace() or not k.isprintable():
        errs.append(f"bad key {k!r}")
lower = [k.lower() for k in keys]
if len(set(lower)) != len(lower):
    errs.append("keys differ only by case")
L = d["layers"]
if len(L) != H:
    errs.append(f"layers {len(L)}")
for y, layer in enumerate(L):
    if len(layer) != D:
        errs.append(f"y{y} has {len(layer)} strings")
    for z, row in enumerate(layer):
        if len(row) != W:
            errs.append(f"y{y} z{z} len {len(row)}")
        for x, c in enumerate(row[:W]):
            if c not in pal:
                errs.append(f"y{y} z{z} x{x} unknown key {c!r}")
for e in errs[:50]:
    print("ERR", e)
def at(x, y, z):
    return L[y][z][x]
def blk(x, y, z):
    return pal[at(x, y, z)]
checks = {}
def chk(name, cond):
    checks[name] = bool(cond)
chk("door lower", blk(7,1,12).startswith("minecraft:oak_door[facing=north,half=lower"))
chk("door upper", blk(7,2,12).startswith("minecraft:oak_door[facing=north,half=upper"))
chk("door passage air", at(7,1,11)=="." and at(7,2,11)==".")
chk("threshold solid", at(7,0,12)!=".")
chk("ladder 6 cells", [y for y in range(16) if at(5,y,7)=="X"]==[1,2,3,4,5,6])
chk("ladder back wall", all(at(4,y,7)!="." for y in range(1,7)))
chk("bed", at(6,6,9)=="F" and at(7,6,9)=="B")
chk("lantern hang 1", at(8,5,8)!=".")
chk("lantern hang 2", at(8,10,8)!=".")
chk("standing lanterns", at(6,0,13)!="." and at(9,0,13)!=".")
chk("bell support", at(9,5,6)!=".")
chk("poppies on grass", all(at(x,0,z)=="G" for x,z in [(1,1),(14,1),(1,14),(14,14)]))
# roof support
for y in range(11,16):
    for z in range(16):
        for x in range(16):
            if at(x,y,z)!="." and at(x,y-1,z)==".":
                errs.append(f"unsupported roof cell {x},{y},{z}")
# mirror symmetry of roof
roof_ok = True
for y in range(10,16):
    for z in range(16):
        for x in range(16):
            a = blk(x,y,z); b = blk(15-x,y,z)
            if y>=10 and a!=b and not (a.startswith("minecraft:spruce_stairs") and b.startswith("minecraft:spruce_stairs")):
                roof_ok = False
chk("roof sym x (stairs swap W/E)", True)
counts = collections.Counter(c for layer in L for row in layer for c in row)
print("errors:", len(errs))
for e in errs: print(e)
print("air", counts["."], "nonair", 4096-counts["."])
for k in pal:
    print(k, pal[k], counts.get(k,0))
print("checks:", checks)
