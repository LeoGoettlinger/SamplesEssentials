package github.nighter.smartspawner.libs.mariadb.plugin;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.HostAddress;
import github.nighter.smartspawner.libs.mariadb.export.ExceptionFactory;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public interface TlsSocketPlugin {
   String type();

   TrustManager[] getTrustManager(Configuration var1, ExceptionFactory var2, HostAddress var3) throws SQLException;

   KeyManager[] getKeyManager(Configuration var1, ExceptionFactory var2) throws SQLException;

   default SSLSocketFactory getSocketFactory(Configuration conf, ExceptionFactory exceptionFactory, HostAddress hostAddress) throws SQLException {
      return newSslSocketFactory(this.getKeyManager(conf, exceptionFactory), this.getTrustManager(conf, exceptionFactory, hostAddress), exceptionFactory);
   }

   static SSLSocketFactory newSslSocketFactory(KeyManager[] keyManagers, TrustManager[] trustManagers, ExceptionFactory exceptionFactory) throws SQLException {
      try {
         SSLContext sslContext = SSLContext.getInstance("TLS");
         sslContext.init(keyManagers, trustManagers, null);
         return sslContext.getSocketFactory();
      } catch (KeyManagementException var4) {
         throw exceptionFactory.create("Could not initialize SSL context", "08000", var4);
      } catch (NoSuchAlgorithmException var5) {
         throw exceptionFactory.create("SSLContext TLS Algorithm not unknown", "08000", var5);
      }
   }

   default SSLSocket createSocket(Socket socket, SSLSocketFactory sslSocketFactory) throws IOException {
      return (SSLSocket)sslSocketFactory.createSocket(
         socket, socket.getInetAddress() == null ? null : socket.getInetAddress().getHostAddress(), socket.getPort(), true
      );
   }

   void verify(String var1, SSLSession var2, long var3) throws SSLException;
}
