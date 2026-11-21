package ac.grim.grimac.internal.storage.backend.mysql;

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
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
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@ApiStatus.Internal
public final class MysqlBackend implements Backend {
   public static final String ID = "mysql";
   private static final long CONNECTION_VALIDATE_AFTER_IDLE_MS = TimeUnit.SECONDS.toMillis(30L);
   private static final int CONNECTION_VALIDATION_TIMEOUT_SECONDS = 2;
   private final MysqlBackendConfig config;
   private final Object writeMutex = new Object();
   private final List<MysqlBackend.BatchingHandler<?>> handlers = new ArrayList<>();
   private final String insertViolations;
   private String upsertSessions;
   private String upsertIdentities;
   private String upsertSettings;
   private MysqlDialect dialect;
   private Connection writeConn;
   private Method holderConnectMethod;

   public MysqlBackend(MysqlBackendConfig config) {
      this.config = config;
      TableNames t = config.tableNames();
      this.insertViolations = "INSERT INTO "
         + t.violations()
         + "(id, session_id, player_uuid, check_id, vl, occurred_at, verbose, verbose_format) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
   }

   @NotNull
   @Override
   public String id() {
      return "mysql";
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
         Class.forName("com.mysql.cj.jdbc.Driver");
      } catch (ClassNotFoundException var8) {
         throw new BackendException("mysql-connector-j not on the classpath — either shade it into the plugin jar or drop it into server/plugins", var8);
      }

      this.holderConnectMethod = this.pickHolderIfInstalled(ctx.logger());
      synchronized (this.writeMutex) {
         try {
            this.writeConn = this.openConnection();
            this.dialect = selectDialect(this.writeConn, ctx.logger());
            TableNames t = this.config.tableNames();
            this.upsertSessions = this.dialect.upsertSessions(t);
            this.upsertIdentities = this.dialect.upsertIdentities(t);
            this.upsertSettings = this.dialect.upsertSettings(t);
            MysqlSchema.ensureInitialized(this.writeConn, "phase1", t, this.dialect);
         } catch (BackendException var5) {
            throw var5;
         } catch (SQLException var6) {
            throw new BackendException("failed to initialise MySQL backend", var6);
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
         TableNames t = this.config.tableNames();
         return JdbcCheckCatalogRepair.run(this::openConnection, t.checks(), t.violations(), legacyToCatalogCheckIds, introducedVersionReplacement);
      } catch (SQLException var4) {
         throw new BackendException("check catalog repair failed", var4);
      }
   }

