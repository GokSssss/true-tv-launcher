package ru.truetv.launcher;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** Original vector glyph as a text-button compound icon, matching the popup icons. */
public final class GlyphDrawable extends Drawable {
 private final Glyph glyph;private final int size;private int opacity=255;
 public GlyphDrawable(Context c,String kind){glyph=new Glyph(c,kind);size=Ui.dp(c,22);}
 public int getIntrinsicWidth(){return size;}public int getIntrinsicHeight(){return size;}
 public void draw(Canvas canvas){Rect bounds=getBounds();int save=canvas.save();canvas.translate(bounds.left,bounds.top);if(opacity<255)canvas.saveLayerAlpha(0,0,bounds.width(),bounds.height(),opacity);glyph.layout(0,0,bounds.width(),bounds.height());glyph.draw(canvas);canvas.restoreToCount(save);}
 public void setAlpha(int alpha){opacity=alpha;invalidateSelf();}public void setColorFilter(ColorFilter ignored){}public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
