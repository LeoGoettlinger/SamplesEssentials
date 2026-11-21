package com.Sw3ys.bigenderchest.manager;

import com.Sw3ys.bigenderchest.BiggerEnderChest;
import com.Sw3ys.bigenderchest.gui.BiggerEnderHolder;
import com.Sw3ys.bigenderchest.storage.ChestStore;
import com.Sw3ys.bigenderchest.util.Sched;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EnderChestManager {
   public static final int VANILLA_SIZE = 27;
   private final BiggerEnderChest plugin;
   private final ChestStore store;
   private final Map<UUID, UUID> viewing = new HashMap<>();
   private final Map<UUID, Inventory> openInventories = new HashMap<>();

   public EnderChestManager(BiggerEnderChest plugin, ChestStore store) {
      this.plugin = plugin;
      this.store = store;
   }

   public ChestStore getStore() {
      return this.store;
   }

   public void open(Player viewer, UUID target) {
      if (this.store.isAsync() && !this.openInventories.containsKey(target)) {
         this.store.loadAsync(target, existed -> Sched.entity(this.plugin, viewer, () -> {
            if (viewer.isOnline()) {
               this.doOpen(viewer, target);
            }
         }));
      } else {
         this.doOpen(viewer, target);
      }
   }

   private void doOpen(Player viewer, UUID target) {
      if (this.viewing.containsKey(viewer.getUniqueId()) || viewer.getOpenInventory().getTopInventory().getHolder() instanceof BiggerEnderHolder) {
         viewer.closeInventory();
      }

      Inventory inv = this.openInventories.get(target);
      if (inv == null) {
         Player targetOnline = Bukkit.getPlayer(target);
         int size = this.getSize(target, targetOnline);
         BiggerEnderHolder holder = new BiggerEnderHolder(target);
         inv = Bukkit.createInventory(holder, size, this.title());
         holder.setInventory(inv);
         this.fill(target, targetOnline, inv);
         this.openInventories.put(target, inv);
      }

      this.viewing.put(viewer.getUniqueId(), target);
      viewer.openInventory(inv);
      this.playOpenSound(viewer);
      Sched.entityLater(this.plugin, viewer, viewer::updateInventory, 1L);
   }

   private void fill(UUID target, Player targetOnline, Inventory inv) {
      ChestStore.Stored data = this.store.read(target);
      int size = inv.getSize();
      if (targetOnline != null) {
         ItemStack[] vanilla = targetOnline.getEnderChest().getContents();

         for (int i = 0; i < size; i++) {
            if (i < 27) {
               inv.setItem(i, i < vanilla.length ? vanilla[i] : null);
            } else {
               inv.setItem(i, data != null && i < data.items.length ? data.items[i] : null);
            }
         }
      } else {
         for (int ix = 0; ix < size; ix++) {
            inv.setItem(ix, data != null && ix < data.items.length ? data.items[ix] : null);
         }
      }
   }

   private int getSize(UUID uuid, Player online) {
      if (online != null) {
         if (online.hasPermission("enderchest.slot.54")) {
            return 54;
         } else if (online.hasPermission("enderchest.slot.45")) {
            return 45;
         } else {
            return online.hasPermission("enderchest.slot.36") ? 36 : 27;
         }
      } else {
         return this.store.getSavedSize(uuid);
      }
   }

   public void save(Player viewer) {
      UUID target = this.viewing.get(viewer.getUniqueId());
      if (target != null) {
         Inventory inv = this.openInventories.get(target);
         if (inv != null) {
            this.writeInventory(target, inv);
         }
      }
   }

   private void writeInventory(UUID target, Inventory inv) {
      Player targetOnline = Bukkit.getPlayer(target);
      int size = inv.getSize();
      ItemStack[] items = new ItemStack[size];

      for (int i = 0; i < size; i++) {
         items[i] = inv.getItem(i);
      }

      if (targetOnline != null) {
         ItemStack[] vanilla = new ItemStack[27];

         for (int i = 0; i < 27 && i < size; i++) {
            vanilla[i] = inv.getItem(i);
         }

         targetOnline.getEnderChest().setContents(vanilla);
         this.store.write(target, size, false, items);
      } else {
         this.store.write(target, size, true, items);
      }
   }

   public void close(Player viewer) {
      UUID target = this.viewing.remove(viewer.getUniqueId());
      if (target != null) {
         if (!this.viewing.containsValue(target)) {
            Inventory inv = this.openInventories.remove(target);
            if (inv != null) {
               this.writeInventory(target, inv);
            }

            if (Bukkit.getPlayer(target) == null) {
               this.store.unload(target);
            }
         }
      }
   }

   public void flushAll() {
      for (Entry<UUID, Inventory> entry : this.openInventories.entrySet()) {
         this.writeInventory(entry.getKey(), entry.getValue());
      }
   }

   public void reconcileJoin(Player player) {
      this.store.loadAsync(player.getUniqueId(), existed -> Sched.entity(this.plugin, player, () -> {
         if (player.isOnline()) {
            this.applyJoin(player, existed);
         }
      }));
   }

   private void applyJoin(Player player, boolean existed) {
      UUID uuid = player.getUniqueId();
      if (!existed) {
         if (this.store.isAuthoritative()) {
            this.snapshotToStore(player);
         }
      } else {
         ChestStore.Stored data = this.store.read(uuid);
         if (data != null) {
            if (this.store.isAuthoritative()) {
               this.setVanilla(player, data);
            } else if (data.pending) {
               this.setVanilla(player, data);
               this.store.write(uuid, data.size, false, data.items);
            } else {
               this.refreshMirror(player, data);
            }
         }
      }
   }

   public void snapshotQuit(Player player) {
      UUID uuid = player.getUniqueId();
      ItemStack[] vanilla = player.getEnderChest().getContents();
      boolean existed = this.store.exists(uuid);
      if (!existed && this.isEmpty(vanilla)) {
         this.store.unload(uuid);
      } else {
         ChestStore.Stored data = existed ? this.store.read(uuid) : null;
         if (data == null) {
            data = new ChestStore.Stored(27, false, new ItemStack[27]);
         }

         int size = data.size;
         ItemStack[] items = new ItemStack[size];

         for (int i = 0; i < size; i++) {
            items[i] = i < data.items.length ? data.items[i] : null;
         }

         for (int i = 0; i < 27 && i < size; i++) {
            items[i] = i < vanilla.length ? vanilla[i] : null;
         }

         this.store.write(uuid, size, false, items);
         this.store.unload(uuid);
      }
   }

   private void snapshotToStore(Player player) {
      UUID uuid = player.getUniqueId();
      int size = this.getSize(uuid, player);
      ItemStack[] vanilla = player.getEnderChest().getContents();
      ItemStack[] items = new ItemStack[size];

      for (int i = 0; i < 27 && i < size; i++) {
         items[i] = i < vanilla.length ? vanilla[i] : null;
      }

      this.store.write(uuid, size, false, items);
   }

   private void setVanilla(Player player, ChestStore.Stored data) {
      ItemStack[] vanilla = new ItemStack[27];

      for (int i = 0; i < 27 && i < data.items.length; i++) {
         vanilla[i] = data.items[i];
      }

      player.getEnderChest().setContents(vanilla);
   }

   private void refreshMirror(Player player, ChestStore.Stored data) {
      ItemStack[] vanilla = player.getEnderChest().getContents();
      ItemStack[] items = (ItemStack[])data.items.clone();

      for (int i = 0; i < 27 && i < items.length; i++) {
         items[i] = i < vanilla.length ? vanilla[i] : null;
      }

      this.store.write(player.getUniqueId(), data.size, false, items);
   }

   private boolean isEmpty(ItemStack[] items) {
      for (ItemStack item : items) {
         if (item != null && !item.getType().isAir()) {
            return false;
         }
      }

      return true;
   }

   private Component title() {
      String raw = this.plugin.getConfig().getString("gui.enderchest-title", "&5Ender Chest");
      return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
   }

   private void playOpenSound(Player viewer) {
      if (this.plugin.getConfig().getBoolean("sounds.enabled", true)) {
         viewer.playSound(viewer.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0F, 1.0F);
      }
   }
}
