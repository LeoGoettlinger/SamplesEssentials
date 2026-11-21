package com.fastasyncworldedit.core.queue;

public interface IChunkCache<T> extends Trimable {
   T get(int chunkX, int chunkZ);

   @Override
   default boolean trim(boolean aggressive) {
      return false;
   }
}
