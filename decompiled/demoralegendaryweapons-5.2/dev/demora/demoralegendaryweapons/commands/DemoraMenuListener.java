package dev.demora.demoralegendaryweapons.commands;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class DemoraMenuListener implements Listener {
   private final DemoraLegendaryWeapons plugin;

   public DemoraMenuListener(DemoraLegendaryWeapons plugin, DemoraMenuCommand menuCommand) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      String title = event.getView().getTitle();
      if (title.equals(DemoraLegendaryWeapons.getInstance().getMessage("commands.menu-legendary-title"))
         || title.equals(DemoraLegendaryWeapons.getInstance().getMessage("commands.menu-rework-title"))) {
         event.setCancelled(true);
         if (!(event.getWhoClicked() instanceof Player)) {
            return;
         }

         Player player = (Player)event.getWhoClicked();
         ItemStack clickedItem = event.getCurrentItem();
         if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
         }

         ItemStack weaponToGive = clickedItem.clone();
         player.getInventory().addItem(new ItemStack[]{weaponToGive});
         String weaponName = clickedItem.getItemMeta().getDisplayName();
         player.sendMessage(this.plugin.getMessageWithPrefix("commands.menu-get-weapon").replace("{weapon}", weaponName));
         player.closeInventory();
      }
   }
}
