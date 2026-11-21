package ac.grim.grimac.internal.storage.verbose;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.event.VerboseSchemaEvent;
import ac.grim.grimac.api.storage.kind.ops.EntityOps;
import ac.grim.grimac.api.storage.model.VerboseSchemaRecord;
import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.api.storage.verbose.VerboseBuf;
import ac.grim.grimac.api.storage.verbose.VerboseFormatter;
import ac.grim.grimac.api.storage.verbose.VerboseRenderContext;
import ac.grim.grimac.api.storage.verbose.VerboseSchema;
import ac.grim.grimac.api.storage.verbose.VerboseSink;
import ac.grim.grimac.internal.storage.checks.CheckRegistry;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApiStatus.Internal
public final class VerboseRegistryImpl implements VerboseRegistry {
   @Nullable
   private final DataStore store;
   @NotNull
   private final CheckRegistry checks;
   private final int flavor;
   @NotNull
   private final Category<VerboseSchemaEvent> category;
   @NotNull
   private final Logger logger;
   private final ConcurrentMap<String, VerboseSchema> schemasByStableKey = new ConcurrentHashMap<>();
   private final ConcurrentMap<String, Verbose> templatesByStableKey = new ConcurrentHashMap<>();
   private final ConcurrentMap<String, VerboseFormatter> formattersByStableKey = new ConcurrentHashMap<>();
   private final ConcurrentMap<VerboseRegistryImpl.FormatterKey, VerboseFormatter> formattersByTuple = new ConcurrentHashMap<>();
   private final ConcurrentMap<VerboseRegistryImpl.LayoutKey, Optional<VerboseSchema.Layout>> layoutCache = new ConcurrentHashMap<>();
   private final Object changeLock = new Object();
   @Nullable
   private volatile Runnable changeListener;
   private int changeBatchDepth;
   private boolean changePending;

   public VerboseRegistryImpl(@Nullable DataStore store, @NotNull CheckRegistry checks, int flavor) {
      this(store, checks, flavor, Categories.VERBOSE_SCHEMA, Logger.getLogger(VerboseRegistryImpl.class.getName()));
   }

   public VerboseRegistryImpl(
      @Nullable DataStore store, @NotNull CheckRegistry checks, int flavor, @NotNull Category<VerboseSchemaEvent> category, @NotNull Logger logger
   ) {
      this.store = store;
      this.checks = checks;
      this.flavor = flavor;
      this.category = category;
      this.logger = logger;
   }

   @Override
   public void register(@NotNull String stableKey, @NotNull VerboseSchema schema) {
      if (stableKey.isEmpty()) {
         throw new IllegalArgumentException("stableKey");
      } else if (!isBinaryVersion(schema.version())) {
         throw new IllegalArgumentException("schema version must be positive");
      } else {
         VerboseSchema previous = this.schemasByStableKey.put(stableKey, schema);
         if (previous != null && (previous.version() != schema.version() || !Arrays.equals(previous.layoutBytes(), schema.layoutBytes()))) {
            this.logger.warning(() -> "verbose schema for " + stableKey + " was registered more than once with different content");
         }

         this.resolveAndIntern(stableKey, schema, this.checks);
      }
   }

   @Override
   public void registerFormatter(@NotNull String stableKey, @NotNull VerboseFormatter formatter) {
      if (stableKey.isEmpty()) {
         throw new IllegalArgumentException("stableKey");
      } else if (!isBinaryVersion(formatter.version())) {
         throw new IllegalArgumentException("formatter version must be positive");
      } else {
         this.formattersByStableKey.put(stableKey, formatter);
         this.checks
            .getId(stableKey)
            .ifPresent(checkId -> this.formattersByTuple.put(new VerboseRegistryImpl.FormatterKey(this.flavor, checkId, formatter.version()), formatter));
      }
   }

