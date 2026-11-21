package ac.grim.grimac.internal.storage.backend.postgres;

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
import ac.grim.grimac.internal.storage.checks.JdbcCheckCatalogPersistence;
import ac.grim.grimac.internal.storage.checks.JdbcCheckCatalogRepair;
import ac.grim.grimac.internal.storage.util.UuidCodec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ApiStatus.Internal
public final class PostgresBackend implements Backend {
   public static final String ID = "postgres";
   private final PostgresBackendConfig config;
   private final Object writeMutex = new Object();
   private final List<PostgresBackend.BatchingHandler<?>> handlers = new ArrayList<>();
   private final String insertViolations;
   private final String upsertSessions;
   private final String upsertIdentities;
   private final String upsertSettings;
   private Connection writeConn;

   public PostgresBackend(PostgresBackendConfig config) {
      this.config = config;
      TableNames t = config.tableNames();
      this.insertViolations = "INSERT INTO "
         + PostgresSchema.quoteId(t.violations())
         + "(id, session_id, player_uuid, check_id, vl, occurred_at, \"verbose\", verbose_format) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
      this.upsertSessions = "INSERT INTO "
         + PostgresSchema.quoteId(t.sessions())
         + "(session_id, player_uuid, server_name, started_at, last_activity, closed_at, grim_version, client_brand, client_version_pvn, server_version, replay_clips_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (session_id) DO UPDATE SET server_name=EXCLUDED.server_name, last_activity=EXCLUDED.last_activity, closed_at=COALESCE("
         + PostgresSchema.quoteId(t.sessions())
         + ".closed_at, EXCLUDED.closed_at), grim_version=EXCLUDED.grim_version, client_brand=EXCLUDED.client_brand, client_version_pvn=EXCLUDED.client_version_pvn, server_version=EXCLUDED.server_version, replay_clips_json=EXCLUDED.replay_clips_json";
      this.upsertIdentities = "INSERT INTO "
         + PostgresSchema.quoteId(t.players())
         + "(uuid, current_name, first_seen, last_seen) VALUES (?, ?, ?, ?) ON CONFLICT (uuid) DO UPDATE SET current_name = EXCLUDED.current_name, first_seen = LEAST("
         + PostgresSchema.quoteId(t.players())
         + ".first_seen, EXCLUDED.first_seen), last_seen = GREATEST("
         + PostgresSchema.quoteId(t.players())
         + ".last_seen, EXCLUDED.last_seen)";
      this.upsertSettings = "INSERT INTO "
         + PostgresSchema.quoteId(t.settings())
         + "(scope, scope_key, key, value, updated_at) VALUES (?, ?, ?, ?, ?) ON CONFLICT (scope, scope_key, key) DO UPDATE SET value = EXCLUDED.value, updated_at = EXCLUDED.updated_at";
   }

   @NotNull
   @Override
   public String id() {
      return "postgres";
   }

   @NotNull
   @Override
   public ApiVersion getApiVersion() {
      return ApiVersion.CURRENT;
   }

   @NotNull
   @Override
   public EnumSet<Capability> capabilities() {
      return EnumSet.of(
         Capability.INDEXED_KV, Capability.TIMESERIES_APPEND, Capability.TRANSACTIONS, Capability.HISTORY, Capability.SETTINGS, Capability.PLAYER_IDENTITY
      );
   }

   @NotNull
   @Override
   public Set<Category<?>> supportedCategories() {
      return Set.of(Categories.VIOLATION, Categories.SESSION, Categories.PLAYER_IDENTITY, Categories.SETTING);
   }

   @Override
   public void init(@NotNull BackendContext ctx) throws BackendException {
      try {
         Class.forName("org.postgresql.Driver");
      } catch (ClassNotFoundException var7) {
         throw new BackendException("postgresql driver not on the classpath — either shade it into the plugin jar or drop it into server/plugins", var7);
      }

      synchronized (this.writeMutex) {
         try {
            this.writeConn = this.openConnection();
            PostgresSchema.ensureInitialized(this.writeConn, "phase1", this.config.tableNames());
         } catch (SQLException var5) {
            throw new BackendException("failed to initialise Postgres backend", var5);
         }
      }
   }

