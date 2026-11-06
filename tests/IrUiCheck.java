package ru.truetv.launcher;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/** Disposable emulator only. Never requests su, writes driver maps or injects IR. */
public final class IrUiCheck extends Instrumentation {
 private final StringBuilder report=new StringBuilder();private int checks;
 public void onCreate(Bundle b){super.onCreate(b);start();}
 private void check(boolean value,String label){checks++;report.append(value?"PASS ":"FAIL ").append(label).append('\n');if(!value)throw new AssertionError(label);}
 private void main(Runnable run){Throwable[] failure={null};runOnMainSync(()->{try{run.run();}catch(Throwable e){failure[0]=e;}});waitForIdleSync();if(failure[0]!=null)throw new AssertionError(failure[0]);}
 private Object field(Object owner,String name)throws Exception {Field f=owner.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(owner);}
 private void call(Object owner,String name,Class<?>[] types,Object...args){try{Method method=owner.getClass().getDeclaredMethod(name,types);method.setAccessible(true);method.invoke(owner,args);}catch(Exception e){throw new AssertionError(e);}}
 private ActionSheet sheet()throws Exception {Field f=ActionSheet.class.getDeclaredField("active");f.setAccessible(true);return (ActionSheet)((java.lang.ref.WeakReference<?>)f.get(null)).get();}
 public void onStart(){Bundle result=new Bundle();Context c=getTargetContext();SharedPreferences draft=c.getSharedPreferences("ir_draft",0);Map<String,?> original=new HashMap<>(draft.getAll());IrRemoteActivity[] activity={null};ReportExport.Result[] saved={null};try{
  check(!new File(c.getExternalFilesDir(null),"IR-diagnostic.txt").exists(),"private report path starts empty and is not used");draft.edit().clear().putString("up","ff00:49").commit();
  SettingsActivity settings=(SettingsActivity)startActivitySync(new Intent(c,SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));main(()->check(settings.findPreference("ir_remote")!=null,"expert settings contain IR entry"));main(settings::finish);
  activity[0]=(IrRemoteActivity)startActivitySync(new Intent(c,IrRemoteActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));IrRemoteActivity a=activity[0];Thread.sleep(350);waitForIdleSync();Button diagnose=(Button)field(a,"diagnose"),learn=(Button)field(a,"learn");TextView text=(TextView)field(a,"report"),status=(TextView)field(a,"status");
  main(()->{check(!(Boolean)getActive(a),"opening screen never starts root");check(text.getText().toString().contains("up=ff00:49"),"draft survives activity launch");check(text.getText().toString().contains("NOT APPLIED"),"report labels unapplied draft");check(status.getText().length()>20,"unsupported apply is explained");diagnose.requestFocusFromTouch();check(diagnose.hasFocus(),"receiver button takes remote focus");});
  sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);waitForIdleSync();main(()->check(learn.hasFocus(),"Dpad moves to codes button"));
  main(()->call(a,"confirm",new Class<?>[]{int.class,IrCodes.Code.class},1,new IrCodes.Code(0xff00,0x02)));ActionSheet confirm=sheet();main(()->{check(confirm.rows.size()==2,"learned code requires explicit confirmation");confirm.rows.get(0).performClick();});check(draft.getString("down","").equals("ff00:02"),"confirmed code saved only in app preferences");
  main(()->call(a,"export",new Class<?>[0]));ActionSheet export=sheet();main(()->{check(export.rows.size()==3,"report provides copy and TXT export plus Cancel");export.rows.get(1).performClick();});saved[0]=(ReportExport.Result)field(a,"lastExport");check(saved[0]!=null,"report exported without SAF picker; status="+status.getText());java.io.InputStream exported=c.getContentResolver().openInputStream(saved[0].uri);ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] chunk=new byte[2048];int n;try(InputStream in=exported){while((n=in.read(chunk))>=0)bytes.write(chunk,0,n);}String data=new String(bytes.toByteArray(),java.nio.charset.StandardCharsets.UTF_8);if(Build.VERSION.SDK_INT<29){check(new File(saved[0].uri.getPath()).getParentFile().getCanonicalFile().equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getCanonicalFile()),"report saved in public Downloads");}else check(saved[0].uri.getAuthority().equals("media"),"report saved via MediaStore Downloads");check(!new File(c.getExternalFilesDir(null),"IR-diagnostic.txt").exists(),"no fallback report in private app folder");check(data.contains("down=ff00:02")&&data.contains("NOT APPLIED"),"export contains draft and its actual status");
  main(()->call(a,"codeMenu",new Class<?>[]{int.class},1));ActionSheet remove=sheet();main(()->remove.rows.get(2).performClick());check(!draft.contains("down"),"single code can be removed");
  android.graphics.Bitmap image=getUiAutomation().takeScreenshot();File shot=new File(c.getExternalFilesDir(null),"IR-screen.png");try(FileOutputStream out=new FileOutputStream(shot)){image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}image.recycle();
  main(a::finish);activity[0]=null;check(!c.getPackageManager().getActivityInfo(new android.content.ComponentName(c,IrRemoteActivity.class),0).exported,"IR activity not exported");
  result.putString("report",report+"Checks="+checks+"; no root/hardware claim");finish(Activity.RESULT_OK,result);
 }catch(Throwable e){StringWriter out=new StringWriter();e.printStackTrace(new PrintWriter(out));result.putString("report",report+"ERROR "+out);finish(Activity.RESULT_CANCELED,result);}finally{if(activity[0]!=null)main(activity[0]::finish);draft.edit().clear().commit();SharedPreferences.Editor restore=draft.edit();for(Map.Entry<String,?> e:original.entrySet())if(e.getValue() instanceof String)restore.putString(e.getKey(),(String)e.getValue());restore.commit();if(saved[0]!=null){if(saved[0].uri.getScheme().equals("file"))new File(saved[0].uri.getPath()).delete();else c.getContentResolver().delete(saved[0].uri,null,null);}}}
 private Object getActive(Object a){try{return field(a,"active");}catch(Exception e){throw new AssertionError(e);}}
}
