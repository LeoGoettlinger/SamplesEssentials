package net.coreprotect.database.rollback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.utility.BlockUtils;
import net.coreprotect.utility.ItemUtils;
import net.coreprotect.utility.MaterialUtils;
import net.coreprotect.utility.Teleport;
import net.coreprotect.utility.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Jukebox;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RollbackProcessor {
   public static boolean processChunk(
      int finalChunkX,
      int finalChunkZ,
      long chunkKey,
      ArrayList<Object[]> blockList,
      ArrayList<Object[]> itemList,
      int rollbackType,
      int preview,
      String finalUserString,
      Player finalUser,
      World bukkitRollbackWorld,
      boolean inventoryRollback
   ) {
      try {
         boolean clearInventories = Config.getGlobal().ROLLBACK_ITEMS;
         ArrayList<Object[]> data = blockList != null ? blockList : new ArrayList<>();
         ArrayList<Object[]> itemData = itemList != null ? itemList : new ArrayList<>();
         Map<Block, BlockData> chunkChanges = new LinkedHashMap<>();

         for (Object[] row : data) {
            int[] rollbackHashData = ConfigHandler.rollbackHash.get(finalUserString);
            int itemCount = rollbackHashData[0];
            int blockCount = rollbackHashData[1];
            int entityCount = rollbackHashData[2];
            int scannedWorlds = rollbackHashData[4];
            int rowX = (Integer)row[3];
            int rowY = (Integer)row[4];
            int rowZ = (Integer)row[5];
            int rowTypeRaw = (Integer)row[6];
            int rowData = (Integer)row[7];
            int rowAction = (Integer)row[8];
            int rowRolledBack = MaterialUtils.rolledBack((Integer)row[9], false);
            int rowWorldId = (Integer)row[10];
            byte[] rowMeta = (byte[])row[12];
            byte[] rowBlockData = (byte[])row[13];
            String blockDataString = BlockUtils.byteDataToString(rowBlockData, rowTypeRaw);
            Material rowType = MaterialUtils.getType(rowTypeRaw);
            List<Object> meta = null;
            if (rowMeta != null) {
               meta = RollbackUtil.deserializeMetadata(rowMeta);
            }

            BlockData blockData = null;
            if (blockDataString != null && blockDataString.contains(":")) {
               try {
                  blockData = Bukkit.getServer().createBlockData(blockDataString);
               } catch (Exception var55) {
               }
            }

            BlockData rawBlockData = null;
            if (blockData != null) {
               rawBlockData = blockData.clone();
            }

            if (rawBlockData == null && rowType != null && rowType.isBlock()) {
               rawBlockData = BlockUtils.createBlockData(rowType);
            }

            String rowUser = ConfigHandler.playerIdCacheReversed.get((Integer)row[2]);
            int oldTypeRaw = rowTypeRaw;
            Material oldTypeMaterial = MaterialUtils.getType(rowTypeRaw);
            if (rowAction == 1 && rollbackType == 0) {
               rowType = Material.AIR;
               blockData = null;
               rowTypeRaw = 0;
            } else if (rowAction == 0 && rollbackType == 1) {
               rowType = Material.AIR;
               blockData = null;
               rowTypeRaw = 0;
            } else if (rowAction == 4 && rollbackType == 0) {
               rowType = null;
               rowTypeRaw = 0;
            } else if (rowAction == 3 && rollbackType == 1) {
               rowType = null;
               rowTypeRaw = 0;
            }

            if (preview > 0) {
               if (rowAction != 3) {
                  String world = WorldUtils.getWorldName(rowWorldId);
                  if (world.length() == 0) {
                     continue;
                  }

                  World bukkitWorld = Bukkit.getServer().getWorld(world);
                  if (bukkitWorld == null) {
                     continue;
                  }

                  Block block = new Location(bukkitWorld, rowX, rowY, rowZ).getBlock();
                  if (preview == 2) {
                     Material blockType = block.getType();
                     if (!BukkitAdapter.ADAPTER.isItemFrame(blockType)
                        && !blockType.equals(Material.PAINTING)
                        && !blockType.equals(Material.ARMOR_STAND)
                        && !blockType.equals(Material.END_CRYSTAL)) {
                        BlockUtils.prepareTypeAndData(chunkChanges, block, blockType, block.getBlockData(), true);
                        blockCount++;
                     }
                  } else if (!BukkitAdapter.ADAPTER.isItemFrame(rowType)
                     && rowType != Material.PAINTING
                     && rowType != Material.ARMOR_STAND
                     && rowType != Material.END_CRYSTAL) {
                     BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, true);
                     blockCount++;
                  }
               } else {
                  entityCount++;
               }
            } else if (rowAction == 3) {
               entityCount += RollbackEntityHandler.processEntity(
                  row,
                  rollbackType,
                  finalUserString,
                  oldTypeRaw,
                  rowTypeRaw,
                  rowData,
                  rowAction,
                  MaterialUtils.rolledBack((Integer)row[9], false),
                  rowX,
                  rowY,
                  rowZ,
                  rowWorldId,
                  (Integer)row[2],
                  rowUser
               );
            } else {
               String worldx = WorldUtils.getWorldName(rowWorldId);
               if (worldx.length() == 0) {
                  continue;
               }

               World bukkitWorldx = Bukkit.getServer().getWorld(worldx);
               if (bukkitWorldx == null) {
                  continue;
               }

               Block block = bukkitWorldx.getBlockAt(rowX, rowY, rowZ);
               if (!bukkitWorldx.isChunkLoaded(block.getChunk())) {
                  bukkitWorldx.getChunkAt(block.getLocation());
               }

               boolean changeBlock = true;
               boolean countBlock = true;
               Material changeType = block.getType();
               BlockData changeBlockData = block.getBlockData();
               BlockData pendingChangeData = chunkChanges.get(block);
               Material pendingChangeType = changeType;
               if (pendingChangeData != null) {
                  pendingChangeType = pendingChangeData.getMaterial();
               } else {
                  pendingChangeData = changeBlockData;
               }

               if (rowRolledBack == 1 && rollbackType == 0) {
                  countBlock = false;
               }

               if (rowType == pendingChangeType
                  && !BukkitAdapter.ADAPTER.isItemFrame(oldTypeMaterial)
                  && oldTypeMaterial != Material.PAINTING
                  && oldTypeMaterial != Material.ARMOR_STAND
                  && oldTypeMaterial != Material.END_CRYSTAL) {
                  BlockData checkData = rowType == Material.AIR ? blockData : rawBlockData;
                  if (checkData != null) {
                     if ((
                           checkData.getAsString().equals(pendingChangeData.getAsString())
                              || checkData instanceof MultipleFacing
                              || checkData instanceof Stairs
                              || checkData instanceof RedstoneWire
                        )
                        && rowType != Material.CHEST
                        && rowType != Material.TRAPPED_CHEST
                        && !BukkitAdapter.ADAPTER.isCopperChest(rowType)) {
                        changeBlock = false;
                     }
                  } else if (rowType == Material.AIR) {
                     changeBlock = false;
                  }

                  countBlock = false;
               } else if (pendingChangeType != Material.AIR && pendingChangeType != Material.CAVE_AIR) {
                  countBlock = true;
               }

               if (pendingChangeType == Material.WATER
                  && rowType != Material.AIR
                  && rowType != Material.CAVE_AIR
                  && blockData != null
                  && blockData instanceof Waterlogged
                  && Material.WATER.createBlockData().equals(block.getBlockData())) {
                  Waterlogged waterlogged = (Waterlogged)blockData;
                  waterlogged.setWaterlogged(true);
               }

               if (RollbackBlockHandler.processBlockChange(
                     bukkitWorldx,
                     block,
                     row,
                     rollbackType,
                     clearInventories,
                     chunkChanges,
                     countBlock,
                     oldTypeMaterial,
                     pendingChangeType,
                     pendingChangeData,
                     finalUserString,
                     rawBlockData,
                     changeType,
                     changeBlock,
                     changeBlockData,
                     meta != null ? new ArrayList<>(meta) : null,
                     blockData,
                     rowUser,
                     rowType,
                     rowX,
                     rowY,
                     rowZ,
                     rowTypeRaw,
                     rowData,
                     rowAction,
                     rowWorldId,
                     BlockUtils.byteDataToString((byte[])row[13], rowTypeRaw)
                  )
                  && countBlock) {
                  blockCount++;
               }
            }

            ConfigHandler.rollbackHash.put(finalUserString, new int[]{itemCount, blockCount, entityCount, 0, scannedWorlds});
         }

         data.clear();
         RollbackBlockHandler.applyBlockChanges(chunkChanges, preview, finalUser instanceof Player ? finalUser : null);
         Map<Player, List<Integer>> sortPlayers = new HashMap<>();
         Object container = null;
         Material containerType = null;
         boolean containerInit = false;
         int lastX = 0;
         int lastY = 0;
         int lastZ = 0;
         int lastWorldId = 0;
         String lastFace = "";

         for (Object[] row : itemData) {
            int[] rollbackHashData1 = ConfigHandler.rollbackHash.get(finalUserString);
            int itemCount1 = rollbackHashData1[0];
            int blockCount1 = rollbackHashData1[1];
            int entityCount1 = rollbackHashData1[2];
            int scannedWorldsx = rollbackHashData1[4];
            int rowXx = (Integer)row[3];
            int rowYx = (Integer)row[4];
            int rowZx = (Integer)row[5];
            int rowTypeRawx = (Integer)row[6];
            int rowDatax = (Integer)row[7];
            int rowActionx = (Integer)row[8];
            int rowRolledBackx = MaterialUtils.rolledBack((Integer)row[9], false);
            int rowWorldIdx = (Integer)row[10];
            int rowAmount = (Integer)row[11];
            byte[] rowMetadata = (byte[])row[12];
            Material rowTypex = MaterialUtils.getType(rowTypeRawx);
            int rolledBackInventory = MaterialUtils.rolledBack((Integer)row[9], true);
            if (rowTypex != null) {
               if (inventoryRollback && (rollbackType == 0 && rolledBackInventory == 0 || rollbackType == 1 && rolledBackInventory == 1)) {
                  Material inventoryItem = ItemUtils.itemFilter(rowTypex, (Integer)row[14] == 0);
                  int rowUserId = (Integer)row[2];
                  String rowUserx = ConfigHandler.playerIdCacheReversed.get(rowUserId);
                  if (rowUserx != null) {
                     String uuid = ConfigHandler.uuidCache.get(rowUserx.toLowerCase(Locale.ROOT));
                     if (uuid != null) {
                        Player player = Bukkit.getServer().getPlayer(UUID.fromString(uuid));
                        if (player != null) {
                           int inventoryAction = 0;
                           if (rowActionx != 2
                              && rowActionx != 3
                              && rowActionx != 6
                              && rowActionx != 7
                              && rowActionx != 8
                              && rowActionx != 9
                              && rowActionx != 10
                              && rowActionx != 11
                              && rowActionx != 12) {
                              if (rowActionx != 4 && rowActionx != 5) {
                                 inventoryAction = rowActionx == 0 ? 1 : 0;
                              } else {
                                 inventoryAction = rowActionx == 4 ? 1 : 0;
                              }
                           } else {
                              inventoryAction = rowActionx != 3 && rowActionx != 10 && rowActionx != 12 ? 0 : 1;
                           }

                           int action = rollbackType == 0 ? inventoryAction ^ 1 : inventoryAction;
                           ItemStack itemstack = new ItemStack(inventoryItem, rowAmount);
                           Object[] populatedStack = RollbackItemHandler.populateItemStack(itemstack, rowMetadata);
                           if (rowActionx == 4 || rowActionx == 5) {
                              RollbackUtil.modifyContainerItems(
                                 containerType, player.getEnderChest(), (Integer)populatedStack[0], ((ItemStack)populatedStack[2]).clone(), action ^ 1
                              );
                           }

                           int modifiedArmor = RollbackUtil.modifyContainerItems(
                              containerType, player.getInventory(), (Integer)populatedStack[0], (ItemStack)populatedStack[2], action
                           );
                           if (modifiedArmor > -1) {
                              List<Integer> currentSortList = sortPlayers.getOrDefault(player, new ArrayList<>());
                              if (!currentSortList.contains(modifiedArmor)) {
                                 currentSortList.add(modifiedArmor);
                              }

                              sortPlayers.put(player, currentSortList);
                           }

                           itemCount1 += rowAmount;
                           ConfigHandler.rollbackHash.put(finalUserString, new int[]{itemCount1, blockCount1, entityCount1, 0, scannedWorldsx});
                        }
                     }
                  }
                  continue;
               }

               if (inventoryRollback || rowActionx > 1) {
                  continue;
               }

               if (rollbackType == 0 && rowRolledBackx == 0 || rollbackType == 1 && rowRolledBackx == 1) {
                  ItemStack itemstackx = new ItemStack(rowTypex, rowAmount);
                  Object[] populatedStackx = RollbackItemHandler.populateItemStack(itemstackx, rowMetadata);
                  String faceData = (String)populatedStackx[1];
                  if (!containerInit || rowXx != lastX || rowYx != lastY || rowZx != lastZ || rowWorldIdx != lastWorldId || !faceData.equals(lastFace)) {
                     container = null;
                     String worldxx = WorldUtils.getWorldName(rowWorldIdx);
                     if (worldxx.length() == 0) {
                        continue;
                     }

                     World bukkitWorldxx = Bukkit.getServer().getWorld(worldxx);
                     if (bukkitWorldxx == null) {
                        continue;
                     }

                     Block blockx = bukkitWorldxx.getBlockAt(rowXx, rowYx, rowZx);
                     if (!bukkitWorldxx.isChunkLoaded(blockx.getChunk())) {
                        bukkitWorldxx.getChunkAt(blockx.getLocation());
                     }

                     if (BlockGroup.CONTAINERS.contains(blockx.getType())) {
                        BlockState blockState = blockx.getState();
                        if (blockState instanceof Jukebox) {
                           container = blockState;
                        } else {
                           container = BlockUtils.getContainerInventory(blockState, false);
                        }

                        containerType = blockx.getType();
                     } else if (BlockGroup.CONTAINERS.contains(Material.ARMOR_STAND) || BlockGroup.CONTAINERS.contains(Material.ITEM_FRAME)) {
                        for (Entity entity : blockx.getChunk().getEntities()) {
                           if (entity.getLocation().getBlockX() == rowXx
                              && entity.getLocation().getBlockY() == rowYx
                              && entity.getLocation().getBlockZ() == rowZx) {
                              if (entity instanceof ArmorStand) {
                                 container = ItemUtils.getEntityEquipment((LivingEntity)entity);
                                 containerType = Material.ARMOR_STAND;
                              } else if (entity instanceof ItemFrame) {
                                 container = entity;
                                 containerType = Material.ITEM_FRAME;
                                 if (faceData.length() > 0 && BlockFace.valueOf(faceData) == ((ItemFrame)entity).getFacing()) {
                                    break;
                                 }
                              }
                           }
                        }
                     }

                     lastX = rowXx;
                     lastY = rowYx;
                     lastZ = rowZx;
                     lastWorldId = rowWorldIdx;
                     lastFace = faceData;
                  }

                  if (container != null) {
                     int actionx = 0;
                     if (rollbackType == 0 && rowActionx == 0) {
                        actionx = 1;
                     }

                     if (rollbackType == 1 && rowActionx == 1) {
                        actionx = 1;
                     }

                     int slot = (Integer)populatedStackx[0];
                     itemstackx = (ItemStack)populatedStackx[2];
                     RollbackUtil.modifyContainerItems(containerType, container, slot, itemstackx, actionx);
                     itemCount1 += rowAmount;
                  }

                  containerInit = true;
               }
            }

            ConfigHandler.rollbackHash.put(finalUserString, new int[]{itemCount1, blockCount1, entityCount1, 0, scannedWorldsx});
         }

         itemData.clear();

         for (Entry<Player, List<Integer>> sortEntry : sortPlayers.entrySet()) {
            RollbackItemHandler.sortContainerItems(sortEntry.getKey().getInventory(), sortEntry.getValue());
         }

         sortPlayers.clear();
         int[] rollbackHashDatax = ConfigHandler.rollbackHash.get(finalUserString);
         int itemCountx = rollbackHashDatax[0];
         int blockCountx = rollbackHashDatax[1];
         int entityCountx = rollbackHashDatax[2];
         int scannedWorldsx = rollbackHashDatax[4];
         ConfigHandler.rollbackHash.put(finalUserString, new int[]{itemCountx, blockCountx, entityCountx, 1, scannedWorldsx + 1});
         if (preview == 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
               Location playerLocation = player.getLocation();
               String playerWorld = playerLocation.getWorld().getName();
               int chunkX = playerLocation.getBlockX() >> 4;
               int chunkZ = playerLocation.getBlockZ() >> 4;
               if (bukkitRollbackWorld.getName().equals(playerWorld) && chunkX == finalChunkX && chunkZ == finalChunkZ) {
                  Teleport.performSafeTeleport(player, playerLocation, false);
               }
            }
         }

         return true;
      } catch (Exception var56) {
         var56.printStackTrace();
         int[] rollbackHashDatax = ConfigHandler.rollbackHash.get(finalUserString);
         int itemCountx = rollbackHashDatax[0];
         int blockCountx = rollbackHashDatax[1];
         int entityCountx = rollbackHashDatax[2];
         int scannedWorldsx = rollbackHashDatax[4];
         ConfigHandler.rollbackHash.put(finalUserString, new int[]{itemCountx, blockCountx, entityCountx, 2, scannedWorldsx + 1});
         return false;
      }
   }
}
