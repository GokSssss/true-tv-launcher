from pathlib import Path
import os,subprocess,zipfile,shutil
root=Path(__file__).resolve().parents[2];os.chdir(root);b=root/'tests/session/build';shutil.rmtree(b,ignore_errors=True);b.mkdir();cache=root/'.cache'
def run(*a):subprocess.run(list(map(str,a)),check=True)
(b/'manifest.xml').write_text('''<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="ru.truetv.session.tests"><uses-sdk android:minSdkVersion="21" android:targetSdkVersion="34"/><application><provider android:name="ru.truetv.launcher.SessionSource" android:authorities="ru.truetv.session.source" android:exported="true" /></application><instrumentation android:name="ru.truetv.launcher.SessionCheck" android:targetPackage="ru.truetv.launcher"/><instrumentation android:name="ru.truetv.launcher.PhoneSessionCheck" android:targetPackage="ru.truetv.totv"/></manifest>''')
run(cache/'aapt2','link','-o',b/'resources.apk','-I',cache/'android34.jar','--manifest',b/'manifest.xml')
(b/'classes').mkdir();(b/'dex').mkdir()
run('java','-jar',cache/'ecj.jar','-8','-nowarn','-cp',str(cache/'android34.jar')+os.pathsep+str(root/'app/build/portable/classes'),'-d',b/'classes','tests/VmWindows.java',*Path('tests/session').glob('*.java'))
run('java','-cp',cache/'r8.jar','com.android.tools.r8.D8','--min-api','21','--lib',cache/'android34.jar','--classpath',root/'app/build/portable/classes','--output',b/'dex',*list((b/'classes').rglob('*.class')))
with zipfile.ZipFile(b/'unsigned.apk','w') as z:
 with zipfile.ZipFile(b/'resources.apk') as src:
  for n in src.namelist():z.writestr(n,src.read(n))
 z.writestr('classes.dex',(b/'dex/classes.dex').read_bytes())
run('java','-cp',str(root/'app/build/portable/signer')+os.pathsep+str(cache/'apksig.jar'),'Signer','sign',root/'.signing/true-tv.p12',root/'.signing/password',b/'unsigned.apk',b/'tests.apk')
