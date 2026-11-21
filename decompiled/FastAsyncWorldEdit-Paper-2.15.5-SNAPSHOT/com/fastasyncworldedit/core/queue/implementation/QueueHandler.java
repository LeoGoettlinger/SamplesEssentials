package com.fastasyncworldedit.core.queue.implementation;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunkCache;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.queue.IQueueChunk;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.fastasyncworldedit.core.queue.Trimable;
import com.fastasyncworldedit.core.queue.implementation.chunk.ChunkCache;
import com.fastasyncworldedit.core.util.MemUtil;
import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.collection.CleanableThreadLocal;
import com.fastasyncworldedit.core.util.task.FaweForkJoinWorkerThreadFactory;
import com.fastasyncworldedit.core.wrappers.WorldWrapper;
import com.google.common.util.concurrent.Futures;
import com.sk89q.worldedit.world.World;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.jetbrains.annotations.ApiStatus.Internal;

public abstract class QueueHandler implements Trimable, Runnable {
   private final ForkJoinPool forkJoinPoolPrimary = new ForkJoinPool(
      Settings.settings().QUEUE.PARALLEL_THREADS,
      new FaweForkJoinWorkerThreadFactory("FAWE Fork Join Pool Primary - %s"),
      null,
      false,
      Settings.settings().QUEUE.PARALLEL_THREADS,
      Settings.settings().QUEUE.PARALLEL_THREADS,
      0,
      pool -> true,
      60L,
      TimeUnit.SECONDS
   );
   private final ForkJoinPool forkJoinPoolSecondary = new ForkJoinPool(
      Settings.settings().QUEUE.PARALLEL_THREADS, new FaweForkJoinWorkerThreadFactory("FAWE Fork Join Pool Secondary - %s"), null, false
   );
   private final ThreadPoolExecutor blockingExecutor = FaweCache.INSTANCE.newBlockingExecutor("FAWE QueueHandler Blocking Executor - %d");
   private final ConcurrentLinkedQueue<FutureTask> syncTasks = new ConcurrentLinkedQueue<>();
   private final ConcurrentLinkedQueue<FutureTask> syncWhenFree = new ConcurrentLinkedQueue<>();
   private final Map<World, WeakReference<IChunkCache<IChunkGet>>> chunkGetCache = new HashMap<>();
   private final CleanableThreadLocal<IQueueExtent<IQueueChunk>> queuePool = new CleanableThreadLocal<>(this::create);
   private long last;
   private long allocate = 50L;

   protected QueueHandler() {
      TaskManager.taskManager().repeat(this, 1);
   }

   @Internal
   public ThreadPoolExecutor getBlockingExecutor() {
      return this.blockingExecutor;
   }

   @Override
   public void run() {
      if (!Fawe.isMainThread()) {
         throw new IllegalStateException("Not main thread");
      } else {
         if (!this.syncTasks.isEmpty()) {
            long currentAllocate = this.getAllocate();
            if (!MemUtil.isMemoryFree()) {
            }

            this.operate(this.syncTasks, this.last, currentAllocate);
         } else if (!this.syncWhenFree.isEmpty()) {
            this.operate(this.syncWhenFree, this.last, this.getAllocate());
         }
      }
   }

   public boolean isUnderutilized() {
      return this.blockingExecutor.getActiveCount() < this.blockingExecutor.getMaximumPoolSize();
   }

   private long getAllocate() {
      long now = System.currentTimeMillis();
      double targetTPS = 18.0 - Math.max(Settings.settings().QUEUE.EXTRA_TIME_MS * 0.05, 0.0);
      long diff = 50L + this.last - (this.last = now);
      long absDiff = Math.abs(diff);
      if (diff == 0L) {
         this.allocate = Math.min(50L, this.allocate + 1L);
      } else if (diff < 0L) {
         this.allocate = Math.max(5L, this.allocate + diff);
      } else if (!Fawe.instance().getTimer().isAbove(targetTPS)) {
         this.allocate = Math.max(5L, this.allocate - 1L);
      }

      return this.allocate - absDiff;
   }

   private void operate(Queue<FutureTask> queue, long start, long currentAllocate) {
      boolean wait = false;

      do {
         Runnable task = queue.poll();
         if (task == null) {
            if (!wait) {
               break;
            }

            synchronized (this.syncTasks) {
               try {
                  queue.wait(1L);
               } catch (InterruptedException var11) {
                  var11.printStackTrace();
               }
            }

            task = queue.poll();
            wait = false;
         }

         if (task != null) {
            task.run();
            wait = true;
         }
      } while (System.currentTimeMillis() - start < currentAllocate);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.6.2"
   )
   public <T extends Future<T>> void complete(Future<T> task) {
      try {
         while (task != null) {
            task = task.get();
         }
      } catch (ExecutionException | InterruptedException var3) {
         var3.printStackTrace();
      }
   }

   public <T> Future<T> async(Runnable run, T value) {
      return this.forkJoinPoolSecondary.submit(run, value);
   }

   public Future<?> async(Runnable run) {
      return this.forkJoinPoolSecondary.submit(run);
   }

   public <T> Future<T> async(Callable<T> call) {
      return this.forkJoinPoolSecondary.submit(call);
   }

   public ForkJoinTask submit(Runnable run) {
      return this.forkJoinPoolPrimary.submit(run);
   }

   public <T> Future<T> sync(Runnable run) {
      return this.sync(run, this.syncTasks);
   }

