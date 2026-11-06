package ru.truetv.launcher;
import java.io.File;

/** File-list order. The separate navigation header and alphabetical tree are unaffected. */
public final class FileOrder {
 private FileOrder(){}
 public static int valid(int mode){return mode>=0&&mode<4?mode:0;}
 public static int compare(File a,File b,int mode){
  mode=valid(mode);int order=0;
  if(mode<2)order=mode==0?Long.compare(b.lastModified(),a.lastModified()):Long.compare(a.lastModified(),b.lastModified());
  if(order!=0)return order;
  order=a.getName().compareToIgnoreCase(b.getName());
  if(order==0)order=a.getName().compareTo(b.getName());
  if(order==0)order=a.getAbsolutePath().compareTo(b.getAbsolutePath());
  return mode==3?-Integer.signum(order):order;
 }
}
