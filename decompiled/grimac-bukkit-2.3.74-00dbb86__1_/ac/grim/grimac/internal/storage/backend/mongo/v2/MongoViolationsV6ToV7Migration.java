package ac.grim.grimac.internal.storage.backend.mongo.v2;

import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.kind.EventStream;
import ac.grim.grimac.api.storage.registry.Migration;
import ac.grim.grimac.api.storage.registry.MigrationContext;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.internal.storage.codec.CapturedBindings;
import ac.grim.grimac.internal.storage.codec.CodecIntrospection;
import ac.grim.grimac.internal.storage.codec.RecordLayout;
import ac.grim.grimac.internal.storage.codec.bson.BsonBinaries;
import ac.grim.grimac.internal.storage.codec.bson.BsonCodecs;
import ac.grim.grimac.internal.storage.codec.bson.BsonTsCodec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.RenameCollectionOptions;
import com.mongodb.client.model.TimeSeriesGranularity;
import com.mongodb.client.model.TimeSeriesOptions;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.Document;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;
import org.bson.types.Binary;

@ApiStatus.Internal
public final class MongoViolationsV6ToV7Migration implements Migration<EventStream<?, ?>> {
   private static final int BATCH = 5000;
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

   public void apply(@NotNull MigrationContext ctx, @NotNull StoreId id, @NotNull EventStream<?, ?> kind) throws Exception {
      if (ctx instanceof MongoMigrationContext mctx) {
         this.run(mctx.database(), mctx.logger(), id, kind);
      } else {
         throw new IllegalArgumentException("MongoViolationsV6ToV7Migration requires a MongoMigrationContext; got " + ctx.getClass().getName());
      }
   }

   private void run(@NotNull MongoDatabase db, @NotNull Logger logger, @NotNull StoreId id, @NotNull EventStream<?, ?> kind) throws Exception {
      String canonical = id.name();
      String backup = canonical + "_v6_bak";
      if (isTimeseriesCollection(db, canonical)) {
         long tsCount = db.getCollection(canonical).countDocuments();
         boolean hasBackup = collectionExists(db, backup);
         long bakCount = hasBackup ? db.getCollection(backup).countDocuments() : -1L;
         if (hasBackup && tsCount == bakCount && bakCount > 0L) {
            logger.fine(() -> canonical + " already migrated (" + bakCount + " rows match between canonical timeseries and " + backup + "); ensuring TTL");
            this.applyTtl(db, canonical, kind);
         } else if (!hasBackup || bakCount <= 0L) {
            logger.fine(() -> canonical + " is already a timeseries collection (no backup); migration skipped");
         } else if (tsCount > bakCount) {
            logger.warning(
               () -> canonical
                  + " is timeseries with "
                  + tsCount
                  + " rows while "
                  + backup
                  + " has "
                  + bakCount
                  + " — treating as already migrated with post-migration writes; preserving backup and ensuring TTL"
            );
            this.applyTtl(db, canonical, kind);
         } else {
            logger.warning(
               () -> canonical
                  + " is timeseries with "
                  + tsCount
                  + " rows but "
                  + backup
                  + " has "
                  + bakCount
                  + " — partial copy detected; re-copying from backup"
            );
            db.getCollection(canonical).drop();
            this.createTimeseries(db, canonical, kind);
            this.streamCopy(db, backup, canonical, kind, logger);
            this.applyTtl(db, canonical, kind);
         }
      } else if (!collectionExists(db, canonical) && collectionExists(db, backup)) {
         long bakCount = db.getCollection(backup).countDocuments();
         logger.warning(
            () -> "recovering: canonical missing, " + backup + " has " + bakCount + " rows. Recreating " + canonical + " as timeseries and copying."
         );
         this.createTimeseries(db, canonical, kind);
         this.streamCopy(db, backup, canonical, kind, logger);
         this.applyTtl(db, canonical, kind);
      } else if (!collectionExists(db, canonical)) {
         logger.fine(() -> canonical + " does not exist; nothing to migrate");
      } else {
         logger.info(() -> "migrating " + canonical + " from v6 → v7 (timeseries)");
         if (collectionExists(db, backup)) {
            long backupCount = db.getCollection(backup).countDocuments();
            long canonicalCount = db.getCollection(canonical).countDocuments();
            throw new IllegalStateException(
               "refusing to overwrite existing "
                  + backup
                  + " (rows="
                  + backupCount
                  + ") while migrating non-empty canonical "
                  + canonical
                  + " (rows="
                  + canonicalCount
                  + "); both contain v6-shaped data and cannot be safely reconciled. Manual operator review required (decide which v6 source is authoritative, drop the other, then rerun migration)."
            );
         } else {
            db.getCollection(canonical).renameCollection(new MongoNamespace(db.getName(), backup), new RenameCollectionOptions());
            this.createTimeseries(db, canonical, kind);
            this.streamCopy(db, backup, canonical, kind, logger);
            this.applyTtl(db, canonical, kind);
            logger.info(
               () -> "migration complete: "
                  + db.getCollection(canonical).countDocuments()
                  + " docs in timeseries "
                  + canonical
                  + "; v6 collection preserved at "
                  + backup
            );
         }
      }
   }

