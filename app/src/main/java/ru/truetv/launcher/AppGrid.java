package ru.truetv.launcher;
import android.content.*;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;
/** Shared application grid for browsing and the standalone desktop picker. */
final class AppGrid extends LinearLayout implements AppCatalog.Listener,StatusWatch.Listener {
 interface Pick { void accept(AppCatalog.Entry entry); }
 final AppCatalog catalog;final SettingsStore settings;final TextView purpose;final EditText search;final CheckBox system;final GridView grid;
 private List<AppCatalog.Entry> visible=Collections.emptyList();private StatusWatch status;private boolean attached,ownerResumed;
 AppGrid(Context context,int slot,Pick pick,Pick hold){super(context);setOrientation(VERTICAL);catalog=AppCatalog.get(context);settings=new SettingsStore(context);
        LinearLayout root=new LinearLayout(context);root.setOrientation(1);root.setPadding(Ui.dp(context,24),Ui.dp(context,16),Ui.dp(context,24),Ui.dp(context,12));root.setBackgroundColor(settings.flag("wallpaper",false)?(Ui.light(context)?0xe8f3f4f6:0xb3000000):Ui.background(context));addView(root,new LinearLayout.LayoutParams(-1,-1));purpose=Ui.label(context,slot>=0?Ui.word(context,"Добавить на главный экран — место ","Add to desktop — slot ")+(slot+1):Ui.word(context,"Приложения","Applications"),18);purpose.setPadding(0,0,0,Ui.dp(context,6));root.addView(purpose);
        search=new EditText(context);search.setId(View.generateViewId());search.setSingleLine();search.setTextColor(Ui.ink(context));search.setHintTextColor(Ui.muted(context));search.setHint(Ui.word(context,"Поиск установленных приложений","Search installed apps"));search.setTextSize(18);search.setSelectAllOnFocus(false);root.addView(search,new LinearLayout.LayoutParams(-1,Ui.dp(context,52)));
        system=new CheckBox(context);system.setId(View.generateViewId());system.setText(Ui.word(context,"Показывать системные приложения","Show system apps"));system.setTextColor(Ui.ink(context));system.setTextSize(16);system.setChecked(settings.flag("system_apps",false));root.addView(system,new LinearLayout.LayoutParams(-1,Ui.dp(context,44)));
        FrameLayout area=new FrameLayout(context);root.addView(area,new LinearLayout.LayoutParams(-1,0,1));grid=new GridView(context);grid.setId(View.generateViewId());grid.setCacheColorHint(android.graphics.Color.TRANSPARENT);int icon=Ui.dp(context,settings.number("menu_icon",64));grid.setColumnWidth(icon+Ui.dp(context,32));grid.setNumColumns(GridView.AUTO_FIT);grid.setStretchMode(GridView.STRETCH_SPACING_UNIFORM);grid.setVerticalSpacing(Ui.dp(context,16));grid.setPadding(0,Ui.dp(context,8),0,Ui.dp(context,28));grid.setClipToPadding(false);grid.setSelector(new ColorDrawable(Ui.focusFill(context)));area.addView(grid,new FrameLayout.LayoutParams(-1,-1));
        TextView empty=Ui.label(context,Ui.word(context,"Приложений не найдено","No apps found"),18);empty.setGravity(Gravity.CENTER);area.addView(empty,new FrameLayout.LayoutParams(-1,-1));grid.setEmptyView(empty);
        grid.setAdapter(new BaseAdapter(){public int getCount(){return visible.size();}public Object getItem(int p){return visible.get(p);}public long getItemId(int p){return p;}public View getView(int p,View reuse,ViewGroup parent){Tile tile=reuse instanceof Tile?(Tile)reuse:new Tile(context,true);tile.setFocusable(false);tile.setClickable(false);tile.bind(visible.get(p),-1);tile.setLayoutParams(new AbsListView.LayoutParams(-1,icon+Ui.dp(context,64)));return tile;}});
        grid.setOnItemClickListener((parent,v,position,id)->pick.accept(visible.get(position)));
        grid.setOnItemLongClickListener((parent,v,position,id)->{hold.accept(visible.get(position));return true;});
        system.setOnCheckedChangeListener((v,value)->{settings.data.edit().putBoolean("system_apps",value).apply();filter();});search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int n,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){filter();}public void afterTextChanged(Editable e){}});
        search.setNextFocusDownId(system.getId());system.setNextFocusDownId(grid.getId());grid.setNextFocusUpId(system.getId());filter();grid.requestFocus();
 }
 protected void onAttachedToWindow(){super.onAttachedToWindow();attached=true;activate();}
 protected void onDetachedFromWindow(){attached=false;deactivate();super.onDetachedFromWindow();}
 void resume(){ownerResumed=true;activate();}
 private void activate(){if(!attached||!ownerResumed)return;catalog.listen(this);filter();if(status==null)status=new StatusWatch(getContext(),this);status.start();}
 void pause(){ownerResumed=false;deactivate();}
 private void deactivate(){catalog.unlisten(this);if(status!=null)status.stop();}
 public void appsChanged(){if(attached)filter();}
    void filter(){if(grid==null)return;int position=grid.getSelectedItemPosition();visible=catalog.filter(search.getText().toString(),system.isChecked(),settings.flag("show_self",true));((BaseAdapter)grid.getAdapter()).notifyDataSetChanged();if(!visible.isEmpty())grid.setSelection(Choices.bound(position,0,visible.size()-1));}
    public List<String> visiblePackages(){ArrayList<String> out=new ArrayList<>();for(int n=0;n<grid.getChildCount();n++){View child=grid.getChildAt(n);if(child instanceof Tile&&!((Tile)child).pkg.isEmpty())out.add(((Tile)child).pkg);}return out;}
    public void status(boolean vpn,String owner,int bluetooth,Set<String> live){for(int n=0;n<grid.getChildCount();n++){View child=grid.getChildAt(n);if(child instanceof Tile)((Tile)child).running(live.contains(((Tile)child).pkg));}}
}
