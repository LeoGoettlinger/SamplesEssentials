package ac.grim.grimac.internal.storage.backend.mongo;

import ac.grim.grimac.api.storage.backend.ApiVersion;
import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendContext;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.check.CheckCatalogPersistence;
import ac.grim.grimac.api.storage.check.CheckCatalogRepairResult;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.api.storage.event.PlayerIdentityEvent;
import ac.grim.grimac.api.storage.event.SessionEvent;
import ac.grim.grimac.api.storage.event.SettingEvent;
import ac.grim.grimac.api.storage.event.ViolationEvent;
import ac.grim.grimac.api.storage.model.PlayerIdentity;
import ac.grim.grimac.api.storage.model.SessionRecord;
import ac.grim.grimac.api.storage.model.SettingRecord;
import ac.grim.grimac.api.storage.model.SettingScope;
import ac.grim.grimac.api.storage.model.VerboseFormat;
import ac.grim.grimac.api.storage.model.ViolationRecord;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.DeleteCriteria;
import ac.grim.grimac.api.storage.query.Deletes;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Queries;
import ac.grim.grimac.api.storage.query.Query;
import ac.grim.grimac.internal.storage.codec.bson.BsonBinaries;
import ac.grim.grimac.internal.storage.util.UuidCodec;
import ac.grim.grimac.internal.storage.util.UuidV7;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.UpdateResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bson.BsonBinary;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

@ApiStatus.Internal
public final class MongoBackend implements Backend {
   public static final String ID = "mongo";
   static final int CURRENT_SCHEMA_VERSION = 6;
   private final MongoBackendConfig config;
   private final List<MongoBackend.BatchingHandler<?>> handlers = new ArrayList<>();
   private MongoClient client;
   private MongoDatabase db;
   private MongoCollection<Document> meta;
   private MongoCollection<Document> checks;
   private MongoCollection<Document> players;
   private MongoCollection<Document> sessions;
   private MongoCollection<Document> violations;
   private MongoCollection<Document> settings;
   private Logger logger;

   public MongoBackend(MongoBackendConfig config) {
      this.config = config;
   }

   @NotNull
   @Override
   public String id() {
      return "mongo";
   }

   @NotNull
   @Override
   public ApiVersion getApiVersion() {
      return ApiVersion.CURRENT;
   }

   @NotNull
   @Override
   public EnumSet<Capability> capabilities() {
      return EnumSet.of(Capability.INDEXED_KV, Capability.TIMESERIES_APPEND, Capability.HISTORY, Capability.SETTINGS, Capability.PLAYER_IDENTITY);
   }

   @NotNull
   @Override
   public Set<Category<?>> supportedCategories() {
      return Set.of(Categories.VIOLATION, Categories.SESSION, Categories.PLAYER_IDENTITY, Categories.SETTING);
   }

   @Override
   public void init(@NotNull BackendContext ctx) throws BackendException {
      this.logger = ctx.logger();

      try {
         Class.forName("com.mongodb.client.MongoClients");
      } catch (ClassNotFoundException var5) {
         throw new BackendException("mongodb-driver-sync not on the classpath — shade it into the plugin jar or drop it into server/plugins", var5);
      }

      try {
         this.client = MongoClients.create(this.config.connectionString());
         this.db = this.client.getDatabase(this.config.database());
         TableNames t = this.config.tableNames();
         this.meta = this.db.getCollection(t.meta());
         this.checks = this.db.getCollection(t.checks());
         this.players = this.db.getCollection(t.players());
         this.sessions = this.db.getCollection(t.sessions());
         this.violations = this.db.getCollection(t.violations());
         this.settings = this.db.getCollection(t.settings());
         int existingVersion = this.readSchemaVersion();
         this.ensureMetaDoc();
         if (existingVersion >= 0 && existingVersion < 6) {
            this.migrateSchema(existingVersion);
         }

         this.ensureIndexes();
         MongoCheckCatalogPersistence.alignCounterWithExistingRows(this.meta, this.checks);
      } catch (RuntimeException var4) {
         throw new BackendException("failed to initialise Mongo backend", var4);
      }
   }

   @NotNull
   @Override
   public CheckCatalogPersistence checkCatalog() {
      return new MongoCheckCatalogPersistence(this.meta, this.checks);
   }

