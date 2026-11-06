package ru.truetv.launcher;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import java.io.*;
import ru.truetv.bridge.*;

/** Public Downloads only. Pending rows / hidden same-filesystem staging never publish partial data. */
final class BridgeDownloads implements Wire.Store {
 interface Published {void saved(Uri uri,String name,String mime);}
 private final Context context;private final SharedPreferences journal;private final Published published;
 BridgeDownloads(Context c)throws IOException {this(c,null);}
 BridgeDownloads(Context c,Published callback)throws IOException {published=callback;context=c.getApplicationContext();journal=c.getSharedPreferences("totv_pending",0);cleanup();}
 private File downloads()throws IOException {File root=Environment.getExternalStorageDirectory().getCanonicalFile(),d=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);if(!d.isDirectory()&&!d.mkdirs())throw new IOException("Downloads unavailable");if(!d.getCanonicalPath().startsWith(root.getPath()+File.separator))throw new IOException("Downloads path escaped shared storage");return d;}
 private void clear(){journal.edit().clear().commit();}
 private void cleanup()throws IOException {String raw=journal.getString("uri",null),path=journal.getString("path",null);if(raw!=null&&Build.VERSION.SDK_INT>=29){Uri u=Uri.parse(raw);if("media".equals(u.getAuthority())&&u.getPath()!=null&&u.getPath().contains("/downloads/"))try(Cursor c=context.getContentResolver().query(u,new String[]{"is_pending"},null,null,null)){if(c!=null&&c.moveToFirst()&&c.getInt(0)==1)context.getContentResolver().delete(u,null,null);}}if(path!=null){File p=new File(path);if(p.getName().startsWith(".ToTV-")&&p.getName().endsWith(".part")&&p.getCanonicalFile().getParentFile().equals(downloads()))p.delete();}clear();}
 public Wire.Target create(Wire.Item item)throws IOException {return Build.VERSION.SDK_INT>=29?scoped(item):legacy(item);}
 private Wire.Target scoped(Wire.Item item)throws IOException {
  ContentResolver resolver=context.getContentResolver();ContentValues v=new ContentValues();v.put(MediaStore.MediaColumns.DISPLAY_NAME,".ToTV-"+java.util.UUID.randomUUID()+".part");v.put(MediaStore.MediaColumns.MIME_TYPE,"application/octet-stream");v.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS);v.put(MediaStore.MediaColumns.IS_PENDING,1);Uri u=resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);if(u==null)throw new IOException("Cannot create Downloads entry");if(!journal.edit().putString("uri",u.toString()).commit()){resolver.delete(u,null,null);throw new IOException("Cannot journal pending file");}
  return new Wire.Target(){public OutputStream stream()throws IOException {ParcelFileDescriptor fd=resolver.openFileDescriptor(u,"w");if(fd==null)throw new IOException("Cannot open Downloads entry");return new ParcelFileDescriptor.AutoCloseOutputStream(fd){public void close()throws IOException {try{getFD().sync();}finally{super.close();}}};}public String publish()throws IOException {ContentValues done=new ContentValues();done.put(MediaStore.MediaColumns.DISPLAY_NAME,item.name);done.put(MediaStore.MediaColumns.MIME_TYPE,item.mime);done.put(MediaStore.MediaColumns.IS_PENDING,0);if(resolver.update(u,done,null,null)!=1)throw new IOException("Cannot publish Downloads entry");clear();if(published!=null){String name=item.name;try(Cursor c=resolver.query(u,new String[]{MediaStore.MediaColumns.DISPLAY_NAME},null,null,null)){if(c!=null&&c.moveToFirst())name=c.getString(0);}published.saved(u,name,item.mime);}return u.toString();}public void discard(){try{resolver.delete(u,null,null);}catch(RuntimeException ignored){}clear();}};
 }
 private Wire.Target legacy(Wire.Item item)throws IOException {
  File dir=downloads();if(item.size>=0&&item.size>dir.getUsableSpace())throw new IOException("Not enough space in Downloads");File stage=File.createTempFile(".ToTV-",".part",dir);if(!journal.edit().putString("path",stage.getAbsolutePath()).commit()){stage.delete();throw new IOException("Cannot journal pending file");}
  return new Wire.Target(){public OutputStream stream()throws IOException {return new FileOutputStream(stage){public void close()throws IOException {try{getFD().sync();}finally{super.close();}}};}public String publish()throws IOException {String name=item.name;int dot=name.lastIndexOf('.');String base=dot>0?name.substring(0,dot):name,ext=dot>0?name.substring(dot):"";for(int n=0;n<10000;n++){File target=new File(dir,n==0?name:base+" ("+n+")"+ext);if(!target.createNewFile())continue;if(!stage.renameTo(target)){target.delete();throw new IOException("Cannot publish received file");}clear();android.media.MediaScannerConnection.scanFile(context,new String[]{target.getAbsolutePath()},new String[]{item.mime},null);if(published!=null)published.saved(FileStore.get(context).uri(FileStore.get(context).identify(target)),target.getName(),item.mime);return target.getName();}throw new IOException("Too many same-name files");}public void discard(){stage.delete();clear();}};
 }
}
