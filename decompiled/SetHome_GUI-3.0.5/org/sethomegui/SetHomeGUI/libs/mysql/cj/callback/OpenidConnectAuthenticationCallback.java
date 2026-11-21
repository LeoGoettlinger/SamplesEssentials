package org.sethomegui.SetHomeGUI.libs.mysql.cj.callback;

import java.util.function.Function;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;

public class OpenidConnectAuthenticationCallback implements MysqlCallback {
   private String user;
   Function<PropertyKey, String> connPropSupplier;
   private byte[] identityToken;

   public OpenidConnectAuthenticationCallback(Function<PropertyKey, String> connPropSupplier) {
      this.connPropSupplier = connPropSupplier;
   }

   public String getConnProperty(PropertyKey propKey) {
      return this.connPropSupplier.apply(propKey);
   }

   public String getUser() {
      return this.user;
   }

   public void setUser(String user) {
      this.user = user;
   }

   public byte[] getIdentityToken() {
      return this.identityToken;
   }

   public void setIdentityToken(byte[] identityToken) {
      this.identityToken = identityToken;
   }
}
