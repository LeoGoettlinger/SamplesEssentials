package com.mysql.cj.jdbc;

import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class SuspendableXAConnection extends MysqlPooledConnection implements XAConnection, XAResource {
   private static final Lock LOCK = new ReentrantLock();
   private final Lock lock = new ReentrantLock();
   private static final Map<Xid, XAConnection> XIDS_TO_PHYSICAL_CONNECTIONS = new HashMap<>();
   private Xid currentXid;
   private XAConnection currentXAConnection;
   private XAResource currentXAResource;
   private JdbcConnection underlyingConnection;

   protected static SuspendableXAConnection getInstance(JdbcConnection mysqlConnection) throws SQLException {
      return new SuspendableXAConnection(mysqlConnection);
   }

   public SuspendableXAConnection(JdbcConnection connection) {
      super(connection);
      this.underlyingConnection = connection;
   }

   private static XAConnection findConnectionForXid(JdbcConnection connectionToWrap, Xid xid) throws SQLException {
      LOCK.lock();

      Object var3;
      try {
         XAConnection conn = XIDS_TO_PHYSICAL_CONNECTIONS.get(xid);
         if (conn == null) {
            conn = new MysqlXAConnection(connectionToWrap, connectionToWrap.getPropertySet().getBooleanProperty(PropertyKey.logXaCommands).getValue());
            XIDS_TO_PHYSICAL_CONNECTIONS.put(xid, conn);
         }

         var3 = conn;
      } finally {
         LOCK.unlock();
      }

      return (XAConnection)var3;
   }

   private static void removeXAConnectionMapping(Xid xid) {
      LOCK.lock();

      try {
         XIDS_TO_PHYSICAL_CONNECTIONS.remove(xid);
      } finally {
         LOCK.unlock();
      }
   }

   private void switchToXid(Xid xid) throws XAException {
      this.lock.lock();

      try {
         if (xid == null) {
            throw new XAException();
         }

         try {
            if (!xid.equals(this.currentXid)) {
               XAConnection toSwitchTo = findConnectionForXid(this.underlyingConnection, xid);
               this.currentXAConnection = toSwitchTo;
               this.currentXid = xid;
               this.currentXAResource = toSwitchTo.getXAResource();
            }
         } catch (SQLException var6) {
            throw new XAException();
         }
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public XAResource getXAResource() throws SQLException {
      try {
         return this;
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2);
      }
   }

   @Override
   public void commit(Xid xid, boolean arg1) throws XAException {
      this.switchToXid(xid);
      this.currentXAResource.commit(xid, arg1);
      removeXAConnectionMapping(xid);
   }

   @Override
   public void end(Xid xid, int arg1) throws XAException {
      this.switchToXid(xid);
      this.currentXAResource.end(xid, arg1);
   }

   @Override
   public void forget(Xid xid) throws XAException {
      this.switchToXid(xid);
      this.currentXAResource.forget(xid);
      removeXAConnectionMapping(xid);
   }

   @Override
   public int getTransactionTimeout() throws XAException {
      return 0;
   }

   @Override
   public boolean isSameRM(XAResource xaRes) throws XAException {
      return xaRes == this;
   }

   @Override
   public int prepare(Xid xid) throws XAException {
      this.switchToXid(xid);
      return this.currentXAResource.prepare(xid);
   }

   @Override
   public Xid[] recover(int flag) throws XAException {
      return MysqlXAConnection.recover(this.underlyingConnection, flag);
   }

   @Override
   public void rollback(Xid xid) throws XAException {
      this.switchToXid(xid);
      this.currentXAResource.rollback(xid);
      removeXAConnectionMapping(xid);
   }

   @Override
   public boolean setTransactionTimeout(int arg0) throws XAException {
      return false;
   }

   @Override
   public void start(Xid xid, int arg1) throws XAException {
      this.switchToXid(xid);
      if (arg1 != 2097152) {
         this.currentXAResource.start(xid, arg1);
      } else {
         this.currentXAResource.start(xid, 134217728);
      }
   }

   @Override
   public Connection getConnection() throws SQLException {
      try {
         this.lock.lock();

         Connection var1;
         try {
            if (this.currentXAConnection != null) {
               return this.currentXAConnection.getConnection();
            }

            var1 = this.getConnection(false, true);
         } finally {
            this.lock.unlock();
         }

         return var1;
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7);
      }
   }

   @Override
   public void close() throws SQLException {
      try {
         if (this.currentXAConnection == null) {
            super.close();
         } else {
            removeXAConnectionMapping(this.currentXid);
            this.currentXAConnection.close();
         }
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2);
      }
   }
}
