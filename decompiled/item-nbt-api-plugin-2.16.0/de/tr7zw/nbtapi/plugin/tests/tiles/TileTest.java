package de.tr7zw.nbtapi.plugin.tests.tiles;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTTileEntity;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.plugin.tests.Test;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class TileTest implements Test {
   @Override
   public void test() throws Exception {
      if (!MinecraftVersion.isFoliaPresent()) {
         if (!Bukkit.getWorlds().isEmpty()) {
            World world = (World)Bukkit.getWorlds().get(0);

            try {
               Block block = world.getBlockAt(world.getSpawnLocation().getBlockX(), 254, world.getSpawnLocation().getBlockZ());
               if (world.isChunkLoaded(block.getX() >> 4, block.getZ() >> 4) && block.getType() == Material.AIR) {
                  block.setType(Material.CHEST);
                  if (MinecraftVersion.isNewerThan(MinecraftVersion.MC1_21_R1)) {
                     NBT.modify(block.getState(), nbt -> nbt.setString("foo", "bar"));
                     block.setType(Material.AIR);
                     return;
                  }

                  NBTTileEntity tile = new NBTTileEntity(block.getState());
                  if (!MinecraftVersion.isNewerThan(MinecraftVersion.MC1_17_R1) && tile.getInteger("y") != 254) {
                     block.setType(Material.AIR);
                     throw new NbtApiException("The Tile Y pos wasn't correct!");
                  }

                  tile.setString("Lock", "test");
                  if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3) && !tile.hasTag("Lock") && !"test".equals(tile.getString("test"))) {
                     block.setType(Material.AIR);
                     throw new NbtApiException("The Lock wasn't successfully set.");
                  }

                  block.setType(Material.AIR);
               }
            } catch (Exception var4) {
               throw new NbtApiException("Wasn't able to use NBTTiles!", var4);
            }
         }
      }
   }
}
