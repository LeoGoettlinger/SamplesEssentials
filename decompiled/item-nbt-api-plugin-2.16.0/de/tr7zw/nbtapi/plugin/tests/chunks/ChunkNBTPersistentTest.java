package de.tr7zw.nbtapi.plugin.tests.chunks;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTType;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.plugin.tests.Test;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkNBTPersistentTest implements Test {
   @Override
   public void test() throws Exception {
      if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_16_R3.getVersionId()) {
         List<World> loadedWorlds = Bukkit.getWorlds();
         if (!loadedWorlds.isEmpty()) {
            World world = loadedWorlds.get(0);
            Chunk[] loadedChunks = world.getLoadedChunks();
            if (loadedChunks.length != 0) {
               Chunk chunk = loadedChunks[0];

               try {
                  NBT.modifyChunkPDC(chunk, nbt -> nbt.setString("Foo", "Bar"));
                  if (!NBT.readAndGetChunkPDC(chunk, nbt -> nbt.hasTag("Foo", NBTType.NBTTagString) && "Bar".equals(nbt.getString("Foo")))) {
                     throw new NbtApiException("Custom Data did not save to the Chunk!");
                  } else {
                     NBT.modifyChunkPDC(chunk, nbt -> nbt.removeKey("Foo"));
                     if (NBT.readAndGetChunkPDC(chunk, nbt -> nbt.hasTag("Foo"))) {
                        throw new NbtApiException("Unable to remove key from Chunk!");
                     }
                  }
               } catch (Exception var6) {
                  throw new NbtApiException("Wasn't able to use NBTChunks!", var6);
               }
            }
         }
      }
   }
}
