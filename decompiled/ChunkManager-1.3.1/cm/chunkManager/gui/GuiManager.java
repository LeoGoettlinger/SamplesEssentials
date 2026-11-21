package cm.chunkManager.gui;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.utils.Scheduler;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GuiManager implements Listener {
   private final ChunkManager plugin;
   private final Map<UUID, Menu> openMenus;
   private final Map<UUID, Scheduler.Handle> updateTasks;
   private final Map<UUID, Long> navigationCooldown;
   private final long cooldownMs;

   public GuiManager(ChunkManager plugin) {
      this.plugin = plugin;
      this.openMenus = new ConcurrentHashMap<>();
      this.updateTasks = new ConcurrentHashMap<>();
      this.navigationCooldown = new ConcurrentHashMap<>();
      this.cooldownMs = plugin.getConfig().getLong("gui.cooldown", 300L);
   }

   public void openMainMenu(Player player) {
      this.openMenu(player, new MainMenu(this.plugin, player, this));
   }

   public void openMenu(Player player, Menu menu) {
      UUID uuid = player.getUniqueId();
      long now = System.currentTimeMillis();
      Long lastNav = this.navigationCooldown.get(uuid);
      if (lastNav == null || now - lastNav >= this.cooldownMs) {
         this.navigationCooldown.put(uuid, now);
         this.stopAutoUpdate(uuid);
         this.openMenus.remove(uuid);
         Scheduler.runGlobalLater(this.plugin, () -> {
            this.openMenus.put(uuid, menu);
            menu.open();
            this.startAutoUpdate(player, menu);
         }, 1L);
      }
   }

   public void closeMenu(Player player) {
      UUID uuid = player.getUniqueId();
      this.openMenus.remove(uuid);
      this.stopAutoUpdate(uuid);
   }

   private void startAutoUpdate(Player player, Menu menu) {
      UUID uuid = player.getUniqueId();
      this.stopAutoUpdate(uuid);
      if (menu instanceof StatsMenu) {
         int interval = this.plugin.getConfig().getInt("gui.update-interval", 20);
         if (interval < 10) {
            interval = 20;
         }

         Scheduler.Handle handle = Scheduler.runGlobalTimer(this.plugin, () -> {
            Menu currentMenu = this.openMenus.get(uuid);
            if (currentMenu instanceof StatsMenu && player.isOnline()) {
               currentMenu.refresh();
            } else {
               this.stopAutoUpdate(uuid);
            }
         }, interval, interval);
         this.updateTasks.put(uuid, handle);
      }
   }

   private void stopAutoUpdate(UUID uuid) {
      Scheduler.Handle handle = this.updateTasks.remove(uuid);
      if (handle != null && !handle.isCancelled()) {
         handle.cancel();
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player player) {
         Menu menu = this.openMenus.get(player.getUniqueId());
         if (menu != null) {
            event.setCancelled(true);
            event.setResult(Result.DENY);
            if (event.getClickedInventory() != null) {
               if (event.getClickedInventory().equals(event.getView().getTopInventory())) {
                  int slot = event.getRawSlot();
                  if (slot >= 0 && slot < menu.getSize()) {
                     menu.handleClick(slot, event.isRightClick());
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onDrag(InventoryDragEvent event) {
      if (event.getWhoClicked() instanceof Player player) {
         if (this.openMenus.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            event.setResult(Result.DENY);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player player) {
         UUID var4 = player.getUniqueId();
         Scheduler.runGlobalLater(this.plugin, () -> {
            if (!this.openMenus.containsKey(var4)) {
               this.stopAutoUpdate(var4);
               this.navigationCooldown.remove(var4);
            }
         }, 2L);
      }
   }

   public void shutdown() {
      this.updateTasks.values().forEach(handle -> {
         if (handle != null && !handle.isCancelled()) {
            handle.cancel();
         }
      });
      this.updateTasks.clear();
      this.openMenus.clear();
      this.navigationCooldown.clear();
   }
}
