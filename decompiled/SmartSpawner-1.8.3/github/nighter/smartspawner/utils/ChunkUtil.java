package github.nighter.smartspawner.utils;

public class ChunkUtil {
   public static long getChunkKey(int x, int z) {
      return x & 4294967295L | (z & 4294967295L) << 32;
   }

   public static int getChunkX(long key) {
      return (int)(key & 4294967295L);
   }

   public static int getChunkZ(long key) {
      return (int)(key >>> 32);
   }
}
