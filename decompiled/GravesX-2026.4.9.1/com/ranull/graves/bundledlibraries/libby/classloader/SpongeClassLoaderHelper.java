package com.ranull.graves.bundledlibraries.libby.classloader;

import com.ranull.graves.bundledlibraries.libby.SpongeLibraryManager;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class SpongeClassLoaderHelper {
   @NotNull
   private final URLClassLoaderHelper classLoader;

   public SpongeClassLoaderHelper(@NotNull ClassLoader pluginClassLoader, @NotNull SpongeLibraryManager<?> libraryManager) {
      Objects.requireNonNull(pluginClassLoader, "pluginClassLoader");
      Objects.requireNonNull(libraryManager, "libraryManager");

      try {
         Field delegatedClassLoaderField = pluginClassLoader.getClass().getDeclaredField("delegatedClassLoader");
         delegatedClassLoaderField.setAccessible(true);
         URLClassLoader spongeClassLoader = (URLClassLoader)delegatedClassLoaderField.get(pluginClassLoader);
         this.classLoader = new URLClassLoaderHelper(spongeClassLoader, libraryManager);
      } catch (IllegalAccessException | NoSuchFieldException var5) {
         throw new RuntimeException(var5);
      }
   }

   public void addToClasspath(@NotNull Path path) {
      Objects.requireNonNull(this.classLoader, "classLoader");
      this.classLoader.addToClasspath(path);
   }
}
