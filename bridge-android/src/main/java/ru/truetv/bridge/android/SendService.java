package ru.truetv.bridge.android;
import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.OpenableColumns;
import java.io.*;
import java.lang.ref.WeakReference;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import ru.truetv.bridge.*;

/** URI grants live with this explicitly started foreground service; no broad storage permission. */
public final class SendService extends Service {
 public interface Listener {void changed();}private static WeakReference<Listener> observer=new WeakReference<>(null);public static volatile SendService active;
 public volatile String status="";public volatile boolean done;public volatile long bytes,total=-1;private final Handler main=new Handler(Looper.getMainLooper());private final ExecutorService worker=Executors.newSingleThreadExecutor();private Transfer.Sender sender;private final TransferWake wake=new TransferWake();private int latestStart;private volatile boolean cancelled;
 public static void observe(Listener l){observer=new WeakReference<>(l);if(l!=null)l.changed();}
 public IBinder onBind(Intent i){return null;}
 public int onStartCommand(Intent intent,int flags,int id){if(intent==null)return START_NOT_STICKY;if(sender!=null&&!done)return START_NOT_STICKY;active=this;latestStart=id;done=false;cancelled=false;bytes=0;total=-1;status=BridgeUi.w(this,"Подключение…","Connecting…");startForeground(21,notification());sender=new Transfer.Sender();final Transfer.Sender transfer=sender;worker.execute(()->send(intent,transfer,id));return START_NOT_STICKY;}
 private Notification notification(){return BridgeUi.notification(this,"To TV",status,false,new Intent(this,SendActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("totv_result",done?status:(String)null),false);}
 private void update(String value){main.post(()->{if(active!=this)return;status=value;((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(21,notification());Listener l=observer.get();if(l!=null)l.changed();});}
 private void send(Intent intent,Transfer.Sender transfer,int startId){try{ArrayList<Uri> uris=intent.getParcelableArrayListExtra("uris");if(uris==null||uris.isEmpty()||uris.size()>Wire.MAX_FILES)throw new IOException("No files to send");ArrayList<Wire.Item> files=new ArrayList<>();for(Uri uri:uris){if(!"content".equals(uri.getScheme()))throw new IOException("Use a file manager that shares content URIs");String name=uri.getLastPathSegment();long size=-1;try(Cursor c=getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE},null,null,null)){if(c!=null&&c.moveToFirst()){int n=c.getColumnIndex(OpenableColumns.DISPLAY_NAME),s=c.getColumnIndex(OpenableColumns.SIZE);if(n>=0&&!c.isNull(n))name=c.getString(n);if(s>=0&&!c.isNull(s))size=c.getLong(s);}}files.add(new Wire.Item(name,getContentResolver().getType(uri),size));}
   String key=intent.getStringExtra("peer");if(key==null||!key.matches("[0-9a-f]{64}"))throw new IOException("Invalid TV identity");Identity identity=Identity.load(new File(getFilesDir(),"to-tv-identity"));Wire.Offer offer=new Wire.Offer(BridgeUi.device(this),files);byte[] hostBytes=intent.getByteArrayExtra("host");InetAddress host=hostBytes.length==16?Inet6Address.getByAddress(null,hostBytes,intent.getIntExtra("scope",0)):InetAddress.getByAddress(hostBytes);
   List<String> result=transfer.send(identity,host,intent.getIntExtra("port",0),key,offer,index->getContentResolver().openInputStream(uris.get(index)),new Transfer.ClientEvents(){public void waiting(String tv,String fingerprint,String value){if(cancelled)throw new CancellationException();SendService.this.update(BridgeUi.w(SendService.this,"Ожидание ответа телевизора…","Waiting for TV response…"));}public void accepted(String fingerprint){wake.start(SendService.this);SendService.this.update(BridgeUi.w(SendService.this,"Отправка…","Sending…"));}public void update(int index,String name,long bytes,long total){SendService.this.bytes=bytes;SendService.this.total=total;SendService.this.update((index+1)+"/"+files.size()+" · "+name+" · "+BridgeUi.bytes(SendService.this,bytes)+(total<0?"":" / "+BridgeUi.bytes(SendService.this,total)));}});
   complete(BridgeUi.w(this,"Отправлено файлов: ","Files sent: ")+result.size(),startId);
  }catch(Exception e){complete(cancelled?BridgeUi.w(this,"Передача отменена","Transfer cancelled"):BridgeUi.error(this,e),startId);}}
 public void cancel(){wake.stop();cancelled=true;Transfer.Sender s=sender;if(s!=null)s.close();}
 private void complete(String value,int startId){wake.stop();main.post(()->{if(active!=this||latestStart!=startId)return;done=true;status=value;stopForeground(false);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(21,notification());Listener l=observer.get();if(l!=null)l.changed();stopSelfResult(startId);});}
 public void onDestroy(){cancel();worker.shutdownNow();if(active==this){active=null;Listener l=observer.get();if(l!=null)l.changed();}super.onDestroy();}
}
