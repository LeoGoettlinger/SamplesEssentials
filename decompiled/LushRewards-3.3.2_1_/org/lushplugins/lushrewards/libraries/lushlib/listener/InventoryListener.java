package org.lushplugins.lushrewards.libraries.lushlib.listener;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.lushplugins.lushrewards.libraries.lushlib.LushLib;
import org.lushplugins.lushrewards.libraries.lushlib.gui.inventory.Gui;
import org.lushplugins.lushrewards.libraries.lushlib.manager.GuiManager;

public class InventoryListener implements Listener {
   @EventHandler
   public void onInventoryOpen(InventoryOpenEvent event) {
      Player player = (Player)event.getPlayer();
      LushLib.getInstance().getPlugin().getManager(GuiManager.class).ifPresent(guiManager -> {
         Gui gui = guiManager.getGui(player.getUniqueId());
         if (gui != null && event.getInventory().equals(gui.getInventory())) {
            gui.onOpen(event);
         }
      });
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      Player player = (Player)event.getPlayer();
      LushLib.getInstance().getPlugin().getManager(GuiManager.class).ifPresent(guiManager -> {
         Gui gui = guiManager.getGui(player.getUniqueId());
         if (gui != null && event.getInventory().equals(gui.getInventory())) {
            gui.onClose(event);
         }
      });
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      Player player = (Player)event.getWhoClicked();
      LushLib.getInstance().getPlugin().getManager(GuiManager.class).ifPresent(guiManager -> {
         Gui gui = guiManager.getGui(player.getUniqueId());
         if (gui != null) {
            Inventory clickedInventory = event.getClickedInventory();
            if (clickedInventory != null && player.getOpenInventory().getTopInventory().equals(gui.getInventory())) {
               gui.onClick(event);
            }
         }
      });
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      Player player = (Player)event.getWhoClicked();
      UUID playerUUID = player.getUniqueId();
      LushLib.getInstance().getPlugin().getManager(GuiManager.class).ifPresent(guiManager -> {
         Gui gui = guiManager.getGui(playerUUID);
         if (gui != null && player.getOpenInventory().getTopInventory().equals(gui.getInventory())) {
            gui.onDrag(event);
         }
      });
   }
}
