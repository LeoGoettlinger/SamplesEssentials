package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.sql.Array;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLPermission;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.CacheAdapter;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.CacheAdapterFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.LicenseConfiguration;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.NativeSession;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.NoSubInterceptorWrapper;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.PreparedQuery;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.QueryInfo;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.ServerVersion;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Session;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.HostInfo;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyDefinitions;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.RuntimeProperty;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJCommunicationsException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionInterceptor;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionInterceptorChain;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.PasswordExpiredException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.UnableToConnectException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.interceptors.QueryInterceptor;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.SQLError;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.ha.MultiHostConnectionProxy;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.ha.MultiHostMySQLConnection;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.interceptors.ConnectionLifecycleInterceptor;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.CachedResultSetMetaData;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.CachedResultSetMetaDataImpl;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.ResultSetFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.ResultSetInternalMethods;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.UpdatableResultSet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ServerSessionStateController;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.SocksProxySocketFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.NativeProtocol;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryAttribute;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryScope;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetrySpan;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetrySpanName;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.LRUCache;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.StringUtils;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.Util;

public class ConnectionImpl implements JdbcConnection, Session.SessionEventListener, Serializable {
   private static final long serialVersionUID = 4009476458425101761L;
   private static final SQLPermission SET_NETWORK_TIMEOUT_PERM = new SQLPermission("setNetworkTimeout");
   private static final SQLPermission ABORT_PERM = new SQLPermission("abort");
   private JdbcConnection parentProxy = null;
   private JdbcConnection topProxy = null;
   private MultiHostConnectionProxy realProxy = null;
   private final Lock lock = new ReentrantLock();
   private static Map<String, Integer> mapTransIsolationNameToValue = null;
   protected static Map<?, ?> roundRobinStatsMap;
   private List<ConnectionLifecycleInterceptor> connectionLifecycleInterceptors;
   private static final int DEFAULT_RESULT_SET_TYPE = 1003;
   private static final int DEFAULT_RESULT_SET_CONCURRENCY = 1007;
   private CacheAdapter<String, QueryInfo> queryInfoCache;
   private String database = null;
   private java.sql.DatabaseMetaData dbmd = null;
   private NativeSession session = null;
   private boolean isInGlobalTx = false;
   private int isolationLevel = 2;
   private final CopyOnWriteArrayList<JdbcStatement> openStatements = new CopyOnWriteArrayList<>();
   private LRUCache<ConnectionImpl.CompoundCacheKey, CallableStatement.CallableStatementParamInfo> parsedCallableStatementCache;
   private final Lock parsedCallableStatementCacheLock = new ReentrantLock();
   private String password = null;
   protected Properties props = null;
   private boolean readOnly = false;
   protected LRUCache<String, CachedResultSetMetaData> resultSetMetadataCache;
   protected final Lock resultSetMetadataCacheLock = new ReentrantLock();
   private Map<String, Class<?>> typeMap;
   private String user = null;
   private LRUCache<String, Boolean> serverSideStatementCheckCache;
   private final Lock serverSideStatementCheckCacheLock = new ReentrantLock();
   private LRUCache<ConnectionImpl.CompoundCacheKey, ServerPreparedStatement> serverSideStatementCache;
   private HostInfo origHostInfo;
   private String origHostToConnectTo;
   private int origPortToConnectTo;
   private List<QueryInterceptor> queryInterceptors;
   protected JdbcPropertySet propertySet;
   private RuntimeProperty<Boolean> autoReconnectForPools;
   private RuntimeProperty<Boolean> cachePrepStmts;
   private RuntimeProperty<Boolean> autoReconnect;
   private RuntimeProperty<Boolean> useUsageAdvisor;
   private RuntimeProperty<Boolean> reconnectAtTxEnd;
   private RuntimeProperty<Boolean> emulateUnsupportedPstmts;
   private RuntimeProperty<Boolean> ignoreNonTxTables;
   private RuntimeProperty<Boolean> pedantic;
   private RuntimeProperty<Integer> prepStmtCacheSqlLimit;
   private RuntimeProperty<Boolean> useLocalSessionState;
   private RuntimeProperty<Boolean> useServerPrepStmts;
   private RuntimeProperty<Boolean> processEscapeCodesForPrepStmts;
   private RuntimeProperty<Boolean> useLocalTransactionState;
   private RuntimeProperty<Boolean> disconnectOnExpiredPasswords;
   private RuntimeProperty<Boolean> readOnlyPropagatesToServer;
   protected ResultSetFactory nullStatementResultSetFactory;
   TelemetrySpan connectionSpan = null;
   private int autoIncrementIncrement = 0;
   private ExceptionInterceptor exceptionInterceptor;
   private ClientInfoProvider infoProvider;

   @Override
   public String getHost() {
      return this.session.getHostInfo().getHost();
   }

   @Override
   public Lock getLock() {
      return this.lock;
   }

   @Override
   public boolean isProxySet() {
      return this.topProxy != null;
   }

   @Override
   public void setProxy(JdbcConnection proxy) {
      if (this.parentProxy == null) {
         this.parentProxy = proxy;
      }

      this.topProxy = proxy;
      this.realProxy = this.topProxy instanceof MultiHostMySQLConnection ? ((MultiHostMySQLConnection)proxy).getThisAsProxy() : null;
   }

   private JdbcConnection getProxy() {
      return (JdbcConnection)(this.topProxy != null ? this.topProxy : this);
   }

   @Override
   public JdbcConnection getMultiHostSafeProxy() {
      return this.getProxy();
   }

   @Override
   public JdbcConnection getMultiHostParentProxy() {
      return this.parentProxy;
   }

   @Override
   public JdbcConnection getActiveMySQLConnection() {
      return this;
   }

   @Override
   public Lock getConnectionLock() {
      return this.realProxy != null ? this.realProxy.getLock() : this.getProxy().getLock();
   }

   public static JdbcConnection getInstance(HostInfo hostInfo) throws SQLException {
      return new ConnectionImpl(hostInfo);
   }

   protected ConnectionImpl() {
   }

