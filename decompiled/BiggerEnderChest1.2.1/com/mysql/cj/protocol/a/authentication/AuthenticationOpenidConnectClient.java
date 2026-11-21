package com.mysql.cj.protocol.a.authentication;

import com.mysql.cj.Messages;
import com.mysql.cj.callback.MysqlCallbackHandler;
import com.mysql.cj.callback.OpenidConnectAuthenticationCallback;
import com.mysql.cj.callback.UsernameCallback;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.AuthenticationPlugin;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.util.Util;
import java.util.List;

public class AuthenticationOpenidConnectClient implements AuthenticationPlugin<NativePacketPayload> {
   public static String PLUGIN_NAME = "authentication_openid_connect_client";
   private Protocol<NativePacketPayload> protocol = null;
   private MysqlCallbackHandler usernameCallbackHandler = null;
   private String user = null;
   private MysqlCallbackHandler openidConnectAuthenticationCallbackHandler = null;
   private OpenidConnectAuthenticationCallback openidConnectAuthCallback = null;

   @Override
   public void init(Protocol<NativePacketPayload> prot, MysqlCallbackHandler cbh) {
      this.protocol = prot;
      this.usernameCallbackHandler = cbh;
      String webAuthnCallbackHandlerClassName = this.protocol
         .getPropertySet()
         .getStringProperty(PropertyKey.authenticationOpenidConnectCallbackHandler)
         .getValue();
      if (webAuthnCallbackHandlerClassName == null) {
         throw ExceptionFactory.createException(Messages.getString("AuthenticationWebAuthnClientPlugin.MissingCallbackHandler"));
      } else {
         this.openidConnectAuthenticationCallbackHandler = Util.getInstance(
            MysqlCallbackHandler.class, webAuthnCallbackHandlerClassName, null, null, this.protocol.getExceptionInterceptor()
         );
      }
   }

   @Override
   public void destroy() {
      this.reset();
      this.protocol = null;
      this.usernameCallbackHandler = null;
      this.openidConnectAuthenticationCallbackHandler = null;
      this.openidConnectAuthCallback = null;
   }

   @Override
   public String getProtocolPluginName() {
      return PLUGIN_NAME;
   }

   @Override
   public boolean requiresConfidentiality() {
      return true;
   }

   @Override
   public boolean isReusable() {
      return true;
   }

   @Override
   public void setAuthenticationParameters(String user, String password) {
      this.user = user;
      if (user == null) {
         this.user = System.getProperty("user.name");
         if (this.usernameCallbackHandler != null) {
            this.usernameCallbackHandler.handle(new UsernameCallback(this.user));
         }
      }
   }

   public boolean nextAuthenticationStep(NativePacketPayload fromServer, List<NativePacketPayload> toServer) {
      toServer.clear();
      if (this.openidConnectAuthCallback == null) {
         this.openidConnectAuthCallback = new OpenidConnectAuthenticationCallback(this::getPropertyStringValue);
      }

      this.openidConnectAuthCallback.setUser(this.user);
      this.openidConnectAuthenticationCallbackHandler.handle(this.openidConnectAuthCallback);
      byte[] idToken = this.openidConnectAuthCallback.getIdentityToken();
      if (idToken != null && idToken.length != 0) {
         int packetLen = 10;
         packetLen += idToken.length;
         NativePacketPayload packet = new NativePacketPayload(packetLen);
         packet.writeInteger(NativeConstants.IntegerDataType.INT1, 1L);
         packet.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, idToken);
         packet.setPosition(0);
         toServer.add(packet);
         return true;
      } else {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class, Messages.getString("AuthenticationOpenidConnect.EmptyIdentityToken")
         );
      }
   }

   private String getPropertyStringValue(PropertyKey key) {
      RuntimeProperty<?> prop = this.protocol.getPropertySet().getProperty(key);
      return prop == null ? null : prop.getStringValue();
   }
}
