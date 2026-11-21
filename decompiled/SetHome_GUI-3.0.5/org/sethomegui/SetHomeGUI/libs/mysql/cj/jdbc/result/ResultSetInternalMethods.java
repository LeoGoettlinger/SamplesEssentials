package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.CloseOption;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.JdbcPreparedStatement;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.JdbcStatement;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Resultset;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ResultsetRowsOwner;

public interface ResultSetInternalMethods extends ResultSet, ResultsetRowsOwner, Resultset {
   Object getObjectStoredProc(int var1, int var2) throws SQLException;

   Object getObjectStoredProc(int var1, Map<Object, Object> var2, int var3) throws SQLException;

   Object getObjectStoredProc(String var1, int var2) throws SQLException;

   Object getObjectStoredProc(String var1, Map<Object, Object> var2, int var3) throws SQLException;

   void doClose(CloseOption... var1) throws SQLException;

   void setFirstCharOfQuery(char var1);

   void setOwningStatement(JdbcStatement var1);

   char getFirstCharOfQuery();

   void setStatementUsedForFetchingRows(JdbcPreparedStatement var1);

   void setWrapperStatement(Statement var1);

   void initializeWithMetadata() throws SQLException;

   void populateCachedMetaData(CachedResultSetMetaData var1) throws SQLException;

   BigInteger getBigInteger(int var1) throws SQLException;
}
