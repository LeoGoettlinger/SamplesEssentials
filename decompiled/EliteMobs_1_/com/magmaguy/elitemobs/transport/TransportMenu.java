package com.magmaguy.elitemobs.transport;

import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

final class TransportMenu implements Listener, AutoCloseable {
   private final TransportModule module;
   private final Plugin plugin;
   private final Set<Inventory> opened = new HashSet<>();

   TransportMenu(TransportModule module, Plugin plugin) {
      this.module = module;
      this.plugin = plugin;
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   void open(Player player, NPCEntity npc) {
      List<String> routes = npc.getNPCsConfigFields().getTransportRoutes().stream().filter(id -> this.module.files.get(id) != null).distinct().toList();
      if (!routes.isEmpty() && routes.size() <= 54) {
         TransportMenu.Choice choice = new TransportMenu.Choice(player.getUniqueId(), npc, routes);
         choice.inventory = Bukkit.createInventory(choice, (routes.size() + 8) / 9 * 9, "Travel destinations");

         for (int slot = 0; slot < routes.size(); slot++) {
            TransportRoute route = this.module.files.get(routes.get(slot));
            ItemStack item = new ItemStack(Material.FEATHER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColorConverter.convert("&6" + route.name()));
            meta.setLore(List.of(ChatColorConverter.convert("&7Click to board")));
            item.setItemMeta(meta);
            choice.inventory.setItem(slot, item);
         }

         this.opened.add(choice.inventory);
         player.openInventory(choice.inventory);
      } else {
         TransportModule.message(player, "This attendant needs between 1 and 54 configured destinations.");
      }
   }

   @EventHandler
   public void click(InventoryClickEvent event) {
      if (event.getView().getTopInventory().getHolder() instanceof TransportMenu.Choice choice) {
         event.setCancelled(true);
         if (event.getWhoClicked() instanceof Player player && choice.player.equals(player.getUniqueId())) {
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < choice.routes.size()) {
               Bukkit.getScheduler()
                  .runTask(
                     this.plugin,
                     () -> {
                        if (player.isOnline() && player.getOpenInventory().getTopInventory() == choice.inventory) {
                           player.closeInventory();
                           Villager villager = choice.npc.getVillager();
                           if (villager != null
                              && villager.isValid()
                              && villager.getWorld() == player.getWorld()
                              && !(villager.getLocation().distanceSquared(player.getLocation()) > 64.0)) {
                              String route = choice.routes.get(slot);
                              if (choice.npc.getNPCsConfigFields().getTransportRoutes().contains(route)) {
                                 TransportModule.startRoute(player, route);
                              }
                           }
                        }
                     }
                  );
            }
         }
      }
   }

   @EventHandler
   public void drag(InventoryDragEvent event) {
      if (event.getView().getTopInventory().getHolder() instanceof TransportMenu.Choice) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void inventoryClosed(InventoryCloseEvent event) {
      this.opened.remove(event.getInventory());
   }

   @Override
   public void close() {
      for (Inventory inventory : List.copyOf(this.opened)) {
         for (HumanEntity viewer : List.copyOf(inventory.getViewers())) {
            viewer.closeInventory();
         }
      }

      this.opened.clear();
      HandlerList.unregisterAll(this);
   }

   private static final class Choice implements InventoryHolder {
      final UUID player;
      final NPCEntity npc;
      final List<String> routes;
      Inventory inventory;

      Choice(UUID player, NPCEntity npc, List<String> routes) {
         this.player = player;
         this.npc = npc;
         this.routes = routes;
      }

      public Inventory getInventory() {
         return this.inventory;
      }
   }
}
