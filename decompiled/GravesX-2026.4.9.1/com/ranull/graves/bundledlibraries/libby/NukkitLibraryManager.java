package com.ranull.graves.bundledlibraries.libby;

import cn.nukkit.plugin.Plugin;
import com.ranull.graves.bundledlibraries.libby.classloader.URLClassLoaderHelper;
import com.ranull.graves.bundledlibraries.libby.logging.adapters.LogAdapter;
import com.ranull.graves.bundledlibraries.libby.logging.adapters.NukkitLogAdapter;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class NukkitLibraryManager extends LibraryManager {
   @NotNull
   private final URLClassLoaderHelper classLoader;
   @NotNull
   private final Plugin plugin;

   public NukkitLibraryManager(@NotNull Plugin plugin) {
      this(plugin, "lib");
   }

   public NukkitLibraryManager(@NotNull Plugin plugin, @NotNull String directoryName) {
      this(plugin, directoryName, new NukkitLogAdapter(Objects.requireNonNull(plugin, "plugin").getLogger()));
   }

   public NukkitLibraryManager(@NotNull Plugin plugin, @NotNull String directoryName, @NotNull LogAdapter logAdapter) {
      super(logAdapter, plugin.getDataFolder().toPath(), directoryName);
      this.classLoader = new URLClassLoaderHelper((URLClassLoader)plugin.getClass().getClassLoader(), this);
      this.plugin = plugin;
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
