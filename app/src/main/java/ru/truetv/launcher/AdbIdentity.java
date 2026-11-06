package ru.truetv.launcher;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;

/** Decode the public RSA parameters from PKCS8, never cast an opaque Android private key to CRT. */
public final class AdbIdentity {
 private AdbIdentity(){}
 public static KeyPair restore(byte[] encoded)throws Exception {
  if(encoded==null||encoded.length>8192)throw new IOException("Invalid ADB identity size");
  Der root=new Der(encoded),pkcs8=root.take(0x30);root.end();BigInteger version=pkcs8.integer();if(!version.equals(BigInteger.ZERO))throw new IOException("Unsupported PKCS8 version");
  Der algorithm=pkcs8.take(0x30);byte[] oid=algorithm.take(6).bytes();if(!Arrays.equals(oid,new byte[]{42,(byte)134,72,(byte)134,(byte)247,13,1,1,1}))throw new IOException("ADB identity is not RSA");if(algorithm.pos<algorithm.end)algorithm.take(5).end();algorithm.end();
  Der octets=pkcs8.take(4),rsa=octets.take(0x30);octets.end();if(!rsa.integer().equals(BigInteger.ZERO))throw new IOException("Unsupported RSA version");BigInteger modulus=rsa.integer(),exponent=rsa.integer();
  if(modulus.bitLength()!=2048||exponent.compareTo(BigInteger.valueOf(3))<0||exponent.bitLength()>31||!exponent.testBit(0))throw new IOException("Invalid ADB RSA parameters");
  KeyFactory factory=KeyFactory.getInstance("RSA");PrivateKey privateKey=factory.generatePrivate(new PKCS8EncodedKeySpec(encoded));PublicKey publicKey=factory.generatePublic(new RSAPublicKeySpec(modulus,exponent));return new KeyPair(publicKey,privateKey);
 }
 private static final class Der {
  final byte[] data;final int end;int pos;
  Der(byte[] data){this(data,0,data.length);}Der(byte[] data,int start,int end){this.data=data;pos=start;this.end=end;}
  Der take(int tag)throws IOException {if(pos+2>end||(data[pos++]&255)!=tag)throw new IOException("Invalid RSA key encoding");int length=data[pos++]&255;if((length&128)!=0){int count=length&127;if(count==0||count>3||pos+count>end)throw new IOException("Invalid DER length");length=0;for(int i=0;i<count;i++)length=(length<<8)|(data[pos++]&255);}if(length<0||length>end-pos)throw new IOException("Truncated RSA key");Der value=new Der(data,pos,pos+length);pos+=length;return value;}
  byte[] bytes(){return Arrays.copyOfRange(data,pos,end);}BigInteger integer()throws IOException {byte[] value=take(2).bytes();if(value.length==0||(value[0]&128)!=0)throw new IOException("Invalid RSA integer");return new BigInteger(value);}
  void end()throws IOException {if(pos!=end)throw new IOException("Unexpected RSA key data");}
 }
}
