package com.viaversion.viabackwards.protocol.v1_16to1_15_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongArrayTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.rewriters.BackwardsItemRewriter;
import com.viaversion.viabackwards.api.rewriters.EnchantmentRewriter;
import com.viaversion.viabackwards.api.rewriters.MapColorRewriter;
import com.viaversion.viabackwards.protocol.v1_16_2to1_16_1.storage.BiomeStorage;
import com.viaversion.viabackwards.protocol.v1_16to1_15_2.Protocol1_16To1_15_2;
import com.viaversion.viabackwards.protocol.v1_16to1_15_2.data.MapColorMappings1_15_2;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_15;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_16;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packet.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.rewriter.ItemPacketRewriter1_16;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.CompactArrayUtil;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.UUIDUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

public class BlockItemPacketRewriter1_16 extends BackwardsItemRewriter<ClientboundPackets1_16, ServerboundPackets1_14, Protocol1_16To1_15_2> {
   private EnchantmentRewriter enchantmentRewriter;

   public BlockItemPacketRewriter1_16(Protocol1_16To1_15_2 protocol) {
      super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_SHORT_ARRAY);
   }

   protected void registerPackets() {
      RecipeRewriter<ClientboundPackets1_16> recipeRewriter = new RecipeRewriter(this.protocol);
      ((Protocol1_16To1_15_2)this.protocol).registerClientbound(ClientboundPackets1_16.UPDATE_RECIPES, wrapper -> {
         int size = (Integer)wrapper.passthrough(Types.VAR_INT);
         int newSize = size;

         for (int i = 0; i < size; i++) {
            String originalType = (String)wrapper.read(Types.STRING);
            String type = Key.stripMinecraftNamespace(originalType);
            if (type.equals("smithing")) {
               newSize--;
               wrapper.read(Types.STRING);
               wrapper.read(Types.ITEM1_13_2_ARRAY);
               wrapper.read(Types.ITEM1_13_2_ARRAY);
               wrapper.read(Types.ITEM1_13_2);
            } else {
               wrapper.write(Types.STRING, originalType);
               wrapper.passthrough(Types.STRING);
               recipeRewriter.handleRecipeType(wrapper, type);
            }
         }

         wrapper.set(Types.VAR_INT, 0, newSize);
      });
      ((Protocol1_16To1_15_2)this.protocol)
         .getBlockRewriter()
         .registerLevelChunk(ClientboundPackets1_16.LEVEL_CHUNK, ChunkType1_16.TYPE, ChunkType1_15.TYPE, (connection, chunk) -> {
            CompoundTag heightMaps = chunk.getHeightMap();

            for (Tag heightMapTag : heightMaps.values()) {
               if (heightMapTag instanceof LongArrayTag heightMap) {
                  int[] heightMapData = new int[256];
                  CompactArrayUtil.iterateCompactArrayWithPadding(9, heightMapData.length, heightMap.getValue(), (i, v) -> heightMapData[i] = v);
                  heightMap.setValue(CompactArrayUtil.createCompactArray(9, heightMapData.length, i -> heightMapData[i]));
               }
            }

            if (chunk.isBiomeData()) {
               if (connection.getProtocolInfo().serverProtocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_16_2)) {
                  BiomeStorage biomeStorage = (BiomeStorage)connection.get(BiomeStorage.class);

                  for (int i = 0; i < 1024; i++) {
                     int biome = chunk.getBiomeData()[i];
                     int legacyBiome = biomeStorage.legacyBiome(biome);
                     if (legacyBiome == -1) {
                        ((Protocol1_16To1_15_2)this.protocol).getLogger().warning("Biome sent that does not exist in the biome registry: " + biome);
                        legacyBiome = 1;
                     }

                     chunk.getBiomeData()[i] = legacyBiome;
                  }
               } else {
                  for (int i = 0; i < 1024; i++) {
                     int biome = chunk.getBiomeData()[i];
                     switch (biome) {
                        case 170:
                        case 171:
                        case 172:
                        case 173:
                           chunk.getBiomeData()[i] = 8;
                           break;
                     }
                  }
               }
            }

            if (chunk.getBlockEntities() != null) {
               for (CompoundTag blockEntity : chunk.getBlockEntities()) {
                  this.handleBlockEntity(blockEntity);
               }
            }
         });
      ((Protocol1_16To1_15_2)this.protocol).registerClientbound(ClientboundPackets1_16.SET_EQUIPMENT, ClientboundPackets1_15.SET_EQUIPPED_ITEM, wrapper -> {
         int entityId = (Integer)wrapper.passthrough(Types.VAR_INT);
         List<BlockItemPacketRewriter1_16.EquipmentData> equipmentData = new ArrayList<>();

         byte slot;
         do {
            slot = (Byte)wrapper.read(Types.BYTE);
            Item item = this.handleItemToClient(wrapper.user(), (Item)wrapper.read(Types.ITEM1_13_2));
            int rawSlot = slot & 127;
            equipmentData.add(new BlockItemPacketRewriter1_16.EquipmentData(rawSlot, item));
         } while ((slot & -128) != 0);

         BlockItemPacketRewriter1_16.EquipmentData firstData = equipmentData.get(0);
         wrapper.write(Types.VAR_INT, firstData.slot);
         wrapper.write(Types.ITEM1_13_2, firstData.item);

         for (int i = 1; i < equipmentData.size(); i++) {
            PacketWrapper equipmentPacket = wrapper.create(ClientboundPackets1_15.SET_EQUIPPED_ITEM);
            BlockItemPacketRewriter1_16.EquipmentData data = equipmentData.get(i);
            equipmentPacket.write(Types.VAR_INT, entityId);
            equipmentPacket.write(Types.VAR_INT, data.slot);
            equipmentPacket.write(Types.ITEM1_13_2, data.item);
            equipmentPacket.send(Protocol1_16To1_15_2.class);
         }
      });
      ((Protocol1_16To1_15_2)this.protocol).registerClientbound(ClientboundPackets1_16.LIGHT_UPDATE, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.read(Types.BOOLEAN);
         }
      });
      ((Protocol1_16To1_15_2)this.protocol).registerClientbound(ClientboundPackets1_16.CONTAINER_SET_DATA, new PacketHandlers() {
         public void register() {
            this.map(Types.UNSIGNED_BYTE);
            this.map(Types.SHORT);
            this.map(Types.SHORT);
            this.handler(wrapper -> {
               short property = (Short)wrapper.get(Types.SHORT, 0);
               if (property >= 4 && property <= 6) {
                  short enchantmentId = (Short)wrapper.get(Types.SHORT, 1);
                  if (enchantmentId > 11) {
                     wrapper.set(Types.SHORT, 1, --enchantmentId);
                  } else if (enchantmentId == 11) {
                     wrapper.set(Types.SHORT, 1, (short)9);
                  }
               }
            });
         }
      });
      ((Protocol1_16To1_15_2)this.protocol).registerClientbound(ClientboundPackets1_16.MAP_ITEM_DATA, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.BYTE);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.handler(MapColorRewriter.getRewriteHandler(MapColorMappings1_15_2::getMappedColor));
         }
      });
      ((Protocol1_16To1_15_2)this.protocol).registerClientbound(ClientboundPackets1_16.BLOCK_ENTITY_DATA, wrapper -> {
         wrapper.passthrough(Types.BLOCK_POSITION1_14);
         wrapper.passthrough(Types.UNSIGNED_BYTE);
         CompoundTag tag = (CompoundTag)wrapper.passthrough(Types.NAMED_COMPOUND_TAG);
         this.handleBlockEntity(tag);
      });
      ((Protocol1_16To1_15_2)this.protocol)
         .registerServerbound(ServerboundPackets1_14.EDIT_BOOK, wrapper -> this.handleItemToServer(wrapper.user(), (Item)wrapper.passthrough(Types.ITEM1_13_2)));
   }

   private void handleBlockEntity(CompoundTag tag) {
      String id = tag.getString("id");
      if (id != null) {
         id = Key.namespaced(id);
         if (id.equals("minecraft:conduit")) {
            Tag targetUuidTag = tag.remove("Target");
            if (!(targetUuidTag instanceof IntArrayTag)) {
               return;
            }

            UUID targetUuid = UUIDUtil.fromIntArray((int[])targetUuidTag.getValue());
            tag.putString("target_uuid", targetUuid.toString());
         } else if (id.equals("minecraft:skull")) {
            if (!(tag.remove("SkullOwner") instanceof CompoundTag skullOwnerTag)) {
               return;
            }

            if (skullOwnerTag.remove("Id") instanceof IntArrayTag ownerUuidTag) {
               UUID ownerUuid = UUIDUtil.fromIntArray(ownerUuidTag.getValue());
               skullOwnerTag.putString("Id", ownerUuid.toString());
            }

            CompoundTag ownerTag = new CompoundTag();

            for (Entry<String, Tag> entry : skullOwnerTag) {
               ownerTag.put(entry.getKey(), entry.getValue());
            }

            tag.put("Owner", ownerTag);
         }
      }
   }

   protected void registerRewrites() {
      this.enchantmentRewriter = new EnchantmentRewriter(this);
      this.enchantmentRewriter.registerEnchantment("minecraft:soul_speed", "§7Soul Speed");
   }

   @Override
   public Item handleItemToClient(UserConnection connection, Item item) {
      if (item == null) {
         return null;
      } else {
         item = super.handleItemToClient(connection, item);
         CompoundTag tag = item.tag();
         if (item.identifier() == 771 && tag != null) {
            CompoundTag ownerTag = tag.getCompoundTag("SkullOwner");
            if (ownerTag != null) {
               IntArrayTag idTag = ownerTag.getIntArrayTag("Id");
               if (idTag != null) {
                  UUID ownerUuid = UUIDUtil.fromIntArray(idTag.getValue());
                  ownerTag.putString("Id", ownerUuid.toString());
               }
            }
         }

         if (item.identifier() == 759 && tag != null) {
            ListTag<StringTag> pagesTag = tag.getListTag("pages", StringTag.class);
            if (pagesTag != null) {
               for (StringTag page : pagesTag) {
                  JsonElement jsonElement = ((Protocol1_16To1_15_2)this.protocol).getComponentRewriter().processText(connection, page.getValue());
                  page.setValue(jsonElement.toString());
               }
            }
         }

         ItemPacketRewriter1_16.newToOldAttributes(item);
         this.enchantmentRewriter.handleToClient(item);
         return item;
      }
   }

   @Override
   public Item handleItemToServer(UserConnection connection, Item item) {
      if (item == null) {
         return null;
      } else {
         int identifier = item.identifier();
         item = super.handleItemToServer(connection, item);
         CompoundTag tag = item.tag();
         if (identifier == 771 && tag != null) {
            CompoundTag ownerTag = tag.getCompoundTag("SkullOwner");
            if (ownerTag != null) {
               StringTag idTag = ownerTag.getStringTag("Id");
               if (idTag != null) {
                  UUID ownerUuid = UUID.fromString(idTag.getValue());
                  ownerTag.put("Id", new IntArrayTag(UUIDUtil.toIntArray(ownerUuid)));
               }
            }
         }

         ItemPacketRewriter1_16.oldToNewAttributes(item);
         this.enchantmentRewriter.handleToServer(item);
         return item;
      }
   }

   private record EquipmentData(int slot, Item item) {
   }
}
