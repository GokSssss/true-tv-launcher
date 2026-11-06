package ru.truetv.launcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Raw Linux volume edges -> extra Android repeats. No physical device is grabbed. */
final class VolumeRepeat {
    interface Output { void send(int key, int action, int repeat, long down, long now) throws Exception; }
    private static final Pattern EVENT = Pattern.compile("^(/dev/input/event[0-9]+):\\s+0001\\s+(0072|0073)\\s+([0-9a-fA-F]{8})\\s*$");
    private final Output output;
    private String device;
    private int key, count;
    private long down, next;
    VolumeRepeat(Output output) { this.output = output; }
    synchronized boolean line(String line, long now) throws Exception {
        Matcher match = EVENT.matcher(line.trim());
        if (!match.matches()) return false;
        int value = (int) Long.parseLong(match.group(3), 16);
        int code = match.group(2).equals("0072") ? 25 : 24;
        String path = match.group(1);
        if (value == 1) {
            // Ignore duplicate DOWN edges from the same device.
            if (code == key && path.equals(device)) return false;
            stop(now);
            key = code; device = path; down = now; next = now + 500;return true;
        } else if ((value == 0 || value == 2) && code == key && path.equals(device)) {
            // A real kernel repeat means native repeat exists: don't double it.
            stop(now);return true;
        }
        return false;
    }
    synchronized long delay(long now) { return key == 0 ? -1 : Math.max(0, Math.min(next, down + 10000) - now); }
    synchronized void tick(long now) throws Exception {
        if (key == 0) return;
        if (now - down >= 10000) { stop(now); return; }
        if (now < next) return;
        output.send(key, 0, ++count, down, now);
        // Never catch up a delayed thread with a burst of volume steps.
        next = now + 50;
    }
    synchronized void stop(long now) throws Exception {
        int oldKey = key, oldCount = count;
        key = 0; device = null; count = 0;
        if (oldKey != 0 && oldCount > 0) output.send(oldKey, 1, 0, down, now);
    }
}
