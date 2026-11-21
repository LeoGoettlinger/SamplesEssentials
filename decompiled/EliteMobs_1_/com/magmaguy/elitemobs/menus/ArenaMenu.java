package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.ArenasConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.menus.premade.ArenaMenuConfig;
import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class ArenaMenu {
   public void constructArenaMenu(Player player, String arenaFilename) {
      ArenaInstance arenaInstance = ArenaInstance.getArenaInstances().get(arenaFilename);
      if (arenaInstance == null) {
         player.sendMessage(ArenaMenuConfig.getInvalidArenaMessage());
      } else {
         String menuName = ArenaMenuConfig.getMenuName() + arenaInstance.getCustomArenasConfigFields().getArenaName();
         if (DefaultConfig.useResourcePackModels()) {
            menuName = "\udb83\udef1\udb83\ude0a\udb83\udef5          " + menuName;
         }

         Inventory shopInventory = Bukkit.createInventory(player, 9, menuName);
         shopInventory.setItem(ArenaMenuConfig.getPlayerItemSlot(), ArenaMenuConfig.getPlayerItem());
         if (DungeonsConfig.isAllowSpectatorsInInstancedContent()) {
            shopInventory.setItem(ArenaMenuConfig.getSpectatorItemSlot(), ArenaMenuConfig.getSpectatorItem());
         }

         player.openInventory(shopInventory);
         ArenaMenu.ArenaMenuEvents.menus.put(shopInventory, new ArenaMenu.MenuContainer(shopInventory, arenaInstance));
      }
   }

   public static class ArenaMenuEvents implements Listener {
      private static final Map<Inventory, ArenaMenu.MenuContainer> menus = new HashMap<>();

      public static void shutdown() {
         menus.clear();
      }

      @EventHandler
      public void onInventoryClick(InventoryClickEvent event) {
         Player player = ((Player)event.getWhoClicked()).getPlayer();
         if (menus.containsKey(event.getInventory())) {
            event.setCancelled(true);
            if (event.getSlot() == ArenaMenuConfig.getPlayerItemSlot()) {
               ArenaInstance arenaInstance = menus.get(event.getInventory()).getArenaInstance();
               if (arenaInstance.getCustomArenasConfigFields().getPermission() != null
                  && !arenaInstance.getCustomArenasConfigFields().getPermission().isEmpty()
                  && !player.hasPermission(arenaInstance.getCustomArenasConfigFields().getPermission())) {
                  player.sendMessage(ArenasConfig.getNoArenaPermissionMessage());
                  player.closeInventory();
               } else {
                  menus.get(event.getInventory()).getArenaInstance().addNewPlayer(player);
                  player.closeInventory();
               }
            } else {
               if (event.getSlot() == ArenaMenuConfig.getSpectatorItemSlot() && DungeonsConfig.isAllowSpectatorsInInstancedContent()) {
                  menus.get(event.getInventory()).getArenaInstance().addSpectator(player, false);
                  player.closeInventory();
               }
            }
         }
      }

      @EventHandler
      public void onInventoryClose(InventoryCloseEvent event) {
         menus.remove(event.getInventory());
      }
   }

   private class MenuContainer {
      private final Inventory inventory;
      private final ArenaInstance arenaInstance;

      private MenuContainer(Inventory inventory, ArenaInstance arenaInstance) {
         this.inventory = inventory;
         this.arenaInstance = arenaInstance;
      }

      @Generated
      public Inventory getInventory() {
         return this.inventory;
      }

      @Generated
      public ArenaInstance getArenaInstance() {
         return this.arenaInstance;
      }
   }
}
