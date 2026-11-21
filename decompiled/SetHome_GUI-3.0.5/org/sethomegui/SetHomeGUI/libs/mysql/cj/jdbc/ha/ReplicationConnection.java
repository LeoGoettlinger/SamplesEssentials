package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.ha;

import java.sql.SQLException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.JdbcConnection;

public interface ReplicationConnection extends JdbcConnection {
   long getConnectionGroupId();

   JdbcConnection getCurrentConnection();

   JdbcConnection getSourceConnection();

   void promoteReplicaToSource(String var1) throws SQLException;

   void removeSourceHost(String var1) throws SQLException;

   void removeSourceHost(String var1, boolean var2) throws SQLException;

   boolean isHostSource(String var1);

   JdbcConnection getReplicaConnection();

   void addReplicaHost(String var1) throws SQLException;

   void removeReplica(String var1) throws SQLException;

   void removeReplica(String var1, boolean var2) throws SQLException;

   boolean isHostReplica(String var1);
}
