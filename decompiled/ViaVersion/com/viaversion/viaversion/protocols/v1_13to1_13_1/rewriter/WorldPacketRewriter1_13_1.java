package com.viaversion.viaversion.protocols.v1_13to1_13_1.rewriter;

import com.viaversion.viaversion.api.connection.ProtocolStorables;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.Protocol1_13To1_13_1;

public class WorldPacketRewriter1_13_1 {
   public static void register(final Protocol1_13To1_13_1 protocol) {
      protocol.registerClientbound(ClientboundPackets1_13.LEVEL_CHUNK, wrapper -> {
         ClientWorld clientWorld = wrapper.user().<ProtocolStorables>storables(protocol).clientWorld();
         Chunk chunk = wrapper.passthrough(ChunkType1_13.forEnvironment(clientWorld.getEnvironment()));
         protocol.getBlockRewriter().handleChunk(chunk);
      });
      protocol.replaceClientbound(ClientboundPackets1_13.LEVEL_EVENT, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.INT);
            this.map(Types.BLOCK_POSITION1_8);
            this.map(Types.INT);
            this.handler(wrapper -> {
               int id = wrapper.get(Types.INT, 0);
               if (id == 2000) {
                  int data = wrapper.get(Types.INT, 1);
                  switch (data) {
                     case 0:
                     case 3:
                     case 6:
                        wrapper.set(Types.INT, 1, 4);
                        break;
                     case 1:
                        wrapper.set(Types.INT, 1, 2);
                        break;
                     case 2:
                     case 5:
                     case 8:
                        wrapper.set(Types.INT, 1, 5);
                        break;
                     case 4:
                     default:
                        wrapper.set(Types.INT, 1, 0);
                        break;
                     case 7:
                        wrapper.set(Types.INT, 1, 3);
                  }
               } else if (id == 1010) {
                  wrapper.set(Types.INT, 1, protocol.getMappingData().getNewItemId(wrapper.get(Types.INT, 1)));
               } else if (id == 2001) {
                  wrapper.set(Types.INT, 1, protocol.getMappingData().getNewBlockStateId(wrapper.get(Types.INT, 1)));
               }
            });
         }
      });
   }
}
