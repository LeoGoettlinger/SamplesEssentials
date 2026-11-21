package com.viaversion.viabackwards.protocol.v1_12_2to1_12_1;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.protocol.v1_12_2to1_12_1.storage.KeepAliveTracker;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_12to1_12_1.packet.ClientboundPackets1_12_1;
import com.viaversion.viaversion.protocols.v1_12to1_12_1.packet.ServerboundPackets1_12_1;

public class Protocol1_12_2To1_12_1
   extends BackwardsProtocol<ClientboundPackets1_12_1, ClientboundPackets1_12_1, ServerboundPackets1_12_1, ServerboundPackets1_12_1> {
   public Protocol1_12_2To1_12_1() {
      super(ClientboundPackets1_12_1.class, ClientboundPackets1_12_1.class, ServerboundPackets1_12_1.class, ServerboundPackets1_12_1.class);
   }

   protected void registerPackets() {
      this.registerClientbound(ClientboundPackets1_12_1.KEEP_ALIVE, wrapper -> {
         long keepAlive = (Long)wrapper.read(Types.LONG);
         int id = ((KeepAliveTracker)wrapper.user().get(KeepAliveTracker.class)).track(keepAlive);
         wrapper.write(Types.VAR_INT, id);
      });
      this.registerServerbound(ServerboundPackets1_12_1.KEEP_ALIVE, wrapper -> {
         int keepAlive = (Integer)wrapper.read(Types.VAR_INT);
         Long realKeepAlive = ((KeepAliveTracker)wrapper.user().get(KeepAliveTracker.class)).consume(keepAlive);
         if (realKeepAlive == null) {
            wrapper.cancel();
         } else {
            wrapper.write(Types.LONG, realKeepAlive);
         }
      });
   }

   public void init(UserConnection userConnection) {
      userConnection.put(new KeepAliveTracker());
   }
}
