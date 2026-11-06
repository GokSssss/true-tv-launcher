package ru.truetv.launcher;
import java.util.*;
public final class RestrictedCheck {
 private static int count;private static void check(boolean ok,String label){count++;if(!ok)throw new AssertionError(label);}
 interface Job {void run()throws Exception;}private static void reject(Job job)throws Exception {try{job.run();throw new AssertionError("Unexpected success");}catch(java.io.IOException|IllegalArgumentException expected){count++;}}
 public static void main(String[] args)throws Exception {
  for(String text:new String[]{"ACCESS_RESTRICTED_SETTINGS: allow","ACCESS_RESTRICTED_SETTINGS: allow; time=+2s","ACCESS_RESTRICTED_SETTINGS: default\nDefault mode: allow","No operations.\nDefault mode: allow"})check(RestrictedOps.allowed(text),"effective allow");
  for(String text:new String[]{"","No operations.","ACCESS_RESTRICTED_SETTINGS: ignore","ACCESS_RESTRICTED_SETTINGS: deny\nDefault mode: allow","ACCESS_RESTRICTED_SETTINGS: disallow","OTHER: allow","ACCESS_RESTRICTED_SETTINGS: allow\nACCESS_RESTRICTED_SETTINGS: deny"})check(!RestrictedOps.allowed(text),"unconfirmed mode rejected");
  check(RestrictedOps.command("ru.test.access",10,true).equals("cmd appops set --user 10 ru.test.access ACCESS_RESTRICTED_SETTINGS allow"),"exact scoped mutation");check(RestrictedOps.command("ru.test.access",0,false).equals("cmd appops get --user 0 ru.test.access ACCESS_RESTRICTED_SETTINGS"),"exact readback");
  for(String pkg:new String[]{"bad;reboot","ru.test a","../pkg","android","ru.test\nother"})reject(()->RestrictedOps.command(pkg,0,true));reject(()->RestrictedOps.command("ru.test.access",-1,true));
  List<String> calls=new ArrayList<>();RestrictedOps.grant(cmd->{calls.add(cmd);return new ShellReply(0,calls.size()==1?"No operations.":cmd.contains(" set ")?"":"ACCESS_RESTRICTED_SETTINGS: allow");},"ru.test.access",0,33);check(calls.size()==3&&!calls.get(0).contains(" set ")&&calls.get(1).contains(" set ")&&!calls.get(2).contains(" set "),"query-mutate-verify sequence");
  calls.clear();RestrictedOps.grant(cmd->{calls.add(cmd);return new ShellReply(0,"ACCESS_RESTRICTED_SETTINGS: allow");},"ru.test.access",0,35);check(calls.size()==1,"already allowed does not mutate");
  calls.clear();reject(()->RestrictedOps.grant(cmd->{calls.add(cmd);return new ShellReply(0,"");},"ru.test.access",0,28));check(calls.isEmpty(),"Android below13 emits no command");
  calls.clear();reject(()->RestrictedOps.grant(cmd->{calls.add(cmd);return new ShellReply(1,"not supported");},"ru.test.access",0,33));check(calls.size()==1,"failed initial query blocks mutation");
  calls.clear();reject(()->RestrictedOps.grant(cmd->{calls.add(cmd);return new ShellReply(calls.size()==2?1:0,"No operations.");},"ru.test.access",0,33));check(calls.size()==2,"failed mutation is not reported as success");
  calls.clear();reject(()->RestrictedOps.grant(cmd->{calls.add(cmd);return new ShellReply(0,"No operations.");},"ru.test.access",0,33));check(calls.size()==3,"missing readback confirmation rejects apparent success");
  for(String cmd:calls)check(!cmd.contains("settings put")&&!cmd.contains("enabled_accessibility_services"),"never enables service or writes secure settings");
  System.out.println("PASS "+count+" integrated restricted-settings command checks (fake runner, no device grant claim)");
 }
}
