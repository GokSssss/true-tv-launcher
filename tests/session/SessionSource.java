package ru.truetv.launcher;
import android.content.*;import android.database.*;import android.net.*;import android.os.*;import java.io.*;import java.util.*;
/** Test-only read-only deterministic content source; never included in production APK. */
public final class SessionSource extends ContentProvider {
 public boolean onCreate(){return true;}public String getType(Uri u){return "application/octet-stream";}public Cursor query(Uri u,String[] p,String s,String[] a,String o){MatrixCursor c=new MatrixCursor(new String[]{"_display_name","_size"});c.addRow(new Object[]{"test231.bin",2*1024*1024+7});return c;}
 public ParcelFileDescriptor openFile(Uri u,String mode)throws FileNotFoundException{if(!mode.equals("r")||!Build.FINGERPRINT.contains("generic")&&!Build.MODEL.contains("Android SDK"))throw new FileNotFoundException();try{ParcelFileDescriptor[] pipe=ParcelFileDescriptor.createPipe();new Thread(()->{try(OutputStream out=new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])){byte[] b=new byte[2*1024*1024+7];new Random(101).nextBytes(b);out.write(b);}catch(IOException ignored){}}).start();return pipe[0];}catch(IOException e){throw new FileNotFoundException(e.toString());}}
 public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
}
