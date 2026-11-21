package com.fastasyncworldedit.bukkit.adapter;

import com.fastasyncworldedit.core.extent.processor.lighting.NMSRelighter;
import com.fastasyncworldedit.core.extent.processor.lighting.Relighter;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public abstract class StarlightRelighter<SERVER_LEVEL, CHUNK_POS> implements Relighter {
   protected static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final int CHUNKS_PER_BATCH = 1024;
   private static final int CHUNKS_PER_BATCH_SQRT_LOG2 = 5;
   private final ReentrantLock lock = new ReentrantLock();
   private final Long2ObjectLinkedOpenHashMap<LongSet> regions = new Long2ObjectLinkedOpenHashMap();
   private final ReentrantLock areaLock = new ReentrantLock();
   private final NMSRelighter delegate;
   protected final SERVER_LEVEL serverLevel;

   protected StarlightRelighter(SERVER_LEVEL serverLevel, IQueueExtent<?> queue) {
      this.serverLevel = serverLevel;
      this.delegate = new NMSRelighter(queue);
   }

   protected Set<CHUNK_POS> convertChunkKeysToChunkPos(LongSet chunks) {
      Set<CHUNK_POS> coords = new HashSet<>();
      LongIterator iterator = chunks.iterator();

      while (iterator.hasNext()) {
         coords.add(this.createChunkPos(iterator.nextLong()));
      }

      return coords;
   }

   protected abstract CHUNK_POS createChunkPos(long chunkKey);

   protected abstract long asLong(int chunkX, int chunkZ);

   protected abstract CompletableFuture<?> chunkLoadFuture(CHUNK_POS pos);

   protected List<CompletableFuture<?>> chunkLoadFutures(Set<CHUNK_POS> coords) {
      List<CompletableFuture<?>> futures = new ArrayList<>();

      for (CHUNK_POS coord : coords) {
         futures.add(this.chunkLoadFuture(coord));
      }

      return futures;
   }

   @NotNull
   protected IntConsumer postProcessCallback(Runnable andThen, Set<CHUNK_POS> coords) {
      return i -> {
         if (i != coords.size()) {
            LOGGER.warn("Processed {} chunks instead of {}", i, coords.size());
         }

         TaskManager.taskManager().task(() -> this.postProcessChunks(coords));
         TaskManager.taskManager().async(andThen);
      };
   }

   protected abstract void invokeRelight(Set<CHUNK_POS> coords, Consumer<CHUNK_POS> chunkCallback, IntConsumer processCallback);

   protected abstract void postProcessChunks(Set<CHUNK_POS> coords);

   protected void fixLighting(LongSet chunks, Runnable andThen) {
      Set<CHUNK_POS> coords = this.convertChunkKeysToChunkPos(chunks);
      TaskManager.taskManager()
         .task(
            () -> {
               List<CompletableFuture<?>> futures = this.chunkLoadFutures(coords);
               CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                  .thenAccept(v -> this.invokeRelight(coords, c -> {}, this.postProcessCallback(andThen, coords)));
            }
         );
   }

   @Override
   public boolean addChunk(int cx, int cz, byte[] skipReason, int bitmask) {
      this.areaLock.lock();

      try {
         for (int x = cx - 1; x <= cx + 1; x++) {
            for (int z = cz - 1; z <= cz + 1; z++) {
               long key = MathMan.pairInt(x >> 5, z >> 5);
               LongSet chunks = (LongSet)this.regions.computeIfAbsent(key, k -> new LongArraySet(256));
               chunks.add(this.asLong(x, z));
            }
         }
      } finally {
         this.areaLock.unlock();
      }

      return true;
   }

   @Override
   public void fixLightingSafe(boolean sky) {
      this.areaLock.lock();

      try {
         if (!this.regions.isEmpty()) {
            LongSet first = (LongSet)this.regions.removeFirst();
            this.fixLighting(first, () -> this.fixLightingSafe(true));
            return;
         }
      } finally {
         this.areaLock.unlock();
      }
   }

   @Override
   public void addLightUpdate(int x, int y, int z) {
      this.delegate.addLightUpdate(x, y, z);
   }

   @Override
   public void clear() {
   }

   @Override
   public void removeLighting() {
      this.delegate.removeLighting();
   }

   @Override
   public void fixBlockLighting() {
      this.fixLightingSafe(true);
   }

   @Override
   public void fixSkyLighting() {
      this.fixLightingSafe(true);
   }

   @Override
   public boolean isEmpty() {
      return true;
   }

   @Override
   public ReentrantLock getLock() {
      return this.lock;
   }

   @Override
   public boolean isFinished() {
      return false;
   }

   @Override
   public void close() throws Exception {
      this.fixLightingSafe(true);
   }
}
