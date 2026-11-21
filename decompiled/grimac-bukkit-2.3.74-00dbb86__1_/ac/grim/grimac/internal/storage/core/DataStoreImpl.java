package ac.grim.grimac.internal.storage.core;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.DataStoreMetrics;
import ac.grim.grimac.api.storage.DeletionReport;
import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.config.WritePathConfig;
import ac.grim.grimac.api.storage.instance.ServerOwnershipGate;
import ac.grim.grimac.api.storage.kind.DataKind;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.kind.ops.EntityOps;
import ac.grim.grimac.api.storage.kind.ops.EventStreamOps;
import ac.grim.grimac.api.storage.model.SettingRecord;
import ac.grim.grimac.api.storage.query.DeleteCriteria;
import ac.grim.grimac.api.storage.query.Deletes;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Queries;
import ac.grim.grimac.api.storage.query.Query;
import ac.grim.grimac.internal.storage.util.UuidV7;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.BsonBinary;
import org.bson.types.Binary;

@ApiStatus.Internal
public final class DataStoreImpl implements DataStore {
   private final CategoryRouter router;
   private final RingRegistry rings;
   private final ExecutorService reader;
   private final AggregateDataStoreMetrics metrics;
   private final long shutdownDrainTimeoutMs;
   private final Logger logger;
   private volatile boolean closed;
   private volatile ServerOwnershipGate ownershipGate = ServerOwnershipGate.disabled();
   private volatile V2Routes v2Routes = V2Routes.empty();

   public DataStoreImpl(CategoryRouter router, WritePathConfig writePath, Logger logger) {
      this.router = router;
      this.logger = logger;
      this.rings = new RingRegistry(writePath, logger);
      this.metrics = new AggregateDataStoreMetrics(this.rings);
      this.shutdownDrainTimeoutMs = writePath.shutdownDrainTimeoutMs();
      AtomicInteger readerSeq = new AtomicInteger();
      this.reader = Executors.newFixedThreadPool(2, r -> {
         Thread t = new Thread(r, "grim-datastore-reader-" + readerSeq.incrementAndGet());
         t.setDaemon(true);
         return t;
      });
   }

   public DataStoreImpl withV2Routes(@NotNull V2Routes v2Routes) {
      this.v2Routes = v2Routes;
      return this;
   }

   public V2Routes v2Routes() {
      return this.v2Routes;
   }

   public DataStoreImpl withOwnershipGate(@NotNull ServerOwnershipGate ownershipGate) {
      this.ownershipGate = ownershipGate;
      this.rings.withWriteGate(() -> this.ownershipGate.allowWrites());
      return this;
   }

   public void start() {
      try {
         for (Category<?> cat : this.router.routedCategories()) {
            this.registerRingFor(cat);
         }

         if (!this.v2Routes.isEmpty()) {
            for (Category<?> cat : this.v2RoutedCategories()) {
               if (!this.rings.hasRing(cat)) {
                  this.registerRingFor(cat);
               }
            }
         }
      } catch (BackendException var3) {
         throw new RuntimeException("failed to start DataStore ring", var3);
      }
   }

   private Set<Category<?>> v2RoutedCategories() {
      Set<Category<?>> out = new LinkedHashSet<>();

      for (Category<?> c : new Category[]{
         Categories.VIOLATION,
         Categories.SESSION,
         Categories.PLAYER_IDENTITY,
         Categories.SETTING,
         Categories.SERVER_STARTUP,
         Categories.VERBOSE_SCHEMA,
         Categories.CHECK_CATALOG
      }) {
         if (this.v2Routes.contains(c)) {
            out.add(c);
         }
      }

      return out;
   }

   private <E> void registerRingFor(Category<E> cat) throws BackendException {
      V2Routes.Route<?> route = this.v2Routes.routeFor(cat);
      if (route != null) {
         this.rings.register(cat, route.backend(), route.adapter(), route.storeId(), route.kind());
      } else {
         Backend b = this.router.backendFor(cat);
         this.rings.register(cat, b);
      }
   }

