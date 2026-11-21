package github.nighter.smartspawner.libs.mariadb;

import github.nighter.smartspawner.libs.mariadb.client.Client;
import github.nighter.smartspawner.libs.mariadb.client.impl.MultiPrimaryClient;
import github.nighter.smartspawner.libs.mariadb.client.impl.MultiPrimaryReplicaClient;
import github.nighter.smartspawner.libs.mariadb.client.impl.ReplayClient;
import github.nighter.smartspawner.libs.mariadb.client.impl.StandardClient;
import github.nighter.smartspawner.libs.mariadb.client.util.ClosableLock;
import github.nighter.smartspawner.libs.mariadb.export.HaMode;
import github.nighter.smartspawner.libs.mariadb.pool.Pools;
import github.nighter.smartspawner.libs.mariadb.util.VersionFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public final class Driver implements java.sql.Driver {
   private static final Pattern identifierPattern = Pattern.compile("[0-9a-zA-Z$_\\u0080-\\uFFFF]*", 64);

   public static Connection connect(Configuration configuration) throws SQLException {
      ClosableLock lock = new ClosableLock();
      if (configuration.haMode() != HaMode.NONE) {
         Client client = (Client)(configuration.havePrimaryHostOnly()
            ? new MultiPrimaryClient(configuration, lock)
            : new MultiPrimaryReplicaClient(configuration, lock));
         return new Connection(configuration, lock, client);
      } else {
         Driver.ClientInstance<Configuration, HostAddress, ClosableLock, Boolean, Client> clientInstance = configuration.transactionReplay()
            ? ReplayClient::new
            : StandardClient::new;
         if (configuration.addresses().isEmpty()) {
            throw new SQLException("host, pipe or local socket must be set to connect socket");
         } else {
            SQLException lastException = null;

            for (HostAddress host : configuration.addresses()) {
               try {
                  Client client = clientInstance.apply(configuration, host, lock, false);
                  return new Connection(configuration, lock, client);
               } catch (SQLException var7) {
                  lastException = var7;
               }
            }

            throw lastException;
         }
      }
   }

   public Connection connect(String url, Properties props) throws SQLException {
      Configuration configuration = Configuration.parse(url, props);
      if (configuration != null) {
         return configuration.pool() ? Pools.retrievePool(configuration).getPoolConnection().getConnection() : connect(configuration);
      } else {
         return null;
      }
   }

   @Override
   public boolean acceptsURL(String url) {
      return Configuration.acceptsUrl(url);
   }

   @Override
   public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
      Configuration conf = Configuration.parse(url, info);
      if (conf == null) {
         return new DriverPropertyInfo[0];
      } else {
         Properties propDesc = new Properties();

         try {
            InputStream inputStream = Driver.class.getClassLoader().getResourceAsStream("driver.properties");

            try {
               propDesc.load(inputStream);
            } catch (Throwable var16) {
               if (inputStream != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var15) {
                     var16.addSuppressed(var15);
                  }
               }

               throw var16;
            }

            if (inputStream != null) {
               inputStream.close();
            }
         } catch (IOException var17) {
         }

         List<DriverPropertyInfo> props = new ArrayList<>();

         for (Field field : Configuration.Builder.class.getDeclaredFields()) {
            if (!field.getName().startsWith("_")) {
               try {
                  Method getterMethod = Configuration.class.getDeclaredMethod(field.getName());
                  Object obj = getterMethod.invoke(conf);
                  String value = obj == null ? null : obj.toString();
                  DriverPropertyInfo propertyInfo = new DriverPropertyInfo(field.getName(), value);
                  propertyInfo.description = value == null ? "" : (String)propDesc.get(field.getName());
                  propertyInfo.required = false;
                  props.add(propertyInfo);
               } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException var14) {
               }
            }
         }

         return props.toArray(new DriverPropertyInfo[0]);
      }
   }

   @Override
   public int getMajorVersion() {
      return VersionFactory.getInstance().getMajorVersion();
   }

   @Override
   public int getMinorVersion() {
      return VersionFactory.getInstance().getMinorVersion();
   }

   @Override
   public boolean jdbcCompliant() {
      return true;
   }

   @Override
   public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      throw new SQLFeatureNotSupportedException("Use logging parameters for enabling logging.");
   }

   public static String enquoteIdentifier(String identifier, boolean alwaysQuote) throws SQLException {
      int len = identifier.length();
      if (isSimpleIdentifier(identifier)) {
         if (len >= 1 && len <= 64) {
            if (alwaysQuote) {
               return "`" + identifier + "`";
            } else {
               for (int i = 0; i < identifier.length(); i++) {
                  if (!Character.isDigit(identifier.charAt(i))) {
                     return identifier;
                  }
               }

               return "`" + identifier + "`";
            }
         } else {
            throw new SQLException("Invalid identifier length");
         }
      } else if (identifier.contains("\u0000")) {
         throw new SQLException("Invalid name - containing u0000 character", "42000");
      } else {
         if (identifier.matches("^`.+`$")) {
            identifier = identifier.substring(1, identifier.length() - 1);
         }

         if (len >= 1 && len <= 64) {
            return "`" + identifier.replace("`", "``") + "`";
         } else {
            throw new SQLException("Invalid identifier length");
         }
      }
   }

   public static String enquoteLiteral(String val) {
      return enquoteLiteral(val, false);
   }

   public static String enquoteLiteral(String val, boolean noBackslashEscapes) {
      int len = val.length();
      int i = 0;

      while (i < len && !mustEscapeLiteral(val.charAt(i), noBackslashEscapes)) {
         i++;
      }

      if (i == len) {
         return "'" + val + "'";
      } else {
         StringBuilder sb = new StringBuilder(len + 16);
         sb.append('\'').append(val, 0, i);
         if (noBackslashEscapes) {
            for (; i < len; i++) {
               char c = val.charAt(i);
               if (c == '\'') {
                  sb.append("''");
               } else {
                  sb.append(c);
               }
            }
         } else {
            for (; i < len; i++) {
               char c = val.charAt(i);
               switch (c) {
                  case '\u0000':
                     sb.append("\\0");
                     break;
                  case '\b':
                     sb.append("\\b");
                     break;
                  case '\t':
                     sb.append("\\t");
                     break;
                  case '\n':
                     sb.append("\\n");
                     break;
                  case '\r':
                     sb.append("\\r");
                     break;
                  case '\u001a':
                     sb.append("\\Z");
                     break;
                  case '"':
                     sb.append("\\\"");
                     break;
                  case '\'':
                     sb.append("''");
                     break;
                  case '\\':
                     sb.append("\\\\");
                     break;
                  default:
                     sb.append(c);
               }
            }
         }

         sb.append('\'');
         return sb.toString();
      }
   }

   private static boolean mustEscapeLiteral(char c, boolean noBackslashEscapes) {
      if (c == '\'') {
         return true;
      } else {
         return noBackslashEscapes ? false : c == '\\' || c == '"' || c == 0 || c == '\b' || c == '\n' || c == '\r' || c == '\t' || c == 26;
      }
   }

   public static boolean isSimpleIdentifier(String identifier) {
      return identifier != null && !identifier.isEmpty() && identifierPattern.matcher(identifier).matches();
   }

   static {
      try {
         DriverManager.registerDriver(new Driver());
      } catch (SQLException var1) {
      }
   }

   @FunctionalInterface
   private interface ClientInstance<T, U, V, W, R> {
      R apply(T var1, U var2, V var3, W var4) throws SQLException;
   }
}
