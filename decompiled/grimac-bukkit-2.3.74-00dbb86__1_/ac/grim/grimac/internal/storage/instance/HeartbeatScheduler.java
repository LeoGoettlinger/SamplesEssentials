package ac.grim.grimac.internal.storage.instance;

import ac.grim.grimac.api.storage.event.ServerStartupEvent;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApiStatus.Internal
public final class HeartbeatScheduler {
   @NotNull
   private final UUID startupId;
   @NotNull
   private final UUID instanceId;
   @NotNull
   private final String serverName;
   private final long startedEpochMs;
   @Nullable
   private final String hostname;
   @Nullable
   private final String grimVersion;
   @Nullable
   private final String serverVersionString;
   @NotNull
   private final Supplier<byte[]> verboseManifest;
   @NotNull
   private final Duration interval;
   @NotNull
   private final Consumer<ServerStartupEvent> publish;
   @NotNull
   private final Logger logger;
   private final Object lifecycleLock = new Object();
   @Nullable
   private ScheduledExecutorService executor;
   @Nullable
   private ScheduledFuture<?> task;
   @Nullable
   private volatile Thread schedulerThread;
   private volatile long closedAtEpochMs;
   @Nullable
   private volatile String closeReason;

   public HeartbeatScheduler(
      @NotNull UUID startupId,
      @NotNull UUID instanceId,
      @NotNull String serverName,
      long startedEpochMs,
      @Nullable String hostname,
      @Nullable String grimVersion,
      @Nullable String serverVersionString,
      @NotNull Duration interval,
      @NotNull Consumer<ServerStartupEvent> publish,
      @NotNull Logger logger
   ) {
      this(startupId, instanceId, serverName, startedEpochMs, hostname, grimVersion, serverVersionString, (byte[])null, interval, publish, logger);
   }

   public HeartbeatScheduler(
      @NotNull UUID startupId,
      @NotNull UUID instanceId,
      @NotNull String serverName,
      long startedEpochMs,
      @Nullable String hostname,
      @Nullable String grimVersion,
      @Nullable String serverVersionString,
      @Nullable byte[] verboseManifest,
      @NotNull Duration interval,
      @NotNull Consumer<ServerStartupEvent> publish,
      @NotNull Logger logger
   ) {
      this(startupId, instanceId, serverName, startedEpochMs, hostname, grimVersion, serverVersionString, () -> verboseManifest, interval, publish, logger);
   }

   public HeartbeatScheduler(
      @NotNull UUID startupId,
      @NotNull UUID instanceId,
      @NotNull String serverName,
      long startedEpochMs,
      @Nullable String hostname,
      @Nullable String grimVersion,
      @Nullable String serverVersionString,
      @NotNull Supplier<byte[]> verboseManifest,
      @NotNull Duration interval,
      @NotNull Consumer<ServerStartupEvent> publish,
      @NotNull Logger logger
   ) {
      if (!interval.isZero() && !interval.isNegative()) {
         this.startupId = startupId;
         this.instanceId = instanceId;
         this.serverName = serverName;
         this.startedEpochMs = startedEpochMs;
         this.hostname = hostname;
         this.grimVersion = grimVersion;
         this.serverVersionString = serverVersionString;
         this.verboseManifest = verboseManifest;
         this.interval = interval;
         this.publish = publish;
         this.logger = logger;
      } else {
         throw new IllegalArgumentException("interval must be positive, got " + interval);
      }
   }

   public void start() {
      synchronized (this.lifecycleLock) {
         if (this.executor == null) {
            this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
               Thread t = new Thread(r, "grim-storage-heartbeat-" + this.serverName);
               t.setDaemon(true);
               this.schedulerThread = t;
               return t;
            });
            long periodMs = this.interval.toMillis();
            this.task = this.executor.scheduleAtFixedRate(this::tick, 0L, periodMs, TimeUnit.MILLISECONDS);
         }
      }
   }

   public void markDrained() {
      this.closedAtEpochMs = System.currentTimeMillis();
      this.closeReason = "graceful";
   }

   public void publishDrainImmediate() {
      this.markDrained();
      this.tick();
   }

   public void publishNow() {
      synchronized (this.lifecycleLock) {
         if (this.executor != null) {
            this.executor.execute(this::tick);
         }
      }
   }

   public void publishNowAndWait() {
      ScheduledExecutorService current;
      synchronized (this.lifecycleLock) {
         current = this.executor;
      }

      if (current != null) {
         if (Thread.currentThread() == this.schedulerThread) {
            this.tick();
         } else {
            Future<?> future = current.submit(this::tick);

            try {
               future.get();
            } catch (InterruptedException var4) {
               Thread.currentThread().interrupt();
            } catch (ExecutionException var5) {
               this.logger.log(Level.WARNING, "heartbeat publish failed for startup " + this.startupId, var5.getCause());
            }
         }
      }
   }

   public void stop() {
      synchronized (this.lifecycleLock) {
         if (this.task != null) {
            this.task.cancel(false);
            this.task = null;
         }

         if (this.executor != null) {
            this.executor.shutdownNow();
            this.executor = null;
            this.schedulerThread = null;
         }
      }
   }

   private void tick() {
      try {
         ServerStartupEvent event = new ServerStartupEvent()
            .startupId(this.startupId)
            .instanceId(this.instanceId)
            .serverName(this.serverName)
            .startedEpochMs(this.startedEpochMs)
            .lastHeartbeatEpochMs(System.currentTimeMillis())
            .hostname(this.hostname)
            .grimVersion(this.grimVersion)
            .serverVersionString(this.serverVersionString)
            .verboseManifest(this.verboseManifest.get())
            .closedAtEpochMs(this.closedAtEpochMs)
            .closeReason(this.closeReason);
         this.publish.accept(event);
      } catch (RuntimeException var2) {
         this.logger.log(Level.WARNING, "heartbeat publish failed for startup " + this.startupId, (Throwable)var2);
      }
   }
}
