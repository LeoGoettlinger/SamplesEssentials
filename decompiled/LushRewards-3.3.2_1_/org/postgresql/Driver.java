package org.postgresql;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.jdbc.ResourceLock;
import org.postgresql.jdbcurlresolver.PgPassParser;
import org.postgresql.jdbcurlresolver.PgServiceConfParser;
import org.postgresql.util.GT;
import org.postgresql.util.HostSpec;
import org.postgresql.util.PGPropertyUtil;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.SharedTimer;
import org.postgresql.util.URLCoder;
import org.postgresql.util.internal.Nullness;

public class Driver implements java.sql.Driver {
   @Nullable
   private static Driver registeredDriver;
   private static final Logger PARENT_LOGGER = Logger.getLogger("org.postgresql");
   private static final Logger LOGGER = Logger.getLogger("org.postgresql.Driver");
   private static final SharedTimer SHARED_TIMER = new SharedTimer();
   @Nullable
   private Properties defaultProperties;
   private final ResourceLock lock = new ResourceLock();

   private Properties getDefaultProperties() throws IOException {
      ResourceLock ignore = this.lock.obtain();

      Properties var8;
      label68: {
         try {
            if (this.defaultProperties != null) {
               var8 = this.defaultProperties;
               break label68;
            }

            try {
               this.defaultProperties = doPrivileged(new PrivilegedExceptionAction<Properties>() {
                  public Properties run() throws IOException {
                     return Driver.this.loadDefaultProperties();
                  }
               });
            } catch (PrivilegedActionException var5) {
               Exception ex = var5.getException();
               if (ex instanceof IOException) {
                  throw (IOException)ex;
               }

               throw new RuntimeException(var5);
            } catch (Throwable var6) {
               if (var6 instanceof IOException) {
                  throw (IOException)var6;
               }

               if (var6 instanceof RuntimeException) {
                  throw (RuntimeException)var6;
               }

               if (var6 instanceof Error) {
                  throw (Error)var6;
               }

               throw new RuntimeException(var6);
            }

            var8 = this.defaultProperties;
         } catch (Throwable var7) {
            if (ignore != null) {
               try {
                  ignore.close();
               } catch (Throwable var4) {
                  var7.addSuppressed(var4);
               }
            }

            throw var7;
         }

         if (ignore != null) {
            ignore.close();
         }

         return var8;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var8;
   }

   private static <T> T doPrivileged(PrivilegedExceptionAction<T> action) throws Throwable {
      try {
         Class<?> accessControllerClass = Class.forName("java.security.AccessController");
         Method doPrivileged = accessControllerClass.getMethod("doPrivileged", PrivilegedExceptionAction.class);
         return (T)doPrivileged.invoke(null, action);
      } catch (ClassNotFoundException var3) {
         return action.run();
      } catch (InvocationTargetException var4) {
         throw Nullness.castNonNull(var4.getCause());
      }
   }

   private Properties loadDefaultProperties() throws IOException {
      Properties merged = new Properties();

      try {
         PGProperty.USER.set(merged, System.getProperty("user.name"));
      } catch (SecurityException var8) {
      }

      ClassLoader cl = this.getClass().getClassLoader();
      if (cl == null) {
         LOGGER.log(Level.FINE, "Can't find our classloader for the Driver; attempt to use the system class loader");
         cl = ClassLoader.getSystemClassLoader();
      }

      if (cl == null) {
         LOGGER.log(Level.WARNING, "Can't find a classloader for the Driver; not loading driver configuration from org/postgresql/driverconfig.properties");
         return merged;
      } else {
         LOGGER.log(Level.FINE, "Loading driver configuration via classloader {0}", cl);
         ArrayList<URL> urls = new ArrayList<>();
         Enumeration<URL> urlEnum = cl.getResources("org/postgresql/driverconfig.properties");

         while (urlEnum.hasMoreElements()) {
            urls.add(urlEnum.nextElement());
         }

         for (int i = urls.size() - 1; i >= 0; i--) {
            URL url = urls.get(i);
            LOGGER.log(Level.FINE, "Loading driver configuration from: {0}", url);
            InputStream is = url.openStream();
            merged.load(is);
            is.close();
         }

         return merged;
      }
   }

   @Nullable
   @Override
   public Connection connect(String url, @Nullable Properties info) throws SQLException {
      if (url == null) {
         throw new SQLException("url is null");
      } else if (!url.startsWith("jdbc:postgresql:")) {
         return null;
      } else {
         Properties defaults;
         try {
            defaults = this.getDefaultProperties();
         } catch (IOException var9) {
            throw new PSQLException(GT.tr("Error loading default settings from driverconfig.properties"), PSQLState.UNEXPECTED_ERROR, var9);
         }

         Properties props = new Properties(defaults);
         if (info != null) {
            for (String propName : info.stringPropertyNames()) {
               String propValue = info.getProperty(propName);
               if (propValue == null) {
                  throw new PSQLException(GT.tr("Properties for the driver contains a non-string value for the key ") + propName, PSQLState.UNEXPECTED_ERROR);
               }

               props.setProperty(propName, propValue);
            }
         }

         if ((props = parseURL(url, props)) == null) {
            throw new PSQLException(GT.tr("Unable to parse URL {0}", url), PSQLState.UNEXPECTED_ERROR);
         } else {
            try {
               LOGGER.log(Level.FINE, "Connecting with URL: {0}", url);
               long timeout = timeout(props);
               if (timeout <= 0L) {
                  return makeConnection(url, props);
               } else {
                  Driver.ConnectThread ct = new Driver.ConnectThread(url, props);
                  Thread thread = new Thread(ct, "PostgreSQL JDBC driver connection thread");
                  thread.setDaemon(true);
                  thread.start();
                  return ct.getResult(timeout);
               }
            } catch (PSQLException var10) {
               LOGGER.log(Level.FINE, "Connection error: ", (Throwable)var10);
               throw var10;
            } catch (Exception var11) {
               if ("java.security.AccessControlException".equals(var11.getClass().getName())) {
                  throw new PSQLException(
                     GT.tr(
                        "Your security policy has prevented the connection from being attempted.  You probably need to grant the connect java.net.SocketPermission to the database server host and port that you wish to connect to."
                     ),
                     PSQLState.UNEXPECTED_ERROR,
                     var11
                  );
               } else {
                  LOGGER.log(Level.FINE, "Unexpected connection error: ", (Throwable)var11);
                  throw new PSQLException(
                     GT.tr("Something unusual has occurred to cause the driver to fail. Please report this exception."), PSQLState.UNEXPECTED_ERROR, var11
                  );
               }
            }
         }
      }
   }

   private void setupLoggerFromProperties(Properties props) {
   }

   private static Connection makeConnection(String url, Properties props) throws SQLException {
      return new PgConnection(hostSpecs(props), props, url);
   }

   @Override
   public boolean acceptsURL(String url) {
      return parseURL(url, null) != null;
   }

   @Override
   public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
      Properties copy = new Properties(info);
      Properties parse = parseURL(url, copy);
      if (parse != null) {
         copy = parse;
      }

      PGProperty[] knownProperties = PGProperty.values();
      DriverPropertyInfo[] props = new DriverPropertyInfo[knownProperties.length];

      for (int i = 0; i < props.length; i++) {
         props[i] = knownProperties[i].toDriverPropertyInfo(copy);
      }

      return props;
   }

