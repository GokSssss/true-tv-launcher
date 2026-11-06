package ru.truetv.launcher;
import android.app.*;
import android.graphics.Rect;
import android.view.*;
import android.widget.*;

/** A single native radio selection, separate from cursor focus; no action icons or checkmark text. */
public final class ChoiceDialog {
 public interface Selected {void at(int index);}
 public final Dialog dialog;public final ListView list;public final TextView cancel;
 public static ChoiceDialog show(Activity a,CharSequence title,CharSequence[] values,int current,Selected chosen){return new ChoiceDialog(a,title,values,current,chosen);}
 private ChoiceDialog(Activity a,CharSequence title,CharSequence[] values,int current,Selected chosen){
  dialog=new Dialog(a);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout panel=new LinearLayout(a);panel.setOrientation(1);panel.setPadding(Ui.dp(a,6),Ui.dp(a,8),Ui.dp(a,6),Ui.dp(a,6));panel.setBackground(Ui.surface(a,Ui.panel(a),Ui.border(a),14));TextView heading=Ui.label(a,title,20);heading.setPadding(Ui.dp(a,12),Ui.dp(a,4),Ui.dp(a,12),Ui.dp(a,10));panel.addView(heading);
  list=new ListView(a);list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);list.setDivider(null);list.setCacheColorHint(android.graphics.Color.TRANSPARENT);list.setSelector(Ui.surface(a,Ui.focusFill(a),Ui.focusBorder(a),8));list.setItemsCanFocus(false);list.setFocusableInTouchMode(true);list.setAdapter(new ArrayAdapter<CharSequence>(a,android.R.layout.simple_list_item_single_choice,values){public View getView(int at,View reuse,ViewGroup parent){CheckedTextView row=(CheckedTextView)super.getView(at,reuse,parent);row.setTextColor(Ui.ink(a));row.setTextSize(18);row.setLayoutParams(new AbsListView.LayoutParams(-1,-2));row.setMinHeight(Ui.dp(a,40));row.setPadding(Ui.dp(a,12),Ui.dp(a,4),Ui.dp(a,12),Ui.dp(a,4));row.setGravity(Gravity.CENTER_VERTICAL);return row;}});int selected=Choices.bound(current,0,Math.max(0,values.length-1));if(values.length>0){list.setItemChecked(selected,true);list.setSelection(selected);}
  int width=Math.min(Ui.dp(a,480),a.getResources().getDisplayMetrics().widthPixels-Ui.dp(a,32));Rect visible=new Rect();a.getWindow().getDecorView().getWindowVisibleDisplayFrame(visible);int available=(visible.height()>0?visible.height():a.getResources().getDisplayMetrics().heightPixels)-Ui.dp(a,112);int rowHeight=Ui.dp(a,44);if(values.length>0){View sample=list.getAdapter().getView(0,null,list);sample.measure(View.MeasureSpec.makeMeasureSpec(width-Ui.dp(a,12),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));rowHeight=sample.getMeasuredHeight();}panel.addView(list,new LinearLayout.LayoutParams(-1,Math.min(Math.max(rowHeight,available),rowHeight*values.length)));
  cancel=Ui.label(a,Ui.word(a,"Отмена","Cancel"),18);cancel.setGravity(Gravity.CENTER_VERTICAL);cancel.setPadding(Ui.dp(a,12),Ui.dp(a,6),Ui.dp(a,12),Ui.dp(a,6));cancel.setMinimumHeight(Ui.dp(a,36));cancel.setFocusable(true);cancel.setClickable(true);cancel.setBackground(Ui.selector(a));cancel.setOnClickListener(v->dialog.dismiss());panel.addView(cancel,new LinearLayout.LayoutParams(-1,-2));
  list.setOnItemClickListener((parent,v,position,id)->{dialog.dismiss();chosen.at(position);});
  list.setOnKeyListener((v,key,e)->{if(key!=KeyEvent.KEYCODE_DPAD_UP&&key!=KeyEvent.KEYCODE_DPAD_DOWN)return false;int at=list.getSelectedItemPosition();if((key==KeyEvent.KEYCODE_DPAD_UP&&at<=0)||(key==KeyEvent.KEYCODE_DPAD_DOWN&&at>=values.length-1)){if(e.getAction()==KeyEvent.ACTION_DOWN)cancel.requestFocus();return true;}return false;});
  cancel.setOnKeyListener((v,key,e)->{if(key!=KeyEvent.KEYCODE_DPAD_UP&&key!=KeyEvent.KEYCODE_DPAD_DOWN)return false;if(e.getAction()==KeyEvent.ACTION_DOWN&&values.length>0){list.requestFocus();list.setSelection(key==KeyEvent.KEYCODE_DPAD_UP?values.length-1:0);}return true;});
  dialog.setContentView(panel);Window w=dialog.getWindow();w.setBackgroundDrawableResource(android.R.color.transparent);dialog.show();w.setLayout(width,-2);list.post(()->{list.requestFocusFromTouch();list.setSelection(selected);});
 }
}
