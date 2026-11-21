package com.fastasyncworldedit.core.queue.implementation;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.PassthroughExtent;
import com.fastasyncworldedit.core.extent.filter.block.CharFilterBlock;
import com.fastasyncworldedit.core.extent.filter.block.ChunkFilterBlock;
import com.fastasyncworldedit.core.extent.processor.EmptyBatchProcessor;
import com.fastasyncworldedit.core.extent.processor.ExtentBatchProcessorHolder;
import com.fastasyncworldedit.core.extent.processor.ProcessorScope;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.queue.IChunkCache;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.queue.IQueueChunk;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.fastasyncworldedit.core.queue.implementation.blocks.CharSetBlocks;
import com.fastasyncworldedit.core.queue.implementation.chunk.ChunkHolder;
import com.fastasyncworldedit.core.queue.implementation.chunk.NullChunk;
import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.MemUtil;
import com.fastasyncworldedit.core.wrappers.WorldWrapper;
import com.google.common.util.concurrent.Futures;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.World;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Logger;

public class SingleThreadQueueExtent extends ExtentBatchProcessorHolder implements IQueueExtent<IQueueChunk> {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final Long2ObjectLinkedOpenHashMap<IQueueChunk<?>> chunks = new Long2ObjectLinkedOpenHashMap();
   private final ConcurrentLinkedQueue<Future<?>> submissions = new ConcurrentLinkedQueue<>();
   private final ReentrantLock getChunkLock = new ReentrantLock();
   private final AtomicReference<IQueueChunk> lastChunk = new AtomicReference<>();
   private World world = null;
   private int minY = 0;
   private int maxY = 255;
   private IChunkCache<IChunkGet> cacheGet;
   private IChunkCache<IChunkSet> cacheSet;
   private boolean initialized;
   private Thread currentThread;
   private boolean enabledQueue = true;
   private boolean fastmode = false;
   private boolean[] faweExceptionReasonsUsed = new boolean[FaweException.Type.values().length];
   private int lastException = Integer.MIN_VALUE;
   private int exceptionCount = 0;
   private SideEffectSet sideEffectSet = SideEffectSet.defaults();
   private int targetSize = Settings.settings().QUEUE.TARGET_SIZE;

   public SingleThreadQueueExtent() {
   }

   public SingleThreadQueueExtent(int minY, int maxY) {
      this.minY = minY;
      this.maxY = maxY;
   }

   @Override
   public void enableQueue() {
      this.enabledQueue = true;
   }

   @Override
   public void disableQueue() {
      this.enabledQueue = false;
   }

   @Override
   public IChunkGet getCachedGet(int chunkX, int chunkZ) {
      return this.processGet(this.cacheGet.get(chunkX, chunkZ));
   }

   @Override
   public IChunkSet getCachedSet(int chunkX, int chunkZ) {
      return this.cacheSet.get(chunkX, chunkZ);
   }

   @Override
   public boolean isFastMode() {
      return this.fastmode;
   }

   @Override
   public void setFastMode(boolean fastmode) {
      this.fastmode = fastmode;
   }

   @Override
   public void setSideEffectSet(SideEffectSet sideEffectSet) {
      this.sideEffectSet = sideEffectSet;
   }

   @Override
   public SideEffectSet getSideEffectSet() {
      return this.sideEffectSet;
   }

   @Override
   public int getMinY() {
      return this.minY;
   }

   @Override
   public int getMaxY() {
      return this.maxY;
   }

   public World getWorld() {
      return this.world;
   }

   public void setTargetSize(int targetSize) {
      this.targetSize = targetSize;
   }

   public void setFaweExceptionArray(final boolean[] faweExceptionReasonsUsed) {
      this.faweExceptionReasonsUsed = faweExceptionReasonsUsed;
   }

   protected synchronized void reset() {
      if (this.initialized) {
         this.getChunkLock.lock();
         this.lastChunk.set(null);

         try {
            this.chunks.clear();
         } finally {
            this.getChunkLock.unlock();
         }

         this.enabledQueue = true;
         this.currentThread = null;
         this.initialized = false;
         this.setProcessor(EmptyBatchProcessor.getInstance());
         this.setPostProcessor(EmptyBatchProcessor.getInstance());
         this.world = null;
         this.faweExceptionReasonsUsed = new boolean[FaweException.Type.values().length];
         this.targetSize = Settings.settings().QUEUE.TARGET_SIZE;
      }
   }

