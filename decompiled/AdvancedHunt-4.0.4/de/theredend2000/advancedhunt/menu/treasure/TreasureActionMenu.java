package de.theredend2000.advancedhunt.menu.treasure;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.menu.reward.RewardsMenu;
import de.theredend2000.advancedhunt.model.Treasure;
import de.theredend2000.advancedhunt.model.TreasureRewardHolder;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class TreasureActionMenu extends Menu {
   private final UUID treasureId;
   private final Treasure treasure;

   public TreasureActionMenu(Player player, Main plugin, Treasure treasure, Menu previousMenu) {
      super(player, plugin);
      this.treasureId = treasure.getId();
      this.treasure = treasure;
      this.previousMenu = previousMenu;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.treasure_action.title", false);
   }

   @Override
   public int getSlots() {
      return 27;
   }

   @Override
   public void handleMenu(InventoryClickEvent e) {
   }

   @Override
   public void setMenuItems() {
      this.fillBorders(this.FILLER_GLASS);
      ItemStack teleportItem = new ItemBuilder(XMaterial.ENDER_PEARL)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.treasure_action.teleport.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList("gui.treasure_action.teleport.lore", false))
         .build();
      this.addButton(11, teleportItem, e -> {
         Player p = (Player)e.getWhoClicked();
         p.closeInventory();
         this.teleportToTreasure(p, this.treasure);
      }, "advancedhunt.admin.teleport");
      ItemStack viewFindersItem = new ItemBuilder(XMaterial.PLAYER_HEAD)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.treasure_action.view_finders.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList("gui.treasure_action.view_finders.lore", false))
         .setSkullOwner(this.playerMenuUtility.getUniqueId())
         .build();
      this.addButton(13, viewFindersItem, e -> {
         Player p = (Player)e.getWhoClicked();
         new WhoFoundMenu(p, this.plugin, this.treasureId, this).open();
      }, "advancedhunt.admin.view_finders");
      ItemStack rewardsItem = new ItemBuilder(XMaterial.CHEST)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.treasure_action.rewards.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList("gui.treasure_action.rewards.lore", false))
         .build();
      this.addButton(15, rewardsItem, e -> {
         Player p = (Player)e.getWhoClicked();
         new RewardsMenu(p, this.plugin, new TreasureRewardHolder(this.plugin, this.treasure)).setPreviousMenu(this).open();
      }, "advancedhunt.admin.rewards");
   }

   private void teleportToTreasure(Player player, Treasure treasure) {
      Location location = treasure.getLocation();
      if (location != null && location.getWorld() != null) {
         if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            location.getChunk().load();
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.performSafeTeleport(player, location), 1L);
         } else {
            this.performSafeTeleport(player, location);
         }
      } else {
         player.sendMessage(this.plugin.getMessageManager().getMessage("feedback.teleport.invalid_location"));
      }
   }

   private void performSafeTeleport(Player player, Location treasureLocation) {
      Location safeLoc = treasureLocation.clone().add(0.5, 1.5, 0.5);
      safeLoc.setYaw(player.getLocation().getYaw());
      safeLoc.setPitch(player.getLocation().getPitch());
      Location checkLoc = safeLoc.clone();
      Material blockAbove = checkLoc.getBlock().getType();
      Material blockAbove2 = checkLoc.clone().add(0.0, 1.0, 0.0).getBlock().getType();
      if (blockAbove.isSolid() || blockAbove2.isSolid()) {
         Location safeLocation = this.findSafeLocation(treasureLocation);
         if (safeLocation == null) {
            player.sendMessage(this.plugin.getMessageManager().getMessage("feedback.teleport.unsafe"));
            return;
         }

         safeLoc = safeLocation;
         safeLocation.setYaw(player.getLocation().getYaw());
         safeLocation.setPitch(player.getLocation().getPitch());
         player.sendMessage(this.plugin.getMessageManager().getMessage("feedback.teleport.adjusted"));
      }

      player.teleport(safeLoc);
      player.sendMessage(this.plugin.getMessageManager().getMessage("feedback.teleport.success"));
   }

   private Location findSafeLocation(Location center) {
      for (int y = 0; y <= 5; y++) {
         for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
               Location testLoc = center.clone().add(x + 0.5, y + 1.5, z + 0.5);
               Material below = testLoc.clone().subtract(0.0, 1.0, 0.0).getBlock().getType();
               Material at = testLoc.getBlock().getType();
               Material above = testLoc.clone().add(0.0, 1.0, 0.0).getBlock().getType();
               if (below.isSolid() && !at.isSolid() && !above.isSolid()) {
                  return testLoc;
               }
            }
         }
      }

      return null;
   }
}
