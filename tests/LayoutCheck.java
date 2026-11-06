package ru.truetv.launcher;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.security.*;
import java.util.*;

/** Read-only platform/UI checks on a disposable emulator; no package mutations. */
public final class LayoutCheck extends Instrumentation {
 private int checks;private final StringBuilder report=new StringBuilder();
 public void onCreate(Bundle b){super.onCreate(b);start();}
 private void check(boolean ok,String text){checks++;report.append(ok?"PASS ":"FAIL ").append(text).append('\n');if(!ok)throw new AssertionError(text);}
 private void main(Runnable task){Throwable[] error={null};runOnMainSync(()->{try{task.run();}catch(Throwable e){error[0]=e;}});waitForIdleSync();if(error[0]!=null)throw new AssertionError(error[0]);}
 private void shot(String name)throws Exception{Bitmap image=getUiAutomation().takeScreenshot();File dir=new File(getTargetContext().getExternalFilesDir(null),"layout"+BuildConfig.VERSION_CODE);dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))){image.compress(Bitmap.CompressFormat.PNG,100,out);}image.recycle();}
 public void onStart(){Bundle result=new Bundle();Context c=getTargetContext();SettingsStore store=new SettingsStore(c);try{
  java.lang.reflect.Method keys=ExpertBackend.class.getDeclaredMethod("keys",Context.class);keys.setAccessible(true);KeyPair first=(KeyPair)keys.invoke(null,c),second=(KeyPair)keys.invoke(null,c);check(Arrays.equals(first.getPublic().getEncoded(),second.getPublic().getEncoded()),"Android saved RSA identity reopened without CRT cast");Signature signature=Signature.getInstance("SHA256withRSA");signature.initSign(second.getPrivate());signature.update(new byte[]{1,2,3});byte[] signed=signature.sign();signature.initVerify(first.getPublic());signature.update(new byte[]{1,2,3});check(signature.verify(signed),"Android restored key signs for existing public identity");

  main(()->{Bitmap source=Bitmap.createBitmap(96,48,Bitmap.Config.ARGB_8888);source.eraseColor(Color.GREEN);Bitmap fit=IconPainter.render(new BitmapDrawable(c.getResources(),source),96);check(Color.alpha(fit.getPixel(48,0))==0&&Color.green(fit.getPixel(48,48))>200,"legacy artwork aspect preserved");source.recycle();fit.recycle();if(Build.VERSION.SDK_INT>=26){Bitmap adaptive=IconPainter.render(new AdaptiveIconDrawable(new ColorDrawable(Color.BLUE),new ColorDrawable(Color.TRANSPARENT)),96);check(Color.alpha(adaptive.getPixel(8,8))>0,"adaptive artwork no longer cropped to circle");check(Color.alpha(adaptive.getPixel(0,0))==0,"adaptive square has rounded corners");adaptive.recycle();}});
  store.assign(0,"com.android.settings");store.data.edit().putString("columns","5").putString("rows","4").putBoolean("labels",false).putString("icon","64").putBoolean("bt_asked",true).putBoolean("bt_count",false).apply();HomeActivity home=VmWindows.home(this);Thread.sleep(1000);waitForIdleSync();
  main(()->{Board board=home.board;check(board.getChildAt(0).getLeft()==0&&board.getChildAt(4).getRight()==board.getWidth(),"5x4 spans full width");Tile tile=(Tile)board.getChildAt(0);View frame=tile.getChildAt(0),box=(View)tile.image.getParent();check(box.getWidth()==Ui.dp(c,64),"fixed desktop icon size");check(frame.getWidth()-box.getWidth()==Ui.dp(c,16),"frame has constant8dp inset");check(frame.getWidth()==frame.getHeight(),"no-label frame square");tile.requestFocusFromTouch();check(frame.getWidth()<tile.getWidth(),"focus frame independent of wide slot");if(Build.VERSION.SDK_INT>=26)check(!tile.getDefaultFocusHighlightEnabled(),"no second Android highlight over entire slot");});shot("desktop-fixed");
  main(()->{store.data.edit().putString("icon","0").apply();home.appsChanged();});Thread.sleep(250);waitForIdleSync();shot("desktop-auto");main(home::finish);
  ExpertActivity expert=(ExpertActivity)startActivitySync(new Intent(c,ExpertActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));java.lang.reflect.Field data=ExpertActivity.class.getDeclaredField("all");data.setAccessible(true);long deadline=System.currentTimeMillis()+30000;while(((List<?>)data.get(expert)).isEmpty()&&System.currentTimeMillis()<deadline){Thread.sleep(100);waitForIdleSync();}check(!((List<?>)data.get(expert)).isEmpty(),"system app list loads");
  ViewGroup root=(ViewGroup)((ViewGroup)expert.findViewById(android.R.id.content)).getChildAt(0);LinearLayout buttons=(LinearLayout)root.getChildAt(2);Button connect=(Button)buttons.getChildAt(0),refresh=(Button)buttons.getChildAt(1);ListView[] list={null};for(int i=0;i<root.getChildCount();i++)if(root.getChildAt(i) instanceof ListView)list[0]=(ListView)root.getChildAt(i);
  main(()->{check(list[0].getAdapter().getView(0,null,list[0]) instanceof ExpertActivity.ExpertRow,"system rows include icon and two text fields");list[0].setSelection(0);list[0].requestFocusFromTouch();});main(()->check(list[0].hasFocus(),"list takes remote focus"));main(()->connect.requestFocusFromTouch());main(()->{check(connect.hasFocus()&&!list[0].hasFocus(),"only top button has focus");check(list[0].getSelector() instanceof ColorDrawable&&Color.alpha(((ColorDrawable)list[0].getSelector()).getColor())==0,"old row highlight cleared when focus leaves list");check(connect.getCurrentTextColor()!=refresh.getCurrentTextColor(),"active button has high-contrast text");});shot("expert-connect-focus");sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);waitForIdleSync();main(()->check(refresh.hasFocus(),"remote right moves to Refresh"));shot("expert-refresh-focus");main(()->ActionSheet.show(expert,"Меню приложения",new ActionSheet.Item("Перезапустить приложение","restart",()->{}),new ActionSheet.Item("Переместить","move",()->{})));Thread.sleep(200);shot("restart-menu");main(expert::finish);
  result.putString("report",report+"Checks="+checks);finish(Activity.RESULT_OK,result);
 }catch(Throwable e){StringWriter out=new StringWriter();e.printStackTrace(new PrintWriter(out));result.putString("report",report+"ERROR "+out);finish(Activity.RESULT_CANCELED,result);}}
}
