package ru.truetv.launcher;

import android.content.Context;
import android.content.SharedPreferences;

/** One owned schema. Reducing the visible grid never deletes off-screen assignments. */
public final class SettingsStore {
    public final SharedPreferences data;
    public SettingsStore(Context context) { data = context.getSharedPreferences("launcher", Context.MODE_PRIVATE); }
    public int number(String key, int fallback) {
        try { return Integer.parseInt(text(key,String.valueOf(fallback))); } catch (RuntimeException ignored) { return Integer.parseInt(Defaults.text(key,String.valueOf(fallback))); }
    }
    public String text(String key, String fallback) { String value=Defaults.text(key,fallback);try { return data.getString(key,value); } catch (RuntimeException e) { return value; } }
    public boolean flag(String key, boolean fallback) { boolean value=Defaults.flag(key,fallback);try { return data.getBoolean(key,value); } catch (RuntimeException e) { return value; } }
    public String slot(int index) { return text("slot." + Choices.bound(index, 0, Choices.MAX_SLOTS - 1), ""); }
    public void assign(int index, String pkg) { data.edit().putString("slot." + Choices.bound(index, 0, Choices.MAX_SLOTS - 1), pkg == null ? "" : pkg).putLong("desktop.slots_revision",slotsRevision()+1).apply(); }
    public boolean assignSaved(int index,String pkg){if(index<0||index>=Choices.MAX_SLOTS||!("android".equals(pkg)||SystemCommands.validPackage(pkg)))throw new IllegalArgumentException("Invalid desktop assignment");return data.edit().putString("slot."+index,pkg).putLong("desktop.slots_revision",slotsRevision()+1).commit();}
    public void exchange(int a, int b) {
        String left = slot(a), right = slot(b);
        data.edit().putString("slot." + a, right).putString("slot." + b, left).putLong("desktop.slots_revision",slotsRevision()+1).apply();
    }
    public long slotsRevision(){try{return data.getLong("desktop.slots_revision",0);}catch(RuntimeException ignored){return 0;}}
    public long layoutRevision(){try{return data.getLong("desktop.layout_revision",0);}catch(RuntimeException ignored){return 0;}}
    public void layoutChanged(){data.edit().putLong("desktop.layout_revision",layoutRevision()+1).apply();}
    static boolean layoutKey(String key){switch(key){case "columns":case "rows":case "icon":case "labels":case "borders":case "opacity":case "theme":case "wallpaper":return true;default:return false;}}
    public int columns() { return Choices.bound(number("columns", 5), 2, 7); }
    public int rows() { return Choices.bound(number("rows", 3), 1, 5); }
}
