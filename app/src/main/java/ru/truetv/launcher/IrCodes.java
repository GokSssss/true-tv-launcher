package ru.truetv.launcher;

import java.util.*;
import java.util.regex.*;

/** Diagnostic NEC decoding only. No driver writes or Android key-event substitution. */
public final class IrCodes {
 private IrCodes(){}
 private static final Pattern FRAME=Pattern.compile("(?:invalid custom\\s*:|framecode\\s*=)\\s*(?:0x)?([0-9a-f]{8})(?![0-9a-z])",Pattern.CASE_INSENSITIVE);
 private static final Pattern SCAN=Pattern.compile("(?:receive[ _]scancode\\s*[=:]|scancode\\s+)\\s*(?:0x)?([0-9a-f]{1,2})(?![0-9a-z])",Pattern.CASE_INSENSITIVE);
 public static final class Code {
  public final int customer,command;
  public Code(int customer,int command){if(customer < -1 || customer>65535 || command<0 || command>255)throw new IllegalArgumentException("IR range");this.customer=customer;this.command=command;}
  public String wire(){return (customer<0?"?":String.format(Locale.ROOT,"%04x",customer))+":"+String.format(Locale.ROOT,"%02x",command);}
  public String toString(){return wire();}
 }
 public static Code parse(String line){
  Matcher frame=FRAME.matcher(line);if(frame.find()){long value=Long.parseLong(frame.group(1),16);int command=(int)(value>>16)&255,check=(int)(value>>24)&255;if((command^check)!=255)return null;return new Code((int)value&65535,command);}
  Matcher scan=SCAN.matcher(line);return scan.find()?new Code(-1,Integer.parseInt(scan.group(1),16)):null;
 }
 public static Code fromWire(String text){if(text==null||!text.matches("(?:[0-9a-fA-F]{4}|\\?):[0-9a-fA-F]{2}"))throw new IllegalArgumentException("IR code format");String[] pieces=text.split(":");return new Code(pieces[0].equals("?")?-1:Integer.parseInt(pieces[0],16),Integer.parseInt(pieces[1],16));}
 /** Each new log line counts once. A candidate needs two new matching observations. */
 public static final class Learning {
  private final Set<String> seen=new HashSet<>();private String candidate;private int matches;
  public Learning(String baseline){remember(baseline);}
  private void remember(String text){for(String line:text.split("\\r?\\n"))if(!line.isEmpty())seen.add(line);}
  public Code accept(String text){for(String line:text.split("\\r?\\n")){if(line.isEmpty()||!seen.add(line))continue;if(seen.size()>4096)throw new IllegalStateException("Too many IR log lines");Code code=parse(line);if(code==null)continue;if(code.wire().equals(candidate))matches++;else{candidate=code.wire();matches=1;}if(matches>=2)return code;}return null;}
 }
 /** Input device report intentionally omits Phys/Uniq addresses. */
 public static String devices(String text){StringBuilder out=new StringBuilder();for(String block:text.split("\\r?\\n\\s*\\r?\\n")){String lower=block.toLowerCase(Locale.ROOT);if(!lower.matches("(?s).*n: name=.*(?:remote|amlogic|infrared|meson|ir[-_ ]receiver).*"))continue;for(String line:block.split("\\r?\\n"))if(line.startsWith("N: Name=")||line.startsWith("H: Handlers="))out.append(line).append('\n');}return out.toString().trim();}
}
