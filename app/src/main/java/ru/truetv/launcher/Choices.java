package ru.truetv.launcher;

import java.text.Normalizer;
import java.util.Locale;

/** Domain rules independent of Android widgets and package-manager calls. */
public final class Choices {
    private Choices() {}
    public static final int MAX_SLOTS = 35;
    public static int bound(int value, int low, int high) { return Math.max(low, Math.min(high, value)); }
    public static String searchable(String value) {
        String decomposed = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
    }
    public static boolean matches(String haystack, String query) {
        String text = searchable(haystack);
        for (String token : searchable(query).split("\\s+")) if (!text.contains(token)) return false;
        return true;
    }
    public static int nextEnabled(boolean[] enabled, int position, int direction) {
        if (enabled.length == 0) return -1;
        for (int n = 1; n <= enabled.length; n++) {
            int at = ((position + direction * n) % enabled.length + enabled.length) % enabled.length;
            if (enabled[at]) return at;
        }
        return -1;
    }
    public static String plain(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replace('\u00a0', ' ').trim().replaceAll("[\\s.!?…]+", " ").trim();
    }
    public static boolean forceStop(String value) {
        String v = plain(value);
        return v.equals("force stop") || v.equals("остановить") || v.equals("принудительно остановить") || v.equals("принудительная остановка");
    }
    public static boolean confirm(String value) {
        String v = plain(value); return v.equals("ok") || v.equals("ок") || v.equals("да") || v.equals("yes") || v.equals("оk") || v.equals("oк");
    }
    public static boolean stopMessage(String value) {
        String v = plain(value); return v.contains("force stop") || v.contains("остановить") || v.contains("принудительн");
    }
}
