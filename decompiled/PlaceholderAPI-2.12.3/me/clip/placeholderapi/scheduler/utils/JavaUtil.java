package me.clip.placeholderapi.scheduler.utils;

public class JavaUtil {
   public static boolean classExists(String className) {
      try {
         Class.forName(className);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }
}
