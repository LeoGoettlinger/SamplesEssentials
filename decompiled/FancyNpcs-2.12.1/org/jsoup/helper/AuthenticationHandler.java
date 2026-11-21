package org.jsoup.helper;

import java.lang.reflect.Constructor;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import org.jspecify.annotations.Nullable;

class AuthenticationHandler extends Authenticator {
   static final int MaxAttempts = 5;
   static AuthenticationHandler.AuthShim handler;
   @Nullable
   RequestAuthenticator auth;
   int attemptCount = 0;

   AuthenticationHandler() {
   }

   AuthenticationHandler(RequestAuthenticator auth) {
      this.auth = auth;
   }

   @Nullable
   @Override
   public final PasswordAuthentication getPasswordAuthentication() {
      AuthenticationHandler delegate = handler.get(this);
      if (delegate == null) {
         return null;
      } else {
         delegate.attemptCount++;
         if (delegate.attemptCount > 5) {
            return null;
         } else if (delegate.auth == null) {
            return null;
         } else {
            RequestAuthenticator.Context ctx = new RequestAuthenticator.Context(this.getRequestingURL(), this.getRequestorType(), this.getRequestingPrompt());
            return delegate.auth.authenticate(ctx);
         }
      }
   }

   static {
      try {
         Class<AuthenticationHandler.AuthShim> perRequestClass = (Class<AuthenticationHandler.AuthShim>)Class.forName("org.jsoup.helper.RequestAuthHandler");
         Constructor<AuthenticationHandler.AuthShim> constructor = perRequestClass.getConstructor();
         handler = constructor.newInstance();
      } catch (ClassNotFoundException var2) {
         handler = new AuthenticationHandler.GlobalHandler();
      } catch (Exception var3) {
         throw new IllegalStateException(var3);
      }
   }

   interface AuthShim {
      void enable(RequestAuthenticator var1, HttpURLConnection var2);

      void remove();

      @Nullable
      AuthenticationHandler get(AuthenticationHandler var1);
   }

   static class GlobalHandler implements AuthenticationHandler.AuthShim {
      static ThreadLocal<AuthenticationHandler> authenticators = new ThreadLocal<>();

      @Override
      public void enable(RequestAuthenticator auth, HttpURLConnection con) {
         authenticators.set(new AuthenticationHandler(auth));
      }

      @Override
      public void remove() {
         authenticators.remove();
      }

      @Override
      public AuthenticationHandler get(AuthenticationHandler helper) {
         return authenticators.get();
      }

      static {
         Authenticator.setDefault(new AuthenticationHandler());
      }
   }
}
