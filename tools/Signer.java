import com.android.apksig.ApkSigner;
import com.android.apksig.ApkVerifier;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
public final class Signer {
 public static void main(String[] args)throws Exception {
  if(args[0].equals("verify")){ApkVerifier.Result r=new ApkVerifier.Builder(new File(args[1])).build().verify();if(!r.isVerified())throw new Exception("APK verification failed: "+r.getErrors());byte[] cert=r.getSignerCertificates().get(0).getEncoded();StringBuilder hash=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(cert))hash.append(String.format("%02x",b));System.out.println("Verified v1="+r.isVerifiedUsingV1Scheme()+" v2="+r.isVerifiedUsingV2Scheme()+" certSHA256="+hash);return;}
  char[] pass=new String(Files.readAllBytes(Paths.get(args[2])),"UTF-8").trim().toCharArray();KeyStore store=KeyStore.getInstance("PKCS12");try(InputStream in=new FileInputStream(args[1])){store.load(in,pass);}String alias=store.aliases().nextElement();PrivateKey key=(PrivateKey)store.getKey(alias,pass);List<X509Certificate> chain=new ArrayList<>();for(java.security.cert.Certificate cert:store.getCertificateChain(alias))chain.add((X509Certificate)cert);
  ApkSigner.SignerConfig signer=new ApkSigner.SignerConfig.Builder("truetv",key,chain).build();new ApkSigner.Builder(Collections.singletonList(signer)).setInputApk(new File(args[3])).setOutputApk(new File(args[4])).setV1SigningEnabled(true).setV2SigningEnabled(true).setV3SigningEnabled(true).setMinSdkVersion(21).build().sign();Arrays.fill(pass,'\0');
 }
}
