package ru.truetv.launcher;
import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Native instrumentation. Assertions inspect real views, focus and navigation on Android. */
public final class UiCheck extends Instrumentation {
 private int checks;private final StringBuilder report=new StringBuilder();
 public void onCreate(Bundle b){super.onCreate(b);start();}
 private void check(boolean ok,String name){checks++;report.append(ok?"PASS ":"FAIL ").append(name).append('\n');if(!ok)throw new AssertionError(name);}
 private void main(Runnable r){final Throwable[] error=new Throwable[1];runOnMainSync(()->{try{r.run();}catch(Throwable t){error[0]=t;}});waitForIdleSync();if(error[0]!=null)throw new AssertionError(error[0]);}
 private <T extends Activity>T open(Class<T> type,int slot){if(type==HomeActivity.class)return (T)VmWindows.home(this);return (T)startActivitySync(new Intent(getTargetContext(),type).putExtra("slot",slot).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
 private final ArrayList<View> found=new ArrayList<>();
 private void find(View v){found.add(v);if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)find(((ViewGroup)v).getChildAt(i));}
 private List<View> tree(Activity a){found.clear();find(a.getWindow().getDecorView());return new ArrayList<>(found);}
 public void onStart(){Bundle result=new Bundle();try{
  Context c=getTargetContext();SettingsStore s=new SettingsStore(c);s.data.edit().clear().putBoolean("bt_asked",true).apply();
  HomeActivity home=open(HomeActivity.class,-1);Thread.sleep(1500);waitForIdleSync();
  main(()->{check(home.board.getChildCount()==20,"owner default 5x4 board");check(home.header.getChildCount()==8,"fixed header controls/status/date-clock");check(home.board.getChildAt(0).getLeft()==0&&home.board.getChildAt(4).getRight()==home.board.getWidth(),"desktop fills screen width independently of rows");});
  final ActionSheet[] menu=new ActionSheet[1];main(()->{menu[0]=ActionSheet.show(home,"Test",new ActionSheet.Item("Unavailable","close",()->{throw new AssertionError("disabled action executed");},()->false),new ActionSheet.Item("Other","info",()->{}));check(menu[0].rows.size()==3,"Cancel is final semantic row");check(menu[0].rows.get(0).isFocusable()&&menu[0].rows.get(0).isEnabled(),"grey item remains focusable and clickable");});
  main(()->check(menu[0].rows.get(0).hasFocus(),"initial focus on first row after layout"));sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);waitForIdleSync();main(()->check(menu[0].rows.get(2).hasFocus(),"first to Cancel Dpad wrap"));sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);waitForIdleSync();main(()->{check(menu[0].rows.get(0).hasFocus(),"Cancel to first Dpad wrap");menu[0].dialog.dismiss();});
  CatalogActivity browse=open(CatalogActivity.class,-1);Thread.sleep(1500);waitForIdleSync();main(()->{check(browse.grid.getAdapter().getCount()>0,"installed apps in menu");CheckBox system=null;EditText search=null;for(View v:tree(browse)){if(v instanceof CheckBox)system=(CheckBox)v;if(v instanceof EditText)search=(EditText)v;}check(system!=null&&!system.isChecked(),"system apps hidden by default");int user=browse.grid.getAdapter().getCount();system.setChecked(true);check(browse.grid.getAdapter().getCount()>user,"system checkbox adds installed packages");search.setText("no_such_app_foobar_792");check(browse.grid.getAdapter().getCount()==0,"search filters app menu");search.setText("");system.setChecked(false);});
  final int[] icon=new int[2];main(()->{Tile t=(Tile)browse.grid.getChildAt(0);icon[0]=t.image.getWidth();icon[1]=t.image.getTop();check(t.getHeight()>=Ui.dp(c,120),"three-line label space reserved");t.setSelected(true);check(t.image.getWidth()==icon[0]&&t.image.getTop()==icon[1],"focus keeps icon geometry");browse.finish();});
  CatalogActivity pick=open(CatalogActivity.class,0);Thread.sleep(350);main(()->{boolean box=false;for(View v:tree(pick))if(v instanceof CheckBox)box=true;check(box,"add screen shares same system checkbox");check(pick.grid.getColumnWidth()==Ui.dp(c,88),"add shares compact grid geometry");pick.finish();});
  SettingsActivity prefs=open(SettingsActivity.class,-1);main(()->{PreferenceScreen root=prefs.getPreferenceScreen();check(prefs.findPreference("running") instanceof CheckBoxPreference,"one running checkbox");check(prefs.findPreference("usage_access")==null,"no separate Usage grant entry");check(prefs.findPreference("cell")==null,"only icon size preference; automatic cells");check(prefs.findPreference("icon") instanceof SheetListPreference,"modern icon size sheet");check(prefs.findPreference("opacity").getSummary().toString().contains("%"),"percentage summary renders literally");check(prefs.findPreference("restricted").getTitle().toString().contains("restriction")||prefs.findPreference("restricted").getTitle().toString().contains("Снять"),"restriction wording");check(((ListPreference)prefs.findPreference("columns")).getEntryValues().length==6,"up to seven columns");check(((ListPreference)prefs.findPreference("rows")).getEntryValues().length==5,"up to five rows");check(((ListPreference)prefs.findPreference("date_format")).getEntries()[2].toString().contains("/"),"date slash format");check(((ListPreference)prefs.findPreference("clock_font")).getEntryValues().length==3,"three clock/date styles");prefs.finish();});
  main(()->{s.data.edit().putString("columns","7").putString("rows","5").apply();s.layoutChanged();});main(()->home.startActivity(new Intent(home,HomeActivity.class)));final HomeActivity max=home;main(()->{check(max.board.getChildCount()==35,"7x5 maximum board");s.assign(34,"example.keep");s.data.edit().putString("columns","2").putString("rows","1").apply();check(s.slot(34).equals("example.keep"),"shrinking grid preserves stored offscreen slots");s.data.edit().clear().putBoolean("bt_asked",true).apply();max.finish();});
  result.putString("report",report+"Checks="+checks);finish(Activity.RESULT_OK,result);
 }catch(Throwable e){result.putString("report",report+"ERROR "+e);finish(Activity.RESULT_CANCELED,result);}}
}
