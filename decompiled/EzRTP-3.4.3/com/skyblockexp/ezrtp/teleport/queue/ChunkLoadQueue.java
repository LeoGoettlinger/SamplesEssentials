package com.skyblockexp.ezrtp.teleport.queue;

import com.skyblockexp.ezrtp.platform.ChunkLoadStrategy;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.platform.PlatformTask;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChunkLoadQueue {
   private static final long DEFAULT_PROCESSING_INTERVAL_TICKS = 1L;
   private static final int DEFAULT_MAX_CHUNKS_PER_TICK = 1;
   private final JavaPlugin plugin;
   private final ChunkLoadStrategy chunkLoadStrategy;
   private final PlatformScheduler scheduler;
   private final Queue<ChunkLoadQueue.ChunkLoadRequest> queue = new ConcurrentLinkedQueue<>();
   private final AtomicBoolean processing = new AtomicBoolean(false);
   private PlatformTask processingTask;
   private final AtomicBoolean processingTaskCancelled = new AtomicBoolean(false);
   private volatile long processingIntervalTicks = 1L;
   private volatile int maxChunksPerTick = 1;
   private volatile boolean enabled;
   private volatile boolean asyncPassthrough = false;
   private final AtomicBoolean schedulerFallbackLogged = new AtomicBoolean(false);
   private volatile long minFreeMemoryMb = 256L;
   private volatile RtpStatistics statistics;

   public ChunkLoadQueue(JavaPlugin plugin, ChunkLoadStrategy chunkLoadStrategy, PlatformScheduler scheduler) {
      this.plugin = plugin;
      this.chunkLoadStrategy = chunkLoadStrategy;
      this.scheduler = scheduler;
      this.enabled = true;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (!enabled) {
         this.stop();
         this.queue.clear();
      } else if (this.processing.get()) {
         this.startProcessing();
      }
   }

   public void setStatistics(RtpStatistics statistics) {
      this.statistics = statistics;
   }

   public void configure(long intervalTicks, int maxChunksPerTick) {
      this.processingIntervalTicks = Math.max(1L, intervalTicks);
      this.maxChunksPerTick = Math.max(1, maxChunksPerTick);
      if (this.processing.get()) {
         this.startProcessing();
      }
   }

   public void configureMemorySafety(long minFreeMemoryMb) {
      this.minFreeMemoryMb = minFreeMemoryMb;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setAsyncPassthrough(boolean passthrough) {
      this.asyncPassthrough = passthrough;
      if (passthrough) {
         this.stop();
      }
   }

   public boolean isAsyncPassthrough() {
      return this.asyncPassthrough;
   }

   public CompletableFuture<Chunk> requestChunkLoad(World world, int chunkX, int chunkZ) {
      if (this.asyncPassthrough) {
         CompletableFuture<Chunk> future = new CompletableFuture<>();
         this.scheduler.executeRegion(world, chunkX, chunkZ, () -> {
            try {
               if (world.isChunkLoaded(chunkX, chunkZ)) {
                  future.complete(world.getChunkAt(chunkX, chunkZ));
                  return;
               }

               CompletableFuture<Chunk> loadFuture = this.chunkLoadStrategy.loadChunk(world, chunkX, chunkZ);
               boolean isAsync = !loadFuture.isDone();
               loadFuture.whenComplete((chunk, ex) -> {
                  RtpStatistics stats = this.statistics;
                  if (stats != null) {
                     stats.recordChunkLoad(isAsync);
                  }

                  if (ex != null) {
                     future.completeExceptionally(ex);
                  } else {
                     future.complete(chunk);
                  }
               });
            } catch (Exception var7x) {
               future.completeExceptionally(var7x);
            }
         });
         return future;
      } else if (!this.enabled) {
         CompletableFuture<Chunk> future = new CompletableFuture<>();
         this.scheduler.executeRegion(world, chunkX, chunkZ, () -> {
            world.loadChunk(chunkX, chunkZ);
            future.complete(world.getChunkAt(chunkX, chunkZ));
         });
         return future;
      } else {
         CompletableFuture<Chunk> future = new CompletableFuture<>();
         ChunkLoadQueue.ChunkLoadRequest request = new ChunkLoadQueue.ChunkLoadRequest(world, chunkX, chunkZ, future);
         this.queue.offer(request);
         if (!this.processing.getAndSet(true)) {
            this.startProcessing();
         }

         return future;
      }
   }

   public int getQueueSize() {
      return this.queue.size();
   }

   private void startProcessing() {
      synchronized (this) {
         if (this.processingTask == null || this.processingTaskCancelled.get()) {
            if (this.processingTask != null) {
               this.processingTaskCancelled.set(true);

               try {
                  this.processingTask.cancel();
               } catch (Exception var5) {
               }

               this.processingTask = null;
            }

            this.processingTaskCancelled.set(false);

            try {
               this.processingTask = this.scheduler.scheduleRepeating(this::processQueue, this.processingIntervalTicks, this.processingIntervalTicks);
            } catch (Exception var4) {
               this.activateSynchronousFallback("scheduler threw while scheduling repeating task: " + var4.getMessage());
               return;
            }

            if (this.processingTask == null) {
               this.activateSynchronousFallback("scheduler returned null for repeating task");
            }
         }
      }
   }

   private void processQueue() {
      if (this.processingTaskCancelled.get()) {
         this.processing.set(false);
         this.stop();
      } else if (this.enabled && !this.queue.isEmpty()) {
         int effectiveMaxChunks = this.maxChunksPerTick;
         if (this.minFreeMemoryMb > 0L) {
            Runtime runtime = Runtime.getRuntime();
            long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / 1048576L;
            if (freeMemoryMb < this.minFreeMemoryMb) {
               effectiveMaxChunks = Math.max(1, this.maxChunksPerTick / 2);
            }
         }

         int processed = 0;

         while (processed < effectiveMaxChunks && !this.queue.isEmpty()) {
            ChunkLoadQueue.ChunkLoadRequest request = this.queue.poll();
            if (request != null) {
               this.processChunkLoad(request);
               processed++;
            }
         }
      } else {
         if (this.queue.isEmpty()) {
            this.processing.set(false);
            this.stop();
         }
      }
   }

   private void processChunkLoad(ChunkLoadQueue.ChunkLoadRequest request) {
      try {
         World world = request.world();
         int chunkX = request.chunkX();
         int chunkZ = request.chunkZ();
         this.scheduler.executeRegion(world, chunkX, chunkZ, () -> {
            try {
               if (world.isChunkLoaded(chunkX, chunkZ)) {
                  request.future().complete(world.getChunkAt(chunkX, chunkZ));
                  return;
               }

               CompletableFuture<Chunk> loadFuture = this.chunkLoadStrategy.loadChunk(world, chunkX, chunkZ);
               boolean isAsync = !loadFuture.isDone();
               loadFuture.whenComplete((chunk, ex) -> {
                  RtpStatistics stats = this.statistics;
                  if (stats != null) {
                     stats.recordChunkLoad(isAsync);
                  }

                  if (ex != null) {
                     request.future().completeExceptionally(ex);
                  } else {
                     request.future().complete(chunk);
                  }
               });
            } catch (Exception var7) {
               request.future().completeExceptionally(var7);
               this.plugin.getLogger().warning("Failed to load chunk: " + var7.getMessage());
            }
         });
      } catch (Exception var5) {
         request.future().completeExceptionally(var5);
         this.plugin.getLogger().warning("Failed to load chunk: " + var5.getMessage());
      }
   }

   public void stop() {
      this.processing.set(false);
      if (this.processingTask != null) {
         this.processingTaskCancelled.set(true);

         try {
            this.processingTask.cancel();
         } catch (Exception var2) {
         }

         this.processingTask = null;
      }
   }

   public void clear() {
      ChunkLoadQueue.ChunkLoadRequest request;
      while ((request = this.queue.poll()) != null) {
         request.future().completeExceptionally(new CancellationException("Chunk load request cancelled"));
      }
   }

   public void shutdown() {
      this.stop();
      this.clear();
      this.enabled = false;
   }

   private void activateSynchronousFallback(String reason) {
      this.enabled = false;
      this.processing.set(false);
      if (this.schedulerFallbackLogged.compareAndSet(false, true)) {
         this.plugin.getLogger().warning("Chunk load queue fallback engaged; using immediate synchronous loading. Reason: " + reason);
      }

      ChunkLoadQueue.ChunkLoadRequest request;
      while ((request = this.queue.poll()) != null) {
         this.processChunkLoadImmediate(request);
      }
   }

   private void processChunkLoadImmediate(ChunkLoadQueue.ChunkLoadRequest request) {
      try {
         World world = request.world();
         int chunkX = request.chunkX();
         int chunkZ = request.chunkZ();
         world.loadChunk(chunkX, chunkZ);
         request.future().complete(world.getChunkAt(chunkX, chunkZ));
      } catch (Exception var5) {
         request.future().completeExceptionally(var5);
         this.plugin.getLogger().warning("Failed to synchronously load chunk after queue fallback: " + var5.getMessage());
      }
   }

   private record ChunkLoadRequest(World world, int chunkX, int chunkZ, CompletableFuture<Chunk> future) {
   }
}
