#!/usr/bin/env python3
"""Portable Linux build with Java21 runtime and Python3. Signing inputs stay private."""
from pathlib import Path
import subprocess,os,shutil,zipfile,struct,re
ROOT=Path(__file__).resolve().parent.parent;os.chdir(ROOT)
def run(*args):subprocess.run([str(a) for a in args],check=True)
run('python3','tools/bootstrap.py')
cache=ROOT/'.cache';build=ROOT/'app/build/portable';shutil.rmtree(build,ignore_errors=True);build.mkdir(parents=True)
for name in ('generated','classes','dex','signer'): (build/name).mkdir()
text=(ROOT/'app/build.gradle').read_text();app_id=re.search("applicationId '([^']+)'",text)[1];version=re.search("versionName '([^']+)'",text)[1];code=re.search('versionCode (\\d+)',text)[1]
manifest=(ROOT/'app/src/main/AndroidManifest.xml').read_text().replace('<manifest ', '<manifest package="ru.truetv.launcher" ',1)
(build/'AndroidManifest.xml').write_text(manifest)
run(cache/'aapt2','compile','--dir','app/src/main/res','-o',build/'compiled.zip')
run(cache/'aapt2','link','-o',build/'resources.apk','-I',cache/'android34.jar','--manifest',build/'AndroidManifest.xml','--java',build/'generated','--custom-package','ru.truetv.launcher','--rename-manifest-package',app_id,'--min-sdk-version','21','--target-sdk-version','34','--version-code',code,'--version-name',version,build/'compiled.zip')
(build/'generated/BuildConfig.java').write_text('package ru.truetv.launcher; public final class BuildConfig { public static final String VERSION_NAME="'+version+'"; public static final int VERSION_CODE='+code+'; }')
sources=list((ROOT/'app/src/main/java').rglob('*.java'))+list((ROOT/'bridge/src/main/java').rglob('*.java'))+list((ROOT/'bridge-android/src/main/java').rglob('*.java'))+list((build/'generated').rglob('*.java'))
run('java','-jar',cache/'ecj.jar','-8','-nowarn','-encoding','UTF-8','-cp',cache/'android34.jar','-d',build/'classes',*sources)
run('java','-cp',cache/'r8.jar','com.android.tools.r8.D8','--min-api','21','--lib',cache/'android34.jar','--output',build/'dex',*list((build/'classes').rglob('*.class')))
# All entries stored/aligned; APK has no native libraries.
with zipfile.ZipFile(build/'unsigned.apk','w',compression=zipfile.ZIP_STORED) as out:
 with zipfile.ZipFile(build/'resources.apk') as src:
  files=[(n,src.read(n)) for n in src.namelist()]
 files += [(p.name,p.read_bytes()) for p in (build/'dex').glob('*.dex')]
 for name,data in files:
  info=zipfile.ZipInfo(name,(2026,11,3,0,0,0));info.compress_type=zipfile.ZIP_STORED
  pos=out.fp.tell()+30+len(name.encode());padding=(-pos)%4
  if padding:info.extra=struct.pack('<HH',0xffff,padding)+bytes(padding)
  out.writestr(info,data)
run('java','-jar',cache/'ecj.jar','-8','-nowarn','-cp',cache/'apksig.jar','-d',build/'signer','tools/Signer.java')
key=ROOT/'.signing/true-tv.p12';password=ROOT/'.signing/password'
if not key.exists():
 key.parent.mkdir(exist_ok=True);os.chmod(key.parent,0o700)
 if not password.exists():password.write_text(__import__('secrets').token_hex(32));os.chmod(password,0o600)
 run('keytool','-genkeypair','-keystore',key,'-storetype','PKCS12','-alias','truetv','-keyalg','RSA','-keysize','3072','-validity','10000','-dname','CN=True TV Launcher','-storepass:file',password,'-keypass:file',password)
 os.chmod(key,0o600)
output=ROOT/'artifacts'/(version+'-True-TV-Launcher.apk');output.parent.mkdir(exist_ok=True)
run('java','-cp',str(build/'signer')+os.pathsep+str(cache/'apksig.jar'),'Signer','sign',key,password,build/'unsigned.apk',output)
run('java','-cp',str(build/'signer')+os.pathsep+str(cache/'apksig.jar'),'Signer','verify',output)
print(output)
