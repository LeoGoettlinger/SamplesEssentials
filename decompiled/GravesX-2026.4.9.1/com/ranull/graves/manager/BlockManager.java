package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.data.ChunkData;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.LocationUtil;
import com.ranull.graves.util.MaterialUtil;
import dev.cwhead.GravesX.util.SkinTextureUtil_post_1_21_9;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.jay.GravesX.util.SkinTextureUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.jetbrains.annotations.NotNull;

public class BlockManager {
   private final Graves plugin;

   public BlockManager(Graves plugin) {
      this.plugin = plugin;
   }

   public BlockData getBlockData(Block block) {
      if (this.plugin.getDataManager().hasChunkData(block.getLocation())) {
         ChunkData chunkData = this.plugin.getDataManager().getChunkData(block.getLocation());
         if (chunkData.getBlockDataMap().containsKey(block.getLocation())) {
            return chunkData.getBlockDataMap().get(block.getLocation());
         }
      }

      return null;
   }

   public Grave getGraveFromBlock(Block block) {
      BlockData blockData = this.getBlockData(block);
      return blockData != null && this.plugin.getCacheManager().getGraveMap().containsKey(blockData.getGraveUUID())
         ? this.plugin.getCacheManager().getGraveMap().get(blockData.getGraveUUID())
         : null;
   }

   public Grave getGraveFromBlockLocation(Location location) {
      if (location == null) {
         return null;
      } else {
         Block block = location.getBlock();
         BlockData blockData = this.getBlockData(block);
         return blockData != null && this.plugin.getCacheManager().getGraveMap().containsKey(blockData.getGraveUUID())
            ? this.plugin.getCacheManager().getGraveMap().get(blockData.getGraveUUID())
            : null;
      }
   }

   public void createBlock(Location location, Grave grave) {
      this.createBlock(location, grave, false);
   }

   public void createBlock(Location location, Grave grave, boolean storeReplacementData) {
      location = LocationUtil.roundLocation(location);
      if (location.getWorld() != null) {
         Material material;
         if (this.plugin.getConfigManager().getConfigSection("block.enabled", grave).getBoolean("block.enabled")) {
            String materialString = this.plugin.getConfigManager().getConfigSection("block.material", grave).getString("block.material", "CHEST");
            if (materialString.equals("PLAYER_HEAD") && !this.plugin.getVersionManager().hasBlockData()) {
               materialString = "SKULL";
            }

            material = Material.matchMaterial(materialString);
         } else {
            material = null;
         }

         int offsetX = this.plugin.getConfigManager().getConfigSection("block.offset.x", grave).getInt("block.offset.x");
         int offsetY = this.plugin.getConfigManager().getConfigSection("block.offset.y", grave).getInt("block.offset.y");
         int offsetZ = this.plugin.getConfigManager().getConfigSection("block.offset.z", grave).getInt("block.offset.z");
         location.add(offsetX, offsetY, offsetZ);
         BlockData blockData = this.plugin.getCompatibility().setBlockData(location, material, grave, this.plugin);
         if (!storeReplacementData) {
            blockData = new BlockData(location, grave.getUUID(), null, null);
         }

         this.plugin.getDataManager().addBlockData(blockData);
         if (this.plugin.getIntegrationManager().hasMultiPaper()) {
            this.plugin.getIntegrationManager().getMultiPaper().notifyBlockCreation(blockData);
         }

         if (this.plugin.getIntegrationManager().hasItemsAdder()) {
            this.plugin.getIntegrationManager().getItemsAdder().createBlock(location, grave);
         }

         if (this.plugin.getIntegrationManager().hasOraxen()) {
            this.plugin.getIntegrationManager().getOraxen().createBlock(location, grave);
         }

         if (this.plugin.getIntegrationManager().hasNexo()) {
            this.plugin.getIntegrationManager().getNexo().createBlock(location, grave);
         }

         if (material != null) {
            this.plugin
               .debugMessage(
                  "Placing grave block for "
                     + grave.getUUID()
                     + " at "
                     + location.getWorld().getName()
                     + ", "
                     + (location.getBlockX() + 0.5)
                     + "x, "
                     + (location.getBlockY() + 0.5)
                     + "y, "
                     + (location.getBlockZ() + 0.5)
                     + "z",
                  1
               );
         } else {
            this.plugin
               .debugMessage(
                  "Placing access location for "
                     + grave.getUUID()
                     + " at "
                     + location.getWorld().getName()
                     + ", "
                     + (location.getBlockX() + 0.5)
                     + "x, "
                     + (location.getBlockY() + 0.5)
                     + "y, "
                     + (location.getBlockZ() + 0.5)
                     + "z",
                  1
               );
         }
      }
   }

