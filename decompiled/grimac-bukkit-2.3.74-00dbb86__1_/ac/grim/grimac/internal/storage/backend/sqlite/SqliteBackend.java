package ac.grim.grimac.internal.storage.backend.sqlite;

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
import ac.grim.grimac.internal.storage.backend.sqlite.writers.IdentityUpserter;
import ac.grim.grimac.internal.storage.backend.sqlite.writers.SessionUpserter;
import ac.grim.grimac.internal.storage.backend.sqlite.writers.SettingsUpserter;
import ac.grim.grimac.internal.storage.backend.sqlite.writers.UpserterFactory;
import ac.grim.grimac.internal.storage.checks.JdbcCheckCatalogPersistence;
import ac.grim.grimac.internal.storage.checks.JdbcCheckCatalogRepair;
import ac.grim.grimac.internal.storage.util.UuidCodec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

@ApiStatus.Internal
public final class SqliteBackend implements Backend {
   public static final String ID = "sqlite";
   private final SqliteBackendConfig config;
   private final Object writeMutex = new Object();
   private final List<SqliteBackend.BatchingHandler<?>> handlers = new ArrayList<>();
   private final String insertViolations;
   private UpserterFactory upserterFactory = UpserterFactory.MODERN;
   private String jdbcUrl;
   private Connection writeConn;
   private Method holderConnectMethod;

   public SqliteBackend(SqliteBackendConfig config) {
      this.config = config;
      TableNames t = config.tableNames();
      this.insertViolations = "INSERT INTO "
         + t.violations()
         + "(id, session_id, player_uuid, check_id, vl, occurred_at, verbose, verbose_format) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
   }

   @NotNull
   @Override
   public String id() {
      return "sqlite";
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
      Path dataDir = ctx.dataDirectory();
      Path dbFile = Path.of(this.config.path()).isAbsolute() ? Path.of(this.config.path()) : dataDir.resolve(this.config.path());

      try {
         Files.createDirectories(dbFile.getParent());
      } catch (IOException var11) {
         throw new BackendException("failed to create SQLite data dir: " + dbFile.getParent(), var11);
      }

      this.jdbcUrl = "jdbc:sqlite:" + dbFile.toAbsolutePath();

      try {
         Class.forName("org.sqlite.JDBC");
      } catch (ClassNotFoundException var10) {
         throw new BackendException("sqlite-jdbc not on the classpath", var10);
      }

      this.holderConnectMethod = this.pickHolderIfNewer(ctx.logger());
      synchronized (this.writeMutex) {
         try {
            this.writeConn = this.openConnection();

            try (Statement s = this.writeConn.createStatement()) {
               s.execute("PRAGMA journal_mode=" + this.config.journalMode());
               s.execute("PRAGMA synchronous=" + this.config.synchronousMode());
               s.execute("PRAGMA busy_timeout=" + this.config.busyTimeoutMs());
               s.execute("PRAGMA cache_size=" + this.config.cachePages());
            }

            this.upserterFactory = selectDialect(this.writeConn, ctx.logger());
            SqliteSchema.ensureInitialized(this.writeConn, "phase1", this.config.tableNames());
         } catch (SQLException var13) {
            throw new BackendException("failed to initialise SQLite backend", var13);
         }
      }
   }

   @NotNull
   @Override
   public CheckCatalogPersistence checkCatalog() {
      return new JdbcCheckCatalogPersistence(this::openConnection, this.config.tableNames().checks());
   }

   @NotNull
   @Override
   public CheckCatalogRepairResult repairCheckCatalog(@NotNull Map<Integer, Integer> legacyToCatalogCheckIds, String introducedVersionReplacement) throws BackendException {
      try {
         synchronized (this.writeMutex) {
            return JdbcCheckCatalogRepair.run(
               this::openConnection,
               this.config.tableNames().checks(),
               this.config.tableNames().violations(),
               legacyToCatalogCheckIds,
               introducedVersionReplacement
            );
         }
      } catch (SQLException var6) {
         throw new BackendException("check catalog repair failed", var6);
      }
   }

