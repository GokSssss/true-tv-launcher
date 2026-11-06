package ru.truetv.launcher;
public final class DefaultsPowerCheck {
 static int n;static void ok(boolean v){n++;if(!v)throw new AssertionError("check "+n);}
 public static void main(String[] args){
  ok(Defaults.text("theme","dark").equals("black"));ok(Defaults.text("rows","3").equals("4"));ok(Defaults.text("columns","7").equals("5"));ok(Defaults.text("menu_icon","64").equals("56"));ok(Defaults.text("date_format","words").equals("numeric"));ok(!Defaults.flag("labels",true));ok(!Defaults.flag("borders",true));ok(Defaults.flag("extended_menu_first",false));ok(!Defaults.flag("running",true));ok(Defaults.flag("unknown",true));ok(Defaults.text("access_endpoint","local").equals("local"));
  for(String v:new String[]{"Power off","Turn off","Shut down","Выключить","Выключение","Отключить питание"}){ok(PowerChoice.action(v,false));ok(!PowerChoice.action(v,true));}
  for(String v:new String[]{"Restart","Reboot","Перезагрузить","Перезагрузка"}){ok(PowerChoice.action(v,true));ok(!PowerChoice.action(v,false));}
  for(String v:new String[]{"Restart app","Please restart","Power off all apps","Перезапустить приложение","Включить","Cancel","OK",""}){ok(!PowerChoice.action(v,false));ok(!PowerChoice.action(v,true));}
  ok(PowerChoice.trusted("android"));ok(PowerChoice.trusted("com.android.systemui"));ok(!PowerChoice.trusted("ru.truetv.launcher"));ok(!PowerChoice.trusted("com.example.systemui"));
  ok(!PowerChoice.menu(false,true,false));ok(!PowerChoice.menu(true,false,false));ok(PowerChoice.menu(true,true,false));ok(PowerChoice.menu(false,false,true));ok(!PowerChoice.menuId("android:id/alertTitle"));ok(PowerChoice.menuId("com.android.systemui:id/global_actions_view"));
  ok(PowerChoice.question("Your device will restart.",true));ok(!PowerChoice.question("Your device will restart.",false));ok(PowerChoice.question("Устройство будет выключено",false));ok(!PowerChoice.question("Force stop this app?",false));ok(!PowerChoice.question("Are you sure?",true));ok(PowerChoice.cancel("Отмена"));
  String apk="/data/app/owner's app/base.apk";ok(VolumeCommand.start(apk).contains("nohup setsid"));ok(VolumeCommand.entry(apk).contains("'\\''"));ok(VolumeCommand.status(apk).endsWith(" --status"));ok(VolumeCommand.stop(apk).endsWith(" --stop"));ok(!VolumeCommand.start(apk).contains("tv-mouse-volume"));try{VolumeCommand.entry("/data/a.apk\nreboot");throw new AssertionError("newline accepted");}catch(IllegalArgumentException expected){ok(true);}
  System.out.println("PASS "+n+" fresh-default/system-power positive and negative checks");
 }
}