   public List<BlockData> getBlockDataList(Grave grave) {
      List<BlockData> blockDataList = new ArrayList<>();

      for (Entry<String, ChunkData> chunkDataEntry : this.plugin.getCacheManager().getChunkMap().entrySet()) {
         for (BlockData blockData : new ArrayList<>(chunkDataEntry.getValue().getBlockDataMap().values())) {
            if (grave.getUUID().equals(blockData.getGraveUUID())) {
               blockDataList.add(blockData);
            }
         }
      }

      return blockDataList;
   }

   public List<Location> getBlockList(Grave grave) {
      List<Location> locationList = new ArrayList<>();

      for (Entry<String, ChunkData> chunkDataEntry : this.plugin.getCacheManager().getChunkMap().entrySet()) {
         for (BlockData blockData : new ArrayList<>(chunkDataEntry.getValue().getBlockDataMap().values())) {
            if (grave.getUUID().equals(blockData.getGraveUUID())) {
               locationList.add(blockData.getLocation());
            }
         }
      }

      return locationList;
   }

   public void removeBlock(Grave grave) {
      for (ChunkData chunkData : this.plugin.getCacheManager().getChunkMap().values()) {
         if (chunkData.isLoaded()) {
            for (BlockData blockData : new ArrayList<>(chunkData.getBlockDataMap().values())) {
               if (grave.getUUID().equals(blockData.getGraveUUID())) {
                  this.removeBlock(blockData);
               }
            }
         }
      }
   }

