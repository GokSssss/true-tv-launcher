package ru.truetv.launcher;
import java.io.IOException;

/** Only the selected package's restricted-settings app-op; never enables a service. */
public final class RestrictedOps {
 private RestrictedOps(){}
 public interface Runner {ShellReply run(String command)throws Exception;}
 public static String command(String pkg,int user,boolean change){SystemCommands.validate(pkg,user);return "cmd appops "+(change?"set":"get")+" --user "+user+" "+pkg+" ACCESS_RESTRICTED_SETTINGS"+(change?" allow":"");}
 public static boolean allowed(String output){
  if(output==null)return false;String mode=null;boolean defaultAllow=false;
  for(String line:output.split("\\r?\\n")){String text=line.trim();if(text.startsWith("ACCESS_RESTRICTED_SETTINGS:")){if(mode!=null)return false;String value=text.substring(text.indexOf(':')+1).trim();mode=value.split("[;\\s]",2)[0];}else if(text.equals("Default mode: allow"))defaultAllow=true;}
  return "allow".equals(mode)||defaultAllow&&(mode==null||"default".equals(mode));
 }
 public static void grant(Runner runner,String pkg,int user,int sdk)throws Exception {
  SystemCommands.validate(pkg,user);if(sdk<33)throw new IOException("Restricted settings apply to Android 13 or later");
  ShellReply before=runner.run(command(pkg,user,false));if(before.code!=0)throw new IOException("Cannot read restricted-settings mode: "+before.text);if(allowed(before.text))return;
  ShellReply changed=runner.run(command(pkg,user,true));if(changed.code!=0)throw new IOException("Restricted-settings change rejected: "+changed.text);
  ShellReply after=runner.run(command(pkg,user,false));if(after.code!=0||!allowed(after.text))throw new IOException("Restricted-settings allowance not confirmed: "+after.text);
 }
}