   @NotNull
   @Override
   public CheckCatalogRepairResult repairCheckCatalog(@NotNull Map<Integer, Integer> legacyToCatalogCheckIds, String introducedVersionReplacement) throws BackendException {
      try {
         long violationsUpdated = 0L;

         for (Entry<Integer, Integer> e : legacyToCatalogCheckIds.entrySet()) {
            int legacyId = e.getKey();
            int catalogId = e.getValue();
            if (legacyId != catalogId) {
               UpdateResult updated = this.violations.updateMany(Filters.eq("check_id", legacyId), Updates.set("check_id", catalogId));
               violationsUpdated += updated.getModifiedCount();
            }
         }

         long versionsUpdated = 0L;
         if (introducedVersionReplacement != null && !introducedVersionReplacement.isBlank()) {
            UpdateResult updated = this.checks
               .updateMany(Filters.eq("introduced_version", "0.0.0-stub"), Updates.set("introduced_version", introducedVersionReplacement));
            versionsUpdated = updated.getModifiedCount();
         }

         return new CheckCatalogRepairResult(legacyToCatalogCheckIds.size(), violationsUpdated, versionsUpdated);
      } catch (RuntimeException var10) {
         throw new BackendException("check catalog repair failed", var10);
      }
   }

   private int readSchemaVersion() {
      Document metaDoc = (Document)this.meta.find(Filters.eq("_id", 0)).first();
      return metaDoc == null ? -1 : metaDoc.getInteger("schema_version", 0);
   }

   private void ensureIndexes() {
      this.checks.createIndex(Indexes.ascending(new String[]{"stable_key"}), new IndexOptions().unique(true));
      this.checks.createIndex(Indexes.ascending(new String[]{"check_id"}), new IndexOptions().unique(true));
      this.players.createIndex(Indexes.ascending(new String[]{"current_name_lower"}));
      this.sessions.createIndex(Indexes.descending(new String[]{"started_at"}));
      this.sessions
         .createIndex(Indexes.compoundIndex(new Bson[]{Indexes.ascending(new String[]{"player_uuid"}), Indexes.descending(new String[]{"started_at"})}));
      this.violations
         .createIndex(
            Indexes.compoundIndex(
               new Bson[]{Indexes.ascending(new String[]{"session_id"}), Indexes.ascending(new String[]{"occurred_at"}), Indexes.ascending(new String[]{"id"})}
            )
         );
      this.violations.createIndex(Indexes.ascending(new String[]{"player_uuid"}));
      this.violations.createIndex(Indexes.ascending(new String[]{"id"}), new IndexOptions().unique(true));
      this.settings
         .createIndex(
            Indexes.compoundIndex(
               new Bson[]{Indexes.ascending(new String[]{"scope"}), Indexes.ascending(new String[]{"scope_key"}), Indexes.ascending(new String[]{"key"})}
            ),
            new IndexOptions().unique(true)
         );
   }

   private void ensureMetaDoc() {
      Document metaDoc = (Document)this.meta.find(Filters.eq("_id", 0)).first();
      if (metaDoc == null) {
         long now = System.currentTimeMillis();
         this.meta
            .insertOne(
               new Document("_id", 0)
                  .append("schema_version", 6)
                  .append("grim_core_version", "phase1")
                  .append("initialized_at", now)
                  .append("last_migration_at", now)
            );
      }
   }

   private void migrateSchema(int existing) {
      if (existing < 6) {
         this.migrateV5ToV6();
      }

      this.meta
         .updateOne(
            Filters.eq("_id", 0),
            Updates.combine(
               new Bson[]{Updates.set("schema_version", 6), Updates.set("last_migration_at", System.currentTimeMillis()), Updates.unset("violation_id_seq")}
            )
         );
   }

   private void migrateV5ToV6() {
      this.logger.info("[grim-datastore] migrating Mongo violations id long→UUIDv7 …");

      try {
         this.violations.dropIndex(Indexes.ascending(new String[]{"id"}));
      } catch (RuntimeException var12) {
      }

      long count = 0L;
      List<WriteModel<Document>> ops = new ArrayList<>();
      MongoCursor var4 = this.violations.find().projection(new Document("_id", 1).append("id", 1).append("occurred_at", 1)).iterator();

      while (var4.hasNext()) {
         Document d = (Document)var4.next();
         Object idObj = d.get("id");
         if (!(idObj instanceof Binary) && !(idObj instanceof BsonBinary)) {
            long occurred = d.getLong("occurred_at") == null ? 0L : d.getLong("occurred_at");
            long legacyId = idObj instanceof Number n ? n.longValue() : 0L;
            UUID newId = UuidV7.fromTimestampMs(occurred, legacyId);
            ops.add(new UpdateOneModel(Filters.eq("_id", d.get("_id")), Updates.set("id", binUuid(newId))));
            if (ops.size() >= 1024) {
               this.violations.bulkWrite(ops);
               count += ops.size();
               ops.clear();
            }
         }
      }

      if (!ops.isEmpty()) {
         this.violations.bulkWrite(ops);
         count += ops.size();
      }

      this.logger.info("[grim-datastore] migrated " + count + " violation rows to UUIDv7 id");
   }

