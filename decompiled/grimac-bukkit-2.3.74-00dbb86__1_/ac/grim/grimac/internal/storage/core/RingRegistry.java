package ac.grim.grimac.internal.storage.core;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.BackendV2;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.config.WritePathConfig;
import ac.grim.grimac.api.storage.event.CheckCatalogEvent;
import ac.grim.grimac.api.storage.event.PlayerIdentityEvent;
import ac.grim.grimac.api.storage.event.ServerInstanceEvent;
import ac.grim.grimac.api.storage.event.ServerStartupEvent;
import ac.grim.grimac.api.storage.event.SessionEvent;
import ac.grim.grimac.api.storage.event.SettingEvent;
import ac.grim.grimac.api.storage.event.VerboseSchemaEvent;
import ac.grim.grimac.api.storage.event.ViolationEvent;
import ac.grim.grimac.api.storage.kind.CounterEvent;
import ac.grim.grimac.api.storage.kind.DataKind;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApiStatus.Internal
public final class RingRegistry {
   private final Map<Category<?>, RingRegistry.Entry<?>> entries = new LinkedHashMap<>();
   private final WritePathConfig cfg;
   private final Logger logger;
   private final AtomicLong submittedTotal = new AtomicLong();
   private final AtomicLong droppedOnOverflowTotal = new AtomicLong();
   @NotNull
   private volatile BooleanSupplier writeAllowed = () -> true;
   private final Map<String, ExecutorService> singleWriterExecutors = new LinkedHashMap<>();
   private static final EventTranslatorOneArg PUBLISH_TRANSLATOR = (event, sequence, cfg) -> {
      Object slot = resetIfKnown(event);
      ((Consumer)cfg).accept(slot);
   };

   public RingRegistry(@NotNull WritePathConfig cfg, @NotNull Logger logger) {
      this.cfg = cfg;
      this.logger = logger;
   }

   @NotNull
   public RingRegistry withWriteGate(@NotNull BooleanSupplier writeAllowed) {
      this.writeAllowed = writeAllowed;
      return this;
   }

   public <E> void register(@NotNull Category<E> cat, @NotNull Backend backend) throws BackendException {
      this.registerInternal(cat, backend.id(), backend, backend.eventHandlerFor(cat), false);
   }

   public <E> void register(
      @NotNull Category<E> cat, @NotNull BackendV2 backend, @NotNull KindAdapter adapter, @NotNull StoreId storeId, @NotNull DataKind kind
   ) throws BackendException {
      StorageEventHandler<E> handler = adapter.writeHandler(storeId, kind, cat);
      if (usesSharedSingleWriter(backend, cat)) {
         ExecutorService exec = this.singleWriterExecutor(backend.id());
         handler = (event, sequence, endOfBatch) -> {
            try {
               exec.submit(() -> {
                  try {
                     handler.onEvent((E)event, sequence, endOfBatch);
                  } catch (Exception var7x) {
                     this.logger.log(Level.WARNING, "[grim-datastore] single-writer handler failed", (Throwable)var7x);
                  }
               }).get();
            } catch (Exception var8) {
               throw new BackendException("single-writer submit failed", var8);
            }
         };
      }

      this.registerInternal(cat, backend.id(), backend, handler, false);
   }

   @NotNull
   public Executor executorForBackendOperation(@NotNull BackendV2 backend, @NotNull Category<?> cat, @NotNull Executor fallback) {
      return (Executor)(usesSharedSingleWriter(backend, cat) ? this.singleWriterExecutor(backend.id()) : fallback);
   }

   private static boolean usesSharedSingleWriter(@NotNull BackendV2 backend, @NotNull Category<?> cat) {
      return backend.writerThreads(cat) <= 1 && !backend.capabilities().contains(Capability.MULTI_WRITER);
   }

   private ExecutorService singleWriterExecutor(@NotNull String backendId) {
      return this.singleWriterExecutors.computeIfAbsent(backendId, id -> Executors.newSingleThreadExecutor(namedDaemon("grim-datastore-" + id + "-writer")));
   }

   private <E> void registerInternal(
      @NotNull Category<E> cat, @NotNull String backendId, @NotNull Object backendRef, @NotNull StorageEventHandler<E> handler, boolean unused
   ) throws BackendException {
      if (this.entries.containsKey(cat)) {
         throw new IllegalStateException("ring already registered for category " + cat.id());
      } else {
         StorageEventHandler<E> guardedHandler = (event, sequence, endOfBatch) -> {
            if (this.writeAllowed.getAsBoolean()) {
               handler.onEvent(event, sequence, endOfBatch);
            }
         };
         DisruptorEventHandlerAdapter<E> adapter = new DisruptorEventHandlerAdapter<>(cat.id(), guardedHandler, this.cfg.warnRateMs(), this.logger);
         ThreadFactory tf = namedDaemon("grim-datastore-" + backendId + "-" + cat.id());
         Disruptor<E> disruptor = new Disruptor<>(
            cat.newEvent()::get,
            this.cfg.queueCapacity(),
            tf,
            ProducerType.MULTI,
            WaitStrategies.resolve(this.cfg.waitStrategy(), Math.max(1L, this.cfg.flushIntervalMs()))
         );
         disruptor.handleEventsWith(adapter);
         RingBuffer<E> ring = disruptor.start();
         this.entries.put(cat, new RingRegistry.Entry<>(cat, backendRef, disruptor, ring, adapter));
      }
   }

