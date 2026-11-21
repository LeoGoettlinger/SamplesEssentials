package com.ranull.graves.bundledlibraries.libby;

import com.ranull.graves.bundledlibraries.libby.logging.adapters.LogAdapter;
import com.ranull.graves.bundledlibraries.libby.logging.adapters.VelocityLogAdapter;
import com.velocitypowered.api.plugin.PluginManager;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class VelocityLibraryManager<T> extends LibraryManager {
   @NotNull
   private final PluginManager pluginManager;
   @NotNull
   private final T plugin;

   public VelocityLibraryManager(@NotNull T plugin, @NotNull Logger logger, @NotNull Path dataDirectory, @NotNull PluginManager pluginManager) {
      this(plugin, logger, dataDirectory, pluginManager, "lib");
   }

   public VelocityLibraryManager(
      @NotNull T plugin, @NotNull Logger logger, @NotNull Path dataDirectory, @NotNull PluginManager pluginManager, @NotNull String directoryName
   ) {
      this(plugin, new VelocityLogAdapter(logger), dataDirectory, pluginManager, directoryName);
   }

   public VelocityLibraryManager(
      @NotNull T plugin, @NotNull LogAdapter logAdapter, @NotNull Path dataDirectory, @NotNull PluginManager pluginManager, @NotNull String directoryName
   ) {
      super(logAdapter, dataDirectory, directoryName);
      this.pluginManager = Objects.requireNonNull(pluginManager, "pluginManager");
      this.plugin = Objects.requireNonNull(plugin, "plugin");
   }

   @Override
   protected void addToClasspath(@NotNull Path file) {
      this.pluginManager.addToClasspath(this.plugin, file);
   }

   @Override
   protected InputStream getResourceAsStream(@NotNull String path) {
      return this.getClass().getClassLoader().getResourceAsStream(path);
   }
}