   @Override
   public void flush() {
   }

   @Override
   public void close() throws BackendException {
      synchronized (this.handlers) {
         for (MongoBackend.BatchingHandler<?> h : this.handlers) {
            h.shutDown();
         }

         this.handlers.clear();
      }

      if (this.client != null) {
         this.client.close();
         this.client = null;
      }
   }

   @NotNull
   @Override
   public <E> StorageEventHandler<E> eventHandlerFor(@NotNull Category<E> cat) throws BackendException {
      MongoBackend.BatchingHandler<?> h;
      if (cat == Categories.VIOLATION) {
         h = new MongoBackend.ViolationHandler();
      } else if (cat == Categories.SESSION) {
         h = new MongoBackend.SessionHandler();
      } else if (cat == Categories.PLAYER_IDENTITY) {
         h = new MongoBackend.IdentityHandler();
      } else {
         if (cat != Categories.SETTING) {
            throw new IllegalArgumentException("unsupported category: " + cat.id());
         }

         h = new MongoBackend.SettingHandler();
      }

      synchronized (this.handlers) {
         this.handlers.add(h);
         return (StorageEventHandler<E>)h;
      }
   }

   private Document sessionUpdateFields(SessionEvent s) {
      if (!s.sessionBlobs().isEmpty()) {
         throw new UnsupportedOperationException(
            "replay-clip serialisation isn't implemented by this backend; sessions with non-empty sessionBlobs cannot be stored"
         );
      } else {
         Document fields = new Document()
            .append("player_uuid", binUuid(s.playerUuid()))
            .append("server_name", s.serverName())
            .append("started_at", s.startedEpochMs())
            .append("last_activity", s.lastActivityEpochMs())
            .append("grim_version", s.grimVersion())
            .append("client_brand", s.clientBrand())
            .append("client_version_pvn", s.clientVersion())
            .append("server_version", s.serverVersionString())
            .append("replay_clips", List.of());
         if (s.instanceId() != null) {
            fields.append("instance_id", BsonBinaries.uuidBinary(s.instanceId()));
         }

         Object closedAtVal = s.closedAtEpochMs() == 0L ? null : s.closedAtEpochMs();
         fields.append("closed_at", new Document("$ifNull", Arrays.asList("$closed_at", closedAtVal)));
         return fields;
      }
   }

   private static BsonBinary binUuid(UUID u) {
      return new BsonBinary(UuidCodec.toBytes(u));
   }

   private static Document violationDoc(UUID id, UUID session, UUID player, int checkId, double vl, long occurred, String verbose, VerboseFormat fmt) {
      return new Document()
         .append("id", binUuid(id))
         .append("session_id", binUuid(session))
         .append("player_uuid", binUuid(player))
         .append("check_id", checkId)
         .append("vl", vl)
         .append("occurred_at", occurred)
         .append("verbose", verbose)
         .append("verbose_format", fmt.code());
   }

   private static Document sessionDoc(
      UUID session,
      UUID player,
      String serverName,
      long startedAt,
      long lastActivity,
      Long closedAt,
      String grimVersion,
      String clientBrand,
      int clientPvn,
      String serverVersion,
      UUID instanceId,
      boolean emptySessionBlobs
   ) {
      if (!emptySessionBlobs) {
         throw new UnsupportedOperationException(
            "replay-clip serialisation isn't implemented by this backend; sessions with non-empty sessionBlobs cannot be stored"
         );
      } else {
         Document d = new Document()
            .append("_id", binUuid(session))
            .append("player_uuid", binUuid(player))
            .append("server_name", serverName)
            .append("started_at", startedAt)
            .append("last_activity", lastActivity)
            .append("grim_version", grimVersion)
            .append("client_brand", clientBrand)
            .append("client_version_pvn", clientPvn)
            .append("server_version", serverVersion)
            .append("replay_clips", List.of());
         if (closedAt != null) {
            d.append("closed_at", closedAt);
         }

         if (instanceId != null) {
            d.append("instance_id", BsonBinaries.uuidBinary(instanceId));
         }

         return d;
      }
   }

   private static Document identityDoc(UUID uuid, String currentName, long firstSeen, long lastSeen) {
      return new Document()
         .append("_id", binUuid(uuid))
         .append("current_name", currentName)
         .append("current_name_lower", currentName == null ? null : currentName.toLowerCase(Locale.ROOT))
         .append("first_seen", firstSeen)
         .append("last_seen", lastSeen);
   }

   private static Document settingDoc(String scope, String scopeKey, String key, byte[] value, long updatedAt) {
      return new Document()
         .append("scope", scope)
         .append("scope_key", scopeKey)
         .append("key", key)
         .append("value", new BsonBinary(value))
         .append("updated_at", updatedAt);
   }

