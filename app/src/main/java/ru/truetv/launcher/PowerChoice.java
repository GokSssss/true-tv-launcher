package ru.truetv.launcher;
/** Exact ru/en system-power labels only. Never match a substring or an arbitrary application's controls. */
final class PowerChoice {
 private PowerChoice(){}
 static boolean trusted(String pkg){return "com.android.systemui".equals(pkg)||"android".equals(pkg);}
 static boolean action(String text,boolean restart){String v=Choices.plain(text);return restart?(v.equals("restart")||v.equals("reboot")||v.equals("перезагрузить")||v.equals("перезагрузка")):(v.equals("power off")||v.equals("turn off")||v.equals("shut down")||v.equals("выключить")||v.equals("выключение")||v.equals("отключить питание"));}
 static boolean menuId(String id){return "com.android.systemui:id/global_actions_container".equals(id)||"com.android.systemui:id/global_actions_grid".equals(id)||"com.android.systemui:id/global_actions_view".equals(id)||"android:id/global_actions".equals(id);}
 static boolean menu(boolean off,boolean restart,boolean knownId){return knownId||off&&restart;}
 static boolean question(String text,boolean restart){String v=Choices.plain(text);return restart?(v.equals("your device will restart")||v.equals("your phone will restart")||v.equals("устройство будет перезагружено")||v.equals("телефон будет перезагружен")||v.equals("перезагрузить устройство")):(v.equals("your device will shut down")||v.equals("your phone will shut down")||v.equals("устройство будет выключено")||v.equals("телефон будет выключен")||v.equals("выключить устройство"));}
 static boolean cancel(String text){String v=Choices.plain(text);return v.equals("cancel")||v.equals("отмена");}
}
