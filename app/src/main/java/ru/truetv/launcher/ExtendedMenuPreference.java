package ru.truetv.launcher;
import android.content.Context;
import android.preference.CheckBoxPreference;
import android.view.*;
import android.widget.*;
/** One native checkbox with the two actual action glyphs. */
public final class ExtendedMenuPreference extends CheckBoxPreference {
 public ExtendedMenuPreference(Context c){super(c);}
 protected View onCreateView(ViewGroup parent){Context c=getContext();LinearLayout row=new LinearLayout(c);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(Ui.dp(c,16),Ui.dp(c,10),Ui.dp(c,16),Ui.dp(c,10));for(String key:new String[]{"close","restart"})row.addView(new Glyph(c,key),new LinearLayout.LayoutParams(Ui.dp(c,24),Ui.dp(c,24)));LinearLayout text=new LinearLayout(c);text.setOrientation(1);TextView title=Ui.label(c,"",18);title.setId(android.R.id.title);text.addView(title);TextView summary=Ui.label(c,"",14);summary.setTextColor(Ui.muted(c));summary.setId(android.R.id.summary);summary.setPadding(0,Ui.dp(c,4),0,0);text.addView(summary);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.leftMargin=Ui.dp(c,16);p.rightMargin=Ui.dp(c,12);row.addView(text,p);CheckBox box=new CheckBox(c);box.setId(android.R.id.checkbox);box.setFocusable(false);box.setClickable(false);row.addView(box,new LinearLayout.LayoutParams(-2,-2));return row;}
}
