package com.mysql.cj.jdbc.jmx;

import java.sql.SQLException;

public interface ReplicationGroupManagerMBean {
   void addReplicaHost(String var1, String var2) throws SQLException;

   void removeReplicaHost(String var1, String var2) throws SQLException;

   void promoteReplicaToSource(String var1, String var2) throws SQLException;

   void removeSourceHost(String var1, String var2) throws SQLException;

   String getSourceHostsList(String var1);

   String getReplicaHostsList(String var1);

   String getRegisteredConnectionGroups();

   int getActiveSourceHostCount(String var1);

   int getActiveReplicaHostCount(String var1);

   int getReplicaPromotionCount(String var1);

   long getTotalLogicalConnectionCount(String var1);

   long getActiveLogicalConnectionCount(String var1);
}
