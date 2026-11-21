package dev.cwhead.GravesX.api.grave;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.exception.GravesXIllegalArgumentException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraveManagementAPI {
   private final Graves plugin;

   public GraveManagementAPI(Graves plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
   }

   public void removeGrave(@NotNull Grave grave) {
      this.plugin.getGraveManager().removeGrave(grave);
   }

   public void breakGrave(@NotNull Grave grave) {
      this.plugin.getGraveManager().breakGrave(grave);
   }

   public void breakGrave(@NotNull Location location, @NotNull Grave grave) {
      this.plugin.getGraveManager().breakGrave(location, grave);
   }

   public void autoLootGrave(@NotNull Entity entity, @NotNull Location location, @NotNull Grave grave) {
      this.plugin.getGraveManager().autoLootGrave(entity, location, grave);
   }

   public void abandonGrave(@NotNull Grave grave) {
      this.plugin.getGraveManager().abandonGrave(grave);
   }

   public void dropGraveItems(@NotNull Location location, @NotNull Grave grave) {
      this.plugin.getGraveManager().dropGraveItems(location, grave);
   }

   public void removeOldestGrave(@NotNull LivingEntity livingEntity) {
      this.plugin.getGraveManager().removeOldestGrave(livingEntity);
   }

   public boolean isNearGrave(@NotNull Location location, @Nullable Player player, @Nullable Block block) {
      return this.plugin.getGraveManager().isNearGrave(location, player, block);
   }

   public boolean isNearGrave(@NotNull Location location) {
      return this.isNearGrave(location, null, null);
   }

   public boolean isNearGrave(@NotNull Location location, @NotNull Player player) {
      return this.isNearGrave(location, player, null);
   }

   public boolean isNearGrave(@NotNull Location location, @NotNull Block block) {
      return this.isNearGrave(location, null, block);
   }

   public Grave getGrave(@NotNull UUID uuid) {
      return new Grave(uuid);
   }

   @Deprecated
   public boolean isGrave(@NotNull Grave grave) {
      return this.isGrave(grave, grave.getLocationDeath());
   }

   public boolean isGrave(@NotNull Grave grave, @NotNull Location location) {
      return location.equals(grave.getLocationDeath());
   }

   public long getGraveAmount() {
      return this.getGraveAmount(null);
   }

   public long getGraveAmount(@Nullable Player targetPlayer) {
      List<Grave> graveList = new ArrayList<>(this.plugin.getCacheManager().getGraveMap().values());
      if (targetPlayer == null) {
         return graveList.size();
      } else {
         UUID playerUUID = targetPlayer.getUniqueId();
         long count = 0L;

         for (Grave g : graveList) {
            if (playerUUID.equals(g.getOwnerUUID())) {
               count++;
            }
         }

         return count;
      }
   }

   @NotNull
   public List<UUID> getGraveUUIDs() {
      return this.getGraveUUIDs((UUID)null);
   }

   @NotNull
   public List<UUID> getGraveUUIDs(@Nullable Player targetPlayer) {
      return this.getGraveUUIDs(targetPlayer != null ? targetPlayer.getUniqueId() : null);
   }

   @NotNull
   public List<UUID> getGraveUUIDs(@Nullable UUID ownerUUID) {
      List<UUID> graveUUIDs = new ArrayList<>();

      for (Grave grave : this.plugin.getCacheManager().getGraveMap().values()) {
         if (ownerUUID == null || ownerUUID.equals(grave.getOwnerUUID())) {
            graveUUIDs.add(grave.getUUID());
         }
      }

      return graveUUIDs;
   }

   @NotNull
   public List<Grave> getGraves(@Nullable Player targetPlayer) {
      return this.getGraves(targetPlayer != null ? targetPlayer.getUniqueId() : null);
   }

   @NotNull
   public List<Grave> getGraves(@Nullable UUID ownerUUID) {
      List<Grave> graves = new ArrayList<>();

      for (Grave grave : this.plugin.getCacheManager().getGraveMap().values()) {
         if (ownerUUID == null || ownerUUID.equals(grave.getOwnerUUID())) {
            graves.add(grave);
         }
      }

      return graves;
   }

   public boolean isGraveLocked(@NotNull Grave grave) {
      UUID graveUUID = grave.getUUID();
      return this.plugin.getCacheManager().isGraveBeingViewed(graveUUID);
   }

   public boolean isGraveLocked(@NotNull Grave grave, @NotNull Player player) {
      UUID graveUUID = grave.getUUID();
      return !this.plugin.getCacheManager().canAccessGrave(graveUUID, player.getUniqueId());
   }

   @Nullable
   public UUID getGraveViewerUUID(@NotNull Grave grave) {
      UUID graveUUID = grave.getUUID();
      return this.plugin.getCacheManager().getGraveViewer(graveUUID);
   }

   public boolean tryGetGraveViewerUUID(@NotNull Grave grave, @NotNull UUID[] outViewer) {
      if (outViewer.length == 0) {
         throw new GravesXIllegalArgumentException("outViewer must have length >= 1");
      } else {
         UUID viewer = this.getGraveViewerUUID(grave);
         if (viewer == null) {
            return false;
         } else {
            outViewer[0] = viewer;
            return true;
         }
      }
   }

   public void setGraveProtection(@NotNull Grave grave, boolean protectedGrave, long timeProtection) {
      grave.setProtection(protectedGrave);
      grave.setTimeProtection(protectedGrave ? Math.max(timeProtection, 0L) : 0L);
   }

   public void clearGraveProtection(@NotNull Grave grave) {
      this.setGraveProtection(grave, false, 0L);
   }

   public int getNextAvailableGraveSlot(@NotNull Grave grave) {
      Inventory inventory = grave.getInventory();
      if (inventory == null) {
         return -1;
      } else {
         for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (this.isEmptyItem(inventory.getItem(slot))) {
               return slot;
            }
         }

         return -1;
      }
   }

   public boolean hasAvailableGraveSlot(@NotNull Grave grave) {
      return this.getNextAvailableGraveSlot(grave) >= 0;
   }

   public boolean addItemToGraveSlot(@NotNull Grave grave, @NotNull ItemStack itemStack, int slot) {
      Inventory inventory = grave.getInventory();
      if (this.canUseItem(itemStack) && inventory != null && this.isValidSlot(inventory, slot) && this.isEmptyItem(inventory.getItem(slot))) {
         inventory.setItem(slot, itemStack.clone());
         return true;
      } else {
         return false;
      }
   }

   public boolean setItemInGraveSlot(@NotNull Grave grave, @NotNull ItemStack itemStack, int slot) {
      Inventory inventory = grave.getInventory();
      if (this.canUseItem(itemStack) && inventory != null && this.isValidSlot(inventory, slot)) {
         inventory.setItem(slot, itemStack.clone());
         return true;
      } else {
         return false;
      }
   }

   public int addItemToNextAvailableGraveSlot(@NotNull Grave grave, @NotNull ItemStack itemStack) {
      int slot = this.getNextAvailableGraveSlot(grave);
      if (slot < 0) {
         return -1;
      } else {
         return this.addItemToGraveSlot(grave, itemStack, slot) ? slot : -1;
      }
   }

   private boolean isValidSlot(@NotNull Inventory inventory, int slot) {
      return slot >= 0 && slot < inventory.getSize();
   }

   private boolean canUseItem(@NotNull ItemStack itemStack) {
      return itemStack.getType() != Material.AIR && itemStack.getAmount() > 0;
   }

   private boolean isEmptyItem(@Nullable ItemStack itemStack) {
      return itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() <= 0;
   }
}
