package me.byteful.plugin.leveltools.api.block;

import java.nio.file.Path;
import me.byteful.plugin.leveltools.api.block.impl.FileBlockDataManager;
import me.byteful.plugin.leveltools.api.block.impl.SqliteBlockDataManager;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import org.bukkit.configuration.ConfigurationSection;

public class BlockDataManagerFactory {
   public static BlockDataManager createBlockDataManager(Path dataFolder, ConfigurationSection config, Scheduler scheduler) {
      String storageType = config.getString("block_data_storage.type", "SQLITE");
      String var4 = storageType.toUpperCase();
      switch (var4) {
         case "LEGACY_TEXT":
            return new FileBlockDataManager(dataFolder.resolve("placed_blocks.txt"), scheduler);
         case "SQLITE":
         default:
            return new SqliteBlockDataManager(dataFolder.resolve("placed_blocks.db"), scheduler);
      }
   }
}
