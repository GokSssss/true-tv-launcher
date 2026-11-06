#!/usr/bin/env python3
"""Disposable emulator only: enable test service AFTER instrumentation starts.
Production launcher has no shell/ADB code. Never run against a personal TV.
"""
from pathlib import Path
import subprocess,time,argparse,shutil,os
root=Path(__file__).resolve().parent.parent
arg=argparse.ArgumentParser();arg.add_argument('--restart',action='store_true');args=arg.parse_args()
adb=os.environ.get('ADB') or shutil.which('adb') or '/workspace/android-test/platform-tools/adb'
def call(*items,timeout=30):return subprocess.check_output([adb,*items],text=True,timeout=timeout).strip()
report=root/'docs/validation';report.mkdir(parents=True,exist_ok=True);log=report/('restart-native.txt' if args.restart else 'stop-native.txt')
print(call('install','-r',str(root/'tests/build/tests.apk'),timeout=120),flush=True)
print(call('shell','am','start','-W','-n','ru.truetv.probe/.Probe',timeout=45),flush=True)
before=call('shell','pidof','ru.truetv.probe')
command=[adb,'shell','am','instrument','-w']
if args.restart:command+=['-e','restart','true']
command+=['ru.truetv.launcher.tests/ru.truetv.launcher.ControlCheck']
with log.open('w') as output:
 process=subprocess.Popen(command,stdout=output,stderr=subprocess.STDOUT)
 try:
  time.sleep(2)
  # Instrumentation force-stops target; toggle only on disposable emulator to rebind.
  call('shell','settings','delete','secure','enabled_accessibility_services')
  call('shell','settings','put','secure','enabled_accessibility_services','ru.truetv.launcher/ru.truetv.launcher.ControlService')
  call('shell','settings','put','secure','accessibility_enabled','1')
  process.wait(timeout=90)
 finally:
  if process.poll() is None:process.terminate();process.wait(timeout=15)
result=log.read_text();print(result,flush=True)
time.sleep(.6)
after=subprocess.run([adb,'shell','pidof','ru.truetv.probe'],capture_output=True,text=True,timeout=15).stdout.strip()
with log.open('a') as output:output.write('Probe PID before='+before+' after='+after+'\n')
assert 'report=PASS' in result,result
assert after and after!=before if args.restart else not after,(before,after)
print('PASS external PID check:',before,'→',after or 'absent')
