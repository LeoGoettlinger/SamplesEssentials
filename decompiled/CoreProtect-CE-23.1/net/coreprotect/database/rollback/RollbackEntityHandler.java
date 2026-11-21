package net.coreprotect.database.rollback;

import net.coreprotect.config.ConfigHandler;
import net.coreprotect.thread.CacheHandler;
import net.coreprotect.utility.EntityUtils;
import net.coreprotect.utility.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class RollbackEntityHandler {
   public static int processEntity(
      Object[] row,
      int rollbackType,
      String finalUserString,
      int oldTypeRaw,
      int rowTypeRaw,
      int rowData,
      int rowAction,
      int rowRolledBack,
      int rowX,
      int rowY,
      int rowZ,
      int rowWorldId,
      int rowUserId,
      String rowUser
   ) {
      try {
         if (rowAction == 3) {
            String world = getWorldName(rowWorldId);
            if (world.isEmpty()) {
               return 0;
            }

            World bukkitWorld = Bukkit.getServer().getWorld(world);
            if (bukkitWorld == null) {
               return 0;
            }

            Block block = bukkitWorld.getBlockAt(rowX, rowY, rowZ);
            if (!bukkitWorld.isChunkLoaded(block.getChunk())) {
               bukkitWorld.getChunkAt(block.getLocation());
            }

            if (rowTypeRaw > 0) {
               if (rowRolledBack == 0) {
                  EntityType entityType = EntityUtils.getEntityType(rowTypeRaw);
                  spawnEntity(rowUser, block.getState(), entityType, rowData);
                  updateEntityCount(finalUserString, 1);
                  return 1;
               }
            } else if (rowTypeRaw <= 0 && rowRolledBack == 1) {
               boolean removed = false;
               int entityId = -1;
               String entityName = EntityUtils.getEntityType(oldTypeRaw).name();
               String token = rowX + "." + rowY + "." + rowZ + "." + rowWorldId + "." + entityName;
               Object[] cachedEntity = CacheHandler.entityCache.get(token);
               if (cachedEntity != null) {
                  entityId = (Integer)cachedEntity[1];
               }

               int xmin = rowX - 5;
               int xmax = rowX + 5;
               int ymin = rowY - 1;
               int ymax = rowY + 1;
               int zmin = rowZ - 5;
               int zmax = rowZ + 5;

               for (Entity entity : block.getChunk().getEntities()) {
                  if (entityId > -1) {
                     int id = entity.getEntityId();
                     if (id == entityId) {
                        updateEntityCount(finalUserString, 1);
                        removed = true;
                        entity.remove();
                        break;
                     }
                  } else if (entity.getType().equals(EntityUtils.getEntityType(oldTypeRaw))) {
                     Location entityLocation = entity.getLocation();
                     int entityx = entityLocation.getBlockX();
                     int entityY = entityLocation.getBlockY();
                     int entityZ = entityLocation.getBlockZ();
                     if (entityx >= xmin && entityx <= xmax && entityY >= ymin && entityY <= ymax && entityZ >= zmin && entityZ <= zmax) {
                        updateEntityCount(finalUserString, 1);
                        removed = true;
                        entity.remove();
                        break;
                     }
                  }
               }

               if (!removed && entityId > -1) {
                  for (Entity entityx : block.getWorld().getLivingEntities()) {
                     int id = entityx.getEntityId();
                     if (id == entityId) {
                        updateEntityCount(finalUserString, 1);
                        removed = true;
                        entityx.remove();
                        break;
                     }
                  }
               }

               if (removed) {
                  return 1;
               }
            }
         }
      } catch (Exception var36) {
         var36.printStackTrace();
      }

      return 0;
   }

   private static String getWorldName(int worldId) {
      return WorldUtils.getWorldName(worldId);
   }

   public static void updateEntityCount(String userString, int increment) {
      int[] rollbackHashData = ConfigHandler.rollbackHash.get(userString);
      if (rollbackHashData != null) {
         int itemCount = rollbackHashData[0];
         int blockCount = rollbackHashData[1];
         int entityCount = rollbackHashData[2];
         int next = rollbackHashData[3];
         int scannedWorlds = rollbackHashData[4];
         entityCount += increment;
         ConfigHandler.rollbackHash.put(userString, new int[]{itemCount, blockCount, entityCount, next, scannedWorlds});
      }
   }

   public static void spawnEntity(String user, BlockState block, EntityType type, int data) {
      RollbackUtil.queueEntitySpawn(user, block, type, data);
   }
}