   public <E> boolean submit(@NotNull Category<E> cat, @NotNull Consumer<E> configurer) {
      RingRegistry.Entry<E> e = (RingRegistry.Entry<E>)this.entries.get(cat);
      if (e == null) {
         this.droppedOnOverflowTotal.incrementAndGet();
         return false;
      } else {
         boolean ok = e.ring.tryPublishEvent(PUBLISH_TRANSLATOR, configurer);
         if (ok) {
            this.submittedTotal.incrementAndGet();
         } else {
            this.droppedOnOverflowTotal.incrementAndGet();
         }

         return ok;
      }
   }

   private static <E> E resetIfKnown(E event) {
      if (event instanceof ViolationEvent v) {
         v.reset();
         return (E)v;
      } else if (event instanceof SessionEvent s) {
         s.reset();
         return (E)s;
      } else if (event instanceof PlayerIdentityEvent p) {
         p.reset();
         return (E)p;
      } else if (event instanceof SettingEvent s) {
         s.reset();
         return (E)s;
      } else if (event instanceof CheckCatalogEvent c) {
         c.reset();
         return (E)c;
      } else if (event instanceof ServerInstanceEvent i) {
         i.reset();
         return (E)i;
      } else if (event instanceof ServerStartupEvent s) {
         s.reset();
         return (E)s;
      } else if (event instanceof VerboseSchemaEvent v) {
         v.reset();
         return (E)v;
      } else if (event instanceof CounterEvent<?> co) {
         co.clear();
         return (E)co;
      } else {
         return event;
      }
   }

   public int queuedCountFor(@NotNull Category<?> cat) {
      RingRegistry.Entry<?> e = this.entries.get(cat);
      return e == null ? 0 : queuedCount(e);
   }

   public int queuedCountTotal() {
      int sum = 0;

      for (RingRegistry.Entry<?> e : this.entries.values()) {
         sum += queuedCount(e);
      }

      return sum;
   }

   private static int queuedCount(RingRegistry.Entry<?> e) {
      return (int)(e.ring.getBufferSize() - e.ring.remainingCapacity());
   }

   public long submittedTotal() {
      return this.submittedTotal.get();
   }

   public long droppedOnOverflowTotal() {
      return this.droppedOnOverflowTotal.get();
   }

   public long droppedOnErrorTotal() {
      long sum = 0L;

      for (RingRegistry.Entry<?> e : this.entries.values()) {
         sum += e.adapter.droppedOnErrorTotal();
      }

      return sum;
   }

   public long writeBatchLatencyMsEmaMax() {
      long max = 0L;

      for (RingRegistry.Entry<?> e : this.entries.values()) {
         max = Math.max(max, e.adapter.batchCommitLatencyMsEma());
      }

      return max;
   }

   public long shutdown(long drainTimeoutMs) {
      long leftover = 0L;

      for (RingRegistry.Entry<?> e : this.entries.values()) {
         leftover += this.shutdownOne(e, drainTimeoutMs);
      }

      for (ExecutorService exec : this.singleWriterExecutors.values()) {
         exec.shutdown();

         try {
            exec.awaitTermination(2L, TimeUnit.SECONDS);
         } catch (InterruptedException var8) {
            Thread.currentThread().interrupt();
            exec.shutdownNow();
         }
      }

      this.singleWriterExecutors.clear();
      return leftover;
   }

   private long shutdownOne(RingRegistry.Entry<?> e, long drainTimeoutMs) {
      try {
         e.disruptor.shutdown(drainTimeoutMs, TimeUnit.MILLISECONDS);
         return 0L;
      } catch (TimeoutException var9) {
         long leftover = queuedCount(e);
         this.logger.log(Level.WARNING, "[grim-datastore] ring " + e.category.id() + " shutdown timed out with " + leftover + " events unwritten");

         try {
            e.disruptor.halt();
         } catch (RuntimeException var8) {
         }

         return leftover;
      }
   }

   public boolean hasRing(@NotNull Category<?> cat) {
      return this.entries.containsKey(cat);
   }

   @Nullable
   public Backend backendFor(@NotNull Category<?> cat) {
      RingRegistry.Entry<?> e = this.entries.get(cat);
      return e != null && e.backendRef instanceof Backend b ? b : null;
   }

   @Nullable
   public BackendV2 backendV2For(@NotNull Category<?> cat) {
      RingRegistry.Entry<?> e = this.entries.get(cat);
      return e != null && e.backendRef instanceof BackendV2 b ? b : null;
   }

   private static ThreadFactory namedDaemon(String prefix) {
      AtomicInteger seq = new AtomicInteger();
      return r -> {
         Thread t = new Thread(r, prefix + "-" + seq.incrementAndGet());
         t.setDaemon(true);
         return t;
      };
   }

   private record Entry<E>(Category<E> category, Object backendRef, Disruptor<E> disruptor, RingBuffer<E> ring, DisruptorEventHandlerAdapter<E> adapter) {
   }
}
