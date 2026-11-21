package org.postgresql.ssl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.UUID;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.postgresql.util.GT;

public class SingleCertValidatingFactory extends WrappedFactory {
   private static final String FILE_PREFIX = "file:";
   private static final String CLASSPATH_PREFIX = "classpath:";
   private static final String ENV_PREFIX = "env:";
   private static final String SYS_PROP_PREFIX = "sys:";

   public SingleCertValidatingFactory(String sslFactoryArg) throws GeneralSecurityException {
      if (sslFactoryArg != null && !"".equals(sslFactoryArg)) {
         InputStream in = null;

         try {
            if (sslFactoryArg.startsWith("file:")) {
               String path = sslFactoryArg.substring("file:".length());
               in = new BufferedInputStream(new FileInputStream(path));
            } else if (sslFactoryArg.startsWith("classpath:")) {
               String path = sslFactoryArg.substring("classpath:".length());
               ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
               InputStream inputStream;
               if (classLoader != null) {
                  inputStream = classLoader.getResourceAsStream(path);
                  if (inputStream == null) {
                     throw new IllegalArgumentException(GT.tr("Unable to find resource {0} via Thread contextClassLoader {1}", path, classLoader));
                  }
               } else {
                  inputStream = this.getClass().getResourceAsStream(path);
                  if (inputStream == null) {
                     throw new IllegalArgumentException(
                        GT.tr("Unable to find resource {0} via class {1} ClassLoader {2}", path, this.getClass(), this.getClass().getClassLoader())
                     );
                  }
               }

               in = new BufferedInputStream(inputStream);
            } else if (sslFactoryArg.startsWith("env:")) {
               String name = sslFactoryArg.substring("env:".length());
               String cert = System.getenv(name);
               if (cert == null || "".equals(cert)) {
                  throw new GeneralSecurityException(GT.tr("The environment variable containing the server's SSL certificate must not be empty."));
               }

               in = new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8));
            } else if (sslFactoryArg.startsWith("sys:")) {
               String name = sslFactoryArg.substring("sys:".length());
               String cert = System.getProperty(name);
               if (cert == null || "".equals(cert)) {
                  throw new GeneralSecurityException(GT.tr("The system property containing the server's SSL certificate must not be empty."));
               }

               in = new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8));
            } else {
               if (!sslFactoryArg.startsWith("-----BEGIN CERTIFICATE-----")) {
                  throw new GeneralSecurityException(
                     GT.tr("The sslfactoryarg property must start with the prefix file:, classpath:, env:, sys:, or -----BEGIN CERTIFICATE-----.")
                  );
               }

               in = new ByteArrayInputStream(sslFactoryArg.getBytes(StandardCharsets.UTF_8));
            }

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{new SingleCertValidatingFactory.SingleCertTrustManager(in)}, null);
            this.factory = ctx.getSocketFactory();
         } catch (RuntimeException var14) {
            throw var14;
         } catch (Exception var15) {
            if (var15 instanceof GeneralSecurityException) {
               throw (GeneralSecurityException)var15;
            }

            throw new GeneralSecurityException(GT.tr("An error occurred reading the certificate"), var15);
         } finally {
            if (in != null) {
               try {
                  in.close();
               } catch (Exception var13) {
               }
            }
         }
      } else {
         throw new GeneralSecurityException(GT.tr("The sslfactoryarg property may not be empty."));
      }
   }

   public static class SingleCertTrustManager implements X509TrustManager {
      X509Certificate cert;
      X509TrustManager trustManager;

      public SingleCertTrustManager(InputStream in) throws IOException, GeneralSecurityException {
         KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
         ks.load(null);
         CertificateFactory cf = CertificateFactory.getInstance("X509");
         this.cert = (X509Certificate)cf.generateCertificate(in);
         ks.setCertificateEntry(UUID.randomUUID().toString(), this.cert);
         TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
         tmf.init(ks);

         for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
               this.trustManager = (X509TrustManager)tm;
               break;
            }
         }

         if (this.trustManager == null) {
            throw new GeneralSecurityException(GT.tr("No X509TrustManager found"));
         }
      }

      @Override
      public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
      }

      @Override
      public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
         this.trustManager.checkServerTrusted(chain, authType);
      }

      @Override
      public X509Certificate[] getAcceptedIssuers() {
         return new X509Certificate[]{this.cert};
      }
   }
}