   private Method pickHolderIfNewer(Logger log) {
      try {
         Class<?> api = Class.forName("dev.axionize.sqlite_jdbc.MinecraftSqliteJdbc", true, this.getClass().getClassLoader());
         String holderEngine = (String)api.getMethod("engineVersion").invoke(null);
         String bundledEngine = bundledEngineVersion();
         if (compareVersionString(holderEngine, bundledEngine) <= 0) {
            log.info(
               "[grim-datastore] minecraft-sqlite-jdbc holder present but not newer (holder="
                  + holderEngine
                  + ", bundled="
                  + bundledEngine
                  + "); using bundled driver"
            );
            return null;
         } else {
            Method connect = api.getMethod("connect", String.class);
            log.info(
               "[grim-datastore] minecraft-sqlite-jdbc holder is newer than bundled (holder="
                  + holderEngine
                  + ", bundled="
                  + bundledEngine
                  + "); routing JDBC through holder's child-first classloader"
            );
            return connect;
         }
      } catch (ClassNotFoundException var6) {
         return null;
      } catch (Throwable var7) {
         log.warning("[grim-datastore] holder probe failed (" + var7 + "); using bundled driver");
         return null;
      }
   }

   private static String bundledEngineVersion() {
      try {
         String var3;
         try (
            Connection c = DriverManager.getConnection("jdbc:sqlite::memory:");
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery("SELECT sqlite_version()");
         ) {
            var3 = rs.next() ? rs.getString(1) : "0.0.0";
         }

         return var3;
      } catch (Exception var11) {
         return "0.0.0";
      }
   }

   private static int compareVersionString(String a, String b) {
      int[] av = parseTriple(a);
      int[] bv = parseTriple(b);
      if (av[0] != bv[0]) {
         return Integer.compare(av[0], bv[0]);
      } else {
         return av[1] != bv[1] ? Integer.compare(av[1], bv[1]) : Integer.compare(av[2], bv[2]);
      }
   }

   private static int[] parseTriple(String version) {
      int[] triple = new int[]{0, 0, 0};
      int idx = 0;

      for (int i = 0; i < version.length() && idx < 3; i++) {
         int start = i;

         while (i < version.length() && Character.isDigit(version.charAt(i))) {
            i++;
         }

         if (i == start) {
            break;
         }

         triple[idx++] = Integer.parseInt(version.substring(start, i));
         if (i >= version.length() || version.charAt(i) != '.') {
            break;
         }
      }

      return triple;
   }

   @ApiStatus.Internal
   void overrideUpserterFactoryForTest(UpserterFactory factory) {
      this.upserterFactory = factory;
   }

   private static UpserterFactory selectDialect(Connection c, Logger log) throws SQLException {
      String version;
      try (
         Statement s = c.createStatement();
         ResultSet rs = s.executeQuery("SELECT sqlite_version()");
      ) {
         if (!rs.next()) {
            log.warning("[grim-datastore] sqlite_version() returned no row; assuming modern dialect");
            return UpserterFactory.MODERN;
         }

         version = rs.getString(1);
      }

      boolean legacy = compareVersionTriple(version, 3, 24, 0) < 0;
      log.info("[grim-datastore] SQLite engine " + version + " — using " + (legacy ? "legacy (pre-3.24, no UPSERT) dialect" : "modern dialect"));
      return legacy ? UpserterFactory.LEGACY : UpserterFactory.MODERN;
   }

   private static int compareVersionTriple(String version, int majorFloor, int minorFloor, int patchFloor) {
      int[] triple = new int[]{0, 0, 0};
      int idx = 0;

      for (int i = 0; i < version.length() && idx < 3; i++) {
         int start = i;

         while (i < version.length() && Character.isDigit(version.charAt(i))) {
            i++;
         }

         if (i == start) {
            break;
         }

         triple[idx++] = Integer.parseInt(version.substring(start, i));
         if (i >= version.length() || version.charAt(i) != '.') {
            break;
         }
      }

      if (triple[0] != majorFloor) {
         return Integer.compare(triple[0], majorFloor);
      } else {
         return triple[1] != minorFloor ? Integer.compare(triple[1], minorFloor) : Integer.compare(triple[2], patchFloor);
      }
   }

