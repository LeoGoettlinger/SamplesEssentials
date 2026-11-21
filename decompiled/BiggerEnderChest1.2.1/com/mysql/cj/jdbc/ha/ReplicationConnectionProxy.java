package com.mysql.cj.jdbc.ha;

import com.mysql.cj.Messages;
import com.mysql.cj.PingTarget;
import com.mysql.cj.conf.ConnectionUrl;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.conf.HostsListView;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.conf.url.LoadBalanceConnectionUrl;
import com.mysql.cj.conf.url.ReplicationConnectionUrl;
import com.mysql.cj.jdbc.JdbcConnection;
import com.mysql.cj.jdbc.JdbcStatement;
import com.mysql.cj.jdbc.exceptions.SQLError;
import com.mysql.cj.util.StringUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ReplicationConnectionProxy extends MultiHostConnectionProxy implements PingTarget {
   private ReplicationConnection thisAsReplicationConnection;
   protected boolean enableJMX = false;
   protected boolean allowSourceDownConnections = false;
   protected boolean allowReplicaDownConnections = false;
   protected boolean readFromSourceWhenNoReplicas = false;
   protected boolean readFromSourceWhenNoReplicasOriginal = false;
   protected boolean readOnly = false;
   ReplicationConnectionGroup connectionGroup;
   private long connectionGroupID = -1L;
   private List<HostInfo> sourceHosts;
   protected LoadBalancedConnection sourceConnection;
   private List<HostInfo> replicaHosts;
   protected LoadBalancedConnection replicasConnection;

   public static ReplicationConnection createProxyInstance(ConnectionUrl connectionUrl) throws SQLException {
      ReplicationConnectionProxy connProxy = new ReplicationConnectionProxy(connectionUrl);
      return (ReplicationConnection)Proxy.newProxyInstance(
         ReplicationConnection.class.getClassLoader(), new Class[]{ReplicationConnection.class, JdbcConnection.class}, connProxy
      );
   }

   private ReplicationConnectionProxy(ConnectionUrl connectionUrl) throws SQLException {
      Properties props = connectionUrl.getConnectionArgumentsAsProperties();
      this.thisAsReplicationConnection = (ReplicationConnection)this.thisAsConnection;
      this.connectionUrl = connectionUrl;
      String enableJMXAsString = props.getProperty(PropertyKey.ha_enableJMX.getKeyName(), "false");

      try {
         this.enableJMX = Boolean.parseBoolean(enableJMXAsString);
      } catch (Exception var14) {
         throw SQLError.createSQLException(Messages.getString("MultihostConnection.badValueForHaEnableJMX", new Object[]{enableJMXAsString}), "S1009", null);
      }

      String allowSourceDownConnectionsAsString = props.getProperty(PropertyKey.allowSourceDownConnections.getKeyName(), "false");

      try {
         this.allowSourceDownConnections = Boolean.parseBoolean(allowSourceDownConnectionsAsString);
      } catch (Exception var13) {
         throw SQLError.createSQLException(
            Messages.getString("ReplicationConnectionProxy.badValueForAllowSourceDownConnections", new Object[]{enableJMXAsString}), "S1009", null
         );
      }

      String allowReplicaDownConnectionsAsString = props.getProperty(PropertyKey.allowReplicaDownConnections.getKeyName(), "false");

      try {
         this.allowReplicaDownConnections = Boolean.parseBoolean(allowReplicaDownConnectionsAsString);
      } catch (Exception var12) {
         throw SQLError.createSQLException(
            Messages.getString("ReplicationConnectionProxy.badValueForAllowReplicaDownConnections", new Object[]{allowReplicaDownConnectionsAsString}),
            "S1009",
            null
         );
      }

      String readFromSourceWhenNoReplicasAsString = props.getProperty(PropertyKey.readFromSourceWhenNoReplicas.getKeyName());

      try {
         this.readFromSourceWhenNoReplicasOriginal = Boolean.parseBoolean(readFromSourceWhenNoReplicasAsString);
      } catch (Exception var11) {
         throw SQLError.createSQLException(
            Messages.getString("ReplicationConnectionProxy.badValueForReadFromSourceWhenNoReplicas", new Object[]{readFromSourceWhenNoReplicasAsString}),
            "S1009",
            null
         );
      }

      String group = props.getProperty(PropertyKey.replicationConnectionGroup.getKeyName(), null);
      if (!StringUtils.isNullOrEmpty(group) && ReplicationConnectionUrl.class.isAssignableFrom(connectionUrl.getClass())) {
         this.connectionGroup = ReplicationConnectionGroupManager.getConnectionGroupInstance(group);
         if (this.enableJMX) {
            ReplicationConnectionGroupManager.registerJmx();
         }

         this.connectionGroupID = this.connectionGroup
            .registerReplicationConnection(
               this.thisAsReplicationConnection,
               ((ReplicationConnectionUrl)connectionUrl).getSourcesListAsHostPortPairs(),
               ((ReplicationConnectionUrl)connectionUrl).getReplicasListAsHostPortPairs()
            );
         this.sourceHosts = ((ReplicationConnectionUrl)connectionUrl).getSourceHostsListFromHostPortPairs(this.connectionGroup.getSourceHosts());
         this.replicaHosts = ((ReplicationConnectionUrl)connectionUrl).getReplicaHostsListFromHostPortPairs(this.connectionGroup.getReplicaHosts());
      } else {
         this.sourceHosts = new ArrayList<>(connectionUrl.getHostsList(HostsListView.SOURCES));
         this.replicaHosts = new ArrayList<>(connectionUrl.getHostsList(HostsListView.REPLICAS));
      }

      this.resetReadFromSourceWhenNoReplicas();

      try {
         this.initializeReplicasConnection();
      } catch (SQLException var15) {
         if (!this.allowReplicaDownConnections) {
            if (this.connectionGroup != null) {
               this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
            }

            throw var15;
         }
      }

      SQLException exCaught = null;

      try {
         this.currentConnection = this.initializeSourceConnection();
      } catch (SQLException var10) {
         exCaught = var10;
      }

      if (this.currentConnection == null) {
         if (!this.allowSourceDownConnections || this.replicasConnection == null) {
            if (this.connectionGroup != null) {
               this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
            }

            if (exCaught != null) {
               throw exCaught;
            }

            throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.initializationWithEmptyHostsLists"), "S1009", null);
         }

         this.readOnly = true;
         this.currentConnection = this.replicasConnection;
      }
   }

   @Override
   JdbcConnection getNewWrapperForThisAsConnection() throws SQLException {
      return new ReplicationMySQLConnection(this);
   }

   @Override
   protected void propagateProxyDown(JdbcConnection proxyConn) {
      if (this.sourceConnection != null) {
         this.sourceConnection.setProxy(proxyConn);
      }

      if (this.replicasConnection != null) {
         this.replicasConnection.setProxy(proxyConn);
      }
   }

   @Override
   boolean shouldExceptionTriggerConnectionSwitch(Throwable t) {
      return false;
   }

   @Override
   public boolean isSourceConnection() {
      return this.currentConnection != null && this.currentConnection == this.sourceConnection;
   }

   public boolean isReplicasConnection() {
      return this.currentConnection != null && this.currentConnection == this.replicasConnection;
   }

   @Override
   void pickNewConnection() throws SQLException {
   }

   @Override
   void syncSessionState(JdbcConnection source, JdbcConnection target, boolean readonly) throws SQLException {
      try {
         super.syncSessionState(source, target, readonly);
      } catch (SQLException var7) {
         try {
            super.syncSessionState(source, target, readonly);
         } catch (SQLException var6) {
         }
      }
   }

   @Override
   void doClose() throws SQLException {
      if (this.sourceConnection != null) {
         this.sourceConnection.close();
      }

      if (this.replicasConnection != null) {
         this.replicasConnection.close();
      }

      if (this.connectionGroup != null) {
         this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
      }
   }

   @Override
   void doAbortInternal() throws SQLException {
      this.sourceConnection.abortInternal();
      this.replicasConnection.abortInternal();
      if (this.connectionGroup != null) {
         this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
      }
   }

   @Override
   void doAbort(Executor executor) throws SQLException {
      this.sourceConnection.abort(executor);
      this.replicasConnection.abort(executor);
      if (this.connectionGroup != null) {
         this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
      }
   }

   @Override
   Object invokeMore(Object proxy, Method method, Object[] args) throws Throwable {
      this.checkConnectionCapabilityForMethod(method);
      boolean invokeAgain = false;

      while (true) {
         try {
            Object result = method.invoke(this.thisAsConnection, args);
            if (result != null && result instanceof JdbcStatement) {
               ((JdbcStatement)result).setPingTarget(this);
            }

            return result;
         } catch (InvocationTargetException var8) {
            if (invokeAgain) {
               invokeAgain = false;
            } else if (var8.getCause() != null
               && var8.getCause() instanceof SQLException
               && ((SQLException)var8.getCause()).getSQLState() == "25000"
               && ((SQLException)var8.getCause()).getErrorCode() == 1000001) {
               try {
                  this.setReadOnly(this.readOnly);
                  invokeAgain = true;
               } catch (SQLException var7) {
               }
            }

            if (!invokeAgain) {
               throw var8;
            }
         }
      }
   }

   private void checkConnectionCapabilityForMethod(Method method) throws Throwable {
      if (this.sourceHosts.isEmpty() && this.replicaHosts.isEmpty() && !ReplicationConnection.class.isAssignableFrom(method.getDeclaringClass())) {
         throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.noHostsInconsistentState"), "25000", 1000002, true, null);
      }
   }

   @Override
   public void doPing() throws SQLException {
      boolean isSourceConn = this.isSourceConnection();
      SQLException sourcesPingException = null;
      SQLException replicasPingException = null;
      if (this.sourceConnection != null) {
         try {
            this.sourceConnection.ping();
         } catch (SQLException var6) {
            sourcesPingException = var6;
         }
      } else {
         this.initializeSourceConnection();
      }

      if (this.replicasConnection != null) {
         try {
            this.replicasConnection.ping();
         } catch (SQLException var5) {
            replicasPingException = var5;
         }
      } else {
         try {
            this.initializeReplicasConnection();
            if (this.switchToReplicasConnectionIfNecessary()) {
               isSourceConn = false;
            }
         } catch (SQLException var7) {
            if (this.sourceConnection == null || !this.readFromSourceWhenNoReplicas) {
               throw var7;
            }
         }
      }

      if (isSourceConn && sourcesPingException != null) {
         if (this.replicasConnection != null && replicasPingException == null) {
            this.sourceConnection = null;
            this.currentConnection = this.replicasConnection;
            this.readOnly = true;
         }

         throw sourcesPingException;
      } else {
         if (!isSourceConn && (replicasPingException != null || this.replicasConnection == null)) {
            if (this.sourceConnection != null && this.readFromSourceWhenNoReplicas && sourcesPingException == null) {
               this.replicasConnection = null;
               this.currentConnection = this.sourceConnection;
               this.readOnly = true;
               this.currentConnection.setReadOnly(true);
            }

            if (replicasPingException != null) {
               throw replicasPingException;
            }
         }
      }
   }

   private JdbcConnection initializeSourceConnection() throws SQLException {
      this.sourceConnection = null;
      if (this.sourceHosts.size() == 0) {
         return null;
      } else {
         LoadBalancedConnection newSourceConn = LoadBalancedConnectionProxy.createProxyInstance(
            new LoadBalanceConnectionUrl(this.sourceHosts, this.connectionUrl.getOriginalProperties())
         );
         newSourceConn.setProxy(this.getProxy());
         this.sourceConnection = newSourceConn;
         return this.sourceConnection;
      }
   }

   private JdbcConnection initializeReplicasConnection() throws SQLException {
      this.replicasConnection = null;
      if (this.replicaHosts.size() == 0) {
         return null;
      } else {
         LoadBalancedConnection newReplicasConn = LoadBalancedConnectionProxy.createProxyInstance(
            new LoadBalanceConnectionUrl(this.replicaHosts, this.connectionUrl.getOriginalProperties())
         );
         newReplicasConn.setProxy(this.getProxy());
         newReplicasConn.setReadOnly(true);
         this.replicasConnection = newReplicasConn;
         return this.replicasConnection;
      }
   }

   private boolean switchToSourceConnection() throws SQLException {
      this.getLock().lock();

      try {
         if (this.sourceConnection == null || this.sourceConnection.isClosed()) {
            try {
               if (this.initializeSourceConnection() == null) {
                  return false;
               }
            } catch (SQLException var5) {
               this.currentConnection = null;
               throw var5;
            }
         }

         if (!this.isSourceConnection() && this.sourceConnection != null) {
            this.syncSessionState(this.currentConnection, this.sourceConnection, false);
            this.currentConnection = this.sourceConnection;
         }

         return true;
      } finally {
         this.getLock().unlock();
      }
   }

   private boolean switchToReplicasConnection() throws SQLException {
      this.getLock().lock();

      try {
         if (this.replicasConnection == null || this.replicasConnection.isClosed()) {
            try {
               if (this.initializeReplicasConnection() == null) {
                  return false;
               }
            } catch (SQLException var5) {
               this.currentConnection = null;
               throw var5;
            }
         }

         if (!this.isReplicasConnection() && this.replicasConnection != null) {
            this.syncSessionState(this.currentConnection, this.replicasConnection, true);
            this.currentConnection = this.replicasConnection;
         }

         return true;
      } finally {
         this.getLock().unlock();
      }
   }

   private boolean switchToReplicasConnectionIfNecessary() throws SQLException {
      return this.currentConnection != null
            && (!this.isSourceConnection() || !this.readOnly && (!this.sourceHosts.isEmpty() || !this.currentConnection.isClosed()))
            && (this.isSourceConnection() || !this.currentConnection.isClosed())
         ? false
         : this.switchToReplicasConnection();
   }

   public JdbcConnection getCurrentConnection() {
      this.getLock().lock();

      Object var1;
      try {
         var1 = this.currentConnection == null ? LoadBalancedConnectionProxy.getNullLoadBalancedConnectionInstance() : this.currentConnection;
      } finally {
         this.getLock().unlock();
      }

      return (JdbcConnection)var1;
   }

   public long getConnectionGroupId() {
      return this.connectionGroupID;
   }

   public JdbcConnection getSourceConnection() {
      this.getLock().lock();

      LoadBalancedConnection var1;
      try {
         var1 = this.sourceConnection;
      } finally {
         this.getLock().unlock();
      }

      return var1;
   }

   public void promoteReplicaToSource(String hostPortPair) throws SQLException {
      this.getLock().lock();

      try {
         HostInfo host = this.getReplicaHost(hostPortPair);
         if (host == null) {
            return;
         }

         this.sourceHosts.add(host);
         this.removeReplica(hostPortPair);
         if (this.sourceConnection != null) {
            this.sourceConnection.addHost(hostPortPair);
         }

         if (!this.readOnly && !this.isSourceConnection()) {
            this.switchToSourceConnection();
         }
      } finally {
         this.getLock().unlock();
      }
   }

   public void removeSourceHost(String hostPortPair) throws SQLException {
      this.getLock().lock();

      try {
         this.removeSourceHost(hostPortPair, true);
      } finally {
         this.getLock().unlock();
      }
   }

   public void removeSourceHost(String hostPortPair, boolean waitUntilNotInUse) throws SQLException {
      this.getLock().lock();

      try {
         this.removeSourceHost(hostPortPair, waitUntilNotInUse, false);
      } finally {
         this.getLock().unlock();
      }
   }

   public void removeSourceHost(String hostPortPair, boolean waitUntilNotInUse, boolean isNowReplica) throws SQLException {
      this.getLock().lock();

      try {
         HostInfo host = this.getSourceHost(hostPortPair);
         if (host == null) {
            return;
         }

         if (isNowReplica) {
            this.replicaHosts.add(host);
            this.resetReadFromSourceWhenNoReplicas();
         }

         this.sourceHosts.remove(host);
         if (this.sourceConnection == null || this.sourceConnection.isClosed()) {
            this.sourceConnection = null;
            return;
         }

         if (waitUntilNotInUse) {
            this.sourceConnection.removeHostWhenNotInUse(hostPortPair);
         } else {
            this.sourceConnection.removeHost(hostPortPair);
         }

         if (this.sourceHosts.isEmpty()) {
            this.sourceConnection.close();
            this.sourceConnection = null;
            this.switchToReplicasConnectionIfNecessary();
         }
      } finally {
         this.getLock().unlock();
      }
   }

   public boolean isHostSource(String hostPortPair) {
      return hostPortPair == null ? false : this.sourceHosts.stream().anyMatch(hi -> hostPortPair.equalsIgnoreCase(hi.getHostPortPair()));
   }

   public JdbcConnection getReplicasConnection() {
      this.getLock().lock();

      LoadBalancedConnection var1;
      try {
         var1 = this.replicasConnection;
      } finally {
         this.getLock().unlock();
      }

      return var1;
   }

   public void addReplicaHost(String hostPortPair) throws SQLException {
      this.getLock().lock();

      try {
         if (this.isHostReplica(hostPortPair)) {
            return;
         }

         this.replicaHosts.add(this.getConnectionUrl().getReplicaHostOrSpawnIsolated(hostPortPair));
         this.resetReadFromSourceWhenNoReplicas();
         if (this.replicasConnection == null) {
            this.initializeReplicasConnection();
            this.switchToReplicasConnectionIfNecessary();
         } else {
            this.replicasConnection.addHost(hostPortPair);
         }
      } finally {
         this.getLock().unlock();
      }
   }

   public void removeReplica(String hostPortPair) throws SQLException {
      this.getLock().lock();

      try {
         this.removeReplica(hostPortPair, true);
      } finally {
         this.getLock().unlock();
      }
   }

   public void removeReplica(String hostPortPair, boolean closeGently) throws SQLException {
      this.getLock().lock();

      try {
         HostInfo host = this.getReplicaHost(hostPortPair);
         if (host == null) {
            return;
         }

         this.replicaHosts.remove(host);
         this.resetReadFromSourceWhenNoReplicas();
         if (this.replicasConnection == null || this.replicasConnection.isClosed()) {
            this.replicasConnection = null;
            return;
         }

         if (closeGently) {
            this.replicasConnection.removeHostWhenNotInUse(hostPortPair);
         } else {
            this.replicasConnection.removeHost(hostPortPair);
         }

         if (this.replicaHosts.isEmpty()) {
            this.replicasConnection.close();
            this.replicasConnection = null;
            this.switchToSourceConnection();
            if (this.isSourceConnection()) {
               this.currentConnection.setReadOnly(this.readOnly);
            }
         }
      } finally {
         this.getLock().unlock();
      }
   }

   public boolean isHostReplica(String hostPortPair) {
      return hostPortPair == null ? false : this.replicaHosts.stream().anyMatch(hi -> hostPortPair.equalsIgnoreCase(hi.getHostPortPair()));
   }

   public void setReadOnly(boolean readOnly) throws SQLException {
      this.getLock().lock();

      try {
         if (readOnly) {
            if (!this.isReplicasConnection() || this.currentConnection.isClosed()) {
               boolean switched = true;
               SQLException exceptionCaught = null;

               try {
                  switched = this.switchToReplicasConnection();
               } catch (SQLException var10) {
                  switched = false;
                  exceptionCaught = var10;
               }

               if (!switched && this.readFromSourceWhenNoReplicas && this.switchToSourceConnection()) {
                  exceptionCaught = null;
               }

               if (exceptionCaught != null) {
                  throw exceptionCaught;
               }
            }
         } else if (!this.isSourceConnection() || this.currentConnection.isClosed()) {
            boolean switched = true;
            SQLException exceptionCaught = null;

            try {
               switched = this.switchToSourceConnection();
            } catch (SQLException var9) {
               switched = false;
               exceptionCaught = var9;
            }

            if (!switched && this.switchToReplicasConnectionIfNecessary()) {
               exceptionCaught = null;
            }

            if (exceptionCaught != null) {
               throw exceptionCaught;
            }
         }

         this.readOnly = readOnly;
         if (this.readFromSourceWhenNoReplicas && this.isSourceConnection()) {
            this.currentConnection.setReadOnly(this.readOnly);
         }
      } finally {
         this.getLock().unlock();
      }
   }

   public boolean isReadOnly() throws SQLException {
      return !this.isSourceConnection() || this.readOnly;
   }

   private void resetReadFromSourceWhenNoReplicas() {
      this.readFromSourceWhenNoReplicas = this.replicaHosts.isEmpty() || this.readFromSourceWhenNoReplicasOriginal;
   }

   private HostInfo getSourceHost(String hostPortPair) {
      return this.sourceHosts.stream().filter(hi -> hostPortPair.equalsIgnoreCase(hi.getHostPortPair())).findFirst().orElse(null);
   }

   private HostInfo getReplicaHost(String hostPortPair) {
      return this.replicaHosts.stream().filter(hi -> hostPortPair.equalsIgnoreCase(hi.getHostPortPair())).findFirst().orElse(null);
   }

   private ReplicationConnectionUrl getConnectionUrl() {
      return (ReplicationConnectionUrl)this.connectionUrl;
   }
}
