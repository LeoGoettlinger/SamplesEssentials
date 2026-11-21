package com.earth2me.essentials;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ess3.nms.refl.ReflUtil;
import net.ess3.provider.LoggerProvider;
import net.ess3.provider.providers.BaseLoggerProvider;
import net.ess3.provider.providers.PaperLoggerProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class EssentialsLogger {
   private static final Map<String, LoggerProvider> loggerProviders = new HashMap<>();
   private static final MethodHandle loggerFieldHandle;

   private EssentialsLogger() {
   }

   public static LoggerProvider getLoggerProvider(final Plugin plugin) {
      if (loggerProviders.containsKey(plugin.getName())) {
         return loggerProviders.get(plugin.getName());
      } else {
         Logger parentLogger = Logger.getLogger(plugin.getName());
         LoggerProvider provider;
         if (ReflUtil.getClassCached("io.papermc.paper.adventure.providers.ComponentLoggerProviderImpl") != null) {
            provider = new PaperLoggerProvider(plugin);
            provider.setParent(parentLogger);
         } else {
            provider = new BaseLoggerProvider(plugin, parentLogger);
            provider.setParent(parentLogger);
         }

         loggerProviders.put(plugin.getName(), provider);
         return provider;
      }
   }

   public static void updatePluginLogger(final Plugin plugin) {
      LoggerProvider provider = getLoggerProvider(plugin);

      try {
         loggerFieldHandle.invoke((Plugin)plugin, (LoggerProvider)provider);
      } catch (Throwable var3) {
         provider.log(Level.SEVERE, "Failed to update " + plugin.getName() + " logger", var3);
      }
   }

   public static LoggerProvider getLoggerProvider(final String pluginName) {
      if (loggerProviders.containsKey(pluginName)) {
         return loggerProviders.get(pluginName);
      } else {
         Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
         if (plugin == null) {
            throw new IllegalArgumentException("Plugin not found: " + pluginName);
         } else {
            return getLoggerProvider(plugin);
         }
      }
   }

   static {
      try {
         Field loggerField = ReflUtil.getFieldCached(JavaPlugin.class, "logger");
         loggerFieldHandle = MethodHandles.lookup().unreflectSetter(loggerField);
      } catch (Throwable var1) {
         throw new RuntimeException("Failed to get logger field handle", var1);
      }
   }
}