   @Override
   public void registerTemplate(
      @NotNull String stableKey, @NotNull String checkName, @Nullable String description, @Nullable String pluginVersion, @NotNull Verbose verbose
   ) {
      if (stableKey.isEmpty()) {
         throw new IllegalArgumentException("stableKey");
      } else {
         Verbose existing = this.templatesByStableKey.get(stableKey);
         if (existing != verbose) {
            if (existing == null || !existing.template().equals(verbose.template())) {
               if (existing != null) {
                  this.logger
                     .warning(
                        () -> "verbose template for "
                           + stableKey
                           + " replaced without restart: \""
                           + existing.template()
                           + "\" -> \""
                           + verbose.template()
                           + "\""
                     );
               }

               this.templatesByStableKey.put(stableKey, verbose);
               this.schemasByStableKey.put(stableKey, verbose.schema());
               VerboseFormatter formatter = verbose.asFormatter();
               this.formattersByStableKey.put(stableKey, formatter);
               this.checks.intern(stableKey, checkName, description, pluginVersion);
               this.resolveAndIntern(stableKey, verbose.schema(), this.checks);
               this.checks
                  .getId(stableKey)
                  .ifPresent(checkId -> this.formattersByTuple.put(new VerboseRegistryImpl.FormatterKey(this.flavor, checkId, verbose.version()), formatter));
               this.emitChange();
            }
         }
      }
   }

   @Override
   public void registerTemplates(@NotNull Runnable registration) {
      Objects.requireNonNull(registration, "registration");
      synchronized (this.changeLock) {
         this.changeBatchDepth++;
      }

      try {
         registration.run();
      } finally {
         Runnable listener = null;
         synchronized (this.changeLock) {
            this.changeBatchDepth--;
            if (this.changeBatchDepth == 0 && this.changePending) {
               this.changePending = false;
               listener = this.changeListener;
            }
         }

         this.runChangeListener(listener);
      }
   }

   @Override
   public void onChange(@Nullable Runnable listener) {
      this.changeListener = listener;
   }

   private void emitChange() {
      Runnable listener;
      synchronized (this.changeLock) {
         if (this.changeBatchDepth > 0) {
            this.changePending = true;
            return;
         }

         listener = this.changeListener;
      }

      this.runChangeListener(listener);
   }

   private void runChangeListener(@Nullable Runnable listener) {
      if (listener != null) {
         try {
            listener.run();
         } catch (RuntimeException var3) {
            this.logger.log(Level.WARNING, "verbose registry change listener failed", (Throwable)var3);
         }
      }
   }

   @NotNull
   @Override
   public String render(@NotNull String stableKey, @NotNull byte[] data, @NotNull VerboseRenderContext ctx) {
      if (!stableKey.isEmpty() && data.length != 0) {
         try {
            VerboseFormatter formatter = this.formattersByStableKey.get(stableKey);
            if (formatter != null) {
               StringBuilder rendered = new StringBuilder();
               formatter.render(VerboseBuf.wrap(data), ctx, VerboseSink.into(rendered));
               return rendered.toString();
            } else {
               VerboseSchema schema = this.schemasByStableKey.get(stableKey);
               if (schema != null) {
                  StringBuilder rendered = new StringBuilder();
                  GenericVerboseReader.render(new VerboseSchema.Layout(schema.fields()), VerboseBuf.wrap(data), ctx, VerboseSink.into(rendered));
                  return rendered.toString();
               } else {
                  return "";
               }
            }
         } catch (Throwable var7) {
            return "";
         }
      } else {
         return "";
      }
   }

   @NotNull
   @Override
   public Map<Integer, Integer> checkIdVersions(@NotNull CheckRegistry checks) {
      Map<Integer, Integer> versions = new LinkedHashMap<>();

      for (Entry<String, VerboseSchema> entry : this.schemasByStableKey.entrySet()) {
         Optional<Integer> checkId = checks.getId(entry.getKey());
         if (!checkId.isEmpty()) {
            VerboseSchema schema = entry.getValue();
            if (isBinaryVersion(schema.version())) {
               versions.put(checkId.get(), schema.version());
               this.resolveAndIntern(entry.getKey(), schema, checks);
            }
         }
      }

      for (Entry<String, VerboseFormatter> entryx : this.formattersByStableKey.entrySet()) {
         VerboseFormatter formatter = entryx.getValue();
         if (isBinaryVersion(formatter.version())) {
            checks.getId(entryx.getKey())
               .ifPresent(checkId -> this.formattersByTuple.put(new VerboseRegistryImpl.FormatterKey(this.flavor, checkId, formatter.version()), formatter));
         }
      }

      return Map.copyOf(versions);
   }

