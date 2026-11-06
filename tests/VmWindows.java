package ru.truetv.launcher;
import android.app.Instrumentation;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.SystemClock;
import android.view.accessibility.*;
/** Disposable VM noise only. Never accepts another application's permission/error dialogs. */
final class VmWindows {
 static int handled;
 static HomeActivity home(Instrumentation test){Instrumentation.ActivityMonitor monitor=new Instrumentation.ActivityMonitor(HomeActivity.class.getName(),null,false);test.addMonitor(monitor);android.content.Context c=test.getTargetContext();c.startActivity(new android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER).setClass(c,LauncherEntry.class).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));HomeActivity home=(HomeActivity)test.waitForMonitorWithTimeout(monitor,20000);test.removeMonitor(monitor);test.waitForIdleSync();if(home==null)throw new AssertionError("App-icon entry did not open Home");return home;}

 static void prepare(Instrumentation test){AccessibilityServiceInfo info=test.getUiAutomation().getServiceInfo();info.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;test.getUiAutomation().setServiceInfo(info);}
 private static AccessibilityNodeInfo waitButton(AccessibilityNodeInfo node){if(node==null)return null;if("android:id/aerr_wait".equals(node.getViewIdResourceName())&&node.isClickable())return node;for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo found=waitButton(node.getChild(i));if(found!=null)return found;}return null;}
 static boolean clear(Instrumentation test){AccessibilityNodeInfo root=test.getUiAutomation().getRootInActiveWindow();if(root==null||!"android".contentEquals(root.getPackageName())||root.findAccessibilityNodeInfosByText("System UI isn't responding").isEmpty()&&root.findAccessibilityNodeInfosByText("Process system isn't responding").isEmpty())return false;AccessibilityNodeInfo wait=waitButton(root);if(wait==null)return false;boolean clicked=wait.performAction(AccessibilityNodeInfo.ACTION_CLICK);if(clicked){handled++;android.util.Log.i("TrueTVTest","Disposable VM: Wait on exact known VM System UI / process-system ANR");}return clicked;}
 static void ready(Instrumentation test)throws Exception {long until=SystemClock.uptimeMillis()+8000;String owner=test.getTargetContext().getPackageName();while(SystemClock.uptimeMillis()<until){if(clear(test)){Thread.sleep(100);continue;}AccessibilityNodeInfo root=test.getUiAutomation().getRootInActiveWindow();if(root!=null&&owner.contentEquals(root.getPackageName()))return;for(AccessibilityWindowInfo window:test.getUiAutomation().getWindows()){AccessibilityNodeInfo node=window.getRoot();if(window.isFocused()&&node!=null&&owner.contentEquals(node.getPackageName()))return;}Thread.sleep(100);}throw new AssertionError("Expected own foreground window; unknown foreign windows are not dismissed");}
}