   @Override
   public int getMajorVersion() {
      return 42;
   }

   @Override
   public int getMinorVersion() {
      return 7;
   }

   @Deprecated
   public static String getVersion() {
      return "PostgreSQL JDBC Driver 42.7.3";
   }

   @Override
   public boolean jdbcCompliant() {
      return false;
   }

   @Nullable
   public static Properties parseURL(String url, @Nullable Properties defaults) {
      Properties priority1Url = new Properties();
      Properties priority3Service = new Properties();
      String urlServer = url;
      String urlArgs = "";
      int qPos = url.indexOf(63);
      if (qPos != -1) {
         urlServer = url.substring(0, qPos);
         urlArgs = url.substring(qPos + 1);
      }

      if (!urlServer.startsWith("jdbc:postgresql:")) {
         LOGGER.log(Level.FINE, "JDBC URL must start with \"jdbc:postgresql:\" but was: {0}", url);
         return null;
      } else {
         urlServer = urlServer.substring("jdbc:postgresql:".length());
         if ("//".equals(urlServer) || "///".equals(urlServer)) {
            urlServer = "";
         } else if (urlServer.startsWith("//")) {
            urlServer = urlServer.substring(2);
            long slashCount = urlServer.chars().filter(ch -> ch == 47).count();
            if (slashCount > 1L) {
               LOGGER.log(Level.WARNING, "JDBC URL contains too many / characters: {0}", url);
               return null;
            }

            int slash = urlServer.indexOf(47);
            if (slash == -1) {
               LOGGER.log(Level.WARNING, "JDBC URL must contain a / at the end of the host or port: {0}", url);
               return null;
            }

            if (!urlServer.endsWith("/")) {
               String value = urlDecode(urlServer.substring(slash + 1));
               if (value == null) {
                  return null;
               }

               PGProperty.PG_DBNAME.set(priority1Url, value);
            }

            urlServer = urlServer.substring(0, slash);
            String[] addresses = urlServer.split(",");
            StringBuilder hosts = new StringBuilder();
            StringBuilder ports = new StringBuilder();

            for (String address : addresses) {
               int portIdx = address.lastIndexOf(58);
               if (portIdx != -1 && address.lastIndexOf(93) < portIdx) {
                  String portStr = address.substring(portIdx + 1);
                  ports.append(portStr);
                  CharSequence hostStr = address.subSequence(0, portIdx);
                  if (hostStr.length() == 0) {
                     hosts.append(PGProperty.PG_HOST.getDefaultValue());
                  } else {
                     hosts.append(hostStr);
                  }
               } else {
                  ports.append(PGProperty.PG_PORT.getDefaultValue());
                  hosts.append(address);
               }

               ports.append(',');
               hosts.append(',');
            }

            ports.setLength(ports.length() - 1);
            hosts.setLength(hosts.length() - 1);
            PGProperty.PG_HOST.set(priority1Url, hosts.toString());
            PGProperty.PG_PORT.set(priority1Url, ports.toString());
         } else {
            if (urlServer.startsWith("/")) {
               return null;
            }

            String value = urlDecode(urlServer);
            if (value == null) {
               return null;
            }

            priority1Url.setProperty(PGProperty.PG_DBNAME.getName(), value);
         }

         String[] args = urlArgs.split("&");
         String serviceName = null;

         for (String token : args) {
            if (!token.isEmpty()) {
               int pos = token.indexOf(61);
               if (pos == -1) {
                  priority1Url.setProperty(token, "");
               } else {
                  String pName = PGPropertyUtil.translatePGServiceToPGProperty(token.substring(0, pos));
                  String pValue = urlDecode(token.substring(pos + 1));
                  if (pValue == null) {
                     return null;
                  }

                  if (PGProperty.SERVICE.getName().equals(pName)) {
                     serviceName = pValue;
                  } else {
                     priority1Url.setProperty(pName, pValue);
                  }
               }
            }
         }

         if (serviceName != null) {
            LOGGER.log(Level.FINE, "Processing option [?service={0}]", serviceName);
            Properties result = PgServiceConfParser.getServiceProperties(serviceName);
            if (result == null) {
               LOGGER.log(Level.WARNING, "Definition of service [{0}] not found", serviceName);
               return null;
            }

            priority3Service.putAll(result);
         }

         Properties result = new Properties();
         result.putAll(priority1Url);
         if (defaults != null) {
            defaults.forEach(result::putIfAbsent);
         }

         priority3Service.forEach(result::putIfAbsent);
         if (defaults != null) {
            defaults.stringPropertyNames().forEach(s -> result.putIfAbsent(s, Nullness.castNonNull(defaults.getProperty(s))));
         }

         result.putIfAbsent(PGProperty.PG_PORT.getName(), Nullness.castNonNull(PGProperty.PG_PORT.getDefaultValue()));
         result.putIfAbsent(PGProperty.PG_HOST.getName(), Nullness.castNonNull(PGProperty.PG_HOST.getDefaultValue()));
         if (PGProperty.USER.getOrDefault(result) != null) {
            result.putIfAbsent(PGProperty.PG_DBNAME.getName(), Nullness.castNonNull(PGProperty.USER.getOrDefault(result)));
         }

         if (!PGPropertyUtil.propertiesConsistencyCheck(result)) {
            return null;
         } else {
            if (PGProperty.PASSWORD.getOrDefault(result) == null) {
               String password = PgPassParser.getPassword(
                  PGProperty.PG_HOST.getOrDefault(result),
                  PGProperty.PG_PORT.getOrDefault(result),
                  PGProperty.PG_DBNAME.getOrDefault(result),
                  PGProperty.USER.getOrDefault(result)
               );
               if (password != null && !password.isEmpty()) {
                  PGProperty.PASSWORD.set(result, password);
               }
            }

            return result;
         }
      }
   }

