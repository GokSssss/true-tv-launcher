package ru.truetv.launcher;

import android.app.Application;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.util.LruCache;
import android.widget.ImageView;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;

/** Application-owned package snapshot and bounded icon work; browsing never waits for PackageManager on main. */
public final class AppCatalog {
    public interface Listener { void appsChanged(); }
    public static final class Entry {
        public final String pkg,label,version; public final boolean system,disabled; public final ComponentName launch; public final long revision;
        Entry(String pkg,String label,boolean system,boolean disabled,ComponentName launch,long revision,String version){this.pkg=pkg;this.label=label;this.system=system;this.disabled=disabled;this.launch=launch;this.revision=revision;this.version=version;}
        public String search(){return label+" "+pkg;}
    }
    private static AppCatalog instance;
    public static synchronized AppCatalog get(Context c){if(instance==null)instance=new AppCatalog(c.getApplicationContext());return instance;}
    private final Context context; private final PackageManager pm; private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService work=Executors.newSingleThreadExecutor();private volatile List<Entry> entries=Collections.emptyList();private volatile boolean loaded;
    private final ArrayList<WeakReference<Listener>> listeners=new ArrayList<>();private long requested;private String language;
    private final LruCache<String,Bitmap> icons=new LruCache<String,Bitmap>(6*1024*1024){protected int sizeOf(String key,Bitmap b){return b.getAllocationByteCount();}};
    private AppCatalog(Context c){context=c;pm=c.getPackageManager();language=c.getResources().getConfiguration().locale.toLanguageTag();
        BroadcastReceiver packages=new BroadcastReceiver(){public void onReceive(Context c,Intent i){reload();}};IntentFilter filter=new IntentFilter();for(String action:new String[]{Intent.ACTION_PACKAGE_ADDED,Intent.ACTION_PACKAGE_REMOVED,Intent.ACTION_PACKAGE_CHANGED,Intent.ACTION_PACKAGE_REPLACED})filter.addAction(action);filter.addDataScheme("package");
        if(Build.VERSION.SDK_INT>=33)c.registerReceiver(packages,filter,Context.RECEIVER_EXPORTED);else c.registerReceiver(packages,filter);reload();
    }
    public boolean loaded(){return loaded;}
    public List<Entry> snapshot(){return entries;}
    public Entry find(String pkg){for(Entry e:entries)if(e.pkg.equals(pkg))return e;return null;}
    public void listen(Listener listener){for(WeakReference<Listener> w:listeners)if(w.get()==listener)return;listeners.add(new WeakReference<>(listener));String locale=context.getResources().getConfiguration().locale.toLanguageTag();if(!language.equals(locale)){language=locale;reload();}}
    public void unlisten(Listener listener){for(Iterator<WeakReference<Listener>> it=listeners.iterator();it.hasNext();){Listener v=it.next().get();if(v==null||v==listener)it.remove();}}
    public void reload(){synchronized(icons){icons.evictAll();}final long ticket=++requested;work.execute(()->{
        HashMap<String,ComponentName> tv=new HashMap<>(),mobile=new HashMap<>();
        for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}){
            Intent query=new Intent(Intent.ACTION_MAIN).addCategory(category);
            for(ResolveInfo item:pm.queryIntentActivities(query,0)){ActivityInfo info=item.activityInfo;if(info!=null&&info.enabled&&info.exported){HashMap<String,ComponentName> map=category.equals(Intent.CATEGORY_LEANBACK_LAUNCHER)?tv:mobile;if(!map.containsKey(info.packageName))map.put(info.packageName,new ComponentName(info.packageName,info.name));}}
        }
        ArrayList<Entry> next=new ArrayList<>();
        for(ApplicationInfo info:pm.getInstalledApplications(PackageManager.MATCH_DISABLED_COMPONENTS)){
            try{String label=String.valueOf(pm.getApplicationLabel(info));boolean disabled=!info.enabled;int setting=pm.getApplicationEnabledSetting(info.packageName);disabled|=setting==PackageManager.COMPONENT_ENABLED_STATE_DISABLED||setting==PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER||setting==PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED;
                ComponentName launch=tv.containsKey(info.packageName)?tv.get(info.packageName):mobile.get(info.packageName);PackageInfo version=pm.getPackageInfo(info.packageName,0);
                next.add(new Entry(info.packageName,label,(info.flags&(ApplicationInfo.FLAG_SYSTEM|ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))!=0,disabled,disabled?null:launch,version.lastUpdateTime,version.versionName==null?String.valueOf(version.versionCode):version.versionName));
            }catch(RuntimeException|PackageManager.NameNotFoundException ignored){}
        }
        java.text.Collator order=java.text.Collator.getInstance(context.getResources().getConfiguration().locale);Collections.sort(next,(a,b)->{int n=order.compare(a.label,b.label);return n!=0?n:a.pkg.compareTo(b.pkg);});
        main.post(()->{if(ticket!=requested)return;entries=Collections.unmodifiableList(next);loaded=true;for(Iterator<WeakReference<Listener>> it=listeners.iterator();it.hasNext();){Listener l=it.next().get();if(l==null)it.remove();else l.appsChanged();}});
    });}
    public List<Entry> filter(String query,boolean system,boolean self){ArrayList<Entry> out=new ArrayList<>();for(Entry e:entries)if((system||!e.system)&&(self||!e.pkg.equals(context.getPackageName()))&&Choices.matches(e.search(),query))out.add(e);return out;}
    public boolean launch(Entry entry){return entry!=null&&entry.launch!=null&&Ui.launch(context,new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(entry.launch).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
    public void image(Entry entry,ImageView target){image(entry.pkg,entry.revision,target);}
    public void image(String pkg,ImageView target){image(pkg,0,target);}
    private void image(String pkg,long revision,ImageView target){String key=pkg+":"+revision;target.setTag(key);target.setImageDrawable(null);Bitmap cached; synchronized(icons){cached=icons.get(key);}if(cached!=null){target.setImageBitmap(cached);return;}
        WeakReference<ImageView> view=new WeakReference<>(target);work.execute(()->{Bitmap picture=null;try{Drawable d=pm.getApplicationIcon(pm.getApplicationInfo(pkg,PackageManager.MATCH_UNINSTALLED_PACKAGES|PackageManager.MATCH_DISABLED_COMPONENTS));int side=Ui.dp(context,192);side=Math.min(side,256);picture=IconPainter.render(d,side);synchronized(icons){icons.put(key,picture);}}catch(RuntimeException|PackageManager.NameNotFoundException ignored){}final Bitmap value=picture;main.post(()->{ImageView v=view.get();if(v!=null&&key.equals(v.getTag()))v.setImageBitmap(value);});});
    }
}
