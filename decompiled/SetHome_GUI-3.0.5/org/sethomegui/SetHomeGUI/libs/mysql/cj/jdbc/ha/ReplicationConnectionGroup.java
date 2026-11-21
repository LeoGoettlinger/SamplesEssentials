package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.ha;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ReplicationConnectionGroup {
   private String groupName;
   private long connections = 0L;
   private long replicasAdded = 0L;
   private long replicasRemoved = 0L;
   private long replicasPromoted = 0L;
   private long activeConnections = 0L;
   private HashMap<Long, ReplicationConnection> replicationConnections = new HashMap<>();
   private Set<String> replicaHostList = new CopyOnWriteArraySet<>();
   private boolean isInitialized = false;
   private Set<String> sourceHostList = new CopyOnWriteArraySet<>();

   ReplicationConnectionGroup(String groupName) {
      this.groupName = groupName;
   }

   public long getConnectionCount() {
      return this.connections;
   }

   public long registerReplicationConnection(ReplicationConnection conn, List<String> localSourceList, List<String> localReplicaList) {
      long currentConnectionId;
      synchronized (this) {
         if (!this.isInitialized) {
            if (localSourceList != null) {
               this.sourceHostList.addAll(localSourceList);
            }

            if (localReplicaList != null) {
               this.replicaHostList.addAll(localReplicaList);
            }

            this.isInitialized = true;
         }

         currentConnectionId = ++this.connections;
         this.replicationConnections.put(currentConnectionId, conn);
      }

      this.activeConnections++;
      return currentConnectionId;
   }

   public String getGroupName() {
      return this.groupName;
   }

   public Collection<String> getSourceHosts() {
      return this.sourceHostList;
   }

   public Collection<String> getReplicaHosts() {
      return this.replicaHostList;
   }

   public void addReplicaHost(String hostPortPair) throws SQLException {
      if (this.replicaHostList.add(hostPortPair)) {
         this.replicasAdded++;

         for (ReplicationConnection c : this.replicationConnections.values()) {
            c.addReplicaHost(hostPortPair);
         }
      }
   }

   public void handleCloseConnection(ReplicationConnection conn) {
      this.replicationConnections.remove(conn.getConnectionGroupId());
      this.activeConnections--;
   }

   public void removeReplicaHost(String hostPortPair, boolean closeGently) throws SQLException {
      if (this.replicaHostList.remove(hostPortPair)) {
         this.replicasRemoved++;

         for (ReplicationConnection c : this.replicationConnections.values()) {
            c.removeReplica(hostPortPair, closeGently);
         }
      }
   }

   public void promoteReplicaToSource(String hostPortPair) throws SQLException {
      if (this.replicaHostList.remove(hostPortPair) | this.sourceHostList.add(hostPortPair)) {
         this.replicasPromoted++;

         for (ReplicationConnection c : this.replicationConnections.values()) {
            c.promoteReplicaToSource(hostPortPair);
         }
      }
   }

   public void removeSourceHost(String hostPortPair) throws SQLException {
      this.removeSourceHost(hostPortPair, true);
   }

   public void removeSourceHost(String hostPortPair, boolean closeGently) throws SQLException {
      if (this.sourceHostList.remove(hostPortPair)) {
         for (ReplicationConnection c : this.replicationConnections.values()) {
            c.removeSourceHost(hostPortPair, closeGently);
         }
      }
   }

   public int getConnectionCountWithHostAsReplica(String hostPortPair) {
      int matched = 0;

      for (ReplicationConnection c : this.replicationConnections.values()) {
         if (c.isHostReplica(hostPortPair)) {
            matched++;
         }
      }

      return matched;
   }

   public int getConnectionCountWithHostAsSource(String hostPortPair) {
      int matched = 0;

      for (ReplicationConnection c : this.replicationConnections.values()) {
         if (c.isHostSource(hostPortPair)) {
            matched++;
         }
      }

      return matched;
   }

   public long getNumberOfReplicasAdded() {
      return this.replicasAdded;
   }

   public long getNumberOfReplicasRemoved() {
      return this.replicasRemoved;
   }

   public long getNumberOfReplicaPromotions() {
      return this.replicasPromoted;
   }

   public long getTotalConnectionCount() {
      return this.connections;
   }

   public long getActiveConnectionCount() {
      return this.activeConnections;
   }

   @Override
   public String toString() {
      return "ReplicationConnectionGroup[groupName="
         + this.groupName
         + ",sourceHostList="
         + this.sourceHostList
         + ",replicaHostList="
         + this.replicaHostList
         + "]";
   }
}
