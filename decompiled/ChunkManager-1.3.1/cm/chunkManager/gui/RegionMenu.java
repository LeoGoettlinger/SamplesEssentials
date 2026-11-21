package cm.chunkManager.gui;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.components.RegionOptimizer;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class RegionMenu extends Menu {
   public RegionMenu(ChunkManager plugin, Player player, GuiManager guiManager) {
      super(plugin, player, guiManager);
   }

   @Override
   public String getTitle() {
      return GuiUtils.title("" + ChatColor.GREEN + ChatColor.BOLD + "Regions");
   }

   @Override
   public int getSize() {
      return 54;
   }

   @Override
   public void build() {
      this.setItem(4, GuiUtils.header("Region Stats", "World region activity"), null);
      if (!GuiUtils.isAdvancedEnabled(this.plugin)) {
         this.setItem(22, GuiUtils.item(Material.BARRIER, ChatColor.RED + "Advanced Features Disabled", ChatColor.GRAY + "Enable in config.yml"), null);
         this.addNav();
         GuiUtils.applyBorder(this.inv, this.getSize());
      } else {
         RegionOptimizer regionOpt = this.plugin.getRegionOptimizer();
         if (regionOpt == null) {
            this.setItem(22, GuiUtils.item(Material.BARRIER, ChatColor.RED + "Region Optimizer Not Available"), null);
            this.addNav();
            GuiUtils.applyBorder(this.inv, this.getSize());
         } else {
            int slot = 19;

            for (World world : Bukkit.getWorlds()) {
               if (!this.plugin.isWorldBlacklisted(world)) {
                  if (slot >= 35) {
                     break;
                  }

                  Map<String, Object> stats = regionOpt.getRegionStatistics(world);
                  int highActivity = (Integer)stats.getOrDefault("highActivityRegions", 0);
                  Material mat = Material.GRASS_BLOCK;
                  ChatColor nameColor = ChatColor.GREEN;
                  if (highActivity > 10) {
                     mat = Material.REDSTONE_BLOCK;
                     nameColor = ChatColor.RED;
                  } else if (highActivity > 5) {
                     mat = Material.GOLD_BLOCK;
                     nameColor = ChatColor.GOLD;
                  }

                  this.setItem(
                     slot++,
                     GuiUtils.item(
                        mat,
                        nameColor + world.getName(),
                        "",
                        ChatColor.GRAY + "Total Regions: " + ChatColor.WHITE + stats.get("totalRegions"),
                        ChatColor.GRAY + "Active: " + ChatColor.WHITE + stats.get("activeRegions"),
                        ChatColor.GRAY + "Optimized: " + ChatColor.WHITE + stats.get("optimizedRegions"),
                        ChatColor.YELLOW + "High Activity: " + ChatColor.WHITE + stats.get("highActivityRegions")
                     ),
                     null
                  );
               }
            }

            this.addNav();
            GuiUtils.applyBorder(this.inv, this.getSize());
         }
      }
   }

   private void addNav() {
      this.setItem(45, GuiUtils.back(), () -> this.guiManager.openMenu(this.player, new MainMenu(this.plugin, this.player, this.guiManager)));
      this.setItem(53, GuiUtils.close(), () -> this.player.closeInventory());
   }
}
