package de.tr7zw.nbtapi.plugin.tests.tiles;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.plugin.tests.Test;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class TilesCustomNBTPersistentTest implements Test {
   @Override
   public void test() throws Exception {
      if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId()) {
         if (!MinecraftVersion.isFoliaPresent()) {
            if (!Bukkit.getWorlds().isEmpty()) {
               World world = (World)Bukkit.getWorlds().get(0);

               try {
                  Block block = world.getBlockAt(world.getSpawnLocation().getBlockX(), 250, world.getSpawnLocation().getBlockZ());
                  if (world.isChunkLoaded(block.getX() >> 4, block.getZ() >> 4) && block.getType() == Material.AIR) {
                     block.setType(Material.CHEST);
                     NBT.modifyPersistentData(block.getState(), persistentData -> persistentData.setString("Foo", "Bar"));
                     NBT.getPersistentData(block.getState(), persistentData -> {
                        if (!persistentData.getString("Foo").equals("Bar")) {
                           block.setType(Material.AIR);
                           throw new NbtApiException("Custom Data did not save to the Tile!");
                        } else {
                           return null;
                        }
                     });
                     block.setType(Material.AIR);
                  }
               } catch (Exception var3) {
                  throw new NbtApiException("Wasn't able to use NBTTiles!", var3);
               }
            }
         }
      }
   }
}
