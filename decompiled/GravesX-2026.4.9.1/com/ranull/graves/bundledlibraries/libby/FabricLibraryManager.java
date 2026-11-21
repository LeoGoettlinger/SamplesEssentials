package com.ranull.graves.bundledlibraries.libby;

import com.ranull.graves.bundledlibraries.libby.logging.adapters.FabricLogAdapter;
import com.ranull.graves.bundledlibraries.libby.logging.adapters.LogAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class FabricLibraryManager extends LibraryManager {
   private final ModContainer modContainer;

   public FabricLibraryManager(@NotNull String modId, @NotNull Logger logger) {
      this(modId, logger, "lib");
   }

   public FabricLibraryManager(@NotNull String modId, @NotNull Logger logger, @NotNull String directoryName) {
      this(modId, new FabricLogAdapter(logger), directoryName);
   }

   public FabricLibraryManager(@NotNull String modId, @NotNull LogAdapter logAdapter, @NotNull String directoryName) {
      super(logAdapter, FabricLoader.getInstance().getConfigDir().resolve(modId), directoryName);
      this.modContainer = (ModContainer)FabricLoader.getInstance()
         .getModContainer(Objects.requireNonNull(modId, modId))
         .orElseThrow(() -> new NullPointerException("modContainer"));
   }

   @Override
   protected void addToClasspath(@NotNull Path file) {
      FabricLauncherBase.getLauncher().addToClassPath(file, new String[0]);
   }

   @Override
   protected InputStream getResourceAsStream(@NotNull String path) {
      try {
         return Files.newInputStream(Objects.requireNonNull((Path)this.modContainer.findPath(path).orElse(null)));
      } catch (IOException var3) {
         return null;
      }
   }
}
