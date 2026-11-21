package com.viaversion.viabackwards.protocol.v1_15to1_14_4.rewriter;

import com.viaversion.viabackwards.api.rewriters.BackwardsItemRewriter;
import com.viaversion.viabackwards.protocol.v1_15to1_14_4.Protocol1_15To1_14_4;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_14;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_15;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packet.ClientboundPackets1_15;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

public class BlockItemPacketRewriter1_15 extends BackwardsItemRewriter<ClientboundPackets1_15, ServerboundPackets1_14, Protocol1_15To1_14_4> {
   public BlockItemPacketRewriter1_15(Protocol1_15To1_14_4 protocol) {
      super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_SHORT_ARRAY);
   }

   protected void registerPackets() {
      new RecipeRewriter(this.protocol).register(ClientboundPackets1_15.UPDATE_RECIPES);
      ((Protocol1_15To1_14_4)this.protocol)
         .registerServerbound(ServerboundPackets1_14.EDIT_BOOK, wrapper -> this.handleItemToServer(wrapper.user(), (Item)wrapper.passthrough(Types.ITEM1_13_2)));
      ((Protocol1_15To1_14_4)this.protocol).registerClientbound(ClientboundPackets1_15.LEVEL_CHUNK, wrapper -> {
         Chunk chunk = (Chunk)wrapper.read(ChunkType1_15.TYPE);
         wrapper.write(ChunkType1_14.TYPE, chunk);
         if (chunk.isFullChunk()) {
            int[] biomeData = chunk.getBiomeData();
            int[] newBiomeData = new int[256];

            for (int i = 0; i < 4; i++) {
               for (int j = 0; j < 4; j++) {
                  int x = j << 2;
                  int z = i << 2;
                  int newIndex = z << 4 | x;
                  int oldIndex = i << 2 | j;
                  int biome = biomeData[oldIndex];

                  for (int k = 0; k < 4; k++) {
                     int offX = newIndex + (k << 4);

                     for (int l = 0; l < 4; l++) {
                        newBiomeData[offX + l] = biome;
                     }
                  }
               }
            }

            chunk.setBiomeData(newBiomeData);
         }

         ((Protocol1_15To1_14_4)this.protocol).getBlockRewriter().handleChunk(chunk);
      });
      ((Protocol1_15To1_14_4)this.protocol).replaceClientbound(ClientboundPackets1_15.LEVEL_PARTICLES, new PacketHandlers() {
         public void register() {
            this.map(Types.INT);
            this.map(Types.BOOLEAN);
            this.map(Types.DOUBLE, Types.FLOAT);
            this.map(Types.DOUBLE, Types.FLOAT);
            this.map(Types.DOUBLE, Types.FLOAT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.map(Types.INT);
            this.handler(wrapper -> {
               int id = (Integer)wrapper.get(Types.INT, 0);
               if (id == 3 || id == 23) {
                  int data = (Integer)wrapper.passthrough(Types.VAR_INT);
                  wrapper.set(Types.VAR_INT, 0, ((Protocol1_15To1_14_4)BlockItemPacketRewriter1_15.this.protocol).getMappingData().getNewBlockStateId(data));
               } else if (id == 32) {
                  Item item = BlockItemPacketRewriter1_15.this.handleItemToClient(wrapper.user(), (Item)wrapper.read(Types.ITEM1_13_2));
                  wrapper.write(Types.ITEM1_13_2, item);
               }

               int mappedId = ((Protocol1_15To1_14_4)BlockItemPacketRewriter1_15.this.protocol).getMappingData().getNewParticleId(id);
               if (id != mappedId) {
                  wrapper.set(Types.INT, 0, mappedId);
               }
            });
         }
      });
   }
}
