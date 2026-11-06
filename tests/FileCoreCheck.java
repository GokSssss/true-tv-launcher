package ru.truetv.launcher;
import java.io.*;
import java.nio.file.*;
import java.util.*;
public final class FileCoreCheck {
 private static int count;private static void ok(boolean b,String why){count++;if(!b)throw new AssertionError(why);}
 interface Risk {void run()throws Exception;}private static void reject(Risk r)throws Exception {try{r.run();throw new AssertionError("Unexpected success");}catch(IOException|IllegalArgumentException expected){count++;}}
 public static void main(String[] args)throws Exception {File base=Files.createTempDirectory("truetv-files-").toFile();try{File src=new File(base,"source"),dest=new File(base,"dest");src.mkdir();dest.mkdir();File file=new File(src,"файл.txt");byte[] bytes="test bytes\nРусский текст".getBytes("UTF-8");Files.write(file.toPath(),bytes);
 File copy=FileCore.transfer(file,dest,false,FileCore.NEVER);ok(Arrays.equals(bytes,Files.readAllBytes(copy.toPath())),"copy contents");ok(file.exists(),"copy source retained");File second=FileCore.transfer(file,dest,false,FileCore.NEVER);ok(!copy.equals(second)&&copy.exists(),"collision never overwrites original");
 File nested=FileCore.create(src,"nested",true);FileCore.create(nested,"child",true);Files.write(new File(nested,"child/data.bin").toPath(),new byte[]{1,2,3});File tree=FileCore.transfer(nested,dest,false,FileCore.NEVER);ok(new File(tree,"child/data.bin").length()==3,"recursive copy");reject(()->FileCore.transfer(nested,new File(nested,"child"),false,FileCore.NEVER));ok(nested.exists(),"rejected recursive destination preserves source");
 File moved=FileCore.transfer(file,dest,true,FileCore.NEVER);ok(!file.exists()&&moved.exists(),"move commits destination before removing source");ok(FileCore.transfer(moved,dest,true,FileCore.NEVER).equals(moved),"move within same folder no-op");String before=moved.getName();reject(()->FileCore.rename(moved,copy.getName()));ok(moved.exists(),"failed rename preserves file");File renamed=FileCore.rename(moved,"новое имя.txt");ok(renamed.exists()&&!moved.exists(),"rename valid unicode");
 for(String bad:new String[]{"","..",".","../escape","x/y","x\\y","x\n"})reject(()->FileCore.name(bad));reject(()->FileCore.resolve(src,"../dest"));ok(!FileCore.inside(src,new File(base,"source2")),"containment boundary");
 File link=new File(src,"link");Files.createSymbolicLink(link.toPath(),dest.toPath());reject(()->FileCore.transfer(link,dest,false,FileCore.NEVER));reject(()->FileCore.resolve(src,"link/файл.txt"));
 File cancel=new File(src,"cancel.bin");Files.write(cancel.toPath(),new byte[100000]);reject(()->FileCore.transfer(cancel,dest,false,()->true));ok(cancel.exists()&&!new File(dest,"cancel.bin").exists(),"cancel preserves source and no visible partial file");
 final int[] ticks={0};reject(()->FileCore.transfer(cancel,dest,false,()->++ticks[0]>=3));ok(cancel.exists()&&!new File(dest,"cancel.bin").exists(),"mid-copy cancellation has no published partial file");ok(Arrays.stream(dest.listFiles()).noneMatch(f->f.getName().startsWith(".truetv-part-")),"cancel removes staging files");
 File unsafe=FileCore.create(src,"unsafe",true);Files.createSymbolicLink(new File(unsafe,"outside").toPath(),dest.toPath());reject(()->FileCore.transfer(unsafe,dest,false,FileCore.NEVER));ok(!new File(dest,"unsafe").exists(),"nested symlink rejected without published tree");
 ok(FileCore.topLevel(Arrays.asList(nested,new File(nested,"child/data.bin"))).size()==1,"nested selection deduplicated");ok(FileCore.matchesMime("application/zip","application/*"),"MIME family");ok(!FileCore.matchesMime("text/plain","image/*"),"MIME filters respected");ok(FileCore.mime("FILE.PDF").equals("application/pdf"),"case-insensitive MIME");
 for(String good:new String[]{"/system/app/YouTube/YouTube.apk","/product/priv-app/Example/base.apk","/system/priv-app/One.apk"})ok(PackagePolicy.factoryPath(good),"allowed exact factory APK");for(String bad:new String[]{"/system","/system/app/../x.apk","/data/app/base.apk","/system/app/X/x.apk;reboot","/system/framework/framework-res.apk"})ok(!PackagePolicy.factoryPath(bad),"dangerous factory path rejected");ok(PackagePolicy.users("Users:\n UserInfo{0:Owner:13}\n UserInfo{10:Other:10}").equals(Arrays.asList(0,10)),"all profile IDs");reject(()->PackagePolicy.users("bad"));reject(()->PackagePolicy.deleteApk("/system"));
 ok(FileWords.subject(Arrays.asList("a.txt"),Arrays.asList(false),true).equals("«a.txt»"),"single action names exact file");
 ok(FileWords.subject(Arrays.asList("a","b"),Arrays.asList(false,false),true).equals("файлы"),"files-only action wording");
 ok(FileWords.subject(Arrays.asList("a","b"),Arrays.asList(true,true),true).equals("папки"),"folders-only action wording");
 ok(FileWords.subject(Arrays.asList("a","b"),Arrays.asList(true,false),true).equals("файлы и папки"),"mixed action wording");
 ok(FileWords.onlyFiles(Arrays.asList(false,false)),"share permits files-only group");ok(!FileWords.onlyFiles(Arrays.asList(false,true))&&!FileWords.onlyFiles(Collections.emptyList()),"share rejects folders and empty group");
 ok(FileWords.textName("заметки").equals("заметки.txt")&&FileWords.textName("a.TXT").equals("a.TXT"),"new text document has txt extension");reject(()->FileWords.textName("../escape"));

 File recent=new File(src,"aaa-new.txt"),old=new File(src,"zzz-old.txt"),middle=new File(src,"middle-folder");recent.createNewFile();old.createNewFile();middle.mkdir();
 ok(recent.setLastModified(300000)&&old.setLastModified(100000)&&middle.setLastModified(200000),"date fixtures stored");
 List<File> order=new ArrayList<>(Arrays.asList(old,recent,middle));order.sort((a,b)->FileOrder.compare(a,b,0));ok(order.equals(Arrays.asList(recent,middle,old)),"newest first mixes files/folders by modified time");
 order.sort((a,b)->FileOrder.compare(a,b,1));ok(order.equals(Arrays.asList(old,middle,recent)),"oldest first reverses date order");
 order.sort((a,b)->FileOrder.compare(a,b,2));ok(order.equals(Arrays.asList(recent,middle,old)),"name ascending");order.sort((a,b)->FileOrder.compare(a,b,3));ok(order.equals(Arrays.asList(old,middle,recent)),"name descending");
 ok(old.setLastModified(300000),"equal timestamp fixture");ok(FileOrder.compare(recent,old,0)<0&&FileOrder.compare(old,recent,0)>0,"equal dates use deterministic name tie break");
 ok(FileOrder.compare(recent,recent,0)==0&&FileOrder.compare(old,old,3)==0,"identical entry compares equal");ok(FileOrder.valid(-1)==0&&FileOrder.valid(4)==0&&FileOrder.valid(3)==3,"unknown preference falls back to newest");
 File unknown=new File(src,"unavailable");ok(FileOrder.compare(unknown,recent,0)>0,"missing timestamp sorts last in default order");
 System.out.println("PASS "+count+" filesystem/MIME/expert path checks");}finally{Files.walk(base.toPath()).sorted(Comparator.reverseOrder()).forEach(p->{try{Files.delete(p);}catch(IOException e){throw new RuntimeException(e);}});}}
}
