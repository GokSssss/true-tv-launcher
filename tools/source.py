#!/usr/bin/env python3
"""One complete source archive; no signing material, tools cache, APK or build output."""
from pathlib import Path
import os,zipfile,hashlib,re
root=Path(__file__).resolve().parent.parent;os.chdir(root)
version=re.search("versionName '([^']+)'",(root/'app/build.gradle').read_text())[1]
excluded={'.signing','.cache','.gradle','.git','build','artifacts','readback','__pycache__'}
files=[]
for p in sorted(root.rglob('*')):
 relative=p.relative_to(root)
 if any(part in excluded for part in relative.parts) or not p.is_file():continue
 if p.is_symlink():raise SystemExit('Unexpected source symlink: '+str(relative))
 if relative.as_posix()=='docs/SOURCE_INPUTS.sha256':continue
 files.append(p)
manifest=root/'docs/SOURCE_INPUTS.sha256';manifest.write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.relative_to(root).as_posix()+'\n' for p in files));files.append(manifest)
out=root/'artifacts'/(version+'-True-TV-Launcher-SOURCE.zip');out.parent.mkdir(exist_ok=True)
with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as z:
 for p in files:
  name=version+'-True-TV-Launcher/'+p.relative_to(root).as_posix();info=zipfile.ZipInfo(name,(2026,11,3,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;z.writestr(info,p.read_bytes())
with zipfile.ZipFile(out) as z:
 assert z.testzip() is None
 assert not any('/.signing/' in n or n.endswith(('.apk','.p12','.keystore','.jks','.dex','.class')) for n in z.namelist())
print(out.name,out.stat().st_size,hashlib.sha256(out.read_bytes()).hexdigest())
