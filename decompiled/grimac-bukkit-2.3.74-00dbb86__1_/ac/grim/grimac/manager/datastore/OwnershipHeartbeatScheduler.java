package ac.grim.grimac.manager.datastore;

import ac.grim.grimac.api.storage.event.ServerStartupEvent;
import ac.grim.grimac.api.storage.instance.OwnershipRenewResult;
import ac.grim.grimac.api.storage.instance.ServerOwnershipAdapter;
import ac.grim.grimac.api.storage.instance.ServerOwnershipGate;
import ac.grim.grimac.api.storage.registry.StoreId;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

final class OwnershipHeartbeatScheduler {
   @NotNull
   private final ServerOwnershipAdapter ownership;
   @NotNull
   private final StoreId ownershipStore;
   @NotNull
   private final UUID startupId;
   @NotNull
   private final UUID instanceId;
   @NotNull
   private final UUID fence;
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
   private final long leaseTtlMs;
   private final long safetyMarginMs;
   @NotNull
   private final Duration interval;
   @NotNull
   private final ServerOwnershipGate gate;
   @NotNull
   private final Consumer<ServerStartupEvent> publish;
   @NotNull
   private final Runnable lostOwnership;
   @NotNull
   private final Logger logger;
   private final AtomicBoolean lost = new AtomicBoolean(false);
   private final Object lifecycleLock = new Object();
   @Nullable
   private ScheduledExecutorService executor;
   @Nullable
   private ScheduledFuture<?> task;
   @Nullable
   private volatile Thread schedulerThread;

   OwnershipHeartbeatScheduler(
      @NotNull ServerOwnershipAdapter ownership,
      @NotNull StoreId ownershipStore,
      @NotNull UUID startupId,
      @NotNull UUID instanceId,
      @NotNull UUID fence,
      @NotNull String serverName,
      long startedEpochMs,
      @Nullable String hostname,
      @Nullable String grimVersion,
      @Nullable String serverVersionString,
      @NotNull Supplier<byte[]> verboseManifest,
      long leaseTtlMs,
      long safetyMarginMs,
      @NotNull Duration interval,
      @NotNull ServerOwnershipGate gate,
      @NotNull Consumer<ServerStartupEvent> publish,
      @NotNull Runnable lostOwnership,
      @NotNull Logger logger
   ) {
      this.ownership = ownership;
      this.ownershipStore = ownershipStore;
      this.startupId = startupId;
      this.instanceId = instanceId;
      this.fence = fence;
      this.serverName = serverName;
      this.startedEpochMs = startedEpochMs;
      this.hostname = hostname;
      this.grimVersion = grimVersion;
      this.serverVersionString = serverVersionString;
      this.verboseManifest = verboseManifest;
      this.leaseTtlMs = leaseTtlMs;
      this.safetyMarginMs = safetyMarginMs;
      this.interval = interval;
      this.gate = gate;
      this.publish = publish;
      this.lostOwnership = lostOwnership;
      this.logger = logger;
   }

   void start() {
      synchronized (this.lifecycleLock) {
         if (this.executor == null) {
            this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
               Thread t = new Thread(r, "grim-storage-ownership-" + this.serverName);
               t.setDaemon(true);
               this.schedulerThread = t;
               return t;
            });
            long periodMs = Math.max(1L, this.interval.toMillis());
            this.task = this.executor.scheduleAtFixedRate(this::tick, 0L, periodMs, TimeUnit.MILLISECONDS);
         }
      }
   }

   void publishNowAndWait() {
      ScheduledExecutorService current;
      synchronized (this.lifecycleLock) {
         current = this.executor;
      }

      if (current != null && !this.lost.get()) {
         if (Thread.currentThread() == this.schedulerThread) {
            this.tick();
         } else {
            Future<?> future = current.submit(this::tick);

            try {
               future.get();
            } catch (InterruptedException var4) {
               Thread.currentThread().interrupt();
            } catch (ExecutionException var5) {
               this.logger.log(Level.WARNING, "ownership heartbeat failed for startup " + this.startupId, var5.getCause());
            }
         }
      }
   }

   void stop() {
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
      if (!this.lost.get()) {
         try {
            OwnershipRenewResult renewed = this.ownership.renewOwnership(this.ownershipStore, this.instanceId, this.startupId, this.fence, this.leaseTtlMs);
            if (!renewed.renewed()) {
               this.onLostOwnership();
               return;
            }

            this.gate.extend(this.startupId, this.fence, this.leaseTtlMs, this.safetyMarginMs);
            ServerStartupEvent event = new ServerStartupEvent()
               .startupId(this.startupId)
               .instanceId(this.instanceId)
               .serverName(this.serverName)
               .startedEpochMs(this.startedEpochMs)
               .lastHeartbeatEpochMs(renewed.dbNowEpochMs())
               .hostname(this.hostname)
               .grimVersion(this.grimVersion)
               .serverVersionString(this.serverVersionString)
               .verboseManifest(this.verboseManifest.get());
            this.publish.accept(event);
         } catch (RuntimeException var3) {
            this.logger.log(Level.WARNING, "ownership heartbeat failed for startup " + this.startupId, (Throwable)var3);
         } catch (Exception var4) {
            this.logger.log(Level.WARNING, "ownership heartbeat failed for startup " + this.startupId, (Throwable)var4);
         }
      }
   }

   private void onLostOwnership() {
      if (this.lost.compareAndSet(false, true)) {
         this.gate.close("lost-ownership");
         this.logger
            .warning(
               "[grim-datastore] lost DB ownership for instanceId="
                  + this.instanceId
                  + " startupId="
                  + this.startupId
                  + "; disabling persistence for this boot"
            );

         try {
            this.lostOwnership.run();
         } finally {
            this.stop();
         }
      }
   }
}
