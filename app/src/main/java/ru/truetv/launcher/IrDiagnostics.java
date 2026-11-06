package ru.truetv.launcher;

/** Fixed, read-only root queries. Never constructs commands from log/profile/user text. */
public final class IrDiagnostics {
 private IrDiagnostics(){}
 public interface Runner {ShellReply run(String command,long timeout)throws Exception;}
 static final String LOG="dmesg 2>/dev/null | grep -iE 'meson[-_ ]remote|amremote|invalid custom|receive[ _]scancode|scancode .*undefined|framecode=' | tail -n 80";
 static final String[] CONFIGS={"/system/etc/remote.conf","/system/etc/remote.cfg","/vendor/etc/remote.conf","/vendor/etc/remote.cfg","/vendor/etc/remote.tab1","/vendor/etc/remote.tab2","/vendor/etc/remote.tab3","/system/etc/remote.tab1","/system/etc/remote.tab2"};
 public static void root(Runner runner)throws Exception {ShellReply reply=runner.run("id -u",20000);if(reply.code!=0||!reply.text.trim().equals("0"))throw new SecurityException("Root uid 0 was not granted");}
 public static String configCommand(String path){boolean allowed=false;for(String p:CONFIGS)if(p.equals(path))allowed=true;if(!allowed)throw new IllegalArgumentException("IR path not allowed");return "if [ -f '"+path+"' ]; then head -c 8192 '"+path+"'; else echo '[absent]'; fi";}
 public static String log(Runner runner)throws Exception {ShellReply r=runner.run(LOG,4000);if(r.code!=0)throw new IllegalStateException("IR log unavailable");return r.text;}
 private static String read(Runner runner,String command)throws Exception {ShellReply r=runner.run(command,5000);return r.code==0?r.text:"[unavailable, exit "+r.code+"]";}
 public static String collect(Runner runner)throws Exception {
  root(runner);StringBuilder report=new StringBuilder("Root uid: 0\n\nIR input devices (Name / Handlers only)\n");
  String devices=IrCodes.devices(read(runner,"cat /proc/bus/input/devices"));report.append(devices.isEmpty()?"[no named IR device found; not a compatibility verdict]":devices).append('\n');
  report.append("\nAmlogic interface / tools\n").append(read(runner,"for p in /sys/class/remote/amremote /sys/class/rc /vendor/bin/remotecfg /system/bin/remotecfg; do if [ -e \"$p\" ]; then echo \"$p [present]\"; fi; done; for p in /sys/class/remote/amremote/debug_enable /sys/class/remote/amremote/protocol; do if [ -f \"$p\" ]; then echo \"$p\"; head -c 256 \"$p\"; echo; fi; done"));
  for(String path:CONFIGS)report.append("\n\n").append(path).append(" (max 8192 bytes)\n").append(read(runner,configCommand(path)));
  report.append("\n\nIR kernel lines (at most 80; may be historical)\n").append(log(runner));
  report.append("\n\nRead-only diagnostic. No remount, debug toggle, driver map, boot or system file was changed. Absent log/config does not prove incompatible hardware. A driver-specific adapter is required before applying any map.\n");return report.toString();
 }
}