   private Connection openConnection() throws SQLException {
      return DriverManager.getConnection(this.config.jdbcUrl(), this.config.user(), this.config.password() == null ? "" : this.config.password());
   }

   @NotNull
   @Override
   public CheckCatalogPersistence checkCatalog() {
      String table = this.config.tableNames().checks();
      String quotedTable = PostgresSchema.quoteId(table);
      String escapedTable = quotedTable.replace("'", "''");
      String alignSequence = "SELECT setval(pg_get_serial_sequence('"
         + escapedTable
         + "', 'check_id'), GREATEST((SELECT COALESCE(MAX(check_id), 0) FROM "
         + quotedTable
         + "), 1), true)";
      return new JdbcCheckCatalogPersistence(this::openConnection, quotedTable, alignSequence);
   }

   @NotNull
   @Override
   public CheckCatalogRepairResult repairCheckCatalog(@NotNull Map<Integer, Integer> legacyToCatalogCheckIds, String introducedVersionReplacement) throws BackendException {
      try {
         TableNames t = this.config.tableNames();
         return JdbcCheckCatalogRepair.run(
            this::openConnection,
            PostgresSchema.quoteId(t.checks()),
            PostgresSchema.quoteId(t.violations()),
            legacyToCatalogCheckIds,
            introducedVersionReplacement
         );
      } catch (SQLException var4) {
         throw new BackendException("check catalog repair failed", var4);
      }
   }

   @Override
   public void flush() {
   }

   @Override
   public void close() throws BackendException {
      synchronized (this.handlers) {
         for (PostgresBackend.BatchingHandler<?> h : this.handlers) {
            h.shutDown();
         }

         this.handlers.clear();
      }

      synchronized (this.writeMutex) {
         try {
            if (this.writeConn != null) {
               this.writeConn.close();
            }
         } catch (SQLException var11) {
            throw new BackendException("close failed", var11);
         } finally {
            this.writeConn = null;
         }
      }
   }

   @NotNull
   @Override
   public <E> StorageEventHandler<E> eventHandlerFor(@NotNull Category<E> cat) throws BackendException {
      PostgresBackend.BatchingHandler<?> h;
      if (cat == Categories.VIOLATION) {
         h = new PostgresBackend.ViolationHandler();
      } else if (cat == Categories.SESSION) {
         h = new PostgresBackend.SessionHandler();
      } else if (cat == Categories.PLAYER_IDENTITY) {
         h = new PostgresBackend.IdentityHandler();
      } else {
         if (cat != Categories.SETTING) {
            throw new IllegalArgumentException("unsupported category: " + cat.id());
         }

         h = new PostgresBackend.SettingHandler();
      }

      h.start();
      synchronized (this.handlers) {
         this.handlers.add(h);
         return (StorageEventHandler<E>)h;
      }
   }

   private static String serializeSessionBlobsShim() {
      throw new UnsupportedOperationException(
         "replay-clip serialisation isn't implemented by this backend; sessions with non-empty sessionBlobs cannot be stored"
      );
   }

   @Override
   public <R> void bulkImport(@NotNull Category<?> cat, @NotNull List<R> records) throws BackendException {
      if (!records.isEmpty()) {
         synchronized (this.writeMutex) {
            if (this.writeConn == null) {
               throw new BackendException("backend not initialised");
            } else {
               try {
                  this.writeConn.setAutoCommit(false);
                  if (cat == Categories.VIOLATION) {
                     this.writeViolationRecords(records);
                  } else if (cat == Categories.SESSION) {
                     this.writeSessionRecords(records);
                  } else if (cat == Categories.PLAYER_IDENTITY) {
                     this.writeIdentityRecords(records);
                  } else {
                     if (cat != Categories.SETTING) {
                        throw new BackendException("unsupported category: " + cat.id());
                     }

                     this.writeSettingRecords(records);
                  }

                  this.writeConn.commit();
               } catch (SQLException var17) {
                  try {
                     this.writeConn.rollback();
                  } catch (SQLException var16) {
                  }

                  throw new BackendException("bulkImport failed for " + cat.id(), var17);
               } finally {
                  try {
                     this.writeConn.setAutoCommit(true);
                  } catch (SQLException var15) {
                  }
               }
            }
         }
      }
   }

