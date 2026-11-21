package com.viaversion.viaversion.protocols.v1_13to1_13_1.rewriter;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.Protocol1_13To1_13_1;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.Key;

public class ItemPacketRewriter1_13_1 extends ItemRewriter<ClientboundPackets1_13, ServerboundPackets1_13, Protocol1_13To1_13_1> {
   public ItemPacketRewriter1_13_1(Protocol1_13To1_13_1 protocol) {
      super(protocol, Types.ITEM1_13, Types.ITEM1_13_SHORT_ARRAY);
   }

   @Override
   public void registerPackets() {
      this.protocol.registerClientbound(ClientboundPackets1_13.CUSTOM_PAYLOAD, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.STRING);
            this.handlerSoftFail(wrapper -> {
               String channel = Key.namespaced(wrapper.get(Types.STRING, 0));
               if (channel.equals("minecraft:trader_list")) {
                  wrapper.passthrough(Types.INT);
                  int size = wrapper.passthrough(Types.UNSIGNED_BYTE);

                  for (int i = 0; i < size; i++) {
                     ItemPacketRewriter1_13_1.this.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13));
                     ItemPacketRewriter1_13_1.this.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13));
                     boolean secondItem = wrapper.passthrough(Types.BOOLEAN);
                     if (secondItem) {
                        ItemPacketRewriter1_13_1.this.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13));
                     }

                     wrapper.passthrough(Types.BOOLEAN);
                     wrapper.passthrough(Types.INT);
                     wrapper.passthrough(Types.INT);
                  }
               }
            });
         }
      });
      RecipeRewriter<ClientboundPackets1_13> recipeRewriter = new RecipeRewriter<ClientboundPackets1_13>(this.protocol) {
         @Override
         protected Type<Item> itemType() {
            return Types.ITEM1_13;
         }

         @Override
         protected Type<Item[]> itemArrayType() {
            return Types.ITEM1_13_ARRAY;
         }
      };
      this.protocol.registerClientbound(ClientboundPackets1_13.UPDATE_RECIPES, wrapper -> {
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.STRING);
            String type = Key.stripMinecraftNamespace(wrapper.passthrough(Types.STRING));
            recipeRewriter.handleRecipeType(wrapper, type);
         }
      });
   }
}
