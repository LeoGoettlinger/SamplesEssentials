package com.viaversion.viaversion.protocols.v1_16_1to1_16_2.rewriter;

import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.Protocol1_16_1To1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ServerboundPackets1_16_2;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

public class ItemPacketRewriter1_16_2 extends ItemRewriter<ClientboundPackets1_16, ServerboundPackets1_16_2, Protocol1_16_1To1_16_2> {
   public ItemPacketRewriter1_16_2(Protocol1_16_1To1_16_2 protocol) {
      super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_SHORT_ARRAY);
   }

   @Override
   public void registerPackets() {
      this.protocol.registerClientbound(ClientboundPackets1_16.RECIPE, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.write(Types.BOOLEAN, false);
         wrapper.write(Types.BOOLEAN, false);
         wrapper.write(Types.BOOLEAN, false);
         wrapper.write(Types.BOOLEAN, false);
      });
      new RecipeRewriter<>(this.protocol).register(ClientboundPackets1_16.UPDATE_RECIPES);
      this.protocol
         .registerServerbound(ServerboundPackets1_16_2.EDIT_BOOK, wrapper -> this.handleItemToServer(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2)));
   }
}