   private void writeViolationRecords(List<ViolationRecord> rows) throws SQLException {
      try (PreparedStatement ps = this.writeConn.prepareStatement(this.insertViolations)) {
         for (ViolationRecord v : rows) {
            ps.setBytes(1, UuidCodec.toBytes(v.id()));
            ps.setBytes(2, UuidCodec.toBytes(v.sessionId()));
            ps.setBytes(3, UuidCodec.toBytes(v.playerUuid()));
            ps.setInt(4, v.checkId());
            ps.setDouble(5, v.vl());
            ps.setLong(6, v.occurredEpochMs());
            ps.setString(7, encodeVerbose(v.verboseData(), v.verboseFormat()));
            ps.setInt(8, v.verboseFormat().code());
            ps.addBatch();
         }

         ps.executeBatch();
      }
   }

   private void writeSessionRecords(List<SessionRecord> rows) throws SQLException {
      try (PreparedStatement ps = this.writeConn.prepareStatement(this.upsertSessions)) {
         for (SessionRecord s : rows) {
            ps.setBytes(1, UuidCodec.toBytes(s.sessionId()));
            ps.setBytes(2, UuidCodec.toBytes(s.playerUuid()));
            ps.setString(3, s.serverName());
            ps.setLong(4, s.startedEpochMs());
            ps.setLong(5, s.lastActivityEpochMs());
            if (!s.isClosed()) {
               ps.setNull(6, -5);
            } else {
               ps.setLong(6, s.closedAtEpochMs());
            }

            ps.setString(7, s.grimVersion());
            ps.setString(8, s.clientBrand());
            ps.setInt(9, s.clientVersion());
            ps.setString(10, s.serverVersionString());
            ps.setString(11, s.sessionBlobs().isEmpty() ? "[]" : serializeSessionBlobsShim());
            ps.addBatch();
         }

         ps.executeBatch();
      }
   }

   private void writeIdentityRecords(List<PlayerIdentity> rows) throws SQLException {
      try (PreparedStatement ps = this.writeConn.prepareStatement(this.upsertIdentities)) {
         for (PlayerIdentity id : rows) {
            ps.setBytes(1, UuidCodec.toBytes(id.uuid()));
            ps.setString(2, id.currentName());
            ps.setLong(3, id.firstSeenEpochMs());
            ps.setLong(4, id.lastSeenEpochMs());
            ps.addBatch();
         }

         ps.executeBatch();
      }
   }

   private void writeSettingRecords(List<SettingRecord> rows) throws SQLException {
      try (PreparedStatement ps = this.writeConn.prepareStatement(this.upsertSettings)) {
         for (SettingRecord s : rows) {
            ps.setString(1, s.scope().name());
            ps.setString(2, s.scopeKey());
            ps.setString(3, s.key());
            ps.setBytes(4, s.value());
            ps.setLong(5, s.updatedEpochMs());
            ps.addBatch();
         }

         ps.executeBatch();
      }
   }

   @NotNull
   @Override
   public <R> Page<R> read(@NotNull Category<?> cat, @NotNull Query<R> query) throws BackendException {
      try {
         Page var5;
         try (Connection c = this.openConnection()) {
            if (query instanceof Queries.ListSessionsByPlayer q) {
               return (Page<R>)this.listSessionsByPlayer(c, q);
            }

            if (query instanceof Queries.GetSessionById q) {
               return (Page<R>)this.getSessionById(c, q);
            }

            if (query instanceof Queries.ListViolationsInSession q) {
               return (Page<R>)this.listViolationsInSession(c, q);
            }

            if (query instanceof Queries.GetPlayerIdentity q) {
               return (Page<R>)this.getPlayerIdentity(c, q);
            }

            if (query instanceof Queries.GetPlayerIdentityByName q) {
               return (Page<R>)this.getPlayerIdentityByName(c, q);
            }

            if (query instanceof Queries.ListPlayersByNamePrefix q) {
               return (Page<R>)this.listPlayersByNamePrefix(c, q);
            }

            if (!(query instanceof Queries.GetSetting q)) {
               throw new BackendException("unsupported query: " + query.getClass().getSimpleName());
            }

            var5 = this.getSetting(c, q);
         }

         return var5;
      } catch (SQLException var8) {
         throw new BackendException("read failed", var8);
      }
   }