   @Override
   public <R> void bulkImport(@NotNull Category<?> cat, @NotNull List<R> records) throws BackendException {
      if (!records.isEmpty()) {
         try {
            if (cat == Categories.VIOLATION) {
               this.bulkViolations(records);
            } else if (cat == Categories.SESSION) {
               this.bulkSessions(records);
            } else if (cat == Categories.PLAYER_IDENTITY) {
               this.bulkIdentities(records);
            } else {
               if (cat != Categories.SETTING) {
                  throw new BackendException("unsupported category: " + cat.id());
               }

               this.bulkSettings(records);
            }
         } catch (RuntimeException var4) {
            throw new BackendException("bulkImport failed for " + cat.id(), var4);
         }
      }
   }

   private void bulkViolations(List<ViolationRecord> rows) {
      List<Document> docs = new ArrayList<>(rows.size());

      for (ViolationRecord v : rows) {
         docs.add(
            violationDoc(
               v.id(),
               v.sessionId(),
               v.playerUuid(),
               v.checkId(),
               v.vl(),
               v.occurredEpochMs(),
               encodeVerbose(v.verboseData(), v.verboseFormat()),
               v.verboseFormat()
            )
         );
      }

      if (!docs.isEmpty()) {
         this.violations.insertMany(docs, new InsertManyOptions().ordered(false));
      }
   }

   private void bulkSessions(List<SessionRecord> rows) {
      List<WriteModel<Document>> ops = new ArrayList<>(rows.size());

      for (SessionRecord s : rows) {
         Long closedAt = s.isClosed() ? s.closedAtEpochMs() : null;
         Document doc = sessionDoc(
            s.sessionId(),
            s.playerUuid(),
            s.serverName(),
            s.startedEpochMs(),
            s.lastActivityEpochMs(),
            closedAt,
            s.grimVersion(),
            s.clientBrand(),
            s.clientVersion(),
            s.serverVersionString(),
            s.instanceId(),
            s.sessionBlobs().isEmpty()
         );
         ops.add(new ReplaceOneModel(Filters.eq("_id", binUuid(s.sessionId())), doc, new ReplaceOptions().upsert(true)));
      }

      this.sessions.bulkWrite(ops);
   }

   private void bulkIdentities(List<PlayerIdentity> rows) {
      List<WriteModel<Document>> ops = new ArrayList<>(rows.size());

      for (PlayerIdentity id : rows) {
         ops.add(
            new ReplaceOneModel(
               Filters.eq("_id", binUuid(id.uuid())),
               identityDoc(id.uuid(), id.currentName(), id.firstSeenEpochMs(), id.lastSeenEpochMs()),
               new ReplaceOptions().upsert(true)
            )
         );
      }

      this.players.bulkWrite(ops);
   }

   private void bulkSettings(List<SettingRecord> rows) {
      List<WriteModel<Document>> ops = new ArrayList<>(rows.size());

      for (SettingRecord s : rows) {
         Bson filter = Filters.and(new Bson[]{Filters.eq("scope", s.scope().name()), Filters.eq("scope_key", s.scopeKey()), Filters.eq("key", s.key())});
         ops.add(
            new ReplaceOneModel(filter, settingDoc(s.scope().name(), s.scopeKey(), s.key(), s.value(), s.updatedEpochMs()), new ReplaceOptions().upsert(true))
         );
      }

      this.settings.bulkWrite(ops);
   }

   @NotNull
   @Override
   public <R> Page<R> read(@NotNull Category<?> cat, @NotNull Query<R> query) throws BackendException {
      try {
         if (query instanceof Queries.ListSessionsByPlayer q) {
            return (Page<R>)this.listSessionsByPlayer(q);
         } else if (query instanceof Queries.GetSessionById q) {
            return (Page<R>)this.getSessionById(q);
         } else if (query instanceof Queries.ListViolationsInSession q) {
            return (Page<R>)this.listViolationsInSession(q);
         } else if (query instanceof Queries.GetPlayerIdentity q) {
            return (Page<R>)this.getPlayerIdentity(q);
         } else if (query instanceof Queries.GetPlayerIdentityByName q) {
            return (Page<R>)this.getPlayerIdentityByName(q);
         } else if (query instanceof Queries.ListPlayersByNamePrefix q) {
            return (Page<R>)this.listPlayersByNamePrefix(q);
         } else if (query instanceof Queries.GetSetting q) {
            return (Page<R>)this.getSetting(q);
         } else {
            throw new BackendException("unsupported query: " + query.getClass().getSimpleName());
         }
      } catch (RuntimeException var4) {
         throw new BackendException("read failed", var4);
      }
   }

