package net.coreprotect.worldedit;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.Locale;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import net.coreprotect.utility.BlockUtils;
import net.coreprotect.utility.EntityUtils;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class WorldEditLogger extends Queue {
   public static WorldEditPlugin getWorldEdit(Server server) {
      Plugin plugin = server.getPluginManager().getPlugin("WorldEdit");
      return plugin != null && plugin instanceof WorldEditPlugin ? (WorldEditPlugin)plugin : null;
   }

   protected static BaseBlock getBaseBlock(Extent extent, BlockVector3 position, Location location, Material oldType, BlockState oldBlock) {
      return oldType != Material.SPAWNER && (!Config.getConfig(location.getWorld()).SIGN_TEXT || !BukkitAdapter.ADAPTER.isSign(oldType))
         ? null
         : extent.getFullBlock(position);
   }

   protected static void postProcess(
      Extent extent,
      Actor actor,
      BlockVector3 position,
      Location location,
      BlockStateHolder<?> blockStateHolder,
      BaseBlock baseBlock,
      Material oldType,
      BlockState oldBlockState,
      ItemStack[] containerContents
   ) {
      BlockData oldBlockData = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(oldBlockState);
      BlockData newBlockData = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(blockStateHolder.toImmutableState());
      Material newType = newBlockData.getMaterial();
      String oldBlockDataString = oldBlockData.getAsString();
      String newBlockDataString = newBlockData.getAsString();
      org.bukkit.block.BlockState oldBlock = new WorldEditBlockState(location, oldType, oldBlockData);
      org.bukkit.block.BlockState newBlock = new WorldEditBlockState(location, newType, newBlockData);
      int oldBlockExtraData = 0;
      int newBlockExtraData = -1;
      if (!oldType.equals(newType) || !oldBlockDataString.equals(newBlockDataString)) {
         try {
            if (baseBlock != null && baseBlock.hasNbtData()) {
               if (Config.getConfig(location.getWorld()).SIGN_TEXT && BukkitAdapter.ADAPTER.isSign(oldType)) {
                  CompoundTag compoundTag = baseBlock.getNbtData();
                  if (!compoundTag.containsKey("front_text")) {
                     String line1 = getSignText(compoundTag.getString("Text1"));
                     String line2 = getSignText(compoundTag.getString("Text2"));
                     String line3 = getSignText(compoundTag.getString("Text3"));
                     String line4 = getSignText(compoundTag.getString("Text4"));
                     int color = DyeColor.valueOf(baseBlock.getNbtData().getString("Color").toUpperCase()).getColor().asRGB();
                     int colorSecondary = 0;
                     boolean frontGlowing = compoundTag.getInt("GlowingText") == 1;
                     boolean backGlowing = false;
                     boolean isWaxed = false;
                     boolean isFront = true;
                     Queue.queueSignText(
                        actor.getName(),
                        location,
                        0,
                        color,
                        colorSecondary,
                        frontGlowing,
                        backGlowing,
                        isWaxed,
                        isFront,
                        line1,
                        line2,
                        line3,
                        line4,
                        "",
                        "",
                        "",
                        "",
                        5
                     );
                  }
               }

               if (oldType == Material.SPAWNER) {
                  String mobType = getMobType(baseBlock);
                  if (mobType != null) {
                     try {
                        EntityType entityType = EntityType.valueOf(mobType);
                        oldBlockExtraData = EntityUtils.getSpawnerType(entityType);
                     } catch (IllegalArgumentException var29) {
                     }
                  }
               }
            }

            if (containerContents != null) {
               Queue.queueContainerBreak(actor.getName(), location, oldType, containerContents);
            }
         } catch (Exception var30) {
            var30.printStackTrace();
         }

         if (!newType.equals(Material.SKELETON_SKULL)
            && !newType.equals(Material.SKELETON_WALL_SKULL)
            && !newType.equals(Material.WITHER_SKELETON_SKULL)
            && !newType.equals(Material.WITHER_SKELETON_WALL_SKULL)
            && !newType.equals(Material.ZOMBIE_HEAD)
            && !newType.equals(Material.ZOMBIE_WALL_HEAD)
            && !newType.equals(Material.PLAYER_HEAD)
            && !newType.equals(Material.PLAYER_WALL_HEAD)
            && !newType.equals(Material.CREEPER_HEAD)
            && !newType.equals(Material.CREEPER_WALL_HEAD)
            && !newType.equals(Material.DRAGON_HEAD)
            && !newType.equals(Material.DRAGON_WALL_HEAD)) {
            if ((oldType.equals(Material.AIR) || oldType.equals(Material.CAVE_AIR)) && !newType.equals(Material.AIR) && !newType.equals(Material.CAVE_AIR)) {
               Queue.queueBlockPlace(actor.getName(), newBlock, newType, null, newType, newBlockExtraData, 0, newBlockDataString);
            } else if (!oldType.equals(Material.AIR)
               && !oldType.equals(Material.CAVE_AIR)
               && !newType.equals(Material.AIR)
               && !newType.equals(Material.CAVE_AIR)) {
               Waterlogged waterlogged = BlockUtils.checkWaterlogged(newBlockData, oldBlock);
               if (waterlogged != null) {
                  newBlockDataString = waterlogged.getAsString();
                  oldBlock = null;
               }

               if (oldBlock != null) {
                  Queue.queueBlockBreak(actor.getName(), oldBlock, oldBlock.getType(), oldBlockDataString, null, oldBlockExtraData, 0);
               }

               Queue.queueBlockPlace(actor.getName(), newBlock, newType, null, newType, newBlockExtraData, 0, newBlockDataString);
            } else if (!oldType.equals(Material.AIR)
               && !oldType.equals(Material.CAVE_AIR)
               && (newType.equals(Material.AIR) || newType.equals(Material.CAVE_AIR))) {
               Queue.queueBlockBreak(actor.getName(), oldBlock, oldBlock.getType(), oldBlockDataString, null, oldBlockExtraData, 0);
               if (oldBlockData instanceof Waterlogged) {
                  Waterlogged waterloggedx = (Waterlogged)oldBlockData;
                  if (waterloggedx.isWaterlogged()) {
                     Queue.queueBlockPlace(actor.getName(), newBlock, newType, null, Material.WATER, -1, 0, null);
                  }
               } else if (oldBlockData instanceof Bisected) {
                  Bisected bisected = (Bisected)oldBlockData;
                  Location bisectLocation = location.clone();
                  if (bisected.getHalf() == Half.TOP) {
                     bisectLocation.setY(bisectLocation.getY() - 1.0);
                  } else {
                     bisectLocation.setY(bisectLocation.getY() + 1.0);
                  }

                  int worldMaxHeight = location.getWorld().getMaxHeight();
                  int worldMinHeight = BukkitAdapter.ADAPTER.getMinHeight(location.getWorld());
                  if (bisectLocation.getBlockY() >= worldMinHeight && bisectLocation.getBlockY() < worldMaxHeight) {
                     org.bukkit.block.BlockState bisectBlock = location.getWorld().getBlockAt(bisectLocation).getState();
                     Queue.queueBlockBreak(actor.getName(), bisectBlock, bisectBlock.getType(), bisectBlock.getBlockData().getAsString(), null, 0, 0);
                  }
               }
            }
         } else {
            Queue.queueBlockPlaceDelayed(actor.getName(), location, newType, newBlockDataString, oldBlock, 0);
         }
      }
   }

   private static String getMobType(BaseBlock fullBlock) {
      String mobType = null;

      try {
         CompoundTag compoundTag = (CompoundTag)NBTUtils.getChildTag(fullBlock.getNbtData().getValue(), "SpawnData", CompoundTag.class);
         mobType = compoundTag.getString("id").toUpperCase(Locale.ROOT);
         if (mobType.contains("MINECRAFT:")) {
            String[] mobTypeSplit = mobType.split(":");
            mobType = mobTypeSplit[1];
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return mobType;
   }

   private static String getSignText(String line) {
      String result = "";
      if (!line.startsWith("{\"text\":\"")) {
         return line;
      } else {
         try {
            JSONObject json = (JSONObject)new JSONParser().parse(line);
            return (String)json.get("text");
         } catch (Exception var3) {
            var3.printStackTrace();
            return result;
         }
      }
   }
}
