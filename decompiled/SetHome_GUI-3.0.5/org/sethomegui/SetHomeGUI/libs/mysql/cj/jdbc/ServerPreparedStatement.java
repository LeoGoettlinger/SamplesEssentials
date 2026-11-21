package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.BindValue;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.CancelQueryTask;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.NativeSession;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.PreparedQuery;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.QueryBindings;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.QueryInfo;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.ServerPreparedQuery;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Session;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionInterceptor;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.WrongArgumentException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.MySQLStatementCancelledException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.MySQLTimeoutException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.SQLError;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.ResultSetInternalMethods;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ColumnDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Message;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryAttribute;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryScope;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetrySpan;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetrySpanName;

public class ServerPreparedStatement extends ClientPreparedStatement {
   private boolean hasOnDuplicateKeyUpdate = false;
   private boolean invalid = false;
   private CJException invalidationException;
   protected boolean isCacheable = false;
   protected boolean isCached = false;

   protected static ServerPreparedStatement getInstance(JdbcConnection conn, String sql, String db, int resultSetType, int resultSetConcurrency) throws SQLException {
      return new ServerPreparedStatement(conn, sql, db, resultSetType, resultSetConcurrency);
   }

   protected ServerPreparedStatement(JdbcConnection conn, String sql, String db, int resultSetType, int resultSetConcurrency) throws SQLException {
      super(conn, db);
      this.checkNullOrEmptyQuery(sql);
      String statementComment = this.session.getQueryComment();
      PreparedQuery prepQuery = (PreparedQuery)this.query;
      prepQuery.setOriginalSql(statementComment == null ? sql : "/* " + statementComment + " */ " + sql);
      prepQuery.setQueryInfo(new QueryInfo(prepQuery.getOriginalSql(), this.session, this.charEncoding));
      this.hasOnDuplicateKeyUpdate = prepQuery.getQueryInfo().containsOnDuplicateKeyUpdate();

      try {
         this.serverPrepare(sql);
      } catch (SQLException | CJException var9) {
         this.doClose(CloseOption.NO_CACHE);
         throw SQLExceptionsMapping.translateException(var9, this.exceptionInterceptor);
      }

      this.setResultSetType(resultSetType);
      this.setResultSetConcurrency(resultSetConcurrency);
   }

   @Override
   protected void initQuery() {
      this.query = ServerPreparedQuery.getInstance(this.session);
   }

