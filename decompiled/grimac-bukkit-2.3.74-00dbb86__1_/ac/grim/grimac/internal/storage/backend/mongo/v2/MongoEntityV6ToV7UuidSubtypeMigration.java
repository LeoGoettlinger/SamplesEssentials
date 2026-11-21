package ac.grim.grimac.internal.storage.backend.mongo.v2;

import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.kind.Entity;
import ac.grim.grimac.api.storage.kind.IndexSpec;
import ac.grim.grimac.api.storage.registry.Migration;
import ac.grim.grimac.api.storage.registry.MigrationContext;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.internal.storage.codec.bson.BsonBinaries;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.RenameCollectionOptions;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.bson.BsonBinarySubType;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

@ApiStatus.Internal
public final class MongoEntityV6ToV7UuidSubtypeMigration implements Migration<Entity<?, ?, ?>> {
   private static final String STAGING_SUFFIX = "_v7_migrating";
   private static final String BACKUP_SUFFIX = "_v6_bak";

   @Override
   public int fromVersion() {
      return 0;
   }

   @Override
   public int toVersion() {
      return 1;
   }

   public void apply(@NotNull MigrationContext ctx, @NotNull StoreId id, @NotNull Entity<?, ?, ?> kind) throws Exception {
      if (ctx instanceof MongoMigrationContext mctx) {
         EncodeShape shape = kind.codec().shape();
         ArrayList uuidFields = new ArrayList();
         boolean idIsUuid = false;

         for (EncodeShape.FieldDef f : shape.fields()) {
            if (f.javaType() == UUID.class) {
               uuidFields.add(f.name());
               if (f.name().equals(shape.idField())) {
                  idIsUuid = true;
               }
            }
         }

         if (!uuidFields.isEmpty()) {
            this.run(mctx.database(), mctx.logger(), id, kind, uuidFields, idIsUuid, shape.idField());
         }
      } else {
         throw new IllegalArgumentException("MongoEntityV6ToV7UuidSubtypeMigration requires a MongoMigrationContext; got " + ctx.getClass().getName());
      }
   }

