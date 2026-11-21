package com.skyblockexp.ezrtp.teleport;

import org.bukkit.plugin.java.JavaPlugin;

public interface ChunkyProvider {
   boolean isRunning(String var1);

   boolean startTask(String var1, String var2, double var3, double var5, int var7, int var8, String var9);

   default void registerListeners(JavaPlugin plugin) {
   }
}
