package ru.truetv.bridge;
import java.util.concurrent.*;
/** Available on Android5 too; a completed answer cannot be changed by a stale window. */
public final class Decision {
 private final CountDownLatch latch=new CountDownLatch(1);private int value;
 public synchronized void answer(int n){if(latch.getCount()!=0){value=n;latch.countDown();}}
 public int await(int seconds)throws InterruptedException {return latch.await(seconds,TimeUnit.SECONDS)?value:0;}
}
