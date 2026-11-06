package ru.truetv.bridge;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/** Small bounded control messages and streamed payloads. No Java object deserialization. */
public final class Wire {
 public static final String TYPE="_truetv._tcp.";
 public static final int MAGIC=0x54545631,CHUNK=65536,MAX_FILES=1024;
 public static final int WAIT=1,ACCEPT=2,REJECT=3,FILE_OK=4,DONE=5,ERROR=6;
 private Wire(){}
 public static void text(DataOutputStream out,String value,int cap)throws IOException {byte[] b=value.getBytes(StandardCharsets.UTF_8);if(b.length>cap)throw new IOException("Text too long");out.writeInt(b.length);out.write(b);}
 public static String text(DataInputStream in,int cap)throws IOException {int n=in.readInt();if(n<0||n>cap)throw new IOException("Invalid text size");byte[] b=new byte[n];in.readFully(b);return new String(b,StandardCharsets.UTF_8);}
 public static byte[] random(){byte[] b=new byte[32];new SecureRandom().nextBytes(b);return b;}
 public static String hex(byte[] b){StringBuilder s=new StringBuilder();for(byte v:b)s.append(String.format(Locale.ROOT,"%02x",v&255));return s.toString();}
 public static byte[] digest(byte[]... parts){try{MessageDigest d=MessageDigest.getInstance("SHA-256");for(byte[] p:parts)d.update(p);return d.digest();}catch(GeneralSecurityException e){throw new IllegalStateException(e);}}
 public static String code(byte[] server,byte[] client,byte[] a,byte[] b){String h=hex(digest(server,client,a,b)).substring(0,16).toUpperCase(Locale.ROOT);return h.substring(0,4)+" "+h.substring(4,8)+" "+h.substring(8,12)+" "+h.substring(12);}
 public static boolean local(InetAddress a){byte[] b=a.getAddress();if(a.isLoopbackAddress()||a.isSiteLocalAddress()||a.isLinkLocalAddress()||b.length==16&&(b[0]&254)==252)return true;try{java.util.Enumeration<NetworkInterface> all=NetworkInterface.getNetworkInterfaces();while(all!=null&&all.hasMoreElements()){NetworkInterface ni=all.nextElement();if(!ni.isUp()||ni.isLoopback()||ni.isPointToPoint())continue;for(InterfaceAddress own:ni.getInterfaceAddresses()){byte[] address=own.getAddress().getAddress();int bits=own.getNetworkPrefixLength();if(address.length!=b.length||bits<(b.length==16?32:8))continue;boolean match=true;for(int i=0;i<bits;i++)if((address[i/8]&(128>>(i%8)))!=(b[i/8]&(128>>(i%8)))){match=false;break;}if(match)return true;}}}catch(SocketException ignored){}return false;}
 public static String name(String raw)throws IOException {if(raw==null)throw new IOException("Missing file name");String s=raw.replace('\\','/');s=s.substring(s.lastIndexOf('/')+1).replaceAll("[\\p{Cntrl}]","").trim();if(s.isEmpty()||s.equals(".")||s.equals(".."))throw new IOException("Invalid file name");while(s.getBytes(StandardCharsets.UTF_8).length>240)s=s.substring(0,s.length()-1);return s;}
 public static final class Item {public final String name,mime;public final long size;public Item(String n,String m,long s)throws IOException {name=Wire.name(n);mime=m!=null&&m.matches("[a-zA-Z0-9!#$&^_.+-]+/[a-zA-Z0-9!#$&^_.+-]+")?m:"application/octet-stream";if(s< -1)throw new IOException("Invalid size");size=s;}}
 public static final class Offer {
  public final String device;public final List<Item> files;public final long size;
  public Offer(String d,List<Item> f)throws IOException {if(d==null||d.isEmpty()||d.getBytes(StandardCharsets.UTF_8).length>256||f.isEmpty()||f.size()>MAX_FILES)throw new IOException("Invalid offer");device=d.replaceAll("[\\p{Cntrl}]","");files=Collections.unmodifiableList(new ArrayList<>(f));long sum=0;boolean unknown=false;for(Item i:files){if(i.size<0)unknown=true;else{if(Long.MAX_VALUE-sum<i.size)throw new IOException("Total too large");sum+=i.size;}}size=unknown?-1:sum;}
  public void write(DataOutputStream out)throws IOException {text(out,device,256);out.writeInt(files.size());for(Item f:files){text(out,f.name,1024);text(out,f.mime,256);out.writeLong(f.size);}}
  public static Offer read(DataInputStream in)throws IOException {String d=text(in,256);int n=in.readInt();if(n<1||n>MAX_FILES)throw new IOException("Invalid file count");List<Item> f=new ArrayList<>();for(int i=0;i<n;i++)f.add(new Item(text(in,1024),text(in,256),in.readLong()));return new Offer(d,f);}
 }
 public interface Source {InputStream open(int index)throws IOException;}
 public interface Target {OutputStream stream()throws IOException;String publish()throws IOException;void discard();}
 public interface Store {Target create(Item item)throws IOException;}
 public interface Progress {void update(int index,String name,long bytes,long total);}
 public static final Progress QUIET=(i,n,b,t)->{};
}
