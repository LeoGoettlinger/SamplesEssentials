package com.magmaguy.elitemobs.magmacore.match;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.WorldFolderResolver;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.World;

final class LeftoverWorlds {
   static final String MARKER_FILE = ".magmacore-match-world";
   static final String MARKER_VERSION = "magmacore-match-world-v1";

   private LeftoverWorlds() {
   }

   static void writeMarker(Path folder, String pluginName, String worldName) throws IOException {
      Files.writeString(folder.resolve(".magmacore-match-world"), "magmacore-match-world-v1\n" + pluginName + "\n" + worldName + "\n", StandardCharsets.UTF_8);
   }

   static void markLoaded(World world) {
      File folder;
      try {
         folder = world.getWorldFolder();
      } catch (RuntimeException var6) {
         try {
            folder = WorldFolderResolver.resolve(world.getName());
         } catch (RuntimeException var5) {
            return;
         }
      }

      if (folder != null && folder.isDirectory()) {
         try {
            writeMarker(folder.toPath(), MatchCore.plugin().getName(), world.getName());
         } catch (IOException var4) {
            Logger.warn("Could not mark temporary world " + world.getName() + ": " + var4.getMessage());
         }
      }
   }

   static void sweep(String pluginName, Iterable<String> worldNames, Function<String, Path> folderOf, Predicate<String> loaded, Consumer<String> delete) {
      for (String name : worldNames) {
         if (!loaded.test(name)) {
            Path folder = folderOf.apply(name);
            if (folder != null) {
               Path marker = folder.resolve(".magmacore-match-world");
               if (Files.isRegularFile(marker)) {
                  try {
                     List<String> lines = Files.readAllLines(marker, StandardCharsets.UTF_8);
                     if (lines.size() >= 2 && "magmacore-match-world-v1".equals(lines.get(0)) && pluginName.equals(lines.get(1))) {
                        Logger.info("Deleting leftover match world " + name);
                        delete.accept(name);
                     }
                  } catch (IOException var10) {
                     Logger.warn("Could not read world marker " + marker + ": " + var10.getMessage());
                  }
               }
            }
         }
      }
   }
}
