package com.viaversion.viaversion.rewriter;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import com.viaversion.viaversion.util.MathUtil;
import java.util.List;
import java.util.function.BiConsumer;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BlockRewriter<C extends ClientboundPacketType> {
   protected final Protocol<C, ?, ?, ?> protocol;
   private final Type<BlockPosition> positionType;
   private final Type<CompoundTag> compoundTagType;
   private final BlockRewriter.ChunkTypeSupplier chunkTypeSupplier;
   private final BlockRewriter.ChunkTypeSupplier mappedChunkTypeSupplier;

   public BlockRewriter(
      Protocol<C, ?, ?, ?> protocol,
      Type<BlockPosition> positionType,
      Type<CompoundTag> compoundTagType,
      BlockRewriter.ChunkTypeSupplier chunkTypeSupplier,
      @Nullable BlockRewriter.ChunkTypeSupplier mappedChunkTypeSupplier
   ) {
      this.protocol = protocol;
      this.positionType = positionType;
      this.compoundTagType = compoundTagType;
      this.chunkTypeSupplier = chunkTypeSupplier;
      this.mappedChunkTypeSupplier = mappedChunkTypeSupplier != null ? mappedChunkTypeSupplier : chunkTypeSupplier;
   }

   public static <C extends ClientboundPacketType> BlockRewriter<C> legacy(Protocol<C, ?, ?, ?> protocol) {
      return new BlockRewriter<>(protocol, Types.BLOCK_POSITION1_8, Types.NAMED_COMPOUND_TAG, null, null);
   }

   public static <C extends ClientboundPacketType> BlockRewriter<C> for1_14(Protocol<C, ?, ?, ?> protocol) {
      return new BlockRewriter<>(protocol, Types.BLOCK_POSITION1_14, Types.NAMED_COMPOUND_TAG, null, null);
   }

   public static <C extends ClientboundPacketType> BlockRewriter<C> for1_18(Protocol<C, ?, ?, ?> protocol, BlockRewriter.ChunkTypeSupplier chunkTypeSupplier) {
      return for1_18(protocol, chunkTypeSupplier, null);
   }

   public static <C extends ClientboundPacketType> BlockRewriter<C> for1_18(
      Protocol<C, ?, ?, ?> protocol, BlockRewriter.ChunkTypeSupplier chunkTypeSupplier, BlockRewriter.ChunkTypeSupplier mappedChunkTypeSupplier
   ) {
      return new BlockRewriter<>(protocol, Types.BLOCK_POSITION1_14, Types.NAMED_COMPOUND_TAG, chunkTypeSupplier, mappedChunkTypeSupplier);
   }

   public static <C extends ClientboundPacketType> BlockRewriter<C> for1_20_2(Protocol<C, ?, ?, ?> protocol, BlockRewriter.ChunkTypeSupplier chunkTypeSupplier) {
      return for1_20_2(protocol, chunkTypeSupplier, null);
   }

   public static <C extends ClientboundPacketType> BlockRewriter<C> for1_20_2(
      Protocol<C, ?, ?, ?> protocol, BlockRewriter.ChunkTypeSupplier chunkTypeSupplier, BlockRewriter.ChunkTypeSupplier mappedChunkTypeSupplier
   ) {
      return new BlockRewriter<>(protocol, Types.BLOCK_POSITION1_14, Types.TRUSTED_COMPOUND_TAG, chunkTypeSupplier, mappedChunkTypeSupplier);
   }

   public void registerBlockEvent(C packetType) {
      if (!Mappings.isIntIdIdentity(this.protocol.getMappingData().getBlockMappings())) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(this.positionType);
            wrapper.passthrough(Types.UNSIGNED_BYTE);
            wrapper.passthrough(Types.UNSIGNED_BYTE);
            int blockId = wrapper.passthrough(Types.VAR_INT);
            int mappedId = this.protocol.getMappingData().getNewBlockId(blockId);
            if (mappedId == -1) {
               wrapper.cancel();
            } else {
               if (blockId != mappedId) {
                  wrapper.set(Types.VAR_INT, 0, mappedId);
               }
            }
         });
      }
   }

   public void registerBlockUpdate(C packetType) {
      if (!Mappings.isIntIdIdentity(this.protocol.getMappingData().getBlockStateMappings())) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(this.positionType);
            int blockId = wrapper.read(Types.VAR_INT);
            wrapper.write(Types.VAR_INT, this.protocol.getMappingData().getNewBlockStateId(blockId));
         });
      }
   }

   public void registerChunkBlocksUpdate(C packetType) {
      if (!Mappings.isIntIdIdentity(this.protocol.getMappingData().getBlockStateMappings())) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);

            for (BlockChangeRecord record : wrapper.passthrough(Types.BLOCK_CHANGE_ARRAY)) {
               record.setBlockId(this.protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
            }
         });
      }
   }

   public void registerSectionBlocksUpdate(C packetType) {
      if (!Mappings.isIntIdIdentity(this.protocol.getMappingData().getBlockStateMappings())) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.LONG);
            wrapper.passthrough(Types.BOOLEAN);

            for (BlockChangeRecord record : wrapper.passthrough(Types.VAR_LONG_BLOCK_CHANGE_ARRAY)) {
               record.setBlockId(this.protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
            }
         });
      }
   }

   public void registerSectionBlocksUpdate1_20(C packetType) {
      if (!Mappings.isIntIdIdentity(this.protocol.getMappingData().getBlockStateMappings())) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.LONG);

            for (BlockChangeRecord record : wrapper.passthrough(Types.VAR_LONG_BLOCK_CHANGE_ARRAY)) {
               record.setBlockId(this.protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
            }
         });
      }
   }

   public void registerBlockBreakAck(C packetType) {
      this.registerBlockUpdate(packetType);
   }

   public void registerLevelEvent1_13(C packetType) {
      this.registerLevelEvent(packetType, 1010, 2001);
   }

   public void registerLevelEvent1_21(C packetType) {
      this.registerLevelEvent(packetType, -1, 2001);
   }

   private void registerLevelEvent(C packetType, int playRecordId, int blockBreakId) {
      MappingData mappingData = this.protocol.getMappingData();
      if (mappingData != null && (!Mappings.isIntIdIdentity(mappingData.getItemMappings()) || !Mappings.isIntIdIdentity(mappingData.getBlockStateMappings()))) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            int id = wrapper.passthrough(Types.INT);
            wrapper.passthrough(this.positionType);
            int data = wrapper.read(Types.INT);
            if (playRecordId != -1 && id == playRecordId && mappingData.getItemMappings() != null) {
               wrapper.write(Types.INT, mappingData.getNewItemId(data));
            } else if (id == blockBreakId && mappingData.getBlockStateMappings() != null) {
               wrapper.write(Types.INT, mappingData.getNewBlockStateId(data));
            } else {
               wrapper.write(Types.INT, data);
            }
         });
      }
   }

   public void registerLevelChunk(C packetType, Type<Chunk> chunkType, Type<Chunk> newChunkType) {
      this.registerLevelChunk(packetType, chunkType, newChunkType, null);
   }

   public void registerLevelChunk(C packetType, Type<Chunk> chunkType, Type<Chunk> newChunkType, @Nullable BiConsumer<UserConnection, Chunk> chunkRewriter) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         Chunk chunk = wrapper.read(chunkType);
         wrapper.write(newChunkType, chunk);
         this.handleChunk(chunk);
         if (chunkRewriter != null) {
            chunkRewriter.accept(wrapper.user(), chunk);
         }
      });
   }

   public void handleChunk(Chunk chunk) {
      for (int s = 0; s < chunk.getSections().length; s++) {
         ChunkSection section = chunk.getSections()[s];
         if (section != null) {
            DataPalette palette = section.palette(PaletteType.BLOCKS);
            palette.replaceIds(this.protocol.getMappingData()::getNewBlockStateId);
         }
      }
   }

   public void registerLevelChunk1_18(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         Chunk chunk = this.handleChunk1_18(wrapper);
         this.handleBlockEntities(chunk, wrapper.user());
      });
   }

   public void handleBlockEntities(Chunk chunk, UserConnection connection) {
      FullMappings blockEntityMappings = this.protocol.getMappingData().getBlockEntityMappings();
      List<BlockEntity> blockEntities = chunk.blockEntities();
      IntList toRemove = new IntArrayList(0);

      for (int i = 0; i < blockEntities.size(); i++) {
         BlockEntity blockEntity = blockEntities.get(i);
         if (blockEntityMappings != null) {
            int id = blockEntity.typeId();
            int mappedId = blockEntityMappings.getNewId(id);
            if (mappedId == -1) {
               toRemove.add(i);
               continue;
            }

            if (id != mappedId) {
               blockEntity = blockEntity.withTypeId(mappedId);
               blockEntities.set(i, blockEntity);
            }
         }

         if (blockEntity.tag() != null) {
            this.handleBlockEntity(connection, blockEntity);
         }
      }

      if (!toRemove.isEmpty()) {
         for (int i = toRemove.size() - 1; i >= 0; i--) {
            blockEntities.remove(toRemove.getInt(i));
         }
      }
   }

   public Chunk handleChunk1_18(PacketWrapper wrapper) {
      EntityTracker tracker = this.protocol.getEntityRewriter().tracker(wrapper.user());
      Preconditions.checkArgument(tracker.biomesSent() != -1, "Biome count not set");
      Preconditions.checkArgument(tracker.currentWorldSectionHeight() != -1, "Section height not set");
      Type<Chunk> chunkType = this.createChunkType(this.chunkTypeSupplier, tracker, false);
      Type<Chunk> mappedChunkType = this.createChunkType(this.mappedChunkTypeSupplier, tracker, true);
      Chunk chunk = wrapper.passthroughAndMap(chunkType, mappedChunkType);
      if (Mappings.isIntIdIdentity(this.protocol.getMappingData().getBlockStateMappings())) {
         return chunk;
      } else {
         for (ChunkSection section : chunk.getSections()) {
            DataPalette blockPalette = section.palette(PaletteType.BLOCKS);
            blockPalette.replaceIds(this.protocol.getMappingData()::getNewBlockStateId);
         }

         return chunk;
      }
   }

   protected Type<Chunk> createChunkType(BlockRewriter.ChunkTypeSupplier supplier, EntityTracker tracker, boolean mapped) {
      Type<Chunk> type = tracker.chunkType(mapped);
      if (type == null) {
         Mappings blockStateMappings = this.protocol.getMappingData().getBlockStateMappings();
         type = supplier.supply(
            tracker.currentWorldSectionHeight(),
            MathUtil.ceilLog2(mapped ? blockStateMappings.mappedSize() : blockStateMappings.size()),
            MathUtil.ceilLog2(tracker.biomesSent())
         );
         tracker.setChunkType(mapped, type);
      }

      return type;
   }

   public void registerBlockEntityData1_18(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         BlockPosition position = wrapper.passthrough(this.positionType);
         int blockEntityId = wrapper.read(Types.VAR_INT);
         Mappings mappings = this.protocol.getMappingData().getBlockEntityMappings();
         if (mappings != null) {
            int mappedBlockEntityId = mappings.getNewId(blockEntityId);
            if (mappedBlockEntityId == -1) {
               wrapper.cancel();
               return;
            }

            wrapper.write(Types.VAR_INT, mappedBlockEntityId);
         } else {
            wrapper.write(Types.VAR_INT, blockEntityId);
         }

         CompoundTag tag = wrapper.passthrough(this.compoundTagType);
         if (tag != null) {
            BlockEntity blockEntity = new BlockEntityImpl(BlockEntity.pack(position.x(), position.z()), (short)position.y(), blockEntityId, tag);
            this.handleBlockEntity(wrapper.user(), blockEntity);
         }
      });
   }

   public void handleBlockEntity(UserConnection connection, BlockEntity blockEntity) {
   }

   @FunctionalInterface
   public interface ChunkTypeSupplier {
      Type<Chunk> supply(int var1, int var2, int var3);
   }
}
