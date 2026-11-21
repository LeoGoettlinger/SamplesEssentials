package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Array;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.BindValue;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.CancelQueryTask;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.ClientPreparedQuery;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.MysqlType;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.NativeQueryBindValue;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.NativeQueryBindings;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.NativeSession;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.PreparedQuery;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Query;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.QueryBindings;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.QueryInfo;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.QueryReturnType;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.FeatureNotAvailableException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.StatementIsClosedException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.MySQLStatementCancelledException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.MySQLTimeoutException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.SQLError;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.CachedResultSetMetaData;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.ResultSetInternalMethods;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ColumnDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Message;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.NativePacketPayload;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.Field;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryAttribute;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryScope;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetrySpan;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetrySpanName;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.Util;

public class ClientPreparedStatement extends StatementImpl implements JdbcPreparedStatement {
   protected boolean batchHasPlainStatements = false;
   protected MysqlParameterMetadata parameterMetaData;
   private ResultSetMetaData pstmtResultMetaData;
   protected String batchedValuesClause;
   private boolean doPingInstead;
   private boolean compensateForOnDuplicateKeyUpdate = false;
   protected int rewrittenBatchSize = 0;

   protected static ClientPreparedStatement getInstance(JdbcConnection conn, String sql, String db) throws SQLException {
      return new ClientPreparedStatement(conn, sql, db);
   }

   protected static ClientPreparedStatement getInstance(JdbcConnection conn, String sql, String db, QueryInfo cachedQueryInfo) throws SQLException {
      return new ClientPreparedStatement(conn, sql, db, cachedQueryInfo);
   }

   @Override
   protected void initQuery() {
      this.query = new ClientPreparedQuery(this.session);
   }

   protected ClientPreparedStatement(JdbcConnection conn, String db) throws SQLException {
      super(conn, db);
      this.setPoolable(true);
      this.compensateForOnDuplicateKeyUpdate = this.session.getPropertySet().getBooleanProperty(PropertyKey.compensateOnDuplicateKeyUpdateCounts).getValue();
   }

   public ClientPreparedStatement(JdbcConnection conn, String sql, String db) throws SQLException {
      this(conn, sql, db, null);
   }

   public ClientPreparedStatement(JdbcConnection conn, String sql, String db, QueryInfo cachedQueryInfo) throws SQLException {
      this(conn, db);

      try {
         ((PreparedQuery)this.query).checkNullOrEmptyQuery(sql);
         ((PreparedQuery)this.query).setOriginalSql(sql);
         ((PreparedQuery)this.query).setQueryInfo(cachedQueryInfo != null ? cachedQueryInfo : new QueryInfo(sql, this.session, this.charEncoding));
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.exceptionInterceptor);
      }

