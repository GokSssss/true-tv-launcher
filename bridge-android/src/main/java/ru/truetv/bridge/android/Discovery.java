package ru.truetv.bridge.android;
import android.content.*;
import android.net.nsd.*;
import android.os.*;
import java.net.*;
import java.util.*;
import ru.truetv.bridge.Wire;

/** Android owns multicast advertisements. Only the visible chooser discovers, without ping loops. */
public final class Discovery implements AutoCloseable {
 public static final class Peer {public final String id,name;public final InetAddress host;public final int port;Peer(String i,String n,InetAddress h,int p){id=i;name=n;host=h;port=p;}}
 public interface Listener {void peers(List<Peer> peers);void error(String text);}
 private final NsdManager nsd;private final Listener listener;private final Handler main=new Handler(Looper.getMainLooper());private final LinkedHashMap<String,Peer> peers=new LinkedHashMap<>();private final ArrayDeque<NsdServiceInfo> queue=new ArrayDeque<>();private boolean active,resolving;private int generation;
 public Discovery(Context c,Listener l){nsd=(NsdManager)c.getApplicationContext().getSystemService(Context.NSD_SERVICE);listener=l;}
 private final NsdManager.DiscoveryListener scan=new NsdManager.DiscoveryListener(){public void onDiscoveryStarted(String t){}public void onDiscoveryStopped(String t){}public void onStartDiscoveryFailed(String t,int e){main.post(()->{if(active)listener.error("mDNS · "+e);});}public void onStopDiscoveryFailed(String t,int e){}public void onServiceFound(NsdServiceInfo info){main.post(()->{if(active&&info.getServiceType().contains("_truetv._tcp")&&queue.size()<64){queue.add(info);next();}});}public void onServiceLost(NsdServiceInfo info){main.post(()->{peers.remove(info.getServiceName());emit();});}};
 public void start(){if(active)return;active=true;generation++;peers.clear();emit();try{nsd.discoverServices(Wire.TYPE,NsdManager.PROTOCOL_DNS_SD,scan);}catch(RuntimeException e){listener.error(e.getMessage());}}
 private void next(){if(!active||resolving||queue.isEmpty())return;resolving=true;int epoch=generation;NsdServiceInfo service=queue.remove();try{nsd.resolveService(service,new NsdManager.ResolveListener(){public void onResolveFailed(NsdServiceInfo i,int e){main.post(()->{if(epoch==generation){resolving=false;next();}});}public void onServiceResolved(NsdServiceInfo i){main.post(()->{if(epoch!=generation||!active)return;resolving=false;Map<String,byte[]> a=i.getAttributes();String id=a.get("id")==null?"":new String(a.get("id"),java.nio.charset.StandardCharsets.UTF_8),name=a.get("name")==null?i.getServiceName():new String(a.get("name"),java.nio.charset.StandardCharsets.UTF_8);if(id.matches("[0-9a-f]{64}")&&i.getHost()!=null&&Wire.local(i.getHost())&&i.getPort()>0&&i.getPort()<65536){peers.put(service.getServiceName(),new Peer(id,name,i.getHost(),i.getPort()));emit();}next();});}});}catch(RuntimeException e){resolving=false;listener.error(e.getMessage());next();}}
 private void emit(){if(active){LinkedHashMap<String,Peer> unique=new LinkedHashMap<>();for(Peer p:peers.values())unique.put(p.id,p);listener.peers(new ArrayList<>(unique.values()));}}
 public void close(){if(!active)return;active=false;generation++;queue.clear();resolving=false;try{nsd.stopServiceDiscovery(scan);}catch(RuntimeException ignored){}}
}