   private Connection openConnection() throws SQLException {
      Connection c;
      if (this.holderConnectMethod != null) {
         try {
            c = (Connection)this.holderConnectMethod.invoke(null, this.jdbcUrl);
         } catch (InvocationTargetException var7) {
            Throwable cause = var7.getCause();
            if (cause instanceof SQLException) {
               throw (SQLException)cause;
            }

            throw new SQLException("holder connect failed", (Throwable)(cause != null ? cause : var7));
         } catch (Exception var8) {
            throw new SQLException("holder connect failed", var8);
         }
      } else {
         c = DriverManager.getConnection(this.jdbcUrl);
      }

      try (Statement s = c.createStatement()) {
         s.execute("PRAGMA busy_timeout=" + this.config.busyTimeoutMs());
      }

      return c;
   }

   @Override
   public void flush() throws BackendException {
   }

   @Override
   public void close() throws BackendException {
      synchronized (this.handlers) {
         for (SqliteBackend.BatchingHandler<?> h : this.handlers) {
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
      SqliteBackend.BatchingHandler<?> h;
      if (cat == Categories.VIOLATION) {
         h = new SqliteBackend.ViolationHandler();
      } else if (cat == Categories.SESSION) {
         h = new SqliteBackend.SessionHandler();
      } else if (cat == Categories.PLAYER_IDENTITY) {
         h = new SqliteBackend.IdentityHandler();
      } else {
         if (cat != Categories.SETTING) {
            throw new IllegalArgumentException("unsupported category: " + cat.id());
         }

         h = new SqliteBackend.SettingHandler();
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
      this.writeRecordsDirect(cat, records);
   }

   public void writeRecordsDirect(Category<?> cat, List<?> records) throws BackendException {
      if (!records.isEmpty()) {
         synchronized (this.writeMutex) {
            if (this.writeConn == null) {
               throw new BackendException("backend not initialised");
            } else {
               try {
                  this.writeConn.setAutoCommit(false);
                  if (cat == Categories.VIOLATION) {
                     this.writeViolationRecords((List<ViolationRecord>)records);
                  } else if (cat == Categories.SESSION) {
                     this.writeSessionRecords((List<SessionRecord>)records);
                  } else if (cat == Categories.PLAYER_IDENTITY) {
                     this.writeIdentityRecords((List<PlayerIdentity>)records);
                  } else {
                     if (cat != Categories.SETTING) {
                        throw new BackendException("unsupported category: " + cat.id());
                     }

                     this.writeSettingRecords((List<SettingRecord>)records);
                  }

                  this.writeConn.commit();
               } catch (SQLException var17) {
                  try {
                     this.writeConn.rollback();
                  } catch (SQLException var16) {
                  }

                  throw new BackendException("writeRecordsDirect failed for " + cat.id(), var17);
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
      try (SessionUpserter u = this.upserterFactory.newSessionUpserter(this.writeConn, this.config.tableNames())) {
         for (SessionRecord s : rows) {
            u.addBatch(
               s.sessionId(),
               s.playerUuid(),
               s.serverName(),
               s.startedEpochMs(),
               s.lastActivityEpochMs(),
               s.closedAtEpochMs(),
               s.grimVersion(),
               s.clientBrand(),
               s.clientVersion(),
               s.serverVersionString(),
               s.sessionBlobs().isEmpty() ? "[]" : serializeSessionBlobsShim()
            );
         }

         u.executeBatch();
      }
   }

   private void writeIdentityRecords(List<PlayerIdentity> rows) throws SQLException {
      try (IdentityUpserter u = this.upserterFactory.newIdentityUpserter(this.writeConn, this.config.tableNames())) {
         for (PlayerIdentity id : rows) {
            u.addBatch(id.uuid(), id.currentName(), id.firstSeenEpochMs(), id.lastSeenEpochMs());
         }

         u.executeBatch();
      }
   }

   private void writeSettingRecords(List<SettingRecord> rows) throws SQLException {
      try (SettingsUpserter u = this.upserterFactory.newSettingsUpserter(this.writeConn, this.config.tableNames())) {
         for (SettingRecord s : rows) {
            u.addBatch(s.scope().name(), s.scopeKey(), s.key(), s.value(), s.updatedEpochMs());
         }

         u.executeBatch();
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
               + this.config.tableNames().sessions()
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
               + this.config.tableNames().sessions()
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
            "SELECT id, session_id, player_uuid, check_id, vl, occurred_at, verbose, verbose_format FROM "
               + this.config.tableNames().violations()
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
            "SELECT uuid, current_name, first_seen, last_seen FROM " + this.config.tableNames().players() + " WHERE uuid = ?"
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
               + this.config.tableNames().players()
               + " WHERE current_name_lower = ? ORDER BY last_seen DESC LIMIT 1"
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
                  + this.config.tableNames().players()
                  + " WHERE current_name_lower LIKE ? ESCAPE '\\' ORDER BY last_seen DESC LIMIT ?"
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
            "SELECT scope, scope_key, key, value, updated_at FROM " + this.config.tableNames().settings() + " WHERE scope = ? AND scope_key = ? AND key = ?"
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
                     this.execDelete("DELETE FROM " + t.violations() + " WHERE player_uuid = ?", uuid);
                  } else if (cat == Categories.SESSION) {
                     this.execDelete("DELETE FROM " + t.violations() + " WHERE player_uuid = ?", uuid);
                     this.execDelete("DELETE FROM " + t.sessions() + " WHERE player_uuid = ?", uuid);
                  } else if (cat == Categories.PLAYER_IDENTITY) {
                     this.execDelete("DELETE FROM " + t.players() + " WHERE uuid = ?", uuid);
                  } else {
                     if (cat != Categories.SETTING) {
                        throw new BackendException("unsupported category for delete: " + cat.id());
                     }

                     try (PreparedStatement ps = this.writeConn.prepareStatement("DELETE FROM " + t.settings() + " WHERE scope = 'PLAYER' AND scope_key = ?")) {
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
                              "DELETE FROM " + t.violations() + " WHERE session_id IN (SELECT session_id FROM " + t.sessions() + " WHERE started_at < ?)"
                           )) {
                        ps.setLong(1, cutoff);
                        ps.executeUpdate();
                     }

                     try (PreparedStatement ps = this.writeConn.prepareStatement("DELETE FROM " + t.sessions() + " WHERE started_at < ?")) {
                        ps.setLong(1, cutoff);
                        ps.executeUpdate();
                     }
                  } else {
                     if (cat != Categories.VIOLATION) {
                        throw new BackendException("unsupported category for retention: " + cat.id());
                     }

                     try (PreparedStatement ps = this.writeConn.prepareStatement("DELETE FROM " + t.violations() + " WHERE occurred_at < ?")) {
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
      try {
         long var5;
         try (
            Connection c = this.openConnection();
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM " + this.config.tableNames().violations() + " WHERE session_id = ?");
         ) {
            ps.setBytes(1, UuidCodec.toBytes(sessionId));

            try (ResultSet rs = ps.executeQuery()) {
               if (rs.next()) {
                  return rs.getLong(1);
               }

               var5 = 0L;
            }
         }

         return var5;
      } catch (SQLException var13) {
         throw new BackendException("countViolationsInSession failed", var13);
      }
   }

   @Override
   public long countUniqueChecksInSession(@NotNull UUID sessionId) throws BackendException {
      try {
         long var5;
         try (
            Connection c = this.openConnection();
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(DISTINCT check_id) FROM " + this.config.tableNames().violations() + " WHERE session_id = ?");
         ) {
            ps.setBytes(1, UuidCodec.toBytes(sessionId));

            try (ResultSet rs = ps.executeQuery()) {
               if (rs.next()) {
                  return rs.getLong(1);
               }

               var5 = 0L;
            }
         }

         return var5;
      } catch (SQLException var13) {
         throw new BackendException("countUniqueChecksInSession failed", var13);
      }
   }

   @Override
   public long countSessionsByPlayer(@NotNull UUID player) throws BackendException {
      try {
         long var5;
         try (
            Connection c = this.openConnection();
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM " + this.config.tableNames().sessions() + " WHERE player_uuid = ?");
         ) {
            ps.setBytes(1, UuidCodec.toBytes(player));

            try (ResultSet rs = ps.executeQuery()) {
               if (rs.next()) {
                  return rs.getLong(1);
               }

               var5 = 0L;
            }
         }

         return var5;
      } catch (SQLException var13) {
         throw new BackendException("countSessionsByPlayer failed", var13);
      }
   }

   @Override
   public long markCrashedSessions() throws BackendException {
      synchronized (this.writeMutex) {
         long var4;
         try (PreparedStatement ps = this.writeConn
               .prepareStatement("UPDATE " + this.config.tableNames().sessions() + " SET closed_at = last_activity WHERE closed_at IS NULL")) {
            int affected = ps.executeUpdate();
            if (!this.writeConn.getAutoCommit()) {
               this.writeConn.commit();
            }

            var4 = affected;
         } catch (SQLException var9) {
            throw new BackendException("markCrashedSessions failed", var9);
         }

         return var4;
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
   public Connection writeConnection() {
      return this.writeConn;
   }

   @ApiStatus.Internal
   public String jdbcUrl() {
      return this.jdbcUrl;
   }

   @ApiStatus.Internal
   public TableNames tableNames() {
      return this.config.tableNames();
   }

   @ApiStatus.Internal
   public Object writeMutexForCopier() {
      return this.writeMutex;
   }

   private abstract class BatchingHandler<E> implements StorageEventHandler<E> {
      protected Connection conn;
      private int pending;

      void start() throws BackendException {
         try {
            this.conn = SqliteBackend.this.openConnection();
            this.conn.setAutoCommit(false);
         } catch (SQLException var2) {
            throw new BackendException("init failed for " + this.categoryId(), var2);
         }
      }

      @Override
      public synchronized void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
         if (this.conn != null) {
            try {
               this.bindOne(event);
               this.pending++;
               if (endOfBatch || this.pending >= SqliteBackend.this.config.batchFlushCap()) {
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
            this.executeBatch();
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
            this.closeStmts();
         } catch (SQLException var2) {
         }

         this.pending = 0;
      }

      synchronized void shutDown() {
         try {
            if (this.pending > 0) {
               this.executeBatch();
               this.conn.commit();
            }
         } catch (SQLException var4) {
         }

         try {
            this.closeStmts();
         } catch (SQLException var3) {
         }

         try {
            if (this.conn != null) {
               this.conn.close();
            }
         } catch (SQLException var2) {
         }

         this.conn = null;
         this.pending = 0;
      }

      protected abstract String categoryId();

      protected abstract void bindOne(E var1) throws SQLException;

      protected abstract void executeBatch() throws SQLException;

      protected abstract void closeStmts() throws SQLException;
   }

   private final class IdentityHandler extends SqliteBackend.BatchingHandler<PlayerIdentityEvent> {
      private IdentityUpserter upserter;

      @Override
      protected String categoryId() {
         return "player-identity";
      }

      protected void bindOne(PlayerIdentityEvent e) throws SQLException {
         if (this.upserter == null) {
            this.upserter = SqliteBackend.this.upserterFactory.newIdentityUpserter(this.conn, SqliteBackend.this.config.tableNames());
         }

         this.upserter.addBatch(e.uuid(), e.currentName(), e.firstSeenEpochMs(), e.lastSeenEpochMs());
      }

      @Override
      protected void executeBatch() throws SQLException {
         if (this.upserter != null) {
            this.upserter.executeBatch();
         }
      }

      @Override
      protected void closeStmts() throws SQLException {
         if (this.upserter != null) {
            this.upserter.close();
            this.upserter = null;
         }
      }
   }

   private final class SessionHandler extends SqliteBackend.BatchingHandler<SessionEvent> {
      private SessionUpserter upserter;

      @Override
      protected String categoryId() {
         return "session";
      }

      protected void bindOne(SessionEvent s) throws SQLException {
         if (this.upserter == null) {
            this.upserter = SqliteBackend.this.upserterFactory.newSessionUpserter(this.conn, SqliteBackend.this.config.tableNames());
         }

         this.upserter
            .addBatch(
               s.sessionId(),
               s.playerUuid(),
               s.serverName(),
               s.startedEpochMs(),
               s.lastActivityEpochMs(),
               s.closedAtEpochMs(),
               s.grimVersion(),
               s.clientBrand(),
               s.clientVersion(),
               s.serverVersionString(),
               s.sessionBlobs().isEmpty() ? "[]" : SqliteBackend.serializeSessionBlobsShim()
            );
      }

      @Override
      protected void executeBatch() throws SQLException {
         if (this.upserter != null) {
            this.upserter.executeBatch();
         }
      }

      @Override
      protected void closeStmts() throws SQLException {
         if (this.upserter != null) {
            this.upserter.close();
            this.upserter = null;
         }
      }
   }

   private final class SettingHandler extends SqliteBackend.BatchingHandler<SettingEvent> {
      private SettingsUpserter upserter;

      @Override
      protected String categoryId() {
         return "setting";
      }

      protected void bindOne(SettingEvent s) throws SQLException {
         if (this.upserter == null) {
            this.upserter = SqliteBackend.this.upserterFactory.newSettingsUpserter(this.conn, SqliteBackend.this.config.tableNames());
         }

         this.upserter.addBatch(s.scope().name(), s.scopeKey(), s.key(), s.value(), s.updatedEpochMs());
      }

      @Override
      protected void executeBatch() throws SQLException {
         if (this.upserter != null) {
            this.upserter.executeBatch();
         }
      }

      @Override
      protected void closeStmts() throws SQLException {
         if (this.upserter != null) {
            this.upserter.close();
            this.upserter = null;
         }
      }
   }

   private final class ViolationHandler extends SqliteBackend.BatchingHandler<ViolationEvent> {
      private PreparedStatement stmt;

      @Override
      protected String categoryId() {
         return "violation";
      }

      protected void bindOne(ViolationEvent v) throws SQLException {
         if (this.stmt == null) {
            this.stmt = this.conn.prepareStatement(SqliteBackend.this.insertViolations);
         }

         UUID id = v.id();
         this.stmt.setBytes(1, UuidCodec.toBytes(id));
         this.stmt.setBytes(2, UuidCodec.toBytes(v.sessionId()));
         this.stmt.setBytes(3, UuidCodec.toBytes(v.playerUuid()));
         this.stmt.setInt(4, v.checkId());
         this.stmt.setDouble(5, v.vl());
         this.stmt.setLong(6, v.occurredEpochMs());
         this.stmt.setString(7, SqliteBackend.encodeVerbose(v.verboseData(), v.verboseFormat()));
         this.stmt.setInt(8, v.verboseFormat().code());
         this.stmt.addBatch();
      }

      @Override
      protected void executeBatch() throws SQLException {
         if (this.stmt != null) {
            this.stmt.executeBatch();
         }
      }

      @Override
      protected void closeStmts() throws SQLException {
         if (this.stmt != null) {
            this.stmt.close();
            this.stmt = null;
         }
      }
   }
}
