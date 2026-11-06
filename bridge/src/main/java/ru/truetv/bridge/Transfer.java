package ru.truetv.bridge;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.net.ssl.*;

/** One transfer at a time; blocked accept/read consumes no idle CPU and bounds open clients. */
public final class Transfer {
 private Transfer(){}
 // TLS close may send close_notify. Revoke first; perform network shutdown off the UI thread.
 private static void closeAsync(SSLSocket socket){Thread t=new Thread(()->{try{socket.close();}catch(IOException ignored){}},"ToTV-close");t.setDaemon(true);t.start();}
 public interface Consent {boolean approve(Wire.Offer offer,String fingerprint,String code)throws Exception;}
 public interface Events extends Wire.Progress {void finished(boolean success,String detail);}
 public static final class Receiver implements Closeable {
  private final SSLServerSocket server;private final Identity identity;private final String name;private final Wire.Store store;private final Consent consent;private final Events events;private final ExecutorService worker=Executors.newSingleThreadExecutor();private final AtomicBoolean busy=new AtomicBoolean();private volatile boolean closed,cancelled;private volatile SSLSocket current;
  public Receiver(Identity id,String n,Wire.Store s,Consent c,Events e)throws Exception {identity=id;name=n;store=s;consent=c;events=e;server=(SSLServerSocket)id.tls(null).getServerSocketFactory().createServerSocket(0,4);server.setEnabledProtocols(new String[]{"TLSv1.2"});server.setNeedClientAuth(true);}
  public int port(){return server.getLocalPort();}
  public void accept()throws IOException {while(!closed){SSLSocket socket=(SSLSocket)server.accept();if(!Wire.local(socket.getInetAddress())||!busy.compareAndSet(false,true)){socket.close();continue;}cancelled=false;current=socket;worker.execute(()->{try{receive(socket);}finally{current=null;busy.set(false);}});}}
  private void receive(SSLSocket socket){Wire.Target target=null;int completed=0;try(SSLSocket connection=socket){socket.setSoTimeout(15000);socket.startHandshake();byte[] own=identity.certificate.getEncoded(),peer=Identity.peer(socket);DataInputStream in=new DataInputStream(new BufferedInputStream(socket.getInputStream(),Wire.CHUNK));DataOutputStream out=new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(),Wire.CHUNK));if(in.readInt()!=Wire.MAGIC)throw new IOException("Unsupported transfer");byte[] a=new byte[32],b=Wire.random();in.readFully(a);out.writeInt(Wire.MAGIC);Wire.text(out,name,256);out.write(b);out.flush();Wire.Offer offer=Wire.Offer.read(in);String fingerprint=Wire.hex(Wire.digest(peer));out.writeInt(Wire.WAIT);out.flush();boolean accepted=consent.approve(offer,fingerprint,Wire.code(own,peer,a,b));out.writeInt(accepted?Wire.ACCEPT:Wire.REJECT);out.flush();if(!accepted){events.finished(false,"REJECTED");return;}socket.setSoTimeout(45000);byte[] buffer=new byte[Wire.CHUNK];long all=0;long last=0;
   for(int i=0;i<offer.files.size();i++){Wire.Item item=offer.files.get(i);target=store.create(item);long bytes=0;MessageDigest digest=MessageDigest.getInstance("SHA-256");try(OutputStream file=target.stream()){int n;while((n=in.readInt())!=0){if(n<0||n>buffer.length)throw new IOException("Invalid file chunk");in.readFully(buffer,0,n);if(Long.MAX_VALUE-bytes<n||item.size>=0&&bytes+n>item.size)throw new IOException("Unexpected file size");file.write(buffer,0,n);digest.update(buffer,0,n);bytes+=n;all+=n;long now=System.nanoTime();if(now-last>=500000000L){last=now;events.update(i,item.name,all,offer.size);}}}
    byte[] expected=new byte[32];in.readFully(expected);if(item.size>=0&&bytes!=item.size||!MessageDigest.isEqual(expected,digest.digest()))throw new IOException("Incomplete or damaged file");if(closed||cancelled||socket.isClosed())throw new IOException("Transfer cancelled");String saved=target.publish();target=null;completed++;out.writeInt(Wire.FILE_OK);Wire.text(out,saved,2048);out.flush();events.update(i,item.name,all,offer.size);
   }out.writeInt(Wire.DONE);out.flush();events.finished(true,Integer.toString(completed));
  }catch(Exception e){if(target!=null){target.discard();target=null;}events.finished(false,completed+" · "+(e.getMessage()==null?e.getClass().getSimpleName():e.getMessage()));}finally{if(target!=null)target.discard();}}
  public void cancel(){cancelled=true;SSLSocket socket=current;if(socket!=null)closeAsync(socket);}
  public void close()throws IOException {closed=true;cancel();server.close();worker.shutdownNow();}
 }
 public interface ClientEvents extends Wire.Progress {void waiting(String tvName,String fingerprint,String code);void accepted(String fingerprint);}
 public static final class Sender implements Closeable {
  private volatile SSLSocket socket;private volatile boolean cancelled;
  public List<String> send(Identity identity,InetAddress host,int port,String pin,Wire.Offer offer,Wire.Source source,ClientEvents events)throws Exception {
   if(!Wire.local(host))throw new IOException("Receiver must be on the local network");SSLSocket s=(SSLSocket)identity.tls(pin).getSocketFactory().createSocket();socket=s;try(SSLSocket connection=s){if(cancelled)throw new IOException("Transfer cancelled");s.setEnabledProtocols(new String[]{"TLSv1.2"});s.connect(new InetSocketAddress(host,port),8000);s.setSoTimeout(15000);s.startHandshake();byte[] own=identity.certificate.getEncoded(),peer=Identity.peer(s),a=Wire.random(),b=new byte[32];String fingerprint=Wire.hex(Wire.digest(peer));DataInputStream in=new DataInputStream(new BufferedInputStream(s.getInputStream(),Wire.CHUNK));DataOutputStream out=new DataOutputStream(new BufferedOutputStream(s.getOutputStream(),Wire.CHUNK));out.writeInt(Wire.MAGIC);out.write(a);out.flush();if(in.readInt()!=Wire.MAGIC)throw new IOException("Unsupported receiver");String tv=Wire.text(in,256);in.readFully(b);offer.write(out);out.flush();if(in.readInt()!=Wire.WAIT)throw new IOException("Unexpected receiver response");events.waiting(tv,fingerprint,Wire.code(peer,own,a,b));s.setSoTimeout(130000);int decision=in.readInt();if(decision==Wire.REJECT)throw new IOException("REJECTED");if(decision!=Wire.ACCEPT)throw new IOException("Unexpected decision");events.accepted(fingerprint);s.setSoTimeout(45000);List<String> saved=new ArrayList<>();byte[] buffer=new byte[Wire.CHUNK];long all=0,last=0;
    for(int i=0;i<offer.files.size();i++){Wire.Item file=offer.files.get(i);MessageDigest digest=MessageDigest.getInstance("SHA-256");long bytes=0;try(InputStream content=source.open(i)){if(content==null)throw new IOException("File unavailable");int n;while((n=content.read(buffer))!=-1){if(cancelled)throw new IOException("Transfer cancelled");if(n==0)continue;out.writeInt(n);out.write(buffer,0,n);digest.update(buffer,0,n);bytes+=n;all+=n;if(file.size>=0&&bytes>file.size)throw new IOException("Source changed during transfer");long now=System.nanoTime();if(now-last>=500000000L){last=now;events.update(i,file.name,all,offer.size);}}}if(file.size>=0&&bytes!=file.size)throw new IOException("Source changed during transfer");out.writeInt(0);out.write(digest.digest());out.flush();int reply=in.readInt();if(reply!=Wire.FILE_OK)throw new IOException("Receiver did not confirm the file");saved.add(Wire.text(in,2048));events.update(i,file.name,all,offer.size);
    }if(in.readInt()!=Wire.DONE)throw new IOException("Receiver did not confirm completion");return saved;
   }finally{socket=null;}
  }
  public void close(){cancelled=true;SSLSocket s=socket;if(s!=null)closeAsync(s);}
 }
}
