package ru.truetv.launcher;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.nsd.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import ru.truetv.bridge.*;
import ru.truetv.bridge.android.*;

/** A visible receive window owns the socket. Leaving it closes discovery, consent and payload. */
public final class ShareSettingsActivity extends Activity {
 private final Handler main=new Handler(Looper.getMainLooper());
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private final Object lock=new Object();
 private final TransferWake wake=new TransferWake();
 private volatile Transfer.Receiver receiver;
 private volatile boolean visible;
 private volatile int generation;
 private NsdManager nsd;private NsdManager.RegistrationListener registration;
 private TextView state,details;private CheckBox automatic;private ProgressBar progress;
 private Button accept,reject,cancel,open,location;private Decision decision;
 private final ArrayList<Received> received=new ArrayList<>();
 private boolean permissionAsked;
 private static final class Received {final Uri uri;final String name,mime;Received(Uri u,String n,String m){uri=u;name=n;mime=m;}}
 private String w(String ru,String en){return BridgeUi.w(this,ru,en);}
 protected void onCreate(Bundle saved){Ui.prepare(this);super.onCreate(saved);requestWindowFeature(Window.FEATURE_NO_TITLE);BridgeUi.top(this);
  // Old versions offered permanent background reception. It no longer exists.
  getSharedPreferences("totv_receiver",0).edit().clear().apply();getSharedPreferences("totv_trust",0).edit().clear().apply();
  LinearLayout body=BridgeUi.box(this);body.addView(BridgeUi.label(this,w("Принять файл по Wi-Fi","Receive a file over Wi-Fi"),23));
  body.addView(BridgeUi.label(this,w("На телефоне: Отправить → To TV → выберите телевизор. Файлы сохраняются в Downloads. Приём работает только пока это окно открыто.","On phone: Share → To TV → choose this TV. Files go to Downloads. Reception works only while this window is open."),16));
  automatic=new CheckBox(this);automatic.setText(w("Принимать без подтверждения","Accept without confirmation"));automatic.setTextColor(android.graphics.Color.WHITE);automatic.setTextSize(17);automatic.setChecked(getSharedPreferences("totv_window",0).getBoolean("automatic",false));automatic.setOnCheckedChangeListener((b,on)->getSharedPreferences("totv_window",0).edit().putBoolean("automatic",on).apply());body.addView(automatic);
  state=BridgeUi.label(this,w("Подготовка…","Preparing…"),19);body.addView(state);details=BridgeUi.label(this,"",16);body.addView(details);
  progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(1000);progress.setVisibility(View.GONE);body.addView(progress,new LinearLayout.LayoutParams(-1,BridgeUi.dp(this,14)));
  accept=BridgeUi.button(this,w("Принять","Accept"),()->answer(true));reject=BridgeUi.button(this,w("Отклонить","Decline"),()->answer(false));cancel=BridgeUi.button(this,w("Отменить передачу","Cancel transfer"),this::cancelTransfer);open=BridgeUi.button(this,w("Открыть файл","Open file"),this::openFile);location=BridgeUi.button(this,w("Показать в папке","Show in folder"),()->{startActivity(new Intent(this,FileActivity.class).putExtra("downloads",true));finish();});
  for(Button b:new Button[]{accept,reject,cancel,open,location}){b.setVisibility(View.GONE);body.addView(b);}body.addView(BridgeUi.button(this,w("Закрыть","Close"),this::finish));ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.addView(body);setContentView(scroll);
 }
 protected void onResume(){super.onResume();visible=true;if(storage())startReceiver();else if(!permissionAsked){permissionAsked=true;requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},40);}else state.setText(w("Для сохранения файлов разрешите доступ к хранилищу.","Allow storage access to save files."));}
 protected void onPause(){visible=false;stopReceiver();super.onPause();}
 protected void onDestroy(){stopReceiver();worker.shutdownNow();super.onDestroy();}
 private boolean storage(){return Build.VERSION.SDK_INT<23||Build.VERSION.SDK_INT>=29||checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE")==PackageManager.PERMISSION_GRANTED;}
 public void onRequestPermissionsResult(int code,String[] names,int[] values){super.onRequestPermissionsResult(code,names,values);if(code==40){if(storage()&&visible)startReceiver();else state.setText(w("Без доступа к хранилищу приём невозможен. Закройте окно и откройте снова для запроса доступа.","Storage access is required. Close and reopen this window to request access again."));}}
 private void post(int g,Runnable action){main.post(()->{if(visible&&generation==g&&!isFinishing())action.run();});}
 private void startReceiver(){final int g;synchronized(lock){if(receiver!=null||registration!=null)return;g=++generation;}
  automatic.setEnabled(true);accept.setVisibility(View.GONE);reject.setVisibility(View.GONE);cancel.setVisibility(View.GONE);cancel.setEnabled(true);progress.setVisibility(View.GONE);state.setText(w("Запуск приёма…","Starting reception…"));worker.execute(()->{try{
   Identity identity=Identity.load(new File(getFilesDir(),"to-tv-identity"));
   String label=BridgeUi.device(this);Transfer.Receiver next=new Transfer.Receiver(identity,label,new BridgeDownloads(this,(uri,name,mime)->post(g,()->received.add(new Received(uri,name,mime)))),(offer,fp,code)->consent(g,offer),new Transfer.Events(){public void update(int index,String name,long bytes,long total){post(g,()->{state.setText(w("Приём: ","Receiving: ")+name);details.setText(BridgeUi.bytes(ShareSettingsActivity.this,bytes)+(total<0?"":" / "+BridgeUi.bytes(ShareSettingsActivity.this,total)));progress.setIndeterminate(total<0);if(total>=0)progress.setProgress(total==0?1000:(int)Math.min(1000,1000d*bytes/total));});}public void finished(boolean success,String detail){post(g,()->complete(success,detail));}});
   synchronized(lock){if(!visible||g!=generation){next.close();return;}receiver=next;}
   String fingerprint=identity.fingerprint();post(g,()->advertise(g,fingerprint,next.port(),label));next.accept();
  }catch(Exception e){post(g,()->{state.setText(BridgeUi.error(this,e));stopReceiver();});}});
 }
 private void advertise(int g,String fingerprint,int port,String label){nsd=(NsdManager)getSystemService(NSD_SERVICE);NsdServiceInfo info=new NsdServiceInfo();info.setServiceType(Wire.TYPE);info.setServiceName("TrueTV-"+fingerprint.substring(0,12));info.setPort(port);info.setAttribute("id",fingerprint);info.setAttribute("name",label);registration=new NsdManager.RegistrationListener(){public void onServiceRegistered(NsdServiceInfo i){post(g,()->state.setText(w("Готов к приёму · ","Ready to receive · ")+label));}public void onServiceUnregistered(NsdServiceInfo i){}public void onRegistrationFailed(NsdServiceInfo i,int error){post(g,()->{state.setText(w("Не удалось объявить приёмник в сети: ","Could not advertise receiver: ")+error);stopReceiver();});}public void onUnregistrationFailed(NsdServiceInfo i,int error){}};try{nsd.registerService(info,NsdManager.PROTOCOL_DNS_SD,registration);}catch(RuntimeException e){state.setText(BridgeUi.error(this,e));stopReceiver();}}
 private boolean consent(int g,Wire.Offer offer)throws Exception {Decision answer=new Decision();main.post(()->{if(!visible||g!=generation||isFinishing()){answer.answer(0);return;}received.clear();decision=answer;automatic.setEnabled(false);open.setVisibility(View.GONE);location.setVisibility(View.GONE);details.setText(offer.files.size()+w(" файлов · "," files · ")+BridgeUi.bytes(this,offer.size)+"\n"+offer.files.get(0).name);state.setText(w("Принять от ","Accept from ")+offer.device+"?");if(automatic.isChecked())answer(true);else{accept.setVisibility(View.VISIBLE);reject.setVisibility(View.VISIBLE);accept.requestFocus();}});return answer.await(120)==1&&visible&&generation==g;}
 private void answer(boolean yes){Decision d=decision;if(d==null)return;decision=null;accept.setVisibility(View.GONE);reject.setVisibility(View.GONE);if(yes){wake.start(this);state.setText(w("Приём файла…","Receiving file…"));progress.setIndeterminate(true);progress.setVisibility(View.VISIBLE);cancel.setVisibility(View.VISIBLE);cancel.requestFocus();}d.answer(yes?1:0);}
 private void cancelTransfer(){if(decision!=null)answer(false);Transfer.Receiver r=receiver;if(r!=null)r.cancel();wake.stop();cancel.setEnabled(false);}
 private void complete(boolean success,String detail){wake.stop();decision=null;automatic.setEnabled(true);accept.setVisibility(View.GONE);reject.setVisibility(View.GONE);progress.setVisibility(View.GONE);cancel.setVisibility(View.GONE);cancel.setEnabled(true);state.setText(success?w("Файл получен","File received"):detail.contains("REJECTED")?w("Приём отклонён","Reception declined"):w("Передача не завершена","Transfer incomplete"));StringBuilder names=new StringBuilder();for(Received f:received){if(names.length()>0)names.append("\n");names.append(f.name);}details.setText((success?"":BridgeUi.error(this,new IOException(detail))+"\n")+names+ (received.isEmpty()?"":"\nDownloads"));open.setVisibility(received.isEmpty()?View.GONE:View.VISIBLE);location.setVisibility(received.isEmpty()?View.GONE:View.VISIBLE);if(!received.isEmpty())open.requestFocus();}
 private void openFile(){if(received.isEmpty())return;if(received.size()==1){open(received.get(0));return;}String[] names=new String[received.size()];for(int i=0;i<names.length;i++)names[i]=received.get(i).name;new AlertDialog.Builder(this).setTitle(w("Открыть файл","Open file")).setItems(names,(d,i)->open(received.get(i))).show();}
 private void open(Received file){Intent view=new Intent(Intent.ACTION_VIEW).setDataAndType(file.uri,file.mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);view.setClipData(ClipData.newRawUri(file.name,file.uri));try{startActivity(view);finish();}catch(RuntimeException e){new AlertDialog.Builder(this).setMessage(w("Нет приложения для открытия этого файла.","No app can open this file.")).setPositiveButton("OK",null).show();}}
 private void stopReceiver(){Transfer.Receiver old;synchronized(lock){generation++;old=receiver;receiver=null;}if(decision!=null){decision.answer(0);decision=null;}wake.stop();if(old!=null)try{old.close();}catch(IOException ignored){}if(nsd!=null&&registration!=null)try{nsd.unregisterService(registration);}catch(RuntimeException ignored){}registration=null;}
}