   public ConnectionImpl(HostInfo hostInfo) throws SQLException {
      try {
         this.origHostInfo = hostInfo;
         this.origHostToConnectTo = hostInfo.getHost();
         this.origPortToConnectTo = hostInfo.getPort();
         this.database = hostInfo.getDatabase();
         this.user = hostInfo.getUser();
         this.password = hostInfo.getPassword();
         this.props = hostInfo.exposeAsProperties();
         this.propertySet = new JdbcPropertySetImpl();
         this.propertySet.initializeProperties(this.props);
         this.nullStatementResultSetFactory = new ResultSetFactory(this, null);
         this.session = new NativeSession(hostInfo, this.propertySet);
         this.session.addListener(this);
      } catch (CJException var30) {
         throw SQLExceptionsMapping.translateException(var30, this.getExceptionInterceptor());
      }

      this.connectionSpan = this.session.getTelemetryHandler().startSpan(TelemetrySpanName.CONNECTION_CREATE);
      this.session.getTelemetryHandler().addLinkTarget(this.connectionSpan);

      try (TelemetryScope scope = this.connectionSpan.makeCurrent()) {
         this.connectionSpan.setAttribute(TelemetryAttribute.DB_CONNECTION_STRING, this.getURL());
         this.connectionSpan.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
         this.connectionSpan.setAttribute(TelemetryAttribute.DB_USER, this.getUser());
         this.connectionSpan.setAttribute(TelemetryAttribute.SERVER_ADDRESS, this.origHostToConnectTo);
         this.connectionSpan.setAttribute(TelemetryAttribute.SERVER_PORT, this.origPortToConnectTo);
         this.connectionSpan.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
         this.connectionSpan.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());

         try {
            String exceptionInterceptorClasses = this.propertySet.getStringProperty(PropertyKey.exceptionInterceptors).getStringValue();
            if (exceptionInterceptorClasses != null && !"".equals(exceptionInterceptorClasses)) {
               this.exceptionInterceptor = new ExceptionInterceptorChain(exceptionInterceptorClasses, this.props, this.session.getLog());
            }

            this.autoReconnectForPools = this.propertySet.getBooleanProperty(PropertyKey.autoReconnectForPools);
            this.cachePrepStmts = this.propertySet.getBooleanProperty(PropertyKey.cachePrepStmts);
            this.autoReconnect = this.propertySet.getBooleanProperty(PropertyKey.autoReconnect);
            this.useUsageAdvisor = this.propertySet.getBooleanProperty(PropertyKey.useUsageAdvisor);
            this.reconnectAtTxEnd = this.propertySet.getBooleanProperty(PropertyKey.reconnectAtTxEnd);
            this.emulateUnsupportedPstmts = this.propertySet.getBooleanProperty(PropertyKey.emulateUnsupportedPstmts);
            this.ignoreNonTxTables = this.propertySet.getBooleanProperty(PropertyKey.ignoreNonTxTables);
            this.pedantic = this.propertySet.getBooleanProperty(PropertyKey.pedantic);
            this.prepStmtCacheSqlLimit = this.propertySet.getIntegerProperty(PropertyKey.prepStmtCacheSqlLimit);
            this.useLocalSessionState = this.propertySet.getBooleanProperty(PropertyKey.useLocalSessionState);
            this.useServerPrepStmts = this.propertySet.getBooleanProperty(PropertyKey.useServerPrepStmts);
            this.processEscapeCodesForPrepStmts = this.propertySet.getBooleanProperty(PropertyKey.processEscapeCodesForPrepStmts);
            this.useLocalTransactionState = this.propertySet.getBooleanProperty(PropertyKey.useLocalTransactionState);
            this.disconnectOnExpiredPasswords = this.propertySet.getBooleanProperty(PropertyKey.disconnectOnExpiredPasswords);
            this.readOnlyPropagatesToServer = this.propertySet.getBooleanProperty(PropertyKey.readOnlyPropagatesToServer);
            if (this.cachePrepStmts.getValue()) {
               this.createPreparedStatementCaches();
            }

            if (this.propertySet.getBooleanProperty(PropertyKey.cacheCallableStmts).getValue()) {
               this.parsedCallableStatementCache = new LRUCache<>(this.propertySet.getIntegerProperty(PropertyKey.callableStmtCacheSize).getValue());
            }

            if (this.propertySet.getBooleanProperty(PropertyKey.allowMultiQueries).getValue()) {
               this.propertySet.<Boolean>getProperty(PropertyKey.cacheResultSetMetadata).setValue(false);
            }

            if (this.propertySet.getBooleanProperty(PropertyKey.cacheResultSetMetadata).getValue()) {
               this.resultSetMetadataCache = new LRUCache<>(this.propertySet.getIntegerProperty(PropertyKey.metadataCacheSize).getValue());
            }

            if (this.propertySet.getStringProperty(PropertyKey.socksProxyHost).getStringValue() != null) {
               this.propertySet.<String>getProperty(PropertyKey.socketFactory).setValue(SocksProxySocketFactory.class.getName());
            }

            this.dbmd = this.getMetaData(false, false);
            this.initializeSafeQueryInterceptors();
         } catch (CJException var33) {
            throw SQLExceptionsMapping.translateException(var33, this.getExceptionInterceptor());
         }

         try {
            this.createNewIO(false);
            this.unSafeQueryInterceptors();
            AbandonedConnectionCleanupThread.trackConnection(this, this.getSession().getNetworkResources());
            SocketAddress socketAddress = this.session.getRemoteSocketAddress();
            if (InetSocketAddress.class.isInstance(socketAddress)) {
               InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;
               this.connectionSpan.setAttribute(TelemetryAttribute.NETWORK_PEER_ADDRESS, inetSocketAddress.getHostName());
               this.connectionSpan.setAttribute(TelemetryAttribute.NETWORK_PEER_PORT, inetSocketAddress.getPort());
               this.connectionSpan.setAttribute(TelemetryAttribute.NETWORK_TRANSPORT, "tcp");
            }

            if (this.propertySet
               .getStringProperty(PropertyKey.socketFactory)
               .getValue()
               .equalsIgnoreCase("org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.NamedPipeSocketFactory")) {
               this.connectionSpan.setAttribute(TelemetryAttribute.NETWORK_TRANSPORT, "pipe");
            } else if (StringUtils.indexOfIgnoreCase(socketAddress.getClass().getName(), "UNIXSocket") >= 0) {
               this.connectionSpan.setAttribute(TelemetryAttribute.NETWORK_TRANSPORT, "unix");
            }
         } catch (SQLException var31) {
            this.cleanup(var31);
            throw var31;
         } catch (Exception var32) {
            this.cleanup(var32);
            throw SQLError.createSQLException(
               this.propertySet.getBooleanProperty(PropertyKey.paranoid).getValue()
                  ? Messages.getString("Connection.0")
                  : Messages.getString("Connection.1", new Object[]{this.session.getHostInfo().getHost(), this.session.getHostInfo().getPort()}),
               "08S01",
               var32,
               this.getExceptionInterceptor()
            );
         }
      } catch (Throwable var36) {
         this.connectionSpan.setError(var36);
         throw var36;
      } finally {
         this.connectionSpan.end();
      }
   }

   @Override
   public JdbcPropertySet getPropertySet() {
      return this.propertySet;
   }

   @Override
   public void unSafeQueryInterceptors() throws SQLException {
      try {
         this.queryInterceptors = this.queryInterceptors
            .stream()
            .map(NoSubInterceptorWrapper.class::cast)
            .map(NoSubInterceptorWrapper::getUnderlyingInterceptor)
            .collect(Collectors.toCollection(LinkedList::new));
         if (this.session != null) {
            this.session.setQueryInterceptors(this.queryInterceptors);
         }
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public void initializeSafeQueryInterceptors() throws SQLException {
      try {
         this.queryInterceptors = Util.loadClasses(
               QueryInterceptor.class,
               this.propertySet.getStringProperty(PropertyKey.queryInterceptors).getStringValue(),
               "MysqlIo.BadQueryInterceptor",
               this.getExceptionInterceptor()
            )
            .stream()
            .map(o -> new NoSubInterceptorWrapper(o.init(this, this.props, this.session.getLog())))
            .collect(Collectors.toCollection(LinkedList::new));
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public List<QueryInterceptor> getQueryInterceptorsInstances() {
      return this.queryInterceptors;
   }

   private boolean canHandleAsServerPreparedStatement(String sql) throws SQLException {
      if (sql == null || sql.length() == 0) {
         return true;
      } else if (!this.useServerPrepStmts.getValue()) {
         return false;
      } else {
         boolean allowMultiQueries = this.propertySet.getBooleanProperty(PropertyKey.allowMultiQueries).getValue()
            || this.propertySet.getBooleanProperty(PropertyKey.rewriteBatchedStatements).getValue();
         if (this.cachePrepStmts.getValue()) {
            this.serverSideStatementCheckCacheLock.lock();

            boolean canHandle;
            try {
               Boolean flag = this.serverSideStatementCheckCache.get(sql);
               if (flag == null) {
                  canHandle = StringUtils.canHandleAsServerPreparedStatementNoCache(
                     sql,
                     this.getServerVersion(),
                     allowMultiQueries,
                     this.session.getServerSession().isNoBackslashEscapesSet(),
                     this.session.getServerSession().useAnsiQuotedIdentifiers()
                  );
                  if (sql.length() < this.prepStmtCacheSqlLimit.getValue()) {
                     this.serverSideStatementCheckCache.put(sql, canHandle ? Boolean.TRUE : Boolean.FALSE);
                  }

                  return canHandle;
               }

               canHandle = flag;
            } finally {
               this.serverSideStatementCheckCacheLock.unlock();
            }

            return canHandle;
         } else {
            return StringUtils.canHandleAsServerPreparedStatementNoCache(
               sql,
               this.getServerVersion(),
               allowMultiQueries,
               this.session.getServerSession().isNoBackslashEscapesSet(),
               this.session.getServerSession().useAnsiQuotedIdentifiers()
            );
         }
      }
   }

   @Override
   public void changeUser(String userName, String newPassword) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            this.checkClosed();
            if (userName == null || userName.equals("")) {
               userName = "";
            }

            if (newPassword == null) {
               newPassword = "";
            }

            try {
               this.session.changeUser(userName, newPassword, this.database);
            } catch (CJException var10) {
               if ("28000".equals(var10.getSQLState())) {
                  this.cleanup(var10);
               }

               throw var10;
            }

            this.user = userName;
            this.password = newPassword;
            this.session.getServerSession().getCharsetSettings().configurePostHandshake(true);
            this.session.setSessionVariables();
            this.handleAutoCommitDefaults();
            this.setupServerForTruncationChecks();
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var12) {
         throw SQLExceptionsMapping.translateException(var12, this.getExceptionInterceptor());
      }
   }

   @Override
   public void checkClosed() {
      this.session.checkClosed();
   }

   @Override
   public void throwConnectionClosedException() throws SQLException {
      try {
         SQLException ex = SQLError.createSQLException(Messages.getString("Connection.2"), "08003", this.getExceptionInterceptor());
         if (this.session.getForceClosedReason() != null) {
            ex.initCause(this.session.getForceClosedReason());
         }

         throw ex;
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   private void checkTransactionIsolationLevel() {
      String s = this.session.getServerSession().getServerVariable("transaction_isolation");
      if (s == null) {
         s = this.session.getServerSession().getServerVariable("tx_isolation");
      }

      if (s != null) {
         Integer intTI = mapTransIsolationNameToValue.get(s);
         if (intTI != null) {
            this.isolationLevel = intTI;
         }
      }
   }

   @Override
   public void abortInternal() throws SQLException {
      try {
         this.session.forceClose();
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public void cleanup(Throwable whyCleanedUp) {
      try {
         if (this.session != null) {
            if (this.isClosed()) {
               this.session.forceClose();
            } else {
               this.doClose(whyCleanedUp, CloseOption.IMPLICIT, CloseOption.PROPAGATE);
            }
         }
      } catch (CJException | SQLException var3) {
      }
   }

   @Override
   public void clearWarnings() throws SQLException {
      try {
         ;
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement clientPrepareStatement(String sql) throws SQLException {
      try {
         return this.clientPrepareStatement(sql, 1003, 1007);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement clientPrepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
      try {
         PreparedStatement pStmt = this.clientPrepareStatement(sql);
         ((ClientPreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
         return pStmt;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      try {
         return this.clientPrepareStatement(sql, resultSetType, resultSetConcurrency, true);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, boolean processEscapeCodesIfNeeded) throws SQLException {
      try {
         this.checkClosed();
         String nativeSql = processEscapeCodesIfNeeded && this.processEscapeCodesForPrepStmts.getValue() ? this.nativeSQL(sql) : sql;
         ClientPreparedStatement pStmt = null;
         if (this.cachePrepStmts.getValue()) {
            QueryInfo pStmtInfo = this.queryInfoCache.get(nativeSql);
            if (pStmtInfo == null) {
               pStmt = ClientPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.database);
               this.queryInfoCache.put(nativeSql, pStmt.getQueryInfo());
            } else {
               pStmt = ClientPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.database, pStmtInfo);
            }
         } else {
            pStmt = ClientPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.database);
         }

         pStmt.setResultSetType(resultSetType);
         pStmt.setResultSetConcurrency(resultSetConcurrency);
         return pStmt;
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement clientPrepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
      try {
         ClientPreparedStatement pStmt = (ClientPreparedStatement)this.clientPrepareStatement(sql);
         pStmt.setRetrieveGeneratedKeys(autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0);
         return pStmt;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement clientPrepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
      try {
         ClientPreparedStatement pStmt = (ClientPreparedStatement)this.clientPrepareStatement(sql);
         pStmt.setRetrieveGeneratedKeys(autoGenKeyColNames != null && autoGenKeyColNames.length > 0);
         return pStmt;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      try {
         return this.clientPrepareStatement(sql, resultSetType, resultSetConcurrency, true);
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public void close() throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            if (this.connectionLifecycleInterceptors != null) {
               for (ConnectionLifecycleInterceptor cli : this.connectionLifecycleInterceptors) {
                  cli.close();
               }
            }

            this.doClose(null, CloseOption.ROLLBACK, CloseOption.PROPAGATE);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   private void closeAllOpenStatements() throws SQLException {
      SQLException postponedException = null;

      for (JdbcStatement stmt : this.openStatements) {
         try {
            stmt.doClose(CloseOption.IMPLICIT, CloseOption.PROPAGATE, CloseOption.NO_CACHE);
         } catch (SQLException var5) {
            postponedException = var5;
         }
      }

      if (postponedException != null) {
         throw postponedException;
      }
   }

   private void closeStatement(Statement stmt) {
      if (stmt != null) {
         try {
            stmt.close();
         } catch (SQLException var3) {
         }

         Statement var4 = null;
      }
   }

   @Override
   public void commit() throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            this.checkClosed();

            try {
               if (this.connectionLifecycleInterceptors != null) {
                  IterateBlock<ConnectionLifecycleInterceptor> iter = new IterateBlock<ConnectionLifecycleInterceptor>(
                     this.connectionLifecycleInterceptors.iterator()
                  ) {
                     void forEach(ConnectionLifecycleInterceptor each) throws SQLException {
                        if (!each.commit()) {
                           this.stopIterating = true;
                        }
                     }
                  };
                  iter.doForAll();
                  if (!iter.fullIteration()) {
                     return;
                  }
               }

               if (this.session.getServerSession().isAutoCommit()) {
                  throw SQLError.createSQLException(Messages.getString("Connection.3"), this.getExceptionInterceptor());
               }

               if (this.useLocalTransactionState.getValue() && !this.session.getServerSession().inTransactionOnServer()) {
                  return;
               }

               TelemetrySpan span = this.session.getTelemetryHandler().startSpan(TelemetrySpanName.ROLLBACK);

               try (TelemetryScope scope = span.makeCurrent()) {
                  span.setAttribute(TelemetryAttribute.DB_NAME, this.getDatabase());
                  span.setAttribute(TelemetryAttribute.DB_OPERATION, "ROLLBACK");
                  span.setAttribute(TelemetryAttribute.DB_STATEMENT, "ROLLBACK");
                  span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
                  span.setAttribute(TelemetryAttribute.DB_USER, this.getUser());
                  span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
                  span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
                  this.session.execSQL(null, "COMMIT", -1, null, false, this.nullStatementResultSetFactory, null, false);
               } catch (Throwable var55) {
                  span.setError(var55);
                  throw var55;
               } finally {
                  span.end();
               }
            } catch (SQLException var57) {
               if ("08S01".equals(var57.getSQLState())) {
                  throw SQLError.createSQLException(Messages.getString("Connection.4"), "08007", this.getExceptionInterceptor());
               }

               throw var57;
            } finally {
               this.session.setNeedsPing(this.reconnectAtTxEnd.getValue());
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var60) {
         throw SQLExceptionsMapping.translateException(var60, this.getExceptionInterceptor());
      }
   }

   @Override
   public void createNewIO(boolean isForReconnect) {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            if (this.autoReconnect.getValue()) {
               this.connectWithRetries(isForReconnect);
               return;
            }

            this.connectOneTryOnly(isForReconnect);
         } catch (SQLException var9) {
            throw (UnableToConnectException)ExceptionFactory.createException(UnableToConnectException.class, var9.getMessage(), var9);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   private void connectWithRetries(boolean isForReconnect) throws SQLException {
      double timeout = this.propertySet.getIntegerProperty(PropertyKey.initialTimeout).getValue().intValue();
      boolean connectionGood = false;
      Exception connectionException = null;

      for (int attemptCount = 0; attemptCount < this.propertySet.getIntegerProperty(PropertyKey.maxReconnects).getValue() && !connectionGood; attemptCount++) {
         try {
            this.session.forceClose();
            JdbcConnection c = this.getProxy();
            this.session.connect(this.origHostInfo, this.user, this.password, this.database, this.getLoginTimeout(), c);
            this.pingInternal(false, 0);
            Lock connectionLock = this.getConnectionLock();
            connectionLock.lock();

            boolean oldAutoCommit;
            int oldIsolationLevel;
            boolean oldReadOnly;
            String oldDb;
            try {
               oldAutoCommit = this.getAutoCommit();
               oldIsolationLevel = this.isolationLevel;
               oldReadOnly = this.isReadOnly(false);
               oldDb = this.getDatabase();
               this.session.setQueryInterceptors(this.queryInterceptors);
            } finally {
               connectionLock.unlock();
            }

            this.initializePropsFromServer();
            if (isForReconnect) {
               this.setAutoCommit(oldAutoCommit);
               this.setTransactionIsolation(oldIsolationLevel);
               this.setDatabase(oldDb);
               this.setReadOnly(oldReadOnly);
            }

            connectionGood = true;
            break;
         } catch (UnableToConnectException var20) {
            this.close();
            this.session.getProtocol().getSocketConnection().forceClose();
         } catch (Exception var21) {
            connectionException = var21;
            connectionGood = false;
         }

         if (connectionGood) {
            break;
         }

         if (attemptCount > 0) {
            try {
               Thread.sleep((long)timeout * 1000L);
            } catch (InterruptedException var18) {
            }
         }
      }

      if (!connectionGood) {
         SQLException chainedEx = SQLError.createSQLException(
            Messages.getString("Connection.UnableToConnectWithRetries", new Object[]{this.propertySet.getIntegerProperty(PropertyKey.maxReconnects).getValue()}),
            "08001",
            connectionException,
            this.getExceptionInterceptor()
         );
         throw chainedEx;
      } else {
         if (this.propertySet.getBooleanProperty(PropertyKey.paranoid).getValue() && !this.autoReconnect.getValue()) {
            this.password = null;
            this.user = null;
         }

         if (isForReconnect) {
            Iterator<JdbcStatement> statementIter = this.openStatements.iterator();
            Stack<JdbcStatement> serverPreparedStatements = null;

            while (statementIter.hasNext()) {
               JdbcStatement statementObj = statementIter.next();
               if (statementObj instanceof ServerPreparedStatement) {
                  if (serverPreparedStatements == null) {
                     serverPreparedStatements = new Stack<>();
                  }

                  serverPreparedStatements.add(statementObj);
               }
            }

            if (serverPreparedStatements != null) {
               while (!serverPreparedStatements.isEmpty()) {
                  ((ServerPreparedStatement)serverPreparedStatements.pop()).rePrepare();
               }
            }
         }
      }
   }

   private void connectOneTryOnly(boolean isForReconnect) throws SQLException {
      Exception connectionNotEstablishedBecause = null;

      try {
         JdbcConnection c = this.getProxy();
         this.session.connect(this.origHostInfo, this.user, this.password, this.database, this.getLoginTimeout(), c);
         boolean oldAutoCommit = this.getAutoCommit();
         int oldIsolationLevel = this.isolationLevel;
         boolean oldReadOnly = this.isReadOnly(false);
         String oldDb = this.getDatabase();
         this.session.setQueryInterceptors(this.queryInterceptors);
         this.initializePropsFromServer();
         if (isForReconnect) {
            this.setAutoCommit(oldAutoCommit);
            this.setTransactionIsolation(oldIsolationLevel);
            this.setDatabase(oldDb);
            this.setReadOnly(oldReadOnly);
         }
      } catch (UnableToConnectException var8) {
         this.close();
         NativeProtocol protocol = this.session.getProtocol();
         if (protocol != null) {
            protocol.getSocketConnection().forceClose();
         }

         throw var8;
      } catch (Exception var9) {
         if (!(var9 instanceof PasswordExpiredException) && (!(var9 instanceof SQLException) || ((SQLException)var9).getErrorCode() != 1820)
            || this.disconnectOnExpiredPasswords.getValue()) {
            if (this.session != null) {
               this.session.forceClose();
            }

            if (var9 instanceof SQLException) {
               throw (SQLException)var9;
            } else if (var9.getCause() != null && var9.getCause() instanceof SQLException) {
               throw (SQLException)var9.getCause();
            } else if (var9 instanceof CJException) {
               throw (CJException)var9;
            } else {
               SQLException chainedEx = SQLError.createSQLException(Messages.getString("Connection.UnableToConnect"), "08001", this.getExceptionInterceptor());
               chainedEx.initCause(var9);
               throw chainedEx;
            }
         }
      }
   }

   private int getLoginTimeout() {
      int loginTimeoutSecs = DriverManager.getLoginTimeout();
      return loginTimeoutSecs <= 0 ? 0 : loginTimeoutSecs * 1000;
   }

   private void createPreparedStatementCaches() throws SQLException {
      Lock connectionLock = this.getConnectionLock();
      connectionLock.lock();

      try {
         int cacheSize = this.propertySet.getIntegerProperty(PropertyKey.prepStmtCacheSize).getValue();
         String queryInfoCacheFactory = this.propertySet.getStringProperty(PropertyKey.queryInfoCacheFactory).getValue();
         CacheAdapterFactory<String, QueryInfo> cacheFactory = Util.getInstance(
            CacheAdapterFactory.class, queryInfoCacheFactory, null, null, this.getExceptionInterceptor()
         );
         this.queryInfoCache = cacheFactory.getInstance(connectionLock, this.origHostInfo.getDatabaseUrl(), cacheSize, this.prepStmtCacheSqlLimit.getValue());
         if (this.useServerPrepStmts.getValue()) {
            this.serverSideStatementCheckCache = new LRUCache<>(cacheSize);
            this.serverSideStatementCache = new LRUCache<ConnectionImpl.CompoundCacheKey, ServerPreparedStatement>(cacheSize) {
               private static final long serialVersionUID = 7692318650375988114L;

               @Override
               protected boolean removeEldestEntry(Entry<ConnectionImpl.CompoundCacheKey, ServerPreparedStatement> eldest) {
                  if (this.maxElements <= 1) {
                     return false;
                  } else {
                     boolean removeIt = super.removeEldestEntry(eldest);
                     if (removeIt) {
                        ServerPreparedStatement ps = eldest.getValue();
                        ps.isCached = false;
                        ps.setClosed(false);

                        try {
                           ps.doClose(CloseOption.PROPAGATE, CloseOption.NO_CACHE);
                        } catch (SQLException var5) {
                        }
                     }

                     return removeIt;
                  }
               }
            };
         }
      } finally {
         connectionLock.unlock();
      }
   }

   @Override
   public Statement createStatement() throws SQLException {
      try {
         return this.createStatement(1003, 1007);
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
      try {
         this.checkClosed();
         StatementImpl stmt = new StatementImpl(this.getMultiHostSafeProxy(), this.database);
         stmt.setResultSetType(resultSetType);
         stmt.setResultSetConcurrency(resultSetConcurrency);
         return stmt;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      try {
         if (this.pedantic.getValue() && resultSetHoldability != 1) {
            throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009", this.getExceptionInterceptor());
         } else {
            return this.createStatement(resultSetType, resultSetConcurrency);
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getActiveStatementCount() {
      return this.openStatements.size();
   }

   @Override
   public boolean getAutoCommit() throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         boolean var2;
         try {
            var2 = this.session.getServerSession().isAutoCommit();
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public String getCatalog() throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         String var2;
         try {
            var2 = this.propertySet.getEnumProperty(PropertyKey.databaseTerm).getValue() == PropertyDefinitions.DatabaseTerm.SCHEMA ? null : this.database;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public String getCharacterSetMetadata() {
      Lock connectionLock = this.getConnectionLock();
      connectionLock.lock();

      String var2;
      try {
         var2 = this.session.getServerSession().getCharsetSettings().getMetadataEncoding();
      } finally {
         connectionLock.unlock();
      }

      return var2;
   }

   @Override
   public int getHoldability() throws SQLException {
      try {
         return 2;
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public long getId() {
      return this.session.getThreadId();
   }

   @Override
   public long getIdleFor() {
      Lock connectionLock = this.getConnectionLock();
      connectionLock.lock();

      long var2;
      try {
         var2 = this.session.getIdleFor();
      } finally {
         connectionLock.unlock();
      }

      return var2;
   }

   @Override
   public java.sql.DatabaseMetaData getMetaData() throws SQLException {
      try {
         return this.getMetaData(true, true);
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   private java.sql.DatabaseMetaData getMetaData(boolean checkClosed, boolean checkForInfoSchema) throws SQLException {
      try {
         if (checkClosed) {
            this.checkClosed();
         }

         DatabaseMetaData dbmeta = DatabaseMetaData.getInstance(
            this.getMultiHostSafeProxy(), this.database, checkForInfoSchema, this.nullStatementResultSetFactory
         );
         if (this.getSession() != null && this.getSession().getProtocol() != null) {
            dbmeta.setMetadataEncoding(this.getSession().getServerSession().getCharsetSettings().getMetadataEncoding());
            dbmeta.setMetadataCollationIndex(this.getSession().getServerSession().getCharsetSettings().getMetadataCollationIndex());
         }

         return dbmeta;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public Statement getMetadataSafeStatement() throws SQLException {
      try {
         return this.getMetadataSafeStatement(0);
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   public Statement getMetadataSafeStatement(int maxRows) throws SQLException {
      Statement stmt = this.createStatement();
      stmt.setMaxRows(maxRows == -1 ? 0 : maxRows);
      stmt.setEscapeProcessing(false);
      if (stmt.getFetchSize() != 0) {
         stmt.setFetchSize(0);
      }

      return stmt;
   }

   @Override
   public ServerVersion getServerVersion() {
      return this.session.getServerSession().getServerVersion();
   }

   @Override
   public int getTransactionIsolation() throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         int var4;
         try {
            if (this.useLocalSessionState.getValue()) {
               return this.isolationLevel;
            }

            String s = this.session
               .queryServerVariable(
                  !this.versionMeetsMinimum(8, 0, 3) && (!this.versionMeetsMinimum(5, 7, 20) || this.versionMeetsMinimum(8, 0, 0))
                     ? "@@session.tx_isolation"
                     : "@@session.transaction_isolation"
               );
            if (s == null) {
               throw SQLError.createSQLException(Messages.getString("Connection.13"), "S1000", this.getExceptionInterceptor());
            }

            Integer intTI = mapTransIsolationNameToValue.get(s);
            if (intTI == null) {
               throw SQLError.createSQLException(Messages.getString("Connection.12", new Object[]{s}), "S1000", this.getExceptionInterceptor());
            }

            this.isolationLevel = intTI;
            var4 = this.isolationLevel;
         } finally {
            connectionLock.unlock();
         }

         return var4;
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public Map<String, Class<?>> getTypeMap() throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         Map var2;
         try {
            if (this.typeMap == null) {
               this.typeMap = new HashMap<>();
            }

            var2 = this.typeMap;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public String getURL() {
      return this.origHostInfo.getDatabaseUrl();
   }

   @Override
   public String getUser() {
      return this.user;
   }

   @Override
   public SQLWarning getWarnings() throws SQLException {
      try {
         return null;
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean hasSameProperties(JdbcConnection c) {
      return this.props.equals(c.getProperties());
   }

   @Override
   public Properties getProperties() {
      return this.props;
   }

   private void initializePropsFromServer() throws SQLException {
      String connectionInterceptorClasses = this.propertySet.getStringProperty(PropertyKey.connectionLifecycleInterceptors).getStringValue();
      this.connectionLifecycleInterceptors = null;
      if (connectionInterceptorClasses != null) {
         try {
            this.connectionLifecycleInterceptors = Util.loadClasses(
                  ConnectionLifecycleInterceptor.class, connectionInterceptorClasses, "Connection.badLifecycleInterceptor", this.getExceptionInterceptor()
               )
               .stream()
               .map(i -> i.init(this, this.props, this.session.getLog()))
               .collect(Collectors.toCollection(LinkedList::new));
         } catch (CJException var4) {
            throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
         }
      }

      this.session.setSessionVariables();
      this.session.loadServerVariables(this.getConnectionLock(), this.dbmd.getDriverVersion());
      this.autoIncrementIncrement = this.session.getServerSession().getServerVariable("auto_increment_increment", 1);

      try {
         LicenseConfiguration.checkLicenseType(this.session.getServerSession().getServerVariables());
      } catch (CJException var3) {
         throw SQLError.createSQLException(var3.getMessage(), "08001", this.getExceptionInterceptor());
      }

      this.session.getProtocol().initServerSession();
      this.checkTransactionIsolationLevel();
      this.handleAutoCommitDefaults();
      ((DatabaseMetaData)this.dbmd).setMetadataEncoding(this.session.getServerSession().getCharsetSettings().getMetadataEncoding());
      ((DatabaseMetaData)this.dbmd).setMetadataCollationIndex(this.session.getServerSession().getCharsetSettings().getMetadataCollationIndex());
      this.setupServerForTruncationChecks();
   }

   private void handleAutoCommitDefaults() throws SQLException {
      try {
         boolean resetAutoCommitDefault = false;
         String initConnectValue = this.session.getServerSession().getServerVariable("init_connect");
         if (initConnectValue != null && initConnectValue.length() > 0) {
            String s = this.session.queryServerVariable("@@session.autocommit");
            if (s != null) {
               this.session.getServerSession().setAutoCommit(Boolean.parseBoolean(s));
               if (!this.session.getServerSession().isAutoCommit()) {
                  resetAutoCommitDefault = true;
               }
            }
         } else {
            resetAutoCommitDefault = true;
         }

         if (resetAutoCommitDefault) {
            try {
               this.setAutoCommit(true);
            } catch (SQLException var5) {
               if (var5.getErrorCode() != 1820 || this.disconnectOnExpiredPasswords.getValue()) {
                  throw var5;
               }
            }
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isClosed() {
      try {
         return this.session.isClosed();
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isInGlobalTx() {
      return this.isInGlobalTx;
   }

   @Override
   public boolean isSourceConnection() {
      return false;
   }

   @Override
   public boolean isReadOnly() throws SQLException {
      try {
         return this.isReadOnly(true);
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isReadOnly(boolean useSessionStatus) throws SQLException {
      try {
         if (useSessionStatus
            && !this.session.isClosed()
            && this.versionMeetsMinimum(5, 6, 5)
            && !this.useLocalSessionState.getValue()
            && this.readOnlyPropagatesToServer.getValue()) {
            String s = this.session
               .queryServerVariable(
                  !this.versionMeetsMinimum(8, 0, 3) && (!this.versionMeetsMinimum(5, 7, 20) || this.versionMeetsMinimum(8, 0, 0))
                     ? "@@session.tx_read_only"
                     : "@@session.transaction_read_only"
               );
            if (s != null) {
               return Integer.parseInt(s) != 0;
            }
         }

         return this.readOnly;
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isSameResource(JdbcConnection otherConnection) {
      Lock connectionLock = this.getConnectionLock();
      connectionLock.lock();

      try {
         if (otherConnection == null) {
            return false;
         } else {
            boolean directCompare = true;
            String otherHost = ((ConnectionImpl)otherConnection).origHostToConnectTo;
            String otherOrigDatabase = ((ConnectionImpl)otherConnection).origHostInfo.getDatabase();
            String otherCurrentDb = ((ConnectionImpl)otherConnection).database;
            if (!StringUtils.nullSafeEqual(otherHost, this.origHostToConnectTo)) {
               directCompare = false;
            } else if (otherHost != null && otherHost.indexOf(44) == -1 && otherHost.indexOf(58) == -1) {
               directCompare = ((ConnectionImpl)otherConnection).origPortToConnectTo == this.origPortToConnectTo;
            }

            if (directCompare
               && (!StringUtils.nullSafeEqual(otherOrigDatabase, this.origHostInfo.getDatabase()) || !StringUtils.nullSafeEqual(otherCurrentDb, this.database))
               )
             {
               directCompare = false;
            }

            if (directCompare) {
               return true;
            } else {
               String otherResourceId = ((ConnectionImpl)otherConnection).getPropertySet().getStringProperty(PropertyKey.resourceId).getValue();
               String myResourceId = this.propertySet.getStringProperty(PropertyKey.resourceId).getValue();
               if (otherResourceId != null || myResourceId != null) {
                  directCompare = StringUtils.nullSafeEqual(otherResourceId, myResourceId);
                  if (directCompare) {
                     return true;
                  }
               }

               return false;
            }
         }
      } finally {
         connectionLock.unlock();
      }
   }

   @Override
   public int getAutoIncrementIncrement() {
      return this.autoIncrementIncrement;
   }

   @Override
   public boolean lowerCaseTableNames() {
      return this.session.getServerSession().isLowerCaseTableNames();
   }

   @Override
   public String nativeSQL(String sql) throws SQLException {
      try {
         if (sql == null) {
            return null;
         } else {
            Object escapedSqlResult = EscapeProcessor.escapeSQL(
               sql,
               this.getMultiHostSafeProxy().getSession().getServerSession().getSessionTimeZone(),
               this.getMultiHostSafeProxy().getSession().getServerSession().getCapabilities().serverSupportsFracSecs(),
               this.getMultiHostSafeProxy().getSession().getServerSession().isServerTruncatesFracSecs(),
               this.getExceptionInterceptor()
            );
            return escapedSqlResult instanceof String ? (String)escapedSqlResult : ((EscapeProcessorResult)escapedSqlResult).escapedSql;
         }
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   private CallableStatement parseCallableStatement(String sql) throws SQLException {
      Object escapedSqlResult = EscapeProcessor.escapeSQL(
         sql,
         this.getMultiHostSafeProxy().getSession().getServerSession().getSessionTimeZone(),
         this.getMultiHostSafeProxy().getSession().getServerSession().getCapabilities().serverSupportsFracSecs(),
         this.getMultiHostSafeProxy().getSession().getServerSession().isServerTruncatesFracSecs(),
         this.getExceptionInterceptor()
      );
      boolean isFunctionCall = false;
      String parsedSql = null;
      if (escapedSqlResult instanceof EscapeProcessorResult) {
         parsedSql = ((EscapeProcessorResult)escapedSqlResult).escapedSql;
         isFunctionCall = ((EscapeProcessorResult)escapedSqlResult).callingStoredFunction;
      } else {
         parsedSql = (String)escapedSqlResult;
         isFunctionCall = false;
      }

      return CallableStatement.getInstance(this.getMultiHostSafeProxy(), parsedSql, this.database, isFunctionCall);
   }

   @Override
   public void ping() throws SQLException {
      try {
         this.pingInternal(true, 0);
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public void pingInternal(boolean checkForClosedConnection, int timeoutMillis) throws SQLException {
      try {
         this.session.ping(checkForClosedConnection, timeoutMillis);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public java.sql.CallableStatement prepareCall(String sql) throws SQLException {
      try {
         return this.prepareCall(sql, 1003, 1007);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         CallableStatement var63;
         try {
            this.checkClosed();
            TelemetrySpan span = this.session.getTelemetryHandler().startSpan(TelemetrySpanName.ROUTINE_PREPARE);

            try (TelemetryScope scope = span.makeCurrent()) {
               CallableStatement cStmt = null;
               if (!this.propertySet.getBooleanProperty(PropertyKey.cacheCallableStmts).getValue()) {
                  cStmt = this.parseCallableStatement(sql);
               } else {
                  this.parsedCallableStatementCacheLock.lock();

                  try {
                     ConnectionImpl.CompoundCacheKey key = new ConnectionImpl.CompoundCacheKey(this.getDatabase(), sql);
                     CallableStatement.CallableStatementParamInfo cachedParamInfo = this.parsedCallableStatementCache.get(key);
                     if (cachedParamInfo != null) {
                        cStmt = CallableStatement.getInstance(this.getMultiHostSafeProxy(), cachedParamInfo);
                     } else {
                        cStmt = this.parseCallableStatement(sql);
                        CallableStatement.CallableStatementParamInfo var62 = cStmt.paramInfo;
                        this.parsedCallableStatementCache.put(key, var62);
                     }
                  } finally {
                     this.parsedCallableStatementCacheLock.unlock();
                  }
               }

               cStmt.setResultSetType(resultSetType);
               cStmt.setResultSetConcurrency(resultSetConcurrency);
               String dbOperation = cStmt.getQueryInfo().getStatementKeyword();
               span.setAttribute(TelemetryAttribute.DB_NAME, this.getDatabase());
               span.setAttribute(TelemetryAttribute.DB_OPERATION, dbOperation);
               span.setAttribute(TelemetryAttribute.DB_STATEMENT, dbOperation + " (...)");
               span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
               span.setAttribute(TelemetryAttribute.DB_USER, this.getUser());
               span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
               span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
               var63 = cStmt;
            } catch (Throwable var56) {
               span.setError(var56);
               throw var56;
            } finally {
               span.end();
            }
         } finally {
            connectionLock.unlock();
         }

         return var63;
      } catch (CJException var59) {
         throw SQLExceptionsMapping.translateException(var59, this.getExceptionInterceptor());
      }
   }

   @Override
   public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      try {
         if (this.pedantic.getValue() && resultSetHoldability != 1) {
            throw SQLError.createSQLException(Messages.getString("Connection.17"), "S1009", this.getExceptionInterceptor());
         } else {
            return (CallableStatement)this.prepareCall(sql, resultSetType, resultSetConcurrency);
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement prepareStatement(String sql) throws SQLException {
      try {
         return this.prepareStatement(sql, 1003, 1007);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement prepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
      try {
         PreparedStatement pStmt = this.prepareStatement(sql);
         ((ClientPreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
         return pStmt;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         Object var12;
         try {
            this.checkClosed();
            TelemetrySpan span = this.session.getTelemetryHandler().startSpan(TelemetrySpanName.STMT_PREPARE);

            try (TelemetryScope scope = span.makeCurrent()) {
               ClientPreparedStatement pStmt = null;
               boolean canServerPrepare = true;
               String nativeSql = this.processEscapeCodesForPrepStmts.getValue() ? this.nativeSQL(sql) : sql;
               if (this.useServerPrepStmts.getValue() && this.emulateUnsupportedPstmts.getValue()) {
                  canServerPrepare = this.canHandleAsServerPreparedStatement(nativeSql);
               }

               if (!this.useServerPrepStmts.getValue() || !canServerPrepare) {
                  pStmt = (ClientPreparedStatement)this.clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
               } else if (this.cachePrepStmts.getValue()) {
                  this.serverSideStatementCheckCacheLock.lock();

                  try {
                     pStmt = this.serverSideStatementCache.remove(new ConnectionImpl.CompoundCacheKey(this.database, sql));
                     if (pStmt != null) {
                        ((ServerPreparedStatement)pStmt).setClosed(false);
                        pStmt.clearParameters();
                        pStmt.setResultSetType(resultSetType);
                        pStmt.setResultSetConcurrency(resultSetConcurrency);
                     }

                     if (pStmt == null) {
                        try {
                           pStmt = ServerPreparedStatement.getInstance(
                              this.getMultiHostSafeProxy(), nativeSql, this.database, resultSetType, resultSetConcurrency
                           );
                           if (sql.length() < this.prepStmtCacheSqlLimit.getValue()) {
                              ((ServerPreparedStatement)pStmt).isCacheable = true;
                           }

                           pStmt.setResultSetType(resultSetType);
                           pStmt.setResultSetConcurrency(resultSetConcurrency);
                        } catch (SQLException var63) {
                           if (!this.emulateUnsupportedPstmts.getValue()) {
                              throw var63;
                           }

                           pStmt = (ClientPreparedStatement)this.clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
                           if (sql.length() < this.prepStmtCacheSqlLimit.getValue()) {
                              this.serverSideStatementCheckCache.put(sql, Boolean.FALSE);
                           }
                        }
                     }
                  } finally {
                     this.serverSideStatementCheckCacheLock.unlock();
                  }
               } else {
                  try {
                     pStmt = ServerPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.database, resultSetType, resultSetConcurrency);
                     pStmt.setResultSetType(resultSetType);
                     pStmt.setResultSetConcurrency(resultSetConcurrency);
                  } catch (SQLException var65) {
                     if (!this.emulateUnsupportedPstmts.getValue()) {
                        throw var65;
                     }

                     pStmt = (ClientPreparedStatement)this.clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
                  }
               }

               String dbOperation = pStmt.getQueryInfo().getStatementKeyword();
               span.setAttribute(TelemetryAttribute.DB_NAME, this.getDatabase());
               span.setAttribute(TelemetryAttribute.DB_OPERATION, dbOperation);
               span.setAttribute(TelemetryAttribute.DB_STATEMENT, dbOperation + " (...)");
               span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
               span.setAttribute(TelemetryAttribute.DB_USER, this.getUser());
               span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
               span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
               var12 = pStmt;
            } catch (Throwable var68) {
               span.setError(var68);
               throw var68;
            } finally {
               span.end();
            }
         } finally {
            connectionLock.unlock();
         }

         return (PreparedStatement)var12;
      } catch (CJException var71) {
         throw SQLExceptionsMapping.translateException(var71, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      try {
         if (this.pedantic.getValue() && resultSetHoldability != 1) {
            throw SQLError.createSQLException(Messages.getString("Connection.17"), "S1009", this.getExceptionInterceptor());
         } else {
            return this.prepareStatement(sql, resultSetType, resultSetConcurrency);
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement prepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
      try {
         PreparedStatement pStmt = this.prepareStatement(sql);
         ((ClientPreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0);
         return pStmt;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement prepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
      try {
         PreparedStatement pStmt = this.prepareStatement(sql);
         ((ClientPreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyColNames != null && autoGenKeyColNames.length > 0);
         return pStmt;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public void doClose(Throwable reason, CloseOption... options) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            if (this.isClosed()) {
               return;
            }

            SQLException sqlEx = null;
            this.session.setForceClosedReason(reason);

            try {
               if (this.propertySet.getBooleanProperty(PropertyKey.gatherPerfMetrics).getValue()) {
                  this.session.getProtocol().getMetricsHolder().reportMetrics(this.session.getLog());
               }

               if (this.useUsageAdvisor.getValue()) {
                  if (CloseOption.IMPLICIT.in(options)) {
                     this.session
                        .getProfilerEventHandler()
                        .processEvent((byte)0, this.session, null, null, 0L, new Throwable(), Messages.getString("Connection.18"));
                  }

                  if (System.currentTimeMillis() - this.session.getConnectionCreationTimeMillis() < 500L) {
                     this.session
                        .getProfilerEventHandler()
                        .processEvent((byte)0, this.session, null, null, 0L, new Throwable(), Messages.getString("Connection.19"));
                  }
               }

               if (!this.getAutoCommit() && CloseOption.ROLLBACK.in(options)) {
                  try {
                     this.rollback();
                  } catch (SQLException var20) {
                     sqlEx = var20;
                  }
               }

               if (CloseOption.PROPAGATE.in(options)) {
                  try {
                     this.closeAllOpenStatements();
                  } catch (SQLException var19) {
                     sqlEx = var19;
                  }
               }

               if (CloseOption.FORCED.in(options)) {
                  this.session.forceClose();
               } else {
                  this.session.quit();
               }

               if (this.queryInterceptors != null) {
                  this.queryInterceptors.forEach(QueryInterceptor::destroy);
               }

               if (this.exceptionInterceptor != null) {
                  this.exceptionInterceptor.destroy();
               }
            } finally {
               this.openStatements.clear();
               this.queryInterceptors = null;
               this.exceptionInterceptor = null;
               this.nullStatementResultSetFactory = null;
               this.session.getTelemetryHandler().removeLinkTarget(this.connectionSpan);
            }

            if (sqlEx != null) {
               throw sqlEx;
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var23) {
         throw SQLExceptionsMapping.translateException(var23, this.getExceptionInterceptor());
      }
   }

   @Override
   public void recachePreparedStatement(JdbcPreparedStatement pstmt) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            if (this.cachePrepStmts.getValue() && pstmt.isPoolable()) {
               this.serverSideStatementCheckCacheLock.lock();

               try {
                  Object oldServerPrepStmt = this.serverSideStatementCache
                     .put(
                        new ConnectionImpl.CompoundCacheKey(pstmt.getCurrentDatabase(), ((PreparedQuery)pstmt.getQuery()).getOriginalSql()),
                        (ServerPreparedStatement)pstmt
                     );
                  if (oldServerPrepStmt != null && oldServerPrepStmt != pstmt) {
                     ((ServerPreparedStatement)oldServerPrepStmt).isCached = false;
                     ((ServerPreparedStatement)oldServerPrepStmt).setClosed(false);
                     ((ServerPreparedStatement)oldServerPrepStmt).doClose(CloseOption.PROPAGATE, CloseOption.NO_CACHE);
                  }
               } finally {
                  this.serverSideStatementCheckCacheLock.unlock();
               }
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var15) {
         throw SQLExceptionsMapping.translateException(var15, this.getExceptionInterceptor());
      }
   }

   @Override
   public void decachePreparedStatement(JdbcPreparedStatement pstmt) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            if (this.cachePrepStmts.getValue()) {
               this.serverSideStatementCheckCacheLock.lock();

               try {
                  this.serverSideStatementCache
                     .remove(new ConnectionImpl.CompoundCacheKey(pstmt.getCurrentDatabase(), ((PreparedQuery)pstmt.getQuery()).getOriginalSql()));
               } finally {
                  this.serverSideStatementCheckCacheLock.unlock();
               }
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var14) {
         throw SQLExceptionsMapping.translateException(var14, this.getExceptionInterceptor());
      }
   }

   @Override
   public void registerStatement(JdbcStatement stmt) {
      this.openStatements.addIfAbsent(stmt);
   }

   @Override
   public void releaseSavepoint(Savepoint savepoint) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            this.checkClosed();
            StringBuilder releaseSavepointQuery = new StringBuilder("RELEASE SAVEPOINT ");
            releaseSavepointQuery.append(
               StringUtils.quoteIdentifier(savepoint.getSavepointName(), this.session.getIdentifierQuoteString(), this.pedantic.getValue())
            );
            Statement stmt = null;

            try {
               stmt = this.getMetadataSafeStatement();
               stmt.executeUpdate(releaseSavepointQuery.toString());
            } finally {
               this.closeStatement(stmt);
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var16) {
         throw SQLExceptionsMapping.translateException(var16, this.getExceptionInterceptor());
      }
   }

   @Override
   public void resetServerState() throws SQLException {
      try {
         if (!this.propertySet.getBooleanProperty(PropertyKey.paranoid).getValue() && this.session != null) {
            TelemetrySpan span = this.session.getTelemetryHandler().startSpan(TelemetrySpanName.CONNECTION_RESET);

            try (TelemetryScope scope = span.makeCurrent()) {
               span.setAttribute(TelemetryAttribute.DB_CONNECTION_STRING, this.getURL());
               span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
               span.setAttribute(TelemetryAttribute.DB_USER, this.getUser());
               span.setAttribute(TelemetryAttribute.SERVER_ADDRESS, this.origHostToConnectTo);
               span.setAttribute(TelemetryAttribute.SERVER_PORT, this.origPortToConnectTo);
               span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
               span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
               this.session.getServerSession().getCharsetSettings().configurePreHandshake(true);
               this.session.resetSessionState();
               this.session.getServerSession().getCharsetSettings().configurePostHandshake(true);
               this.session.setSessionVariables();
               this.handleAutoCommitDefaults();
               this.setupServerForTruncationChecks();
            } catch (Throwable var26) {
               span.setError(var26);
               throw var26;
            } finally {
               span.end();
            }
         }
      } catch (CJException var28) {
         throw SQLExceptionsMapping.translateException(var28, this.getExceptionInterceptor());
      }
   }

   @Override
   public void rollback() throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            this.checkClosed();

            try {
               if (this.connectionLifecycleInterceptors != null) {
                  IterateBlock<ConnectionLifecycleInterceptor> iter = new IterateBlock<ConnectionLifecycleInterceptor>(
                     this.connectionLifecycleInterceptors.iterator()
                  ) {
                     void forEach(ConnectionLifecycleInterceptor each) throws SQLException {
                        if (!each.rollback()) {
                           this.stopIterating = true;
                        }
                     }
                  };
                  iter.doForAll();
                  if (!iter.fullIteration()) {
                     return;
                  }
               }

               if (this.session.getServerSession().isAutoCommit()) {
                  throw SQLError.createSQLException(Messages.getString("Connection.20"), "08003", this.getExceptionInterceptor());
               }

               try {
                  this.rollbackNoChecks();
                  return;
               } catch (SQLException var16) {
                  if (!this.ignoreNonTxTables.getInitialValue() || var16.getErrorCode() != 1196) {
                     throw var16;
                  }
               }
            } catch (SQLException var17) {
               if ("08S01".equals(var17.getSQLState())) {
                  throw SQLError.createSQLException(Messages.getString("Connection.21"), "08007", this.getExceptionInterceptor());
               }

               throw var17;
            } finally {
               this.session.setNeedsPing(this.reconnectAtTxEnd.getValue());
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var20) {
         throw SQLExceptionsMapping.translateException(var20, this.getExceptionInterceptor());
      }
   }

   @Override
   public void rollback(final Savepoint savepoint) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            this.checkClosed();

            try {
               if (this.connectionLifecycleInterceptors != null) {
                  IterateBlock<ConnectionLifecycleInterceptor> iter = new IterateBlock<ConnectionLifecycleInterceptor>(
                     this.connectionLifecycleInterceptors.iterator()
                  ) {
                     void forEach(ConnectionLifecycleInterceptor each) throws SQLException {
                        if (!each.rollback(savepoint)) {
                           this.stopIterating = true;
                        }
                     }
                  };
                  iter.doForAll();
                  if (!iter.fullIteration()) {
                     return;
                  }
               }

               StringBuilder rollbackQuery = new StringBuilder("ROLLBACK TO SAVEPOINT ");
               rollbackQuery.append(
                  StringUtils.quoteIdentifier(savepoint.getSavepointName(), this.session.getIdentifierQuoteString(), this.pedantic.getValue())
               );
               Statement stmt = null;

               try {
                  stmt = this.getMetadataSafeStatement();
                  stmt.executeUpdate(rollbackQuery.toString());
               } catch (SQLException var28) {
                  int errno = var28.getErrorCode();
                  if (errno == 1181) {
                     String msg = var28.getMessage();
                     if (msg != null) {
                        int indexOfError153 = msg.indexOf("153");
                        if (indexOfError153 != -1) {
                           throw SQLError.createSQLException(
                              Messages.getString("Connection.22", new Object[]{savepoint.getSavepointName()}), "S1009", errno, this.getExceptionInterceptor()
                           );
                        }
                     }
                  }

                  if (this.ignoreNonTxTables.getValue() && var28.getErrorCode() != 1196) {
                     throw var28;
                  } else if ("08S01".equals(var28.getSQLState())) {
                     throw SQLError.createSQLException(Messages.getString("Connection.23"), "08007", this.getExceptionInterceptor());
                  } else {
                     throw var28;
                  }
               } finally {
                  this.closeStatement(stmt);
               }
            } finally {
               this.session.setNeedsPing(this.reconnectAtTxEnd.getValue());
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var32) {
         throw SQLExceptionsMapping.translateException(var32, this.getExceptionInterceptor());
      }
   }

   private void rollbackNoChecks() throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            if (this.useLocalTransactionState.getValue() && !this.session.getServerSession().inTransactionOnServer()) {
               return;
            }

            TelemetrySpan span = this.session.getTelemetryHandler().startSpan(TelemetrySpanName.ROLLBACK);

            try (TelemetryScope scope = span.makeCurrent()) {
               span.setAttribute(TelemetryAttribute.DB_NAME, this.getDatabase());
               span.setAttribute(TelemetryAttribute.DB_OPERATION, "ROLLBACK");
               span.setAttribute(TelemetryAttribute.DB_STATEMENT, "ROLLBACK");
               span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
               span.setAttribute(TelemetryAttribute.DB_USER, this.getUser());
               span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
               span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
               this.session.execSQL(null, "ROLLBACK", -1, null, false, this.nullStatementResultSetFactory, null, false);
            } catch (Throwable var38) {
               span.setError(var38);
               throw var38;
            } finally {
               span.end();
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var41) {
         throw SQLExceptionsMapping.translateException(var41, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement serverPrepareStatement(String sql) throws SQLException {
      try {
         String nativeSql = this.processEscapeCodesForPrepStmts.getValue() ? this.nativeSQL(sql) : sql;
         return ServerPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.getDatabase(), 1003, 1007);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement serverPrepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
      try {
         String nativeSql = this.processEscapeCodesForPrepStmts.getValue() ? this.nativeSQL(sql) : sql;
         ClientPreparedStatement pStmt = ServerPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.getDatabase(), 1003, 1007);
         pStmt.setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
         return pStmt;
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement serverPrepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      try {
         String nativeSql = this.processEscapeCodesForPrepStmts.getValue() ? this.nativeSQL(sql) : sql;
         return ServerPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.getDatabase(), resultSetType, resultSetConcurrency);
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement serverPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      try {
         if (this.pedantic.getValue() && resultSetHoldability != 1) {
            throw SQLError.createSQLException(Messages.getString("Connection.17"), "S1009", this.getExceptionInterceptor());
         } else {
            return this.serverPrepareStatement(sql, resultSetType, resultSetConcurrency);
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement serverPrepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
      try {
         ClientPreparedStatement pStmt = (ClientPreparedStatement)this.serverPrepareStatement(sql);
         pStmt.setRetrieveGeneratedKeys(autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0);
         return pStmt;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public PreparedStatement serverPrepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
      try {
         ClientPreparedStatement pStmt = (ClientPreparedStatement)this.serverPrepareStatement(sql);
         pStmt.setRetrieveGeneratedKeys(autoGenKeyColNames != null && autoGenKeyColNames.length > 0);
         return pStmt;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setAutoCommit(final boolean autoCommitFlag) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            this.checkClosed();
            if (this.connectionLifecycleInterceptors != null) {
               IterateBlock<ConnectionLifecycleInterceptor> iter = new IterateBlock<ConnectionLifecycleInterceptor>(
                  this.connectionLifecycleInterceptors.iterator()
               ) {
                  void forEach(ConnectionLifecycleInterceptor each) throws SQLException {
                     if (!each.setAutoCommit(autoCommitFlag)) {
                        this.stopIterating = true;
                     }
                  }
               };
               iter.doForAll();
               if (!iter.fullIteration()) {
                  return;
               }
            }

            if (this.autoReconnectForPools.getValue()) {
               this.autoReconnect.setValue(true);
            }

            boolean isAutoCommit = this.session.getServerSession().isAutoCommit();

            try {
               boolean needsSetOnServer = true;
               if (this.useLocalSessionState.getValue() && isAutoCommit == autoCommitFlag) {
                  needsSetOnServer = false;
               } else if (!this.autoReconnect.getValue()) {
                  needsSetOnServer = this.getSession().isSetNeededForAutoCommitMode(autoCommitFlag);
               }

               this.session.getServerSession().setAutoCommit(autoCommitFlag);
               if (needsSetOnServer) {
                  TelemetrySpan span = this.session.getTelemetryHandler().startSpan(TelemetrySpanName.SET_VARIABLE, "autocommit");

                  try (TelemetryScope scope = span.makeCurrent()) {
                     span.setAttribute(TelemetryAttribute.DB_NAME, this.getDatabase());
                     span.setAttribute(TelemetryAttribute.DB_OPERATION, "SET");
                     span.setAttribute(TelemetryAttribute.DB_STATEMENT, "SET (...)");
                     span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
                     span.setAttribute(TelemetryAttribute.DB_USER, this.getUser());
                     span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
                     span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
                     this.session
                        .execSQL(
                           null, autoCommitFlag ? "SET autocommit=1" : "SET autocommit=0", -1, null, false, this.nullStatementResultSetFactory, null, false
                        );
                  } catch (Throwable var62) {
                     span.setError(var62);
                     throw var62;
                  } finally {
                     span.end();
                  }
               }
            } catch (CJCommunicationsException var64) {
               throw var64;
            } catch (CJException var65) {
               this.session.getServerSession().setAutoCommit(isAutoCommit);
               throw SQLError.createSQLException(
                  var65.getMessage(), var65.getSQLState(), var65.getVendorCode(), var65.isTransient(), var65, this.getExceptionInterceptor()
               );
            } finally {
               if (this.autoReconnectForPools.getValue()) {
                  this.autoReconnect.setValue(false);
               }
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var68) {
         throw SQLExceptionsMapping.translateException(var68, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setCatalog(String catalog) throws SQLException {
      try {
         if (this.propertySet.getEnumProperty(PropertyKey.databaseTerm).getValue() == PropertyDefinitions.DatabaseTerm.CATALOG) {
            this.setDatabase(catalog);
         }
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setDatabase(final String db) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            this.checkClosed();
            if (db == null) {
               throw SQLError.createSQLException("Database can not be null", "S1009", this.getExceptionInterceptor());
            }

            if (this.connectionLifecycleInterceptors != null) {
               IterateBlock<ConnectionLifecycleInterceptor> iter = new IterateBlock<ConnectionLifecycleInterceptor>(
                  this.connectionLifecycleInterceptors.iterator()
               ) {
                  void forEach(ConnectionLifecycleInterceptor each) throws SQLException {
                     if (!each.setDatabase(db)) {
                        this.stopIterating = true;
                     }
                  }
               };
               iter.doForAll();
               if (!iter.fullIteration()) {
                  return;
               }
            }

            if (this.useLocalSessionState.getValue()) {
               if (this.session.getServerSession().isLowerCaseTableNames()) {
                  if (this.database.equalsIgnoreCase(db)) {
                     return;
                  }
               } else if (this.database.equals(db)) {
                  return;
               }
            }

            TelemetrySpan span = this.session.getTelemetryHandler().startSpan(TelemetrySpanName.USE_DATABASE);

            try (TelemetryScope scope = span.makeCurrent()) {
               span.setAttribute(TelemetryAttribute.DB_NAME, db);
               span.setAttribute(TelemetryAttribute.DB_OPERATION, "USE");
               span.setAttribute(TelemetryAttribute.DB_STATEMENT, "USE (...)");
               span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
               span.setAttribute(TelemetryAttribute.DB_USER, this.getUser());
               span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
               span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
               String quotedId = this.session.getIdentifierQuoteString();
               StringBuilder query = new StringBuilder("USE ");
               query.append(StringUtils.quoteIdentifier(db, quotedId, this.pedantic.getValue()));
               this.session.execSQL(null, query.toString(), -1, null, false, this.nullStatementResultSetFactory, null, false);
               this.database = db;
            } catch (Throwable var40) {
               span.setError(var40);
               throw var40;
            } finally {
               span.end();
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var43) {
         throw SQLExceptionsMapping.translateException(var43, this.getExceptionInterceptor());
      }
   }

   @Override
   public String getDatabase() throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         String var2;
         try {
            var2 = this.database;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setFailedOver(boolean flag) {
   }

   @Override
   public void setHoldability(int arg0) throws SQLException {
      try {
         ;
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setInGlobalTx(boolean flag) {
      this.isInGlobalTx = flag;
   }

   @Override
   public void setReadOnly(boolean readOnlyFlag) throws SQLException {
      try {
         this.checkClosed();
         this.setReadOnlyInternal(readOnlyFlag);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setReadOnlyInternal(boolean readOnlyFlag) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            if (this.readOnlyPropagatesToServer.getValue()
               && this.versionMeetsMinimum(5, 6, 5)
               && (!this.useLocalSessionState.getValue() || readOnlyFlag != this.readOnly)) {
               TelemetrySpan span = this.session
                  .getTelemetryHandler()
                  .startSpan(TelemetrySpanName.SET_TRANSACTION_ACCESS_MODE, readOnlyFlag ? "read-only" : "read-write");

               try (TelemetryScope scope = span.makeCurrent()) {
                  span.setAttribute(TelemetryAttribute.DB_NAME, this.getDatabase());
                  span.setAttribute(TelemetryAttribute.DB_OPERATION, "SET");
                  span.setAttribute(TelemetryAttribute.DB_STATEMENT, "SET (...)");
                  span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
                  span.setAttribute(TelemetryAttribute.DB_USER, this.getUser());
                  span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
                  span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
                  this.session
                     .execSQL(
                        null,
                        "SET SESSION TRANSACTION " + (readOnlyFlag ? "READ ONLY" : "READ WRITE"),
                        -1,
                        null,
                        false,
                        this.nullStatementResultSetFactory,
                        null,
                        false
                     );
               } catch (Throwable var39) {
                  span.setError(var39);
                  throw var39;
               } finally {
                  span.end();
               }
            }

            this.readOnly = readOnlyFlag;
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var42) {
         throw SQLExceptionsMapping.translateException(var42, this.getExceptionInterceptor());
      }
   }

   @Override
   public Savepoint setSavepoint() throws SQLException {
      try {
         MysqlSavepoint savepoint = new MysqlSavepoint(this.getExceptionInterceptor());
         this.setSavepoint(savepoint);
         return savepoint;
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   private void setSavepoint(MysqlSavepoint savepoint) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            this.checkClosed();
            StringBuilder savePointQuery = new StringBuilder("SAVEPOINT ");
            savePointQuery.append(StringUtils.quoteIdentifier(savepoint.getSavepointName(), this.session.getIdentifierQuoteString(), this.pedantic.getValue()));
            Statement stmt = null;

            try {
               stmt = this.getMetadataSafeStatement();
               stmt.executeUpdate(savePointQuery.toString());
            } finally {
               this.closeStatement(stmt);
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var16) {
         throw SQLExceptionsMapping.translateException(var16, this.getExceptionInterceptor());
      }
   }

   @Override
   public Savepoint setSavepoint(String name) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         MysqlSavepoint var4;
         try {
            MysqlSavepoint savepoint = new MysqlSavepoint(name, this.getExceptionInterceptor());
            this.setSavepoint(savepoint);
            var4 = savepoint;
         } finally {
            connectionLock.unlock();
         }

         return var4;
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setTransactionIsolation(int level) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            this.checkClosed();
            String sql = null;
            boolean shouldSendSet = false;
            if (this.propertySet.getBooleanProperty(PropertyKey.alwaysSendSetIsolation).getValue()) {
               shouldSendSet = true;
            } else if (level != this.isolationLevel) {
               shouldSendSet = true;
            }

            if (this.useLocalSessionState.getValue()) {
               shouldSendSet = this.isolationLevel != level;
            }

            if (shouldSendSet) {
               TelemetrySpan span = this.session.getTelemetryHandler().startSpan(TelemetrySpanName.SET_TRANSACTION_ISOLATION);

               try (TelemetryScope scope = span.makeCurrent()) {
                  span.setAttribute(TelemetryAttribute.DB_NAME, this.getDatabase());
                  span.setAttribute(TelemetryAttribute.DB_OPERATION, "SET");
                  span.setAttribute(TelemetryAttribute.DB_STATEMENT, "SET (...)");
                  span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
                  span.setAttribute(TelemetryAttribute.DB_USER, this.getUser());
                  span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
                  span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
                  switch (level) {
                     case 0:
                        throw SQLError.createSQLException(Messages.getString("Connection.24"), this.getExceptionInterceptor());
                     case 1:
                        sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
                        break;
                     case 2:
                        sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED";
                        break;
                     case 3:
                     case 5:
                     case 6:
                     case 7:
                     default:
                        throw SQLError.createSQLException(Messages.getString("Connection.25", new Object[]{level}), "S1C00", this.getExceptionInterceptor());
                     case 4:
                        sql = "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ";
                        break;
                     case 8:
                        sql = "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE";
                  }

                  this.session.execSQL(null, sql, -1, null, false, this.nullStatementResultSetFactory, null, false);
                  this.isolationLevel = level;
               } catch (Throwable var41) {
                  span.setError(var41);
                  throw var41;
               } finally {
                  span.end();
               }
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var44) {
         throw SQLExceptionsMapping.translateException(var44, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            this.typeMap = map;
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   private void setupServerForTruncationChecks() throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            RuntimeProperty<Boolean> jdbcCompliantTruncation = this.propertySet.getProperty(PropertyKey.jdbcCompliantTruncation);
            if (jdbcCompliantTruncation.getValue()) {
               String currentSqlMode = this.session.getServerSession().getServerVariable("sql_mode");
               boolean strictTransTablesIsSet = StringUtils.indexOfIgnoreCase(currentSqlMode, "STRICT_TRANS_TABLES") != -1;
               if (currentSqlMode == null || currentSqlMode.length() == 0 || !strictTransTablesIsSet) {
                  TelemetrySpan span = this.session.getTelemetryHandler().startSpan(TelemetrySpanName.SET_VARIABLE, "sql_mode");

                  try (TelemetryScope scope = span.makeCurrent()) {
                     span.setAttribute(TelemetryAttribute.DB_NAME, this.getDatabase());
                     span.setAttribute(TelemetryAttribute.DB_OPERATION, "SET");
                     span.setAttribute(TelemetryAttribute.DB_STATEMENT, "SET (...)");
                     span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
                     span.setAttribute(TelemetryAttribute.DB_USER, this.getUser());
                     span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
                     span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
                     StringBuilder commandBuf = new StringBuilder("SET sql_mode='");
                     if (currentSqlMode != null && currentSqlMode.length() > 0) {
                        commandBuf.append(currentSqlMode);
                        commandBuf.append(",");
                     }

                     commandBuf.append("STRICT_TRANS_TABLES'");
                     this.session.execSQL(null, commandBuf.toString(), -1, null, false, this.nullStatementResultSetFactory, null, false);
                     jdbcCompliantTruncation.setValue(false);
                  } catch (Throwable var41) {
                     span.setError(var41);
                     throw var41;
                  } finally {
                     span.end();
                  }
               } else if (strictTransTablesIsSet) {
                  jdbcCompliantTruncation.setValue(false);
               }
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var44) {
         throw SQLExceptionsMapping.translateException(var44, this.getExceptionInterceptor());
      }
   }

   @Override
   public void shutdownServer() throws SQLException {
      try {
         try {
            this.session.shutdownServer();
         } catch (CJException var4) {
            SQLException sqlEx = SQLError.createSQLException(
               Messages.getString("Connection.UnhandledExceptionDuringShutdown"), "S1000", this.getExceptionInterceptor()
            );
            sqlEx.initCause(var4);
            throw sqlEx;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public void unregisterStatement(JdbcStatement stmt) {
      this.openStatements.remove(stmt);
   }

   public boolean versionMeetsMinimum(int major, int minor, int subminor) {
      try {
         this.checkClosed();
         return this.session.versionMeetsMinimum(major, minor, subminor);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public CachedResultSetMetaData getCachedMetaData(String sql) {
      if (this.resultSetMetadataCache != null) {
         this.resultSetMetadataCacheLock.lock();

         CachedResultSetMetaData var2;
         try {
            var2 = this.resultSetMetadataCache.get(sql);
         } finally {
            this.resultSetMetadataCacheLock.unlock();
         }

         return var2;
      } else {
         return null;
      }
   }

   @Override
   public void initializeResultsMetadataFromCache(String sql, CachedResultSetMetaData cachedMetaData, ResultSetInternalMethods resultSet) throws SQLException {
      try {
         if (cachedMetaData == null) {
            CachedResultSetMetaData var6 = new CachedResultSetMetaDataImpl();
            resultSet.getColumnDefinition().buildIndexMapping();
            resultSet.initializeWithMetadata();
            if (resultSet instanceof UpdatableResultSet) {
               ((UpdatableResultSet)resultSet).checkUpdatability();
            }

            resultSet.populateCachedMetaData(var6);
            this.resultSetMetadataCache.put(sql, var6);
         } else {
            resultSet.getColumnDefinition().initializeFrom(cachedMetaData);
            resultSet.initializeWithMetadata();
            if (resultSet instanceof UpdatableResultSet) {
               ((UpdatableResultSet)resultSet).checkUpdatability();
            }
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public String getStatementComment() {
      return this.session.getQueryComment();
   }

   @Override
   public void setStatementComment(String comment) {
      this.session.setQueryComment(comment);
   }

   @Override
   public void transactionBegun() {
      Lock connectionLock = this.getConnectionLock();
      connectionLock.lock();

      try {
         if (this.connectionLifecycleInterceptors != null) {
            this.connectionLifecycleInterceptors.stream().forEach(ConnectionLifecycleInterceptor::transactionBegun);
         }
      } finally {
         connectionLock.unlock();
      }
   }

   @Override
   public void transactionCompleted() {
      Lock connectionLock = this.getConnectionLock();
      connectionLock.lock();

      try {
         if (this.connectionLifecycleInterceptors != null) {
            this.connectionLifecycleInterceptors.stream().forEach(ConnectionLifecycleInterceptor::transactionCompleted);
         }
      } finally {
         connectionLock.unlock();
      }
   }

   @Override
   public boolean storesLowerCaseTableName() {
      return this.session.getServerSession().storesLowerCaseTableNames();
   }

   @Override
   public ExceptionInterceptor getExceptionInterceptor() {
      return this.exceptionInterceptor;
   }

   @Override
   public boolean isServerLocal() throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         boolean ex;
         try {
            ex = this.session.isServerLocal(this.getSession());
         } catch (CJException var9) {
            SQLException sqlEx = SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
            throw sqlEx;
         } finally {
            connectionLock.unlock();
         }

         return ex;
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getSessionMaxRows() {
      Lock connectionLock = this.getConnectionLock();
      connectionLock.lock();

      int var2;
      try {
         var2 = this.session.getSessionMaxRows();
      } finally {
         connectionLock.unlock();
      }

      return var2;
   }

   @Override
   public void setSessionMaxRows(int max) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            this.checkClosed();
            if (this.session.getSessionMaxRows() != max) {
               TelemetrySpan span = this.session.getTelemetryHandler().startSpan(TelemetrySpanName.SET_VARIABLE, "sql_select_limit");

               try (TelemetryScope scope = span.makeCurrent()) {
                  span.setAttribute(TelemetryAttribute.DB_NAME, this.getDatabase());
                  span.setAttribute(TelemetryAttribute.DB_OPERATION, "SET");
                  span.setAttribute(TelemetryAttribute.DB_STATEMENT, "SET (...)");
                  span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
                  span.setAttribute(TelemetryAttribute.DB_USER, this.getUser());
                  span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
                  span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
                  this.session.setSessionMaxRows(max);
                  this.session
                     .execSQL(
                        null,
                        "SET sql_select_limit=" + (this.session.getSessionMaxRows() == -1 ? "DEFAULT" : this.session.getSessionMaxRows()),
                        -1,
                        null,
                        false,
                        this.nullStatementResultSetFactory,
                        null,
                        false
                     );
               } catch (Throwable var39) {
                  span.setError(var39);
                  throw var39;
               } finally {
                  span.end();
               }
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var42) {
         throw SQLExceptionsMapping.translateException(var42, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setSchema(String schema) throws SQLException {
      try {
         this.checkClosed();
         if (this.propertySet.getEnumProperty(PropertyKey.databaseTerm).getValue() == PropertyDefinitions.DatabaseTerm.SCHEMA) {
            this.setDatabase(schema);
         }
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public String getSchema() throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         String var2;
         try {
            this.checkClosed();
            var2 = this.propertySet.getEnumProperty(PropertyKey.databaseTerm).getValue() == PropertyDefinitions.DatabaseTerm.SCHEMA ? this.database : null;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public void abort(Executor executor) throws SQLException {
      try {
         SecurityManager sec = System.getSecurityManager();
         if (sec != null) {
            sec.checkPermission(ABORT_PERM);
         }

         if (executor == null) {
            throw SQLError.createSQLException(Messages.getString("Connection.26"), "S1009", this.getExceptionInterceptor());
         } else {
            executor.execute(() -> {
               try {
                  this.abortInternal();
               } catch (SQLException var2x) {
                  throw new RuntimeException(var2x);
               }
            });
         }
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            SecurityManager sec = System.getSecurityManager();
            if (sec != null) {
               sec.checkPermission(SET_NETWORK_TIMEOUT_PERM);
            }

            if (executor == null) {
               throw SQLError.createSQLException(Messages.getString("Connection.26"), "S1009", this.getExceptionInterceptor());
            }

            this.checkClosed();
            executor.execute(new ConnectionImpl.NetworkTimeoutSetter(this, milliseconds));
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getNetworkTimeout() throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         int var2;
         try {
            this.checkClosed();
            var2 = this.session.getSocketTimeout();
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public java.sql.Clob createClob() {
      try {
         return new Clob(this.getExceptionInterceptor());
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public java.sql.Blob createBlob() {
      try {
         return new Blob(this.getExceptionInterceptor());
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public java.sql.NClob createNClob() {
      try {
         return new NClob(this.getExceptionInterceptor());
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public SQLXML createSQLXML() throws SQLException {
      try {
         return new MysqlSQLXML(this.getExceptionInterceptor());
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isValid(int timeout) throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         try {
            if (this.isClosed()) {
               return false;
            } else {
               try {
                  try {
                     this.pingInternal(false, timeout * 1000);
                  } catch (Throwable var13) {
                     try {
                        this.abortInternal();
                     } catch (Throwable var12) {
                     }

                     return false;
                  }
               } catch (Throwable var14) {
                  return false;
               }

               return true;
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var16) {
         throw SQLExceptionsMapping.translateException(var16, this.getExceptionInterceptor());
      }
   }

   @Override
   public ClientInfoProvider getClientInfoProviderImpl() throws SQLException {
      try {
         Lock connectionLock = this.getConnectionLock();
         connectionLock.lock();

         ClientInfoProvider var15;
         try {
            if (this.infoProvider == null) {
               String clientInfoProvider = this.propertySet.getStringProperty(PropertyKey.clientInfoProvider).getStringValue();

               try {
                  this.infoProvider = Util.getInstance(ClientInfoProvider.class, clientInfoProvider, null, null, this.getExceptionInterceptor());
               } catch (CJException var12) {
                  if (!ClassNotFoundException.class.isInstance(var12.getCause())) {
                     throw SQLExceptionsMapping.translateException(var12, this.getExceptionInterceptor());
                  }

                  try {
                     this.infoProvider = Util.getInstance(
                        ClientInfoProvider.class,
                        "org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc." + clientInfoProvider,
                        null,
                        null,
                        this.getExceptionInterceptor()
                     );
                  } catch (CJException var11) {
                     throw SQLExceptionsMapping.translateException(var12, this.getExceptionInterceptor());
                  }
               }

               this.infoProvider.initialize(this, this.props);
            }

            var15 = this.infoProvider;
         } finally {
            connectionLock.unlock();
         }

         return var15;
      } catch (CJException var14) {
         throw SQLExceptionsMapping.translateException(var14, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setClientInfo(String name, String value) throws SQLClientInfoException {
      try {
         this.getClientInfoProviderImpl().setClientInfo(this, name, value);
      } catch (SQLClientInfoException var5) {
         throw var5;
      } catch (CJException | SQLException var6) {
         SQLClientInfoException clientInfoEx = new SQLClientInfoException();
         clientInfoEx.initCause(var6);
         throw clientInfoEx;
      }
   }

   @Override
   public void setClientInfo(Properties properties) throws SQLClientInfoException {
      try {
         this.getClientInfoProviderImpl().setClientInfo(this, properties);
      } catch (SQLClientInfoException var4) {
         throw var4;
      } catch (CJException | SQLException var5) {
         SQLClientInfoException clientInfoEx = new SQLClientInfoException();
         clientInfoEx.initCause(var5);
         throw clientInfoEx;
      }
   }

   @Override
   public String getClientInfo(String name) throws SQLException {
      try {
         return this.getClientInfoProviderImpl().getClientInfo(this, name);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public Properties getClientInfo() throws SQLException {
      try {
         return this.getClientInfoProviderImpl().getClientInfo(this);
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
      try {
         throw SQLError.createSQLFeatureNotSupportedException();
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
      try {
         throw SQLError.createSQLFeatureNotSupportedException();
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      try {
         try {
            return iface.cast(this);
         } catch (ClassCastException var4) {
            throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.getExceptionInterceptor());
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      try {
         this.checkClosed();
         return iface.isInstance(this);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   public NativeSession getSession() {
      return this.session;
   }

   @Override
   public String getHostPortPair() {
      return this.origHostInfo.getHostPortPair();
   }

   @Override
   public void handleNormalClose() {
      try {
         this.close();
      } catch (SQLException var2) {
         ExceptionFactory.createException(var2.getMessage(), var2);
      }
   }

   @Override
   public void handleReconnect() {
      this.createNewIO(true);
   }

   @Override
   public void handleCleanup(Throwable whyCleanedUp) {
      this.cleanup(whyCleanedUp);
   }

   @Override
   public ServerSessionStateController getServerSessionStateController() {
      return this.session.getServerSession().getServerSessionStateController();
   }

   static {
      mapTransIsolationNameToValue = new HashMap<>(8);
      mapTransIsolationNameToValue.put("READ-UNCOMMITED", 1);
      mapTransIsolationNameToValue.put("READ-UNCOMMITTED", 1);
      mapTransIsolationNameToValue.put("READ-COMMITTED", 2);
      mapTransIsolationNameToValue.put("REPEATABLE-READ", 4);
      mapTransIsolationNameToValue.put("SERIALIZABLE", 8);
   }

   static class CompoundCacheKey {
      final String componentOne;
      final String componentTwo;
      final int hashCode;

      CompoundCacheKey(String partOne, String partTwo) {
         this.componentOne = partOne;
         this.componentTwo = partTwo;
         int hc = 17;
         hc = 31 * hc + (this.componentOne != null ? this.componentOne.hashCode() : 0);
         hc = 31 * hc + (this.componentTwo != null ? this.componentTwo.hashCode() : 0);
         this.hashCode = hc;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else {
            if (obj != null && ConnectionImpl.CompoundCacheKey.class.isAssignableFrom(obj.getClass())) {
               ConnectionImpl.CompoundCacheKey another = (ConnectionImpl.CompoundCacheKey)obj;
               if (this.componentOne == null ? another.componentOne == null : this.componentOne.equals(another.componentOne)) {
                  return this.componentTwo == null ? another.componentTwo == null : this.componentTwo.equals(another.componentTwo);
               }
            }

            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.hashCode;
      }
   }

   private static class NetworkTimeoutSetter implements Runnable {
      private final WeakReference<JdbcConnection> connRef;
      private final int milliseconds;

      public NetworkTimeoutSetter(JdbcConnection conn, int milliseconds) {
         this.connRef = new WeakReference<>(conn);
         this.milliseconds = milliseconds;
      }

      @Override
      public void run() {
         JdbcConnection conn = this.connRef.get();
         if (conn != null) {
            Lock connectionLock = conn.getConnectionLock();
            connectionLock.lock();

            try {
               ((NativeSession)conn.getSession()).setSocketTimeout(this.milliseconds);
            } finally {
               connectionLock.unlock();
            }
         }
      }
   }
}
