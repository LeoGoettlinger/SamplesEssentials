package com.viaversion.viabackwards.protocol.v1_16_2to1_16_1.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.viabackwards.api.rewriters.BackwardsItemRewriter;
import com.viaversion.viabackwards.protocol.v1_16_2to1_16_1.Protocol1_16_2To1_16_1;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord1_8;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_16;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_16_2;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ServerboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.Key;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BlockItemPacketRewriter1_16_2 extends BackwardsItemRewriter<ClientboundPackets1_16_2, ServerboundPackets1_16, Protocol1_16_2To1_16_1> {
   public BlockItemPacketRewriter1_16_2(Protocol1_16_2To1_16_1 protocol) {
      super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_SHORT_ARRAY);
   }

   protected void registerPackets() {
      new RecipeRewriter(this.protocol).register(ClientboundPackets1_16_2.UPDATE_RECIPES);
      ((Protocol1_16_2To1_16_1)this.protocol).registerClientbound(ClientboundPackets1_16_2.RECIPE, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.read(Types.BOOLEAN);
         wrapper.read(Types.BOOLEAN);
         wrapper.read(Types.BOOLEAN);
         wrapper.read(Types.BOOLEAN);
      });
      ((Protocol1_16_2To1_16_1)this.protocol)
         .getBlockRewriter()
         .registerLevelChunk(ClientboundPackets1_16_2.LEVEL_CHUNK, ChunkType1_16_2.TYPE, ChunkType1_16.TYPE, (connection, chunk) -> {
            chunk.setIgnoreOldLightData(true);

            for (CompoundTag blockEntity : chunk.getBlockEntities()) {
               if (blockEntity != null) {
                  this.handleBlockEntity(blockEntity);
               }
            }
         });
      ((Protocol1_16_2To1_16_1)this.protocol).registerClientbound(ClientboundPackets1_16_2.BLOCK_ENTITY_DATA, new PacketHandlers() {
         public void register() {
            this.map(Types.BLOCK_POSITION1_14);
            this.map(Types.UNSIGNED_BYTE);
            this.handler(wrapper -> BlockItemPacketRewriter1_16_2.this.handleBlockEntity((CompoundTag)wrapper.passthrough(Types.NAMED_COMPOUND_TAG)));
         }
      });
      ((Protocol1_16_2To1_16_1)this.protocol)
         .registerClientbound(ClientboundPackets1_16_2.SECTION_BLOCKS_UPDATE, ClientboundPackets1_16.CHUNK_BLOCKS_UPDATE, wrapper -> {
            long chunkPosition = (Long)wrapper.read(Types.LONG);
            wrapper.read(Types.BOOLEAN);
            int chunkX = (int)(chunkPosition >> 42);
            int chunkY = (int)(chunkPosition << 44 >> 44);
            int chunkZ = (int)(chunkPosition << 22 >> 42);
            wrapper.write(Types.INT, chunkX);
            wrapper.write(Types.INT, chunkZ);
            BlockChangeRecord[] blockChangeRecord = (BlockChangeRecord[])wrapper.read(Types.VAR_LONG_BLOCK_CHANGE_ARRAY);
            wrapper.write(Types.BLOCK_CHANGE_ARRAY, blockChangeRecord);

            for (int i = 0; i < blockChangeRecord.length; i++) {
               BlockChangeRecord record = blockChangeRecord[i];
               int blockId = ((Protocol1_16_2To1_16_1)this.protocol).getMappingData().getNewBlockStateId(record.getBlockId());
               blockChangeRecord[i] = new BlockChangeRecord1_8(record.getSectionX(), record.getY(chunkY), record.getSectionZ(), blockId);
            }
         });
      ((Protocol1_16_2To1_16_1)this.protocol)
         .registerServerbound(ServerboundPackets1_16.EDIT_BOOK, wrapper -> this.handleItemToServer(wrapper.user(), (Item)wrapper.passthrough(Types.ITEM1_13_2)));
   }

   @Nullable
   @Override
   public Item handleItemToClient(UserConnection connection, @Nullable Item item) {
      if (item != null && item.tag() != null) {
         this.addValueHashAsId(item.tag());
      }

      return super.handleItemToClient(connection, item);
   }

   private void handleBlockEntity(CompoundTag tag) {
      String id = tag.getString("id");
      if (id != null && Key.stripMinecraftNamespace(id).equals("skull")) {
         this.addValueHashAsId(tag);
      }
   }

   private void addValueHashAsId(CompoundTag tag) {
      CompoundTag skullOwnerTag = tag.getCompoundTag("SkullOwner");
      if (skullOwnerTag != null) {
         if (skullOwnerTag.contains("Id")) {
            CompoundTag properties = skullOwnerTag.getCompoundTag("Properties");
            if (properties != null) {
               ListTag<CompoundTag> textures = properties.getListTag("textures", CompoundTag.class);
               if (textures != null) {
                  CompoundTag first = !textures.isEmpty() ? (CompoundTag)textures.get(0) : null;
                  if (first != null) {
                     int hashCode = first.get("Value").getValue().hashCode();
                     int[] uuidIntArray = new int[]{hashCode, 0, 0, 0};
                     skullOwnerTag.put("Id", new IntArrayTag(uuidIntArray));
                  }
               }
            }
         }
      }
   }
}
