package ru.truetv.launcher;
import java.util.ArrayList;
import java.util.List;
public final class VolumeRepeatTest {
    private static int checks;
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("check " + checks); }
    private static String line(int device, String code, int value) { return String.format("/dev/input/event%d: 0001 %s %08x",device,code,value); }
    public static void main(String[] args) throws Exception {
        List<String> events = new ArrayList<>();
        VolumeRepeat repeat = new VolumeRepeat((key,action,count,down,now) -> events.add(key+":"+action+":"+count+":"+down+":"+now));
        check(repeat.delay(0)==-1);repeat.line("device name: remote",0); repeat.line(line(1,"0067",1),0);repeat.tick(1000);check(events.isEmpty());
        repeat.line(line(1,"0073",1),1000);check(repeat.delay(1000)==500);repeat.tick(1499);check(events.isEmpty());
        repeat.tick(1500);check(events.get(0).equals("24:0:1:1000:1500"));
        repeat.tick(1549);check(events.size()==1);repeat.tick(1550);check(events.size()==2);
        repeat.line(line(2,"0073",0),1560);repeat.tick(1600);check(events.size()==3);
        repeat.line(line(1,"0073",0),1610);check(events.get(3).equals("24:1:0:1000:1610"));
        repeat.tick(1800);check(events.size()==4);check(repeat.delay(1800)==-1);
        repeat.line(line(1,"0072",1),2000);repeat.line(line(1,"0072",0),2100);repeat.tick(3000);check(events.size()==4);
        repeat.line(line(1,"0072",1),3000);repeat.line(line(1,"0072",1),3200);repeat.tick(3500);check(events.get(4).equals("25:0:1:3000:3500"));
        repeat.line(line(1,"0072",2),3520);repeat.tick(4000);check(events.size()==6);
        repeat.line(line(1,"0073",1),5000);repeat.tick(5500);repeat.tick(9000);check(events.size()==8); // delayed tick never bursts
        repeat.line(line(1,"0072",1),9050);check(events.get(8).startsWith("24:1:"));
        repeat.tick(9550);check(events.get(9).startsWith("25:0:1:"));
        repeat.tick(19050);check(events.get(10).startsWith("25:1:"));repeat.tick(20000);check(events.size()==11);
        repeat.line(line(1,"0072",1),21000);repeat.tick(21500);repeat.stop(21510);repeat.stop(21520);check(events.size()==13);
        repeat.line("/dev/input/event1: 0001 0073 ffffffff",22000);repeat.tick(23000);check(events.size()==13);
        System.out.println("PASS: volume raw parser/repeater, " + checks + " assertions");
    }
}