   @Override
   public String toString() {
      try {
         StringBuilder toStringBuf = new StringBuilder();
         toStringBuf.append(this.getClass().getName() + "[");
         toStringBuf.append(((ServerPreparedQuery)this.query).getServerStatementId());
         toStringBuf.append("]: ");
         toStringBuf.append(((PreparedQuery)this.query).asSql());
         return toStringBuf.toString();
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
            this.query.addBatch(((PreparedQuery)this.query).getQueryBindings().clone());
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   protected JdbcConnection checkClosed() {
      if (this.invalid) {
         throw this.invalidationException;
      } else {
         return super.checkClosed();
      }
   }

   @Override
   public void clearParameters() {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            ((ServerPreparedQuery)this.query).clearParameters(true);
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   protected void setClosed(boolean flag) {
      this.isClosed = flag;
   }

   @Override
   protected long[] executeBatchSerially(long batchTimeout) throws SQLException {
      Lock connectionLock = this.checkClosed().getConnectionLock();
      connectionLock.lock();

      long[] var44;
      try {
         JdbcConnection locallyScopedConn = this.connection;
         if (locallyScopedConn.isReadOnly()) {
            throw SQLError.createSQLException(
               Messages.getString("ServerPreparedStatement.2") + Messages.getString("ServerPreparedStatement.3"), "S1009", this.exceptionInterceptor
            );
         }

         this.clearWarnings();
         BindValue[] oldBindValues = ((ServerPreparedQuery)this.query).getQueryBindings().getBindValues();

         try {
            long[] updateCounts = null;
            if (this.query.getBatchedArgs() != null) {
               int nbrCommands = this.query.getBatchedArgs().size();
               updateCounts = new long[nbrCommands];
               if (this.retrieveGeneratedKeys) {
                  this.batchedGeneratedKeys = new ArrayList<>(nbrCommands);
               }

               for (int i = 0; i < nbrCommands; i++) {
                  updateCounts[i] = -3L;
               }

               SQLException sqlEx = null;
               int commandIndex = 0;
               BindValue[] previousBindValuesForBatch = null;
               CancelQueryTask timeoutTask = null;

               try {
                  timeoutTask = this.startQueryTimer(this, batchTimeout);

                  for (int var46 = 0; var46 < nbrCommands; var46++) {
                     Object arg = this.query.getBatchedArgs().get(var46);

                     try {
                        if (arg instanceof String) {
                           updateCounts[var46] = this.executeUpdateInternal((String)arg, true, this.retrieveGeneratedKeys);
                           this.getBatchedGeneratedKeys(this.results.getFirstCharOfQuery() == 'I' && this.containsOnDuplicateKeyInString((String)arg) ? 1 : 0);
                        } else {
                           ((PreparedQuery)this.query).setQueryBindings((QueryBindings)arg);
                           BindValue[] parameterBindings = ((QueryBindings)arg).getBindValues();
                           if (previousBindValuesForBatch != null) {
                              for (int j = 0; j < parameterBindings.length; j++) {
                                 if (parameterBindings[j].getMysqlType() != previousBindValuesForBatch[j].getMysqlType()) {
                                    ((ServerPreparedQuery)this.query).getQueryBindings().getSendTypesToServer().set(true);
                                    break;
                                 }
                              }
                           }

                           try {
                              updateCounts[var46] = this.executeUpdateInternal(false, true);
                           } finally {
                              previousBindValuesForBatch = parameterBindings;
                           }

                           this.getBatchedGeneratedKeys(this.containsOnDuplicateKeyUpdate() ? 1 : 0);
                        }
                     } catch (SQLException var40) {
                        updateCounts[var46] = -3L;
                        if (!this.continueBatchOnError
                           || var40 instanceof MySQLTimeoutException
                           || var40 instanceof MySQLStatementCancelledException
                           || this.hasDeadlockOrTimeoutRolledBackTx(var40)) {
                           long[] newUpdateCounts = new long[var46];
                           System.arraycopy(updateCounts, 0, newUpdateCounts, 0, var46);
                           throw SQLError.createBatchUpdateException(var40, newUpdateCounts, this.exceptionInterceptor);
                        }

                        sqlEx = var40;
                     }
                  }
               } finally {
                  this.stopQueryTimer(timeoutTask, false, false);
                  this.resetCancelledState();
               }

               if (sqlEx != null) {
                  throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.exceptionInterceptor);
               }
            }

            var44 = updateCounts != null ? updateCounts : new long[0];
         } finally {
            ((ServerPreparedQuery)this.query).getQueryBindings().setBindValues(oldBindValues);
            ((ServerPreparedQuery)this.query).getQueryBindings().getSendTypesToServer().set(true);
            this.clearBatch();
         }
      } finally {
         connectionLock.unlock();
      }

      return var44;
   }

   private static SQLException appendMessageToException(SQLException sqlEx, String messageToAppend, ExceptionInterceptor interceptor) {
      String sqlState = sqlEx.getSQLState();
      int vendorErrorCode = sqlEx.getErrorCode();
      SQLException sqlExceptionWithNewMessage = SQLError.createSQLException(sqlEx.getMessage() + messageToAppend, sqlState, vendorErrorCode, interceptor);
      sqlExceptionWithNewMessage.setStackTrace(sqlEx.getStackTrace());
      return sqlExceptionWithNewMessage;
   }

   @Override
   protected <M extends Message> ResultSetInternalMethods executeInternal(
      int maxRowsToRetrieve, M sendPacket, boolean createStreamingResultSet, boolean queryIsSelectOnly, ColumnDefinition metadata, boolean isBatch
   ) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         ResultSetInternalMethods var22;
         try {
            ((PreparedQuery)this.query).getQueryBindings().setNumberOfExecutions(((PreparedQuery)this.query).getQueryBindings().getNumberOfExecutions() + 1);

            try {
               var22 = this.serverExecute(maxRowsToRetrieve, createStreamingResultSet, metadata);
            } catch (SQLException var18) {
               SQLException sqlEx = var18;
               if (this.session.getPropertySet().getBooleanProperty(PropertyKey.enablePacketDebug).getValue()) {
                  this.session.dumpPacketRingBuffer();
               }

               if (this.dumpQueriesOnException.getValue()) {
                  String extractedSql = this.toString();
                  StringBuilder messageBuf = new StringBuilder(extractedSql.length() + 32);
                  messageBuf.append("\n\nQuery being executed when exception was thrown:\n");
                  messageBuf.append(extractedSql);
                  messageBuf.append("\n\n");
                  sqlEx = appendMessageToException(var18, messageBuf.toString(), this.exceptionInterceptor);
               }

               throw sqlEx;
            } catch (Exception var19) {
               if (this.session.getPropertySet().getBooleanProperty(PropertyKey.enablePacketDebug).getValue()) {
                  this.session.dumpPacketRingBuffer();
               }

               SQLException sqlExx = SQLError.createSQLException(var19.toString(), "S1000", var19, this.exceptionInterceptor);
               if (this.dumpQueriesOnException.getValue()) {
                  String extractedSql = this.toString();
                  StringBuilder messageBuf = new StringBuilder(extractedSql.length() + 32);
                  messageBuf.append("\n\nQuery being executed when exception was thrown:\n");
                  messageBuf.append(extractedSql);
                  messageBuf.append("\n\n");
                  sqlExx = appendMessageToException(sqlExx, messageBuf.toString(), this.exceptionInterceptor);
               }

               throw sqlExx;
            }
         } finally {
            connectionLock.unlock();
         }

         return var22;
      } catch (CJException var21) {
         throw SQLExceptionsMapping.translateException(var21, this.getExceptionInterceptor());
      }
   }

