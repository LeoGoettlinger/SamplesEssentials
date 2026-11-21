package com.mysql.cj.jdbc.ha;

import com.mysql.cj.jdbc.JdbcConnection;
import java.sql.SQLException;

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
