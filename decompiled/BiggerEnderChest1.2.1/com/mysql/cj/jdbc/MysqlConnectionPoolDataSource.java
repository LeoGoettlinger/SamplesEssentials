package com.mysql.cj.jdbc;

import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

public class MysqlConnectionPoolDataSource extends MysqlDataSource implements ConnectionPoolDataSource {
   static final long serialVersionUID = -7767325445592304961L;
   private final Lock lock = new ReentrantLock();

   @Override
   public PooledConnection getPooledConnection() throws SQLException {
      try {
         this.lock.lock();

         MysqlPooledConnection var3;
         try {
            Connection connection = this.getConnection();
            MysqlPooledConnection mysqlPooledConnection = MysqlPooledConnection.getInstance((JdbcConnection)connection);
            var3 = mysqlPooledConnection;
         } finally {
            this.lock.unlock();
         }

         return var3;
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9);
      }
   }

   @Override
   public PooledConnection getPooledConnection(String u, String p) throws SQLException {
      try {
         this.lock.lock();

         MysqlPooledConnection var5;
         try {
            Connection connection = this.getConnection(u, p);
            MysqlPooledConnection mysqlPooledConnection = MysqlPooledConnection.getInstance((JdbcConnection)connection);
            var5 = mysqlPooledConnection;
         } finally {
            this.lock.unlock();
         }

         return var5;
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11);
      }
   }
}
