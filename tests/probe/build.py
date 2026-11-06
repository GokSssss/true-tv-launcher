from pathlib import Path
import os,subprocess,zipfile
root=Path(__file__).resolve().parents[2];os.chdir(root);cache=root/'.cache';b=root/'tests/probe/build';b.mkdir(exist_ok=True)
def run(*a):subprocess.run(list(map(str,a)),check=True)
for p in ('classes','dex'):(b/p).mkdir(exist_ok=True)
run(cache/'aapt2','link','-I',cache/'android34.jar','--manifest','tests/probe/AndroidManifest.xml','-o',b/'res.apk')
run('java','-jar',cache/'ecj.jar','-8','-nowarn','-cp',cache/'android34.jar','-d',b/'classes',*Path('tests/probe').glob('*.java'))
run('java','-cp',cache/'r8.jar','com.android.tools.r8.D8','--min-api','21','--lib',cache/'android34.jar','--output',b/'dex',*list((b/'classes').rglob('*.class')))
with zipfile.ZipFile(b/'unsigned.apk','w') as out:
 with zipfile.ZipFile(b/'res.apk') as src:
  for n in src.namelist():out.writestr(n,src.read(n))
 out.writestr('classes.dex',(b/'dex/classes.dex').read_bytes())
run('java','-cp',str(root/'app/build/portable/signer')+os.pathsep+str(cache/'apksig.jar'),'Signer','sign',root/'.signing/true-tv.p12',root/'.signing/password',b/'unsigned.apk',b/'probe.apk')
