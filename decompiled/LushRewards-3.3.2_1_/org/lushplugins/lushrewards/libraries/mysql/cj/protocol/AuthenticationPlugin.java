package org.lushplugins.lushrewards.libraries.mysql.cj.protocol;

import java.util.List;
import org.lushplugins.lushrewards.libraries.mysql.cj.callback.MysqlCallbackHandler;

public interface AuthenticationPlugin<M extends Message> {
   default void init(Protocol<M> protocol) {
   }

   default void init(Protocol<M> protocol, MysqlCallbackHandler callbackHandler) {
      this.init(protocol);
   }

   default void reset() {
   }

   default void destroy() {
   }

   String getProtocolPluginName();

   boolean requiresConfidentiality();

   boolean isReusable();

   void setAuthenticationParameters(String var1, String var2);

   default void setSourceOfAuthData(String sourceOfAuthData) {
   }

   boolean nextAuthenticationStep(M var1, List<M> var2);
}
