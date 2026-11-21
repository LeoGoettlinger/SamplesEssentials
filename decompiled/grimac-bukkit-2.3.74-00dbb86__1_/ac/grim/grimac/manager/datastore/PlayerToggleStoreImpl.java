package ac.grim.grimac.manager.datastore;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.model.SettingRecord;
import ac.grim.grimac.api.storage.model.SettingScope;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Queries;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerToggleStoreImpl implements PlayerToggleStore {
   private static final long DEFAULT_FLUSH_DELAY_MS = 500L;
   private final DataStore store;
   private final Logger logger;
   private final ScheduledExecutorService scheduler;
   private final long flushDelayMs;
   private final boolean ownsScheduler;
   private final Map<UUID, PlayerToggleStoreImpl.ToggleSlot> slots = new ConcurrentHashMap<>();

   public PlayerToggleStoreImpl(@NotNull DataStore store, @NotNull Logger logger) {
      this(store, logger, defaultScheduler(), 500L, true);
   }

   PlayerToggleStoreImpl(
      @NotNull DataStore store, @NotNull Logger logger, @NotNull ScheduledExecutorService scheduler, long flushDelayMs, boolean ownsScheduler
   ) {
      this.store = store;
      this.logger = logger;
      this.scheduler = scheduler;
      this.flushDelayMs = flushDelayMs;
      this.ownsScheduler = ownsScheduler;
   }

   private static ScheduledExecutorService defaultScheduler() {
      return Executors.newSingleThreadScheduledExecutor(r -> {
         Thread t = new Thread(r, "grim-toggle-flush");
         t.setDaemon(true);
         return t;
      });
   }

   @Override
   public void prefetch(@NotNull UUID uuid) {
      PlayerToggleStoreImpl.ToggleSlot slot = this.slots.computeIfAbsent(uuid, k -> new PlayerToggleStoreImpl.ToggleSlot());
      this.prefetchKey(uuid, slot.alerts, "alerts");
      this.prefetchKey(uuid, slot.verbose, "verbose");
      this.prefetchKey(uuid, slot.brands, "brands");
   }

   private void prefetchKey(UUID uuid, AtomicReference<PlayerToggleStoreImpl.TogglePair> ref, String key) {
      this.store.query(Categories.SETTING, new Queries.GetSetting(SettingScope.PLAYER, uuid.toString(), key)).whenComplete((page, err) -> {
         if (err != null) {
            this.logger.log(Level.FINE, "[grim-toggle] prefetch " + key + " failed for " + uuid, err);
         } else {
            Boolean value = decodeBool((Page<SettingRecord>)page);
            if (value != null) {
               trySetWithPrecedence(ref, PlayerToggleStoreImpl.Source.PERSISTED, value);
            }
         }
      });
   }

   @Nullable
   @Override
   public Boolean current(@NotNull UUID uuid, @NotNull String key) {
      PlayerToggleStoreImpl.ToggleSlot slot = this.slots.get(uuid);
      if (slot == null) {
         return null;
      } else {
         AtomicReference<PlayerToggleStoreImpl.TogglePair> ref = refFor(slot, key);
         return ref == null ? null : ref.get().value();
      }
   }

   @Override
   public void applyPermissionDefault(@NotNull UUID uuid, @NotNull String key, boolean value) {
      PlayerToggleStoreImpl.ToggleSlot slot = this.slots.computeIfAbsent(uuid, k -> new PlayerToggleStoreImpl.ToggleSlot());
      AtomicReference<PlayerToggleStoreImpl.TogglePair> ref = refFor(slot, key);
      if (ref != null) {
         if (trySetWithPrecedence(ref, PlayerToggleStoreImpl.Source.PERMISSION_DEFAULT, value)) {
            this.persist(uuid, key, value);
         }
      }
   }

   @Override
   public void applyUserToggle(@NotNull UUID uuid, @NotNull String key, boolean value) {
      PlayerToggleStoreImpl.ToggleSlot slot = this.slots.computeIfAbsent(uuid, k -> new PlayerToggleStoreImpl.ToggleSlot());
      AtomicReference<PlayerToggleStoreImpl.TogglePair> ref = refFor(slot, key);
      AtomicBoolean dirty = dirtyFor(slot, key);
      if (ref != null && dirty != null) {
         ref.set(new PlayerToggleStoreImpl.TogglePair(PlayerToggleStoreImpl.Source.USER_TOGGLED, value));
         dirty.set(true);
         this.scheduleFlush(slot, uuid);
      }
   }

   @Override
   public void evict(@NotNull UUID uuid) {
      PlayerToggleStoreImpl.ToggleSlot slot = this.slots.remove(uuid);
      if (slot != null) {
         ScheduledFuture<?> f = slot.pendingFlush.getAndSet(null);
         if (f != null) {
            f.cancel(false);
         }

         this.flushDirty(slot, uuid);
      }
   }

   private static boolean trySetWithPrecedence(AtomicReference<PlayerToggleStoreImpl.TogglePair> ref, PlayerToggleStoreImpl.Source newSource, Boolean newValue) {
      PlayerToggleStoreImpl.TogglePair next = new PlayerToggleStoreImpl.TogglePair(newSource, newValue);

      PlayerToggleStoreImpl.TogglePair cur;
      do {
         cur = ref.get();
         if (cur.source().precedence >= newSource.precedence) {
            return false;
         }
      } while (!ref.compareAndSet(cur, next));

      return true;
   }

   private void scheduleFlush(PlayerToggleStoreImpl.ToggleSlot slot, UUID uuid) {
      if (slot.pendingFlush.get() == null) {
         ScheduledFuture<?> f = this.scheduler.schedule(() -> {
            slot.pendingFlush.set(null);
            this.flushDirty(slot, uuid);
         }, this.flushDelayMs, TimeUnit.MILLISECONDS);
         if (!slot.pendingFlush.compareAndSet(null, f)) {
            f.cancel(false);
         }
      }
   }

   private void flushDirty(PlayerToggleStoreImpl.ToggleSlot slot, UUID uuid) {
      this.flushOne(uuid, "alerts", slot.alertsDirty, slot.alerts);
      this.flushOne(uuid, "verbose", slot.verboseDirty, slot.verbose);
      this.flushOne(uuid, "brands", slot.brandsDirty, slot.brands);
   }

   private void flushOne(UUID uuid, String key, AtomicBoolean dirty, AtomicReference<PlayerToggleStoreImpl.TogglePair> ref) {
      if (dirty.compareAndSet(true, false)) {
         Boolean v = ref.get().value();
         if (v != null) {
            this.persist(uuid, key, v);
         }
      }
   }

   private static AtomicReference<PlayerToggleStoreImpl.TogglePair> refFor(PlayerToggleStoreImpl.ToggleSlot s, String key) {
      switch (key) {
         case "alerts":
            return s.alerts;
         case "verbose":
            return s.verbose;
         case "brands":
            return s.brands;
         default:
            return null;
      }
   }

   private static AtomicBoolean dirtyFor(PlayerToggleStoreImpl.ToggleSlot s, String key) {
      switch (key) {
         case "alerts":
            return s.alertsDirty;
         case "verbose":
            return s.verboseDirty;
         case "brands":
            return s.brandsDirty;
         default:
            return null;
      }
   }

   private void persist(UUID uuid, String key, boolean value) {
      long now = System.currentTimeMillis();
      this.store.submit(Categories.SETTING, e -> e.scope(SettingScope.PLAYER).scopeKey(uuid.toString()).key(key).value(encodeBool(value)).updatedEpochMs(now));
   }

   private static byte[] encodeBool(boolean v) {
      return new byte[]{(byte)(v ? 1 : 0)};
   }

   @Nullable
   private static Boolean decodeBool(@NotNull Page<SettingRecord> page) {
      if (page.items().isEmpty()) {
         return null;
      } else {
         byte[] v = page.items().get(0).value();
         if (v != null && v.length != 0) {
            if (v.length == 1) {
               return v[0] != 0;
            } else {
               String s = new String(v, StandardCharsets.UTF_8).trim().toLowerCase(Locale.ROOT);
               return s.equals("1") || s.equals("true");
            }
         } else {
            return null;
         }
      }
   }

   void inspect(@NotNull UUID uuid, @NotNull BiConsumer<String, PlayerToggleStoreImpl.TogglePair> visitor) {
      PlayerToggleStoreImpl.ToggleSlot slot = this.slots.get(uuid);
      if (slot != null) {
         visitor.accept("alerts", slot.alerts.get());
         visitor.accept("verbose", slot.verbose.get());
         visitor.accept("brands", slot.brands.get());
      }
   }

   @Override
   public void shutdown() {
      if (this.ownsScheduler) {
         this.scheduler.shutdownNow();
      }
   }

   public static enum Source {
      UNKNOWN(0),
      PERMISSION_DEFAULT(10),
      PERSISTED(20),
      USER_TOGGLED(30);

      public final int precedence;

      private Source(int precedence) {
         this.precedence = precedence;
      }
   }

   public record TogglePair(@NotNull PlayerToggleStoreImpl.Source source, @Nullable Boolean value) {
      static final PlayerToggleStoreImpl.TogglePair INITIAL = new PlayerToggleStoreImpl.TogglePair(PlayerToggleStoreImpl.Source.UNKNOWN, null);
   }

   public static final class ToggleSlot {
      final AtomicReference<PlayerToggleStoreImpl.TogglePair> alerts = new AtomicReference<>(PlayerToggleStoreImpl.TogglePair.INITIAL);
      final AtomicReference<PlayerToggleStoreImpl.TogglePair> verbose = new AtomicReference<>(PlayerToggleStoreImpl.TogglePair.INITIAL);
      final AtomicReference<PlayerToggleStoreImpl.TogglePair> brands = new AtomicReference<>(PlayerToggleStoreImpl.TogglePair.INITIAL);
      final AtomicBoolean alertsDirty = new AtomicBoolean();
      final AtomicBoolean verboseDirty = new AtomicBoolean();
      final AtomicBoolean brandsDirty = new AtomicBoolean();
      final AtomicReference<ScheduledFuture<?>> pendingFlush = new AtomicReference<>();
   }
}
