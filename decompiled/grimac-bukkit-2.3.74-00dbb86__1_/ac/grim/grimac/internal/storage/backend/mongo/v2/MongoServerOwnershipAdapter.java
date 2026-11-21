package ac.grim.grimac.internal.storage.backend.mongo.v2;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.instance.OwnershipClaimResult;
import ac.grim.grimac.api.storage.instance.OwnershipRenewResult;
import ac.grim.grimac.api.storage.instance.ServerOwnershipAdapter;
import ac.grim.grimac.api.storage.instance.ServerOwnershipMetadata;
import ac.grim.grimac.api.storage.instance.ServerOwnershipSnapshot;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.internal.storage.codec.bson.BsonBinaries;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

@ApiStatus.Internal
public final class MongoServerOwnershipAdapter implements ServerOwnershipAdapter {
   private static final int NAMESPACE_EXISTS = 48;
   private static final int DUPLICATE_KEY = 11000;
   @NotNull
   private final MongoDatabase db;
   @NotNull
   private final Logger logger;

   public MongoServerOwnershipAdapter(@NotNull MongoDatabase db, @NotNull Logger logger) {
      this.db = db;
      this.logger = logger;
   }

   @Override
   public void ensureStore(@NotNull StoreId id) throws BackendException {
      try {
         try {
            this.db.createCollection(id.name());
         } catch (MongoCommandException var3) {
            if (var3.getErrorCode() != 48) {
               throw var3;
            }
         }

         this.collection(id).createIndex(new Document("lease_expires_at_ms", 1), new IndexOptions().name("by_lease_expires"));
      } catch (RuntimeException var4) {
         throw new BackendException("failed to ensure server ownership collection " + id.name(), var4);
      }
   }

   @Override
   public long dbNowEpochMs() throws BackendException {
      try {
         Document hello = this.db.runCommand(new Document("hello", 1));
         Date localTime = hello.getDate("localTime");
         if (localTime != null) {
            return localTime.getTime();
         } else {
            this.logger.fine("[grim-datastore] Mongo hello.localTime missing; falling back to JVM time");
            return System.currentTimeMillis();
         }
      } catch (RuntimeException var3) {
         throw new BackendException("failed to read Mongo DB time", var3);
      }
   }

