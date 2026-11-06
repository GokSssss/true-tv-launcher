package ru.truetv.launcher;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import java.io.*;
import java.util.*;
/** Only disposable VM/probe: activate after instrumentation starts, preserve original service settings. */
public final class ControlCheck extends Instrumentation {
 private boolean restart;private UiAutomation ui;private String endpoint="127.0.0.1:5555";private ExpertBackend backend;private String oldServices,oldEnabled;
 public void onCreate(Bundle b){super.onCreate(b);restart="true".equals(b.getString("restart"));endpoint=b.getString("endpoint","127.0.0.1:5555");ui=getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);start();}
 private boolean stopped()throws Exception{return (getTargetContext().getPackageManager().getApplicationInfo("ru.truetv.probe",0).flags&ApplicationInfo.FLAG_STOPPED)!=0;}
 private String shell(String s)throws Exception{try(ParcelFileDescriptor fd=ui.executeShellCommand(s);InputStream in=new FileInputStream(fd.getFileDescriptor());ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[2048];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return new String(out.toByteArray(),"UTF-8").trim();}}
 public void onStart(){Bundle result=new Bundle();try{
  if(!(Build.FINGERPRINT.contains("generic")||Build.MODEL.contains("Android SDK"))||android.os.Process.myUid()/100000!=0)throw new AssertionError("Disposable VM user0 only");
  HomeActivity home=VmWindows.home(this);oldServices=shell(AccessibilitySettings.read(0,"enabled_accessibility_services"));oldEnabled=shell(AccessibilitySettings.read(0,"accessibility_enabled"));backend=new ExpertBackend(getTargetContext(),false,endpoint);AccessibilitySettings.enable(backend::shell,Collections.singleton("ru.truetv.launcher/ru.truetv.launcher.ControlService"),0);
  for(int n=0;n<120&&!Controls.ready(home);n++)Thread.sleep(250);if(!Controls.ready(home))throw new AssertionError("service not connected after actual in-process activation");if(stopped())throw new AssertionError("probe must be running before test");runOnMainSync(()->Controls.stop(home,"ru.truetv.probe",restart));
  boolean observed=false;for(int n=0;n<180;n++){if(stopped()){observed=true;break;}Thread.sleep(250);}if(!observed)throw new AssertionError("stop was not verified");
  if(restart){boolean started=false;for(int n=0;n<80;n++){Thread.sleep(250);if(!stopped()){started=true;break;}}if(!started)throw new AssertionError("restart did not launch");}
  else{long until=SystemClock.uptimeMillis()+15000;while(!home.hasWindowFocus()&&SystemClock.uptimeMillis()<until)Thread.sleep(100);if(!home.hasWindowFocus())throw new AssertionError("Did not return to same Home after stopping probe");}
  result.putString("report","PASS selected probe "+(restart?"stopped and relaunched":"force-stopped and returned to same Home")+" through actual Accessibility Settings workflow");result.putBoolean("passed",true);
 }catch(Throwable t){result.putString("report","FAIL "+t);result.putBoolean("passed",false);}finally{try{if(backend!=null)backend.close();if(oldServices!=null){if(oldServices.isEmpty()||oldServices.equals("null"))shell("settings delete secure enabled_accessibility_services");else{AccessibilitySettings.parse(oldServices);shell("settings put secure enabled_accessibility_services "+oldServices);}}if(oldEnabled!=null)shell(oldEnabled.equals("null")||oldEnabled.isEmpty()?"settings delete secure accessibility_enabled":"settings put secure accessibility_enabled "+(oldEnabled.equals("1")?"1":"0"));}catch(Exception e){result.putBoolean("passed",false);result.putString("report",result.getString("report")+"; cleanup failed: "+e);}}finish(result.getBoolean("passed")?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);}
}