   @Override
   public synchronized void init(Extent extent, IChunkCache<IChunkGet> get, IChunkCache<IChunkSet> set) {
      this.reset();
      this.minY = extent.getMinY();
      this.maxY = extent.getMaxY();
      this.currentThread = Thread.currentThread();
      if (get == null) {
         get = (x, z) -> {
            throw new UnsupportedOperationException();
         };
      }

      if (set == null) {
         set = CharSetBlocks::newInstance;
      }

      this.cacheGet = get;
      this.cacheSet = set;
      this.setProcessor(EmptyBatchProcessor.getInstance());
      this.setPostProcessor(EmptyBatchProcessor.getInstance());
      this.initialized = true;
      if (extent.isWorld()) {
         this.world = (World)(extent instanceof PassthroughExtent ? ((PassthroughExtent)extent).getExtent() : extent);
      } else if (extent instanceof EditSession) {
         this.world = ((EditSession)extent).getWorld();
      } else {
         this.world = WorldWrapper.unwrap(extent);
      }
   }

   @Override
   public int size() {
      return this.chunks.size() + this.submissions.size();
   }

   @Override
   public boolean isEmpty() {
      return this.chunks.isEmpty() && this.submissions.isEmpty();
   }

   public <V extends Future<V>> V submit(IQueueChunk chunk) {
      this.lastChunk.compareAndExchange(chunk, null);
      long index = MathMan.pairInt(chunk.getX(), chunk.getZ());
      this.getChunkLock.lock();
      this.chunks.remove(index, chunk);
      this.getChunkLock.unlock();
      V future = this.submitUnchecked(chunk);
      this.submissions.add(future);
      return future;
   }

   private <V extends Future<V>> V submitUnchecked(IQueueChunk chunk) {
      if (chunk.isEmpty()) {
         if (chunk instanceof ChunkHolder<?> holder) {
            long age = holder.initAge();
            if (age < 5L) {
               try {
                  Thread.sleep(5L - age);
               } catch (InterruptedException var6) {
               }
            }
         }

         if (chunk.isEmpty()) {
            Future result = Futures.immediateFuture(null);
            return (V)result;
         }
      }

      chunk.invalidateWrapper();
      if (Fawe.isMainThread()) {
         V result = (V)chunk.call();
         return (V)(result == null ? Futures.immediateFuture(null) : result);
      } else {
         return Fawe.instance().getQueueHandler().submit(chunk);
      }
   }

   @Override
   public <V extends Future<V>> V submitTaskUnchecked(Callable<V> callable) {
      V future = Fawe.instance().getQueueHandler().submitToBlocking(callable);
      this.submissions.add(future);
      return future;
   }

   @Override
   public synchronized boolean trim(boolean aggressive) {
      this.cacheGet.trim(aggressive);
      this.cacheSet.trim(aggressive);
      LOGGER.info("trim");
      if (Thread.currentThread() == this.currentThread) {
         this.lastChunk.set(null);
         return this.chunks.isEmpty();
      } else {
         if (!this.submissions.isEmpty()) {
            if (aggressive) {
               this.pollSubmissions(0, aggressive);
            } else {
               this.pollSubmissions(Settings.settings().QUEUE.PARALLEL_THREADS, aggressive);
            }
         }

         synchronized (this) {
            return this.currentThread == null;
         }
      }
   }

   private ChunkHolder poolOrCreate(int chunkX, int chunkZ) {
      ChunkHolder next = this.create(false);
      next.init(this, chunkX, chunkZ);
      return next;
   }

   public IQueueChunk wrap(IQueueChunk chunk) {
      chunk.setFastMode(this.isFastMode());
      chunk.setSideEffectSet(this.getSideEffectSet());
      return chunk;
   }

   public final IQueueChunk getOrCreateChunk(int x, int z) {
      IQueueChunk lastChunk = this.lastChunk.get();
      if (lastChunk != null && lastChunk.getX() == x && lastChunk.getZ() == z) {
         return lastChunk;
      } else {
         long pair = MathMan.pairInt(x, z);
         if (this.processGet(x, z)
            && (!Settings.settings().REGION_RESTRICTIONS_OPTIONS.RESTRICT_TO_SAFE_RANGE || x <= 1875000 && z <= 1875000 && x >= -1875000 && z >= -1875000)) {
            this.getChunkLock.lock();

            IQueueChunk size;
            try {
               IQueueChunk chunk = (IQueueChunk)this.chunks.get(pair);
               if (chunk == null) {
                  int sizex = this.chunks.size();
                  boolean lowMem = MemUtil.isMemoryLimited();
                  int targetSize = lowMem ? Settings.settings().QUEUE.PARALLEL_THREADS + 8 : this.targetSize;
                  if (this.enabledQueue && sizex > targetSize && (lowMem || Fawe.instance().getQueueHandler().isUnderutilized())) {
                     IQueueChunk toSubmit = (IQueueChunk)this.chunks.removeFirst();
                     this.lastChunk.compareAndExchange(toSubmit, null);
                     Future future = this.submitUnchecked(toSubmit);
                     if (future != null && !future.isDone()) {
                        this.pollSubmissions(targetSize, lowMem);
                        this.submissions.add(future);
                     }
                  }

                  IQueueChunk var15 = this.poolOrCreate(x, z);
                  chunk = this.wrap((IQueueChunk)var15);
                  this.chunks.put(pair, chunk);
                  this.lastChunk.set(chunk);
                  return chunk;
               }

               this.lastChunk.set(chunk);
               size = chunk;
            } finally {
               this.getChunkLock.unlock();
            }

            return size;
         } else {
            return NullChunk.getInstance();
         }
      }
   }

