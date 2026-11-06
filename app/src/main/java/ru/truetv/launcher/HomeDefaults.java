package ru.truetv.launcher;
import java.io.IOException;
/** Only an explicit user-approved assignment to this launcher's existing Home component. */
public final class HomeDefaults {
 private HomeDefaults(){}
 /** A successful shell exit is not evidence that Android selected this Home. */
 public static final class NotAssigned extends IOException {
  public final String resolved;public final int priority;
  NotAssigned(String text){super("True TV Launcher was not confirmed as default Home: "+text);String component=null;int rank=0;for(String line:text.split("\\r?\\n")){String v=line.trim();if(v.matches("[A-Za-z_][A-Za-z0-9_.]*/[A-Za-z_.$][A-Za-z0-9_.$]*")){if(component!=null){component=null;break;}component=v;}}java.util.regex.Matcher m=java.util.regex.Pattern.compile("(?:^|\\s)priority=(-?\\d+)(?:\\s|$)").matcher(text);if(m.find())try{rank=Integer.parseInt(m.group(1));}catch(NumberFormatException ignored){}resolved=component;priority=rank;}
  public boolean priorityOverridesPreference(int sdk){return sdk>=24&&sdk<=28&&priority>0&&resolved!=null;}
 }

 public static String query(int user){SystemCommands.validate("ru.truetv.launcher",user);return "cmd package resolve-activity --brief --user "+user+" -a android.intent.action.MAIN -c android.intent.category.HOME";}
 public static boolean isOwn(String text){int components=0;boolean own=false;for(String line:text.split("\\r?\\n")){String value=line.trim();if(value.contains("/")){components++;if(value.equals("ru.truetv.launcher/.HomeActivity")||value.equals("ru.truetv.launcher/ru.truetv.launcher.HomeActivity"))own=true;}}return own&&components==1;}
 /** Explicit confirmed freeze only: avoid the stock-Home assignment deadlock; restore on failure. */
 public static void freezeCurrent(RestrictedOps.Runner run,String pkg,int user,int sdk,int enabledState)throws Exception {
  SystemCommands.validate(pkg,user);if(pkg.equals("ru.truetv.launcher")||pkg.equals("android"))throw new IllegalArgumentException("Cannot freeze this launcher or android");
  if(sdk<24||enabledState!=0&&enabledState!=1)throw new IOException("Cannot safely restore this Home state on this Android version");
  ShellReply current=run.run(query(user));if(current.code!=0)throw new IOException("Cannot read current Home");NotAssigned previous=new NotAssigned(current.text);if(previous.resolved==null||!previous.resolved.startsWith(pkg+"/"))throw new IOException("Selected app is no longer the current Home; refresh first");
  ShellReply own=run.run(query(user)+" -p ru.truetv.launcher");if(own.code!=0||!isOwn(own.text))throw new IOException("True TV Home is unavailable; stock Home was not disabled");
  try{assign(run,user,sdk);}catch(NotAssigned blocked){if(!blocked.priorityOverridesPreference(sdk)||blocked.resolved==null||!blocked.resolved.startsWith(pkg+"/"))throw blocked;}
  try {
   ShellReply disabled=run.run(SystemCommands.mutation(SystemCommands.Operation.DISABLE,pkg,user));ShellReply state=run.run(SystemCommands.query(SystemCommands.Operation.DISABLE,pkg,user));
   if(disabled.code!=0||state.code!=0||!SystemCommands.confirmed(SystemCommands.Operation.DISABLE,pkg,state.text))throw new IOException("Stock Home freeze was not confirmed");
   assign(run,user,sdk);ShellReply after=run.run(query(user));if(after.code!=0||!isOwn(after.text))throw new NotAssigned(after.text);
  }catch(Exception failure){
   try{ShellReply restored=run.run("pm "+(enabledState==0?"default-state":"enable")+" --user "+user+" "+pkg);ShellReply state=run.run(SystemCommands.query(SystemCommands.Operation.ENABLE,pkg,user));if(restored.code!=0||state.code!=0||!SystemCommands.confirmed(SystemCommands.Operation.ENABLE,pkg,state.text))throw new IOException("Stock Home restoration was not confirmed");ShellReply preferred=run.run("cmd package set-home-activity --user "+user+" "+previous.resolved);if(preferred.code!=0)throw new IOException("Cannot restore previous Home preference");ShellReply restoredHome=run.run(query(user));if(restoredHome.code!=0||!previous.resolved.equals(new NotAssigned(restoredHome.text).resolved))throw new IOException("Previous Home resolution was not confirmed: "+restoredHome.text);}
   catch(Exception rollback){failure.addSuppressed(rollback);throw new IOException("Home switch failed; stock Home restoration also needs attention: "+rollback.getMessage(),failure);}
   throw new IOException("Home switch failed; previous stock Home state was restored: "+failure.getMessage(),failure);
  }
 }
 public static void assign(RestrictedOps.Runner run,int user,int sdk)throws Exception {
  if(sdk<24)throw new IOException("Use Android Home settings on Android below 7");ShellReply before=run.run(query(user));if(before.code!=0)throw new IOException("Cannot read default Home app: "+before.text);
  if(isOwn(before.text)){if(sdk<29)return;ShellReply role=run.run("cmd role get-role-holders --user "+user+" android.app.role.HOME");if(role.code==0&&role.text.trim().equals("ru.truetv.launcher"))return;}
  boolean roleAssigned=sdk>=29;ShellReply changed=run.run(roleAssigned?"cmd role add-role-holder --user "+user+" android.app.role.HOME ru.truetv.launcher 1":"cmd package set-home-activity --user "+user+" ru.truetv.launcher/ru.truetv.launcher.HomeActivity");
  if(roleAssigned&&changed.code!=0){roleAssigned=false;changed=run.run("cmd package set-home-activity --user "+user+" ru.truetv.launcher");}
  if(changed.code!=0)throw new IOException("Default Home change rejected: "+changed.text);ShellReply after=run.run(query(user));if(after.code!=0)throw new IOException("Cannot verify default Home: "+after.text);if(!isOwn(after.text))throw new NotAssigned(after.text);
  if(roleAssigned){ShellReply holders=run.run("cmd role get-role-holders --user "+user+" android.app.role.HOME");if(holders.code!=0||!holders.text.trim().equals("ru.truetv.launcher"))throw new IOException("Home role holder was not confirmed: "+holders.text);}
 }
}
