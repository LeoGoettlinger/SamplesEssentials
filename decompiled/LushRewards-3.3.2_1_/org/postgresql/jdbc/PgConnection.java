package org.postgresql.jdbc;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.security.Permission;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.ClientInfoStatus;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLPermission;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Condition;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.dataflow.qual.Pure;
import org.postgresql.Driver;
import org.postgresql.PGNotification;
import org.postgresql.PGProperty;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.BaseStatement;
import org.postgresql.core.CachedQuery;
import org.postgresql.core.ConnectionFactory;
import org.postgresql.core.Encoding;
import org.postgresql.core.Oid;
import org.postgresql.core.Query;
import org.postgresql.core.QueryExecutor;
import org.postgresql.core.ReplicationProtocol;
import org.postgresql.core.ResultHandlerBase;
import org.postgresql.core.ServerVersion;
import org.postgresql.core.SqlCommand;
import org.postgresql.core.TransactionState;
import org.postgresql.core.TypeInfo;
import org.postgresql.core.Utils;
import org.postgresql.core.Version;
import org.postgresql.fastpath.Fastpath;
import org.postgresql.geometric.PGbox;
import org.postgresql.geometric.PGcircle;
import org.postgresql.geometric.PGline;
import org.postgresql.geometric.PGlseg;
import org.postgresql.geometric.PGpath;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;
import org.postgresql.largeobject.LargeObjectManager;
import org.postgresql.replication.PGReplicationConnection;
import org.postgresql.replication.PGReplicationConnectionImpl;
import org.postgresql.util.GT;
import org.postgresql.util.HostSpec;
import org.postgresql.util.LazyCleaner;
import org.postgresql.util.LruCache;
import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGInterval;
import org.postgresql.util.PGmoney;
import org.postgresql.util.PGobject;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;
import org.postgresql.xml.DefaultPGXmlFactoryFactory;
import org.postgresql.xml.LegacyInsecurePGXmlFactoryFactory;
import org.postgresql.xml.PGXmlFactoryFactory;

public class PgConnection implements BaseConnection {
   private static final Logger LOGGER = Logger.getLogger(PgConnection.class.getName());
   private static final Set<Integer> SUPPORTED_BINARY_OIDS = getSupportedBinaryOids();
   private static final SQLPermission SQL_PERMISSION_ABORT = new SQLPermission("callAbort");
   private static final SQLPermission SQL_PERMISSION_NETWORK_TIMEOUT = new SQLPermission("setNetworkTimeout");
   @Nullable
   private static final MethodHandle SYSTEM_GET_SECURITY_MANAGER;
   @Nullable
   private static final MethodHandle SECURITY_MANAGER_CHECK_PERMISSION;
   private final ResourceLock lock = new ResourceLock();
   private final Condition lockCondition = this.lock.newCondition();
   private final Properties clientInfo;
   private final String creatingURL;
   private final PgConnection.ReadOnlyBehavior readOnlyBehavior;
   @Nullable
   private Throwable openStackTrace;
   private final PgConnectionCleaningAction finalizeAction;
   private final Object leakHandle = new Object();
   private final QueryExecutor queryExecutor;
   private final Query commitQuery;
   private final Query rollbackQuery;
   private final CachedQuery setSessionReadOnly;
   private final CachedQuery setSessionNotReadOnly;
   private final TypeInfo typeCache;
   private boolean disableColumnSanitiser;
   protected int prepareThreshold;
   protected int defaultFetchSize;
   protected boolean forcebinary;
   private final Set<? extends Integer> binaryDisabledOids;
   private int rsHoldability = 2;
   private int savepointId;
   private boolean autoCommit = true;
   private boolean readOnly;
   private final boolean hideUnprivilegedObjects;
   private final boolean logServerErrorDetail;
   private final boolean bindStringAsVarchar;
   @Nullable
   private SQLWarning firstWarning;
   private final boolean replicationConnection;
   private final LruCache<FieldMetadata.Key, FieldMetadata> fieldMetadataCache;
   @Nullable
   private final String xmlFactoryFactoryClass;
   @Nullable
   private PGXmlFactoryFactory xmlFactoryFactory;
   private final LazyCleaner.Cleanable<IOException> cleanable;
   private final TimestampUtils timestampUtils;
   protected Map<String, Class<?>> typemap = new HashMap<>();
   @Nullable
   private Fastpath fastpath;
   @Nullable
   private LargeObjectManager largeobject;
   @Nullable
   protected DatabaseMetaData metadata;
   @Nullable
   private CopyManager copyManager;

   final CachedQuery borrowQuery(String sql) throws SQLException {
      return this.queryExecutor.borrowQuery(sql);
   }

   final CachedQuery borrowCallableQuery(String sql) throws SQLException {
      return this.queryExecutor.borrowCallableQuery(sql);
   }

   private CachedQuery borrowReturningQuery(String sql, @Nullable String[] columnNames) throws SQLException {
      return this.queryExecutor.borrowReturningQuery(sql, columnNames);
   }

   @Override
   public CachedQuery createQuery(String sql, boolean escapeProcessing, boolean isParameterized, String... columnNames) throws SQLException {
      return this.queryExecutor.createQuery(sql, escapeProcessing, isParameterized, columnNames);
   }

   void releaseQuery(CachedQuery cachedQuery) {
      this.queryExecutor.releaseQuery(cachedQuery);
   }

   @Override
   public void setFlushCacheOnDeallocate(boolean flushCacheOnDeallocate) {
      this.queryExecutor.setFlushCacheOnDeallocate(flushCacheOnDeallocate);
      LOGGER.log(Level.FINE, "  setFlushCacheOnDeallocate = {0}", flushCacheOnDeallocate);
   }

