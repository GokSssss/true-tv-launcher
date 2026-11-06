package ru.truetv.launcher;
/** A closed command vocabulary; package names and users never become shell syntax. */
public final class SystemCommands {
 public enum Operation {DISABLE,ENABLE,REMOVE,RESTORE}
 private SystemCommands(){}
 public static boolean validPackage(String pkg){return pkg!=null&&pkg.length()<256&&pkg.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+");}
 public static void validate(String pkg,int user){if(!validPackage(pkg)||user<0||user>21474)throw new IllegalArgumentException("Invalid package or user");}
 public static String mutation(Operation op,String pkg,int user){validate(pkg,user);switch(op){case DISABLE:return "pm disable-user --user "+user+" "+pkg;case ENABLE:return "pm enable --user "+user+" "+pkg;case REMOVE:return "pm uninstall -k --user "+user+" "+pkg;case RESTORE:return "pm install-existing --user "+user+" "+pkg;default:throw new IllegalArgumentException("Operation");}}
 public static String query(Operation op,String pkg,int user){validate(pkg,user);return "pm list packages "+(op==Operation.DISABLE?"-d ":op==Operation.ENABLE?"-e ":"")+"--user "+user+" "+pkg;}
 public static boolean confirmed(Operation op,String pkg,String output){boolean found=false;for(String line:output.split("\\r?\\n")){String value=line.trim();if(!value.isEmpty()&&!value.startsWith("package:"))throw new IllegalArgumentException("Unexpected package-manager output");if(value.startsWith("package:")&&!value.substring(8).equals("android")&&!validPackage(value.substring(8)))throw new IllegalArgumentException("Malformed package-manager output");if(value.equals("package:"+pkg))found=true;}return op==Operation.REMOVE?!found:found;}
}
