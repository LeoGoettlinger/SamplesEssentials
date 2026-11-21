package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientInfoProviderSP implements ClientInfoProvider {
   public static final String PNAME_clientInfoSetSPName = "clientInfoSetSPName";
   public static final String PNAME_clientInfoGetSPName = "clientInfoGetSPName";
   public static final String PNAME_clientInfoGetBulkSPName = "clientInfoGetBulkSPName";
   public static final String PNAME_clientInfoDatabase = "clientInfoDatabase";
   PreparedStatement setClientInfoSp;
   PreparedStatement getClientInfoSp;
   PreparedStatement getClientInfoBulkSp;
   private final Lock lock = new ReentrantLock();

   @Override
   public void initialize(Connection conn, Properties configurationProps) throws SQLException {
      this.lock.lock();

      try {
         String identifierQuote = ((JdbcConnection)conn).getSession().getIdentifierQuoteString();
         String setClientInfoSpName = configurationProps.getProperty("clientInfoSetSPName", "setClientInfo");
         String getClientInfoSpName = configurationProps.getProperty("clientInfoGetSPName", "getClientInfo");
         String getClientInfoBulkSpName = configurationProps.getProperty("clientInfoGetBulkSPName", "getClientInfoBulk");
         String clientInfoDatabase = configurationProps.getProperty("clientInfoDatabase", "");
         String db = "".equals(clientInfoDatabase) ? ((JdbcConnection)conn).getDatabase() : clientInfoDatabase;
         this.setClientInfoSp = ((JdbcConnection)conn)
            .clientPrepareStatement("CALL " + identifierQuote + db + identifierQuote + "." + identifierQuote + setClientInfoSpName + identifierQuote + "(?, ?)");
         this.getClientInfoSp = ((JdbcConnection)conn)
            .clientPrepareStatement("CALL" + identifierQuote + db + identifierQuote + "." + identifierQuote + getClientInfoSpName + identifierQuote + "(?)");
         this.getClientInfoBulkSp = ((JdbcConnection)conn)
            .clientPrepareStatement("CALL " + identifierQuote + db + identifierQuote + "." + identifierQuote + getClientInfoBulkSpName + identifierQuote + "()");
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public void destroy() throws SQLException {
      this.lock.lock();

      try {
         if (this.setClientInfoSp != null) {
            this.setClientInfoSp.close();
            this.setClientInfoSp = null;
         }

         if (this.getClientInfoSp != null) {
            this.getClientInfoSp.close();
            this.getClientInfoSp = null;
         }

         if (this.getClientInfoBulkSp != null) {
            this.getClientInfoBulkSp.close();
            this.getClientInfoBulkSp = null;
         }
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public Properties getClientInfo(Connection conn) throws SQLException {
      this.lock.lock();

      Properties var4;
      try {
         ResultSet rs = null;
         Properties props = new Properties();

         try {
            this.getClientInfoBulkSp.execute();
            rs = this.getClientInfoBulkSp.getResultSet();

            while (rs.next()) {
               props.setProperty(rs.getString(1), rs.getString(2));
            }
         } finally {
            if (rs != null) {
               rs.close();
            }
         }

         var4 = props;
      } finally {
         this.lock.unlock();
      }

      return var4;
   }

   @Override
   public String getClientInfo(Connection conn, String name) throws SQLException {
      this.lock.lock();

      String var5;
      try {
         ResultSet rs = null;
         String clientInfo = null;

         try {
            this.getClientInfoSp.setString(1, name);
            this.getClientInfoSp.execute();
            rs = this.getClientInfoSp.getResultSet();
            if (rs.next()) {
               clientInfo = rs.getString(1);
            }
         } finally {
            if (rs != null) {
               rs.close();
            }
         }

         var5 = clientInfo;
      } finally {
         this.lock.unlock();
      }

      return var5;
   }

   @Override
   public void setClientInfo(Connection conn, Properties properties) throws SQLClientInfoException {
      this.lock.lock();

      try {
         Enumeration<?> propNames = properties.propertyNames();

         while (propNames.hasMoreElements()) {
            String name = (String)propNames.nextElement();
            String value = properties.getProperty(name);
            this.setClientInfo(conn, name, value);
         }
      } catch (SQLException var9) {
         SQLClientInfoException clientInfoEx = new SQLClientInfoException();
         clientInfoEx.initCause(var9);
         throw clientInfoEx;
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public void setClientInfo(Connection conn, String name, String value) throws SQLClientInfoException {
      this.lock.lock();

      try {
         this.setClientInfoSp.setString(1, name);
         this.setClientInfoSp.setString(2, value);
         this.setClientInfoSp.execute();
      } catch (SQLException var9) {
         SQLClientInfoException clientInfoEx = new SQLClientInfoException();
         clientInfoEx.initCause(var9);
         throw clientInfoEx;
      } finally {
         this.lock.unlock();
      }
   }
}