   private Page<SessionRecord> listSessionsByPlayer(Queries.ListSessionsByPlayer q) {
      long cursorStarted = decodeStartedCursor(q.cursor(), Long.MAX_VALUE);
      byte[] cursorSessionBytes = decodeSessionIdCursor(q.cursor());
      Bson playerEq = Filters.eq("player_uuid", binUuid(q.player()));
      Bson pageFilter = Filters.or(
         new Bson[]{
            Filters.lt("started_at", cursorStarted),
            Filters.and(new Bson[]{Filters.eq("started_at", cursorStarted), Filters.lt("_id", new BsonBinary(cursorSessionBytes))})
         }
      );
      List<Document> docs = new ArrayList<>();
      MongoCursor out = this.sessions
         .find(Filters.and(new Bson[]{playerEq, pageFilter}))
         .sort(new Document("started_at", -1).append("_id", -1))
         .limit(q.pageSize() + 1)
         .iterator();

      while (out.hasNext()) {
         Document d = (Document)out.next();
         docs.add(d);
      }

      List<SessionRecord> outx = new ArrayList<>();
      boolean hasMore = false;

      for (Document d : docs) {
         if (outx.size() >= q.pageSize()) {
            hasMore = true;
            break;
         }

         outx.add(mapSession(d));
      }

      Cursor next = null;
      if (hasMore && !outx.isEmpty()) {
         SessionRecord last = outx.get(outx.size() - 1);
         next = encodeStartedCursor(last.startedEpochMs(), last.sessionId());
      }

      return new Page<>(outx, next);
   }

   private Page<SessionRecord> getSessionById(Queries.GetSessionById q) {
      Document d = (Document)this.sessions.find(Filters.eq("_id", binUuid(q.sessionId()))).first();
      return d == null ? Page.empty() : new Page<>(List.of(mapSession(d)), null);
   }

   private Page<ViolationRecord> listViolationsInSession(Queries.ListViolationsInSession q) {
      long lastOccurred = decodeViolationOccurredCursor(q.cursor(), Long.MIN_VALUE);
      UUID lastId = decodeViolationIdCursor(q.cursor());
      Bson sessionEq = Filters.eq("session_id", binUuid(q.sessionId()));
      Bson finalFilter = lastId == null
         ? sessionEq
         : Filters.and(
            new Bson[]{
               sessionEq,
               Filters.or(
                  new Bson[]{
                     Filters.gt("occurred_at", lastOccurred),
                     Filters.and(new Bson[]{Filters.eq("occurred_at", lastOccurred), Filters.gt("id", binUuid(lastId))})
                  }
               )
            }
         );
      List<ViolationRecord> out = new ArrayList<>();
      boolean hasMore = false;
      List<Document> docs = new ArrayList<>();
      MongoCursor next = this.violations.find(finalFilter).sort(new Document("occurred_at", 1).append("id", 1)).limit(q.pageSize() + 1).iterator();

      while (next.hasNext()) {
         Document d = (Document)next.next();
         docs.add(d);
      }

      for (Document d : docs) {
         if (out.size() >= q.pageSize()) {
            hasMore = true;
            break;
         }

         out.add(mapViolation(d));
      }

      Cursor nextx = null;
      if (hasMore && !out.isEmpty()) {
         ViolationRecord last = out.get(out.size() - 1);
         nextx = encodeViolationCursor(last.occurredEpochMs(), last.id());
      }

      return new Page<>(out, nextx);
   }

   private Page<PlayerIdentity> getPlayerIdentity(Queries.GetPlayerIdentity q) {
      Document d = (Document)this.players.find(Filters.eq("_id", binUuid(q.uuid()))).first();
      return d == null ? Page.empty() : new Page<>(List.of(mapIdentity(d)), null);
   }

   private Page<PlayerIdentity> getPlayerIdentityByName(Queries.GetPlayerIdentityByName q) {
      Document d = (Document)this.players
         .find(Filters.eq("current_name_lower", q.name().toLowerCase(Locale.ROOT)))
         .sort(new Document("last_seen", -1))
         .limit(1)
         .first();
      return d == null ? Page.empty() : new Page<>(List.of(mapIdentity(d)), null);
   }

   private Page<PlayerIdentity> listPlayersByNamePrefix(Queries.ListPlayersByNamePrefix q) {
      String prefix = q.lowerPrefix();
      if (prefix != null && !prefix.isEmpty() && q.limit() > 0) {
         String anchored = "^" + Pattern.quote(prefix);
         List<PlayerIdentity> out = new ArrayList<>();
         MongoCursor var5 = this.players.find(Filters.regex("current_name_lower", anchored)).sort(new Document("last_seen", -1)).limit(q.limit()).iterator();

         while (var5.hasNext()) {
            Document d = (Document)var5.next();
            out.add(mapIdentity(d));
         }

         return new Page<>(out, null);
      } else {
         return Page.empty();
      }
   }

