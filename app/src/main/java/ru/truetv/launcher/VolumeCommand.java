package ru.truetv.launcher;
/** Only this APK and this namespaced volume helper. No arbitrary command input. */
final class VolumeCommand {
 static final String BASE="/data/local/tmp/true-tv-launcher-volume";
 private VolumeCommand(){}
 static String entry(String apk){if(apk==null||!apk.startsWith("/")||!apk.endsWith(".apk")||apk.indexOf('\n')>=0||apk.indexOf('\r')>=0)throw new IllegalArgumentException("Invalid installed APK path");return "CLASSPATH="+PackagePolicy.quote(apk)+" app_process /system/bin ru.truetv.launcher.VolumeShell";}
 static String status(String apk){return entry(apk)+" --status";}
 static String stop(String apk){return entry(apk)+" --stop";}
 static String start(String apk){return "umask 077; nohup setsid sh -c "+PackagePolicy.quote(entry(apk))+" >"+PackagePolicy.quote(BASE+".log")+" 2>&1 </dev/null & sleep 1";}
}
