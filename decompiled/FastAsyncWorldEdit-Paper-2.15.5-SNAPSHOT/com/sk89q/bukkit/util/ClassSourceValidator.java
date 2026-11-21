package com.sk89q.bukkit.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ClassSourceValidator {
   private int commandHandlers;
   private static final String SEPARATOR_LINE = Strings.repeat("*", 46);
   private static final Method loadClass;
   private static Class<?> pluginClassLoaderClass;
   private final Plugin plugin;
   @Nullable
   private final ClassLoader expectedClassLoader;

   public ClassSourceValidator(Plugin plugin) {
      Preconditions.checkNotNull(plugin, "plugin");
      this.plugin = plugin;
      this.expectedClassLoader = plugin.getClass().getClassLoader();
      if (loadClass == null) {
         plugin.getLogger().info("Bukkit PluginClassLoader seems to have changed. Class source validation will be skipped.");
      }

      this.commandHandlers = this.registerCommandHandlers();
   }

   public Map<Class<?>, Plugin> findMismatches(List<Class<?>> classes) {
      Preconditions.checkNotNull(classes, "classes");
      if (this.expectedClassLoader != null && loadClass != null) {
         Map<Class<?>, Plugin> mismatches = new HashMap<>();

         for (Plugin target : Bukkit.getPluginManager().getPlugins()) {
            if (target != this.plugin) {
               ClassLoader targetLoader = target.getClass().getClassLoader();
               if (pluginClassLoaderClass.isAssignableFrom(targetLoader.getClass())) {
                  for (Class<?> testClass : classes) {
                     Class<?> targetClass;
                     try {
                        targetClass = (Class<?>)loadClass.invoke(targetLoader, testClass.getName(), false, false, false);
                     } catch (InvocationTargetException | IllegalAccessException var12) {
                        continue;
                     }

                     if (targetClass.getClassLoader() != this.expectedClassLoader) {
                        mismatches.putIfAbsent(testClass, targetClass.getClassLoader() == targetLoader ? target : null);
                     }
                  }
               }
            }
         }

         return mismatches;
      } else {
         return ImmutableMap.of();
      }
   }

   public void reportMismatches(List<Class<?>> classes) {
      if (!Boolean.getBoolean("enginehub.disable.class.source.validation")) {
         Map<Class<?>, Plugin> mismatches = this.findMismatches(classes);
         if (!mismatches.isEmpty()) {
            StringBuilder builder = new StringBuilder("\n");
            builder.append(SEPARATOR_LINE).append("\n");
            builder.append("** /!\\    SEVERE WARNING    /!\\\n");
            builder.append("** \n");
            builder.append("** A plugin developer has included a portion of \n");
            builder.append("** ").append(this.plugin.getName()).append(" into their own plugin, so rather than using\n");
            builder.append("** the version of ").append(this.plugin.getName()).append(" that you downloaded, you\n");
            builder.append("** will be using a broken mix of old ").append(this.plugin.getName()).append(" (that came\n");
            builder.append("** with the plugin) and your downloaded version. THIS MAY\n");
            builder.append("** SEVERELY BREAK ").append(this.plugin.getName().toUpperCase(Locale.ROOT)).append(" AND ALL OF ITS FEATURES.\n");
            builder.append("**\n");
            builder.append("** This may have happened because the developer is using\n");
            builder.append("** the ").append(this.plugin.getName()).append(" API and thinks that including\n");
            builder.append("** ").append(this.plugin.getName()).append(" is necessary. However, it is not!\n");
            builder.append("**\n");
            builder.append("** Here are some files that have been overridden:\n");
            builder.append("** \n");

            for (Entry<Class<?>, Plugin> entry : mismatches.entrySet()) {
               Plugin badPlugin = entry.getValue();
               String url = badPlugin == null
                  ? "(unknown)"
                  : badPlugin.getName() + " (" + badPlugin.getClass().getProtectionDomain().getCodeSource().getLocation() + ")";
               builder.append("** '").append(entry.getKey().getSimpleName()).append("' came from '").append(url).append("'\n");
            }

            builder.append("**\n");
            builder.append("** Please report this to the plugins' developers.\n");
            builder.append(SEPARATOR_LINE).append("\n");
            this.plugin.getLogger().severe(builder.toString());
         }
      }
   }

   static {
      Method tmp;
      try {
         pluginClassLoaderClass = Class.forName("org.bukkit.plugin.java.PluginClassLoader", false, Bukkit.class.getClassLoader());
         tmp = pluginClassLoaderClass.getDeclaredMethod("loadClass0", String.class, boolean.class, boolean.class, boolean.class);
         tmp.setAccessible(true);
      } catch (ClassNotFoundException | NoSuchMethodException var2) {
         tmp = null;
      }

      loadClass = tmp;
   }
}
