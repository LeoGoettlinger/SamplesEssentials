package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.CancelQueryTask;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.MysqlType;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.NativeSession;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.PingTarget;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Query;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.QueryAttributesBindings;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.QueryInfo;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.QueryReturnType;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Session;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.SimpleQuery;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.TransactionEventHandler;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.HostInfo;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyDefinitions;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.RuntimeProperty;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.AssertionFailedException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJOperationNotSupportedException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJTimeoutException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionInterceptor;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.OperationCancelledException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.StatementIsClosedException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.MySQLStatementCancelledException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.MySQLTimeoutException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.SQLError;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.CachedResultSetMetaData;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.ResultSetFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.ResultSetImpl;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.ResultSetInternalMethods;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Message;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntityFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Resultset;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.NativeMessageBuilder;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.result.ByteArrayRow;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.result.ResultsetRowsStatic;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.DefaultColumnDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.Field;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.Row;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryAttribute;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryScope;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetrySpan;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetrySpanName;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.StringUtils;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.Util;

public class StatementImpl implements JdbcStatement {
   protected static final String PING_MARKER = "/* ping */";
   public static final byte USES_VARIABLES_FALSE = 0;
   public static final byte USES_VARIABLES_TRUE = 1;
   public static final byte USES_VARIABLES_UNKNOWN = -1;
   protected NativeMessageBuilder commandBuilder = null;
   protected String charEncoding = null;
   protected volatile JdbcConnection connection = null;
   protected boolean doEscapeProcessing = true;
   protected boolean isClosed = false;
   protected long lastInsertId = -1L;
   protected int maxFieldSize = (Integer)PropertyDefinitions.getPropertyDefinition(PropertyKey.maxAllowedPacket).getDefaultValue();
   public int maxRows = -1;
   private Set<ResultSetInternalMethods> openResultSets = new HashSet<>();
   protected boolean pedantic = false;
   protected boolean profileSQL = false;
   protected ResultSetInternalMethods results = null;
   protected ResultSetInternalMethods generatedKeysResults = null;
   protected int resultSetConcurrency = 0;
   protected long updateCount = -1L;
   protected boolean useUsageAdvisor = false;
   protected SQLWarning warningChain = null;
   protected boolean holdResultsOpenOverClose = false;
   protected ArrayList<Row> batchedGeneratedKeys = null;
   protected boolean retrieveGeneratedKeys = false;
   protected boolean continueBatchOnError = false;
   protected PingTarget pingTarget = null;
   protected ExceptionInterceptor exceptionInterceptor;
   protected boolean lastQueryIsOnDupKeyUpdate = false;
   protected RuntimeProperty<Boolean> dontTrackOpenResources;
   protected RuntimeProperty<Boolean> dumpQueriesOnException;
   protected boolean logSlowQueries = false;
   protected RuntimeProperty<Boolean> rewriteBatchedStatements;
   protected RuntimeProperty<Integer> maxAllowedPacket;
   protected boolean dontCheckOnDuplicateKeyUpdateInSQL;
   protected ResultSetFactory resultSetFactory;
   protected Query query;
   protected NativeSession session = null;
   private Resultset.Type originalResultSetType = Resultset.Type.FORWARD_ONLY;
   private int originalFetchSize = 0;
   private boolean isPoolable = false;
   private boolean closeOnCompletion = false;

   public StatementImpl(JdbcConnection c, String db) throws SQLException {
      if (c != null && !c.isClosed()) {
         this.connection = c;
         this.session = (NativeSession)c.getSession();
         this.exceptionInterceptor = c.getExceptionInterceptor();
         this.commandBuilder = new NativeMessageBuilder(this.session.getServerSession().supportsQueryAttributes());

         try {
            this.initQuery();
         } catch (CJException var6) {
            throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
         }

         this.query.setCurrentDatabase(db);
         JdbcPropertySet pset = c.getPropertySet();
         this.dontTrackOpenResources = pset.getBooleanProperty(PropertyKey.dontTrackOpenResources);
         this.dumpQueriesOnException = pset.getBooleanProperty(PropertyKey.dumpQueriesOnException);
         this.continueBatchOnError = pset.getBooleanProperty(PropertyKey.continueBatchOnError).getValue();
         this.pedantic = pset.getBooleanProperty(PropertyKey.pedantic).getValue();
         this.rewriteBatchedStatements = pset.getBooleanProperty(PropertyKey.rewriteBatchedStatements);
         this.charEncoding = pset.getStringProperty(PropertyKey.characterEncoding).getValue();
         this.profileSQL = pset.getBooleanProperty(PropertyKey.profileSQL).getValue();
         this.useUsageAdvisor = pset.getBooleanProperty(PropertyKey.useUsageAdvisor).getValue();
         this.logSlowQueries = pset.getBooleanProperty(PropertyKey.logSlowQueries).getValue();
         this.maxAllowedPacket = pset.getIntegerProperty(PropertyKey.maxAllowedPacket);
         this.dontCheckOnDuplicateKeyUpdateInSQL = pset.getBooleanProperty(PropertyKey.dontCheckOnDuplicateKeyUpdateInSQL).getValue();
         this.doEscapeProcessing = pset.getBooleanProperty(PropertyKey.enableEscapeProcessing).getValue();
         this.maxFieldSize = this.maxAllowedPacket.getValue();
         if (!this.dontTrackOpenResources.getValue()) {
            c.registerStatement(this);
         }

         int defaultFetchSize = pset.getIntegerProperty(PropertyKey.defaultFetchSize).getValue();
         if (defaultFetchSize != 0) {
            this.setFetchSize(defaultFetchSize);
         }

         int maxRowsConn = pset.getIntegerProperty(PropertyKey.maxRows).getValue();
         if (maxRowsConn != -1) {
            this.setMaxRows(maxRowsConn);
         }

         this.holdResultsOpenOverClose = pset.getBooleanProperty(PropertyKey.holdResultsOpenOverStatementClose).getValue();
         this.resultSetFactory = new ResultSetFactory(this.connection, this);
      } else {
         throw SQLError.createSQLException(Messages.getString("Statement.0"), "08003", null);
      }
   }

   protected void initQuery() {
      this.query = new SimpleQuery(this.session);
   }