   protected BindValue getBinding(int parameterIndex, boolean forLongData) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         BindValue var5;
         try {
            int i = this.getCoreParameterIndex(parameterIndex);
            var5 = ((ServerPreparedQuery)this.query).getQueryBindings().getBinding(i, forLongData);
         } finally {
            connectionLock.unlock();
         }

         return var5;
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   @Override
   public ResultSetMetaData getMetaData() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.ResultSetMetaData var3;
         try {
            ColumnDefinition resultFields = ((ServerPreparedQuery)this.query).getResultFields();
            var3 = resultFields != null && resultFields.getFields() != null
               ? new org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.ResultSetMetaData(
                  this.session,
                  resultFields.getFields(),
                  this.session.getPropertySet().getBooleanProperty(PropertyKey.useOldAliasMetadataBehavior).getValue(),
                  this.session.getPropertySet().getBooleanProperty(PropertyKey.yearIsDateType).getValue(),
                  this.exceptionInterceptor
               )
               : null;
         } finally {
            connectionLock.unlock();
         }

         return var3;
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public ParameterMetaData getParameterMetaData() throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         MysqlParameterMetadata var2;
         try {
            if (this.parameterMetaData == null) {
               this.parameterMetaData = new MysqlParameterMetadata(
                  this.session,
                  ((ServerPreparedQuery)this.query).getParameterFields(),
                  ((PreparedQuery)this.query).getParameterCount(),
                  this.exceptionInterceptor
               );
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
   public boolean isNull(int paramIndex) {
      throw new IllegalArgumentException(Messages.getString("ServerPreparedStatement.7"));
   }

   @Override
   public void doClose(CloseOption... options) throws SQLException {
      try {
         JdbcConnection locallyScopedConn = this.connection;
         if (locallyScopedConn != null) {
            Lock connectionLock = locallyScopedConn.getConnectionLock();
            connectionLock.lock();

            try {
               if (this.isClosed) {
                  return;
               }

               if (!CloseOption.NO_CACHE.notIn(options) || !this.isCacheable || !this.isPoolable()) {
                  if (this.connection == null) {
                     return;
                  }

                  CJException exceptionDuringClose = null;
                  if (this.isCached) {
                     locallyScopedConn.decachePreparedStatement(this);
                     this.isCached = false;
                  }

                  Session sessionLocalCopy = this.session;
                  String user = this.connection.getUser();
                  super.doClose(options);
                  ((ServerPreparedQuery)this.query).clearParameters(false);
                  if (!locallyScopedConn.isClosed()) {
                     TelemetrySpan span = sessionLocalCopy.getTelemetryHandler().startSpan(TelemetrySpanName.STMT_DEALLOCATE_PREPARED);

                     try (TelemetryScope scope = span.makeCurrent()) {
                        String dbOperation = this.getQueryInfo().getStatementKeyword();
                        span.setAttribute(TelemetryAttribute.DB_NAME, this.getCurrentDatabase());
                        span.setAttribute(TelemetryAttribute.DB_OPERATION, dbOperation);
                        span.setAttribute(TelemetryAttribute.DB_STATEMENT, dbOperation + " (...)");
                        span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
                        span.setAttribute(TelemetryAttribute.DB_USER, user);
                        span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
                        span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());

                        try {
                           ((NativeSession)locallyScopedConn.getSession())
                              .getProtocol()
                              .sendCommand(this.commandBuilder.buildComStmtClose(null, ((ServerPreparedQuery)this.query).getServerStatementId()), true, 0);
                        } catch (CJException var45) {
                           exceptionDuringClose = var45;
                        }
                     } catch (Throwable var48) {
                        span.setError(var48);
                        throw var48;
                     } finally {
                        span.end();
                     }
                  }

                  if (exceptionDuringClose != null) {
                     throw exceptionDuringClose;
                  }

                  return;
               }

               this.clearParameters();
               this.clearAttributes();
               this.isClosed = true;
               this.connection.recachePreparedStatement(this);
               this.isCached = true;
               this.results = null;
               this.generatedKeysResults = null;
            } finally {
               connectionLock.unlock();
            }
         }
      } catch (CJException var51) {
         throw SQLExceptionsMapping.translateException(var51, this.getExceptionInterceptor());
      }
   }

   protected void rePrepare() {
      Lock connectionLock = this.checkClosed().getConnectionLock();
      connectionLock.lock();

      try {
         TelemetrySpan span = this.session.getTelemetryHandler().startSpan(TelemetrySpanName.STMT_PREPARE);

         try (TelemetryScope scope = span.makeCurrent()) {
            String dbOperation = this.getQueryInfo().getStatementKeyword();
            span.setAttribute(TelemetryAttribute.DB_NAME, this.getCurrentDatabase());
            span.setAttribute(TelemetryAttribute.DB_OPERATION, dbOperation);
            span.setAttribute(TelemetryAttribute.DB_STATEMENT, dbOperation + " (...)");
            span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
            span.setAttribute(TelemetryAttribute.DB_USER, this.connection.getUser());
            span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
            span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
            this.invalidationException = null;

            try {
               this.serverPrepare(((PreparedQuery)this.query).getOriginalSql());
            } catch (Exception var48) {
               this.invalidationException = ExceptionFactory.createException(var48.getMessage(), var48);
            }

            if (this.invalidationException != null) {
               this.invalid = true;
               this.query.closeQuery();
               if (this.results != null) {
                  try {
                     this.results.close();
                  } catch (Exception var47) {
                  }
               }

               if (this.generatedKeysResults != null) {
                  try {
                     this.generatedKeysResults.close();
                  } catch (Exception var46) {
                  }
               }

               try {
                  this.closeAllOpenResults();
               } catch (Exception var45) {
               }

               if (this.connection != null && !this.dontTrackOpenResources.getValue()) {
                  this.connection.unregisterStatement(this);
               }
            }
         } catch (Throwable var51) {
            span.setError(var51);
            throw var51;
         } finally {
            span.end();
         }
      } finally {
         connectionLock.unlock();
      }
   }

   protected ResultSetInternalMethods serverExecute(int maxRowsToRetrieve, boolean createStreamingResultSet, ColumnDefinition metadata) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         ResultSetInternalMethods var5;
         try {
            this.results = ((ServerPreparedQuery)this.query).serverExecute(maxRowsToRetrieve, createStreamingResultSet, metadata, this.resultSetFactory);
            var5 = this.results;
         } finally {
            connectionLock.unlock();
         }

         return var5;
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   protected void serverPrepare(String sql) throws SQLException {
      try {
         Lock connectionLock = this.checkClosed().getConnectionLock();
         connectionLock.lock();

         try {
            SQLException t = null;

            try {
               ServerPreparedQuery q = (ServerPreparedQuery)this.query;
               q.serverPrepare(sql);
            } catch (IOException var30) {
               t = SQLError.createCommunicationsException(
                  this.connection,
                  this.session.getProtocol().getPacketSentTimeHolder(),
                  this.session.getProtocol().getPacketReceivedTimeHolder(),
                  var30,
                  this.exceptionInterceptor
               );
            } catch (CJException var31) {
               SQLException ex = SQLExceptionsMapping.translateException(var31, this.exceptionInterceptor);
               if (this.dumpQueriesOnException.getValue()) {
                  StringBuilder messageBuf = new StringBuilder(((PreparedQuery)this.query).getOriginalSql().length() + 32);
                  messageBuf.append("\n\nQuery being prepared when exception was thrown:\n\n");
                  messageBuf.append(((PreparedQuery)this.query).getOriginalSql());
                  ex = appendMessageToException(ex, messageBuf.toString(), this.exceptionInterceptor);
               }

               t = ex;
            } finally {
               try {
                  this.session.clearInputStream();
               } catch (Exception var29) {
                  if (t == null) {
                     t = SQLError.createCommunicationsException(
                        this.connection,
                        this.session.getProtocol().getPacketSentTimeHolder(),
                        this.session.getProtocol().getPacketReceivedTimeHolder(),
                        var29,
                        this.exceptionInterceptor
                     );
                  }
               }

               if (t != null) {
                  throw t;
               }
            }
         } finally {
            connectionLock.unlock();
         }
      } catch (CJException var34) {
         throw SQLExceptionsMapping.translateException(var34, this.getExceptionInterceptor());
      }
   }

   @Override
   protected void checkBounds(int parameterIndex, int parameterIndexOffset) throws SQLException {
      int paramCount = ((PreparedQuery)this.query).getParameterCount();
      if (paramCount == 0) {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class, Messages.getString("ServerPreparedStatement.8"), this.session.getExceptionInterceptor()
         );
      } else if (parameterIndex < 0 || parameterIndex > paramCount) {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class,
            Messages.getString("ServerPreparedStatement.9") + (parameterIndex + 1) + Messages.getString("ServerPreparedStatement.10") + paramCount,
            this.session.getExceptionInterceptor()
         );
      }
   }

   @Deprecated
   @Override
   public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
      try {
         this.checkClosed();
         throw SQLError.createSQLFeatureNotSupportedException();
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setURL(int parameterIndex, URL x) throws SQLException {
      try {
         this.checkClosed();
         this.setString(parameterIndex, x.toString());
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public long getServerStatementId() {
      return ((ServerPreparedQuery)this.query).getServerStatementId();
   }

   @Override
   protected boolean containsOnDuplicateKeyUpdate() {
      return this.hasOnDuplicateKeyUpdate;
   }

   @Override
   protected ClientPreparedStatement prepareBatchedInsertSQL(JdbcConnection localConn, int numBatches) throws SQLException {
      Lock connectionLock = this.checkClosed().getConnectionLock();
      connectionLock.lock();

      ClientPreparedStatement var11;
      try {
         ClientPreparedStatement pstmt = localConn.prepareStatement(
               ((PreparedQuery)this.query).getQueryInfo().getSqlForBatch(numBatches), this.resultSetConcurrency, this.query.getResultType().getIntValue()
            )
            .unwrap(ClientPreparedStatement.class);
         pstmt.setRetrieveGeneratedKeys(this.retrieveGeneratedKeys);
         this.getQueryAttributesBindings().runThroughAll(a -> pstmt.setAttribute(a.getName(), a.getValue()));
         var11 = pstmt;
      } catch (CJException var9) {
         SQLException sqlEx = SQLError.createSQLException(Messages.getString("ServerPreparedStatement.27"), "S1000", this.exceptionInterceptor);
         sqlEx.initCause(var9);
         throw sqlEx;
      } finally {
         connectionLock.unlock();
      }

      return var11;
   }

   @Override
   public void setPoolable(boolean poolable) throws SQLException {
      try {
         super.setPoolable(poolable);
         if (!poolable && this.isCached) {
            this.connection.decachePreparedStatement(this);
            this.isCached = false;
            if (this.isClosed) {
               this.isClosed = false;
               this.doClose(CloseOption.PROPAGATE, CloseOption.NO_CACHE);
            }
         }
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }
}
