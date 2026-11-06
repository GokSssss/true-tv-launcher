package ru.truetv.launcher;

import android.accessibilityservice.*;
import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.os.*;
import android.view.accessibility.*;
import java.util.*;

/** Explicit bounded operations; observes Settings for selected force-stop or system power UI for a requested power action. */
public final class ControlService extends AccessibilityService {
    private static ControlService instance;private PowerOperation powerOperation;
    private final Handler main=new Handler(Looper.getMainLooper());
    private String target, label, settingsPackage;private boolean restart,identity,pressed;private long deadline,pressedAt;private Intent back;
    public static ControlService connected(){return instance;}
    protected void onServiceConnected(){instance=this;powerOperation=new PowerOperation(this,this::idle);idle();Controls.changed();}
    public boolean onUnbind(Intent i){if(powerOperation!=null)powerOperation.cancel(false);cancel(false);instance=null;Controls.changed();return super.onUnbind(i);}
    public void onInterrupt(){if(powerOperation!=null)powerOperation.cancel(true);cancel(true);}
    private void idle(){AccessibilityServiceInfo info=getServiceInfo();if(info!=null){info.eventTypes=0;info.packageNames=new String[]{getPackageName()};info.notificationTimeout=100;setServiceInfo(info);}}
    public void beginPower(Activity owner,boolean again){if(target!=null||powerOperation==null||powerOperation.active()){Ui.toast(owner,"Служба занята","Service is busy");return;}powerOperation.begin(again);}
    public void begin(Activity owner,String pkg,boolean again){
        if(target!=null||powerOperation!=null&&powerOperation.active()){Ui.toast(owner,"Служба занята","Service is busy");return;}
        Intent details=new Intent("android.settings.APPLICATION_DETAILS_SETTINGS",Uri.parse("package:"+pkg));ResolveInfo handler=getPackageManager().resolveActivity(details,PackageManager.MATCH_DEFAULT_ONLY);
        try{ApplicationInfo app=getPackageManager().getApplicationInfo(pkg,0);if(pkg.equals(getPackageName())||pkg.equals("android")||handler==null||pkg.equals(handler.activityInfo.packageName)){Ui.toast(owner,"Это приложение нельзя остановить из лаунчера","This app cannot be stopped from the launcher");return;}
            target=pkg;label=String.valueOf(getPackageManager().getApplicationLabel(app));settingsPackage=handler.activityInfo.packageName;restart=again;identity=false;pressed=false;deadline=SystemClock.uptimeMillis()+45000;back=owner instanceof HomeActivity?HomeActivity.homeIntent(owner):new Intent(owner,owner.getClass()).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_NEW_TASK);
            AccessibilityServiceInfo info=getServiceInfo();info.eventTypes=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED|AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;info.packageNames=new String[]{settingsPackage,"android",getPackageName()};info.flags|=AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;setServiceInfo(info);
            owner.startActivity(details);main.postDelayed(poll,300);
        }catch(RuntimeException|PackageManager.NameNotFoundException e){cancel(true);}
    }
    private final Runnable poll=new Runnable(){public void run(){step();if(target!=null)main.postDelayed(this,400);}};
    public void onAccessibilityEvent(AccessibilityEvent e){if(powerOperation!=null&&powerOperation.active())powerOperation.step();else if(target!=null)step();}
    private boolean isStopped(){try{return (getPackageManager().getApplicationInfo(target,0).flags&ApplicationInfo.FLAG_STOPPED)!=0;}catch(PackageManager.NameNotFoundException e){return false;}}
    private void step(){if(target==null)return;if(SystemClock.uptimeMillis()>deadline){cancel(true);return;}if(isStopped()){complete();return;}
        AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null)return;String pkg=String.valueOf(root.getPackageName());if(!pkg.equals(settingsPackage)&&!pkg.equals("android")){root.recycle();if(identity)cancel(true);return;}
        ArrayList<AccessibilityNodeInfo> nodes=new ArrayList<>();collect(root,nodes,0);boolean packageSeen=false,labelSeen=false,stopContext=false,cancelSeen=false;
        for(AccessibilityNodeInfo n:nodes){String text=n.getText()==null?"":n.getText().toString();if(text.equals(target))packageSeen=true;if(text.equals(label))labelSeen=true;if(Choices.stopMessage(text))stopContext=true;String plain=Choices.plain(text);if(plain.equals("отмена")||plain.equals("cancel"))cancelSeen=true;}
        boolean proof=packageSeen||labelSeen&&uniqueLabel();if(!pressed&&pkg.equals(settingsPackage))identity=proof;
        AccessibilityNodeInfo chosen=null;
        if(identity&&!pressed&&pkg.equals(settingsPackage))for(AccessibilityNodeInfo n:nodes)if(Choices.forceStop(String.valueOf(n.getText()))&&n.isEnabled()){chosen=n;break;}
        if(pressed&&SystemClock.uptimeMillis()-pressedAt>6000){for(AccessibilityNodeInfo n:nodes)n.recycle();cancel(true);return;}
        if(identity&&pressed&&stopContext&&cancelSeen)for(AccessibilityNodeInfo n:nodes)if(Choices.confirm(String.valueOf(n.getText()))&&n.isEnabled()){chosen=n;break;}
        if(chosen!=null&&click(chosen)){if(!pressed)pressedAt=SystemClock.uptimeMillis();pressed=true;}
        for(AccessibilityNodeInfo n:nodes)n.recycle();
    }
    private boolean uniqueLabel(){AppCatalog catalog=AppCatalog.get(this);if(!catalog.loaded())return false;int found=0;for(AppCatalog.Entry app:catalog.snapshot())if(label.equals(app.label))found++;return found==1;}
    static void collect(AccessibilityNodeInfo n,List<AccessibilityNodeInfo> out,int depth){out.add(n);if(depth>=25||out.size()>500)return;for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo child=n.getChild(i);if(child!=null)collect(child,out,depth+1);}}
    static boolean click(AccessibilityNodeInfo n){AccessibilityNodeInfo p=AccessibilityNodeInfo.obtain(n);for(int i=0;i<4&&p!=null;i++){if(p.isEnabled()&&p.isClickable()){boolean done=p.performAction(AccessibilityNodeInfo.ACTION_CLICK);p.recycle();return done;}AccessibilityNodeInfo next=p.getParent();p.recycle();p=next;}if(p!=null)p.recycle();return false;}
    private void complete(){String pkg=target;boolean relaunch=restart;Intent restore=back;cancel(false);if(restore!=null)Ui.launch(this,restore);if(relaunch){AppCatalog.Entry e=AppCatalog.get(this).find(pkg);main.postDelayed(()->{if(e==null||!AppCatalog.get(this).launch(e))Ui.toast(this,"Приложение остановлено, запуск недоступен","App stopped; launch unavailable");},350);}else Ui.toast(this,"Приложение остановлено","App stopped");}
    private void cancel(boolean explain){boolean busy=target!=null;target=null;back=null;main.removeCallbacks(poll);idle();if(explain&&busy)Ui.toast(this,"Не удалось подтвердить остановку. Операция отменена.","Could not confirm force stop. Operation cancelled.");}
}
