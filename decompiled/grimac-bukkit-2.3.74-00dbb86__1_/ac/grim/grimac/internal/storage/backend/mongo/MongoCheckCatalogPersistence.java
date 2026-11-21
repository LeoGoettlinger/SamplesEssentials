package ac.grim.grimac.internal.storage.backend.mongo;

import ac.grim.grimac.api.storage.check.CheckCatalogPersistence;
import ac.grim.grimac.api.storage.check.CheckCatalogRow;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;

@ApiStatus.Internal
final class MongoCheckCatalogPersistence implements CheckCatalogPersistence {
   private final MongoCollection<Document> meta;
   private final MongoCollection<Document> checks;

   MongoCheckCatalogPersistence(MongoCollection<Document> meta, MongoCollection<Document> checks) {
      this.meta = meta;
      this.checks = checks;
   }

   @Override
   public Iterable<CheckCatalogRow> loadAll() {
      List<CheckCatalogRow> out = new ArrayList<>();
      MongoCursor var2 = this.checks.find().iterator();

      while (var2.hasNext()) {
         Document d = (Document)var2.next();
         CheckCatalogRow row = toRow(d);
         if (row != null) {
            out.add(row);
         }
      }

      return out;
   }

   @Override
   public int insert(String stableKey, @Nullable String display, @Nullable String description, @Nullable String introducedVersion, long introducedAt) {
      int id = this.nextCheckId();
      Document d = new Document("_id", stableKey)
         .append("check_id", id)
         .append("stable_key", stableKey)
         .append("display", display)
         .append("description", description)
         .append("introduced_version", introducedVersion)
         .append("introduced_at", introducedAt);

      try {
         this.checks.insertOne(d);
         return id;
      } catch (MongoWriteException var11) {
         Integer existing = this.findExistingId(stableKey);
         if (existing != null) {
            return existing;
         } else {
            throw var11;
         }
      }
   }

   @Override
   public void upsert(CheckCatalogRow row) {
      this.validateNoConflict(row);

      try {
         this.checks.replaceOne(Filters.eq("check_id", row.checkId()), toDocument(row), new ReplaceOptions().upsert(true));
      } catch (MongoWriteException var4) {
         this.validateNoConflict(row);
         if (var4.getError().getCategory() != ErrorCategory.DUPLICATE_KEY) {
            throw var4;
         }

         UpdateResult updated = this.checks.replaceOne(Filters.eq("check_id", row.checkId()), toDocument(row));
         if (updated.getMatchedCount() == 0L) {
            throw var4;
         }
      }

      this.meta.updateOne(Filters.eq("_id", 0), Updates.max("check_id_seq", row.checkId()), new UpdateOptions().upsert(true));
   }

   @Override
   public void updateDisplayAndDescription(int checkId, @Nullable String display, @Nullable String description) {
      this.checks
         .updateOne(Filters.eq("check_id", checkId), Updates.combine(new Bson[]{Updates.set("display", display), Updates.set("description", description)}));
   }

   private int nextCheckId() {
      Document d = (Document)this.meta
         .findOneAndUpdate(
            Filters.eq("_id", 0), Updates.inc("check_id_seq", 1), new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
         );
      Number n = d == null ? null : (Number)d.get("check_id_seq", Number.class);
      if (n == null) {
         throw new IllegalStateException("Mongo check_id_seq was not returned");
      } else {
         return n.intValue();
      }
   }

   static void alignCounterWithExistingRows(MongoCollection<Document> meta, MongoCollection<Document> checks) {
      Document d = (Document)checks.find().sort(Sorts.descending(new String[]{"check_id"})).limit(1).first();
      if (d != null) {
         Number n = (Number)d.get("check_id", Number.class);
         if (n != null && n.intValue() > 0) {
            meta.updateOne(Filters.eq("_id", 0), Updates.max("check_id_seq", n.intValue()), new UpdateOptions().upsert(true));
         }
      }
   }

   private void validateNoConflict(CheckCatalogRow row) {
      Document byId = (Document)this.checks.find(Filters.eq("check_id", row.checkId())).first();
      if (byId != null) {
         String stableKey = byId.getString("stable_key");
         if (!row.stableKey().equals(stableKey)) {
            throw new IllegalStateException("check_id " + row.checkId() + " already maps to stable key " + stableKey + ", cannot import " + row.stableKey());
         }
      }

      Document byStableKey = (Document)this.checks
         .find(Filters.or(new Bson[]{Filters.eq("stable_key", row.stableKey()), Filters.eq("_id", row.stableKey())}))
         .first();
      if (byStableKey != null) {
         Number id = (Number)byStableKey.get("check_id", Number.class);
         if (id == null || id.intValue() != row.checkId()) {
            throw new IllegalStateException(
               "stable key " + row.stableKey() + " already maps to check_id " + (id == null ? "null" : id.intValue()) + ", cannot import as " + row.checkId()
            );
         }
      }
   }

   private Integer findExistingId(String stableKey) {
      Document existing = (Document)this.checks.find(Filters.or(new Bson[]{Filters.eq("stable_key", stableKey), Filters.eq("_id", stableKey)})).first();
      if (existing == null) {
         return null;
      } else {
         Number n = (Number)existing.get("check_id", Number.class);
         return n == null ? null : n.intValue();
      }
   }

   private static Document toDocument(CheckCatalogRow row) {
      return new Document("_id", row.stableKey())
         .append("check_id", row.checkId())
         .append("stable_key", row.stableKey())
         .append("display", row.display())
         .append("description", row.description())
         .append("introduced_version", row.introducedVersion())
         .append("introduced_at", row.introducedAt());
   }

   @Nullable
   private static CheckCatalogRow toRow(Document d) {
      Number id = (Number)d.get("check_id", Number.class);
      String stableKey = d.getString("stable_key");
      if (id != null && stableKey != null) {
         Number introducedAt = (Number)d.get("introduced_at", Number.class);
         return new CheckCatalogRow(
            id.intValue(),
            stableKey,
            d.getString("display"),
            d.getString("description"),
            d.getString("introduced_version"),
            introducedAt == null ? 0L : introducedAt.longValue()
         );
      } else {
         return null;
      }
   }
}
