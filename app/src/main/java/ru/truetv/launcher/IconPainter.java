package ru.truetv.launcher;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.Build;

/** Preserve legacy artwork; render adaptive layers as a rounded square instead of an OEM circle. */
public final class IconPainter {
 private IconPainter(){}
 public static Bitmap render(Drawable icon,int side){
  Bitmap bitmap=Bitmap.createBitmap(side,side,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(bitmap);Rect previous=new Rect(icon.getBounds());
  try{
   if(Build.VERSION.SDK_INT>=26&&Adaptive.draw(icon,canvas,side))return bitmap;
   int w=icon.getIntrinsicWidth(),h=icon.getIntrinsicHeight();float scale=w>0&&h>0?Math.min((float)side/w,(float)side/h):1;
   int iw=w>0?Math.round(w*scale):side,ih=h>0?Math.round(h*scale):side;
   icon.setBounds((side-iw)/2,(side-ih)/2,(side+iw)/2,(side+ih)/2);icon.draw(canvas);return bitmap;
  }finally{icon.setBounds(previous);}
 }
 private static final class Adaptive {
  static boolean draw(Drawable icon,Canvas canvas,int side){
   if(!(icon instanceof AdaptiveIconDrawable))return false;AdaptiveIconDrawable adaptive=(AdaptiveIconDrawable)icon;adaptive.setBounds(0,0,side,side);
   Path mask=new Path();mask.addRoundRect(new RectF(0,0,side,side),side*.12f,side*.12f,Path.Direction.CW);int save=canvas.save();canvas.clipPath(mask);
   Drawable background=adaptive.getBackground(),foreground=adaptive.getForeground();if(background!=null)background.draw(canvas);if(foreground!=null)foreground.draw(canvas);canvas.restoreToCount(save);return true;
  }
 }
}
