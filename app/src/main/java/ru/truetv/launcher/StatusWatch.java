package ru.truetv.launcher;
import android.Manifest;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.*;
import android.os.*;
import java.lang.reflect.Method;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
/** Android events drive VPN/Bluetooth. Optional process dots alone need foreground sampling. */
public final class StatusWatch {
 public interface Listener {List<String> visiblePackages();void status(boolean vpn,String owner,int bluetooth,Set<String> live);}
 private static final Handler ui=new Handler(Looper.getMainLooper());
 private static final Set<StatusWatch> watches=Collections.newSetFromMap(new WeakHashMap<StatusWatch,Boolean>());
 private static final ThreadPoolExecutor worker=new ThreadPoolExecutor(1,1,0,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(8));
 private static final ConcurrentHashMap<Integer,BluetoothProfile> profiles=new ConcurrentHashMap<>();
 private static final AtomicBoolean profileRequest=new AtomicBoolean(),networkObserved=new AtomicBoolean(),bluetoothObserved=new AtomicBoolean();
 private static final Runnable notifyChanges=()->{for(StatusWatch watch:new ArrayList<>(watches))watch.snapshot();};
 private static void changed(){ui.removeCallbacks(notifyChanges);ui.post(notifyChanges);}
 private static final ConnectivityManager.NetworkCallback networks=new ConnectivityManager.NetworkCallback(){public void onAvailable(Network n){changed();}public void onLost(Network n){changed();}public void onCapabilitiesChanged(Network n,NetworkCapabilities c){changed();}};
 private static final BroadcastReceiver bluetooth=new BroadcastReceiver(){public void onReceive(Context c,Intent i){changed();}};
 private static final BluetoothProfile.ServiceListener profileListener=new BluetoothProfile.ServiceListener(){public void onServiceConnected(int id,BluetoothProfile proxy){profiles.put(id,proxy);changed();}public void onServiceDisconnected(int id){profiles.remove(id);changed();}};
 private final Context context;private final WeakReference<Listener> listener;private final AtomicBoolean pending=new AtomicBoolean();private volatile boolean active;private volatile int epoch;
 private final Runnable runningTick=()->snapshot();
 public StatusWatch(Context c,Listener target){context=c.getApplicationContext();listener=new WeakReference<>(target);}
 // No Binder call in Activity transitions. Shared observers use application context on a worker.
 public void start(){if(active)return;active=true;epoch++;watches.add(this);ui.post(()->snapshot());}
 public void stop(){active=false;epoch++;watches.remove(this);ui.removeCallbacks(runningTick);}
 public void refresh(){snapshot();}
 private void observe(SettingsStore prefs){
  if(networkObserved.compareAndSet(false,true))try{ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);cm.registerNetworkCallback(new NetworkRequest.Builder().removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN).addTransportType(NetworkCapabilities.TRANSPORT_VPN).build(),networks);}catch(RuntimeException ignored){networkObserved.set(false);}
  if(prefs.flag("bt_count",true)&&bluetoothObserved.compareAndSet(false,true))try{IntentFilter f=new IntentFilter();f.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);f.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);f.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);if(Build.VERSION.SDK_INT>=33)context.registerReceiver(bluetooth,f,Context.RECEIVER_EXPORTED);else context.registerReceiver(bluetooth,f);}catch(RuntimeException ignored){bluetoothObserved.set(false);}
 }
 private void requestProfiles(SettingsStore prefs){boolean allowed=Build.VERSION.SDK_INT<31||context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED;if(!allowed||!prefs.flag("bt_count",true)||!profileRequest.compareAndSet(false,true))return;try{BluetoothManager manager=(BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);BluetoothAdapter adapter=manager==null?null:manager.getAdapter();if(adapter==null){profileRequest.set(false);return;}boolean accepted=false;for(int id:new int[]{BluetoothProfile.HEADSET,BluetoothProfile.A2DP,4})accepted|=adapter.getProfileProxy(context,profileListener,id);if(!accepted)profileRequest.set(false);}catch(RuntimeException ignored){profileRequest.set(false);}}
 public static boolean usageAllowed(Context c){try{AppOpsManager ops=(AppOpsManager)c.getSystemService(Context.APP_OPS_SERVICE);return ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),c.getPackageName())==AppOpsManager.MODE_ALLOWED;}catch(RuntimeException ignored){return false;}}
 private void snapshot(){if(!active)return;final int ticket=epoch;Listener target=listener.get();if(target==null){stop();return;}if(!pending.compareAndSet(false,true))return;final List<String> packages=new SettingsStore(context).flag("running",false)?new ArrayList<>(target.visiblePackages()):Collections.emptyList();try{worker.execute(()->{try{
   if(!active||ticket!=epoch)return;SettingsStore prefs=new SettingsStore(context);observe(prefs);requestProfiles(prefs);boolean vpn=false;String owner=null;int count=0;HashSet<String> live=new HashSet<>();
   try{ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);for(Network n:cm.getAllNetworks()){NetworkCapabilities cap=cm.getNetworkCapabilities(n);if(cap!=null&&cap.hasTransport(NetworkCapabilities.TRANSPORT_VPN)){vpn=true;try{int uid=(Integer)NetworkCapabilities.class.getMethod("getOwnerUid").invoke(cap);String[] pkgs=context.getPackageManager().getPackagesForUid(uid);if(uid>=0&&pkgs!=null&&pkgs.length==1)owner=String.valueOf(context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(pkgs[0],0)));}catch(Exception ignored){}}}}catch(RuntimeException ignored){}
   if(prefs.flag("bt_count",true))try{HashSet<String> addresses=new HashSet<>();for(BluetoothProfile p:profiles.values())for(BluetoothDevice d:p.getConnectedDevices())addresses.add(d.getAddress());BluetoothManager m=(BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);if(m!=null)for(BluetoothDevice d:m.getConnectedDevices(BluetoothProfile.GATT))addresses.add(d.getAddress());count=addresses.size();}catch(RuntimeException ignored){}
   final boolean pollRunning=prefs.flag("running",false)&&usageAllowed(context);if(pollRunning)try{ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);Method importance=ActivityManager.class.getMethod("getPackageImportance",String.class);for(String pkg:new HashSet<>(packages)){if(!active||ticket!=epoch)return;int state=(Integer)importance.invoke(am,pkg);if(state>=0&&state<ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE)live.add(pkg);}}catch(Exception ignored){}
   final boolean connected=vpn;final String name=owner;final int devices=count;ui.post(()->{Listener view=listener.get();if(active&&ticket==epoch&&view!=null){view.status(connected,name,devices,live);ui.removeCallbacks(runningTick);if(pollRunning)ui.postDelayed(runningTick,5000);}});
  }finally{pending.set(false);}});}catch(RejectedExecutionException ignored){pending.set(false);}}
}