   private Page<SettingRecord> getSetting(Queries.GetSetting q) {
      Bson filter = Filters.and(new Bson[]{Filters.eq("scope", q.scope().name()), Filters.eq("scope_key", q.scopeKey()), Filters.eq("key", q.key())});
      Document d = (Document)this.settings.find(filter).first();
      return d == null ? Page.empty() : new Page<>(List.of(mapSetting(d)), null);
   }

   private static byte[] binBytes(Object raw) {
      if (raw instanceof Binary b) {
         return b.getData();
      } else if (raw instanceof BsonBinary b) {
         return b.getData();
      } else if (raw instanceof byte[] b) {
         return b;
      } else {
         throw new IllegalStateException("expected binary field, got " + (raw == null ? "null" : raw.getClass().getName()));
      }
   }

   private static byte[] verboseBytes(Object raw) {
      if (raw == null) {
         return null;
      } else {
         return raw instanceof String s ? s.getBytes(StandardCharsets.UTF_8) : binBytes(raw);
      }
   }

   private static String encodeVerbose(byte[] verboseData, VerboseFormat verboseFormat) {
      if (verboseData == null) {
         return null;
      } else {
         return verboseFormat == VerboseFormat.STRUCTURED_V1
            ? Base64.getEncoder().encodeToString(verboseData)
            : new String(verboseData, StandardCharsets.UTF_8);
      }
   }

   private static byte[] decodeVerbose(Object raw, VerboseFormat verboseFormat) {
      if (raw == null) {
         return null;
      } else {
         return verboseFormat == VerboseFormat.STRUCTURED_V1 && raw instanceof String s ? Base64.getDecoder().decode(s) : verboseBytes(raw);
      }
   }

   private static SessionRecord mapSession(Document d) {
      Long closedAtBoxed = d.getLong("closed_at");
      Object instanceRaw = d.get("instance_id");
      UUID instanceId = instanceRaw == null ? null : UuidCodec.fromBytes(binBytes(instanceRaw));
      return new SessionRecord(
         UuidCodec.fromBytes(binBytes(d.get("_id"))),
         UuidCodec.fromBytes(binBytes(d.get("player_uuid"))),
         d.getString("server_name"),
         d.getLong("started_at"),
         d.getLong("last_activity"),
         closedAtBoxed == null ? 0L : closedAtBoxed,
         d.getString("grim_version"),
         d.getString("client_brand"),
         d.getInteger("client_version_pvn", -1),
         d.getString("server_version"),
         instanceId,
         List.of()
      );
   }

   private static ViolationRecord mapViolation(Document d) {
      VerboseFormat verboseFormat = VerboseFormat.fromCode(d.getInteger("verbose_format", 0));
      return new ViolationRecord(
         UuidCodec.fromBytes(binBytes(d.get("id"))),
         UuidCodec.fromBytes(binBytes(d.get("session_id"))),
         UuidCodec.fromBytes(binBytes(d.get("player_uuid"))),
         d.getInteger("check_id"),
         d.getDouble("vl"),
         d.getLong("occurred_at"),
         decodeVerbose(d.get("verbose"), verboseFormat),
         verboseFormat
      );
   }

   private static PlayerIdentity mapIdentity(Document d) {
      return new PlayerIdentity(UuidCodec.fromBytes(binBytes(d.get("_id"))), d.getString("current_name"), d.getLong("first_seen"), d.getLong("last_seen"));
   }

   private static SettingRecord mapSetting(Document d) {
      return new SettingRecord(
         SettingScope.valueOf(d.getString("scope")), d.getString("scope_key"), d.getString("key"), binBytes(d.get("value")), d.getLong("updated_at")
      );
   }