   public <T> Future<T> sync(Callable<T> call) throws Exception {
      return this.sync(call, this.syncTasks);
   }

   public <T> Future<T> sync(Supplier<T> supplier) {
      return this.sync(supplier, this.syncTasks);
   }

   public <T> Future<T> syncWhenFree(Runnable run, T value) {
      return this.sync(run, value, this.syncWhenFree);
   }

   public <T> Future<T> syncWhenFree(Runnable run) {
      return this.sync(run, this.syncWhenFree);
   }

   public <T> Future<T> syncWhenFree(Callable<T> call) throws Exception {
      return this.sync(call, this.syncWhenFree);
   }

   public <T> Future<T> syncWhenFree(Supplier<T> supplier) {
      return this.sync(supplier, this.syncWhenFree);
   }

   private <T> Future<T> sync(Runnable run, T value, Queue<FutureTask> queue) {
      if (Fawe.isMainThread()) {
         run.run();
         return Futures.immediateFuture(value);
      } else {
         FutureTask<T> result = new FutureTask<>(run, value);
         queue.add(result);
         this.notifySync(queue);
         return result;
      }
   }

   private <T> Future<T> sync(Runnable run, Queue<FutureTask> queue) {
      if (Fawe.isMainThread()) {
         run.run();
         return Futures.immediateCancelledFuture();
      } else {
         FutureTask<T> result = new FutureTask<>(run, null);
         queue.add(result);
         this.notifySync(queue);
         return result;
      }
   }

   private <T> Future<T> sync(Callable<T> call, Queue<FutureTask> queue) throws Exception {
      if (Fawe.isMainThread()) {
         return Futures.immediateFuture(call.call());
      } else {
         FutureTask<T> result = new FutureTask<>(call);
         queue.add(result);
         this.notifySync(queue);
         return result;
      }
   }

   private <T> Future<T> sync(Supplier<T> call, Queue<FutureTask> queue) {
      if (Fawe.isMainThread()) {
         return Futures.immediateFuture(call.get());
      } else {
         FutureTask<T> result = new FutureTask<>(call::get);
         queue.add(result);
         this.notifySync(queue);
         return result;
      }
   }

   private void notifySync(Object object) {
      synchronized (object) {
         object.notifyAll();
      }
   }

   public <T extends Future<T>> T submit(IQueueChunk<T> chunk) {
      return (T)this.blockingExecutor.<T>submit(chunk);
   }

   @Internal
   public <T extends Future<T>> T submitToBlocking(Callable<T> callable) {
      return (T)this.blockingExecutor.<T>submit(callable);
   }

   public IChunkCache<IChunkGet> getOrCreateWorldCache(World world) {
      world = WorldWrapper.unwrap(world);
      synchronized (this.chunkGetCache) {
         WeakReference<IChunkCache<IChunkGet>> ref = this.chunkGetCache.get(world);
         if (ref != null) {
            IChunkCache<IChunkGet> cached = ref.get();
            if (cached != null) {
               return cached;
            }
         }

         IChunkCache<IChunkGet> created = new ChunkCache<>(world);
         this.chunkGetCache.put(world, new WeakReference<>(created));
         return created;
      }
   }

   public IQueueExtent<IQueueChunk> create() {
      return new SingleThreadQueueExtent();
   }

   public void unCache() {
      this.queuePool.remove();
   }

   private IQueueExtent<IQueueChunk> pool() {
      IQueueExtent<IQueueChunk> queue = this.queuePool.get();
      if (queue == null) {
         this.queuePool.set(queue = this.queuePool.init());
      }

      return queue;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.6.2"
   )
   public void startSet(boolean parallel) {
      this.startUnsafe(parallel);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.6.2"
   )
   public void endSet(boolean parallel) {
      this.startUnsafe(parallel);
   }

   public abstract void startUnsafe(boolean parallel);

   public abstract void endUnsafe(boolean parallel);

   public IQueueExtent<IQueueChunk> getQueue(World world) {
      return this.getQueue(world, null, null);
   }

   public IQueueExtent<IQueueChunk> getQueue(World world, IBatchProcessor processor, IBatchProcessor postProcessor) {
      IQueueExtent<IQueueChunk> queue = this.pool();
      IChunkCache<IChunkGet> cacheGet = this.getOrCreateWorldCache(world);
      IChunkCache<IChunkSet> set = null;
      queue.init(world, cacheGet, set);
      if (processor != null) {
         queue.setProcessor(processor);
      }

      if (postProcessor != null) {
         queue.setPostProcessor(postProcessor);
      }

      return queue;
   }

   @Override
   public boolean trim(boolean aggressive) {
      boolean result = true;
      synchronized (this.chunkGetCache) {
         Iterator<Entry<World, WeakReference<IChunkCache<IChunkGet>>>> iter = this.chunkGetCache.entrySet().iterator();

         while (iter.hasNext()) {
            Entry<World, WeakReference<IChunkCache<IChunkGet>>> entry = iter.next();
            WeakReference<IChunkCache<IChunkGet>> value = entry.getValue();
            IChunkCache<IChunkGet> cache = value.get();
            if (cache.trim(aggressive)) {
               iter.remove();
            } else {
               result = false;
            }
         }

         return result;
      }
   }

   public ExecutorService getForkJoinPoolPrimary() {
      return this.forkJoinPoolPrimary;
   }

   public ExecutorService getForkJoinPoolSecondary() {
      return this.forkJoinPoolSecondary;
   }
}
