package org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.admin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.ConnectionImpl;
import org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.Driver;
import org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.JdbcConnection;
import org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.exceptions.SQLError;

public class MiniAdmin {
   private JdbcConnection conn;

   public MiniAdmin(Connection conn) throws SQLException {
      if (conn == null) {
         throw SQLError.createSQLException(Messages.getString("MiniAdmin.0"), "S1000", null);
      } else if (!(conn instanceof JdbcConnection)) {
         throw SQLError.createSQLException(Messages.getString("MiniAdmin.1"), "S1000", ((ConnectionImpl)conn).getExceptionInterceptor());
      } else {
         this.conn = (JdbcConnection)conn;
      }
   }

   public MiniAdmin(String jdbcUrl) throws SQLException {
      this(jdbcUrl, new Properties());
   }

   public MiniAdmin(String jdbcUrl, Properties props) throws SQLException {
      this.conn = (JdbcConnection)new Driver().connect(jdbcUrl, props);
   }

   public void shutdown() throws SQLException {
      this.conn.shutdownServer();
   }
}
