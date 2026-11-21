package github.nighter.smartspawner.libs.mariadb.client.tls;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class MariaDbX509TrustingManager implements X509TrustManager {
   @Override
   public void checkClientTrusted(X509Certificate[] x509Certificates, String string) {
   }

   @Override
   public void checkServerTrusted(X509Certificate[] x509Certificates, String string) {
   }

   @Override
   public X509Certificate[] getAcceptedIssuers() {
      return null;
   }
}
