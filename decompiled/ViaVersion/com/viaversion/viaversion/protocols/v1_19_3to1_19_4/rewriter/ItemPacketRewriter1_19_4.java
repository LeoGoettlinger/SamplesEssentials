package com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.rewriter.RecipeRewriter1_19_3;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.Protocol1_19_3To1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ServerboundPackets1_19_4;
import com.viaversion.viaversion.rewriter.ItemRewriter;

public final class ItemPacketRewriter1_19_4 extends ItemRewriter<ClientboundPackets1_19_3, ServerboundPackets1_19_4, Protocol1_19_3To1_19_4> {
   public ItemPacketRewriter1_19_4(Protocol1_19_3To1_19_4 protocol) {
      super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_ARRAY);
   }

   @Override
   public void registerPackets() {
      this.protocol.replaceClientbound(ClientboundPackets1_19_3.LEVEL_EVENT, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.INT);
            this.map(Types.BLOCK_POSITION1_14);
            this.map(Types.INT);
            this.handler(wrapper -> {
               int id = wrapper.get(Types.INT, 0);
               int data = wrapper.get(Types.INT, 1);
               if (id == 1010) {
                  if (data >= 1092 && data <= 1106) {
                     wrapper.set(Types.INT, 1, ItemPacketRewriter1_19_4.this.protocol.getMappingData().getNewItemId(data));
                  } else {
                     wrapper.set(Types.INT, 0, 1011);
                     wrapper.set(Types.INT, 1, 0);
                  }
               } else if (id == 2001) {
                  wrapper.set(Types.INT, 1, ItemPacketRewriter1_19_4.this.protocol.getMappingData().getNewBlockStateId(data));
               }
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_19_3.OPEN_SCREEN, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.COMPONENT);
            this.handler(wrapper -> {
               int windowType = wrapper.get(Types.VAR_INT, 1);
               if (windowType >= 21) {
                  wrapper.set(Types.VAR_INT, 1, windowType + 1);
               }
            });
         }
      });
      (new RecipeRewriter1_19_3<ClientboundPackets1_19_3>(this.protocol) {
         @Override
         public void handleCraftingShaped(PacketWrapper wrapper) {
            super.handleCraftingShaped(wrapper);
            wrapper.write(Types.BOOLEAN, true);
         }
      }).register(ClientboundPackets1_19_3.UPDATE_RECIPES);
   }
}
