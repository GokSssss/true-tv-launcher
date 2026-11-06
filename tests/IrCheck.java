package ru.truetv.launcher;
import java.util.*;

/** Host checks for diagnostic parsing and the read-only root boundary, not real hardware. */
public final class IrCheck {
 private static int count;
 private static void ok(boolean value){count++;if(!value)throw new AssertionError("IR check "+count);}
 private static void rejects(Runnable action){try{action.run();throw new AssertionError("Expected rejection");}catch(IllegalArgumentException expected){count++;}}
 public static void main(String[] args)throws Exception {
  ok(IrCodes.parse("[12.0] meson-remote: invalid custom:0xb649ff00").wire().equals("ff00:49"));
  ok(IrCodes.parse("framecode=0xA55AFE01").wire().equals("fe01:5a"));
  ok(IrCodes.parse("framecode=0x005afe01")==null);
  ok(IrCodes.parse("framecode=0xa55afe010")==null);
  ok(IrCodes.parse("receive scancode=0x49").wire().equals("?:49"));
  ok(IrCodes.parse("scancode 49 undefined").wire().equals("?:49"));
  ok(IrCodes.parse("receive_scancode: 0xff").wire().equals("?:ff"));
  ok(IrCodes.parse("receive scancode=0x100")==null);
  ok(IrCodes.parse("Unrelated kernel log 0xb649ff00")==null);
  ok(IrCodes.fromWire("FFFF:FF").wire().equals("ffff:ff"));ok(IrCodes.fromWire("?:00").wire().equals("?:00"));
  for(String bad:new String[]{"","ff:ff","ffff:fff","zzzz:ff","0000:ff;id","0000:ff\n","$(id):ff"})rejects(()->IrCodes.fromWire(bad));
  IrCodes.Learning session=new IrCodes.Learning("[10] invalid custom:0xb649ff00");
  ok(session.accept("[10] invalid custom:0xb649ff00")==null);
  ok(session.accept("[11] invalid custom:0xb649ff00")==null);
  ok(session.accept("[11] invalid custom:0xb649ff00")==null);
  ok(session.accept("[12] invalid custom:0xb649ff00").wire().equals("ff00:49"));
  session=new IrCodes.Learning("");ok(session.accept("[1] receive scancode=0x01\n[2] receive scancode=0x02")==null);
  ok(session.accept("[3] receive scancode=0x02").wire().equals("?:02"));
  String devices=IrCodes.devices("I: Bus=0010\nN: Name=\"meson-remote\"\nP: Phys=secret\nS: Sysfs=/devices/remote\nU: Uniq=private\nH: Handlers=kbd event2\n\nN: Name=\"Bluetooth keyboard\"\nU: Uniq=address\nH: Handlers=event3\n");
  ok(devices.contains("meson-remote"));ok(devices.contains("event2"));ok(!devices.contains("private"));ok(!devices.contains("secret"));ok(!devices.contains("Bluetooth"));ok(!devices.contains("event3"));
  for(String path:IrDiagnostics.CONFIGS)ok(IrDiagnostics.configCommand(path).contains("head -c 8192 '"+path+"'"));
  for(String bad:new String[]{"/data/x","/vendor/etc/remote.conf; reboot","/vendor/etc/../etc/remote.conf","/vendor/etc/remote.conf'"})rejects(()->IrDiagnostics.configCommand(bad));
  List<String> calls=new ArrayList<>();IrDiagnostics.Runner fake=(cmd,timeout)->{calls.add(cmd);ok(timeout>0&&timeout<=20000);if(cmd.equals("id -u"))return new ShellReply(0,"0");if(cmd.contains("input/devices"))return new ShellReply(0,"N: Name=\"amremote\"\nH: Handlers=event1\nU: Uniq=hidden");if(cmd.equals(IrDiagnostics.LOG))return new ShellReply(0,"[4] invalid custom:0xb649ff00");return new ShellReply(0,"[absent]");};
  String report=IrDiagnostics.collect(fake);ok(report.contains("ff00"));ok(!report.contains("hidden"));ok(report.contains("Read-only"));ok(calls.size()==13);
  for(String cmd:calls){ok(!cmd.contains("dmesg -c"));ok(!cmd.contains("remount"));ok(!cmd.contains("echo 1"));ok(!cmd.contains(" > "));ok(!cmd.contains("su "));}
  calls.clear();try{IrDiagnostics.collect((cmd,timeout)->{calls.add(cmd);return new ShellReply(0,"2000");});throw new AssertionError("Unprivileged root accepted");}catch(SecurityException expected){ok(calls.size()==1);}
  try{IrDiagnostics.root((cmd,timeout)->new ShellReply(1,"0"));throw new AssertionError("Failed root accepted");}catch(SecurityException expected){count++;}
  System.out.println("IR diagnostic host checks: "+count+" PASS (no hardware/root claim)");
 }
}
