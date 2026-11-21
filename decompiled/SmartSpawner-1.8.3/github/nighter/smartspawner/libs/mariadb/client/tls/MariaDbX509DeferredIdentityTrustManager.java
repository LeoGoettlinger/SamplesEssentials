package github.nighter.smartspawner.libs.mariadb.client.tls;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class MariaDbX509DeferredIdentityTrustManager implements X509TrustManager {
   private final X509TrustManager internal;
   private byte[] fingerprint = null;

   public MariaDbX509DeferredIdentityTrustManager(X509TrustManager javaTrustManager) {
      this.internal = javaTrustManager;
   }

   private static byte[] getThumbprint(X509Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return md.digest(cert.getEncoded());
   }

   @Override
   public void checkClientTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
      this.internal.checkClientTrusted(x509Certificates, authType);
   }

   @Override
   public void checkServerTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
      try {
         this.internal.checkServerTrusted(x509Certificates, authType);
      } catch (CertificateException var6) {
         if (x509Certificates == null || x509Certificates.length < 1) {
            throw var6;
         }

         x509Certificates[0].checkValidity();

         try {
            this.fingerprint = getThumbprint(x509Certificates[0]);
         } catch (CertificateEncodingException | NoSuchAlgorithmException var5) {
            throw var6;
         }
      }
   }

   public byte[] getFingerprint() {
      return this.fingerprint;
   }

   @Override
   public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
   }
}
