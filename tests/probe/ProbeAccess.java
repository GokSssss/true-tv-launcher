package ru.truetv.probe;
/** Disposable test fixture: no event capture, screen retrieval or actions. */
public final class ProbeAccess extends android.accessibilityservice.AccessibilityService {
 protected void onServiceConnected(){android.accessibilityservice.AccessibilityServiceInfo info=getServiceInfo();if(info!=null){info.eventTypes=0;info.feedbackType=android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC;setServiceInfo(info);}}
 public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent event){}
 public void onInterrupt(){}
}
