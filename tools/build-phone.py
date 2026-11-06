#!/usr/bin/env python3
"""Small share-target APK, using the same pinned toolchain and separately stored owner key."""
from pathlib import Path
import os,subprocess,shutil,zipfile,struct,re
root=Path(__file__).resolve().parent.parent;os.chdir(root);cache=root/'.cache';build=root/'phone/build/portable'
def run(*a):subprocess.run(list(map(str,a)),check=True)
config=(root/'phone/build.gradle').read_text();version=re.search("versionName '([^']+)'",config)[1];code=re.search(r'versionCode (\d+)',config)[1]
run('python3','tools/bootstrap.py');shutil.rmtree(build,ignore_errors=True)
for d in ['generated','classes','dex','signer']:(build/d).mkdir(parents=True)
manifest=(root/'phone/src/main/AndroidManifest.xml').read_text().replace('<manifest ','<manifest package="ru.truetv.totv" ',1);(build/'AndroidManifest.xml').write_text(manifest)
run(cache/'aapt2','compile','--dir',root/'phone/src/main/res','-o',build/'compiled.zip')
run(cache/'aapt2','link','-o',build/'resources.apk','-I',cache/'android34.jar','--manifest',build/'AndroidManifest.xml','--java',build/'generated','--min-sdk-version','21','--target-sdk-version','34','--version-code',code,'--version-name',version,build/'compiled.zip')
sources=list((root/'bridge/src/main/java').rglob('*.java'))+list((root/'bridge-android/src/main/java').rglob('*.java'))+list((build/'generated').rglob('*.java'))
run('java','-jar',cache/'ecj.jar','-8','-nowarn','-encoding','UTF-8','-cp',cache/'android34.jar','-d',build/'classes',*sources)
run('java','-cp',cache/'r8.jar','com.android.tools.r8.D8','--min-api','21','--lib',cache/'android34.jar','--output',build/'dex',*list((build/'classes').rglob('*.class')))
with zipfile.ZipFile(build/'unsigned.apk','w',compression=zipfile.ZIP_STORED) as z:
 with zipfile.ZipFile(build/'resources.apk') as src:files=[(n,src.read(n)) for n in src.namelist()]
 files += [(p.name,p.read_bytes()) for p in (build/'dex').glob('*.dex')]
 for name,data in files:
  info=zipfile.ZipInfo(name,(2026,11,3,0,0,0));pos=z.fp.tell()+30+len(name.encode());pad=(-pos)%4
  if pad:info.extra=struct.pack('<HH',0xffff,pad)+bytes(pad)
  z.writestr(info,data)
run('java','-jar',cache/'ecj.jar','-8','-nowarn','-cp',cache/'apksig.jar','-d',build/'signer',root/'tools/Signer.java')
signer=str(build/'signer')+os.pathsep+str(cache/'apksig.jar');out=root/'artifacts'/(version+'-To-TV.apk')
run('java','-cp',signer,'Signer','sign',root/'.signing/true-tv.p12',root/'.signing/password',build/'unsigned.apk',out)
run('java','-cp',signer,'Signer','verify',out);print(out)