   private Page<SessionRecord> listSessionsByPlayer(Connection c, Queries.ListSessionsByPlayer q) throws SQLException {
      long cursorStarted = decodeStartedCursor(q.cursor(), Long.MAX_VALUE);
      byte[] cursorSessionId = decodeSessionIdCursor(q.cursor());

      Page var16;
      try (PreparedStatement ps = c.prepareStatement(
            "SELECT session_id, player_uuid, server_name, started_at, last_activity, closed_at, grim_version, client_brand, client_version_pvn, server_version, replay_clips_json FROM "
               + PostgresSchema.quoteId(this.config.tableNames().sessions())
               + " WHERE player_uuid = ? AND (started_at < ? OR (started_at = ? AND session_id < ?)) ORDER BY started_at DESC, session_id DESC LIMIT ?"
         )) {
         ps.setBytes(1, UuidCodec.toBytes(q.player()));
         ps.setLong(2, cursorStarted);
         ps.setLong(3, cursorStarted);
         ps.setBytes(4, cursorSessionId);
         ps.setInt(5, q.pageSize() + 1);

         try (ResultSet rs = ps.executeQuery()) {
            List<SessionRecord> out = new ArrayList<>();
            boolean hasMore = false;

            while (rs.next()) {
               if (out.size() >= q.pageSize()) {
                  hasMore = true;
                  break;
               }

               out.add(mapSession(rs));
            }

            Cursor next = null;
            if (hasMore && !out.isEmpty()) {
               SessionRecord last = out.get(out.size() - 1);
               next = encodeStartedCursor(last.startedEpochMs(), last.sessionId());
            }

            var16 = new Page<>(out, next);
         }
      }

      return var16;
   }

