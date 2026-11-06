package ru.truetv.launcher;
import java.io.IOException;
import java.util.*;

/** Explicit selected-service activation. Existing services are merged, never replaced by selection alone. */
public final class AccessibilitySettings {
 private AccessibilitySettings(){}
 public static String component(String raw){
  if(raw==null||raw.length()>1024)throw new IllegalArgumentException("Invalid service component");int split=raw.indexOf('/');if(split<1||split!=raw.lastIndexOf('/'))throw new IllegalArgumentException("Expected package/service");String pkg=raw.substring(0,split),name=raw.substring(split+1);if(!pkg.equals("android")&&!SystemCommands.validPackage(pkg)||!name.matches("[A-Za-z_.$][A-Za-z0-9_.$]*"))throw new IllegalArgumentException("Invalid service component");String full=name.startsWith(".")?pkg+name:name;if(!full.matches("[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)*"))throw new IllegalArgumentException("Invalid service class");return pkg+"/"+full;
 }
 public static LinkedHashSet<String> parse(String text){LinkedHashSet<String> result=new LinkedHashSet<>();if(text==null||text.trim().isEmpty()||text.trim().equals("null"))return result;if(text.length()>32768)throw new IllegalArgumentException("Service list too long");for(String item:text.trim().split(":",-1))result.add(component(item));return result;}
 private static void user(int user){if(user<0||user>21474)throw new IllegalArgumentException("Invalid user");}
 public static String read(int user,String key){user(user);if(!key.equals("enabled_accessibility_services")&&!key.equals("accessibility_enabled"))throw new IllegalArgumentException("Invalid setting");return "settings --user "+user+" get secure "+key;}
 private static String list(Collection<String> services){StringBuilder out=new StringBuilder();for(String item:services){if(out.length()>0)out.append(':');out.append(component(item));}return out.toString();}
 private static String state(RestrictedOps.Runner run,String command)throws Exception {ShellReply reply=run.run(command);if(reply.code!=0)throw new IOException("Cannot read Accessibility settings: "+reply.text);return reply.text.trim();}
 public static void enable(RestrictedOps.Runner run,Collection<String> selected,int user)throws Exception {
  user(user);LinkedHashSet<String> requested=new LinkedHashSet<>();for(String c:selected)requested.add(component(c));if(requested.isEmpty())throw new IllegalArgumentException("Select at least one service");
  LinkedHashSet<String> before=parse(state(run,read(user,"enabled_accessibility_services")));String enabled=state(run,read(user,"accessibility_enabled"));if(!enabled.equals("0")&&!enabled.equals("1")&&!enabled.equals("null"))throw new IOException("Unexpected Accessibility state");
  LinkedHashSet<String> current=parse(state(run,read(user,"enabled_accessibility_services")));if(!current.equals(before))throw new IOException("Service selection changed while reading; retry without overwriting it");LinkedHashSet<String> merged=new LinkedHashSet<>(current);merged.addAll(requested);
  if(!merged.equals(current)){ShellReply changed=run.run("settings --user "+user+" put secure enabled_accessibility_services '"+list(merged)+"'");if(changed.code!=0)throw new IOException("Service selection rejected: "+changed.text);}
  if(!enabled.equals("1")){ShellReply changed=run.run("settings --user "+user+" put secure accessibility_enabled 1");if(changed.code!=0)throw new IOException("Accessibility activation rejected: "+changed.text);}
  LinkedHashSet<String> after=parse(state(run,read(user,"enabled_accessibility_services")));if(!after.containsAll(merged)||!state(run,read(user,"accessibility_enabled")).equals("1"))throw new IOException("Accessibility settings were not confirmed; existing services must be preserved");
 }
 public static void disable(RestrictedOps.Runner run,Collection<String> selected,int user)throws Exception {
  user(user);LinkedHashSet<String> requested=new LinkedHashSet<>();for(String id:selected)requested.add(component(id));if(requested.isEmpty())throw new IllegalArgumentException("Select at least one service");
  LinkedHashSet<String> before=parse(state(run,read(user,"enabled_accessibility_services")));String enabled=state(run,read(user,"accessibility_enabled"));if(!enabled.equals("0")&&!enabled.equals("1")&&!enabled.equals("null"))throw new IOException("Unexpected Accessibility state");
  if(!parse(state(run,read(user,"enabled_accessibility_services"))).equals(before))throw new IOException("Service selection changed while reading; retry");LinkedHashSet<String> keep=new LinkedHashSet<>(before);keep.removeAll(requested);
  if(!keep.equals(before)){ShellReply changed=run.run("settings --user "+user+" put secure enabled_accessibility_services '"+list(keep)+"'");if(changed.code!=0)throw new IOException("Service deactivation rejected: "+changed.text);}
  if(keep.isEmpty()&&enabled.equals("1")){ShellReply changed=run.run("settings --user "+user+" put secure accessibility_enabled 0");if(changed.code!=0)throw new IOException("Accessibility deactivation rejected: "+changed.text);}
  LinkedHashSet<String> after=parse(state(run,read(user,"enabled_accessibility_services")));for(String id:requested)if(after.contains(id))throw new IOException("Service deactivation was not confirmed");if(!after.containsAll(keep))throw new IOException("Other services were not preserved");if(keep.isEmpty()&&enabled.equals("1")&&!state(run,read(user,"accessibility_enabled")).equals("0"))throw new IOException("Accessibility switch was not confirmed off");
 }
}
