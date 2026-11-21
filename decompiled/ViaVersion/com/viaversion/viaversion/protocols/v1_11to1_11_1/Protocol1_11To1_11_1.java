package com.viaversion.viaversion.protocols.v1_11to1_11_1;

import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_11to1_11_1.rewriter.ItemPacketRewriter1_11_1;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ServerboundPackets1_9_3;

public class Protocol1_11To1_11_1 extends AbstractProtocol<ClientboundPackets1_9_3, ClientboundPackets1_9_3, ServerboundPackets1_9_3, ServerboundPackets1_9_3> {
   private final ItemPacketRewriter1_11_1 itemRewriter = new ItemPacketRewriter1_11_1(this);

   public Protocol1_11To1_11_1() {
      super(ClientboundPackets1_9_3.class, ClientboundPackets1_9_3.class, ServerboundPackets1_9_3.class, ServerboundPackets1_9_3.class);
   }

   @Override
   protected void registerPackets() {
      this.itemRewriter.register();
      this.registerClientbound(ClientboundPackets1_9_3.AWARD_STATS, wrapper -> {
         int size = wrapper.passthrough(Types.VAR_INT);
         int removed = 0;

         for (int i = 0; i < size; i++) {
            String name = wrapper.read(Types.STRING);
            int value = wrapper.read(Types.VAR_INT);
            if (!name.equals("stat.treasureFished") && !name.equals("stat.junkFished")) {
               wrapper.write(Types.STRING, name);
               wrapper.write(Types.VAR_INT, value);
            } else {
               removed++;
            }
         }

         wrapper.set(Types.VAR_INT, 0, size - removed);
      });
   }

   public ItemPacketRewriter1_11_1 getItemRewriter() {
      return this.itemRewriter;
   }
}