   @Override
   public <E> void submit(Category<E> cat, Consumer<E> configurer) {
      if (!this.closed) {
         if (this.ownershipGate.allowWrites()) {
            if (cat == Categories.VIOLATION) {
               this.submitViolation(configurer);
            } else {
               this.rings.submit(cat, configurer);
            }
         }
      }
   }

   private <E> void submitViolation(Consumer<E> configurer) {
      this.rings.submit(Categories.VIOLATION, event -> {
         configurer.accept((E)event);
         if (event.id() == null) {
            event.id(UuidV7.next());
         }
      });
   }

   @Override
   public <R> CompletionStage<R> execute(@NotNull Operation<R> op) {
      if (this.closed) {
         CompletableFuture<R> f = new CompletableFuture<>();
         f.completeExceptionally(new IllegalStateException("DataStore is closed"));
         return f;
      } else {
         boolean isReadShaped = isReadOperation(op);
         if (!isReadShaped && !this.ownershipGate.allowWrites()) {
            CompletableFuture<R> f = new CompletableFuture<>();
            f.completeExceptionally(new IllegalStateException("persistent writes are disabled: " + this.ownershipGate.closeReason()));
            return f;
         } else {
            Category<?> cat = op.category();
            V2Routes.Route<? extends DataKind<?, ?>> route = this.v2Routes.routeFor(cat);
            if (route == null) {
               CompletableFuture<R> f = new CompletableFuture<>();
               f.completeExceptionally(
                  new IllegalArgumentException(
                     "no v2 route registered for category " + cat.id() + " — operation " + op.getClass().getSimpleName() + " cannot dispatch"
                  )
               );
               return f;
            } else {
               Executor executor = (Executor)(isReadShaped ? this.reader : this.rings.executorForBackendOperation(route.backend(), cat, this.reader));
               return CompletableFuture.supplyAsync(() -> {
                  long start = System.nanoTime();

                  Object var7x;
                  try {
                     if (!isReadShaped) {
                        this.requireWritesAllowed();
                     }

                     KindAdapter adapter = route.adapter();
                     var7x = adapter.execute(route.storeId(), route.kind(), op);
                  } catch (BackendException var15) {
                     throw new RuntimeException("v2 execute failed for " + op.getClass().getSimpleName(), var15);
                  } finally {
                     long ms = (System.nanoTime() - start) / 1000000L;
                     if (isReadShaped) {
                        this.metrics.observeReadLatencyMs(ms);
                     }
                  }

                  return (R)var7x;
               }, executor);
            }
         }
      }
   }

   private static boolean isReadOperation(@NotNull Operation<?> op) {
      String n = op.getClass().getSimpleName();
      return !n.startsWith("Upsert")
         && !n.startsWith("Delete")
         && !n.startsWith("Remove")
         && !n.startsWith("Put")
         && !n.startsWith("IncrementBy")
         && !n.startsWith("SetIfHigher");
   }

   @Override
   public <R> CompletionStage<Page<R>> query(Category<?> cat, Query<R> query) {
      V2Routes.Route<?> route = this.v2Routes.routeFor(cat);
      if (route != null) {
         Operation<?> v2Op = QueryToOperationTranslator.translate(cat, query);
         if (v2Op != null) {
            return this.translateV2Result(this.execute(v2Op), query);
         }
      }

      Backend b = this.router.backendFor(cat);
      if (b == null) {
         CompletableFuture<Page<R>> f = new CompletableFuture<>();
         f.completeExceptionally(
            new IllegalArgumentException(
               "no backend (v2 or legacy) for category " + cat.id() + " — query " + query.getClass().getSimpleName() + " cannot dispatch"
            )
         );
         return f;
      } else {
         return CompletableFuture.supplyAsync(() -> {
            long start = System.nanoTime();

            Page e;
            try {
               e = b.read(cat, query);
            } catch (BackendException var10) {
               throw new RuntimeException("backend read failed", var10);
            } finally {
               this.metrics.observeReadLatencyMs((System.nanoTime() - start) / 1000000L);
            }

            return e;
         }, this.reader);
      }
   }

