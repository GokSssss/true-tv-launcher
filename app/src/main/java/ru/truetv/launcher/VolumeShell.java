package ru.truetv.launcher;

import android.os.Process;
import android.os.FileObserver;
import android.os.SystemClock;
import android.view.InputEvent;
import android.view.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.channels.FileLock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;

/** Separate app_process entry point, explicitly launched by the user through ADB. */
public final class VolumeShell {
    private static final String BASE = "/data/local/tmp/true-tv-launcher-volume";
    public static void main(String[] args) {
        try { run(args); }
        catch (Throwable failure) {
            System.err.println("True TV Launcher volume helper failed: " + failure);
            System.exit(1);
        }
    }
    private static void run(String[] args) throws Exception {
        if (Process.myUid() != 2000 && Process.myUid() != 0)
            throw new SecurityException("Launch the volume helper through ADB shell");
        File stop = new File(BASE + ".stop");
        if (args.length == 1 && args[0].equals("--status")) {
            try (RandomAccessFile state = new RandomAccessFile(BASE + ".lock", "rw");
                 FileLock guard = state.getChannel().tryLock()) {
                System.out.println(guard == null ? "TRUE_TV_VOLUME_RUNNING" : "TRUE_TV_VOLUME_STOPPED");
            }
            return;
        }
        if (args.length > 1 || args.length == 1 && !args[0].equals("--stop"))
            throw new IllegalArgumentException("Unknown volume-helper action");
        if (args.length == 1 && args[0].equals("--stop")) {
            if (!stop.exists() && !stop.createNewFile()) throw new IllegalStateException("Stop request failed");
            System.out.println("True TV Launcher: volume helper stop requested");
            return;
        }
        try (RandomAccessFile lockFile = new RandomAccessFile(BASE + ".lock", "rw");
             FileLock lock = lockFile.getChannel().tryLock()) {
            if (lock == null) { System.out.println("True TV Launcher: volume helper already running"); return; }
            if (stop.exists() && !stop.delete()) throw new IllegalStateException("Cannot reset stop request");
            Class<?> managerClass;
            try { managerClass = Class.forName("android.hardware.input.InputManagerGlobal"); }
            catch (ClassNotFoundException olderAndroid) { managerClass = Class.forName("android.hardware.input.InputManager"); }
            Object manager = managerClass.getMethod("getInstance").invoke(null);
            Method inject = managerClass.getMethod("injectInputEvent", InputEvent.class, int.class);
            VolumeRepeat repeat = new VolumeRepeat((key, action, count, down, now) -> {
                KeyEvent event = new KeyEvent(down, now, action, key, count, 0, -1, 0,
                        KeyEvent.FLAG_FROM_SYSTEM | KeyEvent.FLAG_SOFT_KEYBOARD, 0x301);
                if (!Boolean.TRUE.equals(inject.invoke(manager, event, 0)))
                    throw new IllegalStateException("System rejected volume injection");
            });
            try (PtyOutput pty = new PtyOutput()) {
                java.lang.Process reader = new ProcessBuilder("/system/bin/sh", "-c",
                        "exec /system/bin/getevent > " + pty.slave + " 2>&1").start();
                RepeatClock clock = new RepeatClock(repeat, reader);
                FileObserver stopEvents = new FileObserver("/data/local/tmp", FileObserver.CREATE | FileObserver.CLOSE_WRITE) {
                    public void onEvent(int event, String name) {
                        if ("true-tv-launcher-volume.stop".equals(name)) reader.destroy();
                    }
                };
                stopEvents.startWatching();
                Thread shutdown = new Thread(() -> {
                    clock.close(); reader.destroy();
                    try { repeat.stop(SystemClock.uptimeMillis()); } catch (Exception ignored) { }
                });
                Runtime.getRuntime().addShutdownHook(shutdown);
                System.out.println("True TV Launcher: volume helper started");
                try (BufferedReader lines = new BufferedReader(new InputStreamReader(pty.input))) {
                    String line;
                    while (!stop.exists() && (line = lines.readLine()) != null)
                        if (repeat.line(line, SystemClock.uptimeMillis())) clock.changed();
                } catch (java.io.IOException interrupted) {
                    if (!stop.exists()) throw interrupted;
                } finally {
                    stopEvents.stopWatching(); clock.close(); reader.destroy();
                    repeat.stop(SystemClock.uptimeMillis());
                    Runtime.getRuntime().removeShutdownHook(shutdown);
                }
            }
            System.out.println("True TV Launcher: volume helper stopped");
        }
    }
    /** No scheduled work while no volume key is held. */
    private static final class RepeatClock {
        private final VolumeRepeat repeat; private final java.lang.Process reader;
        private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
        private ScheduledFuture<?> next; private boolean closed;
        RepeatClock(VolumeRepeat repeat, java.lang.Process reader) { this.repeat=repeat;this.reader=reader; }
        synchronized void changed() {
            if (closed) return;
            if (next != null) next.cancel(false);
            long delay=repeat.delay(SystemClock.uptimeMillis());
            if (delay < 0) { next=null;return; }
            next=timer.schedule(() -> {
                try { repeat.tick(SystemClock.uptimeMillis());changed(); }
                catch (Exception failure) { reader.destroy(); }
            },delay,TimeUnit.MILLISECONDS);
        }
        synchronized void close() { closed=true;timer.shutdownNow(); }
    }

}
