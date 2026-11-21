package cm.chunkManager.components;

import cm.chunkManager.utils.ChunkUtils;
import cm.chunkManager.utils.Scheduler;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Logger;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkProcessor {
   private final JavaPlugin plugin;
   private final Logger logger;
   private final ExecutorService executorService;
   private final ScheduledExecutorService scheduledExecutor;
   private final BlockingQueue<ChunkProcessor.ChunkTask> taskQueue;
   private final AtomicInteger activeThreads;
   private final int maxThreads;
   private final int coreThreads;

   public ChunkProcessor(JavaPlugin plugin, int coreThreads, int maxThreads) {
      this.plugin = plugin;
      this.logger = plugin.getLogger();
      this.coreThreads = coreThreads;
      this.maxThreads = maxThreads;
      this.activeThreads = new AtomicInteger(0);
      this.taskQueue = new PriorityBlockingQueue<>();
      ThreadFactory threadFactory = new ThreadFactory() {
         private final AtomicInteger threadNumber = new AtomicInteger(1);

         @Override
         public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "ChunkProcessor-" + this.threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
         }
      };
      this.executorService = new ThreadPoolExecutor(
         coreThreads, maxThreads, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), threadFactory, new CallerRunsPolicy()
      );
      this.scheduledExecutor = Executors.newScheduledThreadPool(2, threadFactory);
      this.startWorkers();
   }

   private void startWorkers() {
      for (int i = 0; i < this.coreThreads; i++) {
         this.executorService.submit(this::processTaskQueue);
      }

      this.scheduledExecutor.scheduleAtFixedRate(this::adjustThreadPool, 0L, 5L, TimeUnit.SECONDS);
   }

   private void processTaskQueue() {
      while (!Thread.currentThread().isInterrupted()) {
         try {
            ChunkProcessor.ChunkTask task = this.taskQueue.poll(1L, TimeUnit.SECONDS);
            if (task != null) {
               this.activeThreads.incrementAndGet();

               try {
                  task.execute();
               } finally {
                  this.activeThreads.decrementAndGet();
               }
            }
         } catch (InterruptedException var7) {
            Thread.currentThread().interrupt();
            break;
         } catch (Exception var8) {
            this.logger.severe("Error processing chunk task: " + var8.getMessage());
         }
      }
   }

   private void adjustThreadPool() {
      int queueSize = this.taskQueue.size();
      int active = this.activeThreads.get();
      ThreadPoolExecutor tpe = (ThreadPoolExecutor)this.executorService;
      int currentPoolSize = tpe.getPoolSize();
      if (queueSize > 50 && currentPoolSize < this.maxThreads) {
         for (int i = currentPoolSize; i < Math.min(currentPoolSize + 2, this.maxThreads); i++) {
            this.executorService.submit(this::processTaskQueue);
         }
      } else if (queueSize < 10 && active < 2 && currentPoolSize > this.coreThreads) {
         int newCoreSize = Math.max(this.coreThreads, currentPoolSize - 1);
         newCoreSize = Math.min(newCoreSize, tpe.getMaximumPoolSize());
         tpe.setCorePoolSize(newCoreSize);
      }
   }

   public CompletableFuture<Void> submitTask(Chunk chunk, Consumer<Chunk> operation, ChunkProcessor.TaskPriority priority) {
      ChunkProcessor.ChunkTask task = new ChunkProcessor.ChunkTask(chunk, operation, priority);
      this.taskQueue.offer(task);
      return task.getFuture();
   }

   public CompletableFuture<Void> submitLoadTask(Chunk chunk) {
      World world = chunk.getWorld();
      int x = chunk.getX();
      int z = chunk.getZ();
      return ChunkUtils.getChunkAtAsync(world, x, z, this.plugin).thenAccept(c -> {});
   }

   public CompletableFuture<Void> submitUnloadTask(Chunk chunk) {
      return this.submitTask(chunk, c -> {
         if (c.isLoaded()) {
            Scheduler.runGlobal(this.plugin, () -> c.unload(true));
         }
      }, ChunkProcessor.TaskPriority.LOW);
   }

   public void submitBatch(Collection<Chunk> chunks, Consumer<Chunk> operation, ChunkProcessor.TaskPriority priority) {
      chunks.forEach(chunk -> this.submitTask(chunk, operation, priority));
   }

   public int getQueueSize() {
      return this.taskQueue.size();
   }

   public int getActiveThreads() {
      return this.activeThreads.get();
   }

   public void shutdown() {
      this.scheduledExecutor.shutdown();
      this.executorService.shutdown();

      try {
         if (!this.executorService.awaitTermination(10L, TimeUnit.SECONDS)) {
            this.executorService.shutdownNow();
         }
      } catch (InterruptedException var2) {
         this.executorService.shutdownNow();
      }
   }

   public static class ChunkTask implements Comparable<ChunkProcessor.ChunkTask> {
      private final Chunk chunk;
      private final Consumer<Chunk> operation;
      private final ChunkProcessor.TaskPriority priority;
      private final long timestamp;
      private final CompletableFuture<Void> future;

      public ChunkTask(Chunk chunk, Consumer<Chunk> operation, ChunkProcessor.TaskPriority priority) {
         this.chunk = chunk;
         this.operation = operation;
         this.priority = priority;
         this.timestamp = System.currentTimeMillis();
         this.future = new CompletableFuture<>();
      }

      public int compareTo(ChunkProcessor.ChunkTask other) {
         int priorityCompare = Integer.compare(this.priority.value, other.priority.value);
         return priorityCompare != 0 ? priorityCompare : Long.compare(this.timestamp, other.timestamp);
      }

      public void execute() {
         try {
            this.operation.accept(this.chunk);
            this.future.complete(null);
         } catch (Exception var2) {
            this.future.completeExceptionally(var2);
         }
      }

      public CompletableFuture<Void> getFuture() {
         return this.future;
      }
   }

   public static enum TaskPriority {
      CRITICAL(0),
      HIGH(1),
      NORMAL(2),
      LOW(3);

      private final int value;

      private TaskPriority(int value) {
         this.value = value;
      }
   }
}
