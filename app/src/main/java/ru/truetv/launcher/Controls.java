package ru.truetv.launcher;

import android.app.Activity;
import android.accessibilityservice.AccessibilityService;
import android.content.*;
import android.os.Build;
import java.lang.ref.WeakReference;

/** Manual grants only. No shell commands, device-admin enrollment or silent service enabling. */
public final class Controls {
    private static WeakReference<SettingsActivity> observer=new WeakReference<>(null);
    private Controls(){}
    public static boolean ready(Context c){return ControlService.connected()!=null;}
    public static void observe(SettingsActivity a){observer=new WeakReference<>(a);a.refreshAccess();}
    public static void unobserve(SettingsActivity a){if(observer.get()==a)observer.clear();}
    static void changed(){SettingsActivity a=observer.get();if(a!=null)a.refreshAccess();ActionSheet.refreshActive();}
    public static void openAccessibility(Context c){
        ComponentName service=new ComponentName(c,ControlService.class);
        Intent details=new Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS").putExtra("android.provider.extra.ACCESSIBILITY_COMPONENT_NAME",service);
        if(!Ui.launch(c,details)&&!Ui.launch(c,new Intent("android.settings.ACCESSIBILITY_SETTINGS")))Ui.toast(c,"Специальные возможности недоступны","Accessibility settings unavailable");
    }
    public static void restricted(Activity a){
        new SettingsStore(a).data.edit().remove("restricted_return").apply();
        a.startActivity(new Intent(a,AccessActivity.class));
    }
    public static void stop(Activity a,String pkg,boolean restart){ControlService service=ControlService.connected();if(service==null){openAccessibility(a);return;}service.begin(a,pkg,restart);}
    public static void powerSheet(Activity a){String note=Ui.word(a,"Включите специальные возможности в настройках True TV Launcher или системы.","Enable Accessibility in True TV Launcher or system settings.");Runnable unavailable=()->Ui.toast(a,"Включите специальные возможности в настройках True TV Launcher или системы.","Enable Accessibility in True TV Launcher or system settings.");ActionSheet.show(a,Ui.word(a,"Питание","Power"),
        new ActionSheet.Item(Ui.word(a,"Выключить","Power off"),"power",()->power(a,false),()->ready(a),note,unavailable),
        new ActionSheet.Item(Ui.word(a,"Перезагрузить","Restart"),"restart",()->power(a,true),()->ready(a),note,unavailable));}
    private static void power(Activity a,boolean restart){ControlService service=ControlService.connected();if(service==null){Ui.toast(a,"Включите специальные возможности в настройках True TV Launcher или системы.","Enable Accessibility in True TV Launcher or system settings.");return;}service.beginPower(a,restart);}
}
