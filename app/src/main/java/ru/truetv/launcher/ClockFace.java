package ru.truetv.launcher;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** System typography or our own seven-segment canvas. No external font asset or font download. */
public final class ClockFace extends View {
    private String text="",style="medium"; private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final float size;private final boolean date;
    public ClockFace(Context c,int sp,boolean date){super(c);size=sp*c.getResources().getDisplayMetrics().scaledDensity;this.date=date;setFocusable(false);}
    public void content(String text,String style){if(!this.text.equals(text)||!this.style.equals(style)){this.text=text;this.style=style;setContentDescription(text);requestLayout();invalidate();}}
    private void setup(){paint.setColor(Ui.ink(getContext()));paint.setTextSize(size);paint.setTypeface(Typeface.create(style.equals("condensed")?"sans-serif-condensed":"sans-serif-medium",Typeface.NORMAL));paint.setFontFeatureSettings("'tnum' 1");}
    private float glyph(char ch){return style.equals("digital")&&Character.isDigit(ch)?size*.64f:paint.measureText(String.valueOf(ch));}
    public int naturalWidth(){setup();float width=0;for(char ch:text.toCharArray())width+=glyph(ch);return (int)Math.ceil(width);}
    protected void onMeasure(int w,int h){setMeasuredDimension(resolveSize(naturalWidth(),w),resolveSize((int)Math.ceil(size*1.35f),h));}
    protected void onDraw(Canvas c){setup();float baseline=(getHeight()-paint.ascent()-paint.descent())/2;float total=0;for(char ch:text.toCharArray())total+=glyph(ch);float x=Math.max(0,getWidth()-total);c.save();c.clipRect(0,0,getWidth(),getHeight());
        for(char ch:text.toCharArray()){float advance=glyph(ch);if(style.equals("digital")&&ch>='0'&&ch<='9')digit(c,ch-'0',x,(getHeight()-size)/2,advance*.87f,size);else {paint.setStyle(Paint.Style.FILL);c.drawText(String.valueOf(ch),x,baseline,paint);}x+=advance;}c.restore();}
    private void digit(Canvas c,int n,float x,float y,float width,float height){int[] bits={0b1111110,0b0110000,0b1101101,0b1111001,0b0110011,0b1011011,0b1011111,0b1110000,0b1111111,0b1111011};float[][] points={{.12f,0,.88f,0},{1,.08f,1,.44f},{1,.56f,1,.92f},{.12f,1,.88f,1},{0,.56f,0,.92f},{0,.08f,0,.44f},{.12f,.5f,.88f,.5f}};
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(1,size*.075f));paint.setStrokeCap(Paint.Cap.SQUARE);for(int i=0;i<7;i++)if((bits[n]&(1<<(6-i)))!=0){float[] a=points[i];c.drawLine(x+a[0]*width,y+a[1]*height,x+a[2]*width,y+a[3]*height,paint);}paint.setStyle(Paint.Style.FILL);}
}