   @Nullable
   private static String urlDecode(String url) {
      try {
         return URLCoder.decode(url);
      } catch (IllegalArgumentException var2) {
         LOGGER.log(Level.FINE, "Url [{0}] parsing failed with error [{1}]", new Object[]{url, var2.getMessage()});
         return null;
      }
   }

   private static HostSpec[] hostSpecs(Properties props) {
      String[] hosts = Nullness.castNonNull(PGProperty.PG_HOST.getOrDefault(props)).split(",");
      String[] ports = Nullness.castNonNull(PGProperty.PG_PORT.getOrDefault(props)).split(",");
      String localSocketAddress = PGProperty.LOCAL_SOCKET_ADDRESS.getOrDefault(props);
      HostSpec[] hostSpecs = new HostSpec[hosts.length];

      for (int i = 0; i < hostSpecs.length; i++) {
         hostSpecs[i] = new HostSpec(hosts[i], Integer.parseInt(ports[i]), localSocketAddress);
      }

      return hostSpecs;
   }

   private static long timeout(Properties props) {
      String timeout = PGProperty.LOGIN_TIMEOUT.getOrDefault(props);
      if (timeout != null) {
         try {
            return (long)(Float.parseFloat(timeout) * 1000.0F);
         } catch (NumberFormatException var3) {
            LOGGER.log(Level.WARNING, "Couldn't parse loginTimeout value: {0}", timeout);
         }
      }

      return DriverManager.getLoginTimeout() * 1000L;
   }

