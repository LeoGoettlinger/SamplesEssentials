package com.fastasyncworldedit.bukkit.util;

import com.fastasyncworldedit.core.util.ReflectionUtils;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class BukkitReflectionUtils {
   private static volatile String preClassB = null;
   private static volatile String preClassM = null;

   public static void init() {
      Server server = Bukkit.getServer();
      Class<?> bukkitServerClass = server.getClass();
      String[] pas = bukkitServerClass.getName().split("\\.");
      if (pas.length == 5) {
         String verB = pas[3];
         preClassB = "org.bukkit.craftbukkit." + verB;
      }

      try {
         Method getHandle = bukkitServerClass.getDeclaredMethod("getHandle");
         Object handle = getHandle.invoke(server);
         Class<?> handleServerClass = handle.getClass();
         pas = handleServerClass.getName().split("\\.");
         if (pas.length == 5) {
            String verM = pas[3];
            preClassM = "net.minecraft.server." + verM;
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   public static Class<?> getNmsClass(final String name) {
      String className = "net.minecraft.server." + getVersion() + "." + name;
      return ReflectionUtils.getClass(className);
   }

   public static Class<?> getCbClass(final String name) {
      String className = "org.bukkit.craftbukkit." + getVersion() + "." + name;
      return ReflectionUtils.getClass(className);
   }

   public static String getVersion() {
      String packageName = Bukkit.getServer().getClass().getPackage().getName();
      return packageName.substring(packageName.lastIndexOf(46) + 1);
   }
}
