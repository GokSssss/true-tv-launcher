package ru.truetv.launcher;
import android.app.Activity;
import android.os.Bundle;
/** App-icon entry always reaches the same HOME task; owns no visible UI. */
public final class LauncherEntry extends Activity {
 protected void onCreate(Bundle state){super.onCreate(state);startActivity(HomeActivity.homeIntent(this));finish();}
}
