package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.ha;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.jmx.ReplicationGroupManager;

public class ReplicationConnectionGroupManager {
   private static HashMap<String, ReplicationConnectionGroup> GROUP_MAP = new HashMap<>();
   private static ReplicationGroupManager mbean = new ReplicationGroupManager();
   private static boolean hasRegisteredJmx = false;

   public static synchronized ReplicationConnectionGroup getConnectionGroupInstance(String groupName) {
      if (GROUP_MAP.containsKey(groupName)) {
         return GROUP_MAP.get(groupName);
      } else {
         ReplicationConnectionGroup group = new ReplicationConnectionGroup(groupName);
         GROUP_MAP.put(groupName, group);
         return group;
      }
   }

   public static void registerJmx() throws SQLException {
      if (!hasRegisteredJmx) {
         mbean.registerJmx();
         hasRegisteredJmx = true;
      }
   }

   public static ReplicationConnectionGroup getConnectionGroup(String groupName) {
      return GROUP_MAP.get(groupName);
   }

   public static Collection<ReplicationConnectionGroup> getGroupsMatching(String group) {
      if (group != null && !group.equals("")) {
         Set<ReplicationConnectionGroup> s = new HashSet<>();
         ReplicationConnectionGroup o = GROUP_MAP.get(group);
         if (o != null) {
            s.add(o);
         }

         return s;
      } else {
         Set<ReplicationConnectionGroup> s = new HashSet<>();
         s.addAll(GROUP_MAP.values());
         return s;
      }
   }

   public static void addReplicaHost(String group, String hostPortPair) throws SQLException {
      for (ReplicationConnectionGroup cg : getGroupsMatching(group)) {
         cg.addReplicaHost(hostPortPair);
      }
   }

   public static void removeReplicaHost(String group, String hostPortPair) throws SQLException {
      removeReplicaHost(group, hostPortPair, true);
   }

   public static void removeReplicaHost(String group, String hostPortPair, boolean closeGently) throws SQLException {
      for (ReplicationConnectionGroup cg : getGroupsMatching(group)) {
         cg.removeReplicaHost(hostPortPair, closeGently);
      }
   }

   public static void promoteReplicaToSource(String group, String hostPortPair) throws SQLException {
      for (ReplicationConnectionGroup cg : getGroupsMatching(group)) {
         cg.promoteReplicaToSource(hostPortPair);
      }
   }

   public static long getReplicaPromotionCount(String group) throws SQLException {
      Collection<ReplicationConnectionGroup> s = getGroupsMatching(group);
      long promoted = 0L;

      for (ReplicationConnectionGroup cg : s) {
         long tmp = cg.getNumberOfReplicaPromotions();
         if (tmp > promoted) {
            promoted = tmp;
         }
      }

      return promoted;
   }

   public static void removeSourceHost(String group, String hostPortPair) throws SQLException {
      removeSourceHost(group, hostPortPair, true);
   }

   public static void removeSourceHost(String group, String hostPortPair, boolean closeGently) throws SQLException {
      for (ReplicationConnectionGroup cg : getGroupsMatching(group)) {
         cg.removeSourceHost(hostPortPair, closeGently);
      }
   }

   public static String getRegisteredReplicationConnectionGroups() {
      Collection<ReplicationConnectionGroup> s = getGroupsMatching(null);
      StringBuilder sb = new StringBuilder();
      String sep = "";

      for (ReplicationConnectionGroup cg : s) {
         String group = cg.getGroupName();
         sb.append(sep);
         sb.append(group);
         sep = ",";
      }

      return sb.toString();
   }

   public static int getNumberOfSourcePromotion(String groupFilter) {
      int total = 0;

      for (ReplicationConnectionGroup cg : getGroupsMatching(groupFilter)) {
         total = (int)(total + cg.getNumberOfReplicaPromotions());
      }

      return total;
   }

   public static int getConnectionCountWithHostAsReplica(String groupFilter, String hostPortPair) {
      int total = 0;

      for (ReplicationConnectionGroup cg : getGroupsMatching(groupFilter)) {
         total += cg.getConnectionCountWithHostAsReplica(hostPortPair);
      }

      return total;
   }

   public static int getConnectionCountWithHostAsSource(String groupFilter, String hostPortPair) {
      int total = 0;

      for (ReplicationConnectionGroup cg : getGroupsMatching(groupFilter)) {
         total += cg.getConnectionCountWithHostAsSource(hostPortPair);
      }

      return total;
   }

   public static Collection<String> getReplicaHosts(String groupFilter) {
      Collection<ReplicationConnectionGroup> s = getGroupsMatching(groupFilter);
      Collection<String> hosts = new ArrayList<>();

      for (ReplicationConnectionGroup cg : s) {
         hosts.addAll(cg.getReplicaHosts());
      }

      return hosts;
   }

   public static Collection<String> getSourceHosts(String groupFilter) {
      Collection<ReplicationConnectionGroup> s = getGroupsMatching(groupFilter);
      Collection<String> hosts = new ArrayList<>();

      for (ReplicationConnectionGroup cg : s) {
         hosts.addAll(cg.getSourceHosts());
      }

      return hosts;
   }

   public static long getTotalConnectionCount(String group) {
      long connections = 0L;

      for (ReplicationConnectionGroup cg : getGroupsMatching(group)) {
         connections += cg.getTotalConnectionCount();
      }

      return connections;
   }

   public static long getActiveConnectionCount(String group) {
      long connections = 0L;

      for (ReplicationConnectionGroup cg : getGroupsMatching(group)) {
         connections += cg.getActiveConnectionCount();
      }

      return connections;
   }
}
