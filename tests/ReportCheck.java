package ru.truetv.launcher;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Real temporary filesystem and failing scoped-store transactions; no Android/root claim. */
public final class ReportCheck {
 private static int checks;
 private static void ok(boolean value){checks++;if(!value)throw new AssertionError("Export check "+checks);}
 private static final class Store implements ReportFiles.Store {
  final List<String> events=new ArrayList<>();final ByteArrayOutputStream data=new ByteArrayOutputStream();String fail="";
  public String begin(String name)throws IOException {events.add("begin");if(fail.equals("begin"))return null;return "own-row";}
  public OutputStream open(String token)throws IOException {ok(token.equals("own-row"));events.add("open");if(fail.equals("open"))throw new IOException("no stream");if(fail.equals("null"))return null;return new FilterOutputStream(data){public void write(byte[] bytes)throws IOException {if(fail.equals("write"))throw new IOException("disk full");out.write(bytes);}public void close()throws IOException {events.add("close");if(fail.equals("close"))throw new IOException("close failed");}};}
  public void publish(String token)throws IOException {events.add("publish");if(fail.equals("publish"))throw new IOException("no publish");}
  public void discard(String token){ok(token.equals("own-row"));events.add("discard");}
 }
 public static void main(String[] args)throws Exception {
  byte[] content="ИК-отчёт\nNEC customer unknown\n".getBytes("UTF-8");String name="IR-diagnostic-20261103-123000-001.txt";
  File dir=Files.createTempDirectory("true-tv-downloads-test").toFile();
  try{
   File first=ReportFiles.legacy(dir,name,content),second=ReportFiles.legacy(dir,name,new byte[]{1,2});ok(first.getParentFile().equals(dir));ok(!first.equals(second));ok(Arrays.equals(Files.readAllBytes(first.toPath()),content));ok(Arrays.equals(Files.readAllBytes(second.toPath()),new byte[]{1,2}));
   for(String bad:new String[]{"../other.txt","IR-diagnostic-1.txt/child","/data/IR-diagnostic-1.txt","user-file.txt","IR-diagnostic-1.txt\n"}){boolean rejected=false;try{ReportFiles.legacy(dir,bad,content);}catch(IllegalArgumentException expected){rejected=true;}ok(rejected);}
   File nested=ReportFiles.legacy(new File(dir,"Download"),name,content);ok(nested.isFile());
   Store success=new Store();ok(ReportFiles.scoped(success,name,content).equals("own-row"));ok(success.events.equals(Arrays.asList("begin","open","close","publish")));ok(Arrays.equals(success.data.toByteArray(),content));
   for(String step:new String[]{"begin","open","null","write","close","publish"}){Store store=new Store();store.fail=step;boolean failed=false;try{ReportFiles.scoped(store,name,content);}catch(IOException expected){failed=true;}ok(failed);ok(step.equals("begin")?!store.events.contains("discard"):store.events.get(store.events.size()-1).equals("discard"));if(!step.equals("publish"))ok(!store.events.contains("publish"));}
  }finally{try(java.util.stream.Stream<Path> paths=Files.walk(dir.toPath())){paths.sorted(Comparator.reverseOrder()).forEach(path->{try{Files.delete(path);}catch(IOException e){throw new RuntimeException(e);}});}}
  System.out.println("PASS "+checks+" report export checks: collision preservation, UTF8, scoped commit/rollback");
 }
}
