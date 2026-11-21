package org.jsoup.helper;

import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.Authenticator.RequestorType;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface RequestAuthenticator {
   @Nullable
   PasswordAuthentication authenticate(RequestAuthenticator.Context var1);

   public static class Context {
      private final URL url;
      private final RequestorType type;
      private final String realm;

      Context(URL url, RequestorType type, String realm) {
         this.url = url;
         this.type = type;
         this.realm = realm;
      }

      public URL url() {
         return this.url;
      }

      public RequestorType type() {
         return this.type;
      }

      public String realm() {
         return this.realm;
      }

      public boolean isProxy() {
         return this.type == RequestorType.PROXY;
      }

      public boolean isServer() {
         return this.type == RequestorType.SERVER;
      }

      public PasswordAuthentication credentials(String username, String password) {
         return new PasswordAuthentication(username, password.toCharArray());
      }
   }
}
