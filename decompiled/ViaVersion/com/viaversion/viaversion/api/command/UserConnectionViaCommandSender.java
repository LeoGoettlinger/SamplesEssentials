package com.viaversion.viaversion.api.command;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import java.util.UUID;

public class UserConnectionViaCommandSender implements ViaCommandSender {
   private final UserConnection connection;

   public UserConnectionViaCommandSender(UserConnection connection) {
      this.connection = connection;
   }

   @Override
   public boolean hasPermission(String permission) {
      return true;
   }

   @Override
   public void sendMessage(String msg) {
      Via.getPlatform().sendMessage(this.connection, msg);
   }

   @Override
   public UUID getUUID() {
      return this.connection.getProtocolInfo().getUuid();
   }

   @Override
   public String getName() {
      return this.connection.getProtocolInfo().getUsername();
   }
}
