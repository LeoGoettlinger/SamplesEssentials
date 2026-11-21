package com.viaversion.viabackwards.protocol.v1_14to1_13_2.storage;

import com.viaversion.viaversion.libs.fastutil.longs.Long2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Arrays;

public class ChunkLightStorage {
   public static final byte[] FULL_LIGHT = new byte[2048];
   public static final byte[] EMPTY_LIGHT = new byte[2048];
   private final Long2ObjectMap<ChunkLightStorage.ChunkLight> storedLight = new Long2ObjectOpenHashMap();

   public void setStoredLight(byte[][] skyLight, byte[][] blockLight, int x, int z) {
      this.storedLight.put(this.getChunkSectionIndex(x, z), new ChunkLightStorage.ChunkLight(skyLight, blockLight));
   }

   public ChunkLightStorage.ChunkLight getStoredLight(int x, int z) {
      return (ChunkLightStorage.ChunkLight)this.storedLight.get(this.getChunkSectionIndex(x, z));
   }

   public void clear() {
      this.storedLight.clear();
   }

   public void unloadChunk(int x, int z) {
      this.storedLight.remove(this.getChunkSectionIndex(x, z));
   }

   private long getChunkSectionIndex(int x, int z) {
      return (x & 67108863L) << 38 | z & 67108863L;
   }

   static {
      Arrays.fill(FULL_LIGHT, (byte)-1);
      Arrays.fill(EMPTY_LIGHT, (byte)0);
   }

   public record ChunkLight(byte[][] skyLight, byte[][] blockLight) {
      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            ChunkLightStorage.ChunkLight that = (ChunkLightStorage.ChunkLight)o;
            return !Arrays.deepEquals(this.skyLight, that.skyLight) ? false : Arrays.deepEquals(this.blockLight, that.blockLight);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = Arrays.deepHashCode(this.skyLight);
         return 31 * result + Arrays.deepHashCode(this.blockLight);
      }
   }
}