   private <R> CompletionStage<Page<R>> translateV2Result(CompletionStage<?> future, Query<R> originalQuery) {
      return future.thenApply(
         raw -> {
            if (raw instanceof Page<?> page) {
               return page;
            } else if (raw instanceof Optional<?> opt) {
               if (originalQuery instanceof Queries.GetSetting g) {
                  if (opt.isEmpty()) {
                     return Page.empty();
                  } else {
                     Object rawValue = opt.get();
                     byte[] bytes = rawValue instanceof byte[] bs
                        ? bs
                        : (rawValue instanceof Binary b ? b.getData() : (rawValue instanceof BsonBinary bb ? bb.getData() : null));
                     if (bytes == null) {
                        throw new IllegalStateException("unexpected SETTING value type: " + rawValue.getClass().getName());
                     } else {
                        SettingRecord rec = new SettingRecord(g.scope(), g.scopeKey(), g.key(), bytes, System.currentTimeMillis());
                        return new Page<>(List.of(rec), null);
                     }
                  }
               } else {
                  return opt.isPresent() ? new Page<>(List.of(opt.get()), null) : Page.empty();
               }
            } else {
               throw new IllegalStateException(
                  "unexpected v2 execute result type: " + raw.getClass().getName() + " for query " + originalQuery.getClass().getSimpleName()
               );
            }
         }
      );
   }

   @Override
   public <E> CompletionStage<Void> delete(Category<E> cat, DeleteCriteria criteria) {
      if (!this.ownershipGate.allowWrites()) {
         CompletableFuture<Void> f = new CompletableFuture<>();
         f.completeExceptionally(new IllegalStateException("persistent writes are disabled: " + this.ownershipGate.closeReason()));
         return f;
      } else {
         V2Routes.Route<?> route = this.v2Routes.routeFor(cat);
         if (route != null) {
            Operation<Void> v2Op = DeleteCriteriaTranslator.translate(cat, criteria);
            if (v2Op != null) {
               return this.execute(v2Op).handle((v, ex) -> {
                  if (ex == null) {
                     return CompletableFuture.completedFuture(null);
                  } else if (findUoe(ex) != null) {
                     return this.legacyDelete(cat, criteria);
                  } else {
                     CompletableFuture<Void> f = new CompletableFuture<>();
                     f.completeExceptionally(ex);
                     return f;
                  }
               }).thenCompose(s -> s);
            }
         }

         return this.legacyDelete(cat, criteria);
      }
   }

   private <E> CompletionStage<Void> legacyDelete(Category<E> cat, DeleteCriteria criteria) {
      if (!this.ownershipGate.allowWrites()) {
         CompletableFuture<Void> f = new CompletableFuture<>();
         f.completeExceptionally(new IllegalStateException("persistent writes are disabled: " + this.ownershipGate.closeReason()));
         return f;
      } else {
         Backend b = this.router.backendFor(cat);
         if (b == null) {
            CompletableFuture<Void> f = new CompletableFuture<>();
            f.completeExceptionally(
               new IllegalArgumentException(
                  "no backend (v2 or legacy) for category " + cat.id() + " — delete cannot dispatch (criteria: " + criteria.getClass().getSimpleName() + ")"
               )
            );
            return f;
         } else {
            return CompletableFuture.runAsync(() -> {
               try {
                  this.requireWritesAllowed();
                  b.delete(cat, criteria);
               } catch (BackendException var5x) {
                  throw new RuntimeException("backend delete failed", var5x);
               }
            }, this.reader);
         }
      }
   }

