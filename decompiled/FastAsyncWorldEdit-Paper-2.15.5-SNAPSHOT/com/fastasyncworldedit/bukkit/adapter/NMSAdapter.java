package com.fastasyncworldedit.bukkit.adapter;

import com.fastasyncworldedit.bukkit.FaweBukkitWorld;
import com.fastasyncworldedit.core.FAWEPlatformAdapterImpl;
import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.math.IntPair;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.ReflectionUtils;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.function.IntFunction;

public class NMSAdapter implements FAWEPlatformAdapterImpl {
   public static int createPalette(
      int[] blockToPalette, int[] paletteToBlock, int[] blocksCopy, char[] set, CachedBukkitAdapter adapter, final boolean globalKindaDoesNotExist
   ) {
      int numPaletteEntries = 0;

      for (int i = 0; i < 4096; i++) {
         int ordinal = set[i];
         ordinal = Math.max(ordinal, 1);
         int palette = blockToPalette[ordinal];
         if (palette == Integer.MAX_VALUE) {
            blockToPalette[ordinal] = numPaletteEntries;
            paletteToBlock[numPaletteEntries] = ordinal;
            numPaletteEntries++;
         }
      }

      mapPalette(blockToPalette, paletteToBlock, blocksCopy, set, adapter, numPaletteEntries, globalKindaDoesNotExist);
      return numPaletteEntries;
   }

   public static int createPalette(
      int layer,
      int[] blockToPalette,
      int[] paletteToBlock,
      int[] blocksCopy,
      IntFunction<char[]> get,
      char[] set,
      CachedBukkitAdapter adapter,
      final boolean globalKindaDoesNotExist
   ) {
      int numPaletteEntries = 0;
      char[] getArr = null;

      for (int i = 0; i < 4096; i++) {
         char ordinal = set[i];
         if (ordinal == 0) {
            if (getArr == null) {
               getArr = get.apply(layer);
            }

            ordinal = getArr[i];
            set[i] = (char)Math.max(ordinal, 1);
         }

         int palette = blockToPalette[ordinal];
         if (palette == Integer.MAX_VALUE) {
            blockToPalette[ordinal] = numPaletteEntries;
            paletteToBlock[numPaletteEntries] = ordinal;
            numPaletteEntries++;
         }
      }

      mapPalette(blockToPalette, paletteToBlock, blocksCopy, set, adapter, numPaletteEntries, globalKindaDoesNotExist);
      return numPaletteEntries;
   }

   private static void mapPalette(
      int[] blockToPalette,
      int[] paletteToBlock,
      int[] blocksCopy,
      char[] set,
      CachedBukkitAdapter adapter,
      int numPaletteEntries,
      boolean globalKindaDoesNotExist
   ) {
      int bitsPerEntry = MathMan.log2nlz(numPaletteEntries - 1);
      if (!globalKindaDoesNotExist && bitsPerEntry > 8 && adapter != null) {
         System.arraycopy(adapter.getIbdToOrdinal(), 0, paletteToBlock, 0, adapter.getIbdToOrdinal().length);
         System.arraycopy(adapter.getOrdinalToIbdID(), 0, blockToPalette, 0, adapter.getOrdinalToIbdID().length);
      }

      int oldVal = blockToPalette[0];
      blockToPalette[0] = blockToPalette[1];

      for (int i = 0; i < 4096; i++) {
         int ordinal = set[i];
         int palette = blockToPalette[ordinal];
         blocksCopy[i] = palette;
      }

      blockToPalette[0] = oldVal;
   }

   @Override
   public void sendChunk(IChunkGet chunk, int mask, boolean lighting) {
      if (!(chunk instanceof AbstractBukkitGetBlocks)) {
         throw new IllegalArgumentException("(IChunkGet) chunk not of type BukkitGetBlocks");
      } else {
         ((AbstractBukkitGetBlocks)chunk).send();
      }
   }

   protected static <LevelChunkSection> boolean setSectionAtomic(
      String worldName, IntPair pair, LevelChunkSection[] sections, LevelChunkSection expected, LevelChunkSection value, int layer
   ) {
      if (layer < 0 || layer >= sections.length) {
         return false;
      } else if (Fawe.isMainThread()) {
         return ReflectionUtils.compareAndSet(sections, expected, value, layer);
      } else {
         NMSAdapter.StampLockHolder holder = new NMSAdapter.StampLockHolder();
         ConcurrentHashMap<IntPair, NMSAdapter.ChunkSendLock> chunks = FaweBukkitWorld.getWorldSendingChunksMap(worldName);
         chunks.compute(pair, (k, lock) -> {
            if (lock == null) {
               lock = new NMSAdapter.ChunkSendLock();
            } else if (lock.writeWaiting) {
               throw new IllegalStateException("Attempting to write chunk section when write is already ongoing?!");
            }

            holder.stamp = lock.lock.tryWriteLock();
            holder.chunkLock = lock;
            lock.writeWaiting = true;
            return (NMSAdapter.ChunkSendLock)lock;
         });

         boolean var8;
         try {
            if (holder.stamp == 0L) {
               holder.stamp = holder.chunkLock.lock.writeLock();
            }

            var8 = ReflectionUtils.compareAndSet(sections, expected, value, layer);
         } finally {
            chunks = FaweBukkitWorld.getWorldSendingChunksMap(worldName);
            chunks.computeIfPresent(pair, (k, lock) -> {
               if (lock != holder.chunkLock) {
                  throw new IllegalStateException("SENDING_CHUNKS stored lock does not equal lock attempted to be unlocked?!");
               } else {
                  lock.lock.unlockWrite(holder.stamp);
                  lock.writeWaiting = false;
                  return (NMSAdapter.ChunkSendLock)lock;
               }
            });
         }

         return var8;
      }
   }

   protected static void beginChunkPacketSend(String worldName, IntPair pair, NMSAdapter.StampLockHolder stampedLock) {
      ConcurrentHashMap<IntPair, NMSAdapter.ChunkSendLock> chunks = FaweBukkitWorld.getWorldSendingChunksMap(worldName);
      chunks.compute(pair, (k, lock) -> {
         if (lock == null) {
            lock = new NMSAdapter.ChunkSendLock();
         }

         if (!lock.writeWaiting && lock.lock.getReadLockCount() < 1 && !lock.lock.isWriteLocked()) {
            stampedLock.stamp = lock.lock.readLock();
            stampedLock.chunkLock = lock;
            return (NMSAdapter.ChunkSendLock)lock;
         } else {
            return (NMSAdapter.ChunkSendLock)lock;
         }
      });
   }

   protected static void endChunkPacketSend(String worldName, IntPair pair, NMSAdapter.StampLockHolder lockHolder) {
      ConcurrentHashMap<IntPair, NMSAdapter.ChunkSendLock> chunks = FaweBukkitWorld.getWorldSendingChunksMap(worldName);
      chunks.computeIfPresent(pair, (k, lock) -> {
         if (lock.lock != lockHolder.chunkLock.lock) {
            throw new IllegalStateException("SENDING_CHUNKS stored lock does not equal lock attempted to be unlocked?!");
         } else {
            lock.lock.unlockRead(lockHolder.stamp);
            return null;
         }
      });
   }

   public static final class ChunkSendLock {
      public final StampedLock lock = new StampedLock();
      public boolean writeWaiting = false;
   }

   public static final class StampLockHolder {
      public long stamp;
      public NMSAdapter.ChunkSendLock chunkLock = null;
   }
}