   public void removeBlock(BlockData blockData) {
      Location location = blockData.getLocation();
      boolean hasReplacementData = this.hasReplacementData(blockData);
      if (this.plugin.getIntegrationManager().hasItemsAdder() && this.plugin.getIntegrationManager().getItemsAdder().isCustomBlock(location)) {
         this.plugin.getIntegrationManager().getItemsAdder().removeBlock(location);
      }

      if (this.plugin.getIntegrationManager().hasOraxen() && this.plugin.getIntegrationManager().getOraxen().isCustomBlock(location)) {
         this.plugin.getIntegrationManager().getOraxen().removeBlock(location);
      }

      if (this.plugin.getIntegrationManager().hasNexo() && this.plugin.getIntegrationManager().getNexo().isCustomBlock(location)) {
         this.plugin.getIntegrationManager().getNexo().removeBlock(location);
      }

      Grave grave = this.plugin.getCacheManager().getGrave(blockData.getGraveUUID());
      if (!hasReplacementData && !this.isGraveBlock(location, grave)) {
         this.plugin.getDataManager().removeBlockData(location);
      } else {
         if (location.getWorld() != null) {
            if (hasReplacementData) {
               Material material = Material.matchMaterial(blockData.getReplaceMaterial());
               if (material != null) {
                  location.getBlock().setType(material);
               }
            }

            if (hasReplacementData) {
               String raw = blockData.getReplaceData();
               String bd = raw;
               String gx = null;
               String MARKER = "||GXHEAD||";
               if (raw != null) {
                  int idx = raw.lastIndexOf(MARKER);
                  if (idx >= 0) {
                     bd = raw.substring(0, idx);
                     gx = raw.substring(idx + MARKER.length()).trim();
                  }
               }

               if (bd != null && !bd.isEmpty()) {
                  try {
                     location.getBlock().setBlockData(this.plugin.getServer().createBlockData(bd));
                  } catch (Throwable var23) {
                  }
               }

               if (gx != null && !gx.isEmpty() && location.getBlock().getState() instanceof Skull skull) {
                  String tx = null;
                  String on = null;
                  String ou = null;
                  String nm = null;

                  try {
                     Pattern p = Pattern.compile("\\\"(tx|on|ou|nm)\\\"\\s*:\\s*\\\"(.*?)\\\"");
                     Matcher m = p.matcher(gx);
                     Map<String, String> map = new HashMap<>();

                     while (m.find()) {
                        map.put(m.group(1), m.group(2).replace("\\\"", "\"").replace("\\\\", "\\"));
                     }

                     tx = map.get("tx");
                     on = map.get("on");
                     ou = map.get("ou");
                     nm = map.get("nm");
                  } catch (Throwable var25) {
                  }

                  try {
                     if (tx != null && !tx.isEmpty()) {
                        if (this.plugin.getVersionManager().isPost1_21_9()) {
                           SkinTextureUtil_post_1_21_9.setSkullBlockTexture(skull, on != null && !on.isEmpty() ? on : "gravesx", tx);
                        } else {
                           SkinTextureUtil.setSkullBlockTexture(skull, on != null && !on.isEmpty() ? on : "gravesx", tx);
                        }
                     } else if (ou != null && !ou.isEmpty()) {
                        try {
                           skull.setOwningPlayer(this.plugin.getServer().getOfflinePlayer(UUID.fromString(ou)));
                        } catch (Throwable var22) {
                        }
                     } else if (on != null && !on.isEmpty()) {
                        try {
                           skull.setOwningPlayer(this.plugin.getServer().getOfflinePlayer(on));
                        } catch (Throwable var21) {
                        }
                     }
                  } catch (Throwable var24) {
                  }

                  if (nm != null && !nm.isEmpty()) {
                     String rawName = getRawName(nm);

                     try {
                        if (this.plugin.getIntegrationManager().hasMiniMessage() && !rawName.isEmpty()) {
                           skull.setOwner(MiniMessage.parseString(rawName));
                        }
                     } catch (Throwable var20) {
                        try {
                           if (!rawName.isEmpty()) {
                              skull.setOwner(rawName);
                           }
                        } catch (Throwable var19) {
                        }
                     }
                  }

                  try {
                     skull.update(true, false);
                  } catch (Throwable var18) {
                  }
               }
            }

            this.plugin.getDataManager().removeBlockData(location);
            this.plugin
               .debugMessage(
                  (hasReplacementData ? "Replacing" : "Removing")
                     + " grave block for "
                     + blockData.getGraveUUID()
                     + " at "
                     + location.getWorld().getName()
                     + ", "
                     + (location.getBlockX() + 0.5)
                     + "x, "
                     + (location.getBlockY() + 0.5)
                     + "y, "
                     + (location.getBlockZ() + 0.5)
                     + "z",
                  1
               );
         }
      }
   }

   private boolean hasReplacementData(BlockData blockData) {
      String replaceMaterial = blockData.getReplaceMaterial();
      return replaceMaterial != null && !MaterialUtil.isAir(replaceMaterial) ? Material.matchMaterial(replaceMaterial) != null : false;
   }

   @NotNull
   private static String getRawName(String nm) {
      String rawName = nm;
      if (nm.startsWith("{") && nm.contains("\"text\"")) {
         int i = nm.indexOf("\"text\"");
         if (i >= 0) {
            int c = nm.indexOf(58, i);
            int q1 = nm.indexOf(34, c + 1);
            int q2 = q1 >= 0 ? nm.indexOf(34, q1 + 1) : -1;
            if (q1 >= 0 && q2 > q1) {
               rawName = nm.substring(q1 + 1, q2);
            }
         }
      }

      return rawName;
   }

   private boolean isGraveBlock(Location loc, Grave grave) {
      if (loc == null) {
         return false;
      } else if (grave == null) {
         return false;
      } else {
         Block block = loc.getBlock();
         Material mat = block.getType();
         return mat == Material.AIR
            ? false
            : mat
               == Material.valueOf(this.plugin.getConfigManager().getConfigSection("block.material", grave).getString("block.material", "CHEST").toUpperCase());
      }
   }
}
