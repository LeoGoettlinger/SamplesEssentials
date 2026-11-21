package com.ranull.graves.manager;

import com.ranull.graves.data.ChunkData;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.type.Grave;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class CacheManager {
   private final Map<UUID, Grave> graveMap;
   private final Map<String, ChunkData> chunkMap;
   private final Map<UUID, Location> lastLocationMap;
   private final Map<UUID, List<ItemStack>> removedItemStackMap;
   private final Map<String, Location> rightClickedBlocks = new HashMap<>();
   private final Map<UUID, UUID> graveViewerMap;
   private final Map<UUID, EntityData> entityMap;

   public CacheManager() {
      this.graveMap = new HashMap<>();
      this.chunkMap = new HashMap<>();
      this.lastLocationMap = new HashMap<>();
      this.removedItemStackMap = new HashMap<>();
      this.graveViewerMap = new HashMap<>();
      this.entityMap = new HashMap<>();
   }

   public Map<UUID, Grave> getGraveMap() {
      return this.graveMap;
   }

   public void addRightClickedBlock(String playerName, Location location) {
      this.rightClickedBlocks.put(playerName, location);
   }

   public Location getRightClickedBlock(String playerName) {
      return this.rightClickedBlocks.get(playerName);
   }

   public void removeRightClickedBlock(String playerName, Location location) {
      this.rightClickedBlocks.remove(playerName, location);
   }

   public boolean hasRightClickedBlock(String playerName) {
      return this.rightClickedBlocks.containsKey(playerName);
   }

   public Map<String, ChunkData> getChunkMap() {
      return this.chunkMap;
   }

   public Map<UUID, Location> getLastLocationMap() {
      return this.lastLocationMap;
   }

   public Map<UUID, List<ItemStack>> getRemovedItemStackMap() {
      return this.removedItemStackMap;
   }

   public boolean startViewingGrave(UUID graveUUID, UUID viewerUUID) {
      UUID current = this.graveViewerMap.get(graveUUID);
      if (current != null && !current.equals(viewerUUID)) {
         return false;
      } else {
         this.graveViewerMap.put(graveUUID, viewerUUID);
         return true;
      }
   }

   public void stopViewingGrave(UUID graveUUID, UUID viewerUUID) {
      UUID current = this.graveViewerMap.get(graveUUID);
      if (current != null && current.equals(viewerUUID)) {
         this.graveViewerMap.remove(graveUUID);
      }
   }

   public void clearGraveViewer(UUID graveUUID) {
      this.graveViewerMap.remove(graveUUID);
   }

   public void clearAllGraveViewersFor(UUID viewerUUID) {
      this.graveViewerMap.entrySet().removeIf(e -> viewerUUID.equals(e.getValue()));
   }

   public boolean isGraveBeingViewed(UUID graveUUID) {
      return this.graveViewerMap.containsKey(graveUUID);
   }

   public UUID getGraveViewer(UUID graveUUID) {
      return this.graveViewerMap.get(graveUUID);
   }

   public boolean canAccessGrave(UUID graveUUID, UUID viewerUUID) {
      UUID current = this.graveViewerMap.get(graveUUID);
      return current == null || current.equals(viewerUUID);
   }

   public Grave getGrave(UUID graveUUID) {
      return this.graveMap.get(graveUUID);
   }

   public Grave getGrave(Block block) {
      for (Grave grave : this.graveMap.values()) {
         if (grave != null) {
            Location graveLocation = grave.getLocationDeath();
            if (graveLocation != null
               && graveLocation.getWorld() != null
               && graveLocation.getWorld().equals(block.getWorld())
               && graveLocation.getBlockX() == block.getX()
               && graveLocation.getBlockY() == block.getY()
               && graveLocation.getBlockZ() == block.getZ()) {
               return grave;
            }
         }
      }

      return null;
   }

   public Grave getOldestGrave(UUID playerUUID) {
      long oldestTime = Long.MAX_VALUE;
      Grave oldestGrave = null;

      for (Grave cur : this.graveMap.values()) {
         if (cur.getOwnerUUID().equals(playerUUID)) {
            long curTime = cur.getTimeCreation();
            if (curTime < oldestTime) {
               oldestTime = curTime;
               oldestGrave = cur;
            }
         }
      }

      return oldestGrave;
   }

   public Grave getGrave(Location location) {
      if (location != null && location.getWorld() != null) {
         for (Grave grave : this.graveMap.values()) {
            Location graveLocation = grave.getLocationDeath();
            if (graveLocation != null
               && graveLocation.getWorld() != null
               && graveLocation.getWorld().equals(location.getWorld())
               && graveLocation.getBlockX() == location.getBlockX()
               && graveLocation.getBlockY() == location.getBlockY()
               && graveLocation.getBlockZ() == location.getBlockZ()) {
               return grave;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public Map<UUID, EntityData> getEntityMap() {
      return this.entityMap;
   }

   public EntityData getEntityData(UUID entityUUID) {
      return entityUUID == null ? null : this.entityMap.get(entityUUID);
   }

   public void addEntityData(EntityData entityData) {
      if (entityData != null && entityData.getUUIDEntity() != null) {
         this.entityMap.put(entityData.getUUIDEntity(), entityData);
      }
   }

   public void removeEntityData(UUID entityUUID) {
      if (entityUUID != null) {
         this.entityMap.remove(entityUUID);
      }
   }

   public void removeEntityData(EntityData entityData) {
      if (entityData != null) {
         this.removeEntityData(entityData.getUUIDEntity());
      }
   }
}
