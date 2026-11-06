package ru.truetv.bridge;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.util.*;
import javax.net.ssl.*;

/** App-private transport identity; public self-signed leafs use explicit approval/TOFU pins. */
public final class Identity {
 public final X509Certificate certificate;private final PrivateKey key;
 private Identity(PrivateKey k,X509Certificate c){key=k;certificate=c;}
 public static synchronized Identity load(File folder)throws Exception {
  if(!folder.isDirectory()&&!folder.mkdirs())throw new IOException("Identity storage unavailable");File file=new File(folder,"transport.identity");
  if(file.isFile()){try(DataInputStream in=new DataInputStream(new ByteArrayInputStream(read(file,16384)))){int n=in.readInt();if(n<1||n>8192)throw new IOException("Invalid identity");byte[] kb=new byte[n];in.readFully(kb);int len=in.readInt();if(len<1||len>8192)throw new IOException("Invalid certificate");byte[] cb=new byte[len];in.readFully(cb);if(in.read()!=-1)throw new IOException("Invalid identity suffix");PrivateKey key=KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(kb));X509Certificate cert=decode(cb);byte[] test=Wire.random();Signature verify=Signature.getInstance("SHA256withRSA");verify.initSign(key);verify.update(test);byte[] signed=verify.sign();verify.initVerify(cert.getPublicKey());verify.update(test);if(!verify.verify(signed))throw new IOException("Transport identity mismatch");return new Identity(key,cert);}}
  KeyPairGenerator g=KeyPairGenerator.getInstance("RSA");g.initialize(2048);KeyPair p=g.generateKeyPair();byte[] alg=seq(oid(new int[]{1,2,840,113549,1,1,11}),der(5,new byte[0]));byte[] cn=seq(set(seq(oid(new int[]{2,5,4,3}),der(12,"To TV".getBytes("UTF-8")))));
  byte[] tbs=seq(der(0xa0,integer(BigInteger.valueOf(2))),integer(new BigInteger(127,new SecureRandom()).add(BigInteger.ONE)),alg,cn,seq(der(24,"19700101000000Z".getBytes("US-ASCII")),der(24,"20991231235959Z".getBytes("US-ASCII"))),cn,p.getPublic().getEncoded());Signature sign=Signature.getInstance("SHA256withRSA");sign.initSign(p.getPrivate());sign.update(tbs);byte[] value=seq(tbs,alg,der(3,join(new byte[]{0},sign.sign())));
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);byte[] kb=p.getPrivate().getEncoded();out.writeInt(kb.length);out.write(kb);out.writeInt(value.length);out.write(value);out.flush();File stage=new File(folder,"identity-"+UUID.randomUUID()+".tmp");write(stage,bytes.toByteArray());if(!stage.renameTo(file)){stage.delete();throw new IOException("Cannot commit transport identity");}return new Identity(p.getPrivate(),decode(value));
 }
 public String fingerprint()throws CertificateEncodingException{return Wire.hex(Wire.digest(certificate.getEncoded()));}
 public SSLContext tls(String pin)throws Exception {
  X509KeyManager keys=new X509KeyManager(){public String[] getClientAliases(String type,Principal[] issuers){return "RSA".equals(type)?new String[]{"peer"}:null;}public String chooseClientAlias(String[] types,Principal[] issuers,java.net.Socket s){for(String t:types)if("RSA".equals(t))return "peer";return null;}public String[] getServerAliases(String type,Principal[] issuers){return "RSA".equals(type)?new String[]{"peer"}:null;}public String chooseServerAlias(String type,Principal[] issuers,java.net.Socket s){return "RSA".equals(type)?"peer":null;}public X509Certificate[] getCertificateChain(String alias){return new X509Certificate[]{certificate};}public PrivateKey getPrivateKey(String alias){return key;}};
  X509TrustManager trust=new X509TrustManager(){public X509Certificate[] getAcceptedIssuers(){return new X509Certificate[0];}public void checkClientTrusted(X509Certificate[] chain,String auth)throws CertificateException {check(chain);}public void checkServerTrusted(X509Certificate[] chain,String auth)throws CertificateException {check(chain);}private void check(X509Certificate[] chain)throws CertificateException {try{if(chain==null||chain.length!=1)throw new CertificateException("Expected device certificate");X509Certificate c=chain[0];c.checkValidity();c.verify(c.getPublicKey());if(!(c.getPublicKey() instanceof java.security.interfaces.RSAPublicKey)||((java.security.interfaces.RSAPublicKey)c.getPublicKey()).getModulus().bitLength()<2048)throw new CertificateException("Unsupported identity");if(pin!=null&&!MessageDigest.isEqual(pin.getBytes("US-ASCII"),Wire.hex(Wire.digest(c.getEncoded())).getBytes("US-ASCII")))throw new CertificateException("Device key changed");}catch(CertificateException e){throw e;}catch(Exception e){throw new CertificateException(e);}}};
  SSLContext ctx=SSLContext.getInstance("TLSv1.2");ctx.init(new KeyManager[]{keys},new TrustManager[]{trust},new SecureRandom());return ctx;
 }
 public static byte[] peer(SSLSocket socket)throws Exception{return socket.getSession().getPeerCertificates()[0].getEncoded();}
 private static X509Certificate decode(byte[] b)throws CertificateException{return (X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(b));}
 private static byte[] read(File f,int cap)throws IOException {try(InputStream in=new FileInputStream(f);ByteArrayOutputStream o=new ByteArrayOutputStream()){byte[] b=new byte[2048];int n;while((n=in.read(b))!=-1){if(o.size()+n>cap)throw new IOException("Identity too large");o.write(b,0,n);}return o.toByteArray();}}
 private static void write(File f,byte[] b)throws IOException {if(!f.createNewFile())throw new IOException("Identity already exists");f.setReadable(false,false);f.setWritable(false,false);f.setReadable(true,true);f.setWritable(true,true);try(FileOutputStream o=new FileOutputStream(f)){o.write(b);o.getFD().sync();}catch(IOException e){f.delete();throw e;}}
 private static byte[] join(byte[]... bs){ByteArrayOutputStream o=new ByteArrayOutputStream();for(byte[] b:bs)o.write(b,0,b.length);return o.toByteArray();}
 private static byte[] der(int tag,byte[] b){ByteArrayOutputStream o=new ByteArrayOutputStream();o.write(tag);if(b.length<128)o.write(b.length);else{int n=b.length,count=0;while(n>0){count++;n>>>=8;}o.write(128|count);for(int i=count-1;i>=0;i--)o.write(b.length>>>(i*8));}o.write(b,0,b.length);return o.toByteArray();}
 private static byte[] seq(byte[]... b){return der(48,join(b));}private static byte[] set(byte[] b){return der(49,b);}private static byte[] integer(BigInteger i){return der(2,i.toByteArray());}
 private static byte[] oid(int[] ids){ByteArrayOutputStream o=new ByteArrayOutputStream();o.write(ids[0]*40+ids[1]);for(int i=2;i<ids.length;i++){int n=ids[i],count=1;while((n>>>(count*7))>0)count++;for(int j=count-1;j>=0;j--)o.write(((n>>>(j*7))&127)|(j==0?0:128));}return der(6,o.toByteArray());}
}
