package ru.truetv.bridge.android;
import android.content.Context;
import android.os.PowerManager;
/** No lock while listening/discovering. Only an accepted file transfer keeps the CPU awake. */
public final class TransferWake {
 private PowerManager.WakeLock lock;
 public synchronized void start(Context c){if(lock==null){PowerManager p=(PowerManager)c.getApplicationContext().getSystemService(Context.POWER_SERVICE);lock=p.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"TrueTV:files");lock.setReferenceCounted(false);}if(!lock.isHeld())lock.acquire(5*60*60*1000L);}
 public synchronized void stop(){if(lock!=null&&lock.isHeld())lock.release();}
}
