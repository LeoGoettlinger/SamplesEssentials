package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc;

import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.stream.Collectors;

public class CommentClientInfoProvider implements ClientInfoProvider {
   private Properties clientInfo;

   @Override
   public synchronized void initialize(Connection conn, Properties configurationProps) throws SQLException {
      this.clientInfo = new Properties();
   }

   @Override
   public synchronized void destroy() throws SQLException {
      this.clientInfo = null;
   }

   @Override
   public synchronized Properties getClientInfo(Connection conn) throws SQLException {
      Properties clientInfoOut = new Properties();
      clientInfoOut.putAll(this.clientInfo);
      return clientInfoOut;
   }

   @Override
   public synchronized String getClientInfo(Connection conn, String name) throws SQLException {
      return this.clientInfo.getProperty(name);
   }

   @Override
   public synchronized void setClientInfo(Connection conn, Properties properties) throws SQLClientInfoException {
      this.clientInfo = new Properties();
      if (properties != null) {
         this.clientInfo.putAll(properties);
      }

      this.setComment(conn);
   }

   @Override
   public synchronized void setClientInfo(Connection conn, String name, String value) throws SQLClientInfoException {
      if (value == null) {
         this.clientInfo.remove(name);
      } else {
         this.clientInfo.setProperty(name, value);
      }

      this.setComment(conn);
   }

   private synchronized void setComment(Connection conn) throws SQLClientInfoException {
      String clientInfoComment = this.clientInfo.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(", "));

      try {
         conn.unwrap(JdbcConnection.class).setStatementComment(clientInfoComment);
      } catch (SQLException var5) {
         SQLClientInfoException clientInfoEx = new SQLClientInfoException();
         clientInfoEx.initCause(var5);
      }
   }
}
