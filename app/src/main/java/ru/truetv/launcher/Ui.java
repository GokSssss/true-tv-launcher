package ru.truetv.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public final class Ui {
    public static final int INK = 0xfff4f6fb, MUTED = 0xff818a99, GREEN = 0xff6e9c78, DISABLED_RED = 0xffb36b6b;
    private Ui() {}
    public static String theme(Context c){String value=new SettingsStore(c).text("theme","dark");return value.equals("light")||value.equals("black")?value:"dark";}
    public static boolean light(Context c){return theme(c).equals("light");}
    public static int ink(Context c){return light(c)?0xff191e25:INK;}
    public static int muted(Context c){return light(c)?0xff606973:0xff9ba4b0;}
    public static int background(Context c){return light(c)?0xfff3f4f6:theme(c).equals("black")?0xff000000:0xff10151c;}
    public static int panel(Context c){return light(c)?0xf2f3f4f6:theme(c).equals("black")?0xf20a0a0a:0xf21c2430;}
    public static int border(Context c){return light(c)?0x40616a77:0x40ffffff;}
    public static int focusFill(Context c){return light(c)?0x25315d70:0x2cffffff;}
    public static int focusBorder(Context c){return light(c)?0x99315d70:0x80ffffff;}
    public static int accent(Context c){return light(c)?0xff315d70:0xffb6d6dc;}
    public static String appearance(Context c){return theme(c)+":"+new SettingsStore(c).flag("wallpaper",false);}
    public static String prepare(Activity a){String value=theme(a);a.setTheme(value.equals("light")?R.style.LightTheme:value.equals("black")?R.style.BlackTheme:R.style.AppTheme);return appearance(a);}
    public static void backdrop(Activity a,View root){boolean wallpaper=new SettingsStore(a).flag("wallpaper",false);if(wallpaper){a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);a.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));root.setBackgroundColor(light(a)?0xe8f3f4f6:0xb3000000);}else{a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);a.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(background(a)));root.setBackgroundColor(background(a));}}
    public static android.graphics.drawable.StateListDrawable selector(Context c){return selector(c,focusFill(c));}
    public static android.graphics.drawable.StateListDrawable selector(Context c,int fill){android.graphics.drawable.StateListDrawable state=new android.graphics.drawable.StateListDrawable();state.addState(new int[]{android.R.attr.state_focused},surface(c,fill,focusBorder(c),8));state.addState(new int[]{android.R.attr.state_pressed},surface(c,fill,focusBorder(c),8));state.addState(new int[]{},surface(c,Color.TRANSPARENT,0,8));return state;}

    public static android.widget.Button tvButton(Context c,String title){
        android.widget.Button button=new android.widget.Button(c);button.setText(title);if(Build.VERSION.SDK_INT>=26)button.setDefaultFocusHighlightEnabled(false);button.setAllCaps(false);button.setTextSize(16);button.setStateListAnimator(null);button.setBackgroundTintList(null);
        android.graphics.drawable.StateListDrawable background=new android.graphics.drawable.StateListDrawable();GradientDrawable focused=surface(c,0xffa8a8a8,accent(c),8);focused.setStroke(dp(c,2),accent(c));
        background.addState(new int[]{android.R.attr.state_focused},focused);background.addState(new int[]{android.R.attr.state_pressed},focused);background.addState(new int[]{},surface(c,light(c)?0x0b000000:0x0dffffff,border(c),8));button.setBackground(background);
        int active=0xff151b24;button.setTextColor(new android.content.res.ColorStateList(new int[][]{new int[]{android.R.attr.state_focused},new int[]{android.R.attr.state_pressed},new int[]{}},new int[]{active,active,ink(c)}));return button;
    }

    public static int dp(Context c, float value) { return Math.round(value * c.getResources().getDisplayMetrics().density); }
    public static String word(Context c, String ru, String en) { return c.getResources().getConfiguration().locale.getLanguage().equals("ru") ? ru : en; }
    public static GradientDrawable surface(Context c, int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(c, radius));
        if (stroke != 0) d.setStroke(dp(c, 1), stroke); return d;
    }
    public static TextView label(Context c, CharSequence text, int sp) {
        TextView t = new TextView(c); t.setText(text); t.setTextSize(sp); t.setTextColor(ink(c)); t.setIncludeFontPadding(false); return t;
    }
    public static void toast(Context c, String ru, String en) { Toast.makeText(c, word(c, ru, en), Toast.LENGTH_LONG).show(); }
    public static boolean launch(Context c, Intent intent) {
        try { c.startActivity(intent); return true; } catch (RuntimeException failure) { return false; }
    }
    public static void bars(Activity a) {
        a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int flags=0;if(light(a)&&Build.VERSION.SDK_INT>=23)flags|=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;if(light(a)&&Build.VERSION.SDK_INT>=26)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;a.getWindow().getDecorView().setSystemUiVisibility(flags);a.getWindow().setStatusBarColor(background(a));a.getWindow().setNavigationBarColor(background(a));
        if (Build.VERSION.SDK_INT >= 30 && a.getWindow().getInsetsController() != null)
            { android.view.WindowInsetsController controller=a.getWindow().getInsetsController();int mask=android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;controller.setSystemBarsAppearance(light(a)?mask:0,mask);controller.show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars()); }
    }
    public static void info(Context c, String pkg) {
        if (!launch(c, new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", android.net.Uri.parse("package:" + pkg))))
            toast(c, "Свойства приложения недоступны", "App info is unavailable");
    }
    public static void market(Context c, String pkg) {
        Intent listing = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=" + pkg));
        if (!launch(c, new Intent(listing).setPackage("com.android.vending")) && !launch(c, listing) &&
            !launch(c, new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + pkg))))
            toast(c, "Магазин приложений недоступен", "No app store is available");
    }
    public static void uninstall(Context c, String pkg) {
        if (!launch(c, new Intent(Intent.ACTION_DELETE, android.net.Uri.parse("package:" + pkg))))
            toast(c, "Удаление недоступно", "Uninstall is unavailable");
    }
}
