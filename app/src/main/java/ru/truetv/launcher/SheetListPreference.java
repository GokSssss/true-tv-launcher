package ru.truetv.launcher;
import android.app.Activity;
import android.content.Context;
import android.preference.ListPreference;
/** Native single-choice radio list with the current value selected. */
public final class SheetListPreference extends ListPreference {
 public SheetListPreference(Context c){super(c);}
 public CharSequence getSummary(){return getEntry();}
 protected void onClick(){ChoiceDialog.show((Activity)getContext(),getTitle(),getEntries(),findIndexOfValue(getValue()),at->{String value=getEntryValues()[at].toString();if(callChangeListener(value)){setValue(value);setSummary(getEntry());if(getKey().equals("theme"))((Activity)getContext()).recreate();}});}
}
