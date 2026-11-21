package com.mysql.cj.protocol;

import com.mysql.cj.ServerVersion;
import com.mysql.cj.conf.PropertyDefinitions;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.FeatureNotAvailableException;
import com.mysql.cj.exceptions.RSAException;
import com.mysql.cj.exceptions.SSLParamsException;
import com.mysql.cj.log.Log;
import com.mysql.cj.util.Base64Decoder;
import com.mysql.cj.util.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class ExportControlled {
   private static final String SSL_CONTEXT_PROTOCOL = "TLS";
   private static final String TLSv1 = "TLSv1";
   private static final String TLSv1_1 = "TLSv1.1";
   private static final String TLSv1_2 = "TLSv1.2";
   private static final String TLSv1_3 = "TLSv1.3";
   private static final String[] KNOWN_TLS_PROTOCOLS = new String[]{"TLSv1.3", "TLSv1.2", "TLSv1.1", "TLSv1"};
   private static final String[] APPROVED_TLS_PROTOCOLS = new String[]{"TLSv1.3", "TLSv1.2"};
   private static final String TLS_SETTINGS_RESOURCE = "/com/mysql/cj/TlsSettings.properties";
   private static final List<String> ALLOWED_CIPHERS = new ArrayList<>();
   private static final List<String> UNACCEPTABLE_CIPHER_SUBSTR = new ArrayList<>();

   private ExportControlled() {
   }

   public static boolean enabled() {
      return true;
   }

   public static Socket performTlsHandshake(Socket rawSocket, SocketConnection socketConnection, ServerVersion serverVersion, Log log) throws IOException, SSLParamsException, FeatureNotAvailableException {
      PropertySet pset = socketConnection.getPropertySet();
      PropertyDefinitions.SslMode sslMode = pset.<PropertyDefinitions.SslMode>getEnumProperty(PropertyKey.sslMode).getValue();
      boolean fipsCompliantJsse = pset.getBooleanProperty(PropertyKey.fipsCompliantJsse).getValue();
      boolean verifyServerCert = fipsCompliantJsse
         || sslMode == PropertyDefinitions.SslMode.VERIFY_CA
         || sslMode == PropertyDefinitions.SslMode.VERIFY_IDENTITY;
      boolean fallbackToSystemTrustStore = pset.getBooleanProperty(PropertyKey.fallbackToSystemTrustStore).getValue();
      ExportControlled.KeyStoreConfigurations trustStoreConfigs = !verifyServerCert
         ? new ExportControlled.KeyStoreConfigurations()
         : getTrustStoreConfigurations(pset);
      if (serverVersion == null && verifyServerCert && !fallbackToSystemTrustStore && StringUtils.isNullOrEmpty(trustStoreConfigs.keyStoreUrl)) {
         throw new CJCommunicationsException("No truststore provided to verify the Server certificate.");
      } else {
         ExportControlled.SslContextBuilder sslContextBuilder = new ExportControlled.SslContextBuilder();
         sslContextBuilder.setKeyStoreSettings(getKeyStoreConfigurations(pset));
         sslContextBuilder.setTrustStoreSettings(trustStoreConfigs);
         sslContextBuilder.setVerifyServerCertificate(verifyServerCert);
         sslContextBuilder.setFallbackToSystemTrustStore(fallbackToSystemTrustStore);
         sslContextBuilder.setFipsCompliantJsse(fipsCompliantJsse);
         sslContextBuilder.setKeyManagerFactoryProvider(pset.getStringProperty(PropertyKey.keyManagerFactoryProvider).getValue());
         sslContextBuilder.setTrustManagerFactoryProvider(pset.getStringProperty(PropertyKey.trustManagerFactoryProvider).getValue());
         sslContextBuilder.setKeyStoreProvider(pset.getStringProperty(PropertyKey.keyStoreProvider).getValue());
         sslContextBuilder.setSslContextProvider(pset.getStringProperty(PropertyKey.sslContextProvider).getValue());
         sslContextBuilder.setExceptionInterceptor(socketConnection.getExceptionInterceptor());
         SSLContext sslContext = sslContextBuilder.build();
         SSLSocketFactory socketFactory = sslContext.getSocketFactory();
         SSLSocket sslSocket = (SSLSocket)socketFactory.createSocket(rawSocket, socketConnection.getHost(), socketConnection.getPort(), true);
         String[] allowedProtocols = getAllowedProtocols(pset, sslSocket.getSupportedProtocols());
         sslSocket.setEnabledProtocols(allowedProtocols);
         String[] allowedCiphers = getAllowedCiphers(pset, Arrays.asList(sslSocket.getEnabledCipherSuites()));
         if (allowedCiphers != null) {
            sslSocket.setEnabledCipherSuites(allowedCiphers);
         }

         sslSocket.startHandshake();
         if (sslMode == PropertyDefinitions.SslMode.VERIFY_IDENTITY) {
            String hostname = socketConnection.getHost();
            if (!StringUtils.isNullOrEmpty(hostname)) {
               ExportControlled.HostnameChecker hostnameChecker = new ExportControlled.HostnameChecker(socketConnection.getHost());
               SSLSession session = sslSocket.getSession();
               Certificate[] peerCerts = session.getPeerCertificates();
               if (!(peerCerts[0] instanceof X509Certificate)) {
                  throw (SSLParamsException)ExceptionFactory.createException(
                     SSLParamsException.class, "Server identity verification failed. Could not read Server's X.509 Certificate."
                  );
               }

               X509Certificate peerCert = (X509Certificate)peerCerts[0];

               try {
                  hostnameChecker.match(peerCert);
               } catch (CertificateException var22) {
                  throw new IOException(var22);
               }
            }
         }

         return sslSocket;
      }
   }

   public static boolean isSSLEstablished(Socket socket) {
      return socket == null ? false : SSLSocket.class.isAssignableFrom(socket.getClass());
   }

   public static RSAPublicKey decodeRSAPublicKey(String key) throws RSAException {
      if (key == null) {
         throw (RSAException)ExceptionFactory.createException(RSAException.class, "Key parameter is null");
      } else {
         int offset = key.indexOf("\n") + 1;
         int len = key.indexOf("-----END PUBLIC KEY-----") - offset;
         byte[] certificateData = Base64Decoder.decode(key.getBytes(), offset, len);
         X509EncodedKeySpec spec = new X509EncodedKeySpec(certificateData);

         try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey)kf.generatePublic(spec);
         } catch (InvalidKeySpecException | NoSuchAlgorithmException var6) {
            throw (RSAException)ExceptionFactory.createException(RSAException.class, "Unable to decode public key", var6);
         }
      }
   }

   public static byte[] encryptWithRSAPublicKey(byte[] source, RSAPublicKey key, String transformation) throws RSAException {
      try {
         Cipher cipher = Cipher.getInstance(transformation);
         cipher.init(1, key);
         return cipher.doFinal(source);
      } catch (NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException var4) {
         throw (RSAException)ExceptionFactory.createException(RSAException.class, var4.getMessage(), var4);
      }
   }

   public static byte[] encryptWithRSAPublicKey(byte[] source, RSAPublicKey key) throws RSAException {
      return encryptWithRSAPublicKey(source, key, "RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
   }

   public static RSAPrivateKey decodeRSAPrivateKey(String key) throws RSAException {
      if (key == null) {
         throw (RSAException)ExceptionFactory.createException(RSAException.class, "Key parameter is null");
      } else {
         String keyData = key.replace("-----BEGIN PRIVATE KEY-----", "").replaceAll("\\R", "").replace("-----END PRIVATE KEY-----", "");
         byte[] decodedKeyData = Base64.getDecoder().decode(keyData);

         try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey)keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedKeyData));
         } catch (InvalidKeySpecException | NoSuchAlgorithmException var4) {
            throw (RSAException)ExceptionFactory.createException(RSAException.class, "Unable to decode private key", var4);
         }
      }
   }

   public static byte[] sign(byte[] source, RSAPrivateKey privateKey) throws RSAException {
      try {
         Signature signature = Signature.getInstance("SHA256withRSA");
         signature.initSign(privateKey);
         signature.update(source);
         return signature.sign();
      } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException var3) {
         throw (RSAException)ExceptionFactory.createException(RSAException.class, var3.getMessage(), var3);
      }
   }

   private static ExportControlled.KeyStoreConfigurations getKeyStoreConfigurations(PropertySet propertySet) {
      String keyStoreUrl = propertySet.getStringProperty(PropertyKey.clientCertificateKeyStoreUrl).getValue();
      String keyStorePassword = propertySet.getStringProperty(PropertyKey.clientCertificateKeyStorePassword).getValue();
      String keyStoreType = propertySet.getStringProperty(PropertyKey.clientCertificateKeyStoreType).getValue();
      boolean fallbackToSystemKeyStore = propertySet.getBooleanProperty(PropertyKey.fallbackToSystemKeyStore).getValue();
      if (fallbackToSystemKeyStore && StringUtils.isNullOrEmpty(keyStoreUrl)) {
         keyStoreUrl = System.getProperty("javax.net.ssl.keyStore");
         keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
         keyStoreType = System.getProperty("javax.net.ssl.keyStoreType");
         if (StringUtils.isNullOrEmpty(keyStoreType)) {
            keyStoreType = propertySet.getStringProperty(PropertyKey.clientCertificateKeyStoreType).getInitialValue();
         }

         if (!StringUtils.isNullOrEmpty(keyStoreUrl)) {
            try {
               new URL(keyStoreUrl);
            } catch (MalformedURLException var6) {
               keyStoreUrl = "file:" + keyStoreUrl;
            }
         }
      }

      return new ExportControlled.KeyStoreConfigurations(keyStoreUrl, keyStorePassword, keyStoreType);
   }

   private static ExportControlled.KeyStoreConfigurations getTrustStoreConfigurations(PropertySet propertySet) {
      String trustStoreUrl = propertySet.getStringProperty(PropertyKey.trustCertificateKeyStoreUrl).getValue();
      String trustStorePassword = propertySet.getStringProperty(PropertyKey.trustCertificateKeyStorePassword).getValue();
      String trustStoreType = propertySet.getStringProperty(PropertyKey.trustCertificateKeyStoreType).getValue();
      boolean fallbackToSystemTrustStore = propertySet.getBooleanProperty(PropertyKey.fallbackToSystemTrustStore).getValue();
      if (fallbackToSystemTrustStore && StringUtils.isNullOrEmpty(trustStoreUrl)) {
         trustStoreUrl = System.getProperty("javax.net.ssl.trustStore");
         trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
         trustStoreType = System.getProperty("javax.net.ssl.trustStoreType");
         if (StringUtils.isNullOrEmpty(trustStoreType)) {
            trustStoreType = propertySet.getStringProperty(PropertyKey.trustCertificateKeyStoreType).getInitialValue();
         }

         if (!StringUtils.isNullOrEmpty(trustStoreUrl)) {
            try {
               new URL(trustStoreUrl);
            } catch (MalformedURLException var6) {
               trustStoreUrl = "file:" + trustStoreUrl;
            }
         }
      }

      return new ExportControlled.KeyStoreConfigurations(trustStoreUrl, trustStorePassword, trustStoreType);
   }

   private static String[] getAllowedProtocols(PropertySet pset, String[] socketProtocols) {
      List<String> tryProtocols = null;
      RuntimeProperty<String> tlsVersions = pset.getStringProperty(PropertyKey.tlsVersions);
      if (tlsVersions != null && tlsVersions.isExplicitlySet()) {
         if (tlsVersions.getValue() == null) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class, "Specified list of TLS versions is empty. Accepted values are TLSv1.2 and TLSv1.3."
            );
         }

         tryProtocols = getValidProtocols(tlsVersions.getValue().split("\\s*,\\s*"));
      } else {
         tryProtocols = new ArrayList<>(Arrays.asList(APPROVED_TLS_PROTOCOLS));
      }

      List<String> jvmSupportedProtocols = Arrays.asList(socketProtocols);
      List<String> allowedProtocols = new ArrayList<>();

      for (String protocol : tryProtocols) {
         if (jvmSupportedProtocols.contains(protocol)) {
            allowedProtocols.add(protocol);
         }
      }

      return allowedProtocols.toArray(new String[0]);
   }

   public static void checkValidProtocols(List<String> protocols) {
      getValidProtocols(protocols.toArray(new String[0]));
   }

   private static List<String> getValidProtocols(String[] protocols) {
      List<String> requestedProtocols = Arrays.stream(protocols).filter(p -> !StringUtils.isNullOrEmpty(p.trim())).collect(Collectors.toList());
      if (requestedProtocols.size() == 0) {
         throw (SSLParamsException)ExceptionFactory.createException(
            SSLParamsException.class, "Specified list of TLS versions is empty. Accepted values are TLSv1.2 and TLSv1.3."
         );
      } else {
         List<String> sanitizedProtocols = Stream.of(KNOWN_TLS_PROTOCOLS).filter(requestedProtocols::contains).collect(Collectors.toList());
         if (sanitizedProtocols.size() == 0) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class, "Specified list of TLS versions only contains non valid TLS protocols. Accepted values are TLSv1.2 and TLSv1.3."
            );
         } else {
            List<String> validProtocols = Stream.of(APPROVED_TLS_PROTOCOLS).filter(sanitizedProtocols::contains).collect(Collectors.toList());
            if (validProtocols.size() == 0) {
               throw (SSLParamsException)ExceptionFactory.createException(
                  SSLParamsException.class, "TLS protocols TLSv1 and TLSv1.1 are not supported. Accepted values are TLSv1.2 and TLSv1.3."
               );
            } else {
               return validProtocols;
            }
         }
      }
   }

   private static String[] getAllowedCiphers(PropertySet pset, List<String> socketCipherSuites) {
      String enabledSSLCipherSuites = pset.getStringProperty(PropertyKey.tlsCiphersuites).getValue();
      Stream<String> filterStream = StringUtils.isNullOrEmpty(enabledSSLCipherSuites)
         ? socketCipherSuites.stream()
         : Arrays.stream(enabledSSLCipherSuites.split("\\s*,\\s*")).filter(socketCipherSuites::contains);
      List<String> allowedCiphers = filterStream.filter(ALLOWED_CIPHERS::contains)
         .filter(c -> !UNACCEPTABLE_CIPHER_SUBSTR.stream().filter(c::contains).findFirst().isPresent())
         .collect(Collectors.toList());
      return allowedCiphers.toArray(new String[0]);
   }

   static {
      try {
         Properties tlsSettings = new Properties();
         tlsSettings.load(ExportControlled.class.getResourceAsStream("/com/mysql/cj/TlsSettings.properties"));
         Arrays.stream(tlsSettings.getProperty("TLSCiphers.Mandatory").split("\\s*,\\s*")).forEach(s -> {
            ALLOWED_CIPHERS.add("TLS_" + s.trim());
            ALLOWED_CIPHERS.add("SSL_" + s.trim());
         });
         Arrays.stream(tlsSettings.getProperty("TLSCiphers.Approved").split("\\s*,\\s*")).forEach(s -> {
            ALLOWED_CIPHERS.add("TLS_" + s.trim());
            ALLOWED_CIPHERS.add("SSL_" + s.trim());
         });
         Arrays.stream(tlsSettings.getProperty("TLSCiphers.Deprecated").split("\\s*,\\s*")).forEach(s -> {
            ALLOWED_CIPHERS.add("TLS_" + s.trim());
            ALLOWED_CIPHERS.add("SSL_" + s.trim());
         });
         Arrays.stream(tlsSettings.getProperty("TLSCiphers.Unacceptable.Mask").split("\\s*,\\s*")).forEach(s -> UNACCEPTABLE_CIPHER_SUBSTR.add(s.trim()));
      } catch (IOException var1) {
         throw ExceptionFactory.createException("Unable to load TlsSettings.properties");
      }
   }

   private static class HostnameChecker {
      private String hostname;

      public HostnameChecker(String hostname) {
         this.hostname = hostname;
      }

      public void match(X509Certificate certificate) throws CertificateException {
         boolean hostNameVerified = false;
         Collection<List<?>> subjectAltNames = null;
         subjectAltNames = certificate.getSubjectAlternativeNames();
         if (subjectAltNames != null) {
            boolean sanVerification = false;

            for (List<?> san : subjectAltNames) {
               Integer nameType = (Integer)san.get(0);
               if (nameType == 2) {
                  sanVerification = true;
                  if (this.verifyHostName((String)san.get(1))) {
                     hostNameVerified = true;
                     break;
                  }
               } else if (nameType == 7) {
                  sanVerification = true;
                  if (this.hostname.equalsIgnoreCase((String)san.get(1))) {
                     hostNameVerified = true;
                     break;
                  }
               }
            }

            if (sanVerification && !hostNameVerified) {
               throw new CertificateException(
                  "Server identity verification failed. None of the certificate's DNS or IP Subject Alternative Name entries matched the server hostname/IP '"
                     + this.hostname
                     + "'."
               );
            }
         }

         if (!hostNameVerified) {
            String dn = certificate.getSubjectX500Principal().getName("RFC2253");
            String cn = null;

            try {
               LdapName ldapDN = new LdapName(dn);
               cn = ldapDN.getRdns().stream().filter(r -> r.getType().equalsIgnoreCase("CN")).findFirst().get().getValue().toString();
            } catch (InvalidNameException var8) {
               throw new CertificateException("Failed to retrieve the Common Name (CN) from the server certificate.", var8);
            }

            if (!this.verifyHostName(cn)) {
               throw new CertificateException(
                  "Server identity verification failed. The certificate's Common Name '" + cn + "' does not match '" + this.hostname + "'."
               );
            }
         }
      }

      private boolean verifyHostName(String ptn) {
         int indexOfStar = ptn.indexOf(42);
         if (indexOfStar >= 0 && indexOfStar < ptn.indexOf(46)) {
            String head = ptn.substring(0, indexOfStar);
            String tail = ptn.substring(indexOfStar + 1);
            return StringUtils.startsWithIgnoreCase(this.hostname, head)
               && StringUtils.endsWithIgnoreCase(this.hostname.substring(head.length()), tail)
               && this.hostname.substring(head.length(), this.hostname.length() - tail.length()).indexOf(46) == -1;
         } else {
            return this.hostname.equalsIgnoreCase(ptn);
         }
      }
   }

   private static class KeyStoreConfigurations {
      public String keyStoreUrl = null;
      public String keyStorePassword = null;
      public String keyStoreType = KeyStore.getDefaultType();

      public KeyStoreConfigurations() {
      }

      public KeyStoreConfigurations(String keyStoreUrl, String keyStorePassword, String keyStoreType) {
         this.keyStoreUrl = keyStoreUrl;
         this.keyStorePassword = keyStorePassword;
         this.keyStoreType = keyStoreType;
      }
   }

   private static class SslContextBuilder {
      private ExportControlled.KeyStoreConfigurations keyStoreSettings = null;
      private ExportControlled.KeyStoreConfigurations trustStoreSettings = null;
      private boolean verifyServerCertificate = true;
      private boolean fallbackToSystemTrustStore = true;
      private boolean fipsCompliantJsse = false;
      private String KeyManagerFactoryProvider = null;
      private String trustManagerFactoryProvider = null;
      private String keyStoreProvider = null;
      private String sslContextProvider = null;
      private ExceptionInterceptor exceptionInterceptor = null;

      public SslContextBuilder() {
      }

      public void setKeyStoreSettings(ExportControlled.KeyStoreConfigurations keyStoreSettings) {
         this.keyStoreSettings = keyStoreSettings;
      }

      public void setTrustStoreSettings(ExportControlled.KeyStoreConfigurations keyStoreSettings) {
         this.trustStoreSettings = keyStoreSettings;
      }

      public void setVerifyServerCertificate(boolean verifyServerCertificate) {
         this.verifyServerCertificate = verifyServerCertificate;
      }

      public void setFallbackToSystemTrustStore(boolean fallbackToDefaultTrustStore) {
         this.fallbackToSystemTrustStore = fallbackToDefaultTrustStore;
      }

      public void setFipsCompliantJsse(boolean fipsCompliantJsse) {
         this.fipsCompliantJsse = fipsCompliantJsse;
      }

      public void setKeyManagerFactoryProvider(String keyManagerFactoryProvider) {
         this.KeyManagerFactoryProvider = keyManagerFactoryProvider;
      }

      public void setTrustManagerFactoryProvider(String trustManagerFactoryProvider) {
         this.trustManagerFactoryProvider = trustManagerFactoryProvider;
      }

      public void setKeyStoreProvider(String keyStoreProvider) {
         this.keyStoreProvider = keyStoreProvider;
      }

      public void setSslContextProvider(String sslContextProvider) {
         this.sslContextProvider = sslContextProvider;
      }

      public void setExceptionInterceptor(ExceptionInterceptor exceptionInterceptor) {
         this.exceptionInterceptor = exceptionInterceptor;
      }

      public SSLContext build() {
         KeyManagerFactory kmf = null;
         KeyManager[] kms = null;
         TrustManagerFactory tmf = null;
         TrustManager[] tms = new TrustManager[0];

         try {
            kmf = StringUtils.isNullOrEmpty(this.KeyManagerFactoryProvider)
               ? KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
               : KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm(), this.KeyManagerFactoryProvider);
            tmf = StringUtils.isNullOrEmpty(this.trustManagerFactoryProvider)
               ? TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
               : TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm(), this.trustManagerFactoryProvider);
         } catch (NoSuchAlgorithmException var72) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class,
               "Default algorithm for TrustManager or KeyManager is invalid. Check java security properties file.",
               var72,
               this.exceptionInterceptor
            );
         } catch (NoSuchProviderException var73) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class,
               "Specified TrustManager or KeyManager Provider is invalid. Ensure it is property registered.",
               var73,
               this.exceptionInterceptor
            );
         }

         if (!StringUtils.isNullOrEmpty(this.keyStoreSettings.keyStoreUrl)) {
            InputStream ksIS = null;

            try {
               if (!StringUtils.isNullOrEmpty(this.keyStoreSettings.keyStoreType)) {
                  KeyStore clientKeyStore = StringUtils.isNullOrEmpty(this.keyStoreProvider)
                     ? KeyStore.getInstance(this.keyStoreSettings.keyStoreType)
                     : KeyStore.getInstance(this.keyStoreSettings.keyStoreType, this.keyStoreProvider);
                  URL ksURL = new URL(this.keyStoreSettings.keyStoreUrl);
                  char[] password = this.keyStoreSettings.keyStorePassword == null ? new char[0] : this.keyStoreSettings.keyStorePassword.toCharArray();
                  ksIS = ksURL.openStream();
                  clientKeyStore.load(ksIS, password);
                  kmf.init(clientKeyStore, password);
                  kms = kmf.getKeyManagers();
               }
            } catch (UnrecoverableKeyException var65) {
               throw (SSLParamsException)ExceptionFactory.createException(
                  SSLParamsException.class, "Could not recover keys from client keystore.  Check password?", var65, this.exceptionInterceptor
               );
            } catch (NoSuchAlgorithmException var66) {
               throw (SSLParamsException)ExceptionFactory.createException(
                  SSLParamsException.class, "Unsupported keystore algorithm [" + var66.getMessage() + "]", var66, this.exceptionInterceptor
               );
            } catch (NoSuchProviderException var67) {
               throw (SSLParamsException)ExceptionFactory.createException(
                  SSLParamsException.class, "Specified KeyStore Provider is invalid. Ensure it is property registered.", var67, this.exceptionInterceptor
               );
            } catch (KeyStoreException var68) {
               throw (SSLParamsException)ExceptionFactory.createException(
                  SSLParamsException.class, "Could not create KeyStore instance [" + var68.getMessage() + "]", var68, this.exceptionInterceptor
               );
            } catch (CertificateException var69) {
               throw (SSLParamsException)ExceptionFactory.createException(
                  SSLParamsException.class,
                  "Could not load client" + this.keyStoreSettings.keyStoreType + " keystore from " + this.keyStoreSettings.keyStoreUrl,
                  var69,
                  this.exceptionInterceptor
               );
            } catch (MalformedURLException var70) {
               throw (SSLParamsException)ExceptionFactory.createException(
                  SSLParamsException.class, this.keyStoreSettings.keyStoreUrl + " does not appear to be a valid URL.", var70, this.exceptionInterceptor
               );
            } catch (IOException var71) {
               throw (SSLParamsException)ExceptionFactory.createException(
                  SSLParamsException.class,
                  "Cannot open " + this.keyStoreSettings.keyStoreUrl + " [" + var71.getMessage() + "]",
                  var71,
                  this.exceptionInterceptor
               );
            } finally {
               if (ksIS != null) {
                  try {
                     ksIS.close();
                  } catch (IOException var61) {
                  }
               }
            }
         }

         InputStream trustStoreIS = null;
         boolean x509TrustManagerFound = false;

         try {
            if (this.verifyServerCertificate) {
               KeyStore trustKeyStore = null;
               if (!StringUtils.isNullOrEmpty(this.trustStoreSettings.keyStoreUrl) && !StringUtils.isNullOrEmpty(this.trustStoreSettings.keyStoreType)) {
                  char[] trustStorePassword = this.trustStoreSettings.keyStorePassword == null
                     ? new char[0]
                     : this.trustStoreSettings.keyStorePassword.toCharArray();
                  trustStoreIS = new URL(this.trustStoreSettings.keyStoreUrl).openStream();
                  trustKeyStore = StringUtils.isNullOrEmpty(this.keyStoreProvider)
                     ? KeyStore.getInstance(this.trustStoreSettings.keyStoreType)
                     : KeyStore.getInstance(this.trustStoreSettings.keyStoreType, this.keyStoreProvider);
                  trustKeyStore.load(trustStoreIS, trustStorePassword);
               }

               if (trustKeyStore != null || this.fallbackToSystemTrustStore) {
                  tmf.init(trustKeyStore);
                  tms = tmf.getTrustManagers();

                  for (int i = 0; i < tms.length; i++) {
                     if (tms[i] instanceof X509TrustManager) {
                        x509TrustManagerFound = true;
                        if (!this.fipsCompliantJsse) {
                           tms[i] = new ExportControlled.X509TrustManagerWrapper((X509TrustManager)tms[i]);
                        }
                     }
                  }
               }
            }

            if (tms.length == 0 && !this.fipsCompliantJsse) {
               tms = new TrustManager[]{new ExportControlled.X509TrustManagerWrapper()};
            }
         } catch (MalformedURLException var75) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class, this.trustStoreSettings.keyStoreUrl + " does not appear to be a valid URL.", var75, this.exceptionInterceptor
            );
         } catch (NoSuchAlgorithmException var76) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class, "Unsupported keystore algorithm [" + var76.getMessage() + "]", var76, this.exceptionInterceptor
            );
         } catch (NoSuchProviderException var77) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class, "Specified KeyStore Provider is invalid. Ensure it is property registered.", var77, this.exceptionInterceptor
            );
         } catch (KeyStoreException var78) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class, "Could not create KeyStore instance [" + var78.getMessage() + "]", var78, this.exceptionInterceptor
            );
         } catch (CertificateException var79) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class,
               "Could not load trust" + this.trustStoreSettings.keyStoreType + " keystore from " + this.trustStoreSettings.keyStoreUrl,
               var79,
               this.exceptionInterceptor
            );
         } catch (IOException var80) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class,
               "Cannot open " + this.trustStoreSettings.keyStoreUrl + " [" + var80.getMessage() + "]",
               var80,
               this.exceptionInterceptor
            );
         } finally {
            if (trustStoreIS != null) {
               try {
                  trustStoreIS.close();
               } catch (IOException var60) {
               }
            }
         }

         if (this.verifyServerCertificate && !x509TrustManagerFound) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class, "Failed setting up server certificate validation because no X.509 Trust Manager was found.", this.exceptionInterceptor
            );
         } else {
            try {
               SSLContext sslContext = StringUtils.isNullOrEmpty(this.sslContextProvider)
                  ? SSLContext.getInstance("TLS")
                  : SSLContext.getInstance("TLS", this.sslContextProvider);
               sslContext.init(kms, tms, null);
               return sslContext;
            } catch (NoSuchAlgorithmException var62) {
               throw new SSLParamsException("TLS is not a valid protocol.", var62);
            } catch (NoSuchProviderException var63) {
               throw (SSLParamsException)ExceptionFactory.createException(
                  SSLParamsException.class, "Specified SSLContext Provider is invalid. Ensure it is property registered.", var63, this.exceptionInterceptor
               );
            } catch (KeyManagementException var64) {
               throw new SSLParamsException("KeyManagementException: " + var64.getMessage(), var64);
            }
         }
      }
   }

   public static class X509TrustManagerWrapper implements X509TrustManager {
      private static final String CERT_PATH_VALIDATOR_ALGORITHM = "PKIX";
      private static final String CERT_FACTORY_TYPE = "X.509";
      private boolean validateServerCert = false;
      private X509TrustManager originalTrustManager = null;
      private CertificateFactory certFactory = null;
      private PKIXParameters pkixParams = null;
      private CertPathValidator certPathValidator = null;

      X509TrustManagerWrapper(X509TrustManager tm) throws CertificateException {
         this.originalTrustManager = tm;
         this.validateServerCert = this.originalTrustManager != null;
         if (this.validateServerCert) {
            try {
               Set<TrustAnchor> anch = Arrays.stream(tm.getAcceptedIssuers()).map(c -> new TrustAnchor(c, null)).collect(Collectors.toSet());
               this.pkixParams = new PKIXParameters(anch);
               this.pkixParams.setRevocationEnabled(false);
               this.certPathValidator = CertPathValidator.getInstance("PKIX");
               this.certFactory = CertificateFactory.getInstance("X.509");
            } catch (Exception var3) {
               throw new CertificateException(var3);
            }
         }
      }

      X509TrustManagerWrapper() throws CertificateException {
         this(null);
      }

      @Override
      public X509Certificate[] getAcceptedIssuers() {
         return this.originalTrustManager != null ? this.originalTrustManager.getAcceptedIssuers() : new X509Certificate[0];
      }

      @Override
      public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
         for (int i = 0; i < chain.length; i++) {
            chain[i].checkValidity();
         }

         if (this.validateServerCert) {
            X509Certificate serverCert = chain[0];
            X509CertSelector certSelect = new X509CertSelector();
            certSelect.setSerialNumber(serverCert.getSerialNumber());

            try {
               CertPath certPath = this.certFactory.generateCertPath(Arrays.asList(chain));
               CertPathValidatorResult result = this.certPathValidator.validate(certPath, this.pkixParams);
               ((PKIXCertPathValidatorResult)result).getTrustAnchor().getTrustedCert().checkValidity();
            } catch (CertPathValidatorException | InvalidAlgorithmParameterException var7) {
               throw new CertificateException(var7);
            }

            this.originalTrustManager.checkServerTrusted(chain, authType);
         }
      }

      @Override
      public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
         this.originalTrustManager.checkClientTrusted(chain, authType);
      }
   }
}