      this.doPingInstead = sql.startsWith("/* ping */");
      this.initializeFromQueryInfo();
   }

   @Override
   public QueryBindings getQueryBindings() {
      return ((PreparedQuery)this.query).getQueryBindings();
   }

   @Override
   public String toString() {
      try {
         StringBuilder buf = new StringBuilder();
         buf.append(this.getClass().getName());
         buf.append(": ");
         buf.append(((PreparedQuery)this.query).asSql());
         return buf.toString();
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void addBatch() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            QueryBindings queryBindings = ((PreparedQuery)this.query).getQueryBindings();
            queryBindings.checkAllParametersSet();
            this.query.addBatch(queryBindings.clone());
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public void addBatch(String sql) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            this.batchHasPlainStatements = true;
            super.addBatch(sql);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public void clearBatch() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            this.batchHasPlainStatements = false;
            super.clearBatch();
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   public void clearParameters() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            for (BindValue bv : ((PreparedQuery)this.query).getQueryBindings().getBindValues()) {
               bv.reset();
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   protected boolean checkReadOnlySafeStatement() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         boolean var2;
         try {
            var2 = QueryInfo.isReadOnlySafeQuery(((PreparedQuery)this.query).getOriginalSql(), this.session.getServerSession().isNoBackslashEscapesSet())
               || !this.connection.isReadOnly();
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean execute() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         boolean sendPacket;
         try {
            TelemetrySpan span = this.getSession().getTelemetryHandler().startSpan(TelemetrySpanName.STMT_EXECUTE_PREPARED);

            try (TelemetryScope scope = span.makeCurrent()) {
               String dbOperation = this.getQueryInfo().getStatementKeyword();
               span.setAttribute(TelemetryAttribute.DB_NAME, this.getCurrentDatabase());
               span.setAttribute(TelemetryAttribute.DB_OPERATION, dbOperation);
               span.setAttribute(TelemetryAttribute.DB_STATEMENT, dbOperation + " (...)");
               span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
               span.setAttribute(TelemetryAttribute.DB_USER, this.connection.getUser());
               span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
               span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
               JdbcConnection locallyScopedConn = this.connection;
               if (!this.doPingInstead && !this.checkReadOnlySafeStatement()) {
                  throw SQLError.createSQLException(
                     Messages.getString("PreparedStatement.20") + Messages.getString("PreparedStatement.21"), "S1009", this.exceptionInterceptor
                  );
               }

               ResultSetInternalMethods rs = null;
               this.lastQueryIsOnDupKeyUpdate = false;
               if (this.retrieveGeneratedKeys) {
                  this.lastQueryIsOnDupKeyUpdate = this.containsOnDuplicateKeyUpdate();
               }

               this.batchedGeneratedKeys = null;
               this.resetCancelledState();
               this.implicitlyCloseAllOpenResults();
               this.clearWarnings();
               if (!this.doPingInstead) {
                  this.setupStreamingTimeout(locallyScopedConn);
                  Message sendPacketx = ((PreparedQuery)this.query).fillSendPacket(((PreparedQuery)this.query).getQueryBindings());
                  String oldDb = null;
                  if (!locallyScopedConn.getDatabase().equals(this.getCurrentDatabase())) {
                     oldDb = locallyScopedConn.getDatabase();
                     locallyScopedConn.setDatabase(this.getCurrentDatabase());
                  }

                  CachedResultSetMetaData cachedMetadata = null;
                  boolean cacheResultSetMetadata = locallyScopedConn.getPropertySet().getBooleanProperty(PropertyKey.cacheResultSetMetadata).getValue();
                  if (cacheResultSetMetadata) {
                     cachedMetadata = locallyScopedConn.getCachedMetaData(((PreparedQuery)this.query).getOriginalSql());
                  }

                  locallyScopedConn.setSessionMaxRows(this.getQueryInfo().getFirstStmtChar() == 'S' ? this.maxRows : -1);
                  rs = this.executeInternal(
                     this.maxRows,
                     sendPacketx,
                     this.meetsConditionsForStreamingResultSet(),
                     this.getQueryInfo().getFirstStmtChar() == 'S',
                     cachedMetadata,
                     false
                  );
                  if (cachedMetadata != null) {
                     locallyScopedConn.initializeResultsMetadataFromCache(((PreparedQuery)this.query).getOriginalSql(), cachedMetadata, rs);
                  } else if (rs.hasRows() && cacheResultSetMetadata) {
                     locallyScopedConn.initializeResultsMetadataFromCache(((PreparedQuery)this.query).getOriginalSql(), null, rs);
                  }

                  if (this.retrieveGeneratedKeys) {
                     rs.setFirstCharOfQuery(this.getQueryInfo().getFirstStmtChar());
                  }

                  if (oldDb != null) {
                     locallyScopedConn.setDatabase(oldDb);
                  }

                  if (rs != null) {
                     this.lastInsertId = rs.getUpdateID();
                     this.results = rs;
                  }

                  return rs != null && rs.hasRows();
               }

               this.doPingInstead();
               sendPacket = true;
            } catch (Throwable var49) {
               span.setError(var49);
               throw var49;
            } finally {
               span.end();
            }
         } finally {
            connectionLock.unlock();
         }

         return sendPacket;
      } catch (CJException var52) {
         throw SQLExceptionsMapping.translateException(var52, this.getExceptionInterceptor());
      }
   }

   @Override
   protected long[] executeBatchInternal() throws SQLException {
      Lock connectionLock = this.checkClosed().getConnectionLock();
      connectionLock.lock();

      long[] var7;
      try {
         TelemetrySpan span = this.getSession().getTelemetryHandler().startSpan(TelemetrySpanName.STMT_EXECUTE_BATCH_PREPARED);

         try (TelemetryScope scope = span.makeCurrent()) {
            span.setAttribute(TelemetryAttribute.DB_NAME, this.getCurrentDatabase());
            span.setAttribute(TelemetryAttribute.DB_OPERATION, "(SQL batch)");
            span.setAttribute(TelemetryAttribute.DB_STATEMENT, "(SQL batch)");
            span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
            span.setAttribute(TelemetryAttribute.DB_USER, this.connection.getUser());
            span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
            span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
            if (this.connection.isReadOnly()) {
               throw new SQLException(Messages.getString("PreparedStatement.25") + Messages.getString("PreparedStatement.26"), "S1009");
            }

            if (this.query.getBatchedArgs() == null || this.query.getBatchedArgs().size() == 0) {
               return new long[0];
            }

            long batchTimeout = this.getTimeoutInMillis();
            this.setTimeoutInMillis(0L);
            this.resetCancelledState();

            try {
               this.statementBegins();
               this.clearWarnings();
               if (!this.batchHasPlainStatements && this.rewriteBatchedStatements.getValue()) {
                  if (this.getQueryInfo().isRewritableWithMultiValuesClause()) {
                     return this.executeBatchWithMultiValuesClause(batchTimeout);
                  }

                  if (!this.batchHasPlainStatements && this.query.getBatchedArgs() != null && this.query.getBatchedArgs().size() > 3) {
                     return this.executePreparedBatchAsMultiStatement(batchTimeout);
                  }
               }

               var7 = this.executeBatchSerially(batchTimeout);
            } finally {
               this.query.getStatementExecuting().set(false);
               this.clearBatch();
            }
         } catch (Throwable var58) {
            span.setError(var58);
            throw var58;
         } finally {
            span.end();
         }
      } finally {
         connectionLock.unlock();
      }

      return var7;
   }

   protected long[] executePreparedBatchAsMultiStatement(long batchTimeout) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         long[] var70;
         try {
            if (this.batchedValuesClause == null) {
               this.batchedValuesClause = ((PreparedQuery)this.query).getOriginalSql() + ";";
            }

            JdbcConnection locallyScopedConn = this.connection;
            boolean multiQueriesEnabled = locallyScopedConn.getPropertySet().getBooleanProperty(PropertyKey.allowMultiQueries).getValue();
            CancelQueryTask timeoutTask = null;

            try {
               this.clearWarnings();
               int numBatchedArgs = this.query.getBatchedArgs().size();
               if (this.retrieveGeneratedKeys) {
                  this.batchedGeneratedKeys = new ArrayList<>(numBatchedArgs);
               }

               int numValuesPerBatch = ((PreparedQuery)this.query).computeBatchSize(numBatchedArgs);
               if (numBatchedArgs < numValuesPerBatch) {
                  numValuesPerBatch = numBatchedArgs;
               }

               PreparedStatement batchedStatement = null;
               int batchedParamIndex = 1;
               int numberToExecuteAsMultiValue = 0;
               int batchCounter = 0;
               int updateCountCounter = 0;
               long[] updateCounts = new long[numBatchedArgs * this.getQueryInfo().getNumberOfQueries()];
               SQLException sqlEx = null;

               try {
                  if (!multiQueriesEnabled) {
                     ((NativeSession)locallyScopedConn.getSession()).enableMultiQueries();
                  }

                  batchedStatement = this.retrieveGeneratedKeys
                     ? locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch), 1).unwrap(PreparedStatement.class)
                     : locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch)).unwrap(PreparedStatement.class);
                  timeoutTask = this.startQueryTimer((StatementImpl)batchedStatement, batchTimeout);
                  numberToExecuteAsMultiValue = numBatchedArgs < numValuesPerBatch ? numBatchedArgs : numBatchedArgs / numValuesPerBatch;
                  int numberArgsToExecute = numberToExecuteAsMultiValue * numValuesPerBatch;

                  for (int i = 0; i < numberArgsToExecute; i++) {
                     if (i != 0 && i % numValuesPerBatch == 0) {
                        try {
                           batchedStatement.execute();
                        } catch (SQLException var58) {
                           sqlEx = this.handleExceptionForBatch(batchCounter, numValuesPerBatch, updateCounts, var58);
                        }

                        updateCountCounter = this.processMultiCountsAndKeys((StatementImpl)batchedStatement, updateCountCounter, updateCounts);
                        batchedStatement.clearParameters();
                        batchedParamIndex = 1;
                     }

                     batchedParamIndex = this.setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.query.getBatchedArgs().get(batchCounter++));
                  }

                  try {
                     batchedStatement.execute();
                  } catch (SQLException var57) {
                     sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, var57);
                  }

                  updateCountCounter = this.processMultiCountsAndKeys((StatementImpl)batchedStatement, updateCountCounter, updateCounts);
                  batchedStatement.clearParameters();
                  numValuesPerBatch = numBatchedArgs - batchCounter;
                  if (timeoutTask != null) {
                     ((JdbcPreparedStatement)batchedStatement).checkCancelTimeout();
                  }
               } finally {
                  if (batchedStatement != null) {
                     batchedStatement.close();
                     PreparedStatement var65 = null;
                  }
               }

               try {
                  if (numValuesPerBatch > 0) {
                     batchedStatement = this.retrieveGeneratedKeys
                        ? locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch), 1)
                        : locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch));
                     if (timeoutTask != null) {
                        timeoutTask.setQueryToCancel((Query)batchedStatement);
                     }

                     batchedParamIndex = 1;

                     while (batchCounter < numBatchedArgs) {
                        batchedParamIndex = this.setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.query.getBatchedArgs().get(batchCounter++));
                     }

                     try {
                        batchedStatement.execute();
                     } catch (SQLException var56) {
                        sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, var56);
                     }

                     updateCountCounter = this.processMultiCountsAndKeys((StatementImpl)batchedStatement, updateCountCounter, updateCounts);
                     batchedStatement.clearParameters();
                  }

                  if (timeoutTask != null) {
                     this.stopQueryTimer(timeoutTask, true, true);
                     timeoutTask = null;
                  }

                  if (sqlEx != null) {
                     throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.exceptionInterceptor);
                  }

                  var70 = updateCounts;
               } finally {
                  if (batchedStatement != null) {
                     batchedStatement.close();
                  }
               }
            } finally {
               this.stopQueryTimer(timeoutTask, false, false);
               this.resetCancelledState();
               if (!multiQueriesEnabled) {
                  ((NativeSession)locallyScopedConn.getSession()).disableMultiQueries();
               }

               this.clearBatch();
            }
         } finally {
            connectionLock.unlock();
         }

         return var70;
      } catch (CJException var63) {
         throw SQLExceptionsMapping.translateException(var63, this.getExceptionInterceptor());
      }
   }

   protected int setOneBatchedParameterSet(PreparedStatement batchedStatement, int batchedParamIndex, Object paramSet) throws SQLException {
      BindValue[] bindValues = ((QueryBindings)paramSet).getBindValues();
      QueryBindings batchedStatementBindings = ((PreparedQuery)((ClientPreparedStatement)batchedStatement).getQuery()).getQueryBindings();

      for (int j = 0; j < bindValues.length; j++) {
         batchedStatementBindings.setFromBindValue(batchedParamIndex++ - 1, bindValues[j]);
      }

      return batchedParamIndex;
   }

   private String generateMultiStatementForBatch(int numBatches) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         String var12;
         try {
            String origSql = ((PreparedQuery)this.query).getOriginalSql();
            StringBuilder newStatementSql = new StringBuilder((origSql.length() + 1) * numBatches);
            newStatementSql.append(origSql);

            for (int i = 0; i < numBatches - 1; i++) {
               newStatementSql.append(';');
               newStatementSql.append(origSql);
            }

            var12 = newStatementSql.toString();
         } finally {
            connectionLock.unlock();
         }

         return var12;
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   protected long[] executeBatchWithMultiValuesClause(long batchTimeout) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         long[] var69;
         try {
            JdbcConnection locallyScopedConn = this.connection;
            int numBatchedArgs = this.query.getBatchedArgs().size();
            if (this.retrieveGeneratedKeys) {
               this.batchedGeneratedKeys = new ArrayList<>(numBatchedArgs);
            }

            int numValuesPerBatch = ((PreparedQuery)this.query).computeBatchSize(numBatchedArgs);
            if (numBatchedArgs < numValuesPerBatch) {
               numValuesPerBatch = numBatchedArgs;
            }

            JdbcPreparedStatement batchedStatement = null;
            int batchedParamIndex = 1;
            long updateCountRunningTotal = 0L;
            int numberToExecuteAsMultiValue = 0;
            int batchCounter = 0;
            CancelQueryTask timeoutTask = null;
            SQLException sqlEx = null;
            long[] updateCounts = new long[numBatchedArgs];

            try {
               try {
                  batchedStatement = this.prepareBatchedInsertSQL(locallyScopedConn, numValuesPerBatch);
                  timeoutTask = this.startQueryTimer(batchedStatement, batchTimeout);
                  numberToExecuteAsMultiValue = numBatchedArgs < numValuesPerBatch ? numBatchedArgs : numBatchedArgs / numValuesPerBatch;
                  int numberArgsToExecute = numberToExecuteAsMultiValue * numValuesPerBatch;

                  for (int i = 0; i < numberArgsToExecute; i++) {
                     if (i != 0 && i % numValuesPerBatch == 0) {
                        try {
                           updateCountRunningTotal += batchedStatement.executeLargeUpdate();
                        } catch (SQLException var58) {
                           sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, var58);
                        }

                        this.getBatchedGeneratedKeys(batchedStatement);
                        batchedStatement.clearParameters();
                        batchedParamIndex = 1;
                     }

                     batchedParamIndex = this.setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.query.getBatchedArgs().get(batchCounter++));
                  }

                  try {
                     updateCountRunningTotal += batchedStatement.executeLargeUpdate();
                  } catch (SQLException var57) {
                     sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, var57);
                  }

                  this.getBatchedGeneratedKeys(batchedStatement);
                  numValuesPerBatch = numBatchedArgs - batchCounter;
               } finally {
                  if (batchedStatement != null) {
                     batchedStatement.close();
                     batchedStatement = null;
                  }
               }

               try {
                  if (numValuesPerBatch > 0) {
                     batchedStatement = this.prepareBatchedInsertSQL(locallyScopedConn, numValuesPerBatch);
                     if (timeoutTask != null) {
                        timeoutTask.setQueryToCancel(batchedStatement);
                     }

                     batchedParamIndex = 1;

                     while (batchCounter < numBatchedArgs) {
                        batchedParamIndex = this.setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.query.getBatchedArgs().get(batchCounter++));
                     }

                     try {
                        updateCountRunningTotal += batchedStatement.executeLargeUpdate();
                     } catch (SQLException var56) {
                        sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, var56);
                     }

                     this.getBatchedGeneratedKeys(batchedStatement);
                  }

                  if (sqlEx != null) {
                     throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.exceptionInterceptor);
                  }

                  if (numBatchedArgs > 1) {
                     long updCount = updateCountRunningTotal > 0L ? -2L : 0L;

                     for (int j = 0; j < numBatchedArgs; j++) {
                        updateCounts[j] = updCount;
                     }
                  } else {
                     updateCounts[0] = updateCountRunningTotal;
                  }

                  var69 = updateCounts;
               } finally {
                  if (batchedStatement != null) {
                     batchedStatement.close();
                  }
               }
            } finally {
               this.stopQueryTimer(timeoutTask, false, false);
               this.resetCancelledState();
            }
         } finally {
            connectionLock.unlock();
         }

         return var69;
      } catch (CJException var63) {
         throw SQLExceptionsMapping.translateException(var63, this.getExceptionInterceptor());
      }
   }

   protected long[] executeBatchSerially(long batchTimeout) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         long[] var33;
         try {
            if (this.connection == null) {
               this.checkClosed();
            }

            long[] updateCounts = null;
            if (this.query.getBatchedArgs() != null) {
               int nbrCommands = this.query.getBatchedArgs().size();
               updateCounts = new long[nbrCommands];

               for (int i = 0; i < nbrCommands; i++) {
                  updateCounts[i] = -3L;
               }

               SQLException sqlEx = null;
               CancelQueryTask timeoutTask = null;

               try {
                  timeoutTask = this.startQueryTimer(this, batchTimeout);
                  if (this.retrieveGeneratedKeys) {
                     this.batchedGeneratedKeys = new ArrayList<>(nbrCommands);
                  }

                  int batchCommandIndex = ((PreparedQuery)this.query).getBatchCommandIndex();

                  for (int var35 = 0; var35 < nbrCommands; var35++) {
                     ((PreparedQuery)this.query).setBatchCommandIndex(var35);
                     Object arg = this.query.getBatchedArgs().get(var35);

                     try {
                        if (!(arg instanceof String)) {
                           QueryBindings queryBindings = (QueryBindings)arg;
                           updateCounts[var35] = this.executeUpdateInternal(queryBindings, true);
                           this.getBatchedGeneratedKeys(this.containsOnDuplicateKeyUpdate() ? 1 : 0);
                        } else {
                           updateCounts[var35] = this.executeUpdateInternal((String)arg, true, this.retrieveGeneratedKeys);
                           this.getBatchedGeneratedKeys(this.results.getFirstCharOfQuery() == 'I' && this.containsOnDuplicateKeyInString((String)arg) ? 1 : 0);
                        }
                     } catch (SQLException var28) {
                        updateCounts[var35] = -3L;
                        if (!this.continueBatchOnError
                           || var28 instanceof MySQLTimeoutException
                           || var28 instanceof MySQLStatementCancelledException
                           || this.hasDeadlockOrTimeoutRolledBackTx(var28)) {
                           long[] newUpdateCounts = new long[var35];
                           System.arraycopy(updateCounts, 0, newUpdateCounts, 0, var35);
                           throw SQLError.createBatchUpdateException(var28, newUpdateCounts, this.exceptionInterceptor);
                        }

                        sqlEx = var28;
                     }
                  }

                  if (sqlEx != null) {
                     throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.exceptionInterceptor);
                  }
               } catch (NullPointerException var29) {
                  try {
                     this.checkClosed();
                  } catch (StatementIsClosedException var27) {
                     int batchCommandIndex = ((PreparedQuery)this.query).getBatchCommandIndex();
                     updateCounts[batchCommandIndex] = -3L;
                     long[] newUpdateCounts = new long[batchCommandIndex];
                     System.arraycopy(updateCounts, 0, newUpdateCounts, 0, batchCommandIndex);
                     throw SQLError.createBatchUpdateException(SQLExceptionsMapping.translateException(var27), newUpdateCounts, this.exceptionInterceptor);
                  }

                  throw var29;
               } finally {
                  ((PreparedQuery)this.query).setBatchCommandIndex(-1);
                  this.stopQueryTimer(timeoutTask, false, false);
                  this.resetCancelledState();
               }
            }

            var33 = updateCounts != null ? updateCounts : new long[0];
         } finally {
            connectionLock.unlock();
         }

         return var33;
      } catch (CJException var32) {
         throw SQLExceptionsMapping.translateException(var32, this.getExceptionInterceptor());
      }
   }

   protected <M extends Message> ResultSetInternalMethods executeInternal(
      int maxRowsToRetrieve, M sendPacket, boolean createStreamingResultSet, boolean queryIsSelectOnly, ColumnDefinition metadata, boolean isBatch
   ) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         ResultSetInternalMethods var11;
         try {
            JdbcConnection locallyScopedConnection = this.connection;
            ((PreparedQuery)this.query).getQueryBindings().setNumberOfExecutions(((PreparedQuery)this.query).getQueryBindings().getNumberOfExecutions() + 1);
            CancelQueryTask timeoutTask = null;

            ResultSetInternalMethods rs;
            try {
               timeoutTask = this.startQueryTimer(this, this.getTimeoutInMillis());
               if (!isBatch) {
                  this.statementBegins();
               }

               rs = ((NativeSession)locallyScopedConnection.getSession())
                  .execSQL(
                     this, null, maxRowsToRetrieve, (NativePacketPayload)sendPacket, createStreamingResultSet, this.getResultSetFactory(), metadata, isBatch
                  );
               if (timeoutTask != null) {
                  this.stopQueryTimer(timeoutTask, true, true);
                  timeoutTask = null;
               }
            } finally {
               if (!isBatch) {
                  this.query.getStatementExecuting().set(false);
               }

               this.stopQueryTimer(timeoutTask, false, false);
            }

            var11 = rs;
         } catch (NullPointerException var23) {
            this.checkClosed();
            throw var23;
         } finally {
            connectionLock.unlock();
         }

         return var11;
      } catch (CJException var25) {
         throw SQLExceptionsMapping.translateException(var25, this.getExceptionInterceptor());
      }
   }

   @Override
   public ResultSet executeQuery() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         ResultSetInternalMethods var53;
         try {
            TelemetrySpan span = this.getSession().getTelemetryHandler().startSpan(TelemetrySpanName.STMT_EXECUTE_PREPARED);

            try (TelemetryScope scope = span.makeCurrent()) {
               String dbOperation = this.getQueryInfo().getStatementKeyword();
               span.setAttribute(TelemetryAttribute.DB_NAME, this.getCurrentDatabase());
               span.setAttribute(TelemetryAttribute.DB_OPERATION, dbOperation);
               span.setAttribute(TelemetryAttribute.DB_STATEMENT, dbOperation + " (...)");
               span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
               span.setAttribute(TelemetryAttribute.DB_USER, this.connection.getUser());
               span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
               span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
               JdbcConnection locallyScopedConn = this.connection;
               if (!this.doPingInstead) {
                  QueryReturnType queryReturnType = this.getQueryInfo().getQueryReturnType();
                  if (queryReturnType != QueryReturnType.PRODUCES_RESULT_SET && queryReturnType != QueryReturnType.MAY_PRODUCE_RESULT_SET) {
                     throw SQLError.createSQLException(Messages.getString("Statement.57"), "S1009", this.getExceptionInterceptor());
                  }
               }

               this.batchedGeneratedKeys = null;
               this.resetCancelledState();
               this.implicitlyCloseAllOpenResults();
               this.clearWarnings();
               if (!this.doPingInstead) {
                  this.setupStreamingTimeout(locallyScopedConn);
                  Message sendPacket = ((PreparedQuery)this.query).fillSendPacket(((PreparedQuery)this.query).getQueryBindings());
                  String oldDb = null;
                  if (!locallyScopedConn.getDatabase().equals(this.getCurrentDatabase())) {
                     oldDb = locallyScopedConn.getDatabase();
                     locallyScopedConn.setDatabase(this.getCurrentDatabase());
                  }

                  CachedResultSetMetaData cachedMetadata = null;
                  boolean cacheResultSetMetadata = locallyScopedConn.getPropertySet().getBooleanProperty(PropertyKey.cacheResultSetMetadata).getValue();
                  String origSql = ((PreparedQuery)this.query).getOriginalSql();
                  if (cacheResultSetMetadata) {
                     cachedMetadata = locallyScopedConn.getCachedMetaData(origSql);
                  }

                  locallyScopedConn.setSessionMaxRows(this.maxRows);
                  this.results = this.executeInternal(this.maxRows, sendPacket, this.meetsConditionsForStreamingResultSet(), true, cachedMetadata, false);
                  if (oldDb != null) {
                     locallyScopedConn.setDatabase(oldDb);
                  }

                  if (cachedMetadata != null) {
                     locallyScopedConn.initializeResultsMetadataFromCache(origSql, cachedMetadata, this.results);
                  } else if (cacheResultSetMetadata) {
                     locallyScopedConn.initializeResultsMetadataFromCache(origSql, null, this.results);
                  }

                  this.lastInsertId = this.results.getUpdateID();
                  return this.results;
               }

               this.doPingInstead();
               var53 = this.results;
            } catch (Throwable var49) {
               span.setError(var49);
               throw var49;
            } finally {
               span.end();
            }
         } finally {
            connectionLock.unlock();
         }

         return var53;
      } catch (CJException var52) {
         throw SQLExceptionsMapping.translateException(var52, this.getExceptionInterceptor());
      }
   }

   @Override
   public int executeUpdate() throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeUpdate());
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   protected long executeUpdateInternal(boolean clearBatchedGeneratedKeysAndWarnings, boolean isBatch) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         long var4;
         try {
            if (clearBatchedGeneratedKeysAndWarnings) {
               this.clearWarnings();
               this.batchedGeneratedKeys = null;
            }

            var4 = this.executeUpdateInternal(((PreparedQuery)this.query).getQueryBindings(), isBatch);
         } finally {
            connectionLock.unlock();
         }

         return var4;
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   protected long executeUpdateInternal(QueryBindings bindings, boolean isReallyBatch) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         long var12;
         try {
            TelemetrySpan span = this.getSession().getTelemetryHandler().startSpan(TelemetrySpanName.STMT_EXECUTE_PREPARED);

            try (TelemetryScope scope = span.makeCurrent()) {
               String dbOperation = this.getQueryInfo().getStatementKeyword();
               span.setAttribute(TelemetryAttribute.DB_NAME, this.getCurrentDatabase());
               span.setAttribute(TelemetryAttribute.DB_OPERATION, dbOperation);
               span.setAttribute(TelemetryAttribute.DB_STATEMENT, dbOperation + " (...)");
               span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
               span.setAttribute(TelemetryAttribute.DB_USER, this.connection.getUser());
               span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
               span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
               JdbcConnection locallyScopedConn = this.connection;
               if (locallyScopedConn.isReadOnly(false)) {
                  throw SQLError.createSQLException(
                     Messages.getString("PreparedStatement.34") + Messages.getString("PreparedStatement.35"), "S1009", this.exceptionInterceptor
                  );
               }

               if (!this.isNonResultSetProducingQuery()) {
                  throw SQLError.createSQLException(Messages.getString("PreparedStatement.37"), "01S03", this.exceptionInterceptor);
               }

               this.resetCancelledState();
               this.implicitlyCloseAllOpenResults();
               ResultSetInternalMethods rs = null;
               Message sendPacket = ((PreparedQuery)this.query).fillSendPacket(bindings);
               String oldDb = null;
               if (!locallyScopedConn.getDatabase().equals(this.getCurrentDatabase())) {
                  oldDb = locallyScopedConn.getDatabase();
                  locallyScopedConn.setDatabase(this.getCurrentDatabase());
               }

               locallyScopedConn.setSessionMaxRows(-1);
               rs = this.executeInternal(-1, sendPacket, false, false, null, isReallyBatch);
               if (this.retrieveGeneratedKeys) {
                  rs.setFirstCharOfQuery(this.getQueryInfo().getFirstStmtChar());
               }

               if (oldDb != null) {
                  locallyScopedConn.setDatabase(oldDb);
               }

               this.results = rs;
               this.updateCount = rs.getUpdateCount();
               if (this.containsOnDuplicateKeyUpdate() && this.compensateForOnDuplicateKeyUpdate && (this.updateCount == 2L || this.updateCount == 0L)) {
                  this.updateCount = 1L;
               }

               this.lastInsertId = rs.getUpdateID();
               var12 = this.updateCount;
            } catch (Throwable var47) {
               span.setError(var47);
               throw var47;
            } finally {
               span.end();
            }
         } finally {
            connectionLock.unlock();
         }

         return var12;
      } catch (CJException var50) {
         throw SQLExceptionsMapping.translateException(var50, this.getExceptionInterceptor());
      }
   }

   protected boolean containsOnDuplicateKeyUpdate() {
      return this.getQueryInfo().containsOnDuplicateKeyUpdate();
   }

   protected ClientPreparedStatement prepareBatchedInsertSQL(JdbcConnection localConn, int numBatches) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         ClientPreparedStatement var5;
         try {
            ClientPreparedStatement pstmt = new ClientPreparedStatement(
               localConn,
               "Rewritten batch of: " + ((PreparedQuery)this.query).getOriginalSql(),
               this.getCurrentDatabase(),
               this.getQueryInfo().getQueryInfoForBatch(numBatches)
            );
            pstmt.setRetrieveGeneratedKeys(this.retrieveGeneratedKeys);
            pstmt.rewrittenBatchSize = numBatches;
            this.getQueryAttributesBindings().runThroughAll(a -> pstmt.setAttribute(a.getName(), a.getValue()));
            var5 = pstmt;
         } finally {
            connectionLock.unlock();
         }

         return var5;
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   protected void setRetrieveGeneratedKeys(boolean flag) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            this.retrieveGeneratedKeys = flag;
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public byte[] getBytesRepresentation(int parameterIndex) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         byte[] var3;
         try {
            var3 = ((PreparedQuery)this.query).getQueryBindings().getBytesRepresentation(this.getCoreParameterIndex(parameterIndex));
         } finally {
            connectionLock.unlock();
         }

         return var3;
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public ResultSetMetaData getMetaData() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         JdbcPreparedStatement mdStmt;
         try {
            if (this.isResultSetProducingQuery()) {
               mdStmt = null;
               ResultSet mdRs = null;
               if (this.pstmtResultMetaData == null) {
                  try {
                     mdStmt = new ClientPreparedStatement(
                        this.connection, ((PreparedQuery)this.query).getOriginalSql(), this.getCurrentDatabase(), this.getQueryInfo()
                     );
                     mdStmt.setMaxRows(1);
                     int paramCount = ((PreparedQuery)this.query).getParameterCount();

                     for (int i = 1; i <= paramCount; i++) {
                        mdStmt.setString(i, null);
                     }

                     boolean hadResults = mdStmt.execute();
                     if (hadResults) {
                        mdRs = mdStmt.getResultSet();
                        this.pstmtResultMetaData = mdRs.getMetaData();
                     } else {
                        this.pstmtResultMetaData = new org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.ResultSetMetaData(
                           this.session,
                           new Field[0],
                           this.session.getPropertySet().getBooleanProperty(PropertyKey.useOldAliasMetadataBehavior).getValue(),
                           this.session.getPropertySet().getBooleanProperty(PropertyKey.yearIsDateType).getValue(),
                           this.exceptionInterceptor
                        );
                     }
                  } finally {
                     SQLException sqlExRethrow = null;
                     if (mdRs != null) {
                        try {
                           mdRs.close();
                        } catch (SQLException var26) {
                           sqlExRethrow = var26;
                        }

                        ResultSet var32 = null;
                     }

                     if (mdStmt != null) {
                        try {
                           mdStmt.close();
                        } catch (SQLException var25) {
                           sqlExRethrow = var25;
                        }

                        mdStmt = null;
                     }

                     if (sqlExRethrow != null) {
                        throw sqlExRethrow;
                     }
                  }
               }

               return this.pstmtResultMetaData;
            }

            mdStmt = null;
         } finally {
            connectionLock.unlock();
         }

         return mdStmt;
      } catch (CJException var29) {
         throw SQLExceptionsMapping.translateException(var29, this.getExceptionInterceptor());
      }
   }

   protected boolean isResultSetProducingQuery() {
      QueryReturnType queryReturnType = this.getQueryInfo().getQueryReturnType();
      return queryReturnType == QueryReturnType.PRODUCES_RESULT_SET || queryReturnType == QueryReturnType.MAY_PRODUCE_RESULT_SET;
   }

   private boolean isNonResultSetProducingQuery() {
      QueryReturnType queryReturnType = this.getQueryInfo().getQueryReturnType();
      return queryReturnType == QueryReturnType.DOES_NOT_PRODUCE_RESULT_SET || queryReturnType == QueryReturnType.MAY_PRODUCE_RESULT_SET;
   }

   @Override
   public ParameterMetaData getParameterMetaData() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         MysqlParameterMetadata var2;
         try {
            if (this.parameterMetaData == null) {
               if (this.session.getPropertySet().getBooleanProperty(PropertyKey.generateSimpleParameterMetadata).getValue()) {
                  this.parameterMetaData = new MysqlParameterMetadata(((PreparedQuery)this.query).getParameterCount());
               } else {
                  this.parameterMetaData = new MysqlParameterMetadata(
                     this.session, null, ((PreparedQuery)this.query).getParameterCount(), this.exceptionInterceptor
                  );
               }
            }

            var2 = this.parameterMetaData;
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public QueryInfo getQueryInfo() {
      return ((PreparedQuery)this.query).getQueryInfo();
   }

   private void initializeFromQueryInfo() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            int parameterCount = this.getQueryInfo().getStaticSqlParts().length - 1;
            ((PreparedQuery)this.query).setParameterCount(parameterCount);
            ((PreparedQuery)this.query).setQueryBindings(new NativeQueryBindings(parameterCount, this.session, NativeQueryBindValue::new));
            this.clearParameters();
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isNull(int paramIndex) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         boolean var3;
         try {
            var3 = ((PreparedQuery)this.query).getQueryBindings().getBindValues()[this.getCoreParameterIndex(paramIndex)].isNull();
         } finally {
            connectionLock.unlock();
         }

         return var3;
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void doClose(CloseOption... options) throws SQLException {
      try {
         JdbcConnection locallyScopedConn = this.connection;
         if (locallyScopedConn != null) {
            Lock connectionLock = this.checkClosed().getConnectionLock();
            connectionLock.lock();

            try {
               if (!this.isClosed) {
                  if (this.useUsageAdvisor) {
                     QueryBindings qb = ((PreparedQuery)this.query).getQueryBindings();
                     if (qb == null || qb.getNumberOfExecutions() <= 1) {
                        this.session
                           .getProfilerEventHandler()
                           .processEvent((byte)0, this.session, this, null, 0L, new Throwable(), Messages.getString("PreparedStatement.43"));
                     }
                  }

                  super.doClose(options);
                  ((PreparedQuery)this.query).setOriginalSql(null);
                  ((PreparedQuery)this.query).setQueryBindings(null);
                  return;
               }
            } finally {
               connectionLock.unlock();
            }
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public String getPreparedSql() {
      Lock connectionLock = this.checkClosed().getConnectionLock();
      connectionLock.lock();

      String var2;
      try {
         if (this.rewrittenBatchSize != 0) {
            return this.getQueryInfo().getSqlForBatch();
         }

         var2 = ((PreparedQuery)this.query).getOriginalSql();
      } finally {
         connectionLock.unlock();
      }

      return var2;
   }

   @Override
   public int getUpdateCount() throws SQLException {
      try {
         int count = super.getUpdateCount();
         if (this.containsOnDuplicateKeyUpdate() && this.compensateForOnDuplicateKeyUpdate && (count == 2 || count == 0)) {
            count = 1;
         }

         return count;
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public long executeLargeUpdate() throws SQLException {
      try {
         return this.executeUpdateInternal(true, false);
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public ParameterBindings getParameterBindings() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         ParameterBindingsImpl var2;
         try {
            var2 = new ParameterBindingsImpl((PreparedQuery)this.query, this.session, this.resultSetFactory);
         } finally {
            connectionLock.unlock();
         }

         return var2;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   protected int getParameterIndexOffset() {
      return 0;
   }

   protected void checkBounds(int paramIndex, int parameterIndexOffset) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            if (paramIndex < 1) {
               throw SQLError.createSQLException(
                  Messages.getString("PreparedStatement.49") + paramIndex + Messages.getString("PreparedStatement.50"), "S1009", this.exceptionInterceptor
               );
            }

            if (paramIndex > ((PreparedQuery)this.query).getParameterCount()) {
               throw SQLError.createSQLException(
                  Messages.getString("PreparedStatement.51")
                     + paramIndex
                     + Messages.getString("PreparedStatement.52")
                     + ((PreparedQuery)this.query).getParameterCount()
                     + Messages.getString("PreparedStatement.53"),
                  "S1009",
                  this.exceptionInterceptor
               );
            }

            if (parameterIndexOffset == -1 && paramIndex == 1) {
               throw SQLError.createSQLException(Messages.getString("PreparedStatement.63"), "S1009", this.exceptionInterceptor);
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public final int getCoreParameterIndex(int paramIndex) throws SQLException {
      int parameterIndexOffset = this.getParameterIndexOffset();
      this.checkBounds(paramIndex, parameterIndexOffset);
      return paramIndex - 1 + parameterIndexOffset;
   }

   @Override
   public void setArray(int i, Array x) throws SQLException {
      try {
         throw SQLError.createSQLFeatureNotSupportedException();
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setAsciiStream(this.getCoreParameterIndex(parameterIndex), x, -1);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setAsciiStream(this.getCoreParameterIndex(parameterIndex), x, length);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setAsciiStream(this.getCoreParameterIndex(parameterIndex), x, (int)length);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setBigDecimal(this.getCoreParameterIndex(parameterIndex), x);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setBinaryStream(this.getCoreParameterIndex(parameterIndex), x, -1);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setBinaryStream(this.getCoreParameterIndex(parameterIndex), x, length);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setBinaryStream(this.getCoreParameterIndex(parameterIndex), x, (int)length);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setBlob(int i, java.sql.Blob x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setBlob(this.getCoreParameterIndex(i), x);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setBinaryStream(this.getCoreParameterIndex(parameterIndex), inputStream, -1);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setBinaryStream(this.getCoreParameterIndex(parameterIndex), inputStream, (int)length);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setBoolean(int parameterIndex, boolean x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setBoolean(this.getCoreParameterIndex(parameterIndex), x);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setByte(int parameterIndex, byte x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setByte(this.getCoreParameterIndex(parameterIndex), x);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setBytes(int parameterIndex, byte[] x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setBytes(this.getCoreParameterIndex(parameterIndex), x, true);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setBytes(int parameterIndex, byte[] x, boolean escapeIfNeeded) throws SQLException {
      Lock connectionLock = this.checkClosed().getConnectionLock();
      connectionLock.lock();

      try {
         ((PreparedQuery)this.query).getQueryBindings().setBytes(this.getCoreParameterIndex(parameterIndex), x, escapeIfNeeded);
      } finally {
         connectionLock.unlock();
      }
   }

   @Override
   public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setCharacterStream(this.getCoreParameterIndex(parameterIndex), reader, -1);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setCharacterStream(this.getCoreParameterIndex(parameterIndex), reader, length);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setCharacterStream(this.getCoreParameterIndex(parameterIndex), reader, (int)length);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setClob(int parameterIndex, Reader reader) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setCharacterStream(this.getCoreParameterIndex(parameterIndex), reader, -1);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setCharacterStream(this.getCoreParameterIndex(parameterIndex), reader, (int)length);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setClob(int i, java.sql.Clob x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setClob(this.getCoreParameterIndex(i), x);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setDate(int parameterIndex, Date x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setDate(this.getCoreParameterIndex(parameterIndex), x, null);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setDate(this.getCoreParameterIndex(parameterIndex), x, cal);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setDouble(int parameterIndex, double x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setDouble(this.getCoreParameterIndex(parameterIndex), x);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setFloat(int parameterIndex, float x) throws SQLException {
      try {
         ((PreparedQuery)this.query).getQueryBindings().setFloat(this.getCoreParameterIndex(parameterIndex), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setInt(int parameterIndex, int x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setInt(this.getCoreParameterIndex(parameterIndex), x);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setLong(int parameterIndex, long x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setLong(this.getCoreParameterIndex(parameterIndex), x);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setBigInteger(int parameterIndex, BigInteger x) throws SQLException {
      Lock connectionLock = this.checkClosed().getConnectionLock();
      connectionLock.lock();

      try {
         ((PreparedQuery)this.query).getQueryBindings().setBigInteger(this.getCoreParameterIndex(parameterIndex), x);
      } finally {
         connectionLock.unlock();
      }
   }

   @Override
   public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setNCharacterStream(this.getCoreParameterIndex(parameterIndex), value, -1L);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setNCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setNCharacterStream(this.getCoreParameterIndex(parameterIndex), reader, length);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setNClob(int parameterIndex, Reader reader) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setNCharacterStream(this.getCoreParameterIndex(parameterIndex), reader, -1L);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setNCharacterStream(this.getCoreParameterIndex(parameterIndex), reader, length);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setNClob(int parameterIndex, java.sql.NClob value) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setNClob(this.getCoreParameterIndex(parameterIndex), value);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setNString(int parameterIndex, String x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setNString(this.getCoreParameterIndex(parameterIndex), x);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setNull(int parameterIndex, int sqlType) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setNull(this.getCoreParameterIndex(parameterIndex));
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setNull(this.getCoreParameterIndex(parameterIndex));
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setNull(int parameterIndex, MysqlType mysqlType) throws SQLException {
      this.setNull(parameterIndex, mysqlType.getJdbcType());
   }

   @Override
   public void setObject(int parameterIndex, Object parameterObj) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setObject(this.getCoreParameterIndex(parameterIndex), parameterObj);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setObject(int parameterIndex, Object parameterObj, int targetSqlType) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query)
               .getQueryBindings()
               .setObject(this.getCoreParameterIndex(parameterIndex), parameterObj, MysqlType.getByJdbcType(targetSqlType), -1);
         } catch (FeatureNotAvailableException var11) {
            throw SQLError.createSQLFeatureNotSupportedException(
               Messages.getString("Statement.UnsupportedSQLType") + JDBCType.valueOf(targetSqlType), "S1C00", this.exceptionInterceptor
            );
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var13) {
         throw SQLExceptionsMapping.translateException(var13, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setObject(int parameterIndex, Object parameterObj, SQLType targetSqlType) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            if (targetSqlType instanceof MysqlType) {
               ((PreparedQuery)this.query).getQueryBindings().setObject(this.getCoreParameterIndex(parameterIndex), parameterObj, (MysqlType)targetSqlType, -1);
            } else {
               this.setObject(parameterIndex, parameterObj, targetSqlType.getVendorTypeNumber());
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setObject(int parameterIndex, Object parameterObj, int targetSqlType, int scale) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query)
               .getQueryBindings()
               .setObject(this.getCoreParameterIndex(parameterIndex), parameterObj, MysqlType.getByJdbcType(targetSqlType), scale);
         } catch (FeatureNotAvailableException var12) {
            throw SQLError.createSQLFeatureNotSupportedException(
               Messages.getString("Statement.UnsupportedSQLType") + JDBCType.valueOf(targetSqlType), "S1C00", this.exceptionInterceptor
            );
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var14) {
         throw SQLExceptionsMapping.translateException(var14, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            if (targetSqlType instanceof MysqlType) {
               ((PreparedQuery)this.query).getQueryBindings().setObject(this.getCoreParameterIndex(parameterIndex), x, (MysqlType)targetSqlType, scaleOrLength);
            } else {
               this.setObject(parameterIndex, x, targetSqlType.getVendorTypeNumber(), scaleOrLength);
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setRef(int i, Ref x) throws SQLException {
      try {
         throw SQLError.createSQLFeatureNotSupportedException();
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setRowId(int parameterIndex, RowId x) throws SQLException {
      try {
         throw SQLError.createSQLFeatureNotSupportedException();
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setShort(int parameterIndex, short x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setShort(this.getCoreParameterIndex(parameterIndex), x);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
      try {
         if (xmlObject == null) {
            this.setNull(parameterIndex, MysqlType.VARCHAR);
         } else {
            this.setCharacterStream(parameterIndex, ((MysqlSQLXML)xmlObject).serializeAsCharacterStream());
         }
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setString(int parameterIndex, String x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setString(this.getCoreParameterIndex(parameterIndex), x);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setTime(int parameterIndex, Time x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setTime(this.getCoreParameterIndex(parameterIndex), x, null);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setTime(this.getCoreParameterIndex(parameterIndex), x, cal);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setTimestamp(this.getCoreParameterIndex(parameterIndex), x, null, null, MysqlType.TIMESTAMP);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((PreparedQuery)this.query).getQueryBindings().setTimestamp(this.getCoreParameterIndex(parameterIndex), x, cal, null, MysqlType.TIMESTAMP);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   @Deprecated
   @Override
   public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
      try {
         this.setBinaryStream(parameterIndex, x, length);
         ((PreparedQuery)this.query).getQueryBindings().getBindValues()[this.getCoreParameterIndex(parameterIndex)].setMysqlType(MysqlType.TEXT);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setURL(int parameterIndex, URL arg) throws SQLException {
      try {
         if (arg == null) {
            this.setNull(parameterIndex, MysqlType.VARCHAR);
         } else {
            this.setString(parameterIndex, arg.toString());
            ((PreparedQuery)this.query).getQueryBindings().getBindValues()[this.getCoreParameterIndex(parameterIndex)].setMysqlType(MysqlType.VARCHAR);
         }
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }
}
