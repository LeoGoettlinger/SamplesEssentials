package com.viaversion.viabackwards.protocol.v26_2to26_1.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viabackwards.api.rewriters.BackwardsStructuredItemRewriter;
import com.viaversion.viabackwards.protocol.v26_2to26_1.Protocol26_2To26_1;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys26_2;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPacket26_1;

public final class BlockItemPacketRewriter26_2 extends BackwardsStructuredItemRewriter<ClientboundPacket26_1, ServerboundPacket26_1, Protocol26_2To26_1> {
   private static final int BED_BLOCK_ENTITY_TYPE_ID = 25;

   public BlockItemPacketRewriter26_2(Protocol26_2To26_1 protocol) {
      super(protocol);
   }

   protected void registerPackets() {
      ((Protocol26_2To26_1)this.protocol).replaceClientbound(ClientboundPackets26_1.LEVEL_CHUNK_WITH_LIGHT, wrapper -> {
         Chunk chunk = ((Protocol26_2To26_1)this.protocol).getBlockRewriter().handleChunk1_18(wrapper);
         ((Protocol26_2To26_1)this.protocol).getBlockRewriter().handleBlockEntities(chunk, wrapper.user());
         EntityTracker tracker = wrapper.user().getEntityTracker(this.protocol);

         for (int i = 0; i < chunk.getSections().length; i++) {
            ChunkSection section = chunk.getSections()[i];
            if (section.getNonAirBlocksCount() != 0) {
               DataPalette blockPalette = section.palette(PaletteType.BLOCKS);
               int ySection = i;
               blockPalette.forEachMatchingCoordinate(BlockItemPacketRewriter26_2::bedBlockState, idx -> {
                  byte packedXZ = (byte)(ChunkSection.xFromIndex(idx) << 4 | ChunkSection.zFromIndex(idx));
                  short y = (short)(ChunkSection.yFromIndex(idx) + tracker.currentMinY() + (ySection << 4));
                  chunk.blockEntities().add(new BlockEntityImpl(packedXZ, y, 25, new CompoundTag()));
               });
            }
         }
      });
      ((Protocol26_2To26_1)this.protocol).replaceClientbound(ClientboundPackets26_1.BLOCK_UPDATE, wrapper -> {
         BlockPosition pos = (BlockPosition)wrapper.passthrough(Types.BLOCK_POSITION1_14);
         int blockId = (Integer)wrapper.read(Types.VAR_INT);
         int mappedBlockId = ((Protocol26_2To26_1)this.protocol).getMappingData().getNewBlockStateId(blockId);
         wrapper.write(Types.VAR_INT, mappedBlockId);
         if (bedBlockState(blockId)) {
            sendBedEntity(wrapper.user(), pos);
         }
      });
      ((Protocol26_2To26_1)this.protocol).replaceClientbound(ClientboundPackets26_1.SECTION_BLOCKS_UPDATE, wrapper -> {
         long position = (Long)wrapper.passthrough(Types.LONG);
         int chunkX = (int)(position >> 42);
         int chunkY = (int)(position << 44 >> 44);
         int chunkZ = (int)(position << 22 >> 42);

         for (BlockChangeRecord record : (BlockChangeRecord[])wrapper.passthrough(Types.VAR_LONG_BLOCK_CHANGE_ARRAY)) {
            int blockId = record.getBlockId();
            record.setBlockId(((Protocol26_2To26_1)this.protocol).getMappingData().getNewBlockStateId(blockId));
            if (bedBlockState(blockId)) {
               int x = record.getSectionX() + (chunkX << 4);
               int y = record.getSectionY() + (chunkY << 4);
               int z = record.getSectionZ() + (chunkZ << 4);
               sendBedEntity(wrapper.user(), new BlockPosition(x, y, z));
            }
         }
      });
   }

   private static boolean bedBlockState(int blockStateId) {
      return blockStateId >= 1931 && blockStateId <= 2186;
   }

   private static void sendBedEntity(UserConnection user, BlockPosition pos) {
      PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets26_1.BLOCK_ENTITY_DATA, user);
      wrapper.write(Types.BLOCK_POSITION1_14, pos);
      wrapper.write(Types.VAR_INT, 25);
      wrapper.write(Types.TRUSTED_COMPOUND_TAG, new CompoundTag());
      wrapper.send(Protocol26_2To26_1.class);
   }

   protected void handleItemDataComponentsToClient(UserConnection connection, Item item, StructuredDataContainer container) {
      super.handleItemDataComponentsToClient(connection, item, container);
      com.viaversion.viaversion.protocols.v26_1to26_2.rewriter.BlockItemPacketRewriter26_2.downgradeData(
         (StructuredDataKeys26_2)((Protocol26_2To26_1)this.protocol).types().structuredDataKeys(), container
      );
   }

   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
      super.handleItemDataComponentsToServer(connection, item, container);
      com.viaversion.viaversion.protocols.v26_1to26_2.rewriter.BlockItemPacketRewriter26_2.upgradeData(container);
   }
}
