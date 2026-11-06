package ru.truetv.bridge.android;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public final class BridgeUi {
 private BridgeUi(){}
 public static String w(Context c,String ru,String en){return c.getResources().getConfiguration().locale.getLanguage().equals("ru")?ru:en;}
 public static int dp(Context c,float n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
 public static String device(Context c){
  String name=null;
  try{name=android.provider.Settings.Global.getString(c.getContentResolver(),"device_name");}catch(RuntimeException ignored){}
  if(name==null||name.trim().isEmpty())try{name=android.provider.Settings.Secure.getString(c.getContentResolver(),"device_name");}catch(RuntimeException ignored){}
  if(name==null||name.trim().isEmpty())try{name=android.provider.Settings.System.getString(c.getContentResolver(),"device_name");}catch(RuntimeException ignored){}
  return ru.truetv.bridge.DeviceLabel.format(name,Build.MODEL);
 }
 public static String bytes(Context c,long n){if(n<0)return w(c,"размер неизвестен","size unknown");if(n<1024)return n+" B";if(n<1048576)return String.format(Locale.ROOT,"%.1f KB",n/1024d);if(n<1073741824L)return String.format(Locale.ROOT,"%.1f MB",n/1048576d);return String.format(Locale.ROOT,"%.2f GB",n/1073741824d);}
 public static LinearLayout box(Context c){LinearLayout b=new LinearLayout(c);b.setOrientation(1);b.setPadding(dp(c,18),dp(c,12),dp(c,18),dp(c,12));GradientDrawable bg=new GradientDrawable();bg.setColor(0xf22b3037);bg.setCornerRadius(dp(c,10));bg.setStroke(dp(c,1),0xff9ba4b0);b.setBackground(bg);return b;}
 public static TextView label(Context c,String text,int size){TextView t=new TextView(c);t.setText(text);t.setTextColor(Color.WHITE);t.setTextSize(size);t.setPadding(0,dp(c,3),0,dp(c,5));return t;}
 public static Button button(Context c,String text,Runnable action){Button b=new Button(c);b.setText(text);b.setAllCaps(false);b.setTextSize(16);b.setTextColor(new android.content.res.ColorStateList(new int[][]{new int[]{android.R.attr.state_focused},new int[]{android.R.attr.state_pressed},new int[]{}},new int[]{0xff151b24,0xff151b24,Color.WHITE}));b.setStateListAnimator(null);b.setBackgroundTintList(null);StateListDrawable d=new StateListDrawable();for(int state:new int[]{android.R.attr.state_focused,android.R.attr.state_pressed,0}){GradientDrawable g=new GradientDrawable();g.setColor(state==0?0x183f4650:0xffaaaaaa);g.setCornerRadius(dp(c,6));g.setStroke(dp(c,1),state==0?0xff727b87:0xffdeeff2);d.addState(state==0?new int[]{}:new int[]{state},g);}b.setBackground(d);if(Build.VERSION.SDK_INT>=26)b.setDefaultFocusHighlightEnabled(false);b.setOnClickListener(v->action.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(c,44));p.topMargin=dp(c,5);b.setLayoutParams(p);return b;}
 public static void top(Activity a){a.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));WindowManager.LayoutParams p=a.getWindow().getAttributes();p.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;p.width=Math.min(a.getResources().getDisplayMetrics().widthPixels-dp(a,24),dp(a,850));p.height=-2;p.y=dp(a,24);p.dimAmount=0.25f;a.getWindow().setAttributes(p);}
 public static void channels(Context c){if(Build.VERSION.SDK_INT>=26){NotificationManager m=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);m.createNotificationChannel(new NotificationChannel("totv_status","To TV",NotificationManager.IMPORTANCE_LOW));m.createNotificationChannel(new NotificationChannel("totv_request",w(c,"Запросы To TV","To TV requests"),NotificationManager.IMPORTANCE_HIGH));}}
 public static PendingIntent pending(Context c,Intent intent,int id){return PendingIntent.getActivity(c,id,intent,PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0));}
 public static Notification notification(Context c,String title,String text,boolean request,Intent intent,boolean receive){channels(c);Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,request?"totv_request":"totv_status"):new Notification.Builder(c);b.setSmallIcon(receive?android.R.drawable.stat_sys_download:android.R.drawable.stat_sys_upload).setContentTitle(title).setContentText(text).setOnlyAlertOnce(true).setPriority(request?Notification.PRIORITY_HIGH:Notification.PRIORITY_LOW).setCategory(request?Notification.CATEGORY_MESSAGE:Notification.CATEGORY_PROGRESS);if(intent!=null)b.setContentIntent(pending(c,intent,request?12:11));return b.build();}
 public static void start(Context c,Intent i){if(Build.VERSION.SDK_INT>=26)c.startForegroundService(i);else c.startService(i);}
 public static String error(Context c,Throwable e){String m=e.getMessage();if(m!=null&&m.contains("REJECTED"))return w(c,"Приём отклонён на телевизоре","Rejected on TV");if(e instanceof javax.net.ssl.SSLException)return w(c,"Не удалось подтвердить ключ устройства. Обновите список и проверьте получателя.","Could not verify the device key. Refresh devices and check the receiver.");return w(c,"Передача не завершена: ","Transfer incomplete: ")+(m==null?e.getClass().getSimpleName():m);}
}
