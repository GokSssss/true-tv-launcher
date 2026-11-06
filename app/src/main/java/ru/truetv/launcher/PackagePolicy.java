package ru.truetv.launcher;
import java.util.*;
import java.util.regex.*;

/** Allowlisted package-manager and exact factory APK paths; never rm a directory. */
public final class PackagePolicy {
 private PackagePolicy(){}
 public static List<Integer> users(String output){ArrayList<Integer> result=new ArrayList<>();Matcher m=Pattern.compile("UserInfo\\{([0-9]+):").matcher(output);while(m.find()){int user=Integer.parseInt(m.group(1));SystemCommands.validate("test.package",user);if(!result.contains(user))result.add(user);}if(result.isEmpty())throw new IllegalArgumentException("Не удалось определить профили Android");return result;}
 public static boolean factoryPath(String path){if(path==null||path.contains("..")||!path.matches("/[A-Za-z0-9_./+=-]+\\.apk"))return false;return path.matches("/(?:system/(?:app|priv-app)|system/vendor/(?:app|priv-app)|system/product/(?:app|priv-app)|product/(?:app|priv-app)|vendor/(?:app|priv-app)|system_ext/(?:app|priv-app))/[^/]+/.+\\.apk")||path.matches("/system/(?:app|priv-app)/[^/]+\\.apk");}
 public static String removeProfile(String pkg,int user){SystemCommands.validate(pkg,user);return "pm uninstall --user "+user+" "+pkg;}
 public static String quote(String value){return "'"+value.replace("'","'\\''")+"'";}
 public static String deleteApk(String path){if(!factoryPath(path))throw new IllegalArgumentException("Недопустимый путь заводского APK");return "rm -- "+quote(path);}
 public static Set<String> codePaths(String dump){Set<String> paths=new LinkedHashSet<>();Matcher m=Pattern.compile("(?:codePath=|package:)(/[^\\s]+)").matcher(dump);while(m.find())paths.add(m.group(1));return paths;}
}
