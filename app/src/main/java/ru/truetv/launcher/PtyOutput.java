package ru.truetv.launcher;
import android.system.Os;
import android.system.OsConstants;
import java.io.*;
import java.lang.reflect.*;
/** Line-buffered getevent output on older Android, without a native library or logging raw input. */
final class PtyOutput implements Closeable {
 final FileInputStream input;final String slave;
 PtyOutput()throws Exception{FileDescriptor fd=Os.open("/dev/ptmx",OsConstants.O_RDWR|OsConstants.O_NOCTTY|OsConstants.O_CLOEXEC,0);try{ioctl(fd,request("TIOCSPTLCK",0x40045431,0x80045431),0);int number=ioctl(fd,request("TIOCGPTN",0x80045430,0x40045430),0);if(number<0)throw new IOException("Invalid PTY");slave="/dev/pts/"+number;input=new FileInputStream(fd);}catch(Exception e){Os.close(fd);throw e;}}
 private static int request(String name,int generic,int mips){try{return OsConstants.class.getField(name).getInt(null);}catch(ReflectiveOperationException ignored){return System.getProperty("os.arch","").startsWith("mips")?mips:generic;}}
 private static int ioctl(FileDescriptor fd,int command,int initial)throws Exception{for(Method method:Os.class.getMethods()){Class<?>[] types=method.getParameterTypes();if(!method.getName().equals("ioctlInt")||types.length!=3||types[0]!=FileDescriptor.class||types[1]!=int.class)continue;if(types[2]==int.class)return ((Number)method.invoke(null,fd,command,initial)).intValue();Object value=types[2].getConstructor(int.class).newInstance(initial);method.invoke(null,fd,command,value);return types[2].getField("value").getInt(value);}throw new IOException("PTY ioctl unavailable on this Android");}
 public void close()throws IOException{input.close();}
}
