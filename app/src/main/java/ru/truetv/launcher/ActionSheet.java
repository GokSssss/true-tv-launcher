package ru.truetv.launcher;
import android.app.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.lang.ref.WeakReference;
import java.util.*;

/** Compact focusable actions. Permission explanations belong to their own rows. */
public final class ActionSheet {
 public interface Enabled {boolean now();}
 public static final class Item {
  public final String title,icon;public final Runnable run;public final Enabled enabled;public final String unavailableNote;public final Runnable unavailable;
  public Item(String title,String icon,Runnable run){this(title,icon,run,()->true);}
  public Item(String title,String icon,Runnable run,Enabled enabled){this(title,icon,run,enabled,null,null);}
  public Item(String title,String icon,Runnable run,Enabled enabled,String note,Runnable unavailable){this.title=title;this.icon=icon;this.run=run;this.enabled=enabled;this.unavailableNote=note;this.unavailable=unavailable;}
 }
 private static WeakReference<ActionSheet> active=new WeakReference<>(null);
 private final Activity owner;public final Dialog dialog;public final ArrayList<View> rows=new ArrayList<>();
 private final ArrayList<Item> items;private final ArrayList<TextView> labels=new ArrayList<>(),notes=new ArrayList<>();
 private final LinearLayout body,titleRow;private final ScrollView scroll;private final int width;
 public static void refreshActive(){ActionSheet a=active.get();if(a!=null&&a.dialog.isShowing())a.refresh();}
 public static ActionSheet show(Activity a,String title,Item... choices){return new ActionSheet(a,title,null,Arrays.asList(choices));}
 public static ActionSheet showFiles(Activity a,String title,Item... choices){return new ActionSheet(a,title,null,Arrays.asList(choices),600);}
 public static ActionSheet showApp(Activity a,AppCatalog.Entry app,Item... choices){return new ActionSheet(a,app.label,app.version,Arrays.asList(choices));}
 private ActionSheet(Activity a,String title,String version,List<Item> choices){this(a,title,version,choices,480);}
 private ActionSheet(Activity a,String title,String version,List<Item> choices,int preferredWidth){
  owner=a;items=new ArrayList<>(choices);items.add(new Item(Ui.word(a,"Отмена","Cancel"),"cancel",()->{}));dialog=new Dialog(a);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
  LinearLayout panel=new LinearLayout(a);panel.setOrientation(1);panel.setPadding(Ui.dp(a,6),Ui.dp(a,8),Ui.dp(a,6),Ui.dp(a,6));panel.setBackground(Ui.surface(a,Ui.panel(a),Ui.border(a),14));
  titleRow=new LinearLayout(a);titleRow.setGravity(Gravity.CENTER_VERTICAL);TextView heading=Ui.label(a,title,20);heading.setMaxLines(2);heading.setPadding(Ui.dp(a,12),Ui.dp(a,4),Ui.dp(a,12),Ui.dp(a,8));titleRow.addView(heading,new LinearLayout.LayoutParams(0,-2,1));if(version!=null){TextView v=Ui.label(a,"v"+version,13);v.setTextColor(Ui.muted(a));v.setGravity(Gravity.RIGHT);v.setMaxLines(2);v.setPadding(0,0,Ui.dp(a,12),0);titleRow.addView(v,new LinearLayout.LayoutParams(Ui.dp(a,100),-2));}panel.addView(titleRow);
  body=new LinearLayout(a);body.setOrientation(1);scroll=new ScrollView(a);scroll.addView(body);panel.addView(scroll,new LinearLayout.LayoutParams(-1,-2));
  for(int i=0;i<items.size();i++){
   final int at=i;Item item=items.get(i);LinearLayout row=new LinearLayout(a);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(Ui.dp(a,12),Ui.dp(a,4),Ui.dp(a,12),Ui.dp(a,4));row.setMinimumHeight(Ui.dp(a,36));row.setFocusable(true);row.setClickable(true);row.setId(View.generateViewId());row.addView(new Glyph(a,item.icon),new LinearLayout.LayoutParams(Ui.dp(a,22),Ui.dp(a,22)));
   LinearLayout text=new LinearLayout(a);text.setOrientation(1);TextView label=Ui.label(a,item.title,18);label.setMaxLines(preferredWidth>480?3:2);label.setEllipsize(android.text.TextUtils.TruncateAt.END);text.addView(label);labels.add(label);TextView note=Ui.label(a,"",12);note.setMaxLines(2);note.setPadding(0,Ui.dp(a,2),0,0);note.setTextColor(Ui.muted(a));text.addView(note);notes.add(note);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.leftMargin=Ui.dp(a,12);row.addView(text,lp);
   row.setOnFocusChangeListener((view,focused)->paint(at));row.setOnClickListener(v->{dialog.dismiss();if(!item.enabled.now()){if(item.unavailable!=null)item.unavailable.run();else if(!Controls.ready(a))Controls.openAccessibility(a);else Ui.toast(a,"Действие недоступно на этой прошивке или служба занята","Unavailable on this firmware or service busy");return;}if(at<items.size()-1)item.run.run();});
   row.setOnKeyListener((v,key,e)->{if(key!=KeyEvent.KEYCODE_DPAD_UP&&key!=KeyEvent.KEYCODE_DPAD_DOWN)return false;if(e.getAction()==KeyEvent.ACTION_DOWN)rows.get((at+(key==KeyEvent.KEYCODE_DPAD_UP?-1:1)+items.size())%items.size()).requestFocus();return true;});rows.add(row);body.addView(row,new LinearLayout.LayoutParams(-1,-2));
  }
  width=Math.min(Ui.dp(a,preferredWidth),a.getResources().getDisplayMetrics().widthPixels-Ui.dp(a,32));dialog.setContentView(panel);Window w=dialog.getWindow();w.setBackgroundDrawableResource(android.R.color.transparent);w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams attributes=w.getAttributes();attributes.dimAmount=.25f;w.setAttributes(attributes);dialog.show();w.setLayout(width,-2);active=new WeakReference<>(this);refresh();rows.get(0).post(()->rows.get(0).requestFocusFromTouch());
 }
 private void paint(int i){boolean enabled=items.get(i).enabled.now();View row=rows.get(i);row.setBackground(Ui.surface(owner,row.hasFocus()?Ui.focusFill(owner):Color.TRANSPARENT,row.hasFocus()?Ui.focusBorder(owner):0,8));((ViewGroup)row).getChildAt(0).setAlpha(enabled?1f:.4f);labels.get(i).setTextColor(enabled?Ui.ink(owner):Ui.muted(owner));TextView note=notes.get(i);note.setText(items.get(i).unavailableNote!=null?items.get(i).unavailableNote:Ui.word(owner,!Controls.ready(owner)?"Необходим доступ к специальным возможностям":"Действие недоступно на этой прошивке",!Controls.ready(owner)?"Accessibility access is required":"Unavailable on this firmware"));note.setVisibility(enabled?View.GONE:View.VISIBLE);}
 private void resize(){int spec=View.MeasureSpec.makeMeasureSpec(width-Ui.dp(owner,12),View.MeasureSpec.EXACTLY);titleRow.measure(spec,View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));body.measure(spec,View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));Rect visible=new Rect();owner.getWindow().getDecorView().getWindowVisibleDisplayFrame(visible);int height=visible.height()>0?visible.height():owner.getResources().getDisplayMetrics().heightPixels;scroll.setLayoutParams(new LinearLayout.LayoutParams(-1,Math.min(body.getMeasuredHeight(),Math.max(Ui.dp(owner,36),height-titleRow.getMeasuredHeight()-Ui.dp(owner,40)))));}
 public void refresh(){for(int i=0;i<rows.size();i++)paint(i);resize();}
}
