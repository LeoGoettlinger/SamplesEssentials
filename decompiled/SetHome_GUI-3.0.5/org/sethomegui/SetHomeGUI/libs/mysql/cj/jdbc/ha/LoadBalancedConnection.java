package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.ha;

import java.sql.SQLException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.JdbcConnection;

public interface LoadBalancedConnection extends JdbcConnection {
   boolean addHost(String var1) throws SQLException;

   void removeHost(String var1) throws SQLException;

   void removeHostWhenNotInUse(String var1) throws SQLException;

   void ping(boolean var1) throws SQLException;
}