   private void run(
      @NotNull MongoDatabase db,
      @NotNull Logger logger,
      @NotNull StoreId id,
      @NotNull Entity<?, ?, ?> kind,
      @NotNull List<String> uuidFields,
      boolean idIsUuid,
      @NotNull String idField
   ) {
      String canonical = id.name();
      String staging = canonical + "_v7_migrating";
      String backup = canonical + "_v6_bak";
      if (!collectionExists(db, canonical) && collectionExists(db, backup)) {
         long backupCount = db.getCollection(backup).countDocuments();
         logger.warning(
            () -> "recovering: canonical missing, " + backup + " (" + backupCount + ") present — rebuilding staging from backup, completing second rename"
         );
         long copied = this.rebuildStaging(db, backup, staging, kind, uuidFields, idIsUuid, idField, logger);
         verifyRecoveryRebuild(canonical, backup, staging, copied, backupCount);
         dropIfEmptyOrThrow(db, canonical);
         db.getCollection(staging).renameCollection(new MongoNamespace(db.getName(), canonical), new RenameCollectionOptions());
      } else if (collectionExists(db, canonical) && db.getCollection(canonical).countDocuments() == 0L && collectionExists(db, backup)) {
         long backupCount = db.getCollection(backup).countDocuments();
         logger.warning(
            () -> "recovering: empty canonical, "
               + backup
               + " ("
               + backupCount
               + ") present (ensureStore likely ran between crash and rerun) — rebuilding staging from backup, completing second rename"
         );
         long copied = this.rebuildStaging(db, backup, staging, kind, uuidFields, idIsUuid, idField, logger);
         verifyRecoveryRebuild(canonical, backup, staging, copied, backupCount);
         dropIfEmptyOrThrow(db, canonical);
         db.getCollection(staging).renameCollection(new MongoNamespace(db.getName(), canonical), new RenameCollectionOptions());
      } else if (!collectionExists(db, canonical)) {
         logger.fine(() -> canonical + " does not exist; nothing to migrate");
      } else {
         MongoCollection<Document> src = db.getCollection(canonical);
         long sourceCount = src.countDocuments();
         if (sourceCount == 0L) {
            logger.fine(() -> canonical + " is empty; nothing to migrate");
         } else if (looksAlreadyMigrated(src, idIsUuid, idField)) {
            if (collectionExists(db, staging)) {
               long stagingCount = db.getCollection(staging).countDocuments();
               logger.warning(
                  () -> canonical
                     + " is v7-shaped but "
                     + staging
                     + " ("
                     + stagingCount
                     + ") still exists (stranded from prior successful migration); operator may drop it manually"
               );
            }

            logger.fine(() -> canonical + " already looks v7-shaped (subtype-4 _id + " + idField + " present); migration skipped");
         } else if (collectionExists(db, backup)) {
            long backupCount = db.getCollection(backup).countDocuments();
            throw new IllegalStateException(
               "refusing to overwrite existing "
                  + backup
                  + " (rows="
                  + backupCount
                  + ") while migrating v6 canonical "
                  + canonical
                  + " (rows="
                  + sourceCount
                  + "); both contain v6-shaped data and cannot be safely reconciled. Manual operator review required (decide which v6 source is authoritative, drop the other, then rerun migration)."
            );
         } else {
            if (collectionExists(db, staging)) {
               logger.warning(() -> "found leftover staging " + staging + " (prior crashed main-path attempt); will rebuild from canonical");
            }

            logger.info(() -> "migrating " + canonical + " v6 → v7 (UUID subtype + idField projection)");
            long copied = this.rebuildStaging(db, canonical, staging, kind, uuidFields, idIsUuid, idField, logger);
            long sourceCountFinal = src.countDocuments();
            if (sourceCountFinal != copied) {
               throw new IllegalStateException(
                  "source/staging divergence: source="
                     + sourceCountFinal
                     + " copied="
                     + copied
                     + " — re-run migration after stopping live writers (offline-only)"
               );
            } else {
               MongoNamespace canonicalNs = new MongoNamespace(db.getName(), canonical);
               MongoNamespace backupNs = new MongoNamespace(db.getName(), backup);
               db.getCollection(canonical).renameCollection(backupNs, new RenameCollectionOptions());
               db.getCollection(staging).renameCollection(canonicalNs, new RenameCollectionOptions());
               long copiedFinal = copied;
               logger.info(() -> "migration complete: " + copiedFinal + " row(s) migrated; v6 collection preserved at " + backup);
            }
         }
      }
   }

   private long rebuildStaging(
      @NotNull MongoDatabase db,
      @NotNull String sourceName,
      @NotNull String staging,
      @NotNull Entity<?, ?, ?> kind,
      @NotNull List<String> uuidFields,
      boolean idIsUuid,
      @NotNull String idField,
      @NotNull Logger logger
   ) {
      if (collectionExists(db, staging)) {
         logger.warning(() -> "found leftover staging " + staging + "; dropping");
         db.getCollection(staging).drop();
      }

      db.createCollection(staging);
      MongoCollection<Document> stagingColl = db.getCollection(staging);
      applySecondaryIndexes(stagingColl, kind, logger);
      MongoCollection<Document> source = db.getCollection(sourceName);
      long copied = 0L;
      List<Document> batch = new ArrayList<>(500);
      MongoCursor stagingCount = source.find().noCursorTimeout(true).batchSize(500).iterator();

      while (stagingCount.hasNext()) {
         Document d = (Document)stagingCount.next();
         batch.add(rewrite(d, uuidFields, idIsUuid, idField));
         if (batch.size() >= 500) {
            stagingColl.insertMany(batch);
            copied += batch.size();
            batch.clear();
         }
      }

      if (!batch.isEmpty()) {
         stagingColl.insertMany(batch);
         copied += batch.size();
      }

      long stagingCountx = stagingColl.countDocuments();
      if (stagingCountx != copied) {
         throw new IllegalStateException("staging post-copy count mismatch: copied=" + copied + " in_staging=" + stagingCountx);
      } else {
         return copied;
      }
   }

   private static void dropIfEmptyOrThrow(@NotNull MongoDatabase db, @NotNull String name) {
      if (collectionExists(db, name)) {
         long count = db.getCollection(name).countDocuments();
         if (count > 0L) {
            throw new IllegalStateException("refusing to drop " + name + ": it has " + count + " row(s) (concurrent writer?). Manual operator review required.");
         } else {
            db.getCollection(name).drop();
         }
      }
   }

