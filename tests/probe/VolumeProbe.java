package ru.truetv.probe;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
/** Disposable input sink. Only volume counters, no key text/history or production inclusion. */
public final class VolumeProbe extends Activity {
 private TextView label;private int down,up;
 protected void onCreate(Bundle b){super.onCreate(b);label=new TextView(this);label.setTextSize(24);setContentView(label);update();}
 private void update(){label.setText("VOLUME_PROBE SYNTHETIC_DOWN="+down+" SYNTHETIC_UP="+up);}
 public boolean dispatchKeyEvent(KeyEvent e){if(e.getKeyCode()==24||e.getKeyCode()==25){if(e.getDeviceId()==-1){if(e.getAction()==KeyEvent.ACTION_DOWN)down++;if(e.getAction()==KeyEvent.ACTION_UP)up++;update();}return true;}return super.dispatchKeyEvent(e);}
}
