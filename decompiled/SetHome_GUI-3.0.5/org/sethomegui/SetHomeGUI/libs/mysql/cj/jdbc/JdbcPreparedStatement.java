package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.MysqlType;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.QueryBindings;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.QueryInfo;

public interface JdbcPreparedStatement extends PreparedStatement, JdbcStatement {
   QueryBindings getQueryBindings();

   byte[] getBytesRepresentation(int var1) throws SQLException;

   QueryInfo getQueryInfo();

   boolean isNull(int var1) throws SQLException;

   String getPreparedSql();

   void setBytes(int var1, byte[] var2, boolean var3) throws SQLException;

   void setBigInteger(int var1, BigInteger var2) throws SQLException;

   void setNull(int var1, MysqlType var2) throws SQLException;

   ParameterBindings getParameterBindings() throws SQLException;
}
