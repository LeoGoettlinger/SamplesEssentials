package com.mysql.cj.jdbc.ha;

import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.jdbc.JdbcConnection;
import com.mysql.cj.jdbc.exceptions.SQLError;
import com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ReplicationMySQLConnection extends MultiHostMySQLConnection implements ReplicationConnection {
   public ReplicationMySQLConnection(MultiHostConnectionProxy proxy) {
      super(proxy);
   }

   public ReplicationConnectionProxy getThisAsProxy() {
      return (ReplicationConnectionProxy)super.getThisAsProxy();
   }

   @Override
   public JdbcConnection getActiveMySQLConnection() {
      return this.getCurrentConnection();
   }

   @Override
   public JdbcConnection getCurrentConnection() {
      this.getLock().lock();

      JdbcConnection var1;
      try {
         var1 = this.getThisAsProxy().getCurrentConnection();
      } finally {
         this.getLock().unlock();
      }

      return var1;
   }

   @Override
   public long getConnectionGroupId() {
      return this.getThisAsProxy().getConnectionGroupId();
   }

   @Override
   public JdbcConnection getSourceConnection() {
      this.getLock().lock();

      JdbcConnection var1;
      try {
         var1 = this.getThisAsProxy().getSourceConnection();
      } finally {
         this.getLock().unlock();
      }

      return var1;
   }

   private JdbcConnection getValidatedSourceConnection() {
      JdbcConnection conn = this.getThisAsProxy().sourceConnection;

      try {
         return conn != null && !conn.isClosed() ? conn : null;
      } catch (SQLException var3) {
         return null;
      }
   }

   @Override
   public void promoteReplicaToSource(String host) throws SQLException {
      try {
         this.getThisAsProxy().promoteReplicaToSource(host);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void removeSourceHost(String host) throws SQLException {
      try {
         this.getThisAsProxy().removeSourceHost(host);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void removeSourceHost(String host, boolean waitUntilNotInUse) throws SQLException {
      try {
         this.getThisAsProxy().removeSourceHost(host, waitUntilNotInUse);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isHostSource(String host) {
      return this.getThisAsProxy().isHostSource(host);
   }

   @Override
   public JdbcConnection getReplicaConnection() {
      this.getLock().lock();

      JdbcConnection var1;
      try {
         var1 = this.getThisAsProxy().getReplicasConnection();
      } finally {
         this.getLock().unlock();
      }

      return var1;
   }

   private JdbcConnection getValidatedReplicasConnection() {
      JdbcConnection conn = this.getThisAsProxy().replicasConnection;

      try {
         return conn != null && !conn.isClosed() ? conn : null;
      } catch (SQLException var3) {
         return null;
      }
   }

   @Override
   public void addReplicaHost(String host) throws SQLException {
      try {
         this.getThisAsProxy().addReplicaHost(host);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void removeReplica(String host) throws SQLException {
      try {
         this.getThisAsProxy().removeReplica(host);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void removeReplica(String host, boolean closeGently) throws SQLException {
      try {
         this.getThisAsProxy().removeReplica(host, closeGently);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isHostReplica(String host) {
      return this.getThisAsProxy().isHostReplica(host);
   }

   @Override
   public void setReadOnly(boolean readOnlyFlag) throws SQLException {
      try {
         this.getThisAsProxy().setReadOnly(readOnlyFlag);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean isReadOnly() throws SQLException {
      try {
         return this.getThisAsProxy().isReadOnly();
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public void ping() throws SQLException {
      try {
         this.getLock().lock();

         try {
            try {
               JdbcConnection conn;
               if ((conn = this.getValidatedSourceConnection()) != null) {
                  conn.ping();
               }
            } catch (SQLException var10) {
               if (this.isSourceConnection()) {
                  throw var10;
               }
            }

            try {
               JdbcConnection connx;
               if ((connx = this.getValidatedReplicasConnection()) != null) {
                  connx.ping();
               }
            } catch (SQLException var9) {
               if (!this.isSourceConnection()) {
                  throw var9;
               }
            }
         } finally {
            this.getLock().unlock();
         }
      } catch (CJException var12) {
         throw SQLExceptionsMapping.translateException(var12, this.getExceptionInterceptor());
      }
   }

   @Override
   public void changeUser(String userName, String newPassword) throws SQLException {
      try {
         this.getLock().lock();

         try {
            JdbcConnection conn;
            if ((conn = this.getValidatedSourceConnection()) != null) {
               conn.changeUser(userName, newPassword);
            }

            if ((conn = this.getValidatedReplicasConnection()) != null) {
               conn.changeUser(userName, newPassword);
            }
         } finally {
            this.getLock().unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setStatementComment(String comment) {
      this.getLock().lock();

      try {
         JdbcConnection conn;
         if ((conn = this.getValidatedSourceConnection()) != null) {
            conn.setStatementComment(comment);
         }

         if ((conn = this.getValidatedReplicasConnection()) != null) {
            conn.setStatementComment(comment);
         }
      } finally {
         this.getLock().unlock();
      }
   }

   @Override
   public boolean hasSameProperties(JdbcConnection c) {
      JdbcConnection connM = this.getValidatedSourceConnection();
      JdbcConnection connS = this.getValidatedReplicasConnection();
      return connM == null && connS == null ? false : (connM == null || connM.hasSameProperties(c)) && (connS == null || connS.hasSameProperties(c));
   }

   @Override
   public Properties getProperties() {
      Properties props = new Properties();
      JdbcConnection conn;
      if ((conn = this.getValidatedSourceConnection()) != null) {
         props.putAll(conn.getProperties());
      }

      if ((conn = this.getValidatedReplicasConnection()) != null) {
         props.putAll(conn.getProperties());
      }

      return props;
   }

   @Override
   public void abort(Executor executor) throws SQLException {
      try {
         this.getThisAsProxy().doAbort(executor);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public void abortInternal() throws SQLException {
      try {
         this.getThisAsProxy().doAbortInternal();
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setProxy(JdbcConnection proxy) {
      this.getThisAsProxy().setProxy(proxy);
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      try {
         return iface.isInstance(this);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      try {
         try {
            return iface.cast(this);
         } catch (ClassCastException var4) {
            throw SQLError.createSQLException(
               Messages.getString("Common.UnableToUnwrap", new Object[]{iface.toString()}), "S1009", this.getExceptionInterceptor()
            );
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }
}