   public static SQLFeatureNotSupportedException notImplemented(Class<?> callClass, String functionName) {
      return new SQLFeatureNotSupportedException(
         GT.tr("Method {0} is not yet implemented.", callClass.getName() + "." + functionName), PSQLState.NOT_IMPLEMENTED.getState()
      );
   }

   @Override
   public Logger getParentLogger() {
      return PARENT_LOGGER;
   }

   public static SharedTimer getSharedTimer() {
      return SHARED_TIMER;
   }

   public static void register() throws SQLException {
      if (isRegistered()) {
         throw new IllegalStateException("Driver is already registered. It can only be registered once.");
      } else {
         Driver registeredDriver = new Driver();
         DriverManager.registerDriver(registeredDriver);
         Driver.registeredDriver = registeredDriver;
      }
   }

   public static void deregister() throws SQLException {
      if (registeredDriver == null) {
         throw new IllegalStateException("Driver is not registered (or it has not been registered using Driver.register() method)");
      } else {
         DriverManager.deregisterDriver(registeredDriver);
         registeredDriver = null;
      }
   }

   public static boolean isRegistered() {
      return registeredDriver != null;
   }

   static {
      try {
         register();
      } catch (SQLException var1) {
         throw new ExceptionInInitializerError(var1);
      }
   }

   private static class ConnectThread implements Runnable {
      private final ResourceLock lock = new ResourceLock();
      private final Condition lockCondition = this.lock.newCondition();
      private final String url;
      private final Properties props;
      @Nullable
      private Connection result;
      @Nullable
      private Throwable resultException;
      private boolean abandoned;

      ConnectThread(String url, Properties props) {
         this.url = url;
         this.props = props;
      }

      @Override
      public void run() {
         Connection conn;
         Throwable error;
         try {
            conn = Driver.makeConnection(this.url, this.props);
            error = null;
         } catch (Throwable var8) {
            conn = null;
            error = var8;
         }

         ResourceLock ignore = this.lock.obtain();

         try {
            if (this.abandoned) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (SQLException var7) {
                  }
               }
            } else {
               this.result = conn;
               this.resultException = error;
               this.lockCondition.signal();
            }
         } catch (Throwable var9) {
            if (ignore != null) {
               try {
                  ignore.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (ignore != null) {
            ignore.close();
         }
      }

      public Connection getResult(long timeout) throws SQLException {
         long expiry = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) + timeout;
         ResourceLock ignore = this.lock.obtain();

         Connection var13;
         try {
            while (this.result == null) {
               Throwable resultException = this.resultException;
               if (resultException != null) {
                  if (resultException instanceof SQLException) {
                     resultException.fillInStackTrace();
                     throw (SQLException)resultException;
                  }

                  throw new PSQLException(
                     GT.tr("Something unusual has occurred to cause the driver to fail. Please report this exception."),
                     PSQLState.UNEXPECTED_ERROR,
                     resultException
                  );
               }

               long delay = expiry - TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
               if (delay <= 0L) {
                  this.abandoned = true;
                  throw new PSQLException(GT.tr("Connection attempt timed out."), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
               }

               try {
                  this.lockCondition.await(delay, TimeUnit.MILLISECONDS);
               } catch (InterruptedException var11) {
                  Thread.currentThread().interrupt();
                  this.abandoned = true;
                  throw new RuntimeException(GT.tr("Interrupted while attempting to connect."));
               }
            }

            var13 = this.result;
         } catch (Throwable var12) {
            if (ignore != null) {
               try {
                  ignore.close();
               } catch (Throwable var10) {
                  var12.addSuppressed(var10);
               }
            }

            throw var12;
         }

         if (ignore != null) {
            ignore.close();
         }

         return var13;
      }
   }
}
