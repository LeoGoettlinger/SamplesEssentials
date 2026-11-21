package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a.authentication;

import java.util.List;
import org.lushplugins.lushrewards.libraries.mysql.cj.callback.MysqlCallbackHandler;
import org.lushplugins.lushrewards.libraries.mysql.cj.callback.UsernameCallback;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.AuthenticationPlugin;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Protocol;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a.NativeConstants;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a.NativePacketPayload;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.StringUtils;

public class MysqlClearPasswordPlugin implements AuthenticationPlugin<NativePacketPayload> {
   public static String PLUGIN_NAME = "mysql_clear_password";
   private Protocol<NativePacketPayload> protocol = null;
   private MysqlCallbackHandler usernameCallbackHandler = null;
   private String password = null;

   @Override
   public void init(Protocol<NativePacketPayload> prot, MysqlCallbackHandler cbh) {
      this.protocol = prot;
      this.usernameCallbackHandler = cbh;
   }

   @Override
   public void destroy() {
      this.reset();
      this.protocol = null;
      this.usernameCallbackHandler = null;
      this.password = null;
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
      this.password = password;
      if (user == null && this.usernameCallbackHandler != null) {
         this.usernameCallbackHandler.handle(new UsernameCallback(System.getProperty("user.name")));
      }
   }

   public boolean nextAuthenticationStep(NativePacketPayload fromServer, List<NativePacketPayload> toServer) {
      toServer.clear();
      String encoding = this.protocol.getServerSession().getCharsetSettings().getPasswordCharacterEncoding();
      NativePacketPayload packet = new NativePacketPayload(StringUtils.getBytes(this.password != null ? this.password : "", encoding));
      packet.setPosition(packet.getPayloadLength());
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
      packet.setPosition(0);
      toServer.add(packet);
      return true;
   }
}
