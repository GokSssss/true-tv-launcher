package ru.truetv.launcher;
public final class PinGestureCheck {
 private static int checks;private static void check(boolean v){if(!v)throw new AssertionError("check "+checks);checks++;}
 public static void main(String[] args){ConfirmRelease g=new ConfirmRelease();check(!g.consume(true,0,1,1));g.afterPicker(1000);check(g.consume(false,0,900,1010));check(g.consume(true,2,900,1400));check(g.consume(false,0,900,1410));check(g.consume(true,0,1500,1500));check(g.consume(false,0,1500,1510));check(g.consume(true,0,1700,1700));check(g.consume(false,0,1700,1710));check(!g.consume(true,0,2000,2000));check(!g.consume(false,0,2000,2010));System.out.println("PASS "+checks+" picker-release / discrete IR burst checks");}
}
