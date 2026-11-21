package com.lishid.openinv.util;

import com.google.errorprone.annotations.Keep;
import com.lishid.openinv.OpenInv;
import com.lishid.openinv.event.OpenEvents;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.util.config.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryManager implements Listener {
   private final Map<UUID, ISpecialPlayerInventory> inventories = new ConcurrentHashMap<>();
   private final Map<UUID, ISpecialEnderChest> enderChests = new ConcurrentHashMap<>();
   private final Set<UUID> expectedCloses = ConcurrentHashMap.newKeySet();
   @NotNull
   private final OpenInv plugin;
   @NotNull
   private final Config config;
   @NotNull
   private final InternalAccessor accessor;

   public InventoryManager(@NotNull OpenInv plugin, @NotNull Config config, @NotNull InternalAccessor accessor) {
      this.plugin = plugin;
      this.config = config;
      this.accessor = accessor;
   }

   public void evictAll() {
      Stream.concat(this.inventories.values().stream(), this.enderChests.values().stream()).map(inventory -> {
         if (!this.config.isSaveDisabled() && !OpenEvents.saveCancelled(inventory)) {
            return inventory.getPlayer() instanceof Player player ? player : null;
         } else {
            return null;
         }
      }).filter(Objects::nonNull).distinct().forEach(player -> {
         if (!player.isOnline()) {
            this.accessor.getPlayerDataManager().inject(player).saveData();
         }
      });
      this.inventories.clear();
      this.enderChests.clear();
      this.expectedCloses.clear();
   }

   @NotNull
   public ISpecialPlayerInventory getInventory(@NotNull Player player) {
      return this.inventories.computeIfAbsent(player.getUniqueId(), uuid -> this.accessor.createInventory(player));
   }

   @NotNull
   public ISpecialEnderChest getEnderChest(@NotNull Player player) {
      return this.enderChests.computeIfAbsent(player.getUniqueId(), uuid -> this.accessor.createEnderChest(player));
   }

   @Nullable
   public Player getLoadedPlayer(@NotNull UUID uuid) {
      ISpecialInventory inUse = this.inventories.get(uuid);
      if (inUse != null) {
         return (Player)inUse.getPlayer();
      } else {
         inUse = this.enderChests.get(uuid);
         return inUse != null ? (Player)inUse.getPlayer() : null;
      }
   }

   public void unload(@NotNull UUID uuid) {
      this.inventories.computeIfPresent(uuid, this::remove);
      this.enderChests.computeIfPresent(uuid, this::remove);
   }

   public void save(@NotNull UUID uuid) {
      this.consumeLoaded(uuid, inventory -> {});
   }

   @Keep
   @EventHandler(
      priority = EventPriority.LOWEST
   )
   private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
      this.consumeLoaded(event.getPlayer().getUniqueId(), inventory -> {
         inventory.setPlayerOnline(event.getPlayer());
         this.checkViewerAccess(inventory, true);
      });
   }

   @Keep
   @EventHandler(
      priority = EventPriority.MONITOR
   )
   private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
      this.consumeLoaded(event.getPlayer().getUniqueId(), inventory -> this.checkViewerAccess(inventory, false));
   }

   @Keep
   @EventHandler
   private void onWorldChanged(@NotNull PlayerChangedWorldEvent event) {
      Player player = event.getPlayer();
      this.consumeLoaded(player.getUniqueId(), inventory -> this.checkViewerAccess(inventory, player.isOnline()));
   }

   @Keep
   @EventHandler
   private void onInventoryClose(@NotNull InventoryCloseEvent event) {
      ISpecialInventory inventory = InventoryAccess.getInventory(event.getInventory());
      if (inventory != null && !this.expectedCloses.remove(event.getPlayer().getUniqueId())) {
         Map<UUID, ? extends ISpecialInventory> map = inventory instanceof ISpecialPlayerInventory ? this.inventories : this.enderChests;
         UUID key = inventory.getPlayer().getUniqueId();
         ISpecialInventory loaded = map.get(key);
         if (loaded != null) {
            if (loaded != inventory) {
               map.remove(key);
               this.remove(key, loaded);
               this.remove(key, inventory);
               this.save(loaded);
            }

            this.plugin.getScheduler().runTask(() -> {
               if (!loaded.isInUse()) {
                  ISpecialInventory current = map.remove(key);
                  if (current != null) {
                     this.save(current);
                  }
               }
            });
         }
      }
   }

   @Keep
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   private void onInventoryOpen(@NotNull InventoryOpenEvent event) {
      ISpecialInventory inventory = InventoryAccess.getInventory(event.getInventory());
      if (inventory != null) {
         Map<UUID, ? extends ISpecialInventory> map = inventory instanceof ISpecialPlayerInventory ? this.inventories : this.enderChests;
         UUID key = inventory.getPlayer().getUniqueId();
         ISpecialInventory loaded = map.get(key);
         if (!inventory.equals(loaded)) {
            event.setCancelled(true);
            this.plugin
               .getLogger()
               .log(Level.WARNING, "Prevented a plugin from opening an untracked ISpecialInventory!", new Throwable("Untracked ISpecialInventory"));
         }
      }
   }

   private <T extends ISpecialInventory> void checkViewerAccess(@NotNull T inventory, boolean online) {
      Player owner = (Player)inventory.getPlayer();
      Permissions connectedState = online ? Permissions.ACCESS_ONLINE : Permissions.ACCESS_OFFLINE;
      boolean alwaysDenied = !online && this.config.isOfflineDisabled();

      for (HumanEntity viewer : new ArrayList(inventory.getBukkitInventory().getViewers())) {
         if (alwaysDenied
            || !connectedState.hasPermission(viewer)
            || !Objects.equals(owner.getWorld(), viewer.getWorld()) && !Permissions.ACCESS_CROSSWORLD.hasPermission(viewer)) {
            this.plugin.getScheduler().runTaskAtEntity(viewer, viewer::closeInventory);
         }
      }
   }

   private void consumeLoaded(@NotNull UUID key, @NotNull Consumer<ISpecialInventory> consumer) {
      boolean saved = this.consumeLoaded(this.inventories, key, false, consumer);
      this.consumeLoaded(this.enderChests, key, saved, consumer);
   }

   private <T extends ISpecialInventory> boolean consumeLoaded(
      @NotNull Map<UUID, T> map, @NotNull UUID key, boolean saved, @NotNull Consumer<ISpecialInventory> consumer
   ) {
      T inventory = (T)map.get(key);
      if (inventory == null) {
         return saved;
      } else {
         consumer.accept(inventory);
         if (!inventory.isInUse()) {
            map.remove(key);
            if (!saved) {
               this.save(inventory);
               return true;
            }
         }

         return saved;
      }
   }

   private void save(@NotNull ISpecialInventory inventory) {
      if (!this.config.isSaveDisabled()) {
         Player player = (Player)inventory.getPlayer();
         if (!player.isOnline() && !OpenEvents.saveCancelled(inventory)) {
            this.accessor.getPlayerDataManager().inject(player).saveData();
         }
      }
   }

   @Contract("_, _ -> null")
   @Nullable
   private <T extends ISpecialInventory> T remove(@NotNull UUID key, @NotNull T inventory) {
      for (HumanEntity viewer : List.copyOf(inventory.getBukkitInventory().getViewers())) {
         this.expectedCloses.add(viewer.getUniqueId());
         this.plugin.getScheduler().runTaskAtEntity(viewer, viewer::closeInventory);
      }

      return null;
   }
}
