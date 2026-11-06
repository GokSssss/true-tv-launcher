#!/usr/bin/env python3
from pathlib import Path
import struct,zipfile,hashlib,subprocess,re
root=Path(__file__).resolve().parent.parent;version=re.search("versionName '([^']+)'",(root/'app/build.gradle').read_text())[1];apk=root/'artifacts'/(version+'-True-TV-Launcher.apk')
with zipfile.ZipFile(apk) as z:
 assert z.testzip() is None
 for n in z.namelist():
  assert not n.endswith(('.so','.ttf','.otf'))
  if n.endswith('.dex'):
   data=z.read(n)
   def u(off):return struct.unpack_from('<I',data,off)[0]
   def string(index):
    off=u(u(60)+index*4)
    while data[off]&128:off+=1
    off+=1
    return data[off:data.index(b'\0',off)].decode('utf-8','replace')
   types=[u(u(68)+4*i) for i in range(u(64))]
   classes=[string(types[u(u(100)+32*i)]) for i in range(u(96))]
   assert all(c.startswith(('Lru/truetv/launcher/','Lru/truetv/bridge/')) for c in classes),classes
   assert b'org/cosinus' not in data and b'Compat' not in data
   print('Own runtime classes:',len(classes))
 info=z.getinfo('resources.arsc');raw=apk.read_bytes();offset=info.header_offset+30+len(info.filename.encode())+struct.unpack_from('<H',raw,info.header_offset+28)[0]
 assert info.compress_type==0 and offset%4==0
badging=subprocess.check_output([root/'.cache/aapt2','dump','badging',apk],text=True)
assert "name='ru.truetv.launcher'" in badging and ("versionName='"+version+"'") in badging
assert 'android.permission.INTERNET' in badging # explicit expert ADB and visible-session file transfer
assert "targetSdkVersion:'34'" in badging
print('PASS package, version, resource alignment, no external font/native libraries, no legacy runtime classes')
print('APK SHA256',hashlib.sha256(apk.read_bytes()).hexdigest())
