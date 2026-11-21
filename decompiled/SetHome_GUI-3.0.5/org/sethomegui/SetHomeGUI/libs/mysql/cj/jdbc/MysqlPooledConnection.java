package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEvent;
import javax.sql.StatementEventListener;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionInterceptor;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.SQLError;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;

public class MysqlPooledConnection implements PooledConnection {
   public static final int CONNECTION_ERROR_EVENT = 1;
   public static final int CONNECTION_CLOSED_EVENT = 2;
   private Map<ConnectionEventListener, ConnectionEventListener> connectionEventListeners;
   private Connection logicalHandle;
   private JdbcConnection physicalConn;
   private ExceptionInterceptor exceptionInterceptor;
   private final Map<StatementEventListener, StatementEventListener> statementEventListeners = new HashMap<>();
   private final Lock statementEventListenersLock = new ReentrantLock();
   protected final Lock lock = new ReentrantLock();

   protected static MysqlPooledConnection getInstance(JdbcConnection connection) throws SQLException {
      return new MysqlPooledConnection(connection);
   }

   public MysqlPooledConnection(JdbcConnection connection) {
      this.logicalHandle = null;
      this.physicalConn = connection;
      this.connectionEventListeners = new HashMap<>();
      this.exceptionInterceptor = this.physicalConn.getExceptionInterceptor();
   }

   public Lock getLock() {
      return this.lock;
   }

   @Override
   public void addConnectionEventListener(ConnectionEventListener connectioneventlistener) {
      this.lock.lock();

      try {
         if (this.connectionEventListeners != null) {
            this.connectionEventListeners.put(connectioneventlistener, connectioneventlistener);
         }
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public void removeConnectionEventListener(ConnectionEventListener connectioneventlistener) {
      this.lock.lock();

      try {
         if (this.connectionEventListeners != null) {
            this.connectionEventListeners.remove(connectioneventlistener);
         }
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public Connection getConnection() throws SQLException {
      try {
         this.lock.lock();

         Connection var1;
         try {
            var1 = this.getConnection(true, false);
         } finally {
            this.lock.unlock();
         }

         return var1;
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.exceptionInterceptor);
      }
   }

   protected Connection getConnection(boolean resetServerState, boolean forXa) throws SQLException {
      this.lock.lock();

      Connection sqlException;
      try {
         if (this.physicalConn == null) {
            SQLException sqlExceptionx = SQLError.createSQLException(Messages.getString("MysqlPooledConnection.0"), this.exceptionInterceptor);
            this.callConnectionEventListeners(1, sqlExceptionx);
            throw sqlExceptionx;
         }

         try {
            if (this.logicalHandle != null) {
               ((ConnectionWrapper)this.logicalHandle).close(false);
            }

            if (resetServerState) {
               this.physicalConn.resetServerState();
            }

            this.logicalHandle = ConnectionWrapper.getInstance(this, this.physicalConn, forXa);
         } catch (SQLException var7) {
            this.callConnectionEventListeners(1, var7);
            throw var7;
         }

         sqlException = this.logicalHandle;
      } finally {
         this.lock.unlock();
      }

      return sqlException;
   }

   @Override
   public void close() throws SQLException {
      try {
         this.lock.lock();

         try {
            if (this.physicalConn != null) {
               this.physicalConn.close();
               this.physicalConn = null;
            }

            if (this.connectionEventListeners != null) {
               this.connectionEventListeners.clear();
               this.connectionEventListeners = null;
            }

            this.statementEventListeners.clear();
         } finally {
            this.lock.unlock();
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.exceptionInterceptor);
      }
   }

   protected void callConnectionEventListeners(int eventType, SQLException sqlException) {
      this.lock.lock();

      try {
         if (this.connectionEventListeners == null) {
            return;
         }

         Iterator<Entry<ConnectionEventListener, ConnectionEventListener>> iterator = this.connectionEventListeners.entrySet().iterator();
         ConnectionEvent connectionevent = new ConnectionEvent(this, sqlException);

         while (iterator.hasNext()) {
            ConnectionEventListener connectioneventlistener = iterator.next().getValue();
            if (eventType == 2) {
               connectioneventlistener.connectionClosed(connectionevent);
            } else if (eventType == 1) {
               connectioneventlistener.connectionErrorOccurred(connectionevent);
            }
         }
      } finally {
         this.lock.unlock();
      }
   }

   protected ExceptionInterceptor getExceptionInterceptor() {
      return this.exceptionInterceptor;
   }

   @Override
   public void addStatementEventListener(StatementEventListener listener) {
      this.statementEventListenersLock.lock();

      try {
         this.statementEventListeners.put(listener, listener);
      } finally {
         this.statementEventListenersLock.unlock();
      }
   }

   @Override
   public void removeStatementEventListener(StatementEventListener listener) {
      this.statementEventListenersLock.lock();

      try {
         this.statementEventListeners.remove(listener);
      } finally {
         this.statementEventListenersLock.unlock();
      }
   }

   void fireStatementEvent(StatementEvent event) throws SQLException {
      this.statementEventListenersLock.lock();

      try {
         for (StatementEventListener listener : this.statementEventListeners.keySet()) {
            listener.statementClosed(event);
         }
      } finally {
         this.statementEventListenersLock.unlock();
      }
   }
}
