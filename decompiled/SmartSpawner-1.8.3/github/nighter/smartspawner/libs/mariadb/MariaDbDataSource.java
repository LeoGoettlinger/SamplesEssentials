package github.nighter.smartspawner.libs.mariadb;

import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

public class MariaDbDataSource implements DataSource, ConnectionPoolDataSource, XADataSource {
   private Configuration.Builder builder = new Configuration.Builder();
   private Configuration conf = null;

   public MariaDbDataSource() {
   }

   public MariaDbDataSource(String url) throws SQLException {
      this.applyUrl(url);
   }

   private Configuration conf() throws SQLException {
      if (this.conf == null) {
         if (this.builder._addresses.isEmpty()) {
            throw new SQLException("url not set");
         }

         this.conf = this.builder.build();
      }

      return this.conf;
   }

   @Override
   public java.sql.Connection getConnection() throws SQLException {
      return Driver.connect(this.conf());
   }

   @Override
   public java.sql.Connection getConnection(String username, String password) throws SQLException {
      return Driver.connect(this.conf().clone(username, password));
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
      this.conf = null;
   }

   @Override
   public Logger getParentLogger() {
      return null;
   }

   @Override
   public PooledConnection getPooledConnection() throws SQLException {
      Configuration conf = this.conf();
      Connection conn = Driver.connect(conf);
      MariaDbPoolConnection poolConnection = (MariaDbPoolConnection)(conf.pinGlobalTxToPhysicalConnection()
         ? new MariaDbPoolPinnedConnection(conn)
         : new MariaDbPoolConnection(conn));
      return poolConnection;
   }

   @Override
   public PooledConnection getPooledConnection(String username, String password) throws SQLException {
      Configuration conf = this.conf().clone(username, password);
      Connection conn = Driver.connect(conf);
      MariaDbPoolConnection poolConnection = (MariaDbPoolConnection)(conf.pinGlobalTxToPhysicalConnection()
         ? new MariaDbPoolPinnedConnection(conn)
         : new MariaDbPoolConnection(conn));
      return poolConnection;
   }

   @Override
   public XAConnection getXAConnection() throws SQLException {
      return (MariaDbPoolConnection)this.getPooledConnection();
   }

   @Override
   public XAConnection getXAConnection(String username, String password) throws SQLException {
      return (MariaDbPoolConnection)this.getPooledConnection(username, password);
   }

   public String getUrl() {
      try {
         return this.conf().toString();
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
      }
   }

   public String getUser() {
      return this.builder.user;
   }

   public void setUser(String user) throws SQLException {
      this.builder.user(user);
      this.conf = null;
   }

   public void setPassword(String password) throws SQLException {
      this.builder.password(password);
      this.conf = null;
   }
}