   public PgConnection(HostSpec[] hostSpecs, Properties info, String url) throws SQLException {
      LOGGER.log(Level.FINE, "PostgreSQL JDBC Driver 42.7.3");
      this.creatingURL = url;
      this.readOnlyBehavior = getReadOnlyBehavior(PGProperty.READ_ONLY_MODE.getOrDefault(info));
      this.setDefaultFetchSize(PGProperty.DEFAULT_ROW_FETCH_SIZE.getInt(info));
      this.setPrepareThreshold(PGProperty.PREPARE_THRESHOLD.getInt(info));
      if (this.prepareThreshold == -1) {
         this.setForceBinary(true);
      }

      this.queryExecutor = ConnectionFactory.openConnection(hostSpecs, info);
      if (LOGGER.isLoggable(Level.WARNING) && !this.haveMinimumServerVersion(ServerVersion.v8_2)) {
         LOGGER.log(Level.WARNING, "Unsupported Server Version: {0}", this.queryExecutor.getServerVersion());
      }

      this.setSessionReadOnly = this.createQuery("SET SESSION CHARACTERISTICS AS TRANSACTION READ ONLY", false, true);
      this.setSessionNotReadOnly = this.createQuery("SET SESSION CHARACTERISTICS AS TRANSACTION READ WRITE", false, true);
      if (PGProperty.READ_ONLY.getBoolean(info)) {
         this.setReadOnly(true);
      }

      this.hideUnprivilegedObjects = PGProperty.HIDE_UNPRIVILEGED_OBJECTS.getBoolean(info);
      Set<Integer> binaryOids = getBinaryEnabledOids(info);
      this.binaryDisabledOids = getBinaryDisabledOids(info);
      if (!this.binaryDisabledOids.isEmpty()) {
         binaryOids.removeAll(this.binaryDisabledOids);
      }

      Set<Integer> useBinarySendForOids = new HashSet<>(binaryOids);
      Set<Integer> useBinaryReceiveForOids = new HashSet<>(binaryOids);
      useBinarySendForOids.remove(1082);
      this.queryExecutor.setBinaryReceiveOids(useBinaryReceiveForOids);
      this.queryExecutor.setBinarySendOids(useBinarySendForOids);
      if (LOGGER.isLoggable(Level.FINEST)) {
         LOGGER.log(Level.FINEST, "    types using binary send = {0}", this.oidsToString(useBinarySendForOids));
         LOGGER.log(Level.FINEST, "    types using binary receive = {0}", this.oidsToString(useBinaryReceiveForOids));
         LOGGER.log(Level.FINEST, "    integer date/time = {0}", this.queryExecutor.getIntegerDateTimes());
      }

      String stringType = PGProperty.STRING_TYPE.getOrDefault(info);
      if (stringType != null) {
         if ("unspecified".equalsIgnoreCase(stringType)) {
            this.bindStringAsVarchar = false;
         } else {
            if (!"varchar".equalsIgnoreCase(stringType)) {
               throw new PSQLException(GT.tr("Unsupported value for stringtype parameter: {0}", stringType), PSQLState.INVALID_PARAMETER_VALUE);
            }

            this.bindStringAsVarchar = true;
         }
      } else {
         this.bindStringAsVarchar = true;
      }

      this.timestampUtils = new TimestampUtils(!this.queryExecutor.getIntegerDateTimes(), new QueryExecutorTimeZoneProvider(this.queryExecutor));
      this.commitQuery = this.createQuery("COMMIT", false, true).query;
      this.rollbackQuery = this.createQuery("ROLLBACK", false, true).query;
      int unknownLength = PGProperty.UNKNOWN_LENGTH.getInt(info);
      TypeInfo typeCache = this.createTypeInfo(this, unknownLength);
      this.typeCache = typeCache;
      this.initObjectTypes(info);
      if (PGProperty.LOG_UNCLOSED_CONNECTIONS.getBoolean(info)) {
         this.openStackTrace = new Throwable("Connection was created at this point:");
      }

      this.finalizeAction = new PgConnectionCleaningAction(this.lock, this.openStackTrace, this.queryExecutor.getCloseAction());
      this.logServerErrorDetail = PGProperty.LOG_SERVER_ERROR_DETAIL.getBoolean(info);
      this.disableColumnSanitiser = PGProperty.DISABLE_COLUMN_SANITISER.getBoolean(info);
      if (this.haveMinimumServerVersion(ServerVersion.v8_3)) {
         typeCache.addCoreType("uuid", 2950, 1111, "java.util.UUID", 2951);
         typeCache.addCoreType("xml", 142, 2009, "java.sql.SQLXML", 143);
      }

      this.clientInfo = new Properties();
      if (this.haveMinimumServerVersion(ServerVersion.v9_0)) {
         String appName = PGProperty.APPLICATION_NAME.getOrDefault(info);
         if (appName == null) {
            appName = "";
         }

         this.clientInfo.put("ApplicationName", appName);
      }

      this.fieldMetadataCache = new LruCache<>(
         Math.max(0, PGProperty.DATABASE_METADATA_CACHE_FIELDS.getInt(info)),
         Math.max(0L, PGProperty.DATABASE_METADATA_CACHE_FIELDS_MIB.getInt(info) * 1024L * 1024L),
         false
      );
      this.replicationConnection = PGProperty.REPLICATION.getOrDefault(info) != null;
      this.xmlFactoryFactoryClass = PGProperty.XML_FACTORY_FACTORY.getOrDefault(info);
      this.cleanable = LazyCleaner.getInstance().register(this.leakHandle, this.finalizeAction);
   }

   private static PgConnection.ReadOnlyBehavior getReadOnlyBehavior(@Nullable String property) {
      if (property == null) {
         return PgConnection.ReadOnlyBehavior.transaction;
      } else {
         try {
            return PgConnection.ReadOnlyBehavior.valueOf(property);
         } catch (IllegalArgumentException var4) {
            try {
               return PgConnection.ReadOnlyBehavior.valueOf(property.toLowerCase(Locale.US));
            } catch (IllegalArgumentException var3) {
               return PgConnection.ReadOnlyBehavior.transaction;
            }
         }
      }
   }

   private static Set<Integer> getSupportedBinaryOids() {
      return new HashSet<>(
         Arrays.asList(17, 21, 23, 20, 700, 701, 1700, 1083, 1082, 1266, 1114, 1184, 1001, 1005, 1007, 1016, 1028, 1021, 1022, 1015, 1009, 600, 603, 2950)
      );
   }

   private static Set<Integer> getBinaryEnabledOids(Properties info) throws PSQLException {
      boolean binaryTransfer = PGProperty.BINARY_TRANSFER.getBoolean(info);
      Set<Integer> binaryOids = new HashSet<>(32);
      if (binaryTransfer) {
         binaryOids.addAll(SUPPORTED_BINARY_OIDS);
      }

      String oids = PGProperty.BINARY_TRANSFER_ENABLE.getOrDefault(info);
      if (oids != null) {
         binaryOids.addAll(getOidSet(oids));
      }

      return binaryOids;
   }

   private static Set<? extends Integer> getBinaryDisabledOids(Properties info) throws PSQLException {
      String oids = PGProperty.BINARY_TRANSFER_DISABLE.getOrDefault(info);
      return oids == null ? Collections.emptySet() : getOidSet(oids);
   }

   private static Set<? extends Integer> getOidSet(String oidList) throws PSQLException {
      if (oidList.isEmpty()) {
         return Collections.emptySet();
      } else {
         Set<Integer> oids = new HashSet<>();
         StringTokenizer tokenizer = new StringTokenizer(oidList, ",");

         while (tokenizer.hasMoreTokens()) {
            String oid = tokenizer.nextToken();
            oids.add(Oid.valueOf(oid));
         }

         return oids;
      }
   }

   private String oidsToString(Set<Integer> oids) {
      StringBuilder sb = new StringBuilder();

      for (Integer oid : oids) {
         sb.append(Oid.toString(oid));
         sb.append(',');
      }

      if (sb.length() > 0) {
         sb.setLength(sb.length() - 1);
      } else {
         sb.append(" <none>");
      }

      return sb.toString();
   }

   @Deprecated
   @Override
   public TimestampUtils getTimestampUtils() {
      return this.timestampUtils;
   }

