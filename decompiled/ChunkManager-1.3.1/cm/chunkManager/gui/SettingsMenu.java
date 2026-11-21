package cm.chunkManager.gui;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.components.ConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SettingsMenu extends Menu {
   public SettingsMenu(ChunkManager plugin, Player player, GuiManager guiManager) {
      super(plugin, player, guiManager);
   }

   @Override
   public String getTitle() {
      return GuiUtils.title("" + ChatColor.GOLD + ChatColor.BOLD + "Settings");
   }

   @Override
   public int getSize() {
      return 54;
   }

   @Override
   public void build() {
      ConfigurationManager cfg = this.plugin.getConfigManager();
      boolean maintenanceMode = this.plugin.isMaintenanceMode();
      boolean dynamicRadius = cfg.isDynamicRadiusEnabled();
      this.setItem(
         4, GuiUtils.header("ChunkManager Settings", "Configure plugin behavior", ChatColor.DARK_GRAY + "v" + this.plugin.getDescription().getVersion()), null
      );
      this.setItem(20, GuiUtils.toggle("Maintenance Mode", maintenanceMode, Material.REDSTONE_LAMP, "Reduces server load", "during high usage"), () -> {
         this.plugin.setMaintenanceMode(!maintenanceMode);
         GuiUtils.click(this.player);
         this.refresh();
      });
      this.setItem(22, GuiUtils.toggle("Dynamic Radius", dynamicRadius, Material.COMPASS, "Auto-adjusts radius", "based on performance"), () -> {
         cfg.saveDynamicRadiusEnabled(!dynamicRadius);
         GuiUtils.click(this.player);
         this.refresh();
      });
      this.setItem(
         24,
         GuiUtils.toggle(
            "Smart Unloading", this.plugin.getConfig().getBoolean("smart-unloading.enabled", true), Material.HOPPER, "Heat-based chunk", "unloading system"
         ),
         () -> {
            boolean current = this.plugin.getConfig().getBoolean("smart-unloading.enabled", true);
            this.plugin.getConfig().set("smart-unloading.enabled", !current);
            this.plugin.saveConfig();
            GuiUtils.click(this.player);
            this.refresh();
         }
      );
      this.setItem(
         30,
         GuiUtils.action("World Settings", Material.GRASS_BLOCK, ChatColor.GREEN, "Configure per-world", "chunk settings"),
         () -> this.guiManager.openMenu(this.player, new WorldSettingsMenu(this.plugin, this.player, this.guiManager))
      );
      this.setItem(
         32,
         GuiUtils.action("Performance", Material.CLOCK, ChatColor.AQUA, "View performance", "metrics & stats"),
         () -> this.guiManager.openMenu(this.player, new StatsMenu(this.plugin, this.player, this.guiManager))
      );
      this.setItem(34, GuiUtils.action("Reload Config", Material.BOOK, ChatColor.YELLOW, "Reload settings", "from config files"), () -> {
         this.plugin.reloadConfiguration();
         GuiUtils.click(this.player);
         this.player.sendMessage(ChatColor.GREEN + "Configuration reloaded");
         this.refresh();
      });
      this.setItem(38, GuiUtils.action("Features", Material.REDSTONE_TORCH, ChatColor.RED, "Toggle server", "performance features"), () -> {
         GuiUtils.click(this.player);
         this.guiManager.openMenu(this.player, new FeaturesMenu(this.plugin, this.player, this.guiManager));
      });
      this.setItem(47, GuiUtils.action("Optimize Now", Material.GOLDEN_PICKAXE, ChatColor.GOLD, "Force unload", "cold chunks"), () -> {
         this.player.closeInventory();
         Bukkit.dispatchCommand(this.player, "optimizechunks");
      });
      this.setItem(49, GuiUtils.action("Memory Cleanup", Material.EXPERIENCE_BOTTLE, ChatColor.LIGHT_PURPLE, "Request garbage", "collection"), () -> {
         this.player.closeInventory();
         Bukkit.dispatchCommand(this.player, "chunkgc");
      });
      this.setItem(51, GuiUtils.action("Export Metrics", Material.PAPER, ChatColor.WHITE, "Export performance", "data to file"), () -> {
         this.player.closeInventory();
         Bukkit.dispatchCommand(this.player, "chunkmetrics");
      });
      this.setItem(45, GuiUtils.back(), () -> this.guiManager.openMenu(this.player, new MainMenu(this.plugin, this.player, this.guiManager)));
      this.setItem(53, GuiUtils.close(), () -> this.player.closeInventory());
      GuiUtils.applyBorder(this.inv, this.getSize());
   }
}
