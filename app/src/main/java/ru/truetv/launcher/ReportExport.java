package ru.truetv.launcher;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/** Shared Downloads on both legacy and scoped-storage Android. Does not use root or SAF. */
public final class ReportExport {
 private ReportExport(){}
 public static final class Result {
  public final Uri uri;public final String location;
  Result(Uri uri,String location){this.uri=uri;this.location=location;}
 }
 public static Result save(Context context,String text)throws IOException {
  if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))throw new IOException("Downloads storage unavailable");
  String name="IR-diagnostic-"+new SimpleDateFormat("yyyyMMdd-HHmmss-SSS",Locale.ROOT).format(new Date())+".txt";byte[] content=text.getBytes(StandardCharsets.UTF_8);
  if(Build.VERSION.SDK_INT<29){File file=ReportFiles.legacy(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),name,content);return new Result(Uri.fromFile(file),file.getAbsolutePath());}
  return Modern.save(context,name,content);
 }
 private static final class Modern {
  static Result save(Context c,String name,byte[] content)throws IOException {
   ContentResolver resolver=c.getContentResolver();String token=ReportFiles.scoped(new ReportFiles.Store(){
    public String begin(String displayName){ContentValues values=new ContentValues();values.put(MediaStore.MediaColumns.DISPLAY_NAME,displayName);values.put(MediaStore.MediaColumns.MIME_TYPE,"text/plain");values.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/");values.put(MediaStore.MediaColumns.IS_PENDING,1);Uri uri=resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,values);return uri==null?null:uri.toString();}
    public OutputStream open(String token)throws IOException{return resolver.openOutputStream(Uri.parse(token),"w");}
    public void publish(String token)throws IOException {ContentValues values=new ContentValues();values.put(MediaStore.MediaColumns.IS_PENDING,0);if(resolver.update(Uri.parse(token),values,null,null)!=1)throw new IOException("Cannot publish Downloads report");}
    public void discard(String token){resolver.delete(Uri.parse(token),null,null);}
   },name,content);
   Uri uri=Uri.parse(token);String actual=name;
   try(Cursor cursor=resolver.query(uri,new String[]{MediaStore.MediaColumns.DISPLAY_NAME},null,null,null)){if(cursor!=null&&cursor.moveToFirst())actual=cursor.getString(0);}catch(RuntimeException ignored){}
   return new Result(uri,Environment.DIRECTORY_DOWNLOADS+"/"+actual);
  }
 }
}
