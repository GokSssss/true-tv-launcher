package ru.truetv.launcher;
import android.app.Activity;
import java.util.*;
/** Only the two extended actions may move; App info always remains last. */
public final class AppMenus {
 private AppMenus(){}
 public static ActionSheet.Item[] ordered(Activity owner,AppCatalog.Entry app,ActionSheet.Item... original){ArrayList<ActionSheet.Item> result=new ArrayList<>();ActionSheet.Item close=null,restart=null,info=null;boolean first=new SettingsStore(owner).flag("extended_menu_first",false);for(ActionSheet.Item item:original){if(item.icon.equals("info")){info=item;continue;}if(first&&item.icon.equals("close")){close=item;continue;}if(first&&item.icon.equals("restart")){restart=item;continue;}result.add(item);}if(first){if(restart!=null)result.add(0,restart);if(close!=null)result.add(0,close);}if(info!=null)result.add(info);return result.toArray(new ActionSheet.Item[0]);}
}
