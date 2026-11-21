package org.sethomegui.Listeners;

import java.util.List;
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

public class MenuClickListener implements Listener {
   private final SetHomeGUI plugin;

   public MenuClickListener(SetHomeGUI plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         String title = event.getView().getTitle();
         String mainTitle = Utils.color(this.plugin.getGuisConfig().getString("gui.main-gui.title", "Menu"));
         if (title.equals(mainTitle)) {
            event.setCancelled(true);
            if (event.getRawSlot() >= 0 && event.getRawSlot() < event.getView().getTopInventory().getSize()) {
               ItemStack clickedItem = event.getCurrentItem();
               if (clickedItem != null && clickedItem.hasItemMeta()) {
                  YamlDocument guiConfig = this.plugin.getGuisConfig();
                  Section mainSection = guiConfig.getSection("gui.main-gui");
                  if (MenuActions.execute(this.plugin, player, mainSection, event.getSlot())) {
                     this.plugin.getGuiManager().playConfiguredClickSound(player, "main-gui");
                  } else {
                     String clickedItemKey = Utils.resolveMenuAction(this.plugin, clickedItem, mainSection, event.getSlot());
                     if (clickedItemKey != null) {
                        switch (clickedItemKey) {
                           case "set_home":
                              this.plugin.getGuiManager().playConfiguredClickSound(player, "main-gui");
                              String currentWorld = player.getWorld().getName();
                              List<String> blacklistedWorlds = this.plugin.getMainConfig().getStringList("blacklisted-worlds");
                              if (blacklistedWorlds != null && blacklistedWorlds.contains(currentWorld)) {
                                 String worldMsg = this.plugin
                                    .getMainConfig()
                                    .getString(
                                       "messages.home-creation-messages.world-blacklisted",
                                       "&#ef6603[SetHomeGUI] &cYou cannot set a home in the world &f%world%&#ef6603!"
                                    );
                                 worldMsg = worldMsg.replace("%world%", "&f" + currentWorld);
                                 player.sendMessage(Utils.setPlaceholders(player, worldMsg, this.plugin));
                                 player.closeInventory();
                                 return;
                              }

                              int currentHomes = this.plugin.getHomeManager().getHomeCount(player.getUniqueId());
                              int maxHomes = this.plugin.getHomeManager().getPlayerMaxHomes(player);
                              if (maxHomes != -1 && currentHomes >= maxHomes) {
                                 String limitMsg = this.plugin
                                    .getMainConfig()
                                    .getString(
                                       "messages.home-creation-messages.limit-reached",
                                       "&#ef6603[SetHomeGUI] &cYou have reached your maximum limit of &f%max% &#ef6603homes!"
                                    );
                                 limitMsg = limitMsg.replace("%max%", "&f" + maxHomes);
                                 player.sendMessage(Utils.setPlaceholders(player, limitMsg, this.plugin));
                                 player.closeInventory();
                                 return;
                              }

                              player.closeInventory();
                              String basePath = "messages.home-creation-messages.";
                              String cancelWord = this.plugin.getMainConfig().getString(basePath + "cancel-word", "cancel");
                              String promptMsg = this.plugin
                                 .getMainConfig()
                                 .getString(
                                    basePath + "name-prompt",
                                    "&#ef6603[SetHomeGUI] &#f9a805Please type the name for your home in the chat... Or type &f%cancel_word% &#f9a805to exit."
                                 );
                              promptMsg = promptMsg.replace("%cancel_word%", "&f" + cancelWord);
                              player.sendMessage(Utils.setPlaceholders(player, promptMsg, this.plugin));
                              ChatPromptListener.startPrompt(player);
                              break;
                           case "my_homes":
                              this.plugin.getGuiManager().playConfiguredClickSound(player, "main-gui");
                              player.closeInventory();
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
