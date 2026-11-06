package ru.truetv.launcher;
import android.accessibilityservice.*;
import android.os.*;
import android.view.accessibility.*;
import java.util.*;
/** A single user-requested system power action. No root, private APIs, foreign windows or idle monitoring. */
final class PowerOperation {
 private final AccessibilityService service;private final Runnable idle;private final Handler main=new Handler(Looper.getMainLooper());
 private boolean active,restart,pressed,sawMenu;
 PowerOperation(AccessibilityService service,Runnable idle){this.service=service;this.idle=idle;}
 boolean active(){return active;}
 void begin(boolean again){restart=again;pressed=false;sawMenu=false;active=true;
  AccessibilityServiceInfo info=service.getServiceInfo();if(info==null){cancel(true);return;}info.eventTypes=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED|AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;info.packageNames=new String[]{"com.android.systemui","android",service.getPackageName()};info.flags|=AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;service.setServiceInfo(info);
  if(!service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)){cancel(true);return;}main.postDelayed(timeout,12000);
 }
 private final Runnable timeout=this::onTimeout;
 private void onTimeout(){if(!active)return;AccessibilityNodeInfo root=service.getRootInActiveWindow();boolean system=root!=null&&PowerChoice.trusted(String.valueOf(root.getPackageName()));if(root!=null)root.recycle();cancel(system&&!pressed);}

 void step(){if(!active)return;AccessibilityNodeInfo root=service.getRootInActiveWindow();if(root==null)return;if(!PowerChoice.trusted(String.valueOf(root.getPackageName()))){root.recycle();if(sawMenu)cancel(false);return;}
  ArrayList<AccessibilityNodeInfo> nodes=new ArrayList<>();ControlService.collect(root,nodes,0);
  try{if(!pressed){boolean off=false,reboot=false,known=false;for(AccessibilityNodeInfo n:nodes){off|=PowerChoice.action(String.valueOf(n.getText()),false)||PowerChoice.action(String.valueOf(n.getContentDescription()),false);reboot|=PowerChoice.action(String.valueOf(n.getText()),true)||PowerChoice.action(String.valueOf(n.getContentDescription()),true);known|=PowerChoice.menuId(n.getViewIdResourceName());}if(!PowerChoice.menu(off,reboot,known)){if(sawMenu)cancel(false);return;}sawMenu=true;for(AccessibilityNodeInfo n:nodes)if(n.isEnabled()&&(PowerChoice.action(String.valueOf(n.getText()),restart)||PowerChoice.action(String.valueOf(n.getContentDescription()),restart))&&ControlService.click(n)){pressed=true;break;}}
   else{boolean question=false,cancel=false;for(AccessibilityNodeInfo n:nodes){question|=PowerChoice.question(String.valueOf(n.getText()),restart);cancel|=PowerChoice.cancel(String.valueOf(n.getText()));}if(question&&cancel)for(AccessibilityNodeInfo n:nodes)if(n.isEnabled()&&Choices.confirm(String.valueOf(n.getText()))&&ControlService.click(n)){cancel(false);break;}}
  }finally{for(AccessibilityNodeInfo n:nodes)n.recycle();}
 }
 void cancel(boolean explain){boolean was=active;active=false;main.removeCallbacks(timeout);try{idle.run();}catch(RuntimeException ignored){}if(explain&&was)Ui.toast(service,"Не удалось выбрать это действие на системном экране питания. Выберите его вручную в системном меню.","Could not select this action on the system power screen. Choose it manually in the system menu.");}
}
