package ac.grim.grimac.internal.storage.backend.mongo.v2;

import ac.grim.grimac.api.storage.kind.KeyValueScoped;
import ac.grim.grimac.api.storage.registry.Migration;
import ac.grim.grimac.api.storage.registry.MigrationContext;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.RenameCollectionOptions;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.bson.Document;

@ApiStatus.Internal
public final class MongoSettingsV6ToV7Migration implements Migration<KeyValueScoped<?, ?>> {
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

   public void apply(@NotNull MigrationContext ctx, @NotNull StoreId id, @NotNull KeyValueScoped<?, ?> kind) throws Exception {
      if (ctx instanceof MongoMigrationContext mctx) {
         this.run(mctx.database(), mctx.logger(), id);
      } else {
         throw new IllegalArgumentException("MongoSettingsV6ToV7Migration requires a MongoMigrationContext; got " + ctx.getClass().getName());
      }
   }

   private void run(@NotNull MongoDatabase db, @NotNull Logger logger, @NotNull StoreId id) throws Exception {
      String canonical = id.name();
      String staging = canonical + "_v7_migrating";
      String backup = canonical + "_v6_bak";
      if (!collectionExists(db, canonical) && collectionExists(db, backup)) {
         long backupCount = db.getCollection(backup).countDocuments();
         logger.warning(
            () -> "recovering: canonical missing, " + backup + " (rows=" + backupCount + ") present — rebuilding staging from backup, completing second rename"
         );
         if (collectionExists(db, staging)) {
            db.getCollection(staging).drop();
         }

         aggregateInto(db, backup, staging);
         verifyStagingMatchesBackup(db, canonical, staging, backup, logger);
         dropIfEmptyOrThrow(db, canonical);
         db.getCollection(staging).renameCollection(new MongoNamespace(db.getName(), canonical), new RenameCollectionOptions());
      } else if (collectionExists(db, canonical) && db.getCollection(canonical).countDocuments() == 0L && collectionExists(db, backup)) {
         long backupCount = db.getCollection(backup).countDocuments();
         logger.warning(
            () -> "recovering: empty canonical, "
               + backup
               + " (rows="
               + backupCount
               + ") present (ensureStore likely ran between crash and rerun) — rebuilding staging from backup, completing second rename"
         );
         if (collectionExists(db, staging)) {
            db.getCollection(staging).drop();
         }

         aggregateInto(db, backup, staging);
         verifyStagingMatchesBackup(db, canonical, staging, backup, logger);
         dropIfEmptyOrThrow(db, canonical);
         db.getCollection(staging).renameCollection(new MongoNamespace(db.getName(), canonical), new RenameCollectionOptions());
      } else if (!collectionExists(db, canonical)) {
         logger.fine(() -> canonical + " does not exist; nothing to migrate");
      } else if (isAlreadyV7Shaped(db, canonical)) {
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

         logger.fine(() -> canonical + " is already v7-shaped (envelope docs); migration skipped");
      } else if (collectionExists(db, backup)) {
         long backupCount = db.getCollection(backup).countDocuments();
         long canonicalCount = db.getCollection(canonical).countDocuments();
         throw new IllegalStateException(
            "refusing to overwrite existing "
               + backup
               + " (rows="
               + backupCount
               + ") while migrating v6 canonical "
               + canonical
               + " (rows="
               + canonicalCount
               + "); both contain v6-shaped data and cannot be safely reconciled. Manual operator review required (decide which v6 source is authoritative, drop the other, then rerun migration)."
         );
      } else {
         if (collectionExists(db, staging)) {
            logger.warning(() -> "found leftover staging " + staging + " (prior crashed main-path attempt); dropping");
            db.getCollection(staging).drop();
         }

         logger.info(() -> "migrating " + canonical + " from v6 -> v7 (per-tenant envelopes)");
         MongoCollection<Document> src = db.getCollection(canonical);
         long sourceCount = src.countDocuments();
         aggregateInto(db, canonical, staging);
         long sourceTenants = countDistinctTenants(src);
         long stagingCount = db.getCollection(staging).countDocuments();
         if (stagingCount != sourceTenants) {
            throw new IllegalStateException(
               "tenant count mismatch: source distinct=" + sourceTenants + " staging docs=" + stagingCount + " (source rows=" + sourceCount + ")"
            );
         } else {
            MongoNamespace canonicalNs = new MongoNamespace(db.getName(), canonical);
            MongoNamespace backupNs = new MongoNamespace(db.getName(), backup);
            db.getCollection(canonical).renameCollection(backupNs, new RenameCollectionOptions());
            db.getCollection(staging).renameCollection(canonicalNs, new RenameCollectionOptions());
            logger.info(
               () -> "settings migration complete: "
                  + sourceCount
                  + " v6 rows -> "
                  + stagingCount
                  + " v7 tenant envelopes; v6 collection preserved at "
                  + backup
            );
         }
      }
   }

   private static void aggregateInto(@NotNull MongoDatabase db, @NotNull String sourceName, @NotNull String target) {
      List<Document> pipeline = Arrays.asList(
         new Document(
            "$group",
            new Document()
               .append("_id", new Document().append("scope", "$scope").append("scope_key", "$scope_key"))
               .append("pairs", new Document("$push", new Document().append("k", "$key").append("v", "$value")))
               .append("updated_at", new Document("$max", "$updated_at"))
         ),
         new Document("$project", new Document().append("values", new Document("$arrayToObject", "$pairs")).append("updated_at", 1)),
         new Document("$out", target)
      );

      try {
         db.getCollection(sourceName).aggregate(pipeline).toCollection();
      } catch (RuntimeException var7) {
         try {
            db.getCollection(target).drop();
         } catch (RuntimeException var6) {
         }

         throw var7;
      }
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

   private static boolean isAlreadyV7Shaped(@NotNull MongoDatabase db, @NotNull String name) {
      Document sample = (Document)db.getCollection(name).find().limit(1).first();
      return sample == null ? false : sample.containsKey("values") && !sample.containsKey("key");
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

   private static void verifyStagingMatchesBackup(
      @NotNull MongoDatabase db, @NotNull String canonical, @NotNull String staging, @NotNull String backup, @NotNull Logger logger
   ) {
      long stagingCount = db.getCollection(staging).countDocuments();
      long backupCount = db.getCollection(backup).countDocuments();
      long backupTenants = countDistinctTenants(db.getCollection(backup));
      if (stagingCount == backupTenants && (backupCount <= 0L || stagingCount > 0L)) {
         logger.fine(() -> "post-rebuild verified: " + staging + "=" + stagingCount + " tenants matches " + backup + "_tenants=" + backupTenants);
      } else {
         throw new IllegalStateException(
            "post-rebuild verification failed for "
               + canonical
               + ": "
               + staging
               + " has "
               + stagingCount
               + " tenant envelopes but "
               + backup
               + " has "
               + backupCount
               + " rows / "
               + backupTenants
               + " distinct tenants; aggregation did not produce expected result. Manual operator review required."
         );
      }
   }

   private static long countDistinctTenants(@NotNull MongoCollection<Document> src) {
      List<Document> pipeline = Arrays.asList(
         new Document("$group", new Document("_id", new Document().append("scope", "$scope").append("scope_key", "$scope_key"))), new Document("$count", "n")
      );
      Document r = (Document)src.aggregate(pipeline).first();
      return r == null ? 0L : ((Number)r.get("n")).longValue();
   }
}
