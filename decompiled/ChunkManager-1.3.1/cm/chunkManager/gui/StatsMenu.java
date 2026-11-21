package cm.chunkManager.gui;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.components.PerformanceMonitor;
import cm.chunkManager.utils.MemoryManager;
import java.text.DecimalFormat;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class StatsMenu extends Menu {
   private final DecimalFormat df = new DecimalFormat("#.##");

   public StatsMenu(ChunkManager plugin, Player player, GuiManager guiManager) {
      super(plugin, player, guiManager);
   }

   @Override
   public String getTitle() {
      return GuiUtils.title("" + ChatColor.AQUA + ChatColor.BOLD + "Performance");
   }

   @Override
   public int getSize() {
      return 54;
   }

   @Override
   public void build() {
      PerformanceMonitor perfMon = this.plugin.getPerformanceMonitor();
      MemoryManager memMgr = this.plugin.getMemoryManager();
      double tickTime = perfMon.getAverageTickTime();
      double tps = tickTime > 50.0 ? Math.max(0.0, 20.0 - (tickTime - 50.0) / 50.0) : 20.0;
      MemoryManager.MemoryStats memStats = memMgr.getMemoryStats();
      Map<String, String> info = perfMon.getPerformanceInfo();
      ChatColor tpsColor = tps >= 19.0 ? ChatColor.GREEN : (tps >= 17.0 ? ChatColor.YELLOW : ChatColor.RED);
      this.setItem(4, GuiUtils.header("Performance Stats", "Real-time server metrics"), null);
      this.setItem(
         20,
         GuiUtils.item(
            Material.CLOCK,
            ChatColor.AQUA + "Server Performance",
            "",
            tpsColor + "TPS: " + this.df.format(tps) + "/20.0",
            ChatColor.GRAY + "Tick Time: " + ChatColor.WHITE + this.df.format(tickTime) + "ms",
            ChatColor.GRAY + "Target: " + ChatColor.WHITE + "50ms"
         ),
         null
      );
      ChatColor memColor = GuiUtils.memColor(memStats.usagePercentage);
      this.setItem(
         22,
         GuiUtils.item(
            Material.REDSTONE,
            memColor + "Memory Usage",
            "",
            ChatColor.WHITE + this.df.format(memStats.usedMB) + "/" + this.df.format(memStats.maxMB) + " MB",
            ChatColor.GRAY + "Usage: " + memColor + this.df.format(memStats.usagePercentage) + "%"
         ),
         null
      );
      this.setItem(
         24,
         GuiUtils.item(
            Material.CHEST,
            ChatColor.AQUA + "Chunk Cache",
            "",
            ChatColor.GRAY + "Queue: " + ChatColor.WHITE + this.plugin.getChunkCache().getQueueSize(),
            ChatColor.GRAY + "Kept Loaded: " + ChatColor.WHITE + this.plugin.getChunkCache().getKeepLoadedSize(),
            ChatColor.GRAY + "Max Size: " + ChatColor.WHITE + this.plugin.getConfigManager().getMaxCacheSize()
         ),
         null
      );
      this.setItem(
         13,
         GuiUtils.item(
            Material.PLAYER_HEAD,
            ChatColor.AQUA + "Players & Chunks",
            "",
            ChatColor.GRAY + "Players: " + ChatColor.WHITE + info.get("online_players"),
            ChatColor.GRAY + "Total Chunks: " + ChatColor.WHITE + info.get("total_chunks"),
            ChatColor.GRAY + "Radius: " + ChatColor.WHITE + this.plugin.getConfigManager().getDynamicRadius()
         ),
         null
      );
      if (this.plugin.isMaintenanceMode()) {
         this.setItem(
            31,
            GuiUtils.item(
               Material.BARRIER, ChatColor.RED + "Maintenance Mode", "", ChatColor.YELLOW + "Currently Active", ChatColor.GRAY + "Reduced chunk loading"
            ),
            null
         );
      }

      int worldSlot = 37;

      for (World world : Bukkit.getWorlds()) {
         if (!this.plugin.isWorldBlacklisted(world) && worldSlot < 44) {
            int chunks = 0;
            int entities = 0;

            try {
               chunks = world.getLoadedChunks().length;
               if (GuiUtils.canAccessEntities()) {
                  entities = world.getEntities().size();
               }
            } catch (Exception var17) {
            }
            Material mat = switch (world.getEnvironment()) {
               case NETHER -> Material.NETHERRACK;
               case THE_END -> Material.END_STONE;
               default -> Material.GRASS_BLOCK;
            };
            this.setItem(
               worldSlot++,
               GuiUtils.item(
                  mat,
                  ChatColor.GREEN + world.getName(),
                  "",
                  ChatColor.GRAY + "Chunks: " + ChatColor.WHITE + chunks,
                  ChatColor.GRAY + "Entities: " + ChatColor.WHITE + entities
               ),
               null
            );
         }
      }

      this.setItem(45, GuiUtils.back(), () -> this.guiManager.openMenu(this.player, new MainMenu(this.plugin, this.player, this.guiManager)));
      this.setItem(53, GuiUtils.close(), () -> this.player.closeInventory());
      GuiUtils.applyBorder(this.inv, this.getSize());
   }
}
