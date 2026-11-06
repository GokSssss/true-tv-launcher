package ru.truetv.probe;
import android.app.*;
import android.content.*;
import android.net.Uri;
import java.io.*;
public final class FileReceiver extends Activity {
 protected void onCreate(android.os.Bundle state){super.onCreate(state);Intent result=new Intent(FileClient.RESULT).setPackage("ru.truetv.launcher");try{Uri uri=getIntent().getData();if(uri==null)uri=getIntent().getParcelableExtra(Intent.EXTRA_STREAM);result.putExtra("text",FileClient.read(this,uri));boolean denied=false;try(OutputStream ignored=getContentResolver().openOutputStream(uri,"wa")){}catch(SecurityException|FileNotFoundException expected){denied=true;}result.putExtra("writeDenied",denied);result.putExtra("ok",true);}catch(Exception e){result.putExtra("error",e.toString());}sendBroadcast(result);finish();}
}
