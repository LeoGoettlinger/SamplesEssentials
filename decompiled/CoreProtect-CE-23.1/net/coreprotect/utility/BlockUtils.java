package net.coreprotect.utility;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.coreprotect.CoreProtect;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.thread.CacheHandler;
import net.coreprotect.thread.Scheduler;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Jukebox;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BlockUtils {
   private static final String NAMESPACE = "minecraft:";

   private BlockUtils() {
      throw new IllegalStateException("Utility class");
   }

   public static byte[] stringToByteData(String string, int type) {
      byte[] result = null;
      if (string != null) {
         Material material = MaterialUtils.getType(type);
         if (material == null) {
            return result;
         }

         if (material.isBlock()
            && !createBlockData(material).getAsString().equals(string)
            && string.startsWith("minecraft:" + material.name().toLowerCase(Locale.ROOT) + "[")
            && string.endsWith("]")) {
            String substring = string.substring(material.name().length() + 11, string.length() - 1);
            String[] blockDataSplit = substring.split(",");
            ArrayList<String> blockDataArray = new ArrayList<>();

            for (String data : blockDataSplit) {
               int id = MaterialUtils.getBlockdataId(data, true);
               if (id > -1) {
                  blockDataArray.add(Integer.toString(id));
               }
            }

            string = String.join(",", blockDataArray);
         } else {
            if (string.contains(":") || material != Material.PAINTING && !BukkitAdapter.ADAPTER.isItemFrame(material)) {
               return result;
            }

            int id = MaterialUtils.getBlockdataId(string, true);
            if (id <= -1) {
               return result;
            }

            string = Integer.toString(id);
         }

         result = string.getBytes(StandardCharsets.UTF_8);
      }

      return result;
   }

   public static String byteDataToString(byte[] data, int type) {
      String result = "";
      if (data != null) {
         Material material = MaterialUtils.getType(type);
         if (material == null) {
            return result;
         }

         result = new String(data, StandardCharsets.UTF_8);
         if (result.length() > 0) {
            if (result.matches("\\d+")) {
               result = result + ",";
            }

            if (result.contains(",")) {
               String[] blockDataSplit = result.split(",");
               ArrayList<String> blockDataArray = new ArrayList<>();

               for (String blockData : blockDataSplit) {
                  String block = MaterialUtils.getBlockDataString(Integer.parseInt(blockData));
                  if (block.length() > 0) {
                     blockDataArray.add(block);
                  }
               }

               if (material != Material.PAINTING && !BukkitAdapter.ADAPTER.isItemFrame(material)) {
                  result = "minecraft:" + material.name().toLowerCase(Locale.ROOT) + "[" + String.join(",", blockDataArray) + "]";
               } else {
                  result = String.join(",", blockDataArray);
               }
            } else {
               result = "";
            }
         }
      }

      return result;
   }

   public static Waterlogged checkWaterlogged(BlockData blockData, BlockState blockReplacedState) {
      if (blockReplacedState.getType().equals(Material.WATER)
         && blockData instanceof Waterlogged
         && blockReplacedState.getBlockData().equals(Material.WATER.createBlockData())) {
         Waterlogged waterlogged = (Waterlogged)blockData;
         waterlogged.setWaterlogged(true);
         return waterlogged;
      } else {
         return null;
      }
   }

   public static boolean isAir(Material type) {
      return type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR;
   }

   public static boolean solidBlock(Material type) {
      return type.isSolid();
   }

   public static boolean passableBlock(Block block) {
      return block.isPassable();
   }

   public static Material getType(Block block) {
      return block.getType();
   }

   public static boolean iceBreakCheck(BlockState block, String user, Material type) {
      if (type.equals(Material.ICE)) {
         int unixtimestamp = (int)(System.currentTimeMillis() / 1000L);
         int wid = WorldUtils.getWorldId(block.getWorld().getName());
         CacheHandler.lookupCache.put(block.getX() + "." + block.getY() + "." + block.getZ() + "." + wid, new Object[]{unixtimestamp, user, Material.WATER});
         return true;
      } else {
         return false;
      }
   }

   public static BlockData createBlockData(Material material) {
      try {
         BlockData result = material.createBlockData();
         if (result instanceof Waterlogged) {
            ((Waterlogged)result).setWaterlogged(false);
         }

         return result;
      } catch (Exception var2) {
         return null;
      }
   }

   public static void prepareTypeAndData(Map<Block, BlockData> map, Block block, Material type, BlockData blockData, boolean update) {
      if (blockData == null) {
         blockData = createBlockData(type);
      }

      if (!update) {
         setTypeAndData(block, type, blockData, update);
         map.remove(block);
      } else {
         map.put(block, blockData);
      }
   }

   public static void setTypeAndData(Block block, Material type, BlockData blockData, boolean update) {
      if (blockData == null && type != null) {
         blockData = createBlockData(type);
      }

      if (blockData != null) {
         block.setBlockData(blockData, update);
      }
   }

   public static void updateBlock(BlockState block) {
      Scheduler.runTask(CoreProtect.getInstance(), () -> {
         try {
            if (block.getBlockData() instanceof Waterlogged) {
               Block currentBlock = block.getBlock();
               if (currentBlock.getType().equals(block.getType())) {
                  block.setBlockData(currentBlock.getBlockData());
               }
            }

            block.update();
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      }, block.getLocation());
   }

   public static Inventory getContainerInventory(BlockState blockState, boolean singleBlock) {
      Inventory inventory = null;

      try {
         if (blockState instanceof BlockInventoryHolder) {
            if (singleBlock) {
               List<Material> chests = new ArrayList<>(Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST));
               chests.addAll(BukkitAdapter.ADAPTER.copperChestMaterials());
               Material type = blockState.getType();
               if (chests.contains(type)) {
                  inventory = ((Chest)blockState).getBlockInventory();
               }
            }

            if (inventory == null) {
               inventory = ((BlockInventoryHolder)blockState).getInventory();
            }
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

      return inventory;
   }

   public static List<Object> processMeta(BlockState block) {
      List<Object> meta = new ArrayList<>();

      try {
         if (block instanceof CommandBlock) {
            CommandBlock commandBlock = (CommandBlock)block;
            String command = commandBlock.getCommand();
            if (command.length() > 0) {
               meta.add(command);
            }
         } else if (block instanceof Banner) {
            Banner banner = (Banner)block;
            meta.add(banner.getBaseColor());

            for (Pattern pattern : banner.getPatterns()) {
               meta.add(pattern.serialize());
            }
         } else if (block instanceof ShulkerBox) {
            ShulkerBox shulkerBox = (ShulkerBox)block;
            ItemStack[] inventory = shulkerBox.getSnapshotInventory().getStorageContents();
            int slot = 0;

            for (ItemStack itemStack : inventory) {
               Map<Integer, Object> itemMap = ItemUtils.serializeItemStackLegacy(itemStack, null, slot);
               if (itemMap.size() > 0) {
                  meta.add(itemMap);
               }

               slot++;
            }
         }
      } catch (Exception var10) {
         var10.printStackTrace();
      }

      if (meta.isEmpty()) {
         meta = null;
      }

      return meta;
   }

   public static ItemStack[] getJukeboxItem(Jukebox blockState) {
      ItemStack[] contents = null;

      try {
         contents = new ItemStack[]{blockState.getRecord()};
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return contents;
   }

   public static int getSignData(boolean frontGlowing, boolean backGlowing) {
      if (frontGlowing && backGlowing) {
         return 3;
      } else if (backGlowing) {
         return 2;
      } else {
         return frontGlowing ? 1 : 0;
      }
   }

   public static boolean isSideGlowing(boolean isFront, int data) {
      return isFront && (data == 1 || data == 3) || !isFront && (data == 2 || data == 3);
   }
}
