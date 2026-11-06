#!/usr/bin/env python3
"""Une el jar original del mod (clases, assets, mixin) con el jar compilado del codigo nuevo.

uso: merge_jar.py <original.jar> <nuevo.jar> <salida.jar>

- Del jar nuevo se toma todo (clases nuevas y mods.toml con la version nueva), salvo el MANIFEST.
- Del jar original se toma todo lo que no exista ya en el nuevo, incluido su MANIFEST
  (lleva MixinConfigs) para que el mixin del mod siga funcionando.
"""
import sys
import zipfile

orig_path, new_path, out_path = sys.argv[1:4]
MANIFEST = "META-INF/MANIFEST.MF"

with zipfile.ZipFile(orig_path) as zo, zipfile.ZipFile(new_path) as zn, \
        zipfile.ZipFile(out_path, "w", zipfile.ZIP_DEFLATED) as out:
    written = set()

    def put(name, data):
        if name in written:
            return
        written.add(name)
        out.writestr(name, data)

    put(MANIFEST, zo.read(MANIFEST))
    for info in zn.infolist():
        if info.filename == MANIFEST or info.filename.endswith("/"):
            continue
        if info.filename.upper().startswith("META-INF/") and info.filename.upper().endswith((".SF", ".RSA", ".DSA")):
            continue
        put(info.filename, zn.read(info.filename))
    for info in zo.infolist():
        if info.filename.endswith("/"):
            continue
        put(info.filename, zo.read(info.filename))

print(f"OK -> {out_path} ({len(written)} entradas)")