   private static void verifyRecoveryRebuild(@NotNull String canonical, @NotNull String backup, @NotNull String staging, long copied, long backupCount) {
      if (copied != backupCount || backupCount > 0L && copied <= 0L) {
         throw new IllegalStateException(
            "post-rebuild verification failed for "
               + canonical
               + ": "
               + staging
               + " has "
               + copied
               + " docs but "
               + backup
               + " has "
               + backupCount
               + " — copy did not reproduce backup. Manual operator review required."
         );
      }
   }

   @NotNull
   private static Document rewrite(@NotNull Document d, @NotNull List<String> uuidFields, boolean idIsUuid, @NotNull String idField) {
      Document out = new Document(d);

      for (String fieldName : uuidFields) {
         String storedName = fieldName.equals(idField) ? "_id" : fieldName;
         if (out.get(storedName) instanceof Binary bin && bin.getType() == BsonBinarySubType.BINARY.getValue() && bin.getData().length == 16) {
            UUID u = bytesToUuid(bin.getData());
            out.put(storedName, BsonBinaries.uuidBinary(u));
         }
      }

      if (idIsUuid && out.get(idField) == null) {
         Object curId = out.get("_id");
         if (curId != null) {
            out.put(idField, curId);
         }
      }

      return out;
   }

   private static boolean looksAlreadyMigrated(@NotNull MongoCollection<Document> coll, boolean idIsUuid, @NotNull String idField) {
      Document first = (Document)coll.find().limit(1).first();
      if (first == null) {
         return false;
      } else {
         Object idVal = first.get("_id");
         if (idIsUuid) {
            if (idVal instanceof UUID) {
               return first.get(idField) != null;
            } else {
               return idVal instanceof Binary b && b.getType() == BsonBinarySubType.UUID_STANDARD.getValue() ? first.get(idField) != null : false;
            }
         } else {
            return first.get(idField) != null;
         }
      }
   }

   private static void applySecondaryIndexes(@NotNull MongoCollection<Document> coll, @NotNull Entity<?, ?, ?> kind, @NotNull Logger logger) {
      for (IndexSpec spec : kind.secondaryIndexes()) {
         tryCreateIndex(coll, buildIndex(spec), new IndexOptions().unique(spec.unique()).name(spec.name()), logger);
         if (spec.caseInsensitivePrefix()) {
            for (String f : spec.fields()) {
               String field = stripDir(f);
               tryCreateIndex(coll, Indexes.ascending(new String[]{field + "_lower"}), new IndexOptions().name(spec.name() + "_lower"), logger);
            }
         }
      }
   }

   private static void tryCreateIndex(@NotNull MongoCollection<Document> coll, @NotNull Bson keys, @NotNull IndexOptions opts, @NotNull Logger logger) {
      try {
         coll.createIndex(keys, opts);
      } catch (MongoCommandException var5) {
         if (var5.getErrorCode() == 85) {
            logger.fine(() -> "index " + opts.getName() + " on staging conflicts with existing equivalent; ignoring");
         } else {
            throw var5;
         }
      }
   }

   @NotNull
   private static Bson buildIndex(@NotNull IndexSpec spec) {
      if (spec.fields().size() == 1) {
         String f = spec.fields().get(0);
         return f.startsWith("-") ? Indexes.descending(new String[]{stripDir(f)}) : Indexes.ascending(new String[]{stripDir(f)});
      } else {
         List<Bson> parts = new ArrayList<>(spec.fields().size());

         for (String f : spec.fields()) {
            parts.add(f.startsWith("-") ? Indexes.descending(new String[]{stripDir(f)}) : Indexes.ascending(new String[]{stripDir(f)}));
         }

         return Indexes.compoundIndex(parts);
      }
   }

   @NotNull
   private static String stripDir(@NotNull String f) {
      return f.startsWith("-") ? f.substring(1) : f;
   }

   private static boolean collectionExists(@NotNull MongoDatabase db, @NotNull String name) {
      MongoCursor var2 = db.listCollectionNames().iterator();

      while (var2.hasNext()) {
         String existing = (String)var2.next();
         if (existing.equals(name)) {
            return true;
         }
      }

      return false;
   }

   private static UUID bytesToUuid(byte[] b) {
      ByteBuffer buf = ByteBuffer.wrap(b);
      return new UUID(buf.getLong(), buf.getLong());
   }
}
