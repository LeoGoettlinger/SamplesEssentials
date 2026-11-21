package de.tr7zw.nbtapi.plugin.tests.blocks;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTType;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import de.tr7zw.nbtapi.plugin.tests.Test;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BlockNBTTest implements Test {
   @Override
   public void test() throws Exception {
      if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_16_R3.getVersionId()) {
         List<World> loadedWorlds = Bukkit.getWorlds();
         if (!loadedWorlds.isEmpty()) {
            World world = loadedWorlds.get(0);
            Chunk[] loadedChunks = world.getLoadedChunks();
            if (loadedChunks.length != 0) {
               try {
                  Chunk chunk = loadedChunks[0];
                  Block block = chunk.getBlock(0, 254, 0);
                  String blockKey = block.getX() + "_" + block.getY() + "_" + block.getZ();
                  NBT.modifyChunkPDC(block, nbt -> nbt.setString("Too", "Bar"));
                  boolean storedDataPresent = NBT.readAndGetChunkPDC(chunk, nbt -> {
                     if (!nbt.hasTag("blocks", NBTType.NBTTagCompound)) {
                        return false;
                     } else {
                        ReadableNBT blocksNbt = nbt.getCompound("blocks");
                        if (blocksNbt != null && blocksNbt.hasTag(blockKey, NBTType.NBTTagCompound)) {
                           ReadableNBT blockNbt = blocksNbt.getCompound(blockKey);
                           return blockNbt != null && blockNbt.hasTag("Too", NBTType.NBTTagString) && "Bar".equals(blockNbt.getString("Too"));
                        } else {
                           return false;
                        }
                     }
                  });
                  if (!storedDataPresent) {
                     throw new NbtApiException("Custom Data did not save to a Block!");
                  } else if (!NBT.readAndGetChunkPDC(block, nbt -> nbt.hasTag("Too"))) {
                     throw new NbtApiException("Custom Data did not save to a Block!");
                  } else {
                     boolean blockHadOnlyTestKey = NBT.readAndGetChunkPDC(block, nbt -> nbt.getKeys().size() == 1);
                     boolean chunkHadOnlyOneBlock = NBT.readAndGetChunkPDC(chunk, nbt -> nbt.getCompound("blocks").getKeys().size() == 1);
                     NBT.modifyChunkPDC(block, nbt -> nbt.removeKey("Too"));
                     if (NBT.readAndGetChunkPDC(block, nbt -> nbt.hasTag("Too"))) {
                        throw new NbtApiException("Unable to remove key from Block!");
                     } else {
                        if (chunkHadOnlyOneBlock) {
                           if (NBT.readAndGetChunkPDC(chunk, nbt -> nbt.hasTag("blocks"))) {
                              throw new NbtApiException("Chunk data wasn't cleaned up (hanging \"blocks\" compound)!");
                           }
                        } else if (blockHadOnlyTestKey && NBT.readAndGetChunkPDC(chunk, nbt -> nbt.getCompound("blocks").hasTag(blockKey))) {
                           throw new NbtApiException("Chunk data wasn't cleaned up (hanging block location compound)!");
                        }
                     }
                  }
               } catch (Exception var10) {
                  throw new NbtApiException("Wasn't able to use NBTBlocks!", var10);
               }
            }
         }
      }
   }
}