   static MysqlDialect selectDialect(Connection c, Logger log) throws SQLException, BackendException {
      String version;
      try (
         Statement s = c.createStatement();
         ResultSet rs = s.executeQuery("SELECT VERSION()");
      ) {
         version = rs.next() ? rs.getString(1) : "";
      }

      boolean mariaDb = version.contains("MariaDB");
      if (!mariaDb) {
         log.info("[grim-datastore] MySQL engine " + version + " — using MySQL 8 dialect");
         return new MysqlEightDialect();
      } else {
         String triplePart = version.startsWith("5.5.5-") ? version.substring("5.5.5-".length()) : version;
         int dash = triplePart.indexOf(45);
         if (dash > 0) {
            triplePart = triplePart.substring(0, dash);
         }

         int[] triple = parseTriple(triplePart);
         int major = triple[0];
         int minor = triple[1];
         if (major >= 10 && (major != 10 || minor >= 6)) {
            log.info("[grim-datastore] MariaDB engine " + version + " — using MariaDB dialect");
            return new MariaDbDialect();
         } else {
            throw new BackendException(
               "MariaDB "
                  + version
                  + " is too old; minimum supported is 10.6 (STORED generated columns require 10.2.1, 10.6 is the oldest LTS still in upstream support). Upgrade MariaDB or point Grim at a different storage backend."
            );
         }
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

   private Method pickHolderIfInstalled(Logger log) {
      try {
         Class<?> api = Class.forName("dev.axionize.mysql_jdbc.MinecraftMysqlJdbc", true, this.getClass().getClassLoader());
         String holderVersion = (String)api.getMethod("driverVersion").invoke(null);
         Method connect = api.getMethod("connect", String.class, Properties.class);
         log.info("[grim-datastore] minecraft-mysql-jdbc holder detected (driver " + holderVersion + "); routing JDBC through holder's child-first classloader");
         return connect;
      } catch (ClassNotFoundException var5) {
         return null;
      } catch (Throwable var6) {
         log.warning("[grim-datastore] mysql holder probe failed (" + var6 + "); using bundled driver");
         return null;
      }
   }

   private Connection openConnection() throws SQLException {
      Properties props = new Properties();
      props.setProperty("user", this.config.user());
      props.setProperty("password", this.config.password() == null ? "" : this.config.password());
      if (this.holderConnectMethod != null) {
         try {
            return (Connection)this.holderConnectMethod.invoke(null, this.config.jdbcUrl(), props);
         } catch (InvocationTargetException var4) {
            Throwable cause = var4.getCause();
            if (cause instanceof SQLException) {
               throw (SQLException)cause;
            } else {
               throw new SQLException("mysql holder connect failed", (Throwable)(cause != null ? cause : var4));
            }
         } catch (Exception var5) {
            throw new SQLException("mysql holder connect failed", var5);
         }
      } else {
         return DriverManager.getConnection(this.config.jdbcUrl(), this.config.user(), this.config.password() == null ? "" : this.config.password());
      }
   }

   private Connection ensureWriteConnectionLocked() throws SQLException {
      if (isConnectionUsable(this.writeConn, 0L)) {
         return this.writeConn;
      } else {
         closeQuietly(this.writeConn);
         this.writeConn = this.openConnection();
         return this.writeConn;
      }
   }

   private static boolean isConnectionUsable(Connection connection, long lastUseMs) {
      if (connection == null) {
         return false;
      } else {
         try {
            if (connection.isClosed()) {
               return false;
            } else {
               long now = System.currentTimeMillis();
               return lastUseMs > 0L && now - lastUseMs < CONNECTION_VALIDATE_AFTER_IDLE_MS ? true : connection.isValid(2);
            }
         } catch (SQLException var5) {
            return false;
         }
      }
   }

   private static boolean isConnectionFailure(SQLException e) {
      for (Throwable t = e; t != null; t = t.getCause()) {
         if (t instanceof SQLException sql) {
            String state = sql.getSQLState();
            if (state != null && state.startsWith("08")) {
               return true;
            }
         }

         String className = t.getClass().getName();
         if (className.contains("ConnectionIsClosed") || className.contains("CJCommunicationsException")) {
            return true;
         }
      }

      return false;
   }

   private static void closeQuietly(AutoCloseable closeable) {
      if (closeable != null) {
         try {
            closeable.close();
         } catch (Exception var2) {
         }
      }
   }

   @Override
   public void flush() {
   }

   @Override
   public void close() throws BackendException {
      synchronized (this.handlers) {
         for (MysqlBackend.BatchingHandler<?> h : this.handlers) {
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
      MysqlBackend.BatchingHandler<?> h;
      if (cat == Categories.VIOLATION) {
         h = new MysqlBackend.ViolationHandler();
      } else if (cat == Categories.SESSION) {
         h = new MysqlBackend.SessionHandler();
      } else if (cat == Categories.PLAYER_IDENTITY) {
         h = new MysqlBackend.IdentityHandler();
      } else {
         if (cat != Categories.SETTING) {
            throw new IllegalArgumentException("unsupported category: " + cat.id());
         }

         h = new MysqlBackend.SettingHandler();
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
            try {
               this.ensureWriteConnectionLocked();
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
                  if (this.writeConn != null) {
                     this.writeConn.rollback();
                  }
               } catch (SQLException var16) {
               }

               if (isConnectionFailure(var17)) {
                  closeQuietly(this.writeConn);
                  this.writeConn = null;
               }

               throw new BackendException("bulkImport failed for " + cat.id(), var17);
            } finally {
               try {
                  if (this.writeConn != null) {
                     this.writeConn.setAutoCommit(true);
                  }
               } catch (SQLException var15) {
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
      try (PreparedStatement ps = c.prepareStatement(this.dialect.selectIdentityByName(this.config.tableNames()))) {
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
         try (PreparedStatement ps = c.prepareStatement(this.dialect.selectIdentitiesByNamePrefix(this.config.tableNames()))) {
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
            "SELECT scope, scope_key, `key`, value, updated_at FROM "
               + this.config.tableNames().settings()
               + " WHERE scope = ? AND scope_key = ? AND `key` = ?"
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
         try {
            this.ensureWriteConnectionLocked();
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
                           "DELETE v FROM " + t.violations() + " v JOIN " + t.sessions() + " s ON s.session_id = v.session_id WHERE s.started_at < ?"
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
               if (this.writeConn != null) {
                  this.writeConn.rollback();
               }
            } catch (SQLException var30) {
            }

            if (isConnectionFailure(var39)) {
               closeQuietly(this.writeConn);
               this.writeConn = null;
            }

            throw new BackendException("delete failed", var39);
         } finally {
            try {
               if (this.writeConn != null) {
                  this.writeConn.setAutoCommit(true);
               }
            } catch (SQLException var29) {
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
         "SELECT COUNT(*) FROM " + this.config.tableNames().violations() + " WHERE session_id = ?", UuidCodec.toBytes(sessionId), "countViolationsInSession"
      );
   }

   @Override
   public long countUniqueChecksInSession(@NotNull UUID sessionId) throws BackendException {
      return this.scalarLong(
         "SELECT COUNT(DISTINCT check_id) FROM " + this.config.tableNames().violations() + " WHERE session_id = ?",
         UuidCodec.toBytes(sessionId),
         "countUniqueChecksInSession"
      );
   }

   @Override
   public long countSessionsByPlayer(@NotNull UUID player) throws BackendException {
      return this.scalarLong(
         "SELECT COUNT(*) FROM " + this.config.tableNames().sessions() + " WHERE player_uuid = ?", UuidCodec.toBytes(player), "countSessionsByPlayer"
      );
   }

   @Override
   public long markCrashedSessions() throws BackendException {
      try {
         long var3;
         try (
            Connection c = this.openConnection();
            PreparedStatement ps = c.prepareStatement(
               "UPDATE " + this.config.tableNames().sessions() + " SET closed_at = last_activity WHERE closed_at IS NULL"
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

   @ApiStatus.Internal
   MysqlDialect dialectForTest() {
      return this.dialect;
   }

   private abstract class BatchingHandler<E> implements StorageEventHandler<E> {
      private Connection conn;
      private PreparedStatement stmt;
      private final List<MysqlBackend.PendingWrite> pending = new ArrayList<>();
      private long lastConnectionUseMs;

      void start() throws BackendException {
         try {
            this.reconnectLocked();
         } catch (SQLException var2) {
            throw new BackendException("init failed for " + this.categoryId(), var2);
         }
      }

      @Override
      public synchronized void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
         try {
            this.pending.add(this.snapshot(event));
            if (endOfBatch || this.pending.size() >= MysqlBackend.this.config.batchFlushCap()) {
               this.flushLocked();
            }
         } catch (SQLException var6) {
            this.pending.clear();
            throw new BackendException(this.categoryId() + " write failed", var6);
         }
      }

      private void flushLocked() throws SQLException {
         if (!this.pending.isEmpty()) {
            try {
               this.executePendingLocked();
               this.pending.clear();
            } catch (SQLException var4) {
               this.abortLocked(var4);
               if (!MysqlBackend.isConnectionFailure(var4)) {
                  this.pending.clear();
                  throw var4;
               }

               this.reconnectLocked();

               try {
                  this.executePendingLocked();
                  this.pending.clear();
               } catch (SQLException var3) {
                  this.abortLocked(var3);
                  this.pending.clear();
                  throw var3;
               }
            }
         }
      }

      private void executePendingLocked() throws SQLException {
         this.ensureConnectionLocked();
         if (this.stmt == null) {
            this.stmt = this.conn.prepareStatement(this.sql());
         }

         for (MysqlBackend.PendingWrite write : this.pending) {
            write.bind(this.stmt);
            this.stmt.addBatch();
         }

         this.stmt.executeBatch();
         this.conn.commit();
         this.lastConnectionUseMs = System.currentTimeMillis();
      }

      private void abortLocked(SQLException cause) {
         try {
            if (this.conn != null) {
               this.conn.rollback();
            }
         } catch (SQLException var3) {
         }

         MysqlBackend.closeQuietly(this.stmt);
         this.stmt = null;
         if (MysqlBackend.isConnectionFailure(cause)) {
            MysqlBackend.closeQuietly(this.conn);
            this.conn = null;
            this.lastConnectionUseMs = 0L;
         }
      }

      private void ensureConnectionLocked() throws SQLException {
         if (!MysqlBackend.isConnectionUsable(this.conn, this.lastConnectionUseMs)) {
            this.reconnectLocked();
         }
      }

      private void reconnectLocked() throws SQLException {
         MysqlBackend.closeQuietly(this.stmt);
         MysqlBackend.closeQuietly(this.conn);
         this.stmt = null;
         this.conn = MysqlBackend.this.openConnection();
         this.conn.setAutoCommit(false);
         this.lastConnectionUseMs = System.currentTimeMillis();
      }

      synchronized void shutDown() {
         try {
            this.flushLocked();
         } catch (SQLException var2) {
         }

         MysqlBackend.closeQuietly(this.stmt);
         MysqlBackend.closeQuietly(this.conn);
         this.stmt = null;
         this.conn = null;
         this.pending.clear();
         this.lastConnectionUseMs = 0L;
      }

      protected abstract String sql();

      protected abstract String categoryId();

      protected abstract MysqlBackend.PendingWrite snapshot(E var1);
   }

   private final class IdentityHandler extends MysqlBackend.BatchingHandler<PlayerIdentityEvent> {
      @Override
      protected String sql() {
         return MysqlBackend.this.upsertIdentities;
      }

      @Override
      protected String categoryId() {
         return "player-identity";
      }

      protected MysqlBackend.PendingWrite snapshot(PlayerIdentityEvent e) {
         UUID uuid = e.uuid();
         String currentName = e.currentName();
         long firstSeenEpochMs = e.firstSeenEpochMs();
         long lastSeenEpochMs = e.lastSeenEpochMs();
         return ps -> {
            ps.setBytes(1, UuidCodec.toBytes(uuid));
            ps.setString(2, currentName);
            ps.setLong(3, firstSeenEpochMs);
            ps.setLong(4, lastSeenEpochMs);
         };
      }
   }

   @FunctionalInterface
   private interface PendingWrite {
      void bind(PreparedStatement var1) throws SQLException;
   }

   private final class SessionHandler extends MysqlBackend.BatchingHandler<SessionEvent> {
      @Override
      protected String sql() {
         return MysqlBackend.this.upsertSessions;
      }

      @Override
      protected String categoryId() {
         return "session";
      }

      protected MysqlBackend.PendingWrite snapshot(SessionEvent s) {
         UUID sessionId = s.sessionId();
         UUID playerUuid = s.playerUuid();
         String serverName = s.serverName();
         long startedEpochMs = s.startedEpochMs();
         long lastActivityEpochMs = s.lastActivityEpochMs();
         long closedAtEpochMs = s.closedAtEpochMs();
         boolean isClosed = s.isClosed();
         String grimVersion = s.grimVersion();
         String clientBrand = s.clientBrand();
         int clientVersion = s.clientVersion();
         String serverVersionString = s.serverVersionString();
         boolean hasSessionBlobs = !s.sessionBlobs().isEmpty();
         return ps -> {
            ps.setBytes(1, UuidCodec.toBytes(sessionId));
            ps.setBytes(2, UuidCodec.toBytes(playerUuid));
            ps.setString(3, serverName);
            ps.setLong(4, startedEpochMs);
            ps.setLong(5, lastActivityEpochMs);
            if (!isClosed) {
               ps.setNull(6, -5);
            } else {
               ps.setLong(6, closedAtEpochMs);
            }

            ps.setString(7, grimVersion);
            ps.setString(8, clientBrand);
            ps.setInt(9, clientVersion);
            ps.setString(10, serverVersionString);
            ps.setString(11, hasSessionBlobs ? MysqlBackend.serializeSessionBlobsShim() : "[]");
         };
      }
   }

   private final class SettingHandler extends MysqlBackend.BatchingHandler<SettingEvent> {
      @Override
      protected String sql() {
         return MysqlBackend.this.upsertSettings;
      }

      @Override
      protected String categoryId() {
         return "setting";
      }

      protected MysqlBackend.PendingWrite snapshot(SettingEvent s) {
         String scope = s.scope().name();
         String scopeKey = s.scopeKey();
         String key = s.key();
         byte[] value = (byte[])s.value().clone();
         long updatedEpochMs = s.updatedEpochMs();
         return ps -> {
            ps.setString(1, scope);
            ps.setString(2, scopeKey);
            ps.setString(3, key);
            ps.setBytes(4, value);
            ps.setLong(5, updatedEpochMs);
         };
      }
   }

   private final class ViolationHandler extends MysqlBackend.BatchingHandler<ViolationEvent> {
      @Override
      protected String sql() {
         return MysqlBackend.this.insertViolations;
      }

      @Override
      protected String categoryId() {
         return "violation";
      }

      protected MysqlBackend.PendingWrite snapshot(ViolationEvent v) {
         UUID id = v.id();
         UUID sessionId = v.sessionId();
         UUID playerUuid = v.playerUuid();
         int checkId = v.checkId();
         double vl = v.vl();
         long occurredEpochMs = v.occurredEpochMs();
         byte[] verboseData = v.verboseData();
         int verboseFormat = v.verboseFormat().code();
         return ps -> {
            ps.setBytes(1, UuidCodec.toBytes(id));
            ps.setBytes(2, UuidCodec.toBytes(sessionId));
            ps.setBytes(3, UuidCodec.toBytes(playerUuid));
            ps.setInt(4, checkId);
            ps.setDouble(5, vl);
            ps.setLong(6, occurredEpochMs);
            ps.setString(7, MysqlBackend.encodeVerbose(verboseData, VerboseFormat.fromCode(verboseFormat)));
            ps.setInt(8, verboseFormat);
         };
      }
   }
}
