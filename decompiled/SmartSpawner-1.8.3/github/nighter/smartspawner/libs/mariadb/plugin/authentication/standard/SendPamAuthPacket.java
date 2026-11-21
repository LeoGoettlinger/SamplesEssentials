package github.nighter.smartspawner.libs.mariadb.plugin.authentication.standard;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.socket.Reader;
import github.nighter.smartspawner.libs.mariadb.client.socket.Writer;
import github.nighter.smartspawner.libs.mariadb.plugin.AuthDialogCallback;
import github.nighter.smartspawner.libs.mariadb.plugin.AuthenticationPlugin;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.ServiceLoader;

public class SendPamAuthPacket implements AuthenticationPlugin {
   private final String authenticationData;
   private final Configuration conf;
   private int counter = 0;

   public SendPamAuthPacket(String authenticationData, Configuration conf) {
      this.authenticationData = authenticationData;
      this.conf = conf;
   }

   @Override
   public ReadableByteBuf process(Writer out, Reader in, Context context, boolean sslFingerPrintValidation) throws SQLException, IOException {
      ReadableByteBuf serverPacket = null;

      int type;
      do {
         this.counter++;
         String password = this.resolvePassword(serverPacket);
         byte[] bytePwd = password != null ? password.getBytes(StandardCharsets.UTF_8) : new byte[0];
         out.writeBytes(bytePwd, 0, bytePwd.length);
         out.writeByte(0);
         out.flush();
         serverPacket = in.readReusablePacket();
         type = serverPacket.getUnsignedByte();
      } while (type != 254 && type != 0 && type != 255);

      return serverPacket;
   }

   private String resolvePassword(ReadableByteBuf serverPacket) throws SQLException {
      if (this.counter == 1) {
         return this.authenticationData;
      } else {
         AuthDialogCallback cb = dialogCallback();
         if (serverPacket != null && cb != null) {
            int flags = serverPacket.readUnsignedByte();
            String promptText = serverPacket.readStringEof();
            boolean echo = (flags & 2) != 0;
            String answer = cb.prompt(echo, promptText, this.counter);
            if (answer != null) {
               return answer;
            }
         }

         if (!this.conf.nonMappedOptions().containsKey("password" + this.counter)) {
            throw new SQLException("PAM authentication request multiple passwords, but 'password" + this.counter + "' is not set");
         } else {
            return (String)this.conf.nonMappedOptions().get("password" + this.counter);
         }
      }
   }

   private static AuthDialogCallback dialogCallback() {
      Iterator<AuthDialogCallback> it = ServiceLoader.load(AuthDialogCallback.class).iterator();
      return it.hasNext() ? it.next() : null;
   }
}