   final ResourceLock obtainLock() {
      return this.lock.obtain();
   }

   final Condition lockCondition() {
      return this.lockCondition;
   }

   @Override
   public Statement createStatement() throws SQLException {
      return this.createStatement(1003, 1007);
   }

   @Override
   public PreparedStatement prepareStatement(String sql) throws SQLException {
      return this.prepareStatement(sql, 1003, 1007);
   }

   @Override
   public CallableStatement prepareCall(String sql) throws SQLException {
      return this.prepareCall(sql, 1003, 1007);
   }

   @Override
   public Map<String, Class<?>> getTypeMap() throws SQLException {
      this.checkClosed();
      return this.typemap;
   }

   @Override
   public QueryExecutor getQueryExecutor() {
      return this.queryExecutor;
   }

   @Override
   public ReplicationProtocol getReplicationProtocol() {
      return this.queryExecutor.getReplicationProtocol();
   }

   public void addWarning(SQLWarning warn) {
      if (this.firstWarning != null) {
         this.firstWarning.setNextWarning(warn);
      } else {
         this.firstWarning = warn;
      }
   }

   @Override
   public ResultSet execSQLQuery(String s) throws SQLException {
      return this.execSQLQuery(s, 1003, 1007);
   }

   @Override
   public ResultSet execSQLQuery(String s, int resultSetType, int resultSetConcurrency) throws SQLException {
      BaseStatement stat = (BaseStatement)this.createStatement(resultSetType, resultSetConcurrency);
      boolean hasResultSet = stat.executeWithFlags(s, 16);

      while (!hasResultSet && stat.getUpdateCount() != -1) {
         hasResultSet = stat.getMoreResults();
      }

      if (!hasResultSet) {
         throw new PSQLException(GT.tr("No results were returned by the query."), PSQLState.NO_DATA);
      } else {
         SQLWarning warnings = stat.getWarnings();
         if (warnings != null) {
            this.addWarning(warnings);
         }

         return Nullness.castNonNull(stat.getResultSet(), "hasResultSet==true, yet getResultSet()==null");
      }
   }