   @Nullable
   @Override
   public VerboseFormatter codeFormatter(int flavor, int checkId, int version) {
      if (flavor != this.flavor) {
         return null;
      } else if (!isBinaryVersion(version)) {
         return null;
      } else {
         VerboseRegistryImpl.FormatterKey key = new VerboseRegistryImpl.FormatterKey(flavor, checkId, version);
         VerboseFormatter cached = this.formattersByTuple.get(key);
         if (cached != null) {
            return cached;
         } else {
            Optional<String> stableKey = this.checks.stableKeyFor(checkId);
            if (stableKey.isEmpty()) {
               return null;
            } else {
               VerboseFormatter formatter = this.formattersByStableKey.get(stableKey.get());
               if (formatter != null && formatter.version() == version) {
                  this.formattersByTuple.putIfAbsent(key, formatter);
                  return formatter;
               } else {
                  return null;
               }
            }
         }
      }
   }

   @Nullable
   @Override
   public VerboseSchema.Layout layout(int flavor, int checkId, int version) {
      if (!isBinaryVersion(version)) {
         return null;
      } else {
         VerboseRegistryImpl.LayoutKey key = new VerboseRegistryImpl.LayoutKey(flavor, checkId, version);
         Optional<VerboseSchema.Layout> cached = this.layoutCache.get(key);
         if (cached != null) {
            return cached.orElse(null);
         } else {
            VerboseRegistryImpl.LayoutLookup loaded = this.loadLayout(key);
            if (!loaded.cacheable()) {
               Optional<VerboseSchema.Layout> raced = this.layoutCache.get(key);
               return raced != null ? raced.orElse(null) : loaded.layout().orElse(null);
            } else {
               Optional<VerboseSchema.Layout> previous = this.layoutCache.putIfAbsent(key, loaded.layout());
               return (previous != null ? previous : loaded.layout()).orElse(null);
            }
         }
      }
   }

   private void resolveAndIntern(@NotNull String stableKey, @NotNull VerboseSchema schema, @NotNull CheckRegistry checks) {
      if (isBinaryVersion(schema.version())) {
         Optional<Integer> checkId = checks.getId(stableKey);
         if (!checkId.isEmpty()) {
            VerboseRegistryImpl.LayoutKey key = new VerboseRegistryImpl.LayoutKey(this.flavor, checkId.get(), schema.version());
            Optional<VerboseSchema.Layout> effective = this.internSchema(stableKey, checkId.get(), schema);
            this.layoutCache.put(key, effective);
            VerboseFormatter formatter = this.formattersByStableKey.get(stableKey);
            if (formatter != null && isBinaryVersion(formatter.version())) {
               this.formattersByTuple.put(new VerboseRegistryImpl.FormatterKey(this.flavor, checkId.get(), formatter.version()), formatter);
            }
         }
      }
   }

   @NotNull
   private Optional<VerboseSchema.Layout> internSchema(@NotNull String stableKey, int checkId, @NotNull VerboseSchema schema) {
      if (!isBinaryVersion(schema.version())) {
         return Optional.empty();
      } else {
         String schemaKey = VerboseSchemaRecord.keyOf(this.flavor, checkId, schema.version());
         byte[] layoutBytes = this.layoutBytesFor(stableKey, schema);
         if (this.store == null) {
            return Optional.of(VerboseSchema.decodeLayout(layoutBytes));
         } else {
            VerboseRegistryImpl.RecordLookup existing = this.loadRecord(schemaKey);
            if (existing.isPresent()) {
               VerboseSchemaRecord row = existing.record().get();
               if (!Arrays.equals(row.layout(), layoutBytes)) {
                  this.logger.warning(() -> "verbose schema conflict for " + schemaKey + " (" + stableKey + "): keeping existing durable layout");
               }

               try {
                  return Optional.of(VerboseSchema.decodeLayout(row.layout()));
               } catch (RuntimeException var11) {
                  this.logger.log(Level.WARNING, "stored verbose schema layout is unreadable for " + schemaKey, (Throwable)var11);
                  return Optional.empty();
               }
            } else if (existing.lookupFailed()) {
               return Optional.of(VerboseSchema.decodeLayout(layoutBytes));
            } else {
               long now = System.currentTimeMillis();
               VerboseSchemaRecord record = new VerboseSchemaRecord(schemaKey, this.flavor, checkId, schema.version(), layoutBytes, now);

               try {
                  this.store.execute(new EntityOps.UpsertOp<>(this.category, record)).toCompletableFuture().join();
               } catch (RuntimeException var12) {
                  this.logger.log(Level.WARNING, "failed to persist verbose schema " + schemaKey, (Throwable)var12);
               }

               return Optional.of(VerboseSchema.decodeLayout(layoutBytes));
            }
         }
      }
   }

