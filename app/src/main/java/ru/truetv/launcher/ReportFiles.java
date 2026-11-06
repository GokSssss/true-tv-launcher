package ru.truetv.launcher;

import java.io.*;

/** Export transactions own only freshly created files/rows; existing reports are preserved. */
public final class ReportFiles {
 private ReportFiles(){}
 public interface Store {
  String begin(String name)throws IOException;
  OutputStream open(String token)throws IOException;
  void publish(String token)throws IOException;
  void discard(String token)throws IOException;
 }
 private static void name(String name){if(name==null||!name.matches("IR-diagnostic-[0-9-]+\\.txt"))throw new IllegalArgumentException("Report name");}
 public static File legacy(File downloads,String name,byte[] content)throws IOException {
  name(name);if(!downloads.isDirectory()&&!downloads.mkdirs())throw new IOException("Downloads folder unavailable");
  for(int i=0;i<100;i++){
   String candidate=i==0?name:name.substring(0,name.length()-4)+"-"+i+".txt";File file=new File(downloads,candidate);
   if(!file.createNewFile())continue;
   try{try(OutputStream out=new FileOutputStream(file)){out.write(content);}return file;}catch(IOException|RuntimeException failure){if(!file.delete())failure.addSuppressed(new IOException("Cannot remove incomplete report"));throw failure;}
  }
  throw new IOException("Too many files with this report name");
 }
 public static String scoped(Store store,String name,byte[] content)throws IOException {
  name(name);String token=store.begin(name);if(token==null)throw new IOException("Cannot create Downloads entry");
  try{try(OutputStream out=store.open(token)){if(out==null)throw new IOException("Downloads stream unavailable");out.write(content);}store.publish(token);return token;}
  catch(IOException|RuntimeException failure){try{store.discard(token);}catch(Exception cleanup){failure.addSuppressed(cleanup);}throw failure;}
 }
}
