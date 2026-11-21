package oshi.util;

import com.sun.jna.Platform;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class UserGroupInfo {
   private static final Supplier<Map<String, String>> USERS_ID_MAP = Memoizer.memoize(UserGroupInfo::getUserMap, TimeUnit.MINUTES.toNanos(5L));
   private static final Supplier<Map<String, String>> GROUPS_ID_MAP = Memoizer.memoize(UserGroupInfo::getGroupMap, TimeUnit.MINUTES.toNanos(5L));
   private static final boolean ELEVATED = 0 == ParseUtil.parseIntOrDefault(ExecutingCommand.getFirstAnswer("id -u"), -1);

   private UserGroupInfo() {
   }

   public static boolean isElevated() {
      return ELEVATED;
   }

   public static String getUser(String userId) {
      return USERS_ID_MAP.get().getOrDefault(userId, getentPasswd(userId));
   }

   public static String getGroupName(String groupId) {
      return GROUPS_ID_MAP.get().getOrDefault(groupId, getentGroup(groupId));
   }

   private static Map<String, String> getUserMap() {
      return parsePasswd(FileUtil.readFile("/etc/passwd"));
   }

   private static String getentPasswd(String userId) {
      if (Platform.isAIX()) {
         return "unknown";
      } else {
         Map<String, String> newUsers = parsePasswd(ExecutingCommand.runNative("getent passwd " + userId));
         USERS_ID_MAP.get().putAll(newUsers);
         return newUsers.getOrDefault(userId, "unknown");
      }
   }

   private static Map<String, String> parsePasswd(List<String> passwd) {
      Map<String, String> userMap = new ConcurrentHashMap<>();

      for (String entry : passwd) {
         String[] split = entry.split(":");
         if (split.length > 2) {
            String userName = split[0];
            String uid = split[2];
            userMap.putIfAbsent(uid, userName);
         }
      }

      return userMap;
   }

   private static Map<String, String> getGroupMap() {
      return parseGroup(FileUtil.readFile("/etc/group"));
   }

   private static String getentGroup(String groupId) {
      if (Platform.isAIX()) {
         return "unknown";
      } else {
         Map<String, String> newGroups = parseGroup(ExecutingCommand.runNative("getent group " + groupId));
         GROUPS_ID_MAP.get().putAll(newGroups);
         return newGroups.getOrDefault(groupId, "unknown");
      }
   }

   private static Map<String, String> parseGroup(List<String> group) {
      Map<String, String> groupMap = new ConcurrentHashMap<>();

      for (String entry : group) {
         String[] split = entry.split(":");
         if (split.length > 2) {
            String groupName = split[0];
            String gid = split[2];
            groupMap.putIfAbsent(gid, groupName);
         }
      }

      return groupMap;
   }
}
