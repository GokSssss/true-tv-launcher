package ru.truetv.launcher;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.*;
/** Bounded process/output handling, including a user-controlled superuser prompt. */
public final class RootCommand {
 private RootCommand(){}
 public static ShellReply run(String command,long timeout)throws Exception {return execute(new String[]{"su","-c",command},timeout);}
 static ShellReply execute(String[] argv,long timeout)throws Exception {Process process=new ProcessBuilder(argv).redirectErrorStream(true).start();ByteArrayOutputStream output=new ByteArrayOutputStream();AtomicReference<Exception> failure=new AtomicReference<>();Thread reader=new Thread(()->{try(InputStream in=process.getInputStream()){byte[] bytes=new byte[2048];int n;while((n=in.read(bytes))>=0){synchronized(output){if(output.size()+n>65536)throw new IOException("Root output limit");output.write(bytes,0,n);}}}catch(Exception e){failure.set(e);}},"TrueTV-root-output");reader.setDaemon(true);reader.start();long end=System.nanoTime()+timeout*1000000L;try{int code;for(;;){if(failure.get()!=null)throw failure.get();try{code=process.exitValue();break;}catch(IllegalThreadStateException active){if(System.nanoTime()>end)throw new IOException("Root timed out or authorization pending");Thread.sleep(50);}}reader.join(1000);if(reader.isAlive())throw new IOException("Root output did not close");if(failure.get()!=null)throw failure.get();return new ShellReply(code,new String(output.toByteArray(),StandardCharsets.UTF_8).trim());}finally{process.destroy();try{process.getInputStream().close();}catch(IOException ignored){}try{process.getOutputStream().close();}catch(IOException ignored){}}}
}
