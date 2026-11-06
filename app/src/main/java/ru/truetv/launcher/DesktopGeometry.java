package ru.truetv.launcher;

/** Independent screen axes: square artwork must never compress the entire desktop. */
public final class DesktopGeometry {
 private DesktopGeometry(){}
 public static final class Grid {
  public final int width,height,columns,rows,gapX,gapY;
  private final int usableWidth,usableHeight;
  public Grid(int width,int height,int columns,int rows,int gap){
   this.width=Math.max(0,width);this.height=Math.max(0,height);this.columns=Math.max(1,columns);this.rows=Math.max(1,rows);
   gapX=Math.min(Math.max(0,gap),this.width/Math.max(1,this.columns*2-1));gapY=Math.min(Math.max(0,gap),this.height/Math.max(1,this.rows*2-1));
   usableWidth=this.width-gapX*(this.columns-1);usableHeight=this.height-gapY*(this.rows-1);
  }
  public int left(int col){return (int)((long)usableWidth*col/columns)+gapX*col;}
  public int right(int col){return (int)((long)usableWidth*(col+1)/columns)+gapX*col;}
  public int top(int row){return (int)((long)usableHeight*row/rows)+gapY*row;}
  public int bottom(int row){return (int)((long)usableHeight*(row+1)/rows)+gapY*row;}
 }
 public static final class TileBox {
  public final int left,top,width,height,iconLeft,iconTop,iconSize,caption,padding;
  public TileBox(int width,int height,int padding,int caption,int fixedIcon){
   width=Math.max(0,width);height=Math.max(0,height);this.padding=Math.min(Math.max(0,padding),Math.min(width,height)/4);
   this.caption=Math.min(Math.max(0,caption),Math.max(0,height-this.padding*2-1));
   int limit=Math.max(0,Math.min(width-this.padding*2,height-this.padding*2-this.caption));
   iconSize=fixedIcon>0?Math.min(limit,fixedIcon):limit;
   this.width=iconSize+this.padding*2;this.height=iconSize+this.padding*2+this.caption;
   left=(width-this.width)/2;top=(height-this.height)/2;iconLeft=left+this.padding;iconTop=top+this.padding;
  }
 }
}