   @NotNull
   @Override
   public OwnershipClaimResult claimOwnership(
      @NotNull StoreId id, @NotNull UUID persistentId, @NotNull UUID startupId, @NotNull UUID fence, long ttlMs, @NotNull ServerOwnershipMetadata metadata
   ) throws BackendException {
      for (int attempt = 0; attempt < 3; attempt++) {
         try {
            long now = this.dbNowEpochMs();
            ServerOwnershipSnapshot previous = this.readOwnership(id, persistentId).orElse(null);
            Bson claimableFilter = Filters.and(
               new Bson[]{
                  Filters.eq("_id", BsonBinaries.uuidBinary(persistentId)),
                  Filters.or(
                     new Bson[]{
                        Filters.ne("closed_at_ms", 0L),
                        Filters.lte("lease_expires_at_ms", now),
                        Filters.and(
                           new Bson[]{Filters.eq("owner_startup_id", BsonBinaries.uuidBinary(startupId)), Filters.eq("fence", BsonBinaries.uuidBinary(fence))}
                        )
                     }
                  )
               }
            );
            Bson update = Updates.combine(
               new Bson[]{
                  Updates.set("owner_startup_id", BsonBinaries.uuidBinary(startupId)),
                  Updates.set("fence", BsonBinaries.uuidBinary(fence)),
                  Updates.set("lease_expires_at_ms", now + ttlMs),
                  Updates.set("last_renewed_at_ms", now),
                  Updates.set("closed_at_ms", 0L),
                  Updates.unset("close_reason"),
                  Updates.set("server_name", metadata.serverName()),
                  Updates.set("hostname", metadata.hostname()),
                  Updates.set("grim_version", metadata.grimVersion()),
                  Updates.set("server_version", metadata.serverVersionString())
               }
            );
            Document after = (Document)this.collection(id)
               .findOneAndUpdate(claimableFilter, update, new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
            if (after != null) {
               return OwnershipClaimResult.claimed(persistentId, startupId, fence, now, now + ttlMs, previous);
            }

            if (previous == null) {
               try {
                  this.collection(id).insertOne(ownershipDocument(persistentId, startupId, fence, now, now + ttlMs, metadata));
                  return OwnershipClaimResult.claimed(persistentId, startupId, fence, now, now + ttlMs, null);
               } catch (MongoWriteException var16) {
                  if (var16.getError() == null || var16.getError().getCode() != 11000) {
                     throw var16;
                  }
               }
            }

            ServerOwnershipSnapshot current = this.readOwnership(id, persistentId).orElse(null);
            if (current != null) {
               return OwnershipClaimResult.denied(persistentId, startupId, fence, now, current);
            }
         } catch (RuntimeException var17) {
            if (attempt == 2) {
               throw new BackendException("failed to claim Mongo server ownership", var17);
            }
         }
      }

      throw new BackendException("failed to claim Mongo server ownership after retries", null);
   }

   @NotNull
   @Override
   public OwnershipRenewResult renewOwnership(@NotNull StoreId id, @NotNull UUID persistentId, @NotNull UUID startupId, @NotNull UUID fence, long ttlMs) throws BackendException {
      try {
         long now = this.dbNowEpochMs();
         Bson filter = Filters.and(
            new Bson[]{
               Filters.eq("_id", BsonBinaries.uuidBinary(persistentId)),
               Filters.eq("owner_startup_id", BsonBinaries.uuidBinary(startupId)),
               Filters.eq("fence", BsonBinaries.uuidBinary(fence)),
               Filters.eq("closed_at_ms", 0L),
               Filters.gt("lease_expires_at_ms", now)
            }
         );
         Bson update = Updates.combine(
            new Bson[]{
               Updates.set("lease_expires_at_ms", now + ttlMs),
               Updates.set("last_renewed_at_ms", now),
               Updates.set("closed_at_ms", 0L),
               Updates.unset("close_reason")
            }
         );
         long matched = this.collection(id).updateOne(filter, update).getMatchedCount();
         return matched == 1L
            ? OwnershipRenewResult.renewed(persistentId, startupId, fence, now, now + ttlMs)
            : OwnershipRenewResult.lost(persistentId, startupId, fence, now);
      } catch (RuntimeException var13) {
         throw new BackendException("failed to renew Mongo server ownership", var13);
      }
   }

   @Override
   public boolean closeOwnership(@NotNull StoreId id, @NotNull UUID persistentId, @NotNull UUID startupId, @NotNull UUID fence, @NotNull String reason) throws BackendException {
      try {
         long now = this.dbNowEpochMs();
         Bson filter = Filters.and(
            new Bson[]{
               Filters.eq("_id", BsonBinaries.uuidBinary(persistentId)),
               Filters.eq("owner_startup_id", BsonBinaries.uuidBinary(startupId)),
               Filters.eq("fence", BsonBinaries.uuidBinary(fence)),
               Filters.eq("closed_at_ms", 0L)
            }
         );
         Bson update = Updates.combine(new Bson[]{Updates.set("closed_at_ms", now), Updates.set("close_reason", reason)});
         return this.collection(id).updateOne(filter, update).getMatchedCount() == 1L;
      } catch (RuntimeException var10) {
         throw new BackendException("failed to close Mongo server ownership", var10);
      }
   }

   @NotNull
   @Override
   public Optional<ServerOwnershipSnapshot> readOwnership(@NotNull StoreId id, @NotNull UUID persistentId) throws BackendException {
      try {
         Document doc = (Document)this.collection(id).find(Filters.eq("_id", BsonBinaries.uuidBinary(persistentId))).first();
         return Optional.ofNullable(doc == null ? null : snapshot(doc));
      } catch (RuntimeException var4) {
         throw new BackendException("failed to read Mongo server ownership", var4);
      }
   }

   @NotNull
   private MongoCollection<Document> collection(@NotNull StoreId id) {
      return this.db.getCollection(id.name());
   }

   @NotNull
   private static Document ownershipDocument(
      @NotNull UUID persistentId, @NotNull UUID startupId, @NotNull UUID fence, long now, long leaseExpiresAt, @NotNull ServerOwnershipMetadata metadata
   ) {
      return new Document("_id", BsonBinaries.uuidBinary(persistentId))
         .append("owner_startup_id", BsonBinaries.uuidBinary(startupId))
         .append("fence", BsonBinaries.uuidBinary(fence))
         .append("lease_expires_at_ms", leaseExpiresAt)
         .append("last_renewed_at_ms", now)
         .append("closed_at_ms", 0L)
         .append("server_name", metadata.serverName())
         .append("hostname", metadata.hostname())
         .append("grim_version", metadata.grimVersion())
         .append("server_version", metadata.serverVersionString());
   }

   @NotNull
   private static ServerOwnershipSnapshot snapshot(@NotNull Document doc) {
      UUID persistentId = BsonBinaries.toUuid(doc.get("_id"));
      UUID ownerStartupId = BsonBinaries.toUuid(doc.get("owner_startup_id"));
      UUID fence = BsonBinaries.toUuid(doc.get("fence"));
      if (persistentId != null && ownerStartupId != null && fence != null) {
         return new ServerOwnershipSnapshot(
            persistentId,
            ownerStartupId,
            fence,
            longValue(doc, "lease_expires_at_ms"),
            longValue(doc, "last_renewed_at_ms"),
            longValue(doc, "closed_at_ms"),
            doc.getString("close_reason"),
            doc.getString("server_name"),
            doc.getString("hostname"),
            doc.getString("grim_version"),
            doc.getString("server_version")
         );
      } else {
         throw new IllegalStateException("server ownership document has missing UUID fields");
      }
   }

   private static long longValue(@NotNull Document doc, @NotNull String field) {
      Number n = (Number)doc.get(field, Number.class);
      return n == null ? 0L : n.longValue();
   }
}
