package org.sethomegui.Listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.Utils.MenuActions;
import org.sethomegui.Utils.Utils;

public class HomesMenuClickListener implements Listener {
   private final SetHomeGUI plugin;

   public HomesMenuClickListener(SetHomeGUI plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         UUID uuid = player.getUniqueId();
         String title = event.getView().getTitle();
         int currentPage = this.plugin.getGuiManager().getPlayerPage(uuid);
         YamlDocument guiConfig = this.plugin.getGuisConfig();
         Section homesSection = guiConfig.getSection("gui.homes-gui");
         if (homesSection != null) {
            YamlDocument playerFile = this.plugin.getHomeManager().getPlayerFile(uuid);
            List<String> rawHomesList = (List<String>)(playerFile != null ? playerFile.getStringList("homes") : new ArrayList<>());
            if (rawHomesList == null) {
               rawHomesList = new ArrayList<>();
            }

            List<Integer> homeSlots = homesSection.getIntList("home-slots");
            int homesPerPage = homeSlots.size();
            int maxPages = (int)Math.ceil((double)rawHomesList.size() / homesPerPage);
            if (maxPages == 0) {
               maxPages = 1;
            }

            String expectedTitle = homesSection.getString("title", "")
               .replace("%page%", String.valueOf(currentPage))
               .replace("%max_page%", String.valueOf(maxPages));
            expectedTitle = Utils.setPlaceholders(player, expectedTitle, this.plugin);
            if (title.equals(expectedTitle)) {
               event.setCancelled(true);
               if (event.getRawSlot() >= 0 && event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                  ItemStack clickedItem = event.getCurrentItem();
                  if (clickedItem != null && clickedItem.hasItemMeta()) {
                     int clickedSlot = event.getSlot();
                     if (MenuActions.execute(this.plugin, player, homesSection, clickedSlot)) {
                        this.plugin.getGuiManager().playConfiguredClickSound(player, "homes-gui");
                     } else {
                        String action = Utils.resolveMenuAction(this.plugin, clickedItem, homesSection, clickedSlot);
                        if (action != null) {
                           switch (action) {
                              case "back":
                                 this.plugin.getGuiManager().playConfiguredClickSound(player, "homes-gui");
                                 player.closeInventory();
                                 this.plugin.getGuiManager().openMainGUI(player);
                                 return;
                              case "previous_page":
                                 this.plugin.getGuiManager().playConfiguredClickSound(player, "homes-gui");
                                 if (currentPage > 1) {
                                    this.plugin.getGuiManager().setPlayerPage(uuid, currentPage - 1);
                                    this.plugin.getGuiManager().openHomesGUI(player);
                                 }

                                 return;
                              case "next_page":
                                 this.plugin.getGuiManager().playConfiguredClickSound(player, "homes-gui");
                                 if (currentPage < maxPages) {
                                    this.plugin.getGuiManager().setPlayerPage(uuid, currentPage + 1);
                                    this.plugin.getGuiManager().openHomesGUI(player);
                                 }

                                 return;
                           }
                        }

                        if (homeSlots.contains(clickedSlot)) {
                           int slotIndexInPage = homeSlots.indexOf(clickedSlot);
                           int globalHomeIndex = (currentPage - 1) * homesPerPage + slotIndexInPage;
                           if (globalHomeIndex >= rawHomesList.size()) {
                              return;
                           }

                           String homeName = rawHomesList.get(globalHomeIndex);
                           this.plugin.getGuiManager().playConfiguredClickSound(player, "homes-gui");
                           if (event.getClick() == ClickType.LEFT) {
                              player.closeInventory();
                              String worldName = playerFile.getString(homeName + ".world");
                              World world = Bukkit.getWorld(worldName);
                              if (world == null) {
                                 String worldNotLoadedMsg = this.plugin
                                    .getMainConfig()
                                    .getString(
                                       "messages.home-action-messages.world-not-loaded",
                                       "&#ef6603[SetHomeGUI] &cError: Destination world '%world%' is not loaded."
                                    );
                                 player.sendMessage(Utils.color(worldNotLoadedMsg.replace("%world%", worldName)));
                                 return;
                              }

                              double x = playerFile.getDouble(homeName + ".x");
                              double y = playerFile.getDouble(homeName + ".y");
                              double z = playerFile.getDouble(homeName + ".z");
                              float yaw = playerFile.getDouble(homeName + ".yaw").floatValue();
                              float pitch = playerFile.getDouble(homeName + ".pitch").floatValue();
                              Location targetLoc = new Location(world, x, y, z, yaw, pitch);
                              this.plugin.getTeleportManager().queueTeleport(player, targetLoc);
                           } else if (event.getClick() == ClickType.RIGHT) {
                              this.plugin.getGuiManager().playConfiguredClickSound(player, "homes-gui");
                              this.plugin.getGuiManager().openConfirmationGUI(player, homeName);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
