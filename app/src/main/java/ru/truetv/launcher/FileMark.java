package ru.truetv.launcher;
import android.content.Context;
import android.graphics.*;
import android.view.View;
/** Small original outline file/folder symbols; no bitmap loading blocks TV scrolling. */
public final class FileMark extends View {
 private final Paint paint=new Paint(3);private boolean folder;private String extension="",mime="";
 public FileMark(Context context){super(context);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
 public void bind(boolean directory,String name){folder=directory;extension=FileCore.extension(name).toUpperCase(java.util.Locale.ROOT);mime=FileCore.mime(name);invalidate();}
 protected void onDraw(Canvas canvas){super.onDraw(canvas);int save=canvas.save();float scale=Math.min(getWidth()/48f,getHeight()/48f);canvas.translate((getWidth()-48*scale)/2,(getHeight()-48*scale)/2);canvas.scale(scale,scale);paint.setStrokeWidth(1.8f);paint.setStrokeJoin(Paint.Join.ROUND);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStyle(Paint.Style.STROKE);paint.setColor(folder?0xffd2b779:mime.startsWith("image/")?0xff8abca4:mime.startsWith("audio/")||mime.startsWith("video/")?0xffa8a2cd:0xff9db9ce);Path outline=new Path();if(folder){outline.moveTo(6,14);outline.lineTo(20,14);outline.lineTo(24,19);outline.lineTo(42,19);outline.lineTo(42,36);outline.lineTo(6,36);outline.close();canvas.drawPath(outline,paint);canvas.drawLine(7,23,41,23,paint);}else{outline.moveTo(11,7);outline.lineTo(29,7);outline.lineTo(38,16);outline.lineTo(38,41);outline.lineTo(11,41);outline.close();canvas.drawPath(outline,paint);canvas.drawLine(29,7,29,16,paint);canvas.drawLine(29,16,38,16,paint);paint.setStyle(Paint.Style.FILL);paint.setTextAlign(Paint.Align.CENTER);paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));paint.setTextSize(8);String label=extension.isEmpty()?"FILE":extension;canvas.drawText(label.length()>4?label.substring(0,4):label,24.5f,31,paint);}canvas.restoreToCount(save);}
}