   @Override
   public void execSQLUpdate(String s) throws SQLException {
      BaseStatement stmt = (BaseStatement)this.createStatement();

      try {
         if (stmt.executeWithFlags(s, 22)) {
            throw new PSQLException(GT.tr("A result was returned when none was expected."), PSQLState.TOO_MANY_RESULTS);
         }

         SQLWarning warnings = stmt.getWarnings();
         if (warnings != null) {
            this.addWarning(warnings);
         }
      } catch (Throwable var6) {
         if (stmt != null) {
            try {
               stmt.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (stmt != null) {
         stmt.close();
      }
   }

   void execSQLUpdate(CachedQuery query) throws SQLException {
      BaseStatement stmt = (BaseStatement)this.createStatement();

      try {
         if (stmt.executeWithFlags(query, 22)) {
            throw new PSQLException(GT.tr("A result was returned when none was expected."), PSQLState.TOO_MANY_RESULTS);
         }

         SQLWarning warnings = stmt.getWarnings();
         if (warnings != null) {
            this.addWarning(warnings);
         }
      } catch (Throwable var6) {
         if (stmt != null) {
            try {
               stmt.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (stmt != null) {
         stmt.close();
      }
   }

   public void setCursorName(String cursor) throws SQLException {
      this.checkClosed();
   }

   @Nullable
   public String getCursorName() throws SQLException {
      this.checkClosed();
      return null;
   }

   public String getURL() throws SQLException {
      return this.creatingURL;
   }

   public String getUserName() throws SQLException {
      return this.queryExecutor.getUser();
   }

   @Override
   public Fastpath getFastpathAPI() throws SQLException {
      this.checkClosed();
      if (this.fastpath == null) {
         this.fastpath = new Fastpath(this);
      }

      return this.fastpath;
   }

   @Override
   public LargeObjectManager getLargeObjectAPI() throws SQLException {
      this.checkClosed();
      if (this.largeobject == null) {
         this.largeobject = new LargeObjectManager(this);
      }

      return this.largeobject;
   }

   @Override
   public Object getObject(String type, @Nullable String value, @Nullable byte[] byteValue) throws SQLException {
      if (this.typemap != null) {
         Class<?> c = this.typemap.get(type);
         if (c != null) {
            throw new PSQLException(GT.tr("Custom type maps are not supported."), PSQLState.NOT_IMPLEMENTED);
         }
      }

      PGobject obj = null;
      if (LOGGER.isLoggable(Level.FINEST)) {
         LOGGER.log(Level.FINEST, "Constructing object from type={0} value=<{1}>", new Object[]{type, value});
      }

      try {
         Class<? extends PGobject> klass = this.typeCache.getPGobject(type);
         if (klass != null) {
            obj = klass.getDeclaredConstructor().newInstance();
            obj.setType(type);
            if (byteValue != null && obj instanceof PGBinaryObject) {
               PGBinaryObject binObj = (PGBinaryObject)obj;
               binObj.setByteValue(byteValue, 0);
            } else {
               obj.setValue(value);
            }
         } else {
            obj = new PGobject();
            obj.setType(type);
            obj.setValue(value);
         }

         return obj;
      } catch (SQLException var7) {
         throw var7;
      } catch (Exception var8) {
         throw new PSQLException(GT.tr("Failed to create object for: {0}.", type), PSQLState.CONNECTION_FAILURE, var8);
      }
   }

   protected TypeInfo createTypeInfo(BaseConnection conn, int unknownLength) {
      return new TypeInfoCache(conn, unknownLength);
   }

   @Override
   public TypeInfo getTypeInfo() {
      return this.typeCache;
   }

   @Override
   public void addDataType(String type, String name) {
      try {
         this.addDataType(type, Class.forName(name).asSubclass(PGobject.class));
      } catch (Exception var4) {
         throw new RuntimeException("Cannot register new type " + type, var4);
      }
   }

   @Override
   public void addDataType(String type, Class<? extends PGobject> klass) throws SQLException {
      this.checkClosed();
      this.typeCache.addDataType(type, klass);
      if (PGBinaryObject.class.isAssignableFrom(klass) && this.getPreferQueryMode() != PreferQueryMode.SIMPLE) {
         int oid = this.typeCache.getPGType(type);
         if (oid > 0 && !this.binaryDisabledOids.contains(oid)) {
            this.queryExecutor.addBinaryReceiveOid(oid);
            this.queryExecutor.addBinarySendOid(oid);
         }
      }
   }

   private void initObjectTypes(Properties info) throws SQLException {
      this.addDataType("box", PGbox.class);
      this.addDataType("circle", PGcircle.class);
      this.addDataType("line", PGline.class);
      this.addDataType("lseg", PGlseg.class);
      this.addDataType("path", PGpath.class);
      this.addDataType("point", PGpoint.class);
      this.addDataType("polygon", PGpolygon.class);
      this.addDataType("money", PGmoney.class);
      this.addDataType("interval", PGInterval.class);
      Enumeration<?> e = info.propertyNames();

      while (e.hasMoreElements()) {
         String propertyName = (String)e.nextElement();
         if (propertyName != null && propertyName.startsWith("datatype.")) {
            String typeName = propertyName.substring(9);
            String className = Nullness.castNonNull(info.getProperty(propertyName));

            Class<?> klass;
            try {
               klass = Class.forName(className);
            } catch (ClassNotFoundException var8) {
               throw new PSQLException(
                  GT.tr("Unable to load the class {0} responsible for the datatype {1}", className, typeName), PSQLState.SYSTEM_ERROR, var8
               );
            }

            this.addDataType(typeName, klass.asSubclass(PGobject.class));
         }
      }
   }

   @Override
   public void close() throws SQLException {
      if (this.queryExecutor != null) {
         this.openStackTrace = null;

         try {
            this.cleanable.clean();
         } catch (IOException var2) {
            throw new PSQLException(GT.tr("Unable to close connection properly"), PSQLState.UNKNOWN_STATE, var2);
         }
      }
   }

   @Override
   public String nativeSQL(String sql) throws SQLException {
      this.checkClosed();
      CachedQuery cachedQuery = this.queryExecutor.createQuery(sql, false, true);
      return cachedQuery.query.getNativeSql();
   }

   @Nullable
   @Override
   public SQLWarning getWarnings() throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      SQLWarning var3;
      try {
         this.checkClosed();
         SQLWarning newWarnings = this.queryExecutor.getWarnings();
         if (this.firstWarning == null) {
            this.firstWarning = newWarnings;
         } else if (newWarnings != null) {
            this.firstWarning.setNextWarning(newWarnings);
         }

         var3 = this.firstWarning;
      } catch (Throwable var5) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var3;
   }

   @Override
   public void clearWarnings() throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      try {
         this.checkClosed();
         this.queryExecutor.getWarnings();
         this.firstWarning = null;
      } catch (Throwable var5) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (ignore != null) {
         ignore.close();
      }
   }

   @Override
   public void setReadOnly(boolean readOnly) throws SQLException {
      this.checkClosed();
      if (this.queryExecutor.getTransactionState() != TransactionState.IDLE) {
         throw new PSQLException(GT.tr("Cannot change transaction read-only property in the middle of a transaction."), PSQLState.ACTIVE_SQL_TRANSACTION);
      } else {
         if (readOnly != this.readOnly && this.autoCommit && this.readOnlyBehavior == PgConnection.ReadOnlyBehavior.always) {
            this.execSQLUpdate(readOnly ? this.setSessionReadOnly : this.setSessionNotReadOnly);
         }

         this.readOnly = readOnly;
         LOGGER.log(Level.FINE, "  setReadOnly = {0}", readOnly);
      }
   }

   @Override
   public boolean isReadOnly() throws SQLException {
      this.checkClosed();
      return this.readOnly;
   }

   @Override
   public boolean hintReadOnly() {
      return this.readOnly && this.readOnlyBehavior != PgConnection.ReadOnlyBehavior.ignore;
   }

   @Override
   public void setAutoCommit(boolean autoCommit) throws SQLException {
      this.checkClosed();
      if (this.autoCommit != autoCommit) {
         if (!this.autoCommit) {
            this.commit();
         }

         if (this.readOnly && this.readOnlyBehavior == PgConnection.ReadOnlyBehavior.always) {
            if (autoCommit) {
               this.autoCommit = true;
               this.execSQLUpdate(this.setSessionReadOnly);
            } else {
               this.execSQLUpdate(this.setSessionNotReadOnly);
            }
         }

         this.autoCommit = autoCommit;
         LOGGER.log(Level.FINE, "  setAutoCommit = {0}", autoCommit);
      }
   }

   @Override
   public boolean getAutoCommit() throws SQLException {
      this.checkClosed();
      return this.autoCommit;
   }

   private void executeTransactionCommand(Query query) throws SQLException {
      int flags = 22;
      if (this.prepareThreshold == 0) {
         flags |= 1;
      }

      try {
         this.getQueryExecutor().execute(query, null, new PgConnection.TransactionCommandHandler(), 0, 0, flags);
      } catch (SQLException var4) {
         if (query.getSubqueries() != null || !this.queryExecutor.willHealOnRetry(var4)) {
            throw var4;
         }

         query.close();
         this.getQueryExecutor().execute(query, null, new PgConnection.TransactionCommandHandler(), 0, 0, flags);
      }
   }

   @Override
   public void commit() throws SQLException {
      this.checkClosed();
      if (this.autoCommit) {
         throw new PSQLException(GT.tr("Cannot commit when autoCommit is enabled."), PSQLState.NO_ACTIVE_SQL_TRANSACTION);
      } else {
         if (this.queryExecutor.getTransactionState() != TransactionState.IDLE) {
            this.executeTransactionCommand(this.commitQuery);
         }
      }
   }

   protected void checkClosed() throws SQLException {
      if (this.isClosed()) {
         throw new PSQLException(GT.tr("This connection has been closed."), PSQLState.CONNECTION_DOES_NOT_EXIST);
      }
   }

   @Override
   public void rollback() throws SQLException {
      this.checkClosed();
      if (this.autoCommit) {
         throw new PSQLException(GT.tr("Cannot rollback when autoCommit is enabled."), PSQLState.NO_ACTIVE_SQL_TRANSACTION);
      } else {
         if (this.queryExecutor.getTransactionState() != TransactionState.IDLE) {
            this.executeTransactionCommand(this.rollbackQuery);
         } else {
            LOGGER.log(Level.FINE, "Rollback requested but no transaction in progress");
         }
      }
   }

   @Override
   public TransactionState getTransactionState() {
      return this.queryExecutor.getTransactionState();
   }

   @Override
   public int getTransactionIsolation() throws SQLException {
      this.checkClosed();
      String level = null;
      ResultSet rs = this.execSQLQuery("SHOW TRANSACTION ISOLATION LEVEL");
      if (rs.next()) {
         level = rs.getString(1);
      }

      rs.close();
      if (level == null) {
         return 2;
      } else {
         level = level.toUpperCase(Locale.US);
         if ("READ COMMITTED".equals(level)) {
            return 2;
         } else if ("READ UNCOMMITTED".equals(level)) {
            return 1;
         } else if ("REPEATABLE READ".equals(level)) {
            return 4;
         } else {
            return "SERIALIZABLE".equals(level) ? 8 : 2;
         }
      }
   }

   @Override
   public void setTransactionIsolation(int level) throws SQLException {
      this.checkClosed();
      if (this.queryExecutor.getTransactionState() != TransactionState.IDLE) {
         throw new PSQLException(GT.tr("Cannot change transaction isolation level in the middle of a transaction."), PSQLState.ACTIVE_SQL_TRANSACTION);
      } else {
         String isolationLevelName = this.getIsolationLevelName(level);
         if (isolationLevelName == null) {
            throw new PSQLException(GT.tr("Transaction isolation level {0} not supported.", level), PSQLState.NOT_IMPLEMENTED);
         } else {
            String isolationLevelSQL = "SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL " + isolationLevelName;
            this.execSQLUpdate(isolationLevelSQL);
            LOGGER.log(Level.FINE, "  setTransactionIsolation = {0}", isolationLevelName);
         }
      }
   }

   @Nullable
   protected String getIsolationLevelName(int level) {
      switch (level) {
         case 1:
            return "READ UNCOMMITTED";
         case 2:
            return "READ COMMITTED";
         case 3:
         case 5:
         case 6:
         case 7:
         default:
            return null;
         case 4:
            return "REPEATABLE READ";
         case 8:
            return "SERIALIZABLE";
      }
   }

   @Override
   public void setCatalog(String catalog) throws SQLException {
      this.checkClosed();
   }

   @Override
   public String getCatalog() throws SQLException {
      this.checkClosed();
      return this.queryExecutor.getDatabase();
   }

   public boolean getHideUnprivilegedObjects() {
      return this.hideUnprivilegedObjects;
   }

   public String getDBVersionNumber() {
      return this.queryExecutor.getServerVersion();
   }

   public int getServerMajorVersion() {
      try {
         StringTokenizer versionTokens = new StringTokenizer(this.queryExecutor.getServerVersion(), ".");
         return integerPart(versionTokens.nextToken());
      } catch (NoSuchElementException var2) {
         return 0;
      }
   }

   public int getServerMinorVersion() {
      try {
         StringTokenizer versionTokens = new StringTokenizer(this.queryExecutor.getServerVersion(), ".");
         versionTokens.nextToken();
         return integerPart(versionTokens.nextToken());
      } catch (NoSuchElementException var2) {
         return 0;
      }
   }

   @Override
   public boolean haveMinimumServerVersion(int ver) {
      return this.queryExecutor.getServerVersionNum() >= ver;
   }

   @Override
   public boolean haveMinimumServerVersion(Version ver) {
      return this.haveMinimumServerVersion(ver.getVersionNum());
   }

   @Pure
   @Override
   public Encoding getEncoding() {
      return this.queryExecutor.getEncoding();
   }

   @PolyNull
   @Override
   public byte[] encodeString(@PolyNull String str) throws SQLException {
      try {
         return this.getEncoding().encode(str);
      } catch (IOException var3) {
         throw new PSQLException(GT.tr("Unable to translate data into the desired encoding."), PSQLState.DATA_ERROR, var3);
      }
   }

   @Override
   public String escapeString(String str) throws SQLException {
      return Utils.escapeLiteral(null, str, this.queryExecutor.getStandardConformingStrings()).toString();
   }

   @Override
   public boolean getStandardConformingStrings() {
      return this.queryExecutor.getStandardConformingStrings();
   }

   @Override
   public boolean isClosed() throws SQLException {
      return this.queryExecutor.isClosed();
   }

   @Override
   public void cancelQuery() throws SQLException {
      this.checkClosed();
      this.queryExecutor.sendQueryCancel();
   }

   @Override
   public PGNotification[] getNotifications() throws SQLException {
      return this.getNotifications(-1);
   }

   @Override
   public PGNotification[] getNotifications(int timeoutMillis) throws SQLException {
      this.checkClosed();
      this.getQueryExecutor().processNotifies(timeoutMillis);
      return this.queryExecutor.getNotifications();
   }

   @Override
   public int getPrepareThreshold() {
      return this.prepareThreshold;
   }

   @Override
   public void setDefaultFetchSize(int fetchSize) throws SQLException {
      if (fetchSize < 0) {
         throw new PSQLException(GT.tr("Fetch size must be a value greater to or equal to 0."), PSQLState.INVALID_PARAMETER_VALUE);
      } else {
         this.defaultFetchSize = fetchSize;
         LOGGER.log(Level.FINE, "  setDefaultFetchSize = {0}", fetchSize);
      }
   }

   @Override
   public int getDefaultFetchSize() {
      return this.defaultFetchSize;
   }

   @Override
   public void setPrepareThreshold(int newThreshold) {
      this.prepareThreshold = newThreshold;
      LOGGER.log(Level.FINE, "  setPrepareThreshold = {0}", newThreshold);
   }

   public boolean getForceBinary() {
      return this.forcebinary;
   }

   public void setForceBinary(boolean newValue) {
      this.forcebinary = newValue;
      LOGGER.log(Level.FINE, "  setForceBinary = {0}", newValue);
   }

   public void setTypeMapImpl(Map<String, Class<?>> map) throws SQLException {
      this.typemap = map;
   }

   @Override
   public Logger getLogger() {
      return LOGGER;
   }

   public int getProtocolVersion() {
      return this.queryExecutor.getProtocolVersion();
   }

   @Override
   public boolean getStringVarcharFlag() {
      return this.bindStringAsVarchar;
   }

   @Override
   public CopyManager getCopyAPI() throws SQLException {
      this.checkClosed();
      if (this.copyManager == null) {
         this.copyManager = new CopyManager(this);
      }

      return this.copyManager;
   }

   @Override
   public boolean binaryTransferSend(int oid) {
      return this.queryExecutor.useBinaryForSend(oid);
   }

   @Override
   public int getBackendPID() {
      return this.queryExecutor.getBackendPID();
   }

   @Override
   public boolean isColumnSanitiserDisabled() {
      return this.disableColumnSanitiser;
   }

   public void setDisableColumnSanitiser(boolean disableColumnSanitiser) {
      this.disableColumnSanitiser = disableColumnSanitiser;
      LOGGER.log(Level.FINE, "  setDisableColumnSanitiser = {0}", disableColumnSanitiser);
   }

   @Override
   public PreferQueryMode getPreferQueryMode() {
      return this.queryExecutor.getPreferQueryMode();
   }

   @Override
   public AutoSave getAutosave() {
      return this.queryExecutor.getAutoSave();
   }

   @Override
   public void setAutosave(AutoSave autoSave) {
      this.queryExecutor.setAutoSave(autoSave);
      LOGGER.log(Level.FINE, "  setAutosave = {0}", autoSave.value());
   }

   protected void abort() {
      this.queryExecutor.abort();
   }

   private Timer getTimer() {
      return this.finalizeAction.getTimer();
   }

   @Override
   public void addTimerTask(TimerTask timerTask, long milliSeconds) {
      Timer timer = this.getTimer();
      timer.schedule(timerTask, milliSeconds);
   }

   @Override
   public void purgeTimerTasks() {
      this.finalizeAction.purgeTimerTasks();
   }

   @Override
   public String escapeIdentifier(String identifier) throws SQLException {
      return Utils.escapeIdentifier(null, identifier).toString();
   }

   @Override
   public String escapeLiteral(String literal) throws SQLException {
      return Utils.escapeLiteral(null, literal, this.queryExecutor.getStandardConformingStrings()).toString();
   }

   @Override
   public LruCache<FieldMetadata.Key, FieldMetadata> getFieldMetadataCache() {
      return this.fieldMetadataCache;
   }

   @Override
   public PGReplicationConnection getReplicationAPI() {
      return new PGReplicationConnectionImpl(this);
   }

   private static int integerPart(String dirtyString) {
      int start = 0;

      while (start < dirtyString.length() && !Character.isDigit(dirtyString.charAt(start))) {
         start++;
      }

      int end = start;

      while (end < dirtyString.length() && Character.isDigit(dirtyString.charAt(end))) {
         end++;
      }

      return start == end ? 0 : Integer.parseInt(dirtyString.substring(start, end));
   }

   @Override
   public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      this.checkClosed();
      return new PgStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
   }

   @Override
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      this.checkClosed();
      return new PgPreparedStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
   }

   @Override
   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      this.checkClosed();
      return new PgCallableStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
   }

   @Override
   public DatabaseMetaData getMetaData() throws SQLException {
      this.checkClosed();
      if (this.metadata == null) {
         this.metadata = new PgDatabaseMetaData(this);
      }

      return this.metadata;
   }

   @Override
   public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
      this.setTypeMapImpl(map);
      LOGGER.log(Level.FINE, "  setTypeMap = {0}", map);
   }