   public void addChunkLoad(int cx, int cz) {
      if (this.world != null) {
         this.world.checkLoadedChunk(BlockVector3.at(cx << 4, 0, cz << 4));
      }
   }

   public void preload(Region region) {
      if (Settings.settings().QUEUE.PRELOAD_CHUNK_COUNT > 1) {
         int loadCount = 0;

         for (BlockVector2 from : region.getChunks()) {
            if (loadCount >= Settings.settings().QUEUE.PRELOAD_CHUNK_COUNT) {
               break;
            }

            loadCount++;
            this.addChunkLoad(from.x(), from.z());
         }
      }
   }

   public ChunkHolder create(boolean isFull) {
      return ChunkHolder.newInstance();
   }

   private void pollSubmissions(int targetSize, boolean aggressive) {
      int overflow = this.submissions.size() - targetSize;
      if (aggressive) {
         if (targetSize == 0) {
            while (!this.submissions.isEmpty()) {
               this.iterateSubmissions();
            }
         }

         for (int i = 0; i < overflow; i++) {
            this.iterateSubmissions();
         }
      } else {
         for (int i = 0; i < overflow; i++) {
            Future next = this.submissions.peek();

            while (next != null) {
               if (!next.isDone()) {
                  return;
               }

               Future after = null;

               try {
                  after = (Future)next.get();
               } catch (FaweException var14) {
                  Fawe.handleFaweException(this.faweExceptionReasonsUsed, var14, LOGGER);
               } catch (InterruptedException | ExecutionException var15) {
                  if (var15.getCause() instanceof FaweException) {
                     Fawe.handleFaweException(this.faweExceptionReasonsUsed, (FaweException)var15.getCause(), LOGGER);
                  } else {
                     String message = var15.getMessage();
                     int hash = message != null ? message.hashCode() : 0;
                     if (this.lastException != hash) {
                        this.lastException = hash;
                        this.exceptionCount = 0;
                        LOGGER.catching(var15);
                     } else if (this.exceptionCount < Settings.settings().QUEUE.PARALLEL_THREADS) {
                        this.exceptionCount++;
                        LOGGER.warn(message);
                     }
                  }
               } finally {
                  ;
               }
            }

            this.submissions.poll();
         }
      }
   }

   private void iterateSubmissions() {
      Future first = this.submissions.poll();

      try {
         while (first != null) {
            first = (Future)first.get();
         }
      } catch (FaweException var5) {
         Fawe.handleFaweException(this.faweExceptionReasonsUsed, var5, LOGGER);
      } catch (InterruptedException | ExecutionException var6) {
         if (var6.getCause() instanceof FaweException) {
            Fawe.handleFaweException(this.faweExceptionReasonsUsed, (FaweException)var6.getCause(), LOGGER);
         } else {
            String message = var6.getMessage();
            int hash = message != null ? message.hashCode() : 0;
            if (this.lastException != hash) {
               this.lastException = hash;
               this.exceptionCount = 0;
               LOGGER.catching(var6);
            } else if (this.exceptionCount < Settings.settings().QUEUE.PARALLEL_THREADS) {
               this.exceptionCount++;
               LOGGER.warn(message);
            }
         }
      }
   }

   @Override
   public synchronized void flush() {
      if (!this.chunks.isEmpty()) {
         this.getChunkLock.lock();
         if (MemUtil.isMemoryLimited()) {
            while (!this.chunks.isEmpty()) {
               IQueueChunk chunk = (IQueueChunk)this.chunks.removeFirst();
               this.lastChunk.compareAndExchange(chunk, null);
               Future future = this.submitUnchecked(chunk);
               if (future != null && !future.isDone()) {
                  this.pollSubmissions(Settings.settings().QUEUE.PARALLEL_THREADS, true);
                  this.submissions.add(future);
               }
            }
         } else {
            while (!this.chunks.isEmpty()) {
               IQueueChunk chunk = (IQueueChunk)this.chunks.removeFirst();
               this.lastChunk.compareAndExchange(chunk, null);
               Future future = this.submitUnchecked(chunk);
               if (future != null && !future.isDone()) {
                  this.submissions.add(future);
               }
            }
         }

         this.getChunkLock.unlock();
      }

      this.pollSubmissions(0, true);
   }

   @Override
   public ChunkFilterBlock createFilterBlock() {
      return new CharFilterBlock(this);
   }

   @Override
   public ProcessorScope getScope() {
      return ProcessorScope.ADDING_BLOCKS;
   }
}
