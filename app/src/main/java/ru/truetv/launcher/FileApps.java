package ru.truetv.launcher;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import java.util.*;

/** File associations use MIME and real ACTION_VIEW handlers; only read URI access is granted. */
public final class FileApps {
 private FileApps(){}
 private static Intent view(Activity a,FileStore.Node node){Uri uri=FileStore.get(a).uri(node);Intent intent=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,FileCore.mime(node.file.getName())).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);intent.setClipData(ClipData.newRawUri(node.file.getName(),uri));return intent;}
 public static void open(Activity a,FileStore.Node node,boolean choose){
  Intent intent=view(a,node);List<ResolveInfo> handlers=a.getPackageManager().queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);LinkedHashMap<String,ResolveInfo> unique=new LinkedHashMap<>();for(ResolveInfo r:handlers)if(!r.activityInfo.packageName.equals(a.getPackageName()))unique.put(new ComponentName(r.activityInfo.packageName,r.activityInfo.name).flattenToString(),r);
  if(unique.isEmpty()){new AlertDialog.Builder(a).setTitle(Ui.word(a,"Нет подходящего приложения","No matching app")).setMessage(node.file.getName()+"\n"+FileCore.mime(node.file.getName())).setPositiveButton("OK",null).show();return;}
  String key="open."+FileCore.extension(node.file.getName()),saved=new SettingsStore(a).text(key,"");if(!choose&&unique.containsKey(saved)){launch(a,intent,ComponentName.unflattenFromString(saved));return;}
  ArrayList<String> keys=new ArrayList<>(unique.keySet());String[] names=new String[keys.size()];for(int i=0;i<names.length;i++)names[i]=unique.get(keys.get(i)).loadLabel(a.getPackageManager()).toString();
  ChoiceDialog.show(a,Ui.word(a,"Открыть с помощью","Open with"),names,Math.max(0,keys.indexOf(saved)),at->{String component=keys.get(at);ActionSheet.show(a,names[at],new ActionSheet.Item(Ui.word(a,"Только сейчас","Just once"),"apps",()->launch(a,intent,ComponentName.unflattenFromString(component))),new ActionSheet.Item(Ui.word(a,"Всегда для этого расширения","Always for this extension"),"check",()->{new SettingsStore(a).data.edit().putString(key,component).apply();launch(a,intent,ComponentName.unflattenFromString(component));}));});
 }
 private static void launch(Activity a,Intent intent,ComponentName component){if(!Ui.launch(a,new Intent(intent).setComponent(component)))Ui.toast(a,"Приложение не смогло открыть файл","The app could not open the file");}
 public static void share(Activity a,List<FileStore.Node> nodes){share(a,nodes,false);}
 public static void sendWifi(Activity a,List<FileStore.Node> nodes){share(a,nodes,true);}
 private static void share(Activity a,List<FileStore.Node> nodes,boolean wifi){if(nodes.isEmpty())return;ArrayList<Uri> uris=new ArrayList<>();String mime=FileCore.mime(nodes.get(0).file.getName());for(FileStore.Node n:nodes){if(n.directory()){Ui.toast(a,"Для отправки выберите файлы; папки сначала упакуйте в архив","Select files to share; archive folders first");return;}uris.add(FileStore.get(a).uri(n));if(!FileCore.mime(n.file.getName()).equals(mime))mime="*/*";}
  Intent send=new Intent(uris.size()==1?Intent.ACTION_SEND:Intent.ACTION_SEND_MULTIPLE).setType(mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);if(uris.size()==1)send.putExtra(Intent.EXTRA_STREAM,uris.get(0));else send.putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris);ClipData clip=ClipData.newRawUri(nodes.get(0).file.getName(),uris.get(0));for(int i=1;i<uris.size();i++)clip.addItem(new ClipData.Item(uris.get(i)));send.setClipData(clip);if(wifi)Ui.launch(a,send.setClass(a,ru.truetv.bridge.android.SendActivity.class));else Ui.launch(a,Intent.createChooser(send,Ui.word(a,"Отправить файлы","Share files")));
 }
}
