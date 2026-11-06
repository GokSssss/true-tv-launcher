package ru.truetv.launcher;
import java.security.*;
import java.security.interfaces.*;
import java.util.*;

public final class DesktopCheck {
 private static int checks;
 private static void check(boolean ok,String name){checks++;if(!ok)throw new AssertionError(name);}
 public static void main(String[] args)throws Exception {
  for(int[] screen:new int[][]{{912,400},{1872,920},{3792,1900},{360,640},{1,1},{0,0}})for(int cols=2;cols<=7;cols++)for(int rows=1;rows<=5;rows++){
   DesktopGeometry.Grid g=new DesktopGeometry.Grid(screen[0],screen[1],cols,rows,8);
   check(g.left(0)==0&&g.right(cols-1)==screen[0],"both horizontal edges filled");check(g.top(0)==0&&g.bottom(rows-1)==screen[1],"both vertical edges filled");
   for(int col=0;col<cols;col++){check(g.right(col)>=g.left(col),"nonnegative slot");if(col>0)check(g.left(col)-g.right(col-1)==g.gapX,"uniform column gaps");}
   for(int row=0;row<rows;row++)for(int fixed:new int[]{0,32,64,160})for(int caption:new int[]{0,34}){
    int w=g.right(0)-g.left(0),h=g.bottom(row)-g.top(row);DesktopGeometry.TileBox t=new DesktopGeometry.TileBox(w,h,8,caption,fixed);
    check(t.left>=0&&t.top>=0&&t.left+t.width<=w&&t.top+t.height<=h,"frame stays in slot");check(t.iconLeft-t.left==t.padding&&t.width==t.iconSize+t.padding*2,"constant frame inset");check(t.height==t.iconSize+t.padding*2+t.caption,"label reserved under icon");if(fixed>0)check(t.iconSize<=fixed,"icon setting respected");
   }
  }
  DesktopGeometry.Grid regression=new DesktopGeometry.Grid(912,400,5,4,8);check(regression.left(0)==0&&regression.right(4)==912,"5x4 no longer squeezed into500px");check(regression.left(4)>700,"last column near right edge");
  KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);KeyPair original=generator.generateKeyPair();KeyPair restored=AdbIdentity.restore(original.getPrivate().getEncoded());
  check(Arrays.equals(original.getPublic().getEncoded(),restored.getPublic().getEncoded()),"existing RSA public identity preserved without private CRT cast");Signature signer=Signature.getInstance("SHA256withRSA");signer.initSign(restored.getPrivate());signer.update(new byte[]{1,2,3});byte[] signed=signer.sign();signer.initVerify(original.getPublic());signer.update(new byte[]{1,2,3});check(signer.verify(signed),"restored private key still signs for existing public key");
  for(byte[] bad:new byte[][]{new byte[0],new byte[]{48,1,0},Arrays.copyOf(original.getPrivate().getEncoded(),40),new byte[8193]}){boolean failed=false;try{AdbIdentity.restore(bad);}catch(Exception expected){failed=true;}check(failed,"bad identity rejected without silent rotation");}
  System.out.println("PASS "+checks+" desktop geometry and persisted RSA checks (180 grid configurations)");
 }
}
