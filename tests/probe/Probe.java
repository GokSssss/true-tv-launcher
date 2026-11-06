package ru.truetv.probe;
public final class Probe extends android.app.Activity {
 public void onCreate(android.os.Bundle b){super.onCreate(b);android.widget.TextView label=new android.widget.TextView(this);label.setText("True TV Launcher control probe");label.setTextSize(30);setContentView(label);}
}
