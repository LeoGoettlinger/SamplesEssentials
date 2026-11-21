package com.viaversion.viaversion.protocols.v1_19_1to1_19_3.rewriter;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.Protocol1_19_1To1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ServerboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.packet.ClientboundPackets1_19_1;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.Key;

public final class ItemPacketRewriter1_19_3 extends ItemRewriter<ClientboundPackets1_19_1, ServerboundPackets1_19_3, Protocol1_19_1To1_19_3> {
   private static final int MISC_CRAFTING_BOOK_CATEGORY = 0;

   public ItemPacketRewriter1_19_3(Protocol1_19_1To1_19_3 protocol) {
      super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_ARRAY);
   }

   @Override
   public void registerPackets() {
      RecipeRewriter<ClientboundPackets1_19_1> recipeRewriter = new RecipeRewriter<>(this.protocol);
      this.protocol.registerClientbound(ClientboundPackets1_19_1.UPDATE_RECIPES, wrapper -> {
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            String type = Key.stripMinecraftNamespace(wrapper.passthrough(Types.STRING));
            wrapper.passthrough(Types.STRING);
            switch (type) {
               case "crafting_shapeless":
                  wrapper.passthrough(Types.STRING);
                  wrapper.write(Types.VAR_INT, 0);
                  int ingredients = wrapper.passthrough(Types.VAR_INT);

                  for (int j = 0; j < ingredients; j++) {
                     Item[] items = wrapper.passthrough(Types.ITEM1_13_2_ARRAY);

                     for (Item item : items) {
                        this.handleItemToClient(wrapper.user(), item);
                     }
                  }

                  this.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
                  break;
               case "crafting_shaped":
                  int ingredients = wrapper.passthrough(Types.VAR_INT) * wrapper.passthrough(Types.VAR_INT);
                  wrapper.passthrough(Types.STRING);
                  wrapper.write(Types.VAR_INT, 0);

                  for (int j = 0; j < ingredients; j++) {
                     Item[] items = wrapper.passthrough(Types.ITEM1_13_2_ARRAY);

                     for (Item item : items) {
                        this.handleItemToClient(wrapper.user(), item);
                     }
                  }

                  this.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
                  break;
               case "smelting":
               case "campfire_cooking":
               case "blasting":
               case "smoking":
                  wrapper.passthrough(Types.STRING);
                  wrapper.write(Types.VAR_INT, 0);
                  Item[] items = wrapper.passthrough(Types.ITEM1_13_2_ARRAY);

                  for (Item item : items) {
                     this.handleItemToClient(wrapper.user(), item);
                  }

                  this.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
                  wrapper.passthrough(Types.FLOAT);
                  wrapper.passthrough(Types.VAR_INT);
                  break;
               case "crafting_special_armordye":
               case "crafting_special_bookcloning":
               case "crafting_special_mapcloning":
               case "crafting_special_mapextending":
               case "crafting_special_firework_rocket":
               case "crafting_special_firework_star":
               case "crafting_special_firework_star_fade":
               case "crafting_special_tippedarrow":
               case "crafting_special_bannerduplicate":
               case "crafting_special_shielddecoration":
               case "crafting_special_shulkerboxcoloring":
               case "crafting_special_suspiciousstew":
               case "crafting_special_repairitem":
                  wrapper.write(Types.VAR_INT, 0);
                  break;
               default:
                  recipeRewriter.handleRecipeType(wrapper, type);
            }
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_19_1.EXPLODE, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.FLOAT, Types.DOUBLE);
            this.map(Types.FLOAT, Types.DOUBLE);
            this.map(Types.FLOAT, Types.DOUBLE);
         }
      });
   }
}
