package com.fastasyncworldedit.core.extent.processor.lighting;

import java.util.concurrent.locks.ReentrantLock;

public interface Relighter extends AutoCloseable {
   boolean addChunk(int cx, int cz, byte[] skipReason, int bitmask);

   void addLightUpdate(int x, int y, int z);

   void fixLightingSafe(boolean sky);

   default void removeAndRelight(boolean sky) {
      this.removeLighting();
      this.fixLightingSafe(sky);
   }

   void clear();

   void removeLighting();

   void fixBlockLighting();

   void fixSkyLighting();

   boolean isEmpty();

   ReentrantLock getLock();

   boolean isFinished();

   public static class SkipReason {
      public static final byte NONE = 0;
      public static final byte AIR = 1;
      public static final byte SOLID = 2;
   }
}
