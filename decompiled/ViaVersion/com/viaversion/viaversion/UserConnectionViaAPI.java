package com.viaversion.viaversion;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.buffer.ByteBuf;

public class UserConnectionViaAPI extends ViaAPIBase<UserConnection> {
   public ProtocolVersion getPlayerProtocolVersion(UserConnection connection) {
      return connection.getProtocolInfo().protocolVersion();
   }

   public void sendRawPacket(UserConnection connection, ByteBuf packet) {
      connection.sendRawPacket(packet);
   }
}