   @Override
   public void addBatch(String sql) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            if (sql != null) {
               this.query.addBatch(sql);
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public void addBatch(Object batch) {
      this.query.addBatch(batch);
   }

   @Override
   public List<Object> getBatchedArgs() {
      return this.query.getBatchedArgs();
   }

   @Override
   public void cancel() throws SQLException {
      try {
         if (this.query.getStatementExecuting().get()) {
            if (!this.isClosed && this.connection != null) {
               NativeSession newSession = null;

               try {
                  HostInfo hostInfo = this.session.getHostInfo();
                  String database = hostInfo.getDatabase();
                  String user = hostInfo.getUser();
                  String password = hostInfo.getPassword();
                  newSession = new NativeSession(this.session.getHostInfo(), this.session.getPropertySet());
                  TelemetrySpan span = newSession.getTelemetryHandler().startSpan(TelemetrySpanName.CANCEL_QUERY);

                  try (TelemetryScope scope = span.makeCurrent()) {
                     span.setAttribute(TelemetryAttribute.DB_NAME, database);
                     span.setAttribute(TelemetryAttribute.DB_OPERATION, "KILL");
                     span.setAttribute(TelemetryAttribute.DB_STATEMENT, "KILL (...)");
                     span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
                     span.setAttribute(TelemetryAttribute.DB_USER, user);
                     span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
                     span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
                     newSession.connect(hostInfo, user, password, database, 30000, new TransactionEventHandler() {
                        @Override
                        public void transactionCompleted() {
                        }

                        @Override
                        public void transactionBegun() {
                        }
                     });
                     newSession.getProtocol()
                        .sendCommand(
                           new NativeMessageBuilder(newSession.getServerSession().supportsQueryAttributes())
                              .buildComQuery(newSession.getSharedSendPacket(), newSession, "KILL QUERY " + this.session.getThreadId()),
                           false,
                           0
                        );
                     this.setCancelStatus(Query.CancelStatus.CANCELED_BY_USER);
                  } catch (Throwable var45) {
                     span.setError(var45);
                     throw var45;
                  } finally {
                     span.end();
                  }
               } catch (IOException var47) {
                  throw SQLExceptionsMapping.translateException(var47, this.exceptionInterceptor);
               } finally {
                  if (newSession != null) {
                     newSession.forceClose();
                  }
               }
            }
         }
      } catch (CJException var49) {
         throw SQLExceptionsMapping.translateException(var49, this.getExceptionInterceptor());
      }
   }

   protected JdbcConnection checkClosed() {
      JdbcConnection c = this.connection;
      if (c == null) {
         throw (StatementIsClosedException)ExceptionFactory.createException(
            StatementIsClosedException.class, Messages.getString("Statement.AlreadyClosed"), this.getExceptionInterceptor()
         );
      } else {
         return c;
      }
   }

   protected boolean isResultSetProducingQuery(String sql) {
      QueryReturnType queryReturnType = QueryInfo.getQueryReturnType(sql, this.session.getServerSession().isNoBackslashEscapesSet());
      return queryReturnType == QueryReturnType.PRODUCES_RESULT_SET || queryReturnType == QueryReturnType.MAY_PRODUCE_RESULT_SET;
   }

   protected boolean isNonResultSetProducingQuery(String sql) {
      QueryReturnType queryReturnType = QueryInfo.getQueryReturnType(sql, this.session.getServerSession().isNoBackslashEscapesSet());
      return queryReturnType == QueryReturnType.DOES_NOT_PRODUCE_RESULT_SET
         || queryReturnType == QueryReturnType.MAY_PRODUCE_RESULT_SET
         || queryReturnType == QueryReturnType.NONE;
   }

   protected void checkNullOrEmptyQuery(String sql) throws SQLException {
      if (sql == null) {
         throw SQLError.createSQLException(Messages.getString("Statement.59"), "S1009", this.getExceptionInterceptor());
      } else if (sql.length() == 0) {
         throw SQLError.createSQLException(Messages.getString("Statement.61"), "S1009", this.getExceptionInterceptor());
      }
   }

   @Override
   public void clearBatch() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            this.query.clearBatchedArgs();
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   public void clearBatchedArgs() {
      this.query.clearBatchedArgs();
   }

   @Override
   public void clearWarnings() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            this.setClearWarningsCalled(true);
            this.warningChain = null;
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   public void close() throws SQLException {
      try {
         this.doClose(CloseOption.PROPAGATE);
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   protected void closeAllOpenResults() throws SQLException {
      JdbcConnection locallyScopedConn = this.connection;
      if (locallyScopedConn != null) {
         Lock connectionLock = locallyScopedConn.getConnectionLock();
         connectionLock.lock();

         try {
            if (this.openResultSets != null) {
               this.openResultSets.forEach(rs -> {
                  try {
                     rs.doClose(CloseOption.IMPLICIT);
                  } catch (SQLException var2x) {
                     throw AssertionFailedException.shouldNotHappen(var2x);
                  }
               });
               this.openResultSets.clear();
            }
         } finally {
            connectionLock.unlock();
         }
      }
   }

   protected void implicitlyCloseAllOpenResults() throws SQLException {
      if (!this.holdResultsOpenOverClose && !this.dontTrackOpenResources.getValue()) {
         if (this.results != null) {
            this.results.doClose(CloseOption.IMPLICIT);
         }

         if (this.generatedKeysResults != null) {
            this.generatedKeysResults.doClose(CloseOption.IMPLICIT);
         }

         this.closeAllOpenResults();
      }
   }

   @Override
   public void notifyResultSetClose(ResultSetInternalMethods rs) {
      try {
         try {
            Lock connectionLock = this.checkClosed().getConnectionLock();
            connectionLock.lock();

            try {
               if (this.openResultSets != null) {
                  this.openResultSets.remove(rs);
               }

               boolean hasMoreResults = rs.getNextResultset() != null;
               if (this.results == rs && !hasMoreResults) {
                  this.results = null;
               }

               if (this.generatedKeysResults == rs) {
                  this.generatedKeysResults = null;
               }

               if (!hasMoreResults) {
                  this.checkAndPerformCloseOnCompletionAction();
               }
            } finally {
               connectionLock.unlock();
            }
         } catch (StatementIsClosedException var10) {
         }
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getOpenResultSetCount() {
      try {
         try {
            Lock connectionLock = this.checkClosed().getConnectionLock();
            connectionLock.lock();

            int var2;
            try {
               var2 = this.openResultSets != null ? this.openResultSets.size() : 0;
            } finally {
               connectionLock.unlock();
            }

            return var2;
         } catch (StatementIsClosedException var9) {
            return 0;
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   private void checkAndPerformCloseOnCompletionAction() {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            if (this.isCloseOnCompletion()
               && !this.dontTrackOpenResources.getValue()
               && this.getOpenResultSetCount() == 0
               && (this.results == null || !this.results.hasRows() || this.results.isClosed())
               && (this.generatedKeysResults == null || !this.generatedKeysResults.hasRows() || this.generatedKeysResults.isClosed())) {
               this.doClose(CloseOption.IMPLICIT);
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (SQLException var6) {
      }
   }

   private ResultSetInternalMethods createResultSetUsingServerFetch(String sql) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         ResultSetInternalMethods var5;
         try {
            PreparedStatement pStmt = this.connection.prepareStatement(sql, this.query.getResultType().getIntValue(), this.resultSetConcurrency);
            pStmt.setFetchSize(this.query.getResultFetchSize());
            if (this.getQueryTimeout() > 0) {
               pStmt.setQueryTimeout(this.getQueryTimeout());
            }

            if (this.maxRows > -1) {
               pStmt.setMaxRows(this.maxRows);
            }

            this.statementBegins();
            pStmt.execute();
            ResultSetInternalMethods rs = ((JdbcStatement)pStmt).getResultSetInternal();
            rs.setStatementUsedForFetchingRows((JdbcPreparedStatement)pStmt);
            this.results = rs;
            var5 = rs;
         } finally {
            connectionLock.unlock();
         }

         return var5;
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   protected boolean meetsConditionsForStreamingResultSet() {
      return this.query.getResultType() == Resultset.Type.FORWARD_ONLY
         && this.resultSetConcurrency == 1007
         && this.query.getResultFetchSize() == Integer.MIN_VALUE;
   }

   @Override
   public void enableStreamingResults() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            this.originalResultSetType = this.query.getResultType();
            this.originalFetchSize = this.query.getResultFetchSize();
            this.setFetchSize(Integer.MIN_VALUE);
            this.setResultSetType(Resultset.Type.FORWARD_ONLY);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   public void disableStreamingResults() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            if (this.query.getResultFetchSize() == Integer.MIN_VALUE && this.query.getResultType() == Resultset.Type.FORWARD_ONLY) {
               this.setFetchSize(this.originalFetchSize);
               this.setResultSetType(this.originalResultSetType);
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   protected void setupStreamingTimeout(JdbcConnection con) throws SQLException {
      int netTimeoutForStreamingResults = this.session.getPropertySet().getIntegerProperty(PropertyKey.netTimeoutForStreamingResults).getValue();
      if (this.meetsConditionsForStreamingResultSet() && netTimeoutForStreamingResults > 0) {
         TelemetrySpan span = this.session.getTelemetryHandler().startSpan(TelemetrySpanName.SET_VARIABLE, "net_write_timeout");

         try (TelemetryScope scope = span.makeCurrent()) {
            span.setAttribute(TelemetryAttribute.DB_NAME, this.getCurrentDatabase());
            span.setAttribute(TelemetryAttribute.DB_OPERATION, "SET");
            span.setAttribute(TelemetryAttribute.DB_STATEMENT, "SET (...)");
            span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
            span.setAttribute(TelemetryAttribute.DB_USER, this.connection.getUser());
            span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
            span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
            this.executeSimpleNonQuery(con, "SET net_write_timeout=" + netTimeoutForStreamingResults);
         } catch (Throwable var25) {
            span.setError(var25);
            throw var25;
         } finally {
            span.end();
         }
      }
   }

   @Override
   public CancelQueryTask startQueryTimer(Query stmtToCancel, long timeout) {
      return this.query.startQueryTimer(stmtToCancel, timeout);
   }

   @Override
   public void stopQueryTimer(CancelQueryTask timeoutTask, boolean rethrowCancelReason, boolean checkCancelTimeout) {
      this.query.stopQueryTimer(timeoutTask, rethrowCancelReason, checkCancelTimeout);
   }

   @Override
   public boolean execute(String sql) throws SQLException {
      try {
         return this.executeInternal(sql, false);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean execute(String sql, int returnGeneratedKeys) throws SQLException {
      try {
         return this.executeInternal(sql, returnGeneratedKeys == 1);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean execute(String sql, int[] generatedKeyIndices) throws SQLException {
      try {
         return this.executeInternal(sql, generatedKeyIndices != null && generatedKeyIndices.length > 0);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean execute(String sql, String[] generatedKeyNames) throws SQLException {
      try {
         return this.executeInternal(sql, generatedKeyNames != null && generatedKeyNames.length > 0);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   private boolean executeInternal(String sql, boolean returnGeneratedKeys) throws SQLException {
      try {
         JdbcConnection locallyScopedConn = this.checkClosed();
         Lock connectionLock = locallyScopedConn.getConnectionLock();
         connectionLock.lock();

         boolean cachedMetaData;
         try {
            this.checkClosed();
            TelemetrySpan span = this.getSession().getTelemetryHandler().startSpan(TelemetrySpanName.STMT_EXECUTE);

            try (TelemetryScope scope = span.makeCurrent()) {
               String dbOperation = QueryInfo.getStatementKeyword(sql, this.session.getServerSession().isNoBackslashEscapesSet());
               span.setAttribute(TelemetryAttribute.DB_NAME, this.getCurrentDatabase());
               span.setAttribute(TelemetryAttribute.DB_OPERATION, dbOperation);
               span.setAttribute(TelemetryAttribute.DB_STATEMENT, dbOperation + " (...)");
               span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
               span.setAttribute(TelemetryAttribute.DB_USER, this.connection.getUser());
               span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
               span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
               this.checkNullOrEmptyQuery(sql);
               this.resetCancelledState();
               this.implicitlyCloseAllOpenResults();
               this.clearWarnings();
               if (sql.charAt(0) != '/' || !sql.startsWith("/* ping */")) {
                  this.retrieveGeneratedKeys = returnGeneratedKeys;
                  this.lastQueryIsOnDupKeyUpdate = returnGeneratedKeys
                     && QueryInfo.firstCharOfStatementUc(sql, this.session.getServerSession().isNoBackslashEscapesSet()) == 'I'
                     && this.containsOnDuplicateKeyInString(sql);
                  if (!QueryInfo.isReadOnlySafeQuery(sql, this.session.getServerSession().isNoBackslashEscapesSet()) && locallyScopedConn.isReadOnly()) {
                     throw SQLError.createSQLException(
                        Messages.getString("Statement.27") + Messages.getString("Statement.28"), "S1009", this.getExceptionInterceptor()
                     );
                  }

                  try {
                     this.setupStreamingTimeout(locallyScopedConn);
                     if (this.doEscapeProcessing) {
                        Object escapedSqlResult = EscapeProcessor.escapeSQL(
                           sql,
                           this.session.getServerSession().getSessionTimeZone(),
                           this.session.getServerSession().getCapabilities().serverSupportsFracSecs(),
                           this.session.getServerSession().isServerTruncatesFracSecs(),
                           this.getExceptionInterceptor()
                        );
                        sql = escapedSqlResult instanceof String ? (String)escapedSqlResult : ((EscapeProcessorResult)escapedSqlResult).escapedSql;
                     }

                     CachedResultSetMetaData cachedMetaDatax = null;
                     ResultSetInternalMethods rs = null;
                     this.batchedGeneratedKeys = null;
                     if (this.useServerFetch()) {
                        rs = this.createResultSetUsingServerFetch(sql);
                     } else {
                        CancelQueryTask timeoutTask = null;
                        String oldDb = null;

                        try {
                           timeoutTask = this.startQueryTimer(this, this.getTimeoutInMillis());
                           if (!locallyScopedConn.getDatabase().equals(this.getCurrentDatabase())) {
                              oldDb = locallyScopedConn.getDatabase();
                              locallyScopedConn.setDatabase(this.getCurrentDatabase());
                           }

                           if (locallyScopedConn.getPropertySet().getBooleanProperty(PropertyKey.cacheResultSetMetadata).getValue()) {
                              cachedMetaDatax = locallyScopedConn.getCachedMetaData(sql);
                           }

                           locallyScopedConn.setSessionMaxRows(this.isResultSetProducingQuery(sql) ? this.maxRows : -1);
                           this.statementBegins();
                           rs = ((NativeSession)locallyScopedConn.getSession())
                              .execSQL(
                                 this, sql, this.maxRows, null, this.meetsConditionsForStreamingResultSet(), this.getResultSetFactory(), cachedMetaDatax, false
                              );
                           if (timeoutTask != null) {
                              this.stopQueryTimer(timeoutTask, true, true);
                              timeoutTask = null;
                           }
                        } catch (OperationCancelledException | CJTimeoutException var78) {
                           throw SQLExceptionsMapping.translateException(var78, this.exceptionInterceptor);
                        } finally {
                           this.stopQueryTimer(timeoutTask, false, false);
                           if (oldDb != null) {
                              locallyScopedConn.setDatabase(oldDb);
                           }
                        }
                     }

                     if (rs != null) {
                        this.lastInsertId = rs.getUpdateID();
                        this.results = rs;
                        rs.setFirstCharOfQuery(QueryInfo.firstCharOfStatementUc(sql, this.session.getServerSession().isNoBackslashEscapesSet()));
                        if (rs.hasRows()) {
                           if (cachedMetaDatax != null) {
                              locallyScopedConn.initializeResultsMetadataFromCache(sql, cachedMetaDatax, this.results);
                           } else if (this.session.getPropertySet().getBooleanProperty(PropertyKey.cacheResultSetMetadata).getValue()) {
                              locallyScopedConn.initializeResultsMetadataFromCache(sql, null, this.results);
                           }
                        }
                     }

                     return rs != null && rs.hasRows();
                  } finally {
                     this.query.getStatementExecuting().set(false);
                  }
               }

               this.doPingInstead();
               cachedMetaData = true;
            } catch (Throwable var83) {
               span.setError(var83);
               throw var83;
            } finally {
               span.end();
            }
         } finally {
            connectionLock.unlock();
         }

         return cachedMetaData;
      } catch (CJException var86) {
         throw SQLExceptionsMapping.translateException(var86, this.getExceptionInterceptor());
      }
   }

   @Override
   public void statementBegins() {
      this.query.statementBegins();
   }

   @Override
   public void resetCancelledState() {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            this.query.resetCancelledState();
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   public int[] executeBatch() throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeBatchInternal());
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   protected long[] executeBatchInternal() throws SQLException {
      JdbcConnection locallyScopedConn = this.checkClosed();
      Lock connectionLock = locallyScopedConn.getConnectionLock();
      connectionLock.lock();

      long[] var88;
      try {
         TelemetrySpan span = this.getSession().getTelemetryHandler().startSpan(TelemetrySpanName.STMT_EXECUTE_BATCH);

         try (TelemetryScope scope = span.makeCurrent()) {
            span.setAttribute(TelemetryAttribute.DB_NAME, this.getCurrentDatabase());
            span.setAttribute(TelemetryAttribute.DB_OPERATION, "(SQL batch)");
            span.setAttribute(TelemetryAttribute.DB_STATEMENT, "(SQL batch)");
            span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
            span.setAttribute(TelemetryAttribute.DB_USER, this.connection.getUser());
            span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
            span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
            if (locallyScopedConn.isReadOnly()) {
               throw SQLError.createSQLException(
                  Messages.getString("Statement.34") + Messages.getString("Statement.35"), "S1009", this.getExceptionInterceptor()
               );
            }

            this.implicitlyCloseAllOpenResults();
            List<Object> batchedArgs = this.query.getBatchedArgs();
            if (batchedArgs == null || batchedArgs.size() == 0) {
               return new long[0];
            }

            long individualStatementTimeout = this.getTimeoutInMillis();
            this.setTimeoutInMillis(0L);
            CancelQueryTask timeoutTask = null;

            try {
               this.resetCancelledState();
               this.statementBegins();
               this.clearWarnings();

               try {
                  this.retrieveGeneratedKeys = true;
                  long[] updateCounts = null;
                  if (batchedArgs != null) {
                     int nbrCommands = batchedArgs.size();
                     this.batchedGeneratedKeys = new ArrayList<>(batchedArgs.size());
                     boolean multiQueriesEnabled = locallyScopedConn.getPropertySet().getBooleanProperty(PropertyKey.allowMultiQueries).getValue();
                     if (multiQueriesEnabled || this.rewriteBatchedStatements.getValue() && nbrCommands > 4) {
                        return this.executeBatchUsingMultiQueries(multiQueriesEnabled, nbrCommands, individualStatementTimeout);
                     }

                     timeoutTask = this.startQueryTimer(this, individualStatementTimeout);
                     updateCounts = new long[nbrCommands];

                     for (int i = 0; i < nbrCommands; i++) {
                        updateCounts[i] = -3L;
                     }

                     SQLException sqlEx = null;
                     int commandIndex = 0;

                     for (int var91 = 0; var91 < nbrCommands; var91++) {
                        try {
                           String sql = (String)batchedArgs.get(var91);
                           updateCounts[var91] = this.executeUpdateInternal(sql, true, true);
                           if (timeoutTask != null) {
                              this.checkCancelTimeout();
                           }

                           this.getBatchedGeneratedKeys(this.results.getFirstCharOfQuery() == 'I' && this.containsOnDuplicateKeyInString(sql) ? 1 : 0);
                        } catch (SQLException var79) {
                           updateCounts[var91] = -3L;
                           if (!this.continueBatchOnError
                              || var79 instanceof MySQLTimeoutException
                              || var79 instanceof MySQLStatementCancelledException
                              || this.hasDeadlockOrTimeoutRolledBackTx(var79)) {
                              long[] newUpdateCounts = new long[var91];
                              if (this.hasDeadlockOrTimeoutRolledBackTx(var79)) {
                                 for (int i = 0; i < newUpdateCounts.length; i++) {
                                    newUpdateCounts[i] = -3L;
                                 }
                              } else {
                                 System.arraycopy(updateCounts, 0, newUpdateCounts, 0, var91);
                              }

                              sqlEx = var79;
                              break;
                           }

                           sqlEx = var79;
                        }
                     }

                     if (sqlEx != null) {
                        throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.getExceptionInterceptor());
                     }
                  }

                  if (timeoutTask != null) {
                     this.stopQueryTimer(timeoutTask, true, true);
                     timeoutTask = null;
                  }

                  var88 = updateCounts != null ? updateCounts : new long[0];
               } finally {
                  this.query.getStatementExecuting().set(false);
               }
            } finally {
               this.stopQueryTimer(timeoutTask, false, false);
               this.resetCancelledState();
               this.setTimeoutInMillis(individualStatementTimeout);
               this.clearBatch();
            }
         } catch (Throwable var84) {
            span.setError(var84);
            throw var84;
         } finally {
            span.end();
         }
      } finally {
         connectionLock.unlock();
      }

      return var88;
   }

   protected final boolean hasDeadlockOrTimeoutRolledBackTx(SQLException ex) {
      int vendorCode = ex.getErrorCode();
      switch (vendorCode) {
         case 1205:
            return false;
         case 1206:
         case 1213:
            return true;
         default:
            return false;
      }
   }

   private long[] executeBatchUsingMultiQueries(boolean multiQueriesEnabled, int nbrCommands, long individualStatementTimeout) throws SQLException {
      try {
         JdbcConnection locallyScopedConn = this.checkClosed();
         Lock connectionLock = locallyScopedConn.getConnectionLock();
         connectionLock.lock();

         long[] var63;
         try {
            if (!multiQueriesEnabled) {
               this.session.enableMultiQueries();
            }

            Statement batchStmt = null;
            CancelQueryTask timeoutTask = null;

            try {
               long[] updateCounts = new long[nbrCommands];

               for (int i = 0; i < nbrCommands; i++) {
                  updateCounts[i] = -3L;
               }

               int commandIndex = 0;
               StringBuilder queryBuf = new StringBuilder();
               batchStmt = locallyScopedConn.createStatement();
               JdbcStatement jdbcBatchedStmt = (JdbcStatement)batchStmt;
               this.getQueryAttributesBindings().runThroughAll(a -> jdbcBatchedStmt.setAttribute(a.getName(), a.getValue()));
               timeoutTask = this.startQueryTimer((StatementImpl)batchStmt, individualStatementTimeout);
               int counter = 0;
               String connectionEncoding = locallyScopedConn.getPropertySet().getStringProperty(PropertyKey.characterEncoding).getValue();
               int numberOfBytesPerChar = StringUtils.startsWithIgnoreCase(connectionEncoding, "utf")
                  ? 3
                  : (this.session.getServerSession().getCharsetSettings().isMultibyteCharset(connectionEncoding) ? 2 : 1);
               int escapeAdjust = 1;
               batchStmt.setEscapeProcessing(this.doEscapeProcessing);
               if (this.doEscapeProcessing) {
                  escapeAdjust = 2;
               }

               SQLException sqlEx = null;
               int argumentSetsInBatchSoFar = 0;

               for (commandIndex = 0; commandIndex < nbrCommands; commandIndex++) {
                  String nextQuery = (String)this.query.getBatchedArgs().get(commandIndex);
                  if (queryBuf.length() > 0
                     && ((queryBuf.length() + nextQuery.length()) * numberOfBytesPerChar + 1 + 4) * escapeAdjust + 32 > this.maxAllowedPacket.getValue()) {
                     try {
                        batchStmt.execute(queryBuf.toString(), 1);
                     } catch (SQLException var55) {
                        sqlEx = this.handleExceptionForBatch(commandIndex, argumentSetsInBatchSoFar, updateCounts, var55);
                     }

                     counter = this.processMultiCountsAndKeys((StatementImpl)batchStmt, counter, updateCounts);
                     queryBuf = new StringBuilder();
                     argumentSetsInBatchSoFar = 0;
                  }

                  queryBuf.append(nextQuery);
                  queryBuf.append(";");
                  argumentSetsInBatchSoFar++;
               }

               if (queryBuf.length() > 0) {
                  try {
                     batchStmt.execute(queryBuf.toString(), 1);
                  } catch (SQLException var54) {
                     sqlEx = this.handleExceptionForBatch(commandIndex - 1, argumentSetsInBatchSoFar, updateCounts, var54);
                  }

                  counter = this.processMultiCountsAndKeys((StatementImpl)batchStmt, counter, updateCounts);
               }

               if (timeoutTask != null) {
                  this.stopQueryTimer(timeoutTask, true, true);
                  timeoutTask = null;
               }

               if (sqlEx != null) {
                  throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.getExceptionInterceptor());
               }

               var63 = updateCounts != null ? updateCounts : new long[0];
            } finally {
               this.stopQueryTimer(timeoutTask, false, false);
               this.resetCancelledState();

               try {
                  if (batchStmt != null) {
                     batchStmt.close();
                  }
               } finally {
                  if (!multiQueriesEnabled) {
                     this.session.disableMultiQueries();
                  }
               }
            }
         } finally {
            connectionLock.unlock();
         }

         return var63;
      } catch (CJException var59) {
         throw SQLExceptionsMapping.translateException(var59, this.getExceptionInterceptor());
      }
   }

   protected int processMultiCountsAndKeys(StatementImpl batchedStatement, int updateCountCounter, long[] updateCounts) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         int var19;
         try {
            updateCounts[updateCountCounter++] = batchedStatement.getLargeUpdateCount();
            boolean doGenKeys = this.batchedGeneratedKeys != null;
            byte[][] row = (byte[][])null;
            if (doGenKeys) {
               long generatedKey = batchedStatement.getLastInsertID();
               row = new byte[][]{StringUtils.getBytes(Long.toString(generatedKey))};
               this.batchedGeneratedKeys.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
            }

            while (batchedStatement.getMoreResults() || batchedStatement.getLargeUpdateCount() != -1L) {
               updateCounts[updateCountCounter++] = batchedStatement.getLargeUpdateCount();
               if (doGenKeys) {
                  long generatedKey = batchedStatement.getLastInsertID();
                  row = new byte[][]{StringUtils.getBytes(Long.toString(generatedKey))};
                  this.batchedGeneratedKeys.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
               }
            }

            var19 = updateCountCounter;
         } finally {
            connectionLock.unlock();
         }

         return var19;
      } catch (CJException var14) {
         throw SQLExceptionsMapping.translateException(var14, this.getExceptionInterceptor());
      }
   }

   protected SQLException handleExceptionForBatch(int endOfBatchIndex, int numValuesPerBatch, long[] updateCounts, SQLException ex) throws BatchUpdateException, SQLException {
      for (int j = endOfBatchIndex; j > endOfBatchIndex - numValuesPerBatch; j--) {
         updateCounts[j] = -3L;
      }

      if (this.continueBatchOnError
         && !(ex instanceof MySQLTimeoutException)
         && !(ex instanceof MySQLStatementCancelledException)
         && !this.hasDeadlockOrTimeoutRolledBackTx(ex)) {
         return ex;
      } else {
         long[] newUpdateCounts = new long[endOfBatchIndex];
         System.arraycopy(updateCounts, 0, newUpdateCounts, 0, endOfBatchIndex);
         throw SQLError.createBatchUpdateException(ex, newUpdateCounts, this.getExceptionInterceptor());
      }
   }

   @Override
   public ResultSet executeQuery(String sql) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            TelemetrySpan span = this.getSession().getTelemetryHandler().startSpan(TelemetrySpanName.STMT_EXECUTE);

            try (TelemetryScope scope = span.makeCurrent()) {
               String dbOperation = QueryInfo.getStatementKeyword(sql, this.session.getServerSession().isNoBackslashEscapesSet());
               span.setAttribute(TelemetryAttribute.DB_NAME, this.getCurrentDatabase());
               span.setAttribute(TelemetryAttribute.DB_OPERATION, dbOperation);
               span.setAttribute(TelemetryAttribute.DB_STATEMENT, dbOperation + " (...)");
               span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
               span.setAttribute(TelemetryAttribute.DB_USER, this.connection.getUser());
               span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
               span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
               JdbcConnection locallyScopedConn = this.connection;
               this.retrieveGeneratedKeys = false;
               this.checkNullOrEmptyQuery(sql);
               this.resetCancelledState();
               this.implicitlyCloseAllOpenResults();
               this.clearWarnings();
               if (sql.charAt(0) == '/' && sql.startsWith("/* ping */")) {
                  this.doPingInstead();
                  return this.results;
               }

               this.setupStreamingTimeout(locallyScopedConn);
               if (this.doEscapeProcessing) {
                  Object escapedSqlResult = EscapeProcessor.escapeSQL(
                     sql,
                     this.session.getServerSession().getSessionTimeZone(),
                     this.session.getServerSession().getCapabilities().serverSupportsFracSecs(),
                     this.session.getServerSession().isServerTruncatesFracSecs(),
                     this.getExceptionInterceptor()
                  );
                  sql = escapedSqlResult instanceof String ? (String)escapedSqlResult : ((EscapeProcessorResult)escapedSqlResult).escapedSql;
               }

               if (!this.isResultSetProducingQuery(sql)) {
                  throw SQLError.createSQLException(Messages.getString("Statement.57"), "S1009", this.getExceptionInterceptor());
               }

               CachedResultSetMetaData cachedMetaData = null;
               if (this.useServerFetch()) {
                  this.results = this.createResultSetUsingServerFetch(sql);
                  return this.results;
               }

               CancelQueryTask timeoutTask = null;
               String oldDb = null;

               try {
                  timeoutTask = this.startQueryTimer(this, this.getTimeoutInMillis());
                  if (!locallyScopedConn.getDatabase().equals(this.getCurrentDatabase())) {
                     oldDb = locallyScopedConn.getDatabase();
                     locallyScopedConn.setDatabase(this.getCurrentDatabase());
                  }

                  if (locallyScopedConn.getPropertySet().getBooleanProperty(PropertyKey.cacheResultSetMetadata).getValue()) {
                     cachedMetaData = locallyScopedConn.getCachedMetaData(sql);
                  }

                  locallyScopedConn.setSessionMaxRows(this.maxRows);
                  this.statementBegins();
                  this.results = ((NativeSession)locallyScopedConn.getSession())
                     .execSQL(this, sql, this.maxRows, null, this.meetsConditionsForStreamingResultSet(), this.getResultSetFactory(), cachedMetaData, false);
                  if (timeoutTask != null) {
                     this.stopQueryTimer(timeoutTask, true, true);
                     timeoutTask = null;
                  }
               } catch (OperationCancelledException | CJTimeoutException var64) {
                  throw SQLExceptionsMapping.translateException(var64, this.exceptionInterceptor);
               } finally {
                  this.query.getStatementExecuting().set(false);
                  this.stopQueryTimer(timeoutTask, false, false);
                  if (oldDb != null) {
                     locallyScopedConn.setDatabase(oldDb);
                  }
               }

               this.lastInsertId = this.results.getUpdateID();
               if (cachedMetaData != null) {
                  locallyScopedConn.initializeResultsMetadataFromCache(sql, cachedMetaData, this.results);
               } else if (this.connection.getPropertySet().getBooleanProperty(PropertyKey.cacheResultSetMetadata).getValue()) {
                  locallyScopedConn.initializeResultsMetadataFromCache(sql, null, this.results);
               }
            } catch (Throwable var68) {
               span.setError(var68);
               throw var68;
            } finally {
               span.end();
            }

            return this.results;
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var71) {
         throw SQLExceptionsMapping.translateException(var71, this.getExceptionInterceptor());
      }
   }

   protected void doPingInstead() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            if (this.pingTarget != null) {
               try {
                  this.pingTarget.doPing();
               } catch (SQLException var9) {
                  throw var9;
               } catch (Exception var10) {
                  throw SQLError.createSQLException(var10.getMessage(), "08S01", var10, this.getExceptionInterceptor());
               }
            } else {
               this.connection.ping();
            }

            ResultSetInternalMethods fakeSelectOneResultSet = this.generatePingResultSet();
            this.results = fakeSelectOneResultSet;
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var12) {
         throw SQLExceptionsMapping.translateException(var12, this.getExceptionInterceptor());
      }
   }

   protected ResultSetInternalMethods generatePingResultSet() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         ResultSetImpl var7;
         try {
            String encoding = this.session.getServerSession().getCharsetSettings().getMetadataEncoding();
            int collationIndex = this.session.getServerSession().getCharsetSettings().getMetadataCollationIndex();
            Field[] fields = new Field[]{new Field(null, "1", collationIndex, encoding, MysqlType.BIGINT, 1)};
            ArrayList<Row> rows = new ArrayList<>();
            byte[] colVal = new byte[]{49};
            rows.add(new ByteArrayRow(new byte[][]{colVal}, this.getExceptionInterceptor()));
            var7 = this.resultSetFactory.createFromResultsetRows(1007, 1004, new ResultsetRowsStatic(rows, new DefaultColumnDefinition(fields)));
         } finally {
            connectionLock.unlock();
         }

         return var7;
      } catch (CJException var13) {
         throw SQLExceptionsMapping.translateException(var13, this.getExceptionInterceptor());
      }
   }

   public void executeSimpleNonQuery(JdbcConnection c, String nonQuery) throws SQLException {
      try {
         Lock connectionLock = c.getConnectionLock();
         connectionLock.lock();

         try {
            ((NativeSession)c.getSession()).execSQL(this, nonQuery, -1, null, false, this.getResultSetFactory(), null, false).close();
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public int executeUpdate(String sql) throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql));
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, autoGeneratedKeys));
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, columnIndexes));
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public int executeUpdate(String sql, String[] columnNames) throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, columnNames));
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   protected long executeUpdateInternal(String sql, boolean isBatch, boolean returnGeneratedKeys) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         long var69;
         try {
            TelemetrySpan span = this.getSession().getTelemetryHandler().startSpan(TelemetrySpanName.STMT_EXECUTE);

            try (TelemetryScope scope = span.makeCurrent()) {
               String dbOperation = QueryInfo.getStatementKeyword(sql, this.session.getServerSession().isNoBackslashEscapesSet());
               span.setAttribute(TelemetryAttribute.DB_NAME, this.getCurrentDatabase());
               span.setAttribute(TelemetryAttribute.DB_OPERATION, dbOperation);
               span.setAttribute(TelemetryAttribute.DB_STATEMENT, dbOperation + " (...)");
               span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
               span.setAttribute(TelemetryAttribute.DB_USER, this.connection.getUser());
               span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
               span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
               JdbcConnection locallyScopedConn = this.connection;
               this.checkNullOrEmptyQuery(sql);
               this.resetCancelledState();
               this.clearWarnings();
               char firstStatementChar = QueryInfo.firstCharOfStatementUc(sql, this.session.getServerSession().isNoBackslashEscapesSet());
               if (!this.isNonResultSetProducingQuery(sql)) {
                  throw SQLError.createSQLException(Messages.getString("Statement.46"), "01S03", this.getExceptionInterceptor());
               }

               this.retrieveGeneratedKeys = returnGeneratedKeys;
               this.lastQueryIsOnDupKeyUpdate = returnGeneratedKeys && firstStatementChar == 'I' && this.containsOnDuplicateKeyInString(sql);
               ResultSetInternalMethods rs = null;
               if (!isBatch) {
                  this.batchedGeneratedKeys = null;
               }

               if (this.doEscapeProcessing) {
                  Object escapedSqlResult = EscapeProcessor.escapeSQL(
                     sql,
                     this.session.getServerSession().getSessionTimeZone(),
                     this.session.getServerSession().getCapabilities().serverSupportsFracSecs(),
                     this.session.getServerSession().isServerTruncatesFracSecs(),
                     this.getExceptionInterceptor()
                  );
                  sql = escapedSqlResult instanceof String ? (String)escapedSqlResult : ((EscapeProcessorResult)escapedSqlResult).escapedSql;
               }

               if (locallyScopedConn.isReadOnly(false)) {
                  throw SQLError.createSQLException(
                     Messages.getString("Statement.42") + Messages.getString("Statement.43"), "S1009", this.getExceptionInterceptor()
                  );
               }

               this.implicitlyCloseAllOpenResults();
               CancelQueryTask timeoutTask = null;
               String oldDb = null;

               try {
                  timeoutTask = this.startQueryTimer(this, this.getTimeoutInMillis());
                  if (!locallyScopedConn.getDatabase().equals(this.getCurrentDatabase())) {
                     oldDb = locallyScopedConn.getDatabase();
                     locallyScopedConn.setDatabase(this.getCurrentDatabase());
                  }

                  locallyScopedConn.setSessionMaxRows(-1);
                  this.statementBegins();
                  rs = ((NativeSession)locallyScopedConn.getSession()).execSQL(this, sql, -1, null, false, this.getResultSetFactory(), null, isBatch);
                  if (timeoutTask != null) {
                     this.stopQueryTimer(timeoutTask, true, true);
                     timeoutTask = null;
                  }
               } catch (OperationCancelledException | CJTimeoutException var61) {
                  throw SQLExceptionsMapping.translateException(var61, this.exceptionInterceptor);
               } finally {
                  this.stopQueryTimer(timeoutTask, false, false);
                  if (oldDb != null) {
                     locallyScopedConn.setDatabase(oldDb);
                  }

                  if (!isBatch) {
                     this.query.getStatementExecuting().set(false);
                  }
               }

               this.results = rs;
               rs.setFirstCharOfQuery(firstStatementChar);
               this.updateCount = rs.getUpdateCount();
               this.lastInsertId = rs.getUpdateID();
            } catch (Throwable var65) {
               span.setError(var65);
               throw var65;
            } finally {
               span.end();
            }

            var69 = this.updateCount;
         } finally {
            connectionLock.unlock();
         }

         return var69;
      } catch (CJException var68) {
         throw SQLExceptionsMapping.translateException(var68, this.getExceptionInterceptor());
      }
   }

   @Override
   public Connection getConnection() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         JdbcConnection var2;
         try {
            var2 = this.connection;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getFetchDirection() throws SQLException {
      try {
         return 1000;
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getFetchSize() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         int var2;
         try {
            var2 = this.query.getResultFetchSize();
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public ResultSet getGeneratedKeys() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         ResultSetInternalMethods encoding;
         try {
            if (!this.retrieveGeneratedKeys) {
               throw SQLError.createSQLException(Messages.getString("Statement.GeneratedKeysNotRequested"), "S1009", this.getExceptionInterceptor());
            }

            if (this.batchedGeneratedKeys != null) {
               String encodingx = this.session.getServerSession().getCharsetSettings().getMetadataEncoding();
               int collationIndex = this.session.getServerSession().getCharsetSettings().getMetadataCollationIndex();
               Field[] fields = new Field[]{new Field("", "GENERATED_KEY", collationIndex, encodingx, MysqlType.BIGINT_UNSIGNED, 20)};
               this.generatedKeysResults = this.resultSetFactory
                  .createFromResultsetRows(1007, 1004, new ResultsetRowsStatic(this.batchedGeneratedKeys, new DefaultColumnDefinition(fields)));
               return this.generatedKeysResults;
            }

            if (!this.lastQueryIsOnDupKeyUpdate) {
               return this.generatedKeysResults = this.getGeneratedKeysInternal();
            }

            encoding = this.generatedKeysResults = this.getGeneratedKeysInternal(1L);
         } finally {
            connectionLock.unlock();
         }

         return encoding;
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   protected ResultSetInternalMethods getGeneratedKeysInternal() throws SQLException {
      long numKeys = this.getLargeUpdateCount();
      return this.getGeneratedKeysInternal(numKeys);
   }

   protected ResultSetInternalMethods getGeneratedKeysInternal(long numKeys) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         ResultSetImpl var22;
         try {
            String encoding = this.session.getServerSession().getCharsetSettings().getMetadataEncoding();
            int collationIndex = this.session.getServerSession().getCharsetSettings().getMetadataCollationIndex();
            Field[] fields = new Field[]{new Field("", "GENERATED_KEY", collationIndex, encoding, MysqlType.BIGINT_UNSIGNED, 20)};
            ArrayList<Row> rowSet = new ArrayList<>();
            long beginAt = this.getLastInsertID();
            if (this.results != null) {
               String serverInfo = this.results.getServerInfo();
               if (numKeys > 0L && this.results.getFirstCharOfQuery() == 'R' && serverInfo != null && serverInfo.length() > 0) {
                  numKeys = this.getRecordCountFromInfo(serverInfo);
               }

               if (beginAt != 0L && numKeys > 0L) {
                  for (int i = 0; i < numKeys; i++) {
                     byte[][] row = new byte[1][];
                     if (beginAt > 0L) {
                        row[0] = StringUtils.getBytes(Long.toString(beginAt));
                     } else {
                        byte[] asBytes = new byte[]{
                           (byte)(beginAt >>> 56),
                           (byte)(beginAt >>> 48),
                           (byte)(beginAt >>> 40),
                           (byte)(beginAt >>> 32),
                           (byte)(beginAt >>> 24),
                           (byte)(beginAt >>> 16),
                           (byte)(beginAt >>> 8),
                           (byte)(beginAt & 255L)
                        };
                        BigInteger val = new BigInteger(1, asBytes);
                        row[0] = val.toString().getBytes();
                     }

                     rowSet.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
                     beginAt += this.connection.getAutoIncrementIncrement();
                  }
               }
            }

            ResultSetImpl gkRs = this.resultSetFactory
               .createFromResultsetRows(1007, 1004, new ResultsetRowsStatic(rowSet, new DefaultColumnDefinition(fields)));
            var22 = gkRs;
         } finally {
            connectionLock.unlock();
         }

         return var22;
      } catch (CJException var20) {
         throw SQLExceptionsMapping.translateException(var20, this.getExceptionInterceptor());
      }
   }

   public long getLastInsertID() {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         long var2;
         try {
            var2 = this.lastInsertId;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public long getLongUpdateCount() {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         long var2;
         try {
            if (this.results != null && !this.results.hasRows()) {
               return this.updateCount;
            }

            var2 = -1L;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getMaxFieldSize() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         int var2;
         try {
            var2 = this.maxFieldSize;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getMaxRows() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         byte var2;
         try {
            if (this.maxRows > 0) {
               return this.maxRows;
            }

            var2 = 0;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean getMoreResults() throws SQLException {
      try {
         return this.getMoreResults(1);
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean getMoreResults(int current) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         boolean streamingMode;
         try {
            if (this.results != null) {
               streamingMode = this.meetsConditionsForStreamingResultSet();
               if (streamingMode && this.results.hasRows()) {
                  while (this.results.next()) {
                  }
               }

               ResultSetInternalMethods nextResultSet = (ResultSetInternalMethods)this.results.getNextResultset();
               switch (current) {
                  case 1:
                     if (this.results != null) {
                        if (!streamingMode && !this.dontTrackOpenResources.getValue()) {
                           this.results.doClose(CloseOption.IMPLICIT);
                        }

                        if (this.results != null) {
                           this.results.clearNextResultset();
                        }
                     }
                     break;
                  case 2:
                     if (!this.dontTrackOpenResources.getValue()) {
                        this.openResultSets.add(this.results);
                     }

                     this.results.clearNextResultset();
                     break;
                  case 3:
                     if (this.results != null) {
                        if (!streamingMode && !this.dontTrackOpenResources.getValue()) {
                           this.results.doClose(CloseOption.IMPLICIT);
                        }

                        if (this.results != null) {
                           this.results.clearNextResultset();
                        }
                     }

                     this.closeAllOpenResults();
                     break;
                  default:
                     throw SQLError.createSQLException(Messages.getString("Statement.19"), "S1009", this.getExceptionInterceptor());
               }

               this.results = nextResultSet;
               if (this.results == null) {
                  this.updateCount = -1L;
                  this.lastInsertId = -1L;
               } else if (this.results.hasRows()) {
                  this.updateCount = -1L;
                  this.lastInsertId = -1L;
               } else {
                  this.updateCount = this.results.getUpdateCount();
                  this.lastInsertId = this.results.getUpdateID();
               }

               boolean moreResults = this.results != null && this.results.hasRows();
               if (!moreResults) {
                  this.checkAndPerformCloseOnCompletionAction();
               }

               return moreResults;
            }

            streamingMode = false;
         } finally {
            connectionLock.unlock();
         }

         return streamingMode;
      } catch (CJException var12) {
         throw SQLExceptionsMapping.translateException(var12, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getQueryTimeout() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         int var2;
         try {
            var2 = Math.toIntExact(this.getTimeoutInMillis() / 1000L);
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   private long getRecordCountFromInfo(String serverInfo) {
      StringBuilder recordsBuf = new StringBuilder();
      long recordsCount = 0L;
      long duplicatesCount = 0L;
      char c = '\u0000';
      int length = serverInfo.length();

      int i;
      for (i = 0; i < length; i++) {
         c = serverInfo.charAt(i);
         if (Character.isDigit(c)) {
            break;
         }
      }

      recordsBuf.append(c);
      i++;

      while (i < length) {
         c = serverInfo.charAt(i);
         if (!Character.isDigit(c)) {
            break;
         }

         recordsBuf.append(c);
         i++;
      }

      recordsCount = Long.parseLong(recordsBuf.toString());

      StringBuilder duplicatesBuf;
      for (duplicatesBuf = new StringBuilder(); i < length; i++) {
         c = serverInfo.charAt(i);
         if (Character.isDigit(c)) {
            break;
         }
      }

      duplicatesBuf.append(c);
      i++;

      while (i < length) {
         c = serverInfo.charAt(i);
         if (!Character.isDigit(c)) {
            break;
         }

         duplicatesBuf.append(c);
         i++;
      }

      duplicatesCount = Long.parseLong(duplicatesBuf.toString());
      return recordsCount - duplicatesCount;
   }

   @Override
   public ResultSet getResultSet() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         ResultSetInternalMethods var2;
         try {
            var2 = this.results != null && this.results.hasRows() ? this.results : null;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getResultSetConcurrency() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         int var2;
         try {
            var2 = this.resultSetConcurrency;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getResultSetHoldability() throws SQLException {
      try {
         return 1;
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public ResultSetInternalMethods getResultSetInternal() {
      try {
         try {
            Lock connectionLock = this.checkClosed().getConnectionLock();
            connectionLock.lock();

            ResultSetInternalMethods var2;
            try {
               var2 = this.results;
            } finally {
               connectionLock.unlock();
            }

            return var2;
         } catch (StatementIsClosedException var9) {
            return this.results;
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getResultSetType() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         int var2;
         try {
            var2 = this.query.getResultType().getIntValue();
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getUpdateCount() throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.getLargeUpdateCount());
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public SQLWarning getWarnings() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         SQLWarning pendingWarningsFromServer;
         try {
            if (!this.isClearWarningsCalled()) {
               pendingWarningsFromServer = this.session.getProtocol().convertShowWarningsToSQLWarnings(false);
               if (this.warningChain != null) {
                  this.warningChain.setNextWarning(pendingWarningsFromServer);
               } else {
                  this.warningChain = pendingWarningsFromServer;
               }

               return this.warningChain;
            }

            pendingWarningsFromServer = null;
         } finally {
            connectionLock.unlock();
         }

         return pendingWarningsFromServer;
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void doClose(CloseOption... options) throws SQLException {
      try {
         JdbcConnection locallyScopedConn = this.connection;
         if (locallyScopedConn != null && !this.isClosed) {
            if (!this.dontTrackOpenResources.getValue()) {
               locallyScopedConn.unregisterStatement(this);
            }

            if (this.useUsageAdvisor && CloseOption.IMPLICIT.in(options)) {
               this.session.getProfilerEventHandler().processEvent((byte)0, this.session, this, null, 0L, new Throwable(), Messages.getString("Statement.63"));
            }

            if (CloseOption.PROPAGATE.in(options) && !this.holdResultsOpenOverClose && !this.dontTrackOpenResources.getValue()) {
               if (this.results != null) {
                  try {
                     this.results.close();
                  } catch (Exception var6) {
                  }
               }

               if (this.generatedKeysResults != null) {
                  try {
                     this.generatedKeysResults.close();
                  } catch (Exception var5) {
                  }
               }

               this.closeAllOpenResults();
            }

            this.clearAttributes();
            this.isClosed = true;
            this.closeQuery();
            this.results = null;
            this.generatedKeysResults = null;
            this.connection = null;
            this.session = null;
            this.warningChain = null;
            this.openResultSets = null;
            this.batchedGeneratedKeys = null;
            this.pingTarget = null;
            this.resultSetFactory = null;
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setCursorName(String name) throws SQLException {
      try {
         ;
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setEscapeProcessing(boolean enable) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            this.doEscapeProcessing = enable;
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setFetchDirection(int direction) throws SQLException {
      try {
         switch (direction) {
            case 1000:
            case 1001:
            case 1002:
               return;
            default:
               throw SQLError.createSQLException(Messages.getString("Statement.5"), "S1009", this.getExceptionInterceptor());
         }
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setFetchSize(int rows) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            if (rows < 0 && rows != Integer.MIN_VALUE || this.maxRows > 0 && rows > this.getMaxRows()) {
               throw SQLError.createSQLException(Messages.getString("Statement.7"), "S1009", this.getExceptionInterceptor());
            }

            this.query.setResultFetchSize(rows);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setHoldResultsOpenOverClose(boolean holdResultsOpenOverClose) {
      try {
         try {
            Lock connectionLock = this.checkClosed().getConnectionLock();
            connectionLock.lock();

            try {
               this.holdResultsOpenOverClose = holdResultsOpenOverClose;
            } finally {
               connectionLock.unlock();
            }
         } catch (StatementIsClosedException var9) {
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setMaxFieldSize(int max) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            if (max < 0) {
               throw SQLError.createSQLException(Messages.getString("Statement.11"), "S1009", this.getExceptionInterceptor());
            }

            int maxBuf = this.maxAllowedPacket.getValue();
            if (max > maxBuf) {
               throw SQLError.createSQLException(Messages.getString("Statement.13", new Object[]{(long)maxBuf}), "S1009", this.getExceptionInterceptor());
            }

            this.maxFieldSize = max;
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setMaxRows(int max) throws SQLException {
      try {
         this.setLargeMaxRows(max);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setQueryTimeout(int seconds) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            if (seconds < 0) {
               throw SQLError.createSQLException(Messages.getString("Statement.21"), "S1009", this.getExceptionInterceptor());
            }

            this.setTimeoutInMillis(seconds * 1000);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   void setResultSetConcurrency(int concurrencyFlag) throws SQLException {
      try {
         try {
            Lock connectionLock = this.checkClosed().getConnectionLock();
            connectionLock.lock();

            try {
               this.resultSetConcurrency = concurrencyFlag;
               this.resultSetFactory = new ResultSetFactory(this.connection, this);
            } finally {
               connectionLock.unlock();
            }
         } catch (StatementIsClosedException var9) {
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   void setResultSetType(Resultset.Type typeFlag) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            this.query.setResultType(typeFlag);
            this.resultSetFactory = new ResultSetFactory(this.connection, this);
         } finally {
            connectionLock.unlock();
         }
      } catch (StatementIsClosedException var7) {
      }
   }

   void setResultSetType(int typeFlag) throws SQLException {
      this.query.setResultType(Resultset.Type.fromValue(typeFlag, Resultset.Type.FORWARD_ONLY));
   }

   protected void getBatchedGeneratedKeys(Statement batchedStatement) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            if (this.retrieveGeneratedKeys) {
               ResultSet rs = null;

               try {
                  rs = batchedStatement.getGeneratedKeys();

                  while (rs.next()) {
                     this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][]{rs.getBytes(1)}, this.getExceptionInterceptor()));
                  }
               } finally {
                  if (rs != null) {
                     rs.close();
                  }
               }
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var15) {
         throw SQLExceptionsMapping.translateException(var15, this.getExceptionInterceptor());
      }
   }

   protected void getBatchedGeneratedKeys(int maxKeys) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            if (this.retrieveGeneratedKeys) {
               ResultSetInternalMethods rs = null;

               try {
                  rs = maxKeys == 0 ? this.getGeneratedKeysInternal() : this.getGeneratedKeysInternal(maxKeys);

                  while (rs.next()) {
                     this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][]{rs.getBytes(1)}, this.getExceptionInterceptor()));
                  }
               } finally {
                  if (rs != null) {
                     rs.doClose();
                  }
               }
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var15) {
         throw SQLExceptionsMapping.translateException(var15, this.getExceptionInterceptor());
      }
   }

   private boolean useServerFetch() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         boolean var2;
         try {
            var2 = this.session.getPropertySet().getBooleanProperty(PropertyKey.useCursorFetch).getValue()
               && this.query.getResultFetchSize() > 0
               && this.query.getResultType() == Resultset.Type.FORWARD_ONLY;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isClosed() throws SQLException {
      try {
         JdbcConnection locallyScopedConn = this.connection;
         if (locallyScopedConn == null) {
            return true;
         } else {
            Lock connectionLock = locallyScopedConn.getConnectionLock();
            connectionLock.lock();

            boolean var3;
            try {
               var3 = this.isClosed;
            } finally {
               connectionLock.unlock();
            }

            return var3;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isPoolable() throws SQLException {
      try {
         this.checkClosed();
         return this.isPoolable;
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setPoolable(boolean poolable) throws SQLException {
      try {
         this.checkClosed();
         this.isPoolable = poolable;
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
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

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      try {
         try {
            return iface.cast(this);
         } catch (ClassCastException var4) {
            throw SQLError.createSQLException(
               Messages.getString("Common.UnableToUnwrap", new Object[]{iface.toString()}), "S1009", this.getExceptionInterceptor()
            );
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public InputStream getLocalInfileInputStream() {
      return this.session.getLocalInfileInputStream();
   }

   @Override
   public void setLocalInfileInputStream(InputStream stream) {
      this.session.setLocalInfileInputStream(stream);
   }

   @Override
   public void setPingTarget(PingTarget pingTarget) {
      this.pingTarget = pingTarget;
   }

   @Override
   public ExceptionInterceptor getExceptionInterceptor() {
      return this.exceptionInterceptor;
   }

   protected boolean containsOnDuplicateKeyInString(String sql) {
      return (!this.dontCheckOnDuplicateKeyUpdateInSQL || this.rewriteBatchedStatements.getValue())
         && QueryInfo.containsOnDuplicateKeyUpdateClause(sql, this.session.getServerSession().isNoBackslashEscapesSet());
   }

   @Override
   public void closeOnCompletion() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            this.closeOnCompletion = true;
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isCloseOnCompletion() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         boolean var2;
         try {
            var2 = this.closeOnCompletion;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public long[] executeLargeBatch() throws SQLException {
      try {
         return this.executeBatchInternal();
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public long executeLargeUpdate(String sql) throws SQLException {
      try {
         return this.executeUpdateInternal(sql, false, false);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      try {
         return this.executeUpdateInternal(sql, false, autoGeneratedKeys == 1);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
      try {
         return this.executeUpdateInternal(sql, false, columnIndexes != null && columnIndexes.length > 0);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
      try {
         return this.executeUpdateInternal(sql, false, columnNames != null && columnNames.length > 0);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public long getLargeMaxRows() throws SQLException {
      try {
         return this.getMaxRows();
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public long getLargeUpdateCount() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         long var2;
         try {
            if (this.results != null && !this.results.hasRows()) {
               return this.results.getUpdateCount();
            }

            var2 = -1L;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setLargeMaxRows(long max) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            if (max > 50000000L || max < 0L) {
               throw SQLError.createSQLException(Messages.getString("Statement.15") + max + " > " + 50000000 + ".", "S1009", this.getExceptionInterceptor());
            }

            if (max == 0L) {
               max = -1L;
            }

            this.maxRows = (int)max;
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public String getCurrentDatabase() {
      return this.query.getCurrentDatabase();
   }

   public long getServerStatementId() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, Messages.getString("Statement.65"));
   }

   @Override
   public <T extends Resultset, M extends Message> ProtocolEntityFactory<T, M> getResultSetFactory() {
      return this.resultSetFactory;
   }

   @Override
   public int getId() {
      return this.query.getId();
   }

   @Override
   public void setCancelStatus(Query.CancelStatus cs) {
      this.query.setCancelStatus(cs);
   }

   @Override
   public void checkCancelTimeout() {
      try {
         this.query.checkCancelTimeout();
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public Session getSession() {
      return this.session;
   }

   @Override
   public Lock getCancelTimeoutLock() {
      return this.query.getCancelTimeoutLock();
   }

   @Override
   public void closeQuery() {
      if (this.query != null) {
         this.query.closeQuery();
      }
   }

   @Override
   public int getResultFetchSize() {
      return this.query.getResultFetchSize();
   }

   @Override
   public void setResultFetchSize(int fetchSize) {
      this.query.setResultFetchSize(fetchSize);
   }

   @Override
   public Resultset.Type getResultType() {
      return this.query.getResultType();
   }

   @Override
   public void setResultType(Resultset.Type resultSetType) {
      this.query.setResultType(resultSetType);
   }

   @Override
   public long getTimeoutInMillis() {
      return this.query.getTimeoutInMillis();
   }

   @Override
   public void setTimeoutInMillis(long timeoutInMillis) {
      this.query.setTimeoutInMillis(timeoutInMillis);
   }

   @Override
   public long getExecuteTime() {
      return this.query.getExecuteTime();
   }

   @Override
   public void setExecuteTime(long executeTime) {
      this.query.setExecuteTime(executeTime);
   }

   @Override
   public AtomicBoolean getStatementExecuting() {
      return this.query.getStatementExecuting();
   }

   @Override
   public void setCurrentDatabase(String currentDb) {
      this.query.setCurrentDatabase(currentDb);
   }

   @Override
   public boolean isClearWarningsCalled() {
      return this.query.isClearWarningsCalled();
   }

   @Override
   public void setClearWarningsCalled(boolean clearWarningsCalled) {
      this.query.setClearWarningsCalled(clearWarningsCalled);
   }

   @Override
   public Query getQuery() {
      return this.query;
   }

   @Override
   public QueryAttributesBindings getQueryAttributesBindings() {
      return this.query.getQueryAttributesBindings();
   }

   @Override
   public void setAttribute(String name, Object value) {
      this.getQueryAttributesBindings().setAttribute(name, value);
   }

   @Override
   public void clearAttributes() {
      QueryAttributesBindings qab = this.getQueryAttributesBindings();
      if (qab != null) {
         qab.clearAttributes();
      }
   }
}
