package com.ranull.graves.bundledlibraries.libby;

import com.ranull.graves.bundledlibraries.libby.classloader.ClassLoaderHelper;
import com.ranull.graves.bundledlibraries.libby.classloader.SystemClassLoaderHelper;
import com.ranull.graves.bundledlibraries.libby.classloader.URLClassLoaderHelper;
import com.ranull.graves.bundledlibraries.libby.logging.adapters.LogAdapter;
import java.net.URLClassLoader;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class StandaloneLibraryManager extends LibraryManager {
   @NotNull
   private final ClassLoaderHelper classLoaderHelper;

   public StandaloneLibraryManager(@NotNull LogAdapter logAdapter, @NotNull Path dataDirectory) {
      this(logAdapter, dataDirectory, "lib");
   }

   public StandaloneLibraryManager(@NotNull LogAdapter logAdapter, @NotNull Path dataDirectory, @NotNull String directoryName) {
      super(logAdapter, dataDirectory, directoryName);
      ClassLoader classLoader = this.getClass().getClassLoader();
      if (classLoader instanceof URLClassLoader) {
         this.classLoaderHelper = new URLClassLoaderHelper((URLClassLoader)classLoader, this);
      } else {
         if (classLoader != ClassLoader.getSystemClassLoader()) {
            throw new RuntimeException("Unsupported class loader: " + classLoader.getClass().getName());
         }

         this.classLoaderHelper = new SystemClassLoaderHelper(classLoader, this);
      }
   }

   @Override
   protected void addToClasspath(@NotNull Path file) {
      this.classLoaderHelper.addToClasspath(file);
   }
}
