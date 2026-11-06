package ru.truetv.launcher;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Disposable Android emulator only: uses the app's actual ADB client and restores the fixture. */
public final class ExpertCheck extends Instrumentation {
 private String endpoint="127.0.0.1:5555";private int checks;private final StringBuilder report=new StringBuilder();
 public void onCreate(Bundle b){super.onCreate(b);endpoint=b.getString("endpoint","127.0.0.1:5555");start();}
 private void check(boolean ok,String name){checks++;report.append(ok?"PASS ":"FAIL ").append(name).append('\n');if(!ok)throw new AssertionError(name);}
 private void main(Runnable r){Throwable[] error={null};runOnMainSync(()->{try{r.run();}catch(Throwable t){error[0]=t;}});waitForIdleSync();if(error[0]!=null)throw new AssertionError(error[0]);}
 public void onStart(){Bundle result=new Bundle();Context c=getTargetContext();ExpertBackend backend=null;boolean touched=false;String pkg="com.android.dreams.basic";int original=-1;try{
  check(Build.FINGERPRINT.contains("generic")||Build.MODEL.contains("Android SDK"),"guard: disposable VM only");PackageManager pm=c.getPackageManager();ApplicationInfo fixture=pm.getApplicationInfo(pkg,0);original=pm.getApplicationEnabledSetting(pkg);
  check((fixture.flags&ApplicationInfo.FLAG_SYSTEM)!=0,"fixture is a system package");check(ExpertBackend.user()==0,"disposable emulator user0");
  check(ExpertBackend.protectedReason(c,pm.getApplicationInfo("com.android.settings",0))!=null,"Settings protected");
  check(ExpertBackend.protectedReason(c,pm.getApplicationInfo(c.getPackageName(),0))!=null,"own launcher protected");
  check(ExpertBackend.protectedReason(c,fixture)==null,"noncritical fixture allowed");
  backend=new ExpertBackend(c,false,endpoint);check(backend.shell("id -u").code==0,"real local ADB and same-device proof");
  boolean rejected=false;try{backend.perform("com.android.settings",SystemCommands.Operation.DISABLE);}catch(Exception protectedTarget){rejected=true;}check(rejected,"protected mutation rejected before command");
  touched=true;backend.perform(pkg,SystemCommands.Operation.DISABLE);check(SystemCommands.confirmed(SystemCommands.Operation.DISABLE,pkg,backend.shell(SystemCommands.query(SystemCommands.Operation.DISABLE,pkg,0)).text),"app ADB disables fixture and reads state");
  backend.perform(pkg,SystemCommands.Operation.ENABLE);check(SystemCommands.confirmed(SystemCommands.Operation.ENABLE,pkg,backend.shell(SystemCommands.query(SystemCommands.Operation.ENABLE,pkg,0)).text),"app ADB enables fixture and reads state");
  backend.perform(pkg,SystemCommands.Operation.REMOVE);check(SystemCommands.confirmed(SystemCommands.Operation.REMOVE,pkg,backend.shell(SystemCommands.query(SystemCommands.Operation.REMOVE,pkg,0)).text),"app ADB removes fixture for user only");
  backend.perform(pkg,SystemCommands.Operation.RESTORE);check(SystemCommands.confirmed(SystemCommands.Operation.RESTORE,pkg,backend.shell(SystemCommands.query(SystemCommands.Operation.RESTORE,pkg,0)).text),"app ADB restores factory fixture");
  ShellReply restored=backend.shell("pm "+(original==0?"default-state":original==1?"enable":"disable-user")+" --user 0 "+pkg);check(restored.code==0,"fixture original enabled setting restored");touched=false;backend.close();backend=null;
  ExpertActivity screen=(ExpertActivity)startActivitySync(new Intent(c,ExpertActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  long end=System.currentTimeMillis()+20000;java.lang.reflect.Field field=ExpertActivity.class.getDeclaredField("all");field.setAccessible(true);while(((List<?>)field.get(screen)).isEmpty()&&System.currentTimeMillis()<end)Thread.sleep(100);
  check(!((List<?>)field.get(screen)).isEmpty(),"expert system list loads");
  main(()->{ViewGroup root=(ViewGroup)((ViewGroup)screen.findViewById(android.R.id.content)).getChildAt(0);check(root.getBackground()!=null,"expert theme applied");EditText search=null;CheckBox removed=null;ListView list=null;for(int i=0;i<root.getChildCount();i++){View view=root.getChildAt(i);if(view instanceof EditText)search=(EditText)view;if(view instanceof CheckBox)removed=(CheckBox)view;if(view instanceof ListView)list=(ListView)view;}check(search!=null&&removed!=null&&list!=null,"expert search and removed toggle");search.setText("com.android.dreams.basic");check(list.getCount()==1,"expert package search");search.setText("impossible_pkg_912321");check(list.getCount()==0,"expert empty search");screen.finish();});
  result.putString("report",report+"Checks="+checks);finish(Activity.RESULT_OK,result);
 }catch(Throwable e){java.io.StringWriter trace=new java.io.StringWriter();e.printStackTrace(new java.io.PrintWriter(trace));result.putString("report",report+"ERROR "+trace);finish(Activity.RESULT_CANCELED,result);}finally{
  if(touched&&backend!=null)try{backend.shell("pm install-existing --user 0 "+pkg);backend.shell("pm "+(original==0?"default-state":original==1?"enable":"disable-user")+" --user 0 "+pkg);}catch(Exception failure){android.util.Log.e("TrueTVTest","Fixture cleanup requires host recovery",failure);}
  if(backend!=null)try{backend.close();}catch(Exception ignored){}
 }}
}
