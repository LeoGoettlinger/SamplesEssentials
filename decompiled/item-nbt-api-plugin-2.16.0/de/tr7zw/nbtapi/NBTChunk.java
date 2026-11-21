package de.tr7zw.nbtapi;

import de.tr7zw.nbtapi.utils.CheckUtil;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Chunk;

@Deprecated
public class NBTChunk {
   private final Chunk chunk;

   public NBTChunk(Chunk chunk) {
      this.chunk = chunk;
   }

   public NBTCompound getPersistentDataContainer() {
      CheckUtil.assertAvailable(MinecraftVersion.MC1_16_R3);
      return new NBTPersistentDataContainer(this.chunk.getPersistentDataContainer());
   }
}
