package com.fastasyncworldedit.bukkit.adapter;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.math.IntPair;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.fastasyncworldedit.core.queue.implementation.QueueHandler;
import com.fastasyncworldedit.core.queue.implementation.blocks.CharGetBlocks;
import com.fastasyncworldedit.core.util.MemUtil;
import com.fastasyncworldedit.core.util.task.FaweThreadUtil;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Logger;

public abstract class AbstractBukkitGetBlocks<ServerLevel, LevelChunk> extends CharGetBlocks {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   protected final ServerLevel serverLevel;
   protected final int chunkX;
   protected final int chunkZ;
   protected final ReentrantLock callLock = new ReentrantLock();
   protected final ConcurrentHashMap<Integer, IChunkGet> copies = new ConcurrentHashMap<>();
   protected final IntPair chunkPos;
   protected final int minHeight;
   protected final int maxHeight;
   protected boolean createCopy = false;
   protected boolean forceLoadSections = true;
   protected int copyKey = 0;

   protected AbstractBukkitGetBlocks(ServerLevel serverLevel, int chunkX, int chunkZ, int minY, int maxY) {
      super(minY >> 4, maxY >> 4);
      this.serverLevel = serverLevel;
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
      this.minHeight = minY;
      this.maxHeight = maxY;
      this.chunkPos = new IntPair(chunkX, chunkZ);
   }

   protected abstract void send();

   protected abstract CompletableFuture<LevelChunk> ensureLoaded(ServerLevel serverLevel);

   protected abstract <T extends Future<T>> T internalCall(IChunkSet set, Runnable finalizer, int copyKey, LevelChunk nmsChunk, ServerLevel nmsWorld) throws Exception;

   @Override
   public synchronized <T extends Future<T>> T call(IQueueExtent<? extends IChunk> owner, IChunkSet set, Runnable finalizer) {
      if (!this.callLock.isHeldByCurrentThread()) {
         throw new IllegalStateException("Attempted to call chunk GET but chunk was not call-locked.");
      } else {
         this.forceLoadSections = false;
         ServerLevel nmsWorld = this.serverLevel;
         CompletableFuture<LevelChunk> nmsChunkFuture = this.ensureLoaded(nmsWorld);
         LevelChunk chunk = nmsChunkFuture.getNow(null);
         if (chunk == null && MemUtil.shouldBeginSlow() || Settings.settings().QUEUE.ASYNC_CHUNK_LOAD_WRITE) {
            try {
               chunk = nmsChunkFuture.get();
            } catch (ExecutionException | InterruptedException var9) {
               LOGGER.error("Could not get chunk at {},{} whilst low memory", this.chunkX, this.chunkZ, var9);
               throw new FaweException(
                  TextComponent.of("Could not get chunk at " + this.chunkX + "," + this.chunkZ + " whilst low memory: " + var9.getMessage())
               );
            }
         }

         int finalCopyKey = this.copyKey;
         if (chunk != null) {
            return this.tryInternalCall(set, finalizer, finalCopyKey, chunk, nmsWorld);
         } else {
            Extent extent = FaweThreadUtil.getCurrentExtent();
            nmsChunkFuture.thenApply(
               nmsChunk -> owner.submitTaskUnchecked(() -> this.tryWrappedInternalCall(set, finalizer, finalCopyKey, (LevelChunk)nmsChunk, nmsWorld, extent))
            );
            return (T)CompletableFuture.completedFuture(null);
         }
      }
   }

   private <T extends Future<T>> T tryWrappedInternalCall(
      IChunkSet set, Runnable finalizer, int copyKey, LevelChunk nmsChunk, ServerLevel nmsWorld, Extent extent
   ) {
      FaweThreadUtil.setCurrentExtent(extent);

      Future var7;
      try {
         var7 = this.tryInternalCall(set, finalizer, copyKey, nmsChunk, nmsWorld);
      } finally {
         FaweThreadUtil.clearCurrentExtent();
      }

      return (T)var7;
   }

   private <T extends Future<T>> T tryInternalCall(IChunkSet set, Runnable finalizer, int copyKey, LevelChunk nmsChunk, ServerLevel nmsWorld) {
      Object var7;
      try {
         return this.internalCall(set, finalizer, copyKey, nmsChunk, nmsWorld);
      } catch (Throwable var11) {
         LOGGER.error("Error performing chunk call at chunk {},{}", this.chunkX, this.chunkZ, var11);
         var7 = null;
      } finally {
         this.forceLoadSections = true;
      }

      return (T)var7;
   }

   protected <T extends Future<T>> T handleCallFinalizer(final List<Runnable> syncTasks, final Runnable callback, final Runnable finalizer) throws Exception {
      if (!syncTasks.isEmpty()) {
         QueueHandler queueHandler = Fawe.instance().getQueueHandler();
         Callable<Future<?>> chain = () -> {
            try {
               for (Runnable task : syncTasks) {
                  if (task != null) {
                     task.run();
                  }
               }

               if (callback != null) {
                  return queueHandler.async(callback, null);
               } else {
                  return finalizer != null ? queueHandler.async(finalizer, null) : null;
               }
            } catch (Throwable var7) {
               LOGGER.error("Error performing final chunk calling at {},{}", this.chunkX, this.chunkZ, var7);
               throw var7;
            }
         };
         return (T)queueHandler.<Future<?>>sync(chain);
      } else {
         if (callback != null) {
            callback.run();
         } else if (finalizer != null) {
            finalizer.run();
         }

         return null;
      }
   }

   @Override
   public int getX() {
      return this.chunkX;
   }

   @Override
   public int getZ() {
      return this.chunkZ;
   }

   @Override
   public boolean isCreateCopy() {
      return this.createCopy;
   }

   @Override
   public int setCreateCopy(boolean createCopy) {
      if (!this.callLock.isHeldByCurrentThread()) {
         throw new IllegalStateException("Attempting to set if chunk GET should create copy, but it is not call-locked.");
      } else {
         this.createCopy = createCopy;
         return ++this.copyKey;
      }
   }

   @Override
   public IChunkGet getCopy(final int key) {
      return this.copies.remove(key);
   }

   @Override
   public void lockCall() {
      this.callLock.lock();
   }

   @Override
   public void unlockCall() {
      this.callLock.unlock();
   }

   @Override
   public int getMaxY() {
      return this.maxHeight;
   }

   @Override
   public int getMinY() {
      return this.minHeight;
   }
}
