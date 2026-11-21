package github.nighter.smartspawner.libs.mariadb;

import github.nighter.smartspawner.libs.mariadb.pool.Pool;
import github.nighter.smartspawner.libs.mariadb.pool.Pools;
import java.io.Closeable;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

public class MariaDbPoolDataSource implements DataSource, ConnectionPoolDataSource, XADataSource, Closeable, AutoCloseable {
   private Configuration.Builder builder = new Configuration.Builder();
   private Configuration conf = null;
   private Pool pool;

   public MariaDbPoolDataSource() {
   }

   public MariaDbPoolDataSource(String url) throws SQLException {
      this.applyUrl(url);
   }

   private Pool pool() throws SQLException {
      if (this.conf == null) {
         if (this.builder._addresses.isEmpty()) {
            throw new SQLException("url not set");
         }

         this.conf = this.builder.build();
         this.pool = Pools.retrievePool(this.conf);
      }

      return this.pool;
   }

   @Override
   public java.sql.Connection getConnection() throws SQLException {
      return this.pool().getPoolConnection().getConnection();
   }

   @Override
   public java.sql.Connection getConnection(String username, String password) throws SQLException {
      return this.pool().getPoolConnection(username, password).getConnection();
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      if (this.isWrapperFor(iface)) {
         return iface.cast(this);
      } else {
         throw new SQLException("Datasource is not a wrapper for " + iface.getName());
      }
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) {
      return iface.isInstance(this);
   }

   @Override
   public PrintWriter getLogWriter() {
      return null;
   }

   @Override
   public void setLogWriter(PrintWriter out) {
   }

   @Override
   public int getLoginTimeout() {
      if (this.builder.connectTimeout != null) {
         return this.builder.connectTimeout / 1000;
      } else {
         return DriverManager.getLoginTimeout() > 0 ? DriverManager.getLoginTimeout() : 30;
      }
   }

   @Override
   public void setLoginTimeout(int seconds) throws SQLException {
      this.builder.connectTimeout(seconds * 1000);
      if (this.conf != null) {
         this.conf = null;
         this.pool();
      }
   }

   @Override
   public Logger getParentLogger() {
      return null;
   }

   @Override
   public PooledConnection getPooledConnection() throws SQLException {
      return this.pool().getPoolConnection();
   }

   @Override
   public PooledConnection getPooledConnection(String username, String password) throws SQLException {
      return this.pool().getPoolConnection(username, password);
   }

   @Override
   public XAConnection getXAConnection() throws SQLException {
      return this.pool().getPoolConnection();
   }

   @Override
   public XAConnection getXAConnection(String username, String password) throws SQLException {
      return this.pool().getPoolConnection(username, password);
   }

   public String getUrl() {
      try {
         this.pool();
         return this.conf.toString();
      } catch (SQLException var2) {
         return null;
      }
   }

   public void setUrl(String url) throws SQLException {
      this.applyUrl(url);
   }

   private void applyUrl(String url) throws SQLException {
      if (!Configuration.acceptsUrl(url)) {
         throw new SQLException(String.format("Wrong mariaDB url: %s", url));
      } else {
         Configuration.Builder parsed = Configuration.parse(url).toBuilder();
         if (this.builder.user != null) {
            parsed.user(this.builder.user);
         }

         if (this.builder.password != null) {
            parsed.password(this.builder.password);
         }

         if (this.builder.connectTimeout != null) {
            parsed.connectTimeout(this.builder.connectTimeout);
         }

         this.builder = parsed;
         this.conf = null;
         this.pool();
      }
   }

   public String getUser() {
      return this.builder.user;
   }

   public void setUser(String user) throws SQLException {
      this.builder.user(user);
      if (this.conf != null) {
         this.conf = null;
         this.pool();
      }
   }

   public void setPassword(String password) throws SQLException {
      this.builder.password(password);
      if (this.conf != null) {
         this.conf = null;
         this.pool();
      }
   }

   @Override
   public void close() {
      if (this.pool != null) {
         this.pool.close();
      }
   }

   public String getPoolName() {
      return this.pool != null ? this.pool.getPoolTag() : null;
   }

   public List<Long> testGetConnectionIdleThreadIds() {
      return this.pool != null ? this.pool.testGetConnectionIdleThreadIds() : null;
   }
}
