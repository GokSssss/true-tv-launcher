package ru.truetv.launcher;
/** Owner-approved fresh-install values. Never replaces a stored preference or grants an OS permission. */
final class Defaults {
 private Defaults(){}
 static String text(String key,String fallback){switch(key){case "theme":return "black";case "columns":return "5";case "rows":return "4";case "icon":case "opacity":return "0";case "menu_icon":return "56";case "clock_font":return "medium";case "date_format":return "numeric";default:return fallback;}}
 static boolean flag(String key,boolean fallback){switch(key){case "wallpaper":case "labels":case "borders":case "keep_awake":case "running":return false;case "bt_count":case "show_self":case "extended_menu_first":return true;default:return fallback;}}
}
