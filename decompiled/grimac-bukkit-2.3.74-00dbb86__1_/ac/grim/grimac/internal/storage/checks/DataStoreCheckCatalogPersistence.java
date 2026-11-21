package ac.grim.grimac.internal.storage.checks;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.check.CheckCatalogPersistence;
import ac.grim.grimac.api.storage.check.CheckCatalogRow;
import ac.grim.grimac.api.storage.kind.ops.EntityOps;
import ac.grim.grimac.api.storage.model.CheckCatalogRecord;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApiStatus.Internal
public final class DataStoreCheckCatalogPersistence implements CheckCatalogPersistence {
   @NotNull
   private final DataStore store;
   @NotNull
   private final Logger logger;
   private final Map<String, CheckCatalogRow> byStableKey = new LinkedHashMap<>();
   private final Map<Integer, CheckCatalogRow> byId = new LinkedHashMap<>();
   private int nextCheckId = 1;

   public DataStoreCheckCatalogPersistence(@NotNull Iterable<CheckCatalogRow> initialRows, @NotNull DataStore store, @NotNull Logger logger) {
      this.store = store;
      this.logger = logger;

      for (CheckCatalogRow row : initialRows) {
         this.putLocal(row);
         this.nextCheckId = Math.max(this.nextCheckId, row.checkId() + 1);
      }
   }

   @Override
   public synchronized Iterable<CheckCatalogRow> loadAll() {
      return List.copyOf(this.byStableKey.values());
   }

   @Override
   public synchronized int insert(
      String stableKey, @Nullable String display, @Nullable String description, @Nullable String introducedVersion, long introducedAt
   ) {
      CheckCatalogRow existing = this.byStableKey.get(stableKey);
      if (existing != null) {
         return existing.checkId();
      } else {
         int checkId = this.nextAvailableId();
         CheckCatalogRow row = new CheckCatalogRow(checkId, stableKey, display, description, introducedVersion, introducedAt);
         this.putLocal(row);
         this.persist(row);
         return checkId;
      }
   }

   @Override
   public synchronized void upsert(CheckCatalogRow row) {
      CheckCatalogRow existingByKey = this.byStableKey.get(row.stableKey());
      if (existingByKey != null && existingByKey.checkId() != row.checkId()) {
         throw new IllegalStateException("stable key " + row.stableKey() + " already maps to check_id " + existingByKey.checkId());
      } else {
         CheckCatalogRow existingById = this.byId.get(row.checkId());
         if (existingById != null && !existingById.stableKey().equals(row.stableKey())) {
            throw new IllegalStateException("check_id " + row.checkId() + " already maps to stable key " + existingById.stableKey());
         } else {
            this.putLocal(row);
            this.persist(row);
         }
      }
   }

   @Override
   public synchronized void updateDisplayAndDescription(int checkId, @Nullable String display, @Nullable String description) {
      CheckCatalogRow existing = this.byId.get(checkId);
      if (existing == null) {
         this.logger.warning("[grim-datastore] attempted check-catalog metadata update for unknown check_id " + checkId);
      } else {
         CheckCatalogRow updated = new CheckCatalogRow(
            existing.checkId(), existing.stableKey(), display, description, existing.introducedVersion(), existing.introducedAt()
         );
         this.putLocal(updated);
         this.persist(updated);
      }
   }

   private int nextAvailableId() {
      while (this.byId.containsKey(this.nextCheckId)) {
         this.nextCheckId++;
      }

      return this.nextCheckId++;
   }

   private void putLocal(@NotNull CheckCatalogRow row) {
      this.byStableKey.put(row.stableKey(), row);
      this.byId.put(row.checkId(), row);
   }

   private void persist(@NotNull CheckCatalogRow row) {
      CheckCatalogRecord record = new CheckCatalogRecord(
         row.stableKey(), row.checkId(), row.display(), row.description(), row.introducedVersion(), row.introducedAt()
      );

      try {
         this.store.execute(new EntityOps.UpsertOp<>(Categories.CHECK_CATALOG, record)).toCompletableFuture().join();
      } catch (RuntimeException var4) {
         this.logger.log(Level.WARNING, "[grim-datastore] failed to persist check-catalog row for " + row.stableKey(), (Throwable)var4);
      }
   }
}
