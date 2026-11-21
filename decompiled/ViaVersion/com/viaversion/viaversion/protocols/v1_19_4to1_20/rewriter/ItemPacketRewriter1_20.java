package com.viaversion.viaversion.protocols.v1_19_4to1_20.rewriter;

import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ServerboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.RecipeRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_19_4to1_20.Protocol1_19_4To1_20;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.Key;

public final class ItemPacketRewriter1_20 extends ItemRewriter<ClientboundPackets1_19_4, ServerboundPackets1_19_4, Protocol1_19_4To1_20> {
   public ItemPacketRewriter1_20(Protocol1_19_4To1_20 protocol) {
      super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_ARRAY);
   }

   @Override
   public void registerPackets() {
      this.protocol.replaceClientbound(ClientboundPackets1_19_4.UPDATE_ADVANCEMENTS, wrapper -> {
         wrapper.passthrough(Types.BOOLEAN);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.OPTIONAL_STRING);
            if (wrapper.passthrough(Types.BOOLEAN)) {
               wrapper.passthrough(Types.COMPONENT);
               wrapper.passthrough(Types.COMPONENT);
               this.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
               wrapper.passthrough(Types.VAR_INT);
               int flags = wrapper.passthrough(Types.INT);
               if ((flags & 1) != 0) {
                  wrapper.passthrough(Types.STRING);
               }

               wrapper.passthrough(Types.FLOAT);
               wrapper.passthrough(Types.FLOAT);
            }

            wrapper.passthrough(Types.STRING_ARRAY);
            int requirements = wrapper.passthrough(Types.VAR_INT);

            for (int array = 0; array < requirements; array++) {
               wrapper.passthrough(Types.STRING_ARRAY);
            }

            wrapper.write(Types.BOOLEAN, false);
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_19_4.OPEN_SIGN_EDITOR, wrapper -> {
         wrapper.passthrough(Types.BLOCK_POSITION1_14);
         wrapper.write(Types.BOOLEAN, true);
      });
      this.protocol.registerServerbound(ServerboundPackets1_19_4.SIGN_UPDATE, wrapper -> {
         wrapper.passthrough(Types.BLOCK_POSITION1_14);
         boolean frontText = wrapper.read(Types.BOOLEAN);
         if (!frontText) {
            wrapper.cancel();
         }
      });
      this.protocol.replaceClientbound(ClientboundPackets1_19_4.LEVEL_CHUNK_WITH_LIGHT, new PacketHandlers() {
         @Override
         protected void register() {
            this.handler(wrapper -> {
               Chunk chunk = ItemPacketRewriter1_20.this.protocol.getBlockRewriter().handleChunk1_18(wrapper);
               ItemPacketRewriter1_20.this.protocol.getBlockRewriter().handleBlockEntities(chunk, wrapper.user());
            });
            this.read(Types.BOOLEAN);
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_19_4.LIGHT_UPDATE, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.read(Types.BOOLEAN);
      });
      this.protocol.replaceClientbound(ClientboundPackets1_19_4.SECTION_BLOCKS_UPDATE, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.LONG);
            this.read(Types.BOOLEAN);
            this.handler(wrapper -> {
               for (BlockChangeRecord record : wrapper.passthrough(Types.VAR_LONG_BLOCK_CHANGE_ARRAY)) {
                  record.setBlockId(ItemPacketRewriter1_20.this.protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
               }
            });
         }
      });
      RecipeRewriter<ClientboundPackets1_19_4> recipeRewriter = new RecipeRewriter1_19_4<>(this.protocol);
      this.protocol.registerClientbound(ClientboundPackets1_19_4.UPDATE_RECIPES, wrapper -> {
         int size = wrapper.passthrough(Types.VAR_INT);
         int newSize = size;

         for (int i = 0; i < size; i++) {
            String type = wrapper.read(Types.STRING);
            String cutType = Key.stripMinecraftNamespace(type);
            if (cutType.equals("smithing")) {
               newSize--;
               wrapper.read(Types.STRING);
               wrapper.read(Types.ITEM1_13_2_ARRAY);
               wrapper.read(Types.ITEM1_13_2_ARRAY);
               wrapper.read(Types.ITEM1_13_2);
            } else {
               wrapper.write(Types.STRING, type);
               wrapper.passthrough(Types.STRING);
               recipeRewriter.handleRecipeType(wrapper, cutType);
            }
         }

         wrapper.set(Types.VAR_INT, 0, newSize);
      });
   }
}
