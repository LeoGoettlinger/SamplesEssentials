package net.coreprotect.database.rollback;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.paper.PaperAdapter;
import net.coreprotect.thread.CacheHandler;
import net.coreprotect.utility.BlockUtils;
import net.coreprotect.utility.ChestTool;
import net.coreprotect.utility.EntityUtils;
import net.coreprotect.utility.ItemUtils;
import net.coreprotect.utility.Util;
import net.coreprotect.utility.entity.HangingUtil;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.PistonHead;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.Snow;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TechnicalPiston;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.block.data.type.Door.Hinge;
import org.bukkit.block.data.type.TechnicalPiston.Type;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RollbackBlockHandler extends Queue {
   public static boolean processBlockChange(
      World bukkitWorld,
      Block block,
      Object[] row,
      int rollbackType,
      boolean clearInventories,
      Map<Block, BlockData> chunkChanges,
      boolean countBlock,
      Material oldTypeMaterial,
      Material pendingChangeType,
      BlockData pendingChangeData,
      String finalUserString,
      BlockData rawBlockData,
      Material changeType,
      boolean changeBlock,
      BlockData changeBlockData,
      ArrayList<Object> meta,
      BlockData blockData,
      String rowUser,
      Material rowType,
      int rowX,
      int rowY,
      int rowZ,
      int rowTypeRaw,
      int rowData,
      int rowAction,
      int rowWorldId,
      String blockDataString
   ) {
      int unixtimestamp = (int)(System.currentTimeMillis() / 1000L);

      try {
         if (changeBlock) {
            if (changeBlockData instanceof PistonHead) {
               PistonHead pistonHead = (PistonHead)changeBlockData;
               Block pistonBlock = block.getRelative(pistonHead.getFacing().getOppositeFace());
               BlockData pistonData = pistonBlock.getBlockData();
               if (pistonData instanceof Piston) {
                  Piston piston = (Piston)pistonData;
                  piston.setExtended(false);
                  pistonBlock.setBlockData(piston, false);
               }
            } else if (rowType == Material.MOVING_PISTON && blockData instanceof TechnicalPiston && !(blockData instanceof PistonHead)) {
               TechnicalPiston technicalPiston = (TechnicalPiston)blockData;
               rowType = technicalPiston.getType() == Type.STICKY ? Material.STICKY_PISTON : Material.PISTON;
               blockData = rowType.createBlockData();
               ((Piston)blockData).setFacing(technicalPiston.getFacing());
            }

            if (rowType != Material.AIR || !BukkitAdapter.ADAPTER.isItemFrame(oldTypeMaterial) && oldTypeMaterial != Material.PAINTING) {
               if (!BukkitAdapter.ADAPTER.isItemFrame(rowType) && rowType != Material.PAINTING) {
                  if (rowType == Material.ARMOR_STAND) {
                     Location location1 = block.getLocation();
                     location1.setX(location1.getX() + 0.5);
                     location1.setZ(location1.getZ() + 0.5);
                     location1.setYaw(rowData);
                     boolean exists = false;

                     for (Entity entity : block.getChunk().getEntities()) {
                        if (entity instanceof ArmorStand
                           && entity.getLocation().getBlockX() == location1.getBlockX()
                           && entity.getLocation().getBlockY() == location1.getBlockY()
                           && entity.getLocation().getBlockZ() == location1.getBlockZ()) {
                           exists = true;
                        }
                     }

                     if (!exists) {
                        Entity entityx = block.getLocation().getWorld().spawnEntity(location1, EntityType.ARMOR_STAND);
                        PaperAdapter.ADAPTER.teleportAsync(entityx, location1);
                     }
                  } else if (rowType == Material.END_CRYSTAL) {
                     Location location1 = block.getLocation();
                     location1.setX(location1.getX() + 0.5);
                     location1.setZ(location1.getZ() + 0.5);
                     boolean exists = false;

                     for (Entity entityx : block.getChunk().getEntities()) {
                        if (entityx instanceof EnderCrystal
                           && entityx.getLocation().getBlockX() == location1.getBlockX()
                           && entityx.getLocation().getBlockY() == location1.getBlockY()
                           && entityx.getLocation().getBlockZ() == location1.getBlockZ()) {
                           exists = true;
                        }
                     }

                     if (!exists) {
                        Entity entityxx = block.getLocation().getWorld().spawnEntity(location1, BukkitAdapter.ADAPTER.getEntityType(Material.END_CRYSTAL));
                        EnderCrystal enderCrystal = (EnderCrystal)entityxx;
                        enderCrystal.setShowingBottom(rowData != 0);
                        PaperAdapter.ADAPTER.teleportAsync(entityxx, location1);
                     }
                  } else {
                     if (rowType == Material.AIR && oldTypeMaterial == Material.WATER) {
                        if (pendingChangeData instanceof Waterlogged) {
                           Waterlogged waterlogged = (Waterlogged)pendingChangeData;
                           waterlogged.setWaterlogged(false);
                           BlockUtils.prepareTypeAndData(chunkChanges, block, null, waterlogged, false);
                        } else {
                           BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, true);
                        }

                        return countBlock;
                     }

                     if (rowType == Material.AIR && oldTypeMaterial == Material.SNOW) {
                        BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, true);
                        return countBlock;
                     }

                     if (rowType == Material.AIR && oldTypeMaterial == Material.END_CRYSTAL) {
                        for (Entity entityxx : block.getChunk().getEntities()) {
                           if (entityxx instanceof EnderCrystal
                              && entityxx.getLocation().getBlockX() == rowX
                              && entityxx.getLocation().getBlockY() == rowY
                              && entityxx.getLocation().getBlockZ() == rowZ) {
                              entityxx.remove();
                           }
                        }
                     } else {
                        label530:
                        if (rollbackType != 0 || rowAction != 0 || rowType != Material.AIR) {
                           if (rowType != Material.AIR && rowType != Material.TNT) {
                              if (rowType == Material.SPAWNER) {
                                 try {
                                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, false);
                                    CreatureSpawner mobSpawner = (CreatureSpawner)block.getState();
                                    mobSpawner.setSpawnedType(EntityUtils.getSpawnerType(rowData));
                                    mobSpawner.update();
                                    return countBlock;
                                 } catch (Exception var35) {
                                    break label530;
                                 }
                              }

                              if (rowType != Material.SKELETON_SKULL
                                 && rowType != Material.SKELETON_WALL_SKULL
                                 && rowType != Material.WITHER_SKELETON_SKULL
                                 && rowType != Material.WITHER_SKELETON_WALL_SKULL
                                 && rowType != Material.ZOMBIE_HEAD
                                 && rowType != Material.ZOMBIE_WALL_HEAD
                                 && rowType != Material.PLAYER_HEAD
                                 && rowType != Material.PLAYER_WALL_HEAD
                                 && rowType != Material.CREEPER_HEAD
                                 && rowType != Material.CREEPER_WALL_HEAD
                                 && rowType != Material.DRAGON_HEAD
                                 && rowType != Material.DRAGON_WALL_HEAD) {
                                 if (BukkitAdapter.ADAPTER.isSign(rowType)) {
                                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, false);
                                    Queue.queueSignUpdate(rowUser, block.getState(), rollbackType, (Integer)row[1]);
                                    return countBlock;
                                 }

                                 if (BlockGroup.SHULKER_BOXES.contains(rowType)) {
                                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, false);
                                    if (countBlock) {
                                       updateBlockCount(finalUserString, 1);
                                    }

                                    if (meta != null) {
                                       Inventory inventory = BlockUtils.getContainerInventory(block.getState(), false);

                                       for (Object value : meta) {
                                          ItemStack item = ItemUtils.unserializeItemStackLegacy(value);
                                          if (item != null) {
                                             RollbackUtil.modifyContainerItems(rowType, inventory, 0, item, 1);
                                          }
                                       }
                                    }

                                    return false;
                                 }

                                 if (rowType != Material.COMMAND_BLOCK
                                    && rowType != Material.REPEATING_COMMAND_BLOCK
                                    && rowType != Material.CHAIN_COMMAND_BLOCK) {
                                    if (rowType == Material.WATER) {
                                       if (pendingChangeData instanceof Waterlogged) {
                                          Waterlogged waterlogged = (Waterlogged)pendingChangeData;
                                          waterlogged.setWaterlogged(true);
                                          BlockUtils.prepareTypeAndData(chunkChanges, block, null, waterlogged, false);
                                       } else {
                                          BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, false);
                                       }

                                       return countBlock;
                                    }

                                    if (rowType == Material.NETHER_PORTAL && rowAction == 0) {
                                       BlockUtils.prepareTypeAndData(chunkChanges, block, Material.FIRE, null, true);
                                       break label530;
                                    }

                                    if (blockData != null || rowData <= 0 || rowType != Material.IRON_DOOR && !BlockGroup.DOORS.contains(rowType)) {
                                       if (blockData == null && rowData > 0 && rowType.name().endsWith("_BED")) {
                                          if (countBlock) {
                                             updateBlockCount(finalUserString, 1);
                                          }

                                          block.setType(rowType, false);
                                          Bed bed = (Bed)block.getBlockData();
                                          BlockFace face = BlockFace.NORTH;
                                          if (rowData > 4) {
                                             bed.setPart(Part.HEAD);
                                             rowData -= 4;
                                          }

                                          switch (rowData) {
                                             case 2:
                                                face = BlockFace.WEST;
                                                break;
                                             case 3:
                                                face = BlockFace.EAST;
                                                break;
                                             case 4:
                                                face = BlockFace.SOUTH;
                                          }

                                          bed.setFacing(face);
                                          block.setBlockData(bed, false);
                                          return false;
                                       }

                                       if (rowType.name().endsWith("_BANNER")) {
                                          BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, false);
                                          if (countBlock) {
                                             updateBlockCount(finalUserString, 1);
                                          }

                                          if (meta != null) {
                                             Banner banner = (Banner)block.getState();

                                             for (Object valuex : meta) {
                                                if (valuex instanceof DyeColor) {
                                                   banner.setBaseColor((DyeColor)valuex);
                                                } else if (valuex instanceof Map) {
                                                   Pattern pattern = new Pattern((Map)valuex);
                                                   banner.addPattern(pattern);
                                                }
                                             }

                                             banner.update();
                                          }

                                          return false;
                                       }

                                       if (rowType != changeType && (BlockGroup.CONTAINERS.contains(rowType) || BlockGroup.CONTAINERS.contains(changeType))) {
                                          block.setType(Material.AIR);
                                          boolean isChest = blockData instanceof Chest;
                                          BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, isChest);
                                          if (isChest) {
                                             ChestTool.updateDoubleChest(block, blockData, false);
                                          }

                                          return countBlock;
                                       }

                                       if (!BlockGroup.UPDATE_STATE.contains(rowType) && !rowType.name().contains("CANDLE")) {
                                          if (rowType != Material.AIR
                                             && rawBlockData instanceof Bisected
                                             && !(rawBlockData instanceof Stairs)
                                             && !(rawBlockData instanceof TrapDoor)) {
                                             Bisected bisected = (Bisected)rawBlockData;
                                             Bisected bisectData = (Bisected)rawBlockData.clone();
                                             Location bisectLocation = block.getLocation().clone();
                                             if (bisected.getHalf() == Half.TOP) {
                                                bisectData.setHalf(Half.BOTTOM);
                                                bisectLocation.setY(bisectLocation.getY() - 1.0);
                                             } else {
                                                bisectData.setHalf(Half.TOP);
                                                bisectLocation.setY(bisectLocation.getY() + 1.0);
                                             }

                                             int worldMaxHeight = bukkitWorld.getMaxHeight();
                                             int worldMinHeight = BukkitAdapter.ADAPTER.getMinHeight(bukkitWorld);
                                             if (bisectLocation.getBlockY() >= worldMinHeight && bisectLocation.getBlockY() < worldMaxHeight) {
                                                Block bisectBlock = block.getWorld().getBlockAt(bisectLocation);
                                                BlockUtils.prepareTypeAndData(chunkChanges, bisectBlock, rowType, bisectData, false);
                                             }

                                             BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, false);
                                             if (countBlock) {
                                                updateBlockCount(finalUserString, 2);
                                             }

                                             return false;
                                          }

                                          if (rowType != Material.AIR && rawBlockData instanceof Bed) {
                                             Bed bed = (Bed)rawBlockData;
                                             if (bed.getPart() == Part.FOOT) {
                                                Block adjacentBlock = block.getRelative(bed.getFacing());
                                                Bed bedData = (Bed)rawBlockData.clone();
                                                bedData.setPart(Part.HEAD);
                                                BlockUtils.prepareTypeAndData(chunkChanges, adjacentBlock, rowType, bedData, false);
                                                if (countBlock) {
                                                   updateBlockCount(finalUserString, 1);
                                                }
                                             }

                                             BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, true);
                                             return countBlock;
                                          }

                                          boolean physics = true;
                                          BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, physics);
                                          return countBlock;
                                       }

                                       BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, true);
                                       ChestTool.updateDoubleChest(block, blockData, true);
                                       return countBlock;
                                    }

                                    if (countBlock) {
                                       updateBlockCount(finalUserString, 1);
                                    }

                                    block.setType(rowType, false);
                                    Door door = (Door)block.getBlockData();
                                    if (rowData >= 8) {
                                       door.setHalf(Half.TOP);
                                       rowData -= 8;
                                    } else {
                                       door.setHalf(Half.BOTTOM);
                                    }

                                    if (rowData >= 4) {
                                       door.setHinge(Hinge.RIGHT);
                                       rowData -= 4;
                                    } else {
                                       door.setHinge(Hinge.LEFT);
                                    }

                                    BlockFace face = BlockFace.NORTH;
                                    switch (rowData) {
                                       case 0:
                                          face = BlockFace.EAST;
                                          break;
                                       case 1:
                                          face = BlockFace.SOUTH;
                                          break;
                                       case 2:
                                          face = BlockFace.WEST;
                                    }

                                    door.setFacing(face);
                                    door.setOpen(false);
                                    block.setBlockData(door, false);
                                    return false;
                                 }

                                 BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, false);
                                 if (countBlock) {
                                    updateBlockCount(finalUserString, 1);
                                 }

                                 if (meta != null) {
                                    CommandBlock commandBlock = (CommandBlock)block.getState();

                                    for (Object valuexx : meta) {
                                       if (valuexx instanceof String) {
                                          String string = (String)valuexx;
                                          commandBlock.setCommand(string);
                                          commandBlock.update();
                                       }
                                    }
                                 }

                                 return false;
                              }

                              BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, false);
                              if (rowData > 0) {
                                 Queue.queueSkullUpdate(rowUser, block.getState(), rowData);
                              }

                              return countBlock;
                           }

                           if (clearInventories) {
                              if (BlockGroup.CONTAINERS.contains(changeType)) {
                                 Inventory inventory = BlockUtils.getContainerInventory(block.getState(), false);
                                 if (inventory != null) {
                                    inventory.clear();
                                 }
                              } else if (BlockGroup.CONTAINERS.contains(Material.ARMOR_STAND) && oldTypeMaterial == Material.ARMOR_STAND) {
                                 for (Entity entityxxx : block.getChunk().getEntities()) {
                                    if (entityxxx instanceof ArmorStand) {
                                       Location entityLocation = entityxxx.getLocation();
                                       entityLocation.setY(entityLocation.getY() + 0.99);
                                       if (entityLocation.getBlockX() == rowX && entityLocation.getBlockY() == rowY && entityLocation.getBlockZ() == rowZ) {
                                          ItemUtils.getEntityEquipment((ArmorStand)entityxxx).clear();
                                          entityLocation.setY(entityLocation.getY() - 1.99);
                                          PaperAdapter.ADAPTER.teleportAsync(entityxxx, entityLocation);
                                          entityxxx.remove();
                                       }
                                    }
                                 }
                              }
                           }

                           boolean remove = true;
                           if (rowType == Material.AIR) {
                              if (pendingChangeData instanceof Waterlogged) {
                                 Waterlogged waterlogged = (Waterlogged)pendingChangeData;
                                 if (waterlogged.isWaterlogged()) {
                                    BlockUtils.prepareTypeAndData(chunkChanges, block, Material.WATER, Material.WATER.createBlockData(), true);
                                    remove = false;
                                 }
                              } else if (pendingChangeType == Material.WATER && rawBlockData instanceof Waterlogged) {
                                 Waterlogged waterlogged = (Waterlogged)rawBlockData;
                                 if (waterlogged.isWaterlogged()) {
                                    remove = false;
                                 }
                              }
                           }

                           if (remove) {
                              boolean physics = true;
                              if (changeType == Material.NETHER_PORTAL
                                 || changeBlockData instanceof MultipleFacing
                                 || changeBlockData instanceof Snow
                                 || changeBlockData instanceof Stairs
                                 || changeBlockData instanceof RedstoneWire
                                 || changeBlockData instanceof Chest) {
                                 physics = true;
                              } else if (changeBlockData instanceof Bisected && !(changeBlockData instanceof TrapDoor)) {
                                 Bisected bisectedx = (Bisected)changeBlockData;
                                 Location bisectLocationx = block.getLocation().clone();
                                 if (bisectedx.getHalf() == Half.TOP) {
                                    bisectLocationx.setY(bisectLocationx.getY() - 1.0);
                                 } else {
                                    bisectLocationx.setY(bisectLocationx.getY() + 1.0);
                                 }

                                 int worldMaxHeightx = bukkitWorld.getMaxHeight();
                                 int worldMinHeightx = BukkitAdapter.ADAPTER.getMinHeight(bukkitWorld);
                                 if (bisectLocationx.getBlockY() >= worldMinHeightx && bisectLocationx.getBlockY() < worldMaxHeightx) {
                                    Block bisectBlock = block.getWorld().getBlockAt(bisectLocationx);
                                    BlockUtils.prepareTypeAndData(chunkChanges, bisectBlock, rowType, null, false);
                                    if (countBlock) {
                                       updateBlockCount(finalUserString, 1);
                                    }
                                 }
                              } else if (changeBlockData instanceof Bed) {
                                 Bed bed = (Bed)changeBlockData;
                                 if (bed.getPart() == Part.FOOT) {
                                    Block adjacentBlock = block.getRelative(bed.getFacing());
                                    BlockUtils.prepareTypeAndData(chunkChanges, adjacentBlock, rowType, null, false);
                                 }
                              }

                              BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, null, physics);
                           }

                           return countBlock;
                        }
                     }
                  }
               } else {
                  HangingUtil.spawnHanging(block.getState(), rowType, blockDataString, rowData);
               }
            } else {
               HangingUtil.removeHanging(block.getState(), blockDataString);
            }
         }
      } catch (Exception var36) {
         var36.printStackTrace();
      }

      if (rowType != Material.AIR && changeBlock && rowUser.length() > 0) {
         CacheHandler.lookupCache.put(rowX + "." + rowY + "." + rowZ + "." + rowWorldId, new Object[]{unixtimestamp, rowUser, rowType});
      }

      return countBlock;
   }

   protected static void updateBlockCount(String userString, int increment) {
      int[] rollbackHashData = ConfigHandler.rollbackHash.get(userString);
      int itemCount = rollbackHashData[0];
      int blockCount = rollbackHashData[1];
      int entityCount = rollbackHashData[2];
      int scannedWorlds = rollbackHashData[4];
      blockCount += increment;
      ConfigHandler.rollbackHash.put(userString, new int[]{itemCount, blockCount, entityCount, 0, scannedWorlds});
   }

   public static void applyBlockChanges(Map<Block, BlockData> chunkChanges, int preview, Player user) {
      for (Entry<Block, BlockData> chunkChange : chunkChanges.entrySet()) {
         Block changeBlock = chunkChange.getKey();
         BlockData changeBlockData = chunkChange.getValue();
         if (preview > 0 && user != null) {
            Util.sendBlockChange(user, changeBlock.getLocation(), changeBlockData);
         } else {
            BlockUtils.setTypeAndData(changeBlock, null, changeBlockData, true);
         }
      }

      chunkChanges.clear();
   }
}
