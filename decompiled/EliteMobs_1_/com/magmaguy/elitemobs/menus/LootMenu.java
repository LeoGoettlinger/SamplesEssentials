package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.menus.premade.LootMenuConfig;
import com.magmaguy.elitemobs.items.customloottable.SharedLootTable;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LootMenu extends EliteMenu {
   private static final Map<UUID, List<LootMenu>> playerLoot = new HashMap<>();
   private static final Map<UUID, Integer> nextSessionIndex = new HashMap<>();
   private static final List<Integer> glassSlots = List.of(4, 13, 22, 31, 40, 49);
   private static final int previousPageSlot = 0;
   private static final int greedInfo = 2;
   private static final int pageIndicatorSlot = 4;
   private static final int needInfo = 6;
   private static final int nextPageSlot = 8;
   private static final List<Integer> greedSlots = List.of(9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48);
   private static final List<Integer> needSlots = List.of(14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35, 41, 42, 43, 44, 50, 51, 52, 53);
   private static final int entriesPerPage = 20;
   private final SharedLootTable sharedLootTable;
   private final SharedLootTable.PlayerTable playerTable;
   private final Inventory inventory;
   private final Player player;
   private final Map<Integer, SharedLootTable.LootRollEntry> displayedEntries = new HashMap<>();
   private int page;

   public LootMenu(Player player, SharedLootTable sharedLootTable, SharedLootTable.PlayerTable playerTable) {
      this.inventory = Bukkit.createInventory(player, 54);
      this.sharedLootTable = sharedLootTable;
      this.playerTable = playerTable;
      this.player = player;

      for (int i : glassSlots) {
         this.inventory.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
      }

      this.inventory
         .setItem(
            2,
            ItemStackGenerator.generateItemStack(
               Material.ORANGE_STAINED_GLASS_PANE,
               LootMenuConfig.getGreedListTitle(),
               List.of(LootMenuConfig.getGreedListLore1(), LootMenuConfig.getGreedListLore2(), LootMenuConfig.getGreedListLore3())
            )
         );
      this.inventory
         .setItem(
            6,
            ItemStackGenerator.generateItemStack(
               Material.GREEN_STAINED_GLASS_PANE,
               LootMenuConfig.getNeedListTitle(),
               List.of(LootMenuConfig.getNeedListLore1(), LootMenuConfig.getNeedListLore2(), LootMenuConfig.getNeedListLore3())
            )
         );
      List<LootMenu> sessions = playerLoot.computeIfAbsent(player.getUniqueId(), ignored -> new ArrayList<>());
      sessions.add(this);
      nextSessionIndex.put(player.getUniqueId(), sessions.size() - 1);
   }

   public static void shutdown() {
      for (List<LootMenu> sessions : new ArrayList<>(playerLoot.values())) {
         for (LootMenu menu : new ArrayList<>(sessions)) {
            menu.removeMenu();
         }
      }

      playerLoot.clear();
      nextSessionIndex.clear();
   }

   public static HashMap<UUID, LootMenu> getPlayerLoot() {
      HashMap<UUID, LootMenu> latest = new HashMap<>();
      playerLoot.forEach((playerId, sessions) -> {
         if (!sessions.isEmpty()) {
            latest.put(playerId, sessions.get(sessions.size() - 1));
         }
      });
      return latest;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public static void openMenu(Player player) {
      List<LootMenu> sessions = playerLoot.get(player.getUniqueId());
      if (sessions != null && !sessions.isEmpty()) {
         int index = Math.min(nextSessionIndex.getOrDefault(player.getUniqueId(), sessions.size() - 1), sessions.size() - 1);
         LootMenu lootMenu = sessions.get(index);
         nextSessionIndex.put(player.getUniqueId(), index == 0 ? sessions.size() - 1 : index - 1);
         lootMenu.open();
      } else {
         player.sendMessage(LootMenuConfig.getNoGroupLootMessage());
      }
   }

   private void open() {
      this.player.openInventory(this.renderMenu());
   }

   private Inventory renderMenu() {
      greedSlots.forEach(slotx -> this.inventory.setItem(slotx, null));
      needSlots.forEach(slotx -> this.inventory.setItem(slotx, null));
      this.displayedEntries.clear();
      List<SharedLootTable.LootRollEntry> entries = this.sharedLootTable.getLoot(this.player);
      int totalPages = Math.max(1, (entries.size() + 20 - 1) / 20);
      this.page = Math.max(0, Math.min(this.page, totalPages - 1));
      int fromIndex = this.page * 20;
      int toIndex = Math.min(entries.size(), fromIndex + 20);
      int greedIndex = 0;
      int needIndex = 0;

      for (SharedLootTable.LootRollEntry entry : entries.subList(fromIndex, toIndex)) {
         int slot;
         if (this.playerTable.needs(entry.id())) {
            slot = needSlots.get(needIndex++);
         } else {
            slot = greedSlots.get(greedIndex++);
         }

         this.inventory.setItem(slot, entry.itemStack());
         this.displayedEntries.put(slot, entry);
      }

      this.inventory
         .setItem(
            4,
            ItemStackGenerator.generateItemStack(
               Material.PAPER,
               LootMenuConfig.getPageIndicatorTitle().replace("$current", String.valueOf(this.page + 1)).replace("$total", String.valueOf(totalPages))
            )
         );
      this.inventory.setItem(0, this.page > 0 ? ItemStackGenerator.generateItemStack(Material.ARROW, LootMenuConfig.getPreviousPageTitle()) : null);
      this.inventory.setItem(8, this.page + 1 < totalPages ? ItemStackGenerator.generateItemStack(Material.ARROW, LootMenuConfig.getNextPageTitle()) : null);
      return this.inventory;
   }

   public void removeMenu() {
      if (this.player.isOnline() && this.player.getOpenInventory().getTopInventory().equals(this.inventory)) {
         this.player.closeInventory();
      }

      List<LootMenu> sessions = playerLoot.get(this.player.getUniqueId());
      if (sessions != null) {
         sessions.remove(this);
         if (sessions.isEmpty()) {
            playerLoot.remove(this.player.getUniqueId());
            nextSessionIndex.remove(this.player.getUniqueId());
         } else {
            nextSessionIndex.put(this.player.getUniqueId(), sessions.size() - 1);
         }
      }
   }

   private static LootMenu findOpenMenu(Player player, Inventory inventory) {
      List<LootMenu> sessions = playerLoot.get(player.getUniqueId());
      if (sessions == null) {
         return null;
      } else {
         for (LootMenu session : sessions) {
            if (session.inventory.equals(inventory)) {
               return session;
            }
         }

         return null;
      }
   }

   public static class LootMenuEvents implements Listener {
      @EventHandler
      public void onInventoryInteract(InventoryClickEvent event) {
         if (event.getWhoClicked() instanceof Player player) {
            LootMenu lootMenu = LootMenu.findOpenMenu(player, event.getInventory());
            if (lootMenu != null) {
               event.setCancelled(true);
               if (!EliteMenu.isBottomMenu(event)) {
                  int slot = event.getSlot();
                  if (slot == 0 && lootMenu.page > 0) {
                     lootMenu.page--;
                     lootMenu.open();
                  } else {
                     int totalEntries = lootMenu.sharedLootTable.getLoot(player).size();
                     if (slot == 8 && (lootMenu.page + 1) * 20 < totalEntries) {
                        lootMenu.page++;
                        lootMenu.open();
                     } else {
                        SharedLootTable.LootRollEntry entry = lootMenu.displayedEntries.get(slot);
                        if (entry != null) {
                           if (LootMenu.greedSlots.contains(slot)) {
                              lootMenu.playerTable.setNeed(entry, true);
                           } else if (LootMenu.needSlots.contains(slot)) {
                              lootMenu.playerTable.setNeed(entry, false);
                           }

                           lootMenu.open();
                        }
                     }
                  }
               }
            }
         }
      }

      @EventHandler
      public void onPlayerQuit(PlayerQuitEvent event) {
         SharedLootTable.onPlayerQuit(event.getPlayer().getUniqueId());
         LootMenu.playerLoot.remove(event.getPlayer().getUniqueId());
         LootMenu.nextSessionIndex.remove(event.getPlayer().getUniqueId());
      }
   }
}