   @Nullable
   private static UnsupportedOperationException findUoe(@NotNull Throwable t) {
      Throwable cur = t;
      int depth = 0;

      while (cur != null && depth++ < 32) {
         if (cur instanceof UnsupportedOperationException uoe) {
            return uoe;
         }

         Throwable next = cur.getCause();
         if (next == null || next == cur) {
            return null;
         }

         cur = next;
      }

      return null;
   }

   @Override
   public CompletionStage<DeletionReport> forgetPlayer(UUID uuid) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            int sessions = 0;
            int violations = 0;
            int identities = 0;
            V2Routes.Route<?> sessionRoute = this.v2Routes.routeFor(Categories.SESSION);
            if (sessionRoute != null) {
               Operation<?> op = QueryToOperationTranslator.translate(Categories.SESSION, Queries.listSessionsByPlayer(uuid, 100000, null));
               if (op != null && this.executeSync(sessionRoute, op) instanceof Page<?> p) {
                  sessions = p.items().size();
               }
            } else {
               Backend b = this.router.backendFor(Categories.SESSION);
               Page<?> p = b.read(Categories.SESSION, Queries.listSessionsByPlayer(uuid, 100000, null));
               sessions = p.items().size();
            }

            V2Routes.Route<?> violationRoute = this.v2Routes.routeFor(Categories.VIOLATION);
            if (violationRoute != null && sessions > 0) {
               try {
                  if (this.executeSync(violationRoute, new EventStreamOps.CountOp(Categories.VIOLATION, "player_uuid", uuid)) instanceof Long l) {
                     violations = l.intValue();
                  }
               } catch (Exception var11) {
                  violations = 0;
               }
            } else {
               Backend b = this.router.backendFor(Categories.VIOLATION);
               violations = (int)b.countViolationsInSession(uuid);
            }

            V2Routes.Route<?> idRoute = this.v2Routes.routeFor(Categories.PLAYER_IDENTITY);
            if (idRoute != null) {
               Operation<?> op = new EntityOps.GetByIdOp<>(Categories.PLAYER_IDENTITY, uuid);
               identities = this.executeSync(idRoute, op) instanceof Optional<?> opt && opt.isPresent() ? 1 : 0;
            } else {
               Backend b = this.router.backendFor(Categories.PLAYER_IDENTITY);
               Page<?> p = b.read(Categories.PLAYER_IDENTITY, Queries.getPlayerIdentity(uuid));
               identities = p.items().size();
            }

