package ru.truetv.launcher;
/** A selection gesture must end before a newly pinned app can be launched. */
final class ConfirmRelease {
 private long cutoff,quietUntil;private boolean waiting;
 void afterPicker(long now){cutoff=now;quietUntil=now+250;waiting=true;}
 boolean consume(boolean down,int repeat,long downTime,long eventTime){if(!waiting)return false;if(down&&repeat==0&&downTime>cutoff&&eventTime>=quietUntil){waiting=false;return false;}quietUntil=Math.max(quietUntil,eventTime+250);return true;}
}
