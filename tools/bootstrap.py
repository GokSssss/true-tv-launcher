#!/usr/bin/env python3
"""Download pinned public build tools; nothing from the legacy app is a build input."""
import hashlib,urllib.request,zipfile,os
from pathlib import Path
root=Path(__file__).resolve().parent.parent; cache=root/'.cache';cache.mkdir(exist_ok=True)
tools={
 'ecj.jar':('https://repo.maven.apache.org/maven2/org/eclipse/jdt/ecj/3.39.0/ecj-3.39.0.jar','01f5a92ac19bb2b3bf85e295a68f2c73c264369109158b566ce9b490af982948'),
 'r8.jar':('https://dl.google.com/android/maven2/com/android/tools/r8/8.3.37/r8-8.3.37.jar','59753e70a74f918389cc87f1b7d66b5c0862932559167425708ded159e3de439'),
 'apksig.jar':('https://dl.google.com/android/maven2/com/android/tools/build/apksig/8.7.3/apksig-8.7.3.jar','c070ed1394629d74641aa0906f60b2ffa1ee77e6366a1f93437f59717b1aeb89'),
 'android34.jar':('https://raw.githubusercontent.com/Sable/android-platforms/b439048ed3def8f48fa5801bb4bf4729b112f7ac/android-34/android.jar','6cea1df3efb77103ac3e2beb9bf4718964b0e0869ab16d39d29d5cbae1c147ad'),
 'aapt2.jar':('https://dl.google.com/android/maven2/com/android/tools/build/aapt2/8.7.3-12006047/aapt2-8.7.3-12006047-linux.jar','028916a040b58f34c9c89984e1bc48f8da6581662014f2b66391860def8b9c36')}
for name,(url,digest) in tools.items():
 p=cache/name
 if not p.exists():
  print('Downloading',name,flush=True);p.write_bytes(urllib.request.urlopen(url,timeout=90).read())
 if hashlib.sha256(p.read_bytes()).hexdigest()!=digest:raise SystemExit('Hash mismatch: '+name)
with zipfile.ZipFile(cache/'aapt2.jar') as z:(cache/'aapt2').write_bytes(z.read('aapt2'))
os.chmod(cache/'aapt2',0o755)
print('Pinned tools verified')