   @NotNull
   private byte[] layoutBytesFor(@NotNull String stableKey, @NotNull VerboseSchema schema) {
      Verbose verbose = this.templatesByStableKey.get(stableKey);
      return verbose != null && verbose.version() == schema.version() ? verbose.layoutBytes() : schema.layoutBytes();
   }

   @NotNull
   private VerboseRegistryImpl.LayoutLookup loadLayout(@NotNull VerboseRegistryImpl.LayoutKey key) {
      if (key.flavor() == this.flavor) {
         Optional<String> stableKey = this.checks.stableKeyFor(key.checkId());
         if (stableKey.isPresent()) {
            VerboseSchema schema = this.schemasByStableKey.get(stableKey.get());
            if (schema != null && schema.version() == key.version()) {
               return VerboseRegistryImpl.LayoutLookup.confirmed(Optional.of(VerboseSchema.decodeLayout(this.layoutBytesFor(stableKey.get(), schema))));
            }
         }
      }

      String schemaKey = VerboseSchemaRecord.keyOf(key.flavor(), key.checkId(), key.version());
      VerboseRegistryImpl.RecordLookup row = this.loadRecord(schemaKey);
      if (row.lookupFailed()) {
         return VerboseRegistryImpl.LayoutLookup.retryableMiss();
      } else if (row.isEmpty()) {
         return VerboseRegistryImpl.LayoutLookup.confirmed(Optional.empty());
      } else {
         try {
            return VerboseRegistryImpl.LayoutLookup.confirmed(Optional.of(VerboseSchema.decodeLayout(row.record().get().layout())));
         } catch (RuntimeException var5) {
            this.logger.log(Level.WARNING, "stored verbose schema layout is unreadable for " + schemaKey, (Throwable)var5);
            return VerboseRegistryImpl.LayoutLookup.confirmed(Optional.empty());
         }
      }
   }

   @NotNull
   private VerboseRegistryImpl.RecordLookup loadRecord(@NotNull String schemaKey) {
      if (this.store == null) {
         return VerboseRegistryImpl.RecordLookup.failure();
      } else {
         try {
            CompletionStage<Optional<VerboseSchemaRecord>> stage = this.store.execute(new EntityOps.GetByIdOp<>(this.category, schemaKey));
            return VerboseRegistryImpl.RecordLookup.confirmed(stage.toCompletableFuture().join());
         } catch (RuntimeException var3) {
            this.logger.log(Level.FINE, "verbose schema lookup failed for " + schemaKey, (Throwable)var3);
            return VerboseRegistryImpl.RecordLookup.failure();
         }
      }
   }

   private static boolean isBinaryVersion(int version) {
      return version >= 1;
   }

   private record FormatterKey(int flavor, int checkId, int version) {
   }

   private record LayoutKey(int flavor, int checkId, int version) {
   }

   private record LayoutLookup(@NotNull Optional<VerboseSchema.Layout> layout, boolean cacheable) {
      @NotNull
      private static VerboseRegistryImpl.LayoutLookup confirmed(@NotNull Optional<VerboseSchema.Layout> layout) {
         return new VerboseRegistryImpl.LayoutLookup(layout, true);
      }

      @NotNull
      private static VerboseRegistryImpl.LayoutLookup retryableMiss() {
         return new VerboseRegistryImpl.LayoutLookup(Optional.empty(), false);
      }
   }

   private record RecordLookup(@NotNull Optional<VerboseSchemaRecord> record, boolean lookupFailed) {
      @NotNull
      private static VerboseRegistryImpl.RecordLookup confirmed(@NotNull Optional<VerboseSchemaRecord> record) {
         return new VerboseRegistryImpl.RecordLookup(record, false);
      }

      @NotNull
      private static VerboseRegistryImpl.RecordLookup failure() {
         return new VerboseRegistryImpl.RecordLookup(Optional.empty(), true);
      }

      private boolean isPresent() {
         return this.record.isPresent();
      }

      private boolean isEmpty() {
         return this.record.isEmpty();
      }
   }
}
