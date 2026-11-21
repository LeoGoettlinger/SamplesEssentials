package cm.chunkManager.components;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.utils.ChunkUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public class ChunkFreezer {
   private final ChunkManager plugin;
   private final File dataFile;
   private final Set<String> frozenChunks = ConcurrentHashMap.newKeySet();

   public ChunkFreezer(ChunkManager plugin) {
      this.plugin = plugin;
      this.dataFile = new File(plugin.getDataFolder(), "frozen.yml");
      this.load();
      this.keepFrozenLoaded();
   }

   public boolean freeze(String world, int x, int z) {
      String key = world + ";" + x + ";" + z;
      if (this.frozenChunks.add(key)) {
         this.save();
         World w = Bukkit.getWorld(world);
         if (w != null) {
            ChunkUtils.getChunkAtAsync(w, x, z, this.plugin);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean unfreeze(String world, int x, int z) {
      String key = world + ";" + x + ";" + z;
      if (this.frozenChunks.remove(key)) {
         this.save();
         return true;
      } else {
         return false;
      }
   }

   public boolean isFrozen(String world, int x, int z) {
      return this.frozenChunks.contains(world + ";" + x + ";" + z);
   }

   public Set<String> getFrozenChunks() {
      return Set.copyOf(this.frozenChunks);
   }

   public int clear() {
      int count = this.frozenChunks.size();
      this.frozenChunks.clear();
      this.save();
      return count;
   }

   private void keepFrozenLoaded() {
      for (String key : this.frozenChunks) {
         String[] parts = key.split(";");
         if (parts.length == 3) {
            World world = Bukkit.getWorld(parts[0]);
            if (world != null) {
               int x = Integer.parseInt(parts[1]);
               int z = Integer.parseInt(parts[2]);
               ChunkUtils.getChunkAtAsync(world, x, z, this.plugin);
            }
         }
      }
   }

   private void load() {
      if (this.dataFile.exists()) {
         YamlConfiguration data = YamlConfiguration.loadConfiguration(this.dataFile);
         this.frozenChunks.addAll(data.getStringList("frozen"));
         if (!this.frozenChunks.isEmpty()) {
            this.plugin.getLogger().info("Loaded " + this.frozenChunks.size() + " frozen chunks");
         }
      }
   }

   private void save() {
      YamlConfiguration data = new YamlConfiguration();
      data.set("frozen", new ArrayList<>(this.frozenChunks));

      try {
         data.save(this.dataFile);
      } catch (IOException var3) {
         this.plugin.getLogger().warning("Failed to save frozen chunks: " + var3.getMessage());
      }
   }
}
