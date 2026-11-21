package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;

public class MysqlXADataSource extends MysqlDataSource implements XADataSource {
   static final long serialVersionUID = 7911390333152247455L;

   @Override
   public XAConnection getXAConnection() throws SQLException {
      try {
         Connection conn = this.getConnection();
         return this.wrapConnection(conn);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3);
      }
   }

   @Override
   public XAConnection getXAConnection(String u, String p) throws SQLException {
      try {
         Connection conn = this.getConnection(u, p);
         return this.wrapConnection(conn);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5);
      }
   }

   private XAConnection wrapConnection(Connection conn) throws SQLException {
      return (XAConnection)(!this.getBooleanProperty(PropertyKey.pinGlobalTxToPhysicalConnection).getValue()
            && !((JdbcConnection)conn).getPropertySet().getBooleanProperty(PropertyKey.pinGlobalTxToPhysicalConnection).getValue()
         ? MysqlXAConnection.getInstance((JdbcConnection)conn, this.getBooleanProperty(PropertyKey.logXaCommands).getValue())
         : SuspendableXAConnection.getInstance((JdbcConnection)conn));
   }
}
