package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.block.BlockCompat;
import ch.njol.skript.bukkitutil.block.BlockValues;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class BlockUtils {
   public static boolean set(Block block, Material type, @Nullable BlockValues blockValues, boolean applyPhysics) {
      int flags = 13;
      if (applyPhysics) {
         flags |= 16;
      }

      BlockCompat.SETTER.setBlock(block, type, blockValues, flags);
      return true;
   }

   public static boolean set(Block block, ItemData type, boolean applyPhysics) {
      return set(block, type.getType(), type.getBlockValues(), applyPhysics);
   }

   public static void sendBlockChange(Player player, Location location, Material type, @Nullable BlockValues blockValues) {
      BlockCompat.SETTER.sendBlockChange(player, location, type, blockValues);
   }

   public static Iterable<Block> getBlocksAround(Block b) {
      return Arrays.asList(b.getRelative(BlockFace.NORTH), b.getRelative(BlockFace.EAST), b.getRelative(BlockFace.SOUTH), b.getRelative(BlockFace.WEST));
   }

   public static Iterable<BlockFace> getFaces() {
      return Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
   }

   @Nullable
   public static Location getLocation(@Nullable Block b) {
      if (b == null) {
         return null;
      } else {
         Location l = b.getLocation().add(0.5, 0.5, 0.5);
         BlockFace blockFace = Direction.getFacing(b);
         if (blockFace != BlockFace.SELF) {
            l.setPitch(Direction.getPitch(Math.asin(blockFace.getModY())));
            l.setYaw(Direction.getYaw(Math.atan2(blockFace.getModZ(), blockFace.getModX())));
         }

         return l;
      }
   }

   @Nullable
   public static BlockData createBlockData(String dataString) {
      String data = dataString.replace(";", ",");
      data = data.replaceAll(" (?=[^\\[]*])", "");
      data = data.replaceAll("\\s+\\[", "[");
      data = data.replace(" ", "_");
      String errorData = new String(data);

      try {
         return Bukkit.createBlockData(data.startsWith("minecraft:") ? data : "minecraft:" + data);
      } catch (IllegalArgumentException var8) {
         try {
            String alias = dataString.substring(0, dataString.lastIndexOf("["));
            data = data.substring(data.lastIndexOf("["));
            ItemType type = Aliases.parseItemType(alias);
            return type == null ? null : Bukkit.createBlockData(type.getMaterial(), data);
         } catch (StringIndexOutOfBoundsException var6) {
            return null;
         } catch (IllegalArgumentException var7) {
            Skript.error("Block data '" + errorData + "' is not valid for this material");
            return null;
         }
      }
   }

   @Nullable
   public static String blockToString(Block block, int flags) {
      String type = ItemType.toString(block, flags);
      Location location = getLocation(block);
      if (location == null) {
         return null;
      } else {
         double x = location.getX();
         double y = location.getY();
         double z = location.getZ();
         World world = location.getWorld();
         return world == null
            ? String.format("'%s' at %s, %s, %s", type, x, y, z)
            : String.format("'%s' at %s, %s, %s in world '%s'", type, x, y, z, world.getName());
      }
   }

   public static Block extractBlock(Block block) {
      return block instanceof DelayedChangeBlock ? ((DelayedChangeBlock)block).block : block;
   }
}
