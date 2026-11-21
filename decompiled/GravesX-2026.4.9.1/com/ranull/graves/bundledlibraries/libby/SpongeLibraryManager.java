package com.ranull.graves.bundledlibraries.libby;

import com.ranull.graves.bundledlibraries.libby.classloader.SpongeClassLoaderHelper;
import com.ranull.graves.bundledlibraries.libby.logging.adapters.LogAdapter;
import com.ranull.graves.bundledlibraries.libby.logging.adapters.SpongeLogAdapter;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class SpongeLibraryManager<T> extends LibraryManager {
   @NotNull
   private final SpongeClassLoaderHelper classLoader;

   public SpongeLibraryManager(@NotNull T plugin, @NotNull Logger logger, @NotNull Path dataDirectory) {
      this(plugin, logger, dataDirectory, "lib");
   }

   public SpongeLibraryManager(@NotNull T plugin, @NotNull Logger logger, @NotNull Path dataDirectory, @NotNull String directoryName) {
      this(plugin, new SpongeLogAdapter(logger), dataDirectory, directoryName);
   }

   public SpongeLibraryManager(@NotNull T plugin, @NotNull LogAdapter logAdapter, @NotNull Path dataDirectory, @NotNull String directoryName) {
      super(logAdapter, dataDirectory, directoryName);
      this.classLoader = new SpongeClassLoaderHelper(plugin.getClass().getClassLoader(), this);
   }

   @Override
   protected void addToClasspath(@NotNull Path file) {
      Objects.requireNonNull(this.classLoader, "classLoader");
      this.classLoader.addToClasspath(file);
   }

   @Override
   protected InputStream getResourceAsStream(@NotNull String path) {
      return this.getClass().getClassLoader().getResourceAsStream(path);
   }
}
