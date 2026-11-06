package ru.truetv.launcher;

import android.content.Context;
import android.view.*;

/** Full-width desktop slots; artwork and its compact focus frame stay square inside each slot. */
public final class Board extends ViewGroup {
    public interface Geometry { void edges(int left,int right); }
    private Geometry geometry;private View header;private DesktopGeometry.Grid grid;private final SettingsStore settings;
    public Board(Context c){super(c);settings=new SettingsStore(c);setClipChildren(false);setFocusable(false);}
    public void header(View header,Geometry geometry){this.header=header;this.geometry=geometry;}
    protected void onMeasure(int w,int h){int width=MeasureSpec.getSize(w),height=MeasureSpec.getSize(h);grid=new DesktopGeometry.Grid(width,height,settings.columns(),settings.rows(),Ui.dp(getContext(),8));setMeasuredDimension(width,height);for(int i=0;i<getChildCount();i++){int col=i%grid.columns,row=i/grid.columns;getChildAt(i).measure(MeasureSpec.makeMeasureSpec(grid.right(col)-grid.left(col),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(grid.bottom(row)-grid.top(row),MeasureSpec.EXACTLY));}}
    protected void onLayout(boolean changed,int l,int t,int r,int b){int cols=grid.columns;for(int i=0;i<getChildCount();i++){View v=getChildAt(i);int col=i%cols,row=i/cols;v.layout(grid.left(col),grid.top(row),grid.right(col),grid.bottom(row));v.setNextFocusLeftId(getChildAt(col==0?i:i-1).getId());v.setNextFocusRightId(getChildAt(col==cols-1||i+1==getChildCount()?i:i+1).getId());v.setNextFocusDownId(getChildAt(Math.min(i+cols,getChildCount()-1)).getId());v.setNextFocusUpId(i>=cols?getChildAt(i-cols).getId():header==null?v.getId():header.getId());}if(geometry!=null&&getChildCount()>=cols){Tile first=(Tile)getChildAt(0),last=(Tile)getChildAt(cols-1);geometry.edges(first.getLeft()+first.iconLeft(),last.getLeft()+last.iconRight());}}
}
