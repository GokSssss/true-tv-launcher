package ru.truetv.launcher;

import java.io.*;
import java.util.*;

/** Filesystem operations never invoke a shell or overwrite an existing destination. */
public final class FileCore {
 private FileCore(){}
 public interface Cancel {boolean cancelled();}
 public static final Cancel NEVER=()->false;
 public static String name(String text)throws IOException {if(text==null||text.trim().isEmpty()||text.equals(".")||text.equals("..")||text.length()>255||text.matches("(?s).*[\\\\/\\x00-\\x1f].*"))throw new IOException("Недопустимое имя файла");return text;}
 public static boolean inside(File root,File file)throws IOException {String base=root.getCanonicalPath(),path=file.getCanonicalPath();return path.equals(base)||path.startsWith(base+File.separator);}
 public static File resolve(File root,String relative)throws IOException {if(relative==null||relative.startsWith("/")||Arrays.asList(relative.split("/",-1)).contains(".."))throw new IOException("Недопустимый путь");File f=relative.isEmpty()?root:new File(root,relative);if(!inside(root,f))throw new IOException("Файл вне доступного хранилища");return f.getCanonicalFile();}
 public static void regular(File file)throws IOException {if(!file.getCanonicalFile().equals(file.getAbsoluteFile()))throw new IOException("Символьная ссылка не поддерживается: "+file.getName());}
 private static void check(Cancel cancel)throws IOException {if(Thread.currentThread().isInterrupted()||cancel.cancelled())throw new InterruptedIOException("Операция отменена");}
 public static File unique(File directory,String displayName)throws IOException {name(displayName);String stem=displayName,suffix="";int dot=displayName.lastIndexOf('.');if(dot>0){stem=displayName.substring(0,dot);suffix=displayName.substring(dot);}for(int i=0;i<10000;i++){File f=new File(directory,i==0?displayName:stem+" ("+(i+1)+")"+suffix);if(!f.exists())return f;}throw new IOException("Слишком много совпадений имён");}
 public static synchronized File create(File parent,String displayName,boolean directory)throws IOException {if(!parent.isDirectory())throw new IOException("Папка недоступна");File file=unique(parent,displayName);boolean created=directory?file.mkdir():file.createNewFile();if(!created)throw new IOException("Не удалось создать "+file.getName());return file;}
 public static synchronized File rename(File source,String displayName)throws IOException {regular(source);name(displayName);File target=new File(source.getParentFile(),displayName);if(source.equals(target))return source;if(target.exists())throw new IOException("Такое имя уже занято");if(!source.renameTo(target))throw new IOException("Не удалось переименовать файл");return target;}
 public static List<File> topLevel(Collection<File> files)throws IOException {ArrayList<File> result=new ArrayList<>();for(File f:files){regular(f);boolean nested=false;for(File parent:files)if(!f.equals(parent)&&parent.isDirectory()&&inside(parent,f)){nested=true;break;}if(!nested&&!result.contains(f))result.add(f);}return result;}
 public static synchronized File transfer(File source,File parent,boolean move,Cancel cancel)throws IOException {
  check(cancel);regular(source);if(!source.exists()||!parent.isDirectory())throw new IOException("Источник или папка недоступны");if(source.isDirectory()&&inside(source,parent))throw new IOException("Нельзя копировать папку внутрь неё самой");
  if(move&&source.getParentFile().getCanonicalFile().equals(parent.getCanonicalFile()))return source;
  File target=unique(parent,source.getName());if(move&&source.renameTo(target))return target;
  File temporary=new File(parent,".truetv-part-"+UUID.randomUUID());
  try{copy(source,temporary,cancel);check(cancel);if(target.exists())target=unique(parent,source.getName());if(!temporary.renameTo(target))throw new IOException("Не удалось завершить копирование");}
  catch(IOException|RuntimeException e){try{erase(temporary,NEVER);}catch(Exception cleanup){e.addSuppressed(cleanup);}throw e;}
  // A move never removes its source before the entire destination has been committed.
  if(move)try{erase(source,cancel);}catch(IOException e){throw new IOException("Копия создана, но источник удалён не полностью: "+source.getName(),e);}return target;
 }
 private static void copy(File source,File target,Cancel cancel)throws IOException {check(cancel);regular(source);if(source.isDirectory()){if(!target.mkdir())throw new IOException("Нет доступа для создания папки");File[] children=source.listFiles();if(children==null)throw new IOException("Нет доступа к "+source.getName());for(File child:children)copy(child,new File(target,child.getName()),cancel);}else{long expected=source.length(),copied=0;try(InputStream in=new FileInputStream(source);FileOutputStream out=new FileOutputStream(target)){byte[] buffer=new byte[65536];int n;while((n=in.read(buffer))!=-1){check(cancel);out.write(buffer,0,n);copied+=n;}out.getFD().sync();}if(copied!=expected||source.length()!=expected)throw new IOException("Источник изменился во время копирования");}target.setLastModified(source.lastModified());}
 public static void erase(File file,Cancel cancel)throws IOException {check(cancel);if(!file.exists())return;regular(file);if(file.isDirectory()){File[] children=file.listFiles();if(children==null)throw new IOException("Нет доступа для удаления "+file.getName());for(File child:children)erase(child,cancel);}if(!file.delete())throw new IOException("Не удалось удалить "+file.getName());}
 public static String extension(String name){int at=name.lastIndexOf('.');return at>=0?name.substring(at+1).toLowerCase(Locale.ROOT):"";}
 public static String mime(String name){switch(extension(name)){case "txt":case "log":case "ini":case "cfg":return "text/plain";case "pdf":return "application/pdf";case "apk":return "application/vnd.android.package-archive";case "zip":return "application/zip";case "7z":return "application/x-7z-compressed";case "rar":return "application/vnd.rar";case "json":return "application/json";case "jpg":case "jpeg":return "image/jpeg";case "png":return "image/png";case "webp":return "image/webp";case "gif":return "image/gif";case "mp4":case "m4v":return "video/mp4";case "mkv":return "video/x-matroska";case "avi":return "video/x-msvideo";case "mp3":return "audio/mpeg";case "flac":return "audio/flac";case "wav":return "audio/wav";case "nes":return "application/x-nes-rom";case "sfc":case "smc":return "application/x-snes-rom";case "md":case "gen":case "smd":return "application/x-genesis-rom";default:return "application/octet-stream";}}
 public static boolean matchesMime(String actual,String filter){if(filter==null||filter.equals("*/*"))return true;String a=actual.toLowerCase(Locale.ROOT),f=filter.toLowerCase(Locale.ROOT);return a.equals(f)||f.endsWith("/*")&&a.startsWith(f.substring(0,f.length()-1));}
}
