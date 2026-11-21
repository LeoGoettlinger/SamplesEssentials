package com.viaversion.viabackwards.protocol.v1_16_4to1_16_3;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.protocol.v1_16_4to1_16_3.storage.ProtocolStorables1_16_4;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ServerboundPackets1_16_2;

public class Protocol1_16_4To1_16_3
   extends BackwardsProtocol<ClientboundPackets1_16_2, ClientboundPackets1_16_2, ServerboundPackets1_16_2, ServerboundPackets1_16_2> {
   public Protocol1_16_4To1_16_3() {
      super(ClientboundPackets1_16_2.class, ClientboundPackets1_16_2.class, ServerboundPackets1_16_2.class, ServerboundPackets1_16_2.class);
   }

   protected void registerPackets() {
      this.registerServerbound(ServerboundPackets1_16_2.EDIT_BOOK, new PacketHandlers() {
         public void register() {
            this.map(Types.ITEM1_13_2);
            this.map(Types.BOOLEAN);
            this.handler(wrapper -> {
               int slot = (Integer)wrapper.read(Types.VAR_INT);
               if (slot == 1) {
                  wrapper.write(Types.VAR_INT, 40);
               } else {
                  ProtocolStorables1_16_4 storables = (ProtocolStorables1_16_4)wrapper.user().storables(Protocol1_16_4To1_16_3.this);
                  wrapper.write(Types.VAR_INT, storables.playerHandStorage().getCurrentHand());
               }
            });
         }
      });
      this.registerServerbound(ServerboundPackets1_16_2.SET_CARRIED_ITEM, wrapper -> {
         short slot = (Short)wrapper.passthrough(Types.SHORT);
         ProtocolStorables1_16_4 storables = (ProtocolStorables1_16_4)wrapper.user().storables(this);
         storables.playerHandStorage().setCurrentHand(slot);
      });
   }

   public void init(UserConnection user) {
   }

   public ProtocolStorablesBase createStorables() {
      return new ProtocolStorables1_16_4();
   }
}