   protected Array makeArray(int oid, @Nullable String fieldString) throws SQLException {
      return new PgArray(this, oid, fieldString);
   }

   protected Blob makeBlob(long oid) throws SQLException {
      return new PgBlob(this, oid);
   }

   protected Clob makeClob(long oid) throws SQLException {
      return new PgClob(this, oid);
   }

   protected SQLXML makeSQLXML() throws SQLException {
      return new PgSQLXML(this);
   }

   @Override
   public Clob createClob() throws SQLException {
      this.checkClosed();
      throw Driver.notImplemented(this.getClass(), "createClob()");
   }

   @Override
   public Blob createBlob() throws SQLException {
      this.checkClosed();
      throw Driver.notImplemented(this.getClass(), "createBlob()");
   }

   @Override
   public NClob createNClob() throws SQLException {
      this.checkClosed();
      throw Driver.notImplemented(this.getClass(), "createNClob()");
   }

   @Override
   public SQLXML createSQLXML() throws SQLException {
      this.checkClosed();
      return this.makeSQLXML();
   }

   @Override
   public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
      this.checkClosed();
      throw Driver.notImplemented(this.getClass(), "createStruct(String, Object[])");
   }

   @Override
   public Array createArrayOf(String typeName, @Nullable Object elements) throws SQLException {
      this.checkClosed();
      TypeInfo typeInfo = this.getTypeInfo();
      int oid = typeInfo.getPGArrayType(typeName);
      char delim = typeInfo.getArrayDelimiter(oid);
      if (oid == 0) {
         throw new PSQLException(GT.tr("Unable to find server array type for provided name {0}.", typeName), PSQLState.INVALID_NAME);
      } else if (elements == null) {
         return this.makeArray(oid, null);
      } else {
         ArrayEncoding.ArrayEncoder arraySupport = ArrayEncoding.getArrayEncoder(elements);
         if (arraySupport.supportBinaryRepresentation(oid) && this.getPreferQueryMode() != PreferQueryMode.SIMPLE) {
            return new PgArray(this, oid, arraySupport.toBinaryRepresentation(this, elements, oid));
         } else {
            String arrayString = arraySupport.toArrayString(delim, elements);
            return this.makeArray(oid, arrayString);
         }
      }
   }

   @Override
   public Array createArrayOf(String typeName, @Nullable Object[] elements) throws SQLException {
      return this.createArrayOf(typeName, elements);
   }

   @Override
   public boolean isValid(int timeout) throws SQLException {
      if (timeout < 0) {
         throw new PSQLException(GT.tr("Invalid timeout ({0}<0).", timeout), PSQLState.INVALID_PARAMETER_VALUE);
      } else if (this.isClosed()) {
         return false;
      } else {
         boolean changedNetworkTimeout = false;

         try {
            int oldNetworkTimeout = this.getNetworkTimeout();
            int newNetworkTimeout = (int)Math.min(timeout * 1000L, 2147483647L);

            boolean var22;
            try {
               if (newNetworkTimeout != 0 && (oldNetworkTimeout == 0 || newNetworkTimeout < oldNetworkTimeout)) {
                  changedNetworkTimeout = true;
                  this.setNetworkTimeout(null, newNetworkTimeout);
               }

               if (this.replicationConnection) {
                  Statement statement = this.createStatement();

                  try {
                     statement.execute("IDENTIFY_SYSTEM");
                  } catch (Throwable var18) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var16) {
                           var18.addSuppressed(var16);
                        }
                     }

                     throw var18;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               } else {
                  Statement checkConnectionQuery = this.createStatement();

                  try {
                     ((PgStatement)checkConnectionQuery).execute("", 1024);
                  } catch (Throwable var17) {
                     if (checkConnectionQuery != null) {
                        try {
                           checkConnectionQuery.close();
                        } catch (Throwable var15) {
                           var17.addSuppressed(var15);
                        }
                     }

                     throw var17;
                  }

                  if (checkConnectionQuery != null) {
                     checkConnectionQuery.close();
                  }
               }

               var22 = true;
            } finally {
               if (changedNetworkTimeout) {
                  this.setNetworkTimeout(null, oldNetworkTimeout);
               }
            }

            return var22;
         } catch (SQLException var20) {
            if (PSQLState.IN_FAILED_SQL_TRANSACTION.getState().equals(var20.getSQLState())) {
               return true;
            } else {
               LOGGER.log(Level.FINE, GT.tr("Validating connection."), (Throwable)var20);
               return false;
            }
         }
      }
   }

   @Override
   public void setClientInfo(String name, @Nullable String value) throws SQLClientInfoException {
      try {
         this.checkClosed();
      } catch (SQLException var7) {
         Map<String, ClientInfoStatus> failures = new HashMap<>();
         failures.put(name, ClientInfoStatus.REASON_UNKNOWN);
         throw new SQLClientInfoException(GT.tr("This connection has been closed."), failures, var7);
      }

      if (this.haveMinimumServerVersion(ServerVersion.v9_0) && "ApplicationName".equals(name)) {
         if (value == null) {
            value = "";
         }

         String oldValue = this.queryExecutor.getApplicationName();
         if (!value.equals(oldValue)) {
            try {
               StringBuilder sql = new StringBuilder("SET application_name = '");
               Utils.escapeLiteral(sql, value, this.getStandardConformingStrings());
               sql.append("'");
               this.execSQLUpdate(sql.toString());
            } catch (SQLException var6) {
               Map<String, ClientInfoStatus> failures = new HashMap<>();
               failures.put(name, ClientInfoStatus.REASON_UNKNOWN);
               throw new SQLClientInfoException(GT.tr("Failed to set ClientInfo property: {0}", "ApplicationName"), var6.getSQLState(), failures, var6);
            }

            if (LOGGER.isLoggable(Level.FINE)) {
               LOGGER.log(Level.FINE, "  setClientInfo = {0} {1}", new Object[]{name, value});
            }

            this.clientInfo.put(name, value);
         }
      } else {
         this.addWarning(new SQLWarning(GT.tr("ClientInfo property not supported."), PSQLState.NOT_IMPLEMENTED.getState()));
      }
   }

   @Override
   public void setClientInfo(Properties properties) throws SQLClientInfoException {
      try {
         this.checkClosed();
      } catch (SQLException var9) {
         Map<String, ClientInfoStatus> failures = new HashMap<>();

         for (Entry<Object, Object> e : properties.entrySet()) {
            failures.put((String)e.getKey(), ClientInfoStatus.REASON_UNKNOWN);
         }

         throw new SQLClientInfoException(GT.tr("This connection has been closed."), failures, var9);
      }

      Map<String, ClientInfoStatus> failures = new HashMap<>();

      for (String name : new String[]{"ApplicationName"}) {
         try {
            this.setClientInfo(name, properties.getProperty(name, null));
         } catch (SQLClientInfoException var8) {
            failures.putAll(var8.getFailedProperties());
         }
      }

      if (!failures.isEmpty()) {
         throw new SQLClientInfoException(GT.tr("One or more ClientInfo failed."), PSQLState.NOT_IMPLEMENTED.getState(), failures);
      }
   }

   @Nullable
   @Override
   public String getClientInfo(String name) throws SQLException {
      this.checkClosed();
      this.clientInfo.put("ApplicationName", this.queryExecutor.getApplicationName());
      return this.clientInfo.getProperty(name);
   }

   @Override
   public Properties getClientInfo() throws SQLException {
      this.checkClosed();
      this.clientInfo.put("ApplicationName", this.queryExecutor.getApplicationName());
      return this.clientInfo;
   }

   public <T> T createQueryObject(Class<T> ifc) throws SQLException {
      this.checkClosed();
      throw Driver.notImplemented(this.getClass(), "createQueryObject(Class<T>)");
   }

   @Override
   public boolean getLogServerErrorDetail() {
      return this.logServerErrorDetail;
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      this.checkClosed();
      return iface.isAssignableFrom(this.getClass());
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      this.checkClosed();
      if (iface.isAssignableFrom(this.getClass())) {
         return iface.cast(this);
      } else {
         throw new SQLException("Cannot unwrap to " + iface.getName());
      }
   }

   @Nullable
   @Override
   public String getSchema() throws SQLException {
      this.checkClosed();
      Statement stmt = this.createStatement();

      String var3;
      label73: {
         try {
            ResultSet rs = stmt.executeQuery("select current_schema()");

            label75: {
               try {
                  if (rs.next()) {
                     var3 = rs.getString(1);
                     break label75;
                  }

                  var3 = null;
               } catch (Throwable var7) {
                  if (rs != null) {
                     try {
                        rs.close();
                     } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                     }
                  }

                  throw var7;
               }

               if (rs != null) {
                  rs.close();
               }
               break label73;
            }

            if (rs != null) {
               rs.close();
            }
         } catch (Throwable var8) {
            if (stmt != null) {
               try {
                  stmt.close();
               } catch (Throwable var5) {
                  var8.addSuppressed(var5);
               }
            }

            throw var8;
         }

         if (stmt != null) {
            stmt.close();
         }

         return var3;
      }

      if (stmt != null) {
         stmt.close();
      }

      return var3;
   }

   @Override
   public void setSchema(@Nullable String schema) throws SQLException {
      this.checkClosed();
      Statement stmt = this.createStatement();

      try {
         if (schema == null) {
            stmt.executeUpdate("SET SESSION search_path TO DEFAULT");
         } else {
            StringBuilder sb = new StringBuilder();
            sb.append("SET SESSION search_path TO '");
            Utils.escapeLiteral(sb, schema, this.getStandardConformingStrings());
            sb.append("'");
            stmt.executeUpdate(sb.toString());
            LOGGER.log(Level.FINE, "  setSchema = {0}", schema);
         }
      } catch (Throwable var6) {
         if (stmt != null) {
            try {
               stmt.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (stmt != null) {
         stmt.close();
      }
   }

   @Override
   public void abort(Executor executor) throws SQLException {
      if (executor == null) {
         throw new SQLException("executor is null");
      } else if (!this.isClosed()) {
         SQL_PERMISSION_ABORT.checkGuard(this);
         PgConnection.AbortCommand command = new PgConnection.AbortCommand();
         executor.execute(command);
      }
   }

   @Override
   public void setNetworkTimeout(@Nullable Executor executor, int milliseconds) throws SQLException {
      this.checkClosed();
      if (milliseconds < 0) {
         throw new PSQLException(GT.tr("Network timeout must be a value greater than or equal to 0."), PSQLState.INVALID_PARAMETER_VALUE);
      } else {
         this.checkPermission(SQL_PERMISSION_NETWORK_TIMEOUT);

         try {
            this.queryExecutor.setNetworkTimeout(milliseconds);
         } catch (IOException var4) {
            throw new PSQLException(GT.tr("Unable to set network timeout."), PSQLState.COMMUNICATION_ERROR, var4);
         }
      }
   }

   private void checkPermission(SQLPermission sqlPermissionNetworkTimeout) {
      if (SYSTEM_GET_SECURITY_MANAGER != null && SECURITY_MANAGER_CHECK_PERMISSION != null) {
         try {
            Object securityManager = (Object)SYSTEM_GET_SECURITY_MANAGER.invoke();
            if (securityManager != null) {
               SECURITY_MANAGER_CHECK_PERMISSION.invoke((Object)securityManager, (SQLPermission)sqlPermissionNetworkTimeout);
            }
         } catch (Throwable var3) {
            throw new RuntimeException(var3);
         }
      }
   }

   @Override
   public int getNetworkTimeout() throws SQLException {
      this.checkClosed();

      try {
         return this.queryExecutor.getNetworkTimeout();
      } catch (IOException var2) {
         throw new PSQLException(GT.tr("Unable to get network timeout."), PSQLState.COMMUNICATION_ERROR, var2);
      }
   }

   @Override
   public void setHoldability(int holdability) throws SQLException {
      this.checkClosed();
      switch (holdability) {
         case 1:
         case 2:
            this.rsHoldability = holdability;
            LOGGER.log(Level.FINE, "  setHoldability = {0}", holdability);
            return;
         default:
            throw new PSQLException(GT.tr("Unknown ResultSet holdability setting: {0}.", holdability), PSQLState.INVALID_PARAMETER_VALUE);
      }
   }

   @Override
   public int getHoldability() throws SQLException {
      this.checkClosed();
      return this.rsHoldability;
   }

   @Override
   public Savepoint setSavepoint() throws SQLException {
      this.checkClosed();
      if (this.getAutoCommit()) {
         throw new PSQLException(GT.tr("Cannot establish a savepoint in auto-commit mode."), PSQLState.NO_ACTIVE_SQL_TRANSACTION);
      } else {
         PSQLSavepoint savepoint = new PSQLSavepoint(this.savepointId++);
         String pgName = savepoint.getPGName();
         Statement stmt = this.createStatement();
         stmt.executeUpdate("SAVEPOINT " + pgName);
         stmt.close();
         return savepoint;
      }
   }

   @Override
   public Savepoint setSavepoint(String name) throws SQLException {
      this.checkClosed();
      if (this.getAutoCommit()) {
         throw new PSQLException(GT.tr("Cannot establish a savepoint in auto-commit mode."), PSQLState.NO_ACTIVE_SQL_TRANSACTION);
      } else {
         PSQLSavepoint savepoint = new PSQLSavepoint(name);
         Statement stmt = this.createStatement();
         stmt.executeUpdate("SAVEPOINT " + savepoint.getPGName());
         stmt.close();
         return savepoint;
      }
   }

   @Override
   public void rollback(Savepoint savepoint) throws SQLException {
      this.checkClosed();
      PSQLSavepoint pgSavepoint = (PSQLSavepoint)savepoint;
      this.execSQLUpdate("ROLLBACK TO SAVEPOINT " + pgSavepoint.getPGName());
   }

   @Override
   public void releaseSavepoint(Savepoint savepoint) throws SQLException {
      this.checkClosed();
      PSQLSavepoint pgSavepoint = (PSQLSavepoint)savepoint;
      this.execSQLUpdate("RELEASE SAVEPOINT " + pgSavepoint.getPGName());
      pgSavepoint.invalidate();
   }

   @Override
   public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
      this.checkClosed();
      return this.createStatement(resultSetType, resultSetConcurrency, this.getHoldability());
   }

   @Override
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      this.checkClosed();
      return this.prepareStatement(sql, resultSetType, resultSetConcurrency, this.getHoldability());
   }

   @Override
   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      this.checkClosed();
      return this.prepareCall(sql, resultSetType, resultSetConcurrency, this.getHoldability());
   }

   @Override
   public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
      return autoGeneratedKeys != 1 ? this.prepareStatement(sql) : this.prepareStatement(sql, (String[])null);
   }

   @Override
   public PreparedStatement prepareStatement(String sql, @Nullable int[] columnIndexes) throws SQLException {
      if (columnIndexes != null && columnIndexes.length == 0) {
         return this.prepareStatement(sql);
      } else {
         this.checkClosed();
         throw new PSQLException(GT.tr("Returning autogenerated keys is not supported."), PSQLState.NOT_IMPLEMENTED);
      }
   }

   @Override
   public PreparedStatement prepareStatement(String sql, @Nullable String[] columnNames) throws SQLException {
      if (columnNames != null && columnNames.length == 0) {
         return this.prepareStatement(sql);
      } else {
         CachedQuery cachedQuery = this.borrowReturningQuery(sql, columnNames);
         PgPreparedStatement ps = new PgPreparedStatement(this, cachedQuery, 1003, 1007, this.getHoldability());
         Query query = cachedQuery.query;
         SqlCommand sqlCommand = query.getSqlCommand();
         if (sqlCommand != null) {
            ps.wantsGeneratedKeysAlways = sqlCommand.isReturningKeywordPresent();
         }

         return ps;
      }
   }

   @Override
   public final Map<String, String> getParameterStatuses() {
      return this.queryExecutor.getParameterStatuses();
   }

   @Nullable
   @Override
   public final String getParameterStatus(String parameterName) {
      return this.queryExecutor.getParameterStatus(parameterName);
   }

   @Override
   public boolean getAdaptiveFetch() {
      return this.queryExecutor.getAdaptiveFetch();
   }

   @Override
   public void setAdaptiveFetch(boolean adaptiveFetch) {
      this.queryExecutor.setAdaptiveFetch(adaptiveFetch);
   }

   @Override
   public PGXmlFactoryFactory getXmlFactoryFactory() throws SQLException {
      PGXmlFactoryFactory xmlFactoryFactory = this.xmlFactoryFactory;
      if (xmlFactoryFactory != null) {
         return xmlFactoryFactory;
      } else {
         if (this.xmlFactoryFactoryClass == null || "".equals(this.xmlFactoryFactoryClass)) {
            xmlFactoryFactory = DefaultPGXmlFactoryFactory.INSTANCE;
         } else if ("LEGACY_INSECURE".equals(this.xmlFactoryFactoryClass)) {
            xmlFactoryFactory = LegacyInsecurePGXmlFactoryFactory.INSTANCE;
         } else {
            Class<?> clazz;
            try {
               clazz = Class.forName(this.xmlFactoryFactoryClass);
            } catch (ClassNotFoundException var5) {
               throw new PSQLException(
                  GT.tr("Could not instantiate xmlFactoryFactory: {0}", this.xmlFactoryFactoryClass), PSQLState.INVALID_PARAMETER_VALUE, var5
               );
            }

            if (!clazz.isAssignableFrom(PGXmlFactoryFactory.class)) {
               throw new PSQLException(
                  GT.tr("Connection property xmlFactoryFactory must implement PGXmlFactoryFactory: {0}", this.xmlFactoryFactoryClass),
                  PSQLState.INVALID_PARAMETER_VALUE
               );
            }

            try {
               xmlFactoryFactory = clazz.asSubclass(PGXmlFactoryFactory.class).getDeclaredConstructor().newInstance();
            } catch (Exception var4) {
               throw new PSQLException(
                  GT.tr("Could not instantiate xmlFactoryFactory: {0}", this.xmlFactoryFactoryClass), PSQLState.INVALID_PARAMETER_VALUE, var4
               );
            }
         }

         this.xmlFactoryFactory = xmlFactoryFactory;
         return xmlFactoryFactory;
      }
   }

   static {
      MethodHandle systemGetSecurityManagerHandle = null;
      MethodHandle securityManagerCheckPermission = null;

      try {
         Class<?> securityManagerClass = Class.forName("java.lang.SecurityManager");
         systemGetSecurityManagerHandle = MethodHandles.lookup().findStatic(System.class, "getSecurityManager", MethodType.methodType(securityManagerClass));
         securityManagerCheckPermission = MethodHandles.lookup()
            .findVirtual(securityManagerClass, "checkPermission", MethodType.methodType(void.class, Permission.class));
      } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException var3) {
      }

      SYSTEM_GET_SECURITY_MANAGER = systemGetSecurityManagerHandle;
      SECURITY_MANAGER_CHECK_PERMISSION = securityManagerCheckPermission;
   }

   public class AbortCommand implements Runnable {
      @Override
      public void run() {
         PgConnection.this.abort();
      }
   }

   private static enum ReadOnlyBehavior {
      ignore,
      transaction,
      always;
   }

   private class TransactionCommandHandler extends ResultHandlerBase {
      private TransactionCommandHandler() {
      }

      @Override
      public void handleCompletion() throws SQLException {
         SQLWarning warning = this.getWarning();
         if (warning != null) {
            PgConnection.this.addWarning(warning);
         }

         super.handleCompletion();
      }
   }
}
