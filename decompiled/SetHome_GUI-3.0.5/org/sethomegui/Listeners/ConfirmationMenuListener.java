package org.sethomegui.Listeners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.Utils.MenuActions;
import org.sethomegui.Utils.Utils;

public class ConfirmationMenuListener implements Listener {
   private final SetHomeGUI plugin;

   public ConfirmationMenuListener(SetHomeGUI plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         UUID uuid = player.getUniqueId();
         String homeName = this.plugin.getGuiManager().getPendingDeletionHome(uuid);
         if (homeName != null) {
            YamlDocument guiConfig = this.plugin.getGuisConfig();
            Section confirmSection = guiConfig.getSection("gui.confirmation-gui");
            if (confirmSection != null) {
               String expectedTitle = confirmSection.getString("title", "").replace("%home_name%", homeName);
               expectedTitle = Utils.setPlaceholders(player, expectedTitle, this.plugin);
               if (event.getView().getTitle().equals(expectedTitle)) {
                  event.setCancelled(true);
                  if (event.getRawSlot() >= 0 && event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                     ItemStack clickedItem = event.getCurrentItem();
                     if (clickedItem != null && clickedItem.hasItemMeta()) {
                        if (MenuActions.execute(this.plugin, player, confirmSection, event.getSlot())) {
                           this.plugin.getGuiManager().playConfiguredClickSound(player, "confirmation-gui");
                        } else {
                           String clickedKey = Utils.resolveMenuAction(this.plugin, clickedItem, confirmSection, event.getSlot());
                           if (clickedKey != null) {
                              if (clickedKey.equals("confirm_button") || clickedKey.equals("cancel_button")) {
                                 this.plugin.getGuiManager().playConfiguredClickSound(player, "confirmation-gui");
                                 if (clickedKey.equals("confirm_button")) {
                                    player.closeInventory();
                                    this.plugin.getGuiManager().clearPendingDeletion(uuid);
                                    YamlDocument playerFile = this.plugin.getHomeManager().getPlayerFile(uuid);
                                    List<String> rawHomesList = (List<String>)(playerFile != null ? playerFile.getStringList("homes") : new ArrayList<>());
                                    if (rawHomesList == null) {
                                       rawHomesList = new ArrayList<>();
                                    }

                                    if (rawHomesList.contains(homeName)) {
                                       rawHomesList.remove(homeName);
                                       playerFile.set("homes", rawHomesList);
                                       playerFile.remove(homeName);

                                       try {
                                          playerFile.save();
                                          if (this.plugin.getAdminGUIManager() != null) {
                                             this.plugin.getAdminGUIManager().removePlayerFromCacheIfEmpty(player);
                                          }

                                          String deleteMsg = this.plugin
                                             .getMainConfig()
                                             .getString(
                                                "messages.home-action-messages.home-deleted",
                                                "&#ef6603[SetHomeGUI] &#f9a805Home &f%name% &#f9a805has been successfully deleted."
                                             );
                                          player.sendMessage(Utils.setPlaceholders(player, deleteMsg.replace("%name%", homeName), this.plugin));
                                       } catch (IOException var14) {
                                          String deletionErrorMsg = this.plugin
                                             .getMainConfig()
                                             .getString(
                                                "messages.home-action-messages.file-deletion-error",
                                                "&#ef6603[SetHomeGUI] &cAn error occurred while deleting the file."
                                             );
                                          player.sendMessage(Utils.color(deletionErrorMsg));
                                          var14.printStackTrace();
                                       }
                                    }
                                 } else {
                                    player.closeInventory();
                                    this.plugin.getGuiManager().clearPendingDeletion(uuid);
                                    this.plugin.getGuiManager().openHomesGUI(player);
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
   }
}