   @Override
   public <E> void delete(@NotNull Category<E> cat, @NotNull DeleteCriteria criteria) throws BackendException {
      try {
         if (criteria instanceof Deletes.ByPlayer d) {
            BsonBinary uuid = binUuid(d.uuid());
            if (cat == Categories.VIOLATION) {
               this.violations.deleteMany(Filters.eq("player_uuid", uuid));
            } else if (cat == Categories.SESSION) {
               this.violations.deleteMany(Filters.eq("player_uuid", uuid));
               this.sessions.deleteMany(Filters.eq("player_uuid", uuid));
            } else if (cat == Categories.PLAYER_IDENTITY) {
               this.players.deleteOne(Filters.eq("_id", uuid));
            } else {
               if (cat != Categories.SETTING) {
                  throw new BackendException("unsupported category for delete: " + cat.id());
               }

               this.settings.deleteMany(Filters.and(new Bson[]{Filters.eq("scope", "PLAYER"), Filters.eq("scope_key", d.uuid().toString())}));
            }
         } else {
            if (!(criteria instanceof Deletes.OlderThan dx)) {
               throw new BackendException("unknown DeleteCriteria: " + criteria.getClass().getSimpleName());
            }

            long cutoff = System.currentTimeMillis() - dx.maxAgeMs();
            if (cat == Categories.SESSION) {
               List<BsonBinary> doomed = new ArrayList<>();
               MongoCursor var8 = this.sessions.find(Filters.lt("started_at", cutoff)).projection(new Document("_id", 1)).iterator();

               while (var8.hasNext()) {
                  Document s = (Document)var8.next();
                  doomed.add((BsonBinary)s.get("_id"));
               }

               if (!doomed.isEmpty()) {
                  this.violations.deleteMany(Filters.in("session_id", doomed));
                  this.sessions.deleteMany(Filters.in("_id", doomed));
               }
            } else {
               if (cat != Categories.VIOLATION) {
                  throw new BackendException("unsupported category for retention: " + cat.id());
               }

               this.violations.deleteMany(Filters.lt("occurred_at", cutoff));
            }
         }
      } catch (RuntimeException var10) {
         throw new BackendException("delete failed", var10);
      }
   }

   @Override
   public long countViolationsInSession(@NotNull UUID sessionId) throws BackendException {
      try {
         return this.violations.countDocuments(Filters.eq("session_id", binUuid(sessionId)));
      } catch (RuntimeException var3) {
         throw new BackendException("countViolationsInSession failed", var3);
      }
   }

   @Override
   public long countUniqueChecksInSession(@NotNull UUID sessionId) throws BackendException {
      try {
         List<Document> pipeline = Arrays.asList(
            new Document("$match", new Document("session_id", binUuid(sessionId))),
            new Document("$group", new Document("_id", "$check_id")),
            new Document("$count", "n")
         );
         MongoCursor var3 = this.violations.aggregate(pipeline).iterator();
         if (var3.hasNext()) {
            Document d = (Document)var3.next();
            return d.getInteger("n", 0);
         } else {
            return 0L;
         }
      } catch (RuntimeException var5) {
         throw new BackendException("countUniqueChecksInSession failed", var5);
      }
   }

   @Override
   public long countSessionsByPlayer(@NotNull UUID player) throws BackendException {
      try {
         return this.sessions.countDocuments(Filters.eq("player_uuid", binUuid(player)));
      } catch (RuntimeException var3) {
         throw new BackendException("countSessionsByPlayer failed", var3);
      }
   }

   @Override
   public long markCrashedSessions() throws BackendException {
      try {
         Document setStage = new Document("$set", new Document("closed_at", "$last_activity"));
         return this.sessions
            .updateMany(
               new Document("$or", List.of(Filters.exists("closed_at", false), Filters.eq("closed_at", null), Filters.eq("closed_at", 0L))), List.of(setStage)
            )
            .getModifiedCount();
      } catch (RuntimeException var2) {
         throw new BackendException("markCrashedSessions failed", var2);
      }
   }

   private static Cursor encodeStartedCursor(long started, UUID sessionId) {
      return new Cursor(started + ":" + sessionId.toString().replace("-", ""));
   }

   private static long decodeStartedCursor(Cursor c, long defaultVal) {
      if (c == null) {
         return defaultVal;
      } else {
         String t = c.token();
         int colon = t.indexOf(58);
         if (colon <= 0) {
            return defaultVal;
         } else {
            try {
               return Long.parseLong(t.substring(0, colon));
            } catch (NumberFormatException var6) {
               return defaultVal;
            }
         }
      }
   }

   private static byte[] decodeSessionIdCursor(Cursor c) {
      if (c == null) {
         return new byte[16];
      } else {
         String t = c.token();
         int colon = t.indexOf(58);
         if (colon > 0 && colon != t.length() - 1) {
            String hex = t.substring(colon + 1);
            if (hex.length() != 32) {
               return new byte[16];
            } else {
               byte[] out = new byte[16];

               for (int i = 0; i < 16; i++) {
                  out[i] = (byte)Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
               }

               return out;
            }
         } else {
            return new byte[16];
         }
      }
   }

   private static Cursor encodeViolationCursor(long occurredAt, UUID id) {
      return new Cursor("v:" + occurredAt + ":" + id);
   }