            this.deleteSync(Categories.VIOLATION, Deletes.byPlayer(uuid));
            this.deleteSync(Categories.SESSION, Deletes.byPlayer(uuid));
            this.deleteSync(Categories.PLAYER_IDENTITY, Deletes.byPlayer(uuid));
            this.deleteSync(Categories.SETTING, Deletes.byPlayer(uuid));
            return new DeletionReport(sessions, violations, 0, identities, 0);
         } catch (Exception var12) {
            throw new RuntimeException("forgetPlayer failed", var12);
         }
      }, this.reader);
   }

   private Object executeSync(@NotNull V2Routes.Route<?> route, @NotNull Operation<?> op) throws BackendException {
      if (!isReadOperation(op)) {
         this.requireWritesAllowed();
      }

      KindAdapter adapter = route.adapter();
      return adapter.execute(route.storeId(), route.kind(), op);
   }

   private <E> void deleteSync(@NotNull Category<E> cat, @NotNull DeleteCriteria criteria) throws BackendException {
      this.requireWritesAllowed();
      V2Routes.Route<?> route = this.v2Routes.routeFor(cat);
      if (route != null) {
         Operation<Void> v2Op = DeleteCriteriaTranslator.translate(cat, criteria);
         if (v2Op != null) {
            this.executeSync(route, v2Op);
            return;
         }
      }

      Backend b = this.router.backendFor(cat);
      b.delete(cat, criteria);
   }

   private void requireWritesAllowed() {
      if (!this.ownershipGate.allowWrites()) {
         throw new IllegalStateException("persistent writes are disabled: " + this.ownershipGate.closeReason());
      }
   }

   @Override
   public CompletionStage<Long> countViolationsInSession(UUID sessionId) {
      V2Routes.Route<?> route = this.v2Routes.routeFor(Categories.VIOLATION);
      if (route != null) {
         return this.execute(new EventStreamOps.CountOp(Categories.VIOLATION, "session_id", sessionId));
      } else {
         Backend b = this.router.backendFor(Categories.VIOLATION);
         return CompletableFuture.supplyAsync(() -> {
            try {
               return b.countViolationsInSession(sessionId);
            } catch (BackendException var3x) {
               throw new RuntimeException("countViolationsInSession failed", var3x);
            }
         }, this.reader);
      }
   }

   @Override
   public CompletionStage<Long> countUniqueChecksInSession(UUID sessionId) {
      V2Routes.Route<?> route = this.v2Routes.routeFor(Categories.VIOLATION);
      if (route != null) {
         return this.execute(new EventStreamOps.CountDistinctOp(Categories.VIOLATION, "session_id", sessionId, "check_id"));
      } else {
         Backend b = this.router.backendFor(Categories.VIOLATION);
         return CompletableFuture.supplyAsync(() -> {
            try {
               return b.countUniqueChecksInSession(sessionId);
            } catch (BackendException var3x) {
               throw new RuntimeException("countUniqueChecksInSession failed", var3x);
            }
         }, this.reader);
      }
   }

   @Override
   public CompletionStage<Long> countSessionsByPlayer(UUID player) {
      V2Routes.Route<?> route = this.v2Routes.routeFor(Categories.SESSION);
      if (route != null) {
         return this.execute(new EntityOps.CountByIndexOp(Categories.SESSION, "by_player_started", player));
      } else {
         Backend b = this.router.backendFor(Categories.SESSION);
         return CompletableFuture.supplyAsync(() -> {
            try {
               return b.countSessionsByPlayer(player);
            } catch (BackendException var3x) {
               throw new RuntimeException("countSessionsByPlayer failed", var3x);
            }
         }, this.reader);
      }
   }

   @Override
   public DataStoreMetrics metrics() {
      return this.metrics;
   }

   @Override
   public synchronized void flushAndClose(long drainTimeoutMs) {
      if (!this.closed) {
         this.closed = true;
         long effective = drainTimeoutMs > 0L ? drainTimeoutMs : this.shutdownDrainTimeoutMs;
         long leftover = this.rings.shutdown(effective);
         this.reader.shutdown();

         try {
            if (!this.reader.awaitTermination(effective, TimeUnit.MILLISECONDS)) {
               this.reader.shutdownNow();
               if (!this.reader.awaitTermination(2L, TimeUnit.SECONDS)) {
                  this.logger
                     .log(
                        Level.WARNING,
                        "[grim-datastore] reader pool did not terminate after "
                           + effective
                           + "ms drain + 2s interrupt grace; proceeding with backend close (in-flight reads may fail)."
                     );
               }
            }
         } catch (InterruptedException var12) {
            Thread.currentThread().interrupt();
            this.reader.shutdownNow();
         }

         for (Backend b : this.router.allBackends()) {
            try {
               b.flush();
            } catch (BackendException var11) {
               this.logger.log(Level.WARNING, "[grim-datastore] flush failed for backend " + b.id(), (Throwable)var11);
            }

            try {
               b.close();
            } catch (BackendException var10) {
               this.logger.log(Level.WARNING, "[grim-datastore] close failed for backend " + b.id(), (Throwable)var10);
            }
         }

         if (leftover > 0L) {
            this.logger.log(Level.WARNING, "[grim-datastore] shutdown dropped " + leftover + " unwritten records total");
         }
      }
   }

   public CategoryRouter router() {
      return this.router;
   }

   public ExecutorService readerExecutor() {
      return this.reader;
   }
}