   private Page<SessionRecord> getSessionById(Connection c, Queries.GetSessionById q) throws SQLException {
      Page var11;
      try (PreparedStatement ps = c.prepareStatement(
            "SELECT session_id, player_uuid, server_name, started_at, last_activity, closed_at, grim_version, client_brand, client_version_pvn, server_version, replay_clips_json FROM "
               + PostgresSchema.quoteId(this.config.tableNames().sessions())
               + " WHERE session_id = ?"
         )) {
         ps.setBytes(1, UuidCodec.toBytes(q.sessionId()));

         try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
               return new Page<>(List.of(mapSession(rs)), null);
            }

            var11 = Page.empty();
         }
      }

      return var11;
   }

   private Page<ViolationRecord> listViolationsInSession(Connection c, Queries.ListViolationsInSession q) throws SQLException {
      long lastOccurred = decodeViolationOccurredCursor(q.cursor(), Long.MIN_VALUE);
      byte[] lastId = decodeViolationIdCursor(q.cursor());

      Page var16;
      try (PreparedStatement ps = c.prepareStatement(
            "SELECT id, session_id, player_uuid, check_id, vl, occurred_at, \"verbose\", verbose_format FROM "
               + PostgresSchema.quoteId(this.config.tableNames().violations())
               + " WHERE session_id = ? AND (occurred_at > ? OR (occurred_at = ? AND id > ?)) ORDER BY occurred_at ASC, id ASC LIMIT ?"
         )) {
         ps.setBytes(1, UuidCodec.toBytes(q.sessionId()));
         ps.setLong(2, lastOccurred);
         ps.setLong(3, lastOccurred);
         ps.setBytes(4, lastId);
         ps.setInt(5, q.pageSize() + 1);

         try (ResultSet rs = ps.executeQuery()) {
            List<ViolationRecord> out = new ArrayList<>();
            boolean hasMore = false;

            while (rs.next()) {
               if (out.size() >= q.pageSize()) {
                  hasMore = true;
                  break;
               }

               out.add(mapViolation(rs));
            }

            Cursor next = null;
            if (hasMore && !out.isEmpty()) {
               ViolationRecord last = out.get(out.size() - 1);
               next = encodeViolationCursor(last.occurredEpochMs(), last.id());
            }

            var16 = new Page<>(out, next);
         }
      }

      return var16;
   }

   private Page<PlayerIdentity> getPlayerIdentity(Connection c, Queries.GetPlayerIdentity q) throws SQLException {
      Page var11;
      try (PreparedStatement ps = c.prepareStatement(
            "SELECT uuid, current_name, first_seen, last_seen FROM " + PostgresSchema.quoteId(this.config.tableNames().players()) + " WHERE uuid = ?"
         )) {
         ps.setBytes(1, UuidCodec.toBytes(q.uuid()));

         try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
               return new Page<>(List.of(mapIdentity(rs)), null);
            }

            var11 = Page.empty();
         }
      }

      return var11;
   }

   private Page<PlayerIdentity> getPlayerIdentityByName(Connection c, Queries.GetPlayerIdentityByName q) throws SQLException {
      Page var11;
      try (PreparedStatement ps = c.prepareStatement(
            "SELECT uuid, current_name, first_seen, last_seen FROM "
               + PostgresSchema.quoteId(this.config.tableNames().players())
               + " WHERE lower(current_name) = ? ORDER BY last_seen DESC LIMIT 1"
         )) {
         ps.setString(1, q.name().toLowerCase(Locale.ROOT));

         try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
               return new Page<>(List.of(mapIdentity(rs)), null);
            }

            var11 = Page.empty();
         }
      }

      return var11;
   }

   private Page<PlayerIdentity> listPlayersByNamePrefix(Connection c, Queries.ListPlayersByNamePrefix q) throws SQLException {
      String prefix = q.lowerPrefix();
      if (prefix != null && !prefix.isEmpty() && q.limit() > 0) {
         String escaped = escapeLike(prefix);

         Page var8;
         try (PreparedStatement ps = c.prepareStatement(
               "SELECT uuid, current_name, first_seen, last_seen FROM "
                  + PostgresSchema.quoteId(this.config.tableNames().players())
                  + " WHERE lower(current_name) LIKE ? ESCAPE '\\' ORDER BY last_seen DESC LIMIT ?"
            )) {
            ps.setString(1, escaped + "%");
            ps.setInt(2, q.limit());

            try (ResultSet rs = ps.executeQuery()) {
               List<PlayerIdentity> out = new ArrayList<>();

               while (rs.next()) {
                  out.add(mapIdentity(rs));
               }

               var8 = new Page<>(out, null);
            }
         }

         return var8;
      } else {
         return Page.empty();
      }
   }

   private static String escapeLike(String s) {
      StringBuilder out = new StringBuilder(s.length());

      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         if (c == '\\' || c == '%' || c == '_') {
            out.append('\\');
         }

         out.append(c);
      }

      return out.toString();
   }

   private Page<SettingRecord> getSetting(Connection c, Queries.GetSetting q) throws SQLException {
      Page var11;
      try (PreparedStatement ps = c.prepareStatement(
            "SELECT scope, scope_key, key, value, updated_at FROM "
               + PostgresSchema.quoteId(this.config.tableNames().settings())
               + " WHERE scope = ? AND scope_key = ? AND key = ?"
         )) {
         ps.setString(1, q.scope().name());
         ps.setString(2, q.scopeKey());
         ps.setString(3, q.key());

         try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
               return new Page<>(List.of(mapSetting(rs)), null);
            }

            var11 = Page.empty();
         }
      }

      return var11;
   }

   private static SessionRecord mapSession(ResultSet rs) throws SQLException {
      long closedAt = rs.getLong("closed_at");
      if (rs.wasNull()) {
         closedAt = 0L;
      }

      return new SessionRecord(
         UuidCodec.fromBytes(rs.getBytes("session_id")),
         UuidCodec.fromBytes(rs.getBytes("player_uuid")),
         rs.getString("server_name"),
         rs.getLong("started_at"),
         rs.getLong("last_activity"),
         closedAt,
         rs.getString("grim_version"),
         rs.getString("client_brand"),
         rs.getInt("client_version_pvn"),
         rs.getString("server_version"),
         null,
         List.of()
      );
   }

   private static ViolationRecord mapViolation(ResultSet rs) throws SQLException {
      VerboseFormat verboseFormat = VerboseFormat.fromCode(rs.getInt("verbose_format"));
      return new ViolationRecord(
         UuidCodec.fromBytes(rs.getBytes("id")),
         UuidCodec.fromBytes(rs.getBytes("session_id")),
         UuidCodec.fromBytes(rs.getBytes("player_uuid")),
         rs.getInt("check_id"),
         rs.getDouble("vl"),
         rs.getLong("occurred_at"),
         decodeVerbose(rs.getString("verbose"), verboseFormat),
         verboseFormat
      );
   }

   private static PlayerIdentity mapIdentity(ResultSet rs) throws SQLException {
      return new PlayerIdentity(UuidCodec.fromBytes(rs.getBytes("uuid")), rs.getString("current_name"), rs.getLong("first_seen"), rs.getLong("last_seen"));
   }

   private static SettingRecord mapSetting(ResultSet rs) throws SQLException {
      return new SettingRecord(
         SettingScope.valueOf(rs.getString("scope")), rs.getString("scope_key"), rs.getString("key"), rs.getBytes("value"), rs.getLong("updated_at")
      );
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

   private static byte[] decodeVerbose(String verbose, VerboseFormat verboseFormat) {
      if (verbose == null) {
         return null;
      } else {
         return verboseFormat == VerboseFormat.STRUCTURED_V1 ? Base64.getDecoder().decode(verbose) : verbose.getBytes(StandardCharsets.UTF_8);
      }
   }

   @Override
   public <E> void delete(@NotNull Category<E> cat, @NotNull DeleteCriteria criteria) throws BackendException {
      synchronized (this.writeMutex) {
         if (this.writeConn == null) {
            throw new BackendException("backend not initialised");
         } else {
            try {
               this.writeConn.setAutoCommit(false);
               TableNames t = this.config.tableNames();
               if (criteria instanceof Deletes.ByPlayer d) {
                  byte[] uuid = UuidCodec.toBytes(d.uuid());
                  if (cat == Categories.VIOLATION) {
                     this.execDelete("DELETE FROM " + PostgresSchema.quoteId(t.violations()) + " WHERE player_uuid = ?", uuid);
                  } else if (cat == Categories.SESSION) {
                     this.execDelete("DELETE FROM " + PostgresSchema.quoteId(t.violations()) + " WHERE player_uuid = ?", uuid);
                     this.execDelete("DELETE FROM " + PostgresSchema.quoteId(t.sessions()) + " WHERE player_uuid = ?", uuid);
                  } else if (cat == Categories.PLAYER_IDENTITY) {
                     this.execDelete("DELETE FROM " + PostgresSchema.quoteId(t.players()) + " WHERE uuid = ?", uuid);
                  } else {
                     if (cat != Categories.SETTING) {
                        throw new BackendException("unsupported category for delete: " + cat.id());
                     }

                     try (PreparedStatement ps = this.writeConn
                           .prepareStatement("DELETE FROM " + PostgresSchema.quoteId(t.settings()) + " WHERE scope = 'PLAYER' AND scope_key = ?")) {
                        ps.setString(1, d.uuid().toString());
                        ps.executeUpdate();
                     }
                  }
               } else {
                  if (!(criteria instanceof Deletes.OlderThan dx)) {
                     throw new BackendException("unknown DeleteCriteria: " + criteria.getClass().getSimpleName());
                  }

                  long cutoff = System.currentTimeMillis() - dx.maxAgeMs();
                  if (cat == Categories.SESSION) {
                     try (PreparedStatement ps = this.writeConn
                           .prepareStatement(
                              "DELETE FROM "
                                 + PostgresSchema.quoteId(t.violations())
                                 + " WHERE session_id IN (SELECT session_id FROM "
                                 + PostgresSchema.quoteId(t.sessions())
                                 + " WHERE started_at < ?)"
                           )) {
                        ps.setLong(1, cutoff);
                        ps.executeUpdate();
                     }

                     try (PreparedStatement ps = this.writeConn
                           .prepareStatement("DELETE FROM " + PostgresSchema.quoteId(t.sessions()) + " WHERE started_at < ?")) {
                        ps.setLong(1, cutoff);
                        ps.executeUpdate();
                     }
                  } else {
                     if (cat != Categories.VIOLATION) {
                        throw new BackendException("unsupported category for retention: " + cat.id());
                     }

                     try (PreparedStatement ps = this.writeConn
                           .prepareStatement("DELETE FROM " + PostgresSchema.quoteId(t.violations()) + " WHERE occurred_at < ?")) {
                        ps.setLong(1, cutoff);
                        ps.executeUpdate();
                     }
                  }
               }

               this.writeConn.commit();
            } catch (SQLException var39) {
               try {
                  this.writeConn.rollback();
               } catch (SQLException var30) {
               }

               throw new BackendException("delete failed", var39);
            } finally {
               try {
                  this.writeConn.setAutoCommit(true);
               } catch (SQLException var29) {
               }
            }
         }
      }
   }

   private void execDelete(String sql, byte[] uuid) throws SQLException {
      try (PreparedStatement ps = this.writeConn.prepareStatement(sql)) {
         ps.setBytes(1, uuid);
         ps.executeUpdate();
      }
   }

   @Override
   public long countViolationsInSession(@NotNull UUID sessionId) throws BackendException {
      return this.scalarLong(
         "SELECT COUNT(*) FROM " + PostgresSchema.quoteId(this.config.tableNames().violations()) + " WHERE session_id = ?",
         UuidCodec.toBytes(sessionId),
         "countViolationsInSession"
      );
   }

   @Override
   public long countUniqueChecksInSession(@NotNull UUID sessionId) throws BackendException {
      return this.scalarLong(
         "SELECT COUNT(DISTINCT check_id) FROM " + PostgresSchema.quoteId(this.config.tableNames().violations()) + " WHERE session_id = ?",
         UuidCodec.toBytes(sessionId),
         "countUniqueChecksInSession"
      );
   }

   @Override
   public long countSessionsByPlayer(@NotNull UUID player) throws BackendException {
      return this.scalarLong(
         "SELECT COUNT(*) FROM " + PostgresSchema.quoteId(this.config.tableNames().sessions()) + " WHERE player_uuid = ?",
         UuidCodec.toBytes(player),
         "countSessionsByPlayer"
      );
   }

   @Override
   public long markCrashedSessions() throws BackendException {
      try {
         long var3;
         try (
            Connection c = this.openConnection();
            PreparedStatement ps = c.prepareStatement(
               "UPDATE " + PostgresSchema.quoteId(this.config.tableNames().sessions()) + " SET closed_at = last_activity WHERE closed_at IS NULL"
            );
         ) {
            var3 = ps.executeUpdate();
         }

         return var3;
      } catch (SQLException var9) {
         throw new BackendException("markCrashedSessions failed", var9);
      }
   }

   private long scalarLong(String sql, byte[] bind, String op) throws BackendException {
      try {
         long var7;
         try (
            Connection c = this.openConnection();
            PreparedStatement ps = c.prepareStatement(sql);
         ) {
            ps.setBytes(1, bind);

            try (ResultSet rs = ps.executeQuery()) {
               var7 = rs.next() ? rs.getLong(1) : 0L;
            }
         }

         return var7;
      } catch (SQLException var15) {
         throw new BackendException(op + " failed", var15);
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

   private static byte[] decodeViolationIdCursor(Cursor c) {
      if (c == null) {
         return new byte[16];
      } else {
         String[] parts = c.token().split(":", 3);
         if (parts.length >= 3 && "v".equals(parts[0])) {
            try {
               return UuidCodec.toBytes(UUID.fromString(parts[2]));
            } catch (IllegalArgumentException var3) {
               return new byte[16];
            }
         } else {
            return new byte[16];
         }
      }
   }

   @ApiStatus.Internal
   public TableNames tableNames() {
      return this.config.tableNames();
   }

   private abstract class BatchingHandler<E> implements StorageEventHandler<E> {
      private Connection conn;
      private PreparedStatement stmt;
      private int pending;

      void start() throws BackendException {
         try {
            this.conn = PostgresBackend.this.openConnection();
            this.conn.setAutoCommit(false);
         } catch (SQLException var2) {
            throw new BackendException("init failed for " + this.categoryId(), var2);
         }
      }

      @Override
      public synchronized void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
         if (this.conn != null) {
            try {
               if (this.stmt == null) {
                  this.stmt = this.conn.prepareStatement(this.sql());
               }

               this.bind(this.stmt, event);
               this.stmt.addBatch();
               this.pending++;
               if (endOfBatch || this.pending >= PostgresBackend.this.config.batchFlushCap()) {
                  this.flushLocked();
               }
            } catch (SQLException var6) {
               this.abortLocked();
               throw new BackendException(this.categoryId() + " write failed", var6);
            }
         }
      }

      private void flushLocked() throws SQLException {
         if (this.pending != 0) {
            this.stmt.executeBatch();
            this.conn.commit();
            this.pending = 0;
         }
      }

      private void abortLocked() {
         try {
            if (this.conn != null) {
               this.conn.rollback();
            }
         } catch (SQLException var3) {
         }

         try {
            if (this.stmt != null) {
               this.stmt.close();
               this.stmt = null;
            }
         } catch (SQLException var2) {
         }

         this.pending = 0;
      }

      synchronized void shutDown() {
         try {
            if (this.pending > 0 && this.stmt != null) {
               this.stmt.executeBatch();
               this.conn.commit();
            }
         } catch (SQLException var4) {
         }

         try {
            if (this.stmt != null) {
               this.stmt.close();
            }
         } catch (SQLException var3) {
         }

         try {
            if (this.conn != null) {
               this.conn.close();
            }
         } catch (SQLException var2) {
         }

         this.stmt = null;
         this.conn = null;
         this.pending = 0;
      }

      protected abstract String sql();

      protected abstract String categoryId();

      protected abstract void bind(PreparedStatement var1, E var2) throws SQLException;
   }

   private final class IdentityHandler extends PostgresBackend.BatchingHandler<PlayerIdentityEvent> {
      @Override
      protected String sql() {
         return PostgresBackend.this.upsertIdentities;
      }

      @Override
      protected String categoryId() {
         return "player-identity";
      }

      protected void bind(PreparedStatement ps, PlayerIdentityEvent e) throws SQLException {
         ps.setBytes(1, UuidCodec.toBytes(e.uuid()));
         ps.setString(2, e.currentName());
         ps.setLong(3, e.firstSeenEpochMs());
         ps.setLong(4, e.lastSeenEpochMs());
      }
   }

   private final class SessionHandler extends PostgresBackend.BatchingHandler<SessionEvent> {
      @Override
      protected String sql() {
         return PostgresBackend.this.upsertSessions;
      }

      @Override
      protected String categoryId() {
         return "session";
      }

      protected void bind(PreparedStatement ps, SessionEvent s) throws SQLException {
         ps.setBytes(1, UuidCodec.toBytes(s.sessionId()));
         ps.setBytes(2, UuidCodec.toBytes(s.playerUuid()));
         ps.setString(3, s.serverName());
         ps.setLong(4, s.startedEpochMs());
         ps.setLong(5, s.lastActivityEpochMs());
         if (!s.isClosed()) {
            ps.setNull(6, -5);
         } else {
            ps.setLong(6, s.closedAtEpochMs());
         }

         ps.setString(7, s.grimVersion());
         ps.setString(8, s.clientBrand());
         ps.setInt(9, s.clientVersion());
         ps.setString(10, s.serverVersionString());
         ps.setString(11, s.sessionBlobs().isEmpty() ? "[]" : PostgresBackend.serializeSessionBlobsShim());
      }
   }

   private final class SettingHandler extends PostgresBackend.BatchingHandler<SettingEvent> {
      @Override
      protected String sql() {
         return PostgresBackend.this.upsertSettings;
      }

      @Override
      protected String categoryId() {
         return "setting";
      }

      protected void bind(PreparedStatement ps, SettingEvent s) throws SQLException {
         ps.setString(1, s.scope().name());
         ps.setString(2, s.scopeKey());
         ps.setString(3, s.key());
         ps.setBytes(4, s.value());
         ps.setLong(5, s.updatedEpochMs());
      }
   }

   private final class ViolationHandler extends PostgresBackend.BatchingHandler<ViolationEvent> {
      @Override
      protected String sql() {
         return PostgresBackend.this.insertViolations;
      }

      @Override
      protected String categoryId() {
         return "violation";
      }

      protected void bind(PreparedStatement ps, ViolationEvent v) throws SQLException {
         UUID id = v.id();
         ps.setBytes(1, UuidCodec.toBytes(id));
         ps.setBytes(2, UuidCodec.toBytes(v.sessionId()));
         ps.setBytes(3, UuidCodec.toBytes(v.playerUuid()));
         ps.setInt(4, v.checkId());
         ps.setDouble(5, v.vl());
         ps.setLong(6, v.occurredEpochMs());
         ps.setString(7, PostgresBackend.encodeVerbose(v.verboseData(), v.verboseFormat()));
         ps.setInt(8, v.verboseFormat().code());
      }
   }
}