   private static long decodeViolationOccurredCursor(Cursor c, long defaultVal) {
      if (c == null) {
         return defaultVal;
      } else {
         String[] parts = c.token().split(":", 3);
         if (parts.length >= 3 && "v".equals(parts[0])) {
            try {
               return Long.parseLong(parts[1]);
            } catch (NumberFormatException var5) {
               return defaultVal;
            }
         } else {
            return defaultVal;
         }
      }
   }

   private static UUID decodeViolationIdCursor(Cursor c) {
      if (c == null) {
         return null;
      } else {
         String[] parts = c.token().split(":", 3);
         if (parts.length >= 3 && "v".equals(parts[0])) {
            try {
               return UUID.fromString(parts[2]);
            } catch (IllegalArgumentException var3) {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   @ApiStatus.Internal
   public TableNames tableNames() {
      return this.config.tableNames();
   }

   private abstract class BatchingHandler<E> implements StorageEventHandler<E> {
      final List<WriteModel<Document>> pending = new ArrayList<>();

      @Override
      public synchronized void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
         try {
            this.append(event);
            if (endOfBatch || this.pending.size() >= MongoBackend.this.config.batchFlushCap()) {
               this.flushLocked();
            }
         } catch (RuntimeException var6) {
            this.pending.clear();
            throw new BackendException(this.categoryId() + " write failed", var6);
         }
      }

      private void flushLocked() {
         if (!this.pending.isEmpty()) {
            this.collection().bulkWrite(this.pending);
            this.pending.clear();
         }
      }

      synchronized void shutDown() {
         try {
            this.flushLocked();
         } catch (RuntimeException var2) {
         }
      }

      protected abstract MongoCollection<Document> collection();

      protected abstract String categoryId();

      protected abstract void append(E var1);
   }

   private final class IdentityHandler extends MongoBackend.BatchingHandler<PlayerIdentityEvent> {
      @Override
      protected MongoCollection<Document> collection() {
         return MongoBackend.this.players;
      }

      @Override
      protected String categoryId() {
         return "player-identity";
      }

      protected void append(PlayerIdentityEvent e) {
         Document existing = (Document)MongoBackend.this.players.find(Filters.eq("_id", MongoBackend.binUuid(e.uuid()))).first();
         long first = existing == null ? e.firstSeenEpochMs() : Math.min(e.firstSeenEpochMs(), existing.getLong("first_seen"));
         long last = existing == null ? e.lastSeenEpochMs() : Math.max(e.lastSeenEpochMs(), existing.getLong("last_seen"));
         Document doc = MongoBackend.identityDoc(e.uuid(), e.currentName(), first, last);
         this.pending.add(new ReplaceOneModel(Filters.eq("_id", MongoBackend.binUuid(e.uuid())), doc, new ReplaceOptions().upsert(true)));
      }
   }

   private final class SessionHandler extends MongoBackend.BatchingHandler<SessionEvent> {
      @Override
      protected MongoCollection<Document> collection() {
         return MongoBackend.this.sessions;
      }

      @Override
      protected String categoryId() {
         return "session";
      }

      protected void append(SessionEvent s) {
         Document setStage = new Document("$set", MongoBackend.this.sessionUpdateFields(s));
         this.pending.add(new UpdateOneModel(Filters.eq("_id", MongoBackend.binUuid(s.sessionId())), List.of(setStage), new UpdateOptions().upsert(true)));
      }
   }

   private final class SettingHandler extends MongoBackend.BatchingHandler<SettingEvent> {
      @Override
      protected MongoCollection<Document> collection() {
         return MongoBackend.this.settings;
      }

      @Override
      protected String categoryId() {
         return "setting";
      }

      protected void append(SettingEvent s) {
         Document doc = MongoBackend.settingDoc(s.scope().name(), s.scopeKey(), s.key(), s.value(), s.updatedEpochMs());
         Bson filter = Filters.and(new Bson[]{Filters.eq("scope", s.scope().name()), Filters.eq("scope_key", s.scopeKey()), Filters.eq("key", s.key())});
         this.pending.add(new ReplaceOneModel(filter, doc, new ReplaceOptions().upsert(true)));
      }
   }

   private final class ViolationHandler extends MongoBackend.BatchingHandler<ViolationEvent> {
      @Override
      protected MongoCollection<Document> collection() {
         return MongoBackend.this.violations;
      }

      @Override
      protected String categoryId() {
         return "violation";
      }

      protected void append(ViolationEvent v) {
         UUID id = v.id();
         this.pending
            .add(
               new InsertOneModel(
                  MongoBackend.violationDoc(
                     id,
                     v.sessionId(),
                     v.playerUuid(),
                     v.checkId(),
                     v.vl(),
                     v.occurredEpochMs(),
                     MongoBackend.encodeVerbose(v.verboseData(), v.verboseFormat()),
                     v.verboseFormat()
                  )
               )
            );
      }
   }
}
