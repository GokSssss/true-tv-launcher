package ru.truetv.launcher;

import android.app.Activity;
import android.content.*;
import android.graphics.drawable.ColorDrawable;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

/** One screen and one adapter for browsing, adding and replacement. */
public final class CatalogActivity extends Activity implements AppCatalog.Listener,StatusWatch.Listener {
    static final String PICK_APP="ru.truetv.launcher.PICK_DESKTOP_APP";private final ExecutorService saver=Executors.newSingleThreadExecutor();private boolean saving;private TextView purpose;
    private String appearance;private AppGrid content;
    private AppCatalog catalog;private SettingsStore settings;private EditText search;private CheckBox system;public GridView grid;private int slot;private List<AppCatalog.Entry> visible=Collections.emptyList();private StatusWatch status;
    protected void onCreate(Bundle state){appearance=Ui.prepare(this);super.onCreate(state);Ui.bars(this);catalog=AppCatalog.get(this);settings=new SettingsStore(this);slot=state!=null?state.getInt("slot",getIntent().getIntExtra("slot",-1)):getIntent().getIntExtra("slot",-1);if(slot<-1||slot>=Choices.MAX_SLOTS)slot=-1;if(PICK_APP.equals(getIntent().getAction())&&slot<0){Ui.toast(this,"Не указано место. Откройте плюс ещё раз.","No slot specified. Open the plus again.");setResult(RESULT_CANCELED);finish();return;}
        content=new AppGrid(this,slot,entry->{if(slot>=0)saveSelection(entry);else if(!catalog.launch(entry))Ui.info(this,entry.pkg);},this::menu);setContentView(content);Ui.backdrop(this,content.getChildAt(0));purpose=content.purpose;search=content.search;system=content.system;grid=content.grid;if(state!=null)search.setText(state.getString("query",""));

    }
    private void saveSelection(AppCatalog.Entry entry){if(saving||isFinishing()||isDestroyed())return;saving=true;final int target=slot;purpose.setText(Ui.word(this,"Сохраняю приложение…","Saving app…"));saver.execute(()->{boolean saved=false;try{saved=settings.assignSaved(target,entry.pkg);}catch(RuntimeException ignored){}final boolean ok=saved;runOnUiThread(()->{saving=false;if(isFinishing()||isDestroyed())return;if(ok){setResult(RESULT_OK,new Intent().putExtra("slot",target).putExtra("package",entry.pkg));finish();}else{purpose.setText(Ui.word(this,"Не удалось сохранить место на рабочем столе","Could not save the desktop slot"));new android.app.AlertDialog.Builder(this).setTitle(Ui.word(this,"Приложение не добавлено","App not added")).setMessage(Ui.word(this,"Запись настроек не подтвердилась. Повторите выбор; данные не очищайте.","Saving settings was not confirmed. Try selecting again; keep existing app data.")).setPositiveButton("OK",null).show();}});});}
    public void onBackPressed(){if(!saving)super.onBackPressed();}
    protected void onDestroy(){saver.shutdown();super.onDestroy();}
    private void filter(){if(content!=null)content.filter();}
    private void menu(AppCatalog.Entry entry){showActions(this,entry);}
    static void showActions(Activity owner,AppCatalog.Entry e){ActionSheet.showApp(owner,e,AppMenus.ordered(owner,e,
        new ActionSheet.Item(Ui.word(owner,"Закрыть приложение","Close app"),"close",()->Controls.stop(owner,e.pkg,false),()->Controls.ready(owner)),
        new ActionSheet.Item(Ui.word(owner,"Перезапустить приложение","Restart app"),"restart",()->Controls.stop(owner,e.pkg,true),()->Controls.ready(owner)),
        new ActionSheet.Item(Ui.word(owner,"Удалить приложение","Uninstall app"),"delete",()->Ui.uninstall(owner,e.pkg)),
        new ActionSheet.Item(Ui.word(owner,"Открыть в маркете / обновить","Open in store / update"),"market",()->Ui.market(owner,e.pkg)),
        new ActionSheet.Item(Ui.word(owner,"Свойства приложения","App info"),"info",()->Ui.info(owner,e.pkg))));
    }
    protected void onResume(){super.onResume();if(!Ui.appearance(this).equals(appearance)){recreate();return;}Ui.bars(this);content.resume();ActionSheet.refreshActive();}
    protected void onPause(){if(content!=null)content.pause();super.onPause();}
    protected void onSaveInstanceState(Bundle out){out.putString("query",search.getText().toString());out.putInt("slot",slot);super.onSaveInstanceState(out);}
    public void appsChanged(){filter();}
    public List<String> visiblePackages(){return content.visiblePackages();}
    public void status(boolean vpn,String owner,int bluetooth,Set<String> live){content.status(vpn,owner,bluetooth,live);}
}