   private void createTimeseries(@NotNull MongoDatabase db, @NotNull String name, @NotNull EventStream<?, ?> kind) {
      TimeSeriesOptions tsOpts = new TimeSeriesOptions(kind.timestampField()).metaField("meta").granularity(toMongoGranularity(kind));
      db.createCollection(name, new CreateCollectionOptions().timeSeriesOptions(tsOpts));
   }

   private void streamCopy(@NotNull MongoDatabase db, @NotNull String fromName, @NotNull String toName, @NotNull EventStream<?, ?> kind, @NotNull Logger logger) throws Exception {
      MongoCollection<Document> src = db.getCollection(fromName);
      MongoCollection<RawBsonDocument> dst = db.getCollection(toName, RawBsonDocument.class);
      BsonTsCodec codec = BsonCodecs.timeseries(kind.recordType());
      long total = src.countDocuments();
      long copied = 0L;
      BasicOutputBuffer buf = new BasicOutputBuffer(256);
      List<RawBsonDocument> pending = new ArrayList<>(5000);
      MongoCursor destCount = src.find().noCursorTimeout(true).batchSize(5000).iterator();

      while (destCount.hasNext()) {
         Document v6 = (Document)destCount.next();
         Object record = decodeV6Doc(v6, kind);
         buf.truncateToPosition(0);
         BsonBinaryWriter writer = new BsonBinaryWriter(buf);

         try {
            codec.encode(record, writer);
         } finally {
            writer.close();
         }

         pending.add(new RawBsonDocument(buf.toByteArray()));
         if (pending.size() >= 5000) {
            dst.insertMany(pending);
            copied += pending.size();
            pending.clear();
            long pct = total > 0L ? copied * 100L / total : 0L;
            logger.fine(() -> "migrated " + pct + "%");
         }
      }

      if (!pending.isEmpty()) {
         dst.insertMany(pending);
         copied += pending.size();
      }

      long destCountx = dst.countDocuments();
      if (destCountx != copied) {
         throw new IllegalStateException("post-copy count mismatch: copied=" + copied + " in_dest=" + destCountx);
      } else {
         long sourceCountFinal = src.countDocuments();
         if (sourceCountFinal != copied) {
            throw new IllegalStateException(
               "source/destination divergence: source="
                  + sourceCountFinal
                  + " copied="
                  + copied
                  + " — re-run migration after stopping live writes, or enable online mode (Phase 3+)."
            );
         }
      }
   }

