package ru.truetv.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** Original geometric glyph renderer; no bundled icon/font files. */
public final class Glyph extends View {
    public final String kind;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    public Glyph(Context context, String name) { super(context); kind = name; setFocusable(false); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); }
    private void line(Canvas c, float x, float y, float ex, float ey) { c.drawLine(x,y,ex,ey,p); }
    private void arc(Canvas c, float start, float sweep) { c.drawArc(new RectF(4,4,20,20), start, sweep, false, p); }
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); Canvas c = canvas; c.save(); float scale = Math.min(getWidth(), getHeight())/24f;
        c.translate((getWidth()-24*scale)/2,(getHeight()-24*scale)/2); c.scale(scale,scale);
        p.setColor(Ui.ink(getContext())); p.setStrokeWidth(1.7f); p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND);
        if (kind.equals("apps")) { p.setStyle(Paint.Style.FILL); for(int y=4;y<=16;y+=6) for(int x=4;x<=16;x+=6)c.drawRect(x,y,x+3,y+3,p); }
        else if(kind.equals("wifi_folder")){
            // The whole composition shares the file-manager folder bounds: x3..21, y6..21.
            Path f=new Path();f.moveTo(3,6);f.lineTo(9,6);f.lineTo(11,9);f.lineTo(15,9);f.moveTo(21,12);f.lineTo(21,21);f.lineTo(3,21);f.lineTo(3,6);c.drawPath(f,p);
            line(c,19,6,19,10);line(c,17,8,21,8);
            c.drawArc(new RectF(6,11,18,23),225,90,false,p);c.drawArc(new RectF(8.5f,13.5f,15.5f,20.5f),225,90,false,p);p.setStyle(Paint.Style.FILL);c.drawCircle(12,17,0.8f,p);
        }
        else if (kind.equals("tune")) { for(int y=6;y<=18;y+=6){line(c,3,y,21,y);p.setStyle(Paint.Style.FILL);c.drawCircle(y==12?16:8,y,2.7f,p);p.setStyle(Paint.Style.STROKE);} }
        else if (kind.equals("settings")) { c.drawCircle(12,12,3.5f,p); Path path=new Path();for(int i=0;i<32;i++){double angle=i*Math.PI/16;float radius=(i%4==1||i%4==2)?9.5f:8f;float x=12+(float)Math.cos(angle)*radius,y=12+(float)Math.sin(angle)*radius;if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}path.close();c.drawPath(path,p); }
        else if (kind.equals("bluetooth")) {line(c,11,2,11,22);line(c,11,2,18,8);line(c,18,8,5,19);line(c,11,22,18,16);line(c,18,16,5,5);}
        else if(kind.equals("power")||kind.equals("close")){arc(c,-48,276);line(c,12,2,12,12);}
        else if(kind.equals("restart")||kind.equals("reset")){arc(c,45,270);float endX=12+8*(float)Math.cos(Math.PI/4),endY=12-8*(float)Math.sin(Math.PI/4),tipY=endY+20-endX;line(c,endX,endY,20,tipY);line(c,14,tipY,20,tipY);line(c,20,tipY,20,tipY-6);}
        else if(kind.equals("sleep")){Path moon=new Path();moon.moveTo(17,3);moon.cubicTo(3,1,1,18,12,21);moon.cubicTo(17,22,21,18,21,15);moon.cubicTo(12,18,9,9,17,3);c.drawPath(moon,p);}
        else if(kind.equals("check")){line(c,4,12,9,18);line(c,9,18,21,5);}
        else if(kind.equals("plus")){line(c,12,4,12,20);line(c,4,12,20,12);}
        else if(kind.equals("cancel")){line(c,6,6,18,18);line(c,6,18,18,6);}
        else if(kind.equals("move")){line(c,12,3,12,21);line(c,3,12,21,12);for(int a=0;a<4;a++){c.save();c.rotate(a*90,12,12);line(c,9.5f,5.5f,12,3);line(c,12,3,14.5f,5.5f);c.restore();}}
        else if(kind.equals("remove")){Path h=new Path();h.moveTo(3,11);h.lineTo(12,3);h.lineTo(21,11);h.moveTo(5,9);h.lineTo(5,21);h.lineTo(19,21);h.lineTo(19,9);c.drawPath(h,p);line(c,8,12,16,19);line(c,16,12,8,19);}
        else if(kind.equals("replace")){line(c,4,7,20,7);line(c,16,3,20,7);line(c,20,7,16,11);line(c,20,17,4,17);line(c,8,13,4,17);line(c,4,17,8,21);}
        else if(kind.equals("sort")){line(c,4,5,20,5);line(c,4,11,16,11);line(c,4,17,12,17);line(c,20,10,20,22);line(c,17,19,20,22);line(c,20,22,23,19);}
        else if(kind.equals("back")){line(c,20,12,4,12);line(c,4,12,11,5);line(c,4,12,11,19);}
        else if(kind.equals("document")){Path f=new Path();f.moveTo(5,2);f.lineTo(14,2);f.lineTo(20,8);f.lineTo(20,22);f.lineTo(5,22);f.close();c.drawPath(f,p);line(c,14,2,14,8);line(c,14,8,20,8);line(c,9,13,16,13);line(c,9,17,16,17);}
        else if(kind.equals("folder")){Path f=new Path();f.moveTo(3,6);f.lineTo(10,6);f.lineTo(12,9);f.lineTo(21,9);f.lineTo(21,21);f.lineTo(3,21);f.close();c.drawPath(f,p);}
        else if(kind.equals("copy")){c.drawRect(8,7,21,21,p);line(c,4,17,4,3);line(c,4,3,17,3);}
        else if(kind.equals("cut")){c.drawCircle(6,18,3,p);c.drawCircle(18,18,3,p);line(c,5,3,18,15);line(c,19,3,6,15);}
        else if(kind.equals("paste")){c.drawRect(5,5,19,22,p);c.drawRect(9,2,15,7,p);}
        else if(kind.equals("share")){line(c,4,17,4,21);line(c,4,21,20,21);line(c,20,21,20,17);line(c,12,3,12,16);line(c,8,7,12,3);line(c,12,3,16,7);}
        else if(kind.equals("rename")){line(c,4,20,9,19);line(c,9,19,21,7);line(c,21,7,17,3);line(c,17,3,5,15);line(c,5,15,4,20);}
        else if(kind.equals("delete")){c.drawRect(6,7,18,21,p);line(c,4,6,20,6);line(c,9,3,15,3);line(c,10,11,10,17);line(c,14,11,14,17);}
        else if(kind.equals("market")){c.drawRect(5,8,19,21,p);c.drawArc(new RectF(8,2,16,12),180,180,false,p);line(c,12,11,12,18);line(c,9,15,12,18);line(c,12,18,15,15);}
        else if(kind.equals("access")){c.drawCircle(12,4,2,p);line(c,4,9,12,11);line(c,12,11,20,9);line(c,12,11,12,16);line(c,12,16,7,22);line(c,12,16,17,22);}
        else {c.drawCircle(12,12,9,p);line(c,12,11,12,18);c.drawPoint(12,6,p);}
        c.restore();
    }
}
