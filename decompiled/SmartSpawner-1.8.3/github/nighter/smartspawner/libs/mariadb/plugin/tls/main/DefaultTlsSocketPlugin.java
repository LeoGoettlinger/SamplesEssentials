package github.nighter.smartspawner.libs.mariadb.plugin.tls.main;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.HostAddress;
import github.nighter.smartspawner.libs.mariadb.client.tls.HostnameVerifier;
import github.nighter.smartspawner.libs.mariadb.client.tls.MariaDbX509DeferredIdentityTrustManager;
import github.nighter.smartspawner.libs.mariadb.client.tls.MariaDbX509KeyManager;
import github.nighter.smartspawner.libs.mariadb.client.tls.MariaDbX509TrustingManager;
import github.nighter.smartspawner.libs.mariadb.export.ExceptionFactory;
import github.nighter.smartspawner.libs.mariadb.export.SslMode;
import github.nighter.smartspawner.libs.mariadb.plugin.TlsSocketPlugin;
import github.nighter.smartspawner.libs.mariadb.util.log.Logger;
import github.nighter.smartspawner.libs.mariadb.util.log.Loggers;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class DefaultTlsSocketPlugin implements TlsSocketPlugin {
   private static final Logger logger = Loggers.getLogger(DefaultTlsSocketPlugin.class);
   private static final ConcurrentHashMap<String, DefaultTlsSocketPlugin.CachedTrust> TRUST_CACHE = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<String, KeyManager[]> KEY_CACHE = new ConcurrentHashMap<>();
   private static final int MAX_CACHE_SIZE = 256;

   private static boolean cacheDisabled(Configuration conf) {
      return Boolean.parseBoolean(conf.nonMappedOptions().getProperty("disableSslContextCache", "false"));
   }

   private static KeyManager loadClientCerts(
      String keyStoreUrl, String keyStorePassword, String keyPassword, String storeType, ExceptionFactory exceptionFactory
   ) throws SQLException {
      try {
         InputStream inStream = loadFromUrl(keyStoreUrl);

         MariaDbX509KeyManager var9;
         try {
            char[] keyStorePasswordChars = keyStorePassword == null ? null : (keyStorePassword.isEmpty() ? null : keyStorePassword.toCharArray());
            char[] keyStoreChars = keyPassword == null ? keyStorePasswordChars : (keyPassword.isEmpty() ? null : keyPassword.toCharArray());
            KeyStore ks = KeyStore.getInstance(storeType != null ? storeType : KeyStore.getDefaultType());
            ks.load(inStream, keyStorePasswordChars);
            var9 = new MariaDbX509KeyManager(ks, keyStoreChars);
         } catch (Throwable var11) {
            if (inStream != null) {
               try {
                  inStream.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }
            }

            throw var11;
         }

         if (inStream != null) {
            inStream.close();
         }

         return var9;
      } catch (GeneralSecurityException | IOException var12) {
         throw exceptionFactory.create("Failed to read keyStore file. Option keyStore=" + keyStoreUrl, "08000", var12);
      }
   }

   private static InputStream loadFromUrl(String keyStoreUrl) throws FileNotFoundException {
      try {
         return new URI(keyStoreUrl).toURL().openStream();
      } catch (Exception var2) {
         return new FileInputStream(keyStoreUrl);
      }
   }

   private static InputStream getInputStreamFromPath(String path) throws IOException {
      try {
         return new URI(path).toURL().openStream();
      } catch (Exception var3) {
         if (path.startsWith("-----")) {
            return new ByteArrayInputStream(path.getBytes());
         } else {
            File f = new File(path);
            if (f.exists() && !f.isDirectory()) {
               return f.toURI().toURL().openStream();
            } else {
               throw new IOException(String.format("File not found for option `serverSslCert` (value: '%s')", path), var3);
            }
         }
      }
   }

   @Override
   public String type() {
      return "DEFAULT";
   }

   @Override
   public TrustManager[] getTrustManager(Configuration conf, ExceptionFactory exceptionFactory, HostAddress hostAddress) throws SQLException {
      if (cacheDisabled(conf)) {
         return this.buildTrustManager(conf, exceptionFactory, hostAddress);
      } else {
         String key = trustCacheKey(conf, hostAddress);
         DefaultTlsSocketPlugin.CachedTrust cached = TRUST_CACHE.get(key);
         if (cached == null) {
            cached = this.buildTrust(conf, exceptionFactory, hostAddress);
            if (TRUST_CACHE.size() >= 256) {
               TRUST_CACHE.clear();
            }

            DefaultTlsSocketPlugin.CachedTrust previous = TRUST_CACHE.putIfAbsent(key, cached);
            if (previous != null) {
               cached = previous;
            }
         }

         return wrapTrust(cached);
      }
   }

   TrustManager[] buildTrustManager(Configuration conf, ExceptionFactory exceptionFactory, HostAddress hostAddress) throws SQLException {
      return wrapTrust(this.buildTrust(conf, exceptionFactory, hostAddress));
   }

   private static TrustManager[] wrapTrust(DefaultTlsSocketPlugin.CachedTrust cachedTrust) {
      return new TrustManager[]{(TrustManager)(cachedTrust.deferIdentity ? new MariaDbX509DeferredIdentityTrustManager(cachedTrust.base) : cachedTrust.base)};
   }

   private DefaultTlsSocketPlugin.CachedTrust buildTrust(Configuration conf, ExceptionFactory exceptionFactory, HostAddress hostAddress) throws SQLException {
      SslMode sslMode = hostAddress.sslMode == null ? conf.sslMode() : hostAddress.sslMode;
      if (sslMode == SslMode.TRUST) {
         return new DefaultTlsSocketPlugin.CachedTrust(new MariaDbX509TrustingManager(), false);
      } else {
         if (conf.serverSslCert() != null || conf.trustStore() != null) {
            KeyStore ks;
            try {
               ks = KeyStore.getInstance(conf.trustStoreType() != null ? conf.trustStoreType() : KeyStore.getDefaultType());
            } catch (GeneralSecurityException var32) {
               throw exceptionFactory.create("Failed to create keystore instance", "08000", var32);
            }

            if (conf.trustStore() != null) {
               InputStream inStream;
               try {
                  inStream = loadFromUrl(conf.trustStore());
               } catch (IOException var31) {
                  try {
                     inStream = new FileInputStream(conf.trustStore());
                  } catch (FileNotFoundException var30) {
                     throw new SQLException("Failed to find trustStore file. trustStore=" + conf.trustStore(), "08000", var30);
                  }
               }

               try {
                  ks.load(inStream, conf.trustStorePassword() == null ? null : conf.trustStorePassword().toCharArray());
               } catch (NoSuchAlgorithmException | CertificateException | IOException var28) {
                  throw exceptionFactory.create("Failed load keyStore", "08000", var28);
               } finally {
                  try {
                     inStream.close();
                  } catch (IOException var27) {
                  }
               }

               try {
                  TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                  tmf.init(ks);

                  for (TrustManager tm : tmf.getTrustManagers()) {
                     if (tm instanceof X509TrustManager) {
                        return new DefaultTlsSocketPlugin.CachedTrust((X509TrustManager)tm, false);
                     }
                  }
               } catch (GeneralSecurityException var38) {
                  throw exceptionFactory.create("Failed to load certificates from serverSslCert/trustStore", "08000", var38);
               }
            } else {
               try {
                  InputStream inStream = getInputStreamFromPath(conf.serverSslCert());

                  try {
                     ks.load(null);
                     CertificateFactory cf = CertificateFactory.getInstance("X.509");

                     for (Certificate ca : cf.generateCertificates(inStream)) {
                        ks.setCertificateEntry(UUID.randomUUID().toString(), ca);
                     }
                  } catch (Throwable var35) {
                     if (inStream != null) {
                        try {
                           inStream.close();
                        } catch (Throwable var26) {
                           var35.addSuppressed(var26);
                        }
                     }

                     throw var35;
                  }

                  if (inStream != null) {
                     inStream.close();
                  }
               } catch (IOException var36) {
                  throw exceptionFactory.create("Failed load keyStore", "08000", var36);
               } catch (GeneralSecurityException var37) {
                  throw exceptionFactory.create("Failed to store certificate from serverSslCert into a keyStore", "08000", var37);
               }

               try {
                  TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                  tmf.init(ks);

                  for (TrustManager tmx : tmf.getTrustManagers()) {
                     if (tmx instanceof X509TrustManager) {
                        return new DefaultTlsSocketPlugin.CachedTrust((X509TrustManager)tmx, false);
                     }
                  }
               } catch (GeneralSecurityException var34) {
                  throw exceptionFactory.create("Failed to load certificates from serverSslCert/trustStore", "08000", var34);
               }
            }
         } else if (conf.fallbackToSystemTrustStore()) {
            try {
               TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
               tmf.init((KeyStore)null);

               for (TrustManager tmxx : tmf.getTrustManagers()) {
                  if (tmxx instanceof X509TrustManager) {
                     return new DefaultTlsSocketPlugin.CachedTrust((X509TrustManager)tmxx, true);
                  }
               }
            } catch (Exception var33) {
               throw new SQLException("No X509TrustManager found", var33);
            }
         }

         throw new SQLException("No X509TrustManager found");
      }
   }

   @Override
   public KeyManager[] getKeyManager(Configuration conf, ExceptionFactory exceptionFactory) throws SQLException {
      if (conf.keyStore() != null && !cacheDisabled(conf)) {
         String key = keyCacheKey(conf);
         KeyManager[] cached = KEY_CACHE.get(key);
         if (cached != null) {
            return cached;
         } else {
            KeyManager[] built = this.buildKeyManager(conf, exceptionFactory);
            if (built == null) {
               return null;
            } else {
               if (KEY_CACHE.size() >= 256) {
                  KEY_CACHE.clear();
               }

               KeyManager[] previous = KEY_CACHE.putIfAbsent(key, built);
               return previous != null ? previous : built;
            }
         }
      } else {
         return this.buildKeyManager(conf, exceptionFactory);
      }
   }

   KeyManager[] buildKeyManager(Configuration conf, ExceptionFactory exceptionFactory) throws SQLException {
      KeyManager[] keyManager = null;
      if (conf.keyStore() != null) {
         keyManager = new KeyManager[]{loadClientCerts(conf.keyStore(), conf.keyStorePassword(), conf.keyPassword(), conf.keyStoreType(), exceptionFactory)};
      } else if (conf.fallbackToSystemKeyStore()) {
         String keyStore = System.getProperty("javax.net.ssl.keyStore");
         String keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword", conf.keyStorePassword());
         String keyStoreType = System.getProperty("javax.net.ssl.keyStoreType", conf.keyStoreType());
         if (keyStore != null) {
            try {
               keyManager = new KeyManager[]{loadClientCerts(keyStore, keyStorePassword, keyStorePassword, keyStoreType, exceptionFactory)};
            } catch (SQLException var8) {
               keyManager = null;
               logger.error("Error loading key manager from system properties", var8);
            }
         }
      }

      return keyManager;
   }

   private static String trustCacheKey(Configuration conf, HostAddress hostAddress) {
      SslMode sslMode = hostAddress.sslMode == null ? conf.sslMode() : hostAddress.sslMode;
      StringBuilder sb = new StringBuilder();
      sb.append(sslMode == SslMode.TRUST ? "TRUST" : "VERIFY").append('\n');
      appendFile(sb, conf.serverSslCert());
      appendFile(sb, conf.trustStore());
      appendValue(sb, conf.trustStorePassword());
      appendValue(sb, conf.trustStoreType());
      sb.append(conf.fallbackToSystemTrustStore()).append('\n');
      appendFile(sb, System.getProperty("javax.net.ssl.trustStore"));
      appendValue(sb, System.getProperty("javax.net.ssl.trustStorePassword"));
      appendValue(sb, System.getProperty("javax.net.ssl.trustStoreType"));
      return sha256(sb.toString());
   }

   private static String keyCacheKey(Configuration conf) {
      StringBuilder sb = new StringBuilder();
      appendFile(sb, conf.keyStore());
      appendValue(sb, conf.keyStorePassword());
      appendValue(sb, conf.keyPassword());
      appendValue(sb, conf.keyStoreType());
      return sha256(sb.toString());
   }

   private static void appendValue(StringBuilder sb, String value) {
      sb.append(value == null ? "" : value).append('\n');
   }

   private static void appendFile(StringBuilder sb, String value) {
      appendValue(sb, value);
      sb.append(freshnessToken(value)).append('\n');
   }

   private static String freshnessToken(String value) {
      if (value != null && !value.startsWith("-----")) {
         File file = toFile(value);
         return file != null && file.isFile() ? file.lastModified() + ":" + file.length() : "";
      } else {
         return "";
      }
   }

   private static File toFile(String value) {
      try {
         URI uri = new URI(value);
         if ("file".equalsIgnoreCase(uri.getScheme())) {
            return new File(uri);
         }
      } catch (Exception var3) {
      }

      try {
         return new File(value);
      } catch (Exception var2) {
         return null;
      }
   }

   private static String sha256(String input) {
      try {
         MessageDigest md = MessageDigest.getInstance("SHA-256");
         byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
         StringBuilder hex = new StringBuilder(digest.length * 2);

         for (byte b : digest) {
            hex.append(Character.forDigit(b >> 4 & 15, 16)).append(Character.forDigit(b & 15, 16));
         }

         return hex.toString();
      } catch (NoSuchAlgorithmException var8) {
         return input;
      }
   }

   @Override
   public void verify(String host, SSLSession session, long serverThreadId) throws SSLException {
      try {
         Certificate[] certs = session.getPeerCertificates();
         X509Certificate cert = (X509Certificate)certs[0];
         HostnameVerifier.verify(host, cert, serverThreadId);
      } catch (SSLException var7) {
         logger.info(var7.getMessage(), var7);
         throw var7;
      }
   }

   private static final class CachedTrust {
      private final X509TrustManager base;
      private final boolean deferIdentity;

      private CachedTrust(X509TrustManager base, boolean deferIdentity) {
         this.base = base;
         this.deferIdentity = deferIdentity;
      }
   }
}
