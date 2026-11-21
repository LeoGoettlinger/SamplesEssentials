package com.ranull.graves.bundledlibraries.libby;

import com.ranull.graves.bundledlibraries.libby.classloader.URLClassLoaderHelper;
import com.ranull.graves.bundledlibraries.libby.logging.adapters.JDKLogAdapter;
import com.ranull.graves.bundledlibraries.libby.logging.adapters.LogAdapter;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Objects;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PaperLibraryManager extends LibraryManager {
   @NotNull
   private final URLClassLoaderHelper classLoader;
   @NotNull
   private final Plugin plugin;

   public PaperLibraryManager(@NotNull Plugin plugin) {
      this(plugin, "lib");
   }

   public PaperLibraryManager(@NotNull Plugin plugin, @NotNull String directoryName) {
      this(plugin, directoryName, new JDKLogAdapter(Objects.requireNonNull(plugin, "plugin").getLogger()));
   }

   public PaperLibraryManager(@NotNull Plugin plugin, @NotNull String directoryName, @NotNull LogAdapter logAdapter) {
      super(logAdapter, plugin.getDataFolder().toPath(), directoryName);
      ClassLoader cl = plugin.getClass().getClassLoader();

      Class<?> paperClClazz;
      try {
         paperClClazz = Class.forName("io.papermc.paper.plugin.entrypoint.classloader.PaperPluginClassLoader");
      } catch (ClassNotFoundException var11) {
         this.getLogger().error("PaperPluginClassLoader not found, are you using Paper 1.19.3+?");
         throw new RuntimeException(var11);
      }

      if (!paperClClazz.isAssignableFrom(cl.getClass())) {
         throw new RuntimeException("Plugin classloader is not a PaperPluginClassLoader, are you using paper-plugin.yml?");
      } else {
         Field libraryLoaderField;
         try {
            libraryLoaderField = paperClClazz.getDeclaredField("libraryLoader");
         } catch (NoSuchFieldException var10) {
            this.getLogger().error("Cannot find libraryLoader field in PaperPluginClassLoader, please open a bug report.");
            throw new RuntimeException(var10);
         }

         libraryLoaderField.setAccessible(true);

         URLClassLoader libraryLoader;
         try {
            libraryLoader = (URLClassLoader)libraryLoaderField.get(cl);
         } catch (IllegalAccessException var9) {
            throw new RuntimeException(var9);
         }

         this.classLoader = new URLClassLoaderHelper(libraryLoader, this);
         this.plugin = plugin;
      }
   }

   @Override
   protected void addToClasspath(@NotNull Path file) {
      this.classLoader.addToClasspath(file);
   }

   @Override
   protected InputStream getResourceAsStream(@NotNull String path) {
      return this.plugin.getResource(path);
   }
}
