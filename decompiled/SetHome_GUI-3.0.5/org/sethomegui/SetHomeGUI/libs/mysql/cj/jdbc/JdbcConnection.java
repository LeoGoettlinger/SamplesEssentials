package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.locks.Lock;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.MysqlConnection;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.ServerVersion;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.TransactionEventHandler;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.interceptors.QueryInterceptor;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.CachedResultSetMetaData;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result.ResultSetInternalMethods;

public interface JdbcConnection extends Connection, MysqlConnection, TransactionEventHandler {
   Lock getLock();

   JdbcPropertySet getPropertySet();

   void changeUser(String var1, String var2) throws SQLException;

   PreparedStatement clientPrepareStatement(String var1) throws SQLException;

   PreparedStatement clientPrepareStatement(String var1, int var2) throws SQLException;

   PreparedStatement clientPrepareStatement(String var1, int var2, int var3) throws SQLException;

   PreparedStatement clientPrepareStatement(String var1, int[] var2) throws SQLException;

   PreparedStatement clientPrepareStatement(String var1, int var2, int var3, int var4) throws SQLException;

   PreparedStatement clientPrepareStatement(String var1, String[] var2) throws SQLException;

   int getActiveStatementCount();

   long getIdleFor();

   String getStatementComment();

   boolean isInGlobalTx();

   void setInGlobalTx(boolean var1);

   boolean isSourceConnection();

   boolean isSameResource(JdbcConnection var1);

   boolean lowerCaseTableNames();

   void ping() throws SQLException;

   void resetServerState() throws SQLException;

   PreparedStatement serverPrepareStatement(String var1) throws SQLException;

   PreparedStatement serverPrepareStatement(String var1, int var2) throws SQLException;

   PreparedStatement serverPrepareStatement(String var1, int var2, int var3) throws SQLException;

   PreparedStatement serverPrepareStatement(String var1, int var2, int var3, int var4) throws SQLException;

   PreparedStatement serverPrepareStatement(String var1, int[] var2) throws SQLException;

   PreparedStatement serverPrepareStatement(String var1, String[] var2) throws SQLException;

   void setFailedOver(boolean var1);

   void setStatementComment(String var1);

   void shutdownServer() throws SQLException;

   int getAutoIncrementIncrement();

   boolean hasSameProperties(JdbcConnection var1);

   String getHost();

   String getHostPortPair();

   void setProxy(JdbcConnection var1);

   boolean isServerLocal() throws SQLException;

   int getSessionMaxRows();

   void setSessionMaxRows(int var1) throws SQLException;

   void abortInternal() throws SQLException;

   boolean isProxySet();

   CachedResultSetMetaData getCachedMetaData(String var1);

   String getCharacterSetMetadata();

   Statement getMetadataSafeStatement() throws SQLException;

   ServerVersion getServerVersion();

   List<QueryInterceptor> getQueryInterceptorsInstances();

   void initializeResultsMetadataFromCache(String var1, CachedResultSetMetaData var2, ResultSetInternalMethods var3) throws SQLException;

   void initializeSafeQueryInterceptors() throws SQLException;

   boolean isReadOnly(boolean var1) throws SQLException;

   void pingInternal(boolean var1, int var2) throws SQLException;

   void doClose(Throwable var1, CloseOption... var2) throws SQLException;

   void recachePreparedStatement(JdbcPreparedStatement var1) throws SQLException;

   void decachePreparedStatement(JdbcPreparedStatement var1) throws SQLException;

   void registerStatement(JdbcStatement var1);

   void setReadOnlyInternal(boolean var1) throws SQLException;

   boolean storesLowerCaseTableName();

   void throwConnectionClosedException() throws SQLException;

   void unregisterStatement(JdbcStatement var1);

   void unSafeQueryInterceptors() throws SQLException;

   JdbcConnection getMultiHostSafeProxy();

   JdbcConnection getMultiHostParentProxy();

   JdbcConnection getActiveMySQLConnection();

   ClientInfoProvider getClientInfoProviderImpl() throws SQLException;

   void setDatabase(String var1) throws SQLException;

   String getDatabase() throws SQLException;
}
