package ru.truetv.launcher;
import java.util.*;
/** Object names shared by file actions; group wording never disguises a mixed selection. */
public final class FileWords {
 private FileWords(){}
 public static String subject(List<String> names,List<Boolean> folders,boolean ru){if(names.isEmpty())return ru?"элементы":"items";if(names.size()!=folders.size())throw new IllegalArgumentException("Different selection sizes");if(names.size()==1)return "«"+names.get(0)+"»";boolean file=false,folder=false;for(boolean dir:folders){folder|=dir;file|=!dir;}return ru?(file&&folder?"файлы и папки":folder?"папки":"файлы"):(file&&folder?"files and folders":folder?"folders":"files");}
 public static boolean onlyFiles(List<Boolean> folders){if(folders.isEmpty())return false;for(boolean dir:folders)if(dir)return false;return true;}
 public static String textName(String value)throws java.io.IOException {FileCore.name(value);return value.toLowerCase(Locale.ROOT).endsWith(".txt")?value:value+".txt";}
}