   private void applyTtl(@NotNull MongoDatabase db, @NotNull String name, @NotNull EventStream<?, ?> kind) {
      if (kind.retention() != null) {
         db.runCommand(new Document().append("collMod", name).append("expireAfterSeconds", kind.retention().toSeconds()));
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

   private static boolean isTimeseriesCollection(@NotNull MongoDatabase db, @NotNull String name) {
      try {
         MongoCursor var2 = db.listCollections().filter(new Document("name", name)).iterator();

         while (var2.hasNext()) {
            Document collInfo = (Document)var2.next();
            Document options = (Document)collInfo.get("options", Document.class);
            if (options != null && options.containsKey("timeseries")) {
               return true;
            }

            String type = collInfo.getString("type");
            if ("timeseries".equals(type)) {
               return true;
            }
         }
      } catch (RuntimeException var6) {
      }

      return false;
   }

   @NotNull
   private static TimeSeriesGranularity toMongoGranularity(@NotNull EventStream<?, ?> kind) {
      return switch (kind.granularity()) {
         case SECONDS -> TimeSeriesGranularity.SECONDS;
         case MINUTES -> TimeSeriesGranularity.MINUTES;
         case HOURS -> TimeSeriesGranularity.HOURS;
      };
   }

   @NotNull
   private static Object decodeV6Doc(@NotNull Document v6, @NotNull EventStream<?, ?> kind) {
      try {
         Class<?> rec = kind.recordType();
         EncodeShape shape = CodecIntrospection.inspect(rec);
         RecordLayout layout = CapturedBindings.layout(rec);
         if (layout == null) {
            layout = RecordLayout.fromReflection(rec, shape);
         }

         Class<?>[] paramTypes = layout.ctorParamTypes();
         Object[] args = new Object[paramTypes.length];

         for (EncodeShape.FieldDef f : shape.fields()) {
            int pos = f.recordIndex();
            String encoded = f.name();
            args[pos] = coerceV6Value(v6, v6.get(encoded), paramTypes[pos], encoded);
         }

         MethodHandle ctor = MethodHandles.privateLookupIn(rec, MethodHandles.lookup()).findConstructor(rec, MethodType.methodType(void.class, paramTypes));
         return ctor.invokeWithArguments(args);
      } catch (Throwable var11) {
         throw new IllegalStateException("v6 decode failed: " + v6, var11);
      }
   }

   private static Object coerceV6Value(@NotNull Document v6, Object raw, @NotNull Class<?> javaType, @NotNull String encoded) {
      if (raw == null) {
         if (javaType.isPrimitive()) {
            if (javaType == int.class) {
               return 0;
            } else if (javaType == long.class) {
               return 0L;
            } else if (javaType == double.class) {
               return 0.0;
            } else if (javaType == float.class) {
               return 0.0F;
            } else if (javaType == boolean.class) {
               return false;
            } else {
               throw new IllegalStateException("unsupported primitive " + javaType + " in v6 field " + encoded);
            }
         } else {
            return null;
         }
      } else if (javaType == int.class || javaType == Integer.class) {
         return ((Number)raw).intValue();
      } else if (javaType != long.class && javaType != Long.class) {
         if (javaType == double.class || javaType == Double.class) {
            return ((Number)raw).doubleValue();
         } else if (javaType == float.class || javaType == Float.class) {
            return ((Number)raw).floatValue();
         } else if (javaType == boolean.class || javaType == Boolean.class) {
            return raw;
         } else if (javaType == String.class) {
            return raw.toString();
         } else if (javaType == UUID.class) {
            return BsonBinaries.toUuid(raw);
         } else if (javaType == byte[].class) {
            if (raw instanceof byte[] b) {
               return b;
            } else if (raw instanceof Binary b) {
               return b.getData();
            } else if (raw instanceof BsonBinary b) {
               return b.getData();
            } else if ("verbose".equals(encoded) && raw instanceof String s) {
               int format = v6.get("verbose_format") instanceof Number n ? n.intValue() : 0;
               return format == 1 ? Base64.getDecoder().decode(s) : s.getBytes(StandardCharsets.UTF_8);
            } else {
               throw new IllegalStateException("v6 " + encoded + ": unexpected binary type " + raw.getClass().getName());
            }
         } else if (javaType.isEnum()) {
            int ordinal = ((Number)raw).intValue();
            Object[] values = javaType.getEnumConstants();
            if (ordinal >= 0 && ordinal < values.length) {
               return values[ordinal];
            } else {
               throw new IllegalStateException("v6 " + encoded + ": enum ordinal " + ordinal + " out of range");
            }
         } else {
            throw new IllegalStateException("v6 " + encoded + ": unsupported java type " + javaType.getName());
         }
      } else {
         return raw instanceof Date d ? d.getTime() : ((Number)raw).longValue();
      }
   }
}
