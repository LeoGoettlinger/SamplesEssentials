package cm.chunkManager.components;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.utils.ChunkUtils;
import cm.chunkManager.utils.Scheduler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ChunkPreGen {
   private final ChunkManager plugin;
   private boolean active;
   private World world;
   private int generated;
   private int total;
   private int skipped;
   private int chunksPerTick;
   private double tpsThreshold;
   private boolean showActionbar;
   private UUID playerUUID;
   private Scheduler.Handle taskHandle;
   private List<int[]> chunks;
   private int index;

   public ChunkPreGen(ChunkManager plugin) {
      this.plugin = plugin;
      this.active = false;
      this.loadConfig();
   }

   public void loadConfig() {
      ConfigurationManager cfg = this.plugin.getConfigManager();
      this.chunksPerTick = cfg.getPreGenChunksPerTick();
      this.tpsThreshold = cfg.getPreGenTpsThreshold();
      this.showActionbar = cfg.isPreGenActionbar();
   }

   public boolean start(Player player, World world, int centerX, int centerZ, int radius) {
      if (this.active) {
         return false;
      } else if (radius >= 1 && radius <= 500) {
         this.world = world;
         this.playerUUID = player.getUniqueId();
         this.generated = 0;
         this.skipped = 0;
         this.index = 0;
         this.chunks = this.buildSpiral(centerX, centerZ, radius);
         this.total = this.chunks.size();
         this.active = true;
         this.loadConfig();
         this.taskHandle = Scheduler.runGlobalTimer(this.plugin, this::tick, 1L, 1L);
         Map<String, String> ph = new HashMap<>();
         ph.put("world", world.getName());
         ph.put("total", String.valueOf(this.total));
         player.sendMessage(this.plugin.getLanguageManager().getMessage("pregen-started", ph));
         return true;
      } else {
         return false;
      }
   }

   public void stop() {
      if (this.active) {
         this.active = false;
         if (this.taskHandle != null && !this.taskHandle.isCancelled()) {
            this.taskHandle.cancel();
            this.taskHandle = null;
         }

         Player player = Bukkit.getPlayer(this.playerUUID);
         if (player != null && player.isOnline()) {
            Map<String, String> ph = new HashMap<>();
            ph.put("generated", String.valueOf(this.generated));
            ph.put("total", String.valueOf(this.total));
            player.sendMessage(this.plugin.getLanguageManager().getMessage("pregen-stopped", ph));
         }
      }
   }

   public void shutdown() {
      if (this.taskHandle != null && !this.taskHandle.isCancelled()) {
         this.taskHandle.cancel();
         this.taskHandle = null;
      }

      this.active = false;
   }

   public boolean isActive() {
      return this.active;
   }

   public Map<String, String> getProgress() {
      Map<String, String> info = new HashMap<>();
      info.put("generated", String.valueOf(this.generated));
      info.put("total", String.valueOf(this.total));
      info.put("skipped", String.valueOf(this.skipped));
      info.put("world", this.world != null ? this.world.getName() : "none");
      int percent = this.total > 0 ? (int)((this.generated + this.skipped) * 100L / this.total) : 0;
      info.put("percent", String.valueOf(percent));
      return info;
   }

   private void tick() {
      if (this.active && this.world != null) {
         double tickTime = this.plugin.getPerformanceMonitor().getAverageTickTime();
         double maxTickTime = 1000.0 / this.tpsThreshold;
         if (!(tickTime > maxTickTime)) {
            for (int processed = 0; processed < this.chunksPerTick && this.index < this.chunks.size(); processed++) {
               int[] coord = this.chunks.get(this.index);
               this.index++;
               if (this.world.isChunkLoaded(coord[0], coord[1])) {
                  this.skipped++;
               } else {
                  ChunkUtils.getChunkAtAsync(this.world, coord[0], coord[1], this.plugin);
                  this.generated++;
               }
            }

            if ((this.generated + this.skipped) % 100 < this.chunksPerTick && this.showActionbar) {
               this.sendActionbar();
            }

            if (this.index >= this.chunks.size()) {
               this.complete();
            }
         }
      }
   }

   private void sendActionbar() {
      Player player = Bukkit.getPlayer(this.playerUUID);
      if (player != null && player.isOnline()) {
         Map<String, String> ph = this.getProgress();
         String msg = this.plugin.getLanguageManager().getMessage("pregen-progress", ph);

         try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(msg));
         } catch (Exception var5) {
         }
      }
   }

   private void complete() {
      this.active = false;
      if (this.taskHandle != null && !this.taskHandle.isCancelled()) {
         this.taskHandle.cancel();
         this.taskHandle = null;
      }

      Player player = Bukkit.getPlayer(this.playerUUID);
      if (player != null && player.isOnline()) {
         Map<String, String> ph = new HashMap<>();
         ph.put("generated", String.valueOf(this.generated));
         ph.put("world", this.world.getName());
         player.sendMessage(this.plugin.getLanguageManager().getMessage("pregen-complete", ph));
      }

      this.plugin.getLogger().info("Pre-generation complete for " + this.world.getName() + " - Generated: " + this.generated + ", Skipped: " + this.skipped);
   }

   private List<int[]> buildSpiral(int cx, int cz, int radius) {
      List<int[]> list = new ArrayList<>();
      list.add(new int[]{cx, cz});

      for (int layer = 1; layer <= radius; layer++) {
         int x = cx + layer;
         int z = cz - layer + 1;

         for (int i = 0; i < layer * 2; i++) {
            list.add(new int[]{x, z});
            z++;
         }

         x--;

         for (int i = 0; i < layer * 2 - 1; i++) {
            list.add(new int[]{x, z});
            x--;
         }

         z--;

         for (int i = 0; i < layer * 2 - 1; i++) {
            list.add(new int[]{x, z});
            z--;
         }

         x++;

         for (int i = 0; i < layer * 2 - 2; i++) {
            list.add(new int[]{x, z});
            x++;
         }
      }

      return list;
   }
}
