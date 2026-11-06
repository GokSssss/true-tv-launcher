package ru.truetv.launcher;
public class ChoicesCheck {
 public static void main(String[] args){int n=0;for(int count=1;count<40;count++){boolean[] e=new boolean[count];java.util.Arrays.fill(e,true);if(Choices.nextEnabled(e,0,-1)!=count-1)throw new AssertionError();if(Choices.nextEnabled(e,count-1,1)!=0)throw new AssertionError();n+=2;}if(!Choices.matches("Пульт Bluetooth Accessories","BLUETOOTH пульт"))throw new AssertionError();if(Choices.forceStop("Удалить"))throw new AssertionError();if(Choices.forceStop("Не останавливать"))throw new AssertionError();if(!Choices.forceStop("  Остановить "))throw new AssertionError();if(!Choices.confirm("OK"))throw new AssertionError();if(Choices.confirm("Удалить"))throw new AssertionError();System.out.println("PASS "+(n+6)+" domain assertions");}
}
