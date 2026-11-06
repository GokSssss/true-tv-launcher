package ru.truetv.bridge;

/** A readable name plus model, bounded for mDNS TXT and the transfer header. */
public final class DeviceLabel {
 private DeviceLabel(){}
 public static String format(String name,String model){
  String n=clean(name,155),m=clean(model,80);
  if(n.isEmpty())return m.isEmpty()?"Android":m;
  return m.isEmpty()||n.equalsIgnoreCase(m)?n:n+" · "+m;
 }
 private static String clean(String value,int limit){
  if(value==null)return "";StringBuilder out=new StringBuilder();int bytes=0;
  for(int i=0;i<value.length();){int c=value.codePointAt(i);i+=Character.charCount(c);if(Character.isISOControl(c))c=' ';int size=c<128?1:c<2048?2:c<65536?3:4;if(bytes+size>limit)break;out.appendCodePoint(c);bytes+=size;}
  return out.toString().trim();
 }
}
