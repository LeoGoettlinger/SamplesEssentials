package cm.chunkManager.gui;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.components.ConfigurationManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FeaturesMenu extends Menu {
   public FeaturesMenu(ChunkManager plugin, Player player, GuiManager guiManager) {
      super(plugin, player, guiManager);
   }

   @Override
   public String getTitle() {
      return GuiUtils.title("Feature Toggles");
   }

   @Override
   public int getSize() {
      return 54;
   }

   @Override
   public void build() {
      ConfigurationManager cfg = this.plugin.getConfigManager();
      this.setItem(4, GuiUtils.header("Feature Toggles", "Toggle server features"), null);
      boolean entityLimiter = cfg.isEntityLimiterEnabled();
      this.setItem(10, GuiUtils.toggle("Entity Limiter", entityLimiter, Material.IRON_SWORD, "Auto-cull excess entities", "per chunk"), () -> {
         cfg.saveEntityLimiterEnabled(!entityLimiter);
         GuiUtils.click(this.player);
         this.refresh();
      });
      boolean tpsThrottle = cfg.isTpsThrottleEnabled();
      this.setItem(12, GuiUtils.toggle("TPS Throttle", tpsThrottle, Material.REPEATER, "Throttle operations when", "TPS drops"), () -> {
         cfg.saveTpsThrottleEnabled(!tpsThrottle);
         GuiUtils.click(this.player);
         this.refresh();
      });
      boolean loadPriority = cfg.isLoadPriorityEnabled();
      this.setItem(14, GuiUtils.toggle("Load Priority", loadPriority, Material.HOPPER, "Prioritize chunk loads", "near players"), () -> {
         cfg.saveLoadPriorityEnabled(!loadPriority);
         GuiUtils.click(this.player);
         this.refresh();
      });
      boolean ticketSystem = cfg.isTicketSystemEnabled();
      this.setItem(16, GuiUtils.toggle("Chunk Tickets", ticketSystem, Material.PAPER, "Keep important chunks", "loaded efficiently"), () -> {
         cfg.saveTicketSystemEnabled(!ticketSystem);
         GuiUtils.click(this.player);
         this.refresh();
      });
      boolean teleportPreload = cfg.isTeleportPreloadEnabled();
      this.setItem(20, GuiUtils.toggle("Teleport Preload", teleportPreload, Material.ENDER_PEARL, "Preload chunks at", "teleport destination"), () -> {
         cfg.saveTeleportPreloadEnabled(!teleportPreload);
         GuiUtils.click(this.player);
         this.refresh();
      });
      boolean portalPreload = cfg.isPortalPreloadEnabled();
      this.setItem(22, GuiUtils.toggle("Portal Preload", portalPreload, Material.OBSIDIAN, "Preload nether/end", "portal destinations"), () -> {
         cfg.savePortalPreloadEnabled(!portalPreload);
         GuiUtils.click(this.player);
         this.refresh();
      });
      boolean adaptiveLoading = cfg.isAdaptiveLoadingEnabled();
      this.setItem(24, GuiUtils.toggle("Adaptive Loading", adaptiveLoading, Material.SPYGLASS, "Adjust radius based on", "player ping"), () -> {
         cfg.saveAdaptiveLoadingEnabled(!adaptiveLoading);
         GuiUtils.click(this.player);
         this.refresh();
      });
      boolean geyserDetect = cfg.isGeyserAutoDetect();
      this.setItem(30, GuiUtils.toggle("Geyser Detection", geyserDetect, Material.SCULK_SENSOR, "Detect Bedrock players", "and reduce radius"), () -> {
         cfg.saveGeyserAutoDetect(!geyserDetect);
         GuiUtils.click(this.player);
         this.refresh();
      });
      boolean costTracking = cfg.isChunkCostEnabled();
      this.setItem(32, GuiUtils.toggle("Cost Tracking", costTracking, Material.GOLD_INGOT, "Track chunk performance", "cost for /cmtop"), () -> {
         cfg.saveChunkCostEnabled(!costTracking);
         GuiUtils.click(this.player);
         this.refresh();
      });
      this.setItem(45, GuiUtils.back(), () -> this.guiManager.openMenu(this.player, new SettingsMenu(this.plugin, this.player, this.guiManager)));
      this.setItem(53, GuiUtils.close(), () -> this.player.closeInventory());
      GuiUtils.applyBorder(this.inv, this.getSize());
   }
}
