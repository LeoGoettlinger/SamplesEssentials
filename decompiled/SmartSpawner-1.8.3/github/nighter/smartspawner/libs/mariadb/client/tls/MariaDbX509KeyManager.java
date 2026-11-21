package github.nighter.smartspawner.libs.mariadb.client.tls;

import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.security.auth.x500.X500Principal;

public class MariaDbX509KeyManager extends X509ExtendedKeyManager {
   private final Hashtable<String, PrivateKeyEntry> privateKeyHash = new Hashtable<>();

   public MariaDbX509KeyManager(KeyStore keyStore, char[] pwd) throws KeyStoreException {
      Enumeration<String> aliases = keyStore.aliases();

      while (aliases.hasMoreElements()) {
         String alias = aliases.nextElement();
         if (keyStore.entryInstanceOf(alias, PrivateKeyEntry.class)) {
            try {
               this.privateKeyHash.put(alias, (PrivateKeyEntry)keyStore.getEntry(alias, new PasswordProtection(pwd)));
            } catch (NoSuchAlgorithmException | UnrecoverableEntryException var6) {
            }
         }
      }
   }

   @Override
   public String[] getClientAliases(String keyType, Principal[] issuers) {
      List<String> accurateAlias = this.searchAccurateAliases(new String[]{keyType}, issuers);
      return accurateAlias != null && !accurateAlias.isEmpty() ? accurateAlias.toArray(new String[0]) : null;
   }

   @Override
   public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
      List<String> accurateAlias = this.searchAccurateAliases(keyType, issuers);
      return accurateAlias != null && !accurateAlias.isEmpty() ? accurateAlias.get(0) : null;
   }

   @Override
   public X509Certificate[] getCertificateChain(String alias) {
      PrivateKeyEntry keyEntry = this.privateKeyHash.get(alias);
      if (keyEntry == null) {
         return null;
      } else {
         Certificate[] certs = keyEntry.getCertificateChain();
         return certs.length > 0 && certs[0] instanceof X509Certificate ? Arrays.copyOf(certs, certs.length, X509Certificate[].class) : null;
      }
   }

   @Override
   public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
      return this.chooseClientAlias(keyType, issuers, null);
   }

   @Override
   public PrivateKey getPrivateKey(String alias) {
      PrivateKeyEntry keyEntry = this.privateKeyHash.get(alias);
      return keyEntry == null ? null : keyEntry.getPrivateKey();
   }

   private ArrayList<String> searchAccurateAliases(String[] keyTypes, Principal[] issuers) {
      if (keyTypes != null && keyTypes.length != 0) {
         ArrayList<String> accurateAliases = new ArrayList<>();

         for (Entry<String, PrivateKeyEntry> mapEntry : this.privateKeyHash.entrySet()) {
            Certificate[] certs = mapEntry.getValue().getCertificateChain();
            String alg = certs[0].getPublicKey().getAlgorithm();

            label54:
            for (String keyType : keyTypes) {
               if (alg.equals(keyType)) {
                  if (issuers != null && issuers.length != 0) {
                     for (Certificate cert : certs) {
                        if (cert instanceof X509Certificate) {
                           X500Principal certificateIssuer = ((X509Certificate)cert).getIssuerX500Principal();

                           for (Principal issuer : issuers) {
                              if (certificateIssuer.equals(issuer)) {
                                 accurateAliases.add(mapEntry.getKey());
                                 continue label54;
                              }
                           }
                        }
                     }
                  } else {
                     accurateAliases.add(mapEntry.getKey());
                  }
               }
            }
         }

         return accurateAliases;
      } else {
         return null;
      }
   }

   @Override
   public String[] getServerAliases(String keyType, Principal[] issuers) {
      return null;
   }

   @Override
   public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
      return null;
   }

   @Override
   public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
      return null;
   }
}
