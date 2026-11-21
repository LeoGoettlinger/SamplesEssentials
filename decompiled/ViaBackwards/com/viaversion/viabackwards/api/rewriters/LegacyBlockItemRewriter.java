package com.viaversion.viabackwards.api.rewriters;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingDataLoader;
import com.viaversion.viabackwards.api.data.MappedLegacyBlockItem;
import com.viaversion.viabackwards.protocol.v1_12to1_11_1.data.BlockColors1_11_1;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_12.ObjectType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.IdAndData;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class LegacyBlockItemRewriter<C extends ClientboundPacketType, S extends ServerboundPacketType, T extends BackwardsProtocol<C, ?, ?, S>>
   extends BackwardsItemRewriterBase<C, S, T> {
   protected final Int2ObjectMap<MappedLegacyBlockItem> itemReplacements = new Int2ObjectOpenHashMap(8);
   protected final Int2ObjectMap<MappedLegacyBlockItem> blockReplacements = new Int2ObjectOpenHashMap(8);

   protected LegacyBlockItemRewriter(
      T protocol, String name, Type<Item> itemType, Type<Item[]> itemArrayType, Type<Item> mappedItemType, Type<Item[]> mappedItemArrayType
   ) {
      super(protocol, itemType, itemArrayType, mappedItemType, mappedItemArrayType, false);
      Int2ObjectMap<MappedLegacyBlockItem> blockItemReplacements = new Int2ObjectOpenHashMap(8);
      JsonObject jsonObject = this.readMappingsFile("item-mappings-" + name + ".json");
      this.addMappings(MappedLegacyBlockItem.Type.ITEM, jsonObject, this.itemReplacements);
      this.addMappings(MappedLegacyBlockItem.Type.BLOCK_ITEM, jsonObject, blockItemReplacements);
      this.addMappings(MappedLegacyBlockItem.Type.BLOCK, jsonObject, this.blockReplacements);
      this.blockReplacements.putAll(blockItemReplacements);
      this.itemReplacements.putAll(blockItemReplacements);
   }

   protected LegacyBlockItemRewriter(T protocol, String name, Type<Item> itemType, Type<Item[]> itemArrayType) {
      this(protocol, name, itemType, itemArrayType, itemType, itemArrayType);
   }

   protected LegacyBlockItemRewriter(T protocol, String name) {
      this(protocol, name, Types.ITEM1_8, Types.ITEM1_8_SHORT_ARRAY);
   }

   private void addMappings(MappedLegacyBlockItem.Type type, JsonObject object, Int2ObjectMap<MappedLegacyBlockItem> mappings) {
      if (object.has(type.getName())) {
         JsonObject mappingsObject = object.getAsJsonObject(type.getName());

         for (Entry<String, JsonElement> dataEntry : mappingsObject.entrySet()) {
            this.addMapping(dataEntry.getKey(), dataEntry.getValue().getAsJsonObject(), type, mappings);
         }
      }
   }

   private void addMapping(String key, JsonObject object, MappedLegacyBlockItem.Type type, Int2ObjectMap<MappedLegacyBlockItem> mappings) {
      int id = object.getAsJsonPrimitive("id").getAsInt();
      JsonPrimitive jsonData = object.getAsJsonPrimitive("data");
      short data = jsonData != null ? jsonData.getAsShort() : 0;
      String name = type != MappedLegacyBlockItem.Type.BLOCK ? object.getAsJsonPrimitive("name").getAsString() : null;
      if (key.indexOf(45) == -1) {
         int dataSeparatorIndex = key.indexOf(58);
         int unmappedId;
         if (dataSeparatorIndex != -1) {
            short unmappedData = Short.parseShort(key.substring(dataSeparatorIndex + 1));
            unmappedId = Integer.parseInt(key.substring(0, dataSeparatorIndex));
            unmappedId = this.compress(unmappedId, unmappedData);
         } else {
            unmappedId = this.compress(Integer.parseInt(key), -1);
         }

         mappings.put(unmappedId, new MappedLegacyBlockItem(id, data, name, type));
      } else {
         String[] split = key.split("-", 2);
         int from = Integer.parseInt(split[0]);
         int to = Integer.parseInt(split[1]);
         if (name != null && name.contains("%color%")) {
            for (int i = from; i <= to; i++) {
               mappings.put(this.compress(i, -1), new MappedLegacyBlockItem(id, data, name.replace("%color%", BlockColors1_11_1.get(i - from)), type));
            }
         } else {
            MappedLegacyBlockItem mappedBlockItem = new MappedLegacyBlockItem(id, data, name, type);

            for (int i = from; i <= to; i++) {
               mappings.put(this.compress(i, -1), mappedBlockItem);
            }
         }
      }
   }

   public void registerBlockChange(C packetType) {
      ((BackwardsProtocol)this.protocol).registerClientbound(packetType, new PacketHandlers() {
         public void register() {
            this.map(Types.BLOCK_POSITION1_8);
            this.map(Types.VAR_INT);
            this.handler(wrapper -> {
               int idx = (Integer)wrapper.get(Types.VAR_INT, 0);
               wrapper.set(Types.VAR_INT, 0, LegacyBlockItemRewriter.this.handleBlockId(idx));
            });
         }
      });
   }

   public void registerMultiBlockChange(C packetType) {
      ((BackwardsProtocol)this.protocol).registerClientbound(packetType, new PacketHandlers() {
         public void register() {
            this.map(Types.INT);
            this.map(Types.INT);
            this.map(Types.BLOCK_CHANGE_ARRAY);
            this.handler(wrapper -> {
               for (BlockChangeRecord record : (BlockChangeRecord[])wrapper.get(Types.BLOCK_CHANGE_ARRAY, 0)) {
                  record.setBlockId(LegacyBlockItemRewriter.this.handleBlockId(record.getBlockId()));
               }
            });
         }
      });
   }

   @Nullable
   public Item handleItemToClient(UserConnection connection, @Nullable Item item) {
      if (item == null) {
         return null;
      } else {
         MappedLegacyBlockItem data = this.getMappedItem(item.identifier(), item.data());
         if (data == null) {
            return super.handleItemToClient(connection, item);
         } else {
            if (item.tag() == null) {
               item.setTag(new CompoundTag());
            }

            short originalData = item.data();
            item.tag().putInt(this.nbtTagName("id"), item.identifier());
            item.setIdentifier(data.getId());
            if (data.getData() != -1) {
               item.setData(data.getData());
               item.tag().putShort(this.nbtTagName("data"), originalData);
            }

            if (data.getName() != null) {
               CompoundTag display = item.tag().getCompoundTag("display");
               if (display == null) {
                  item.tag().put("display", display = new CompoundTag());
               }

               StringTag nameTag = display.getStringTag("Name");
               if (nameTag == null) {
                  nameTag = new StringTag(data.getName());
                  display.put("Name", nameTag);
                  display.put(this.nbtTagName("customName"), new ByteTag(false));
               }

               String value = nameTag.getValue();
               if (value.contains("%vb_color%")) {
                  display.putString("Name", value.replace("%vb_color%", BlockColors1_11_1.get(originalData)));
               }
            }

            return item;
         }
      }
   }

   @Nullable
   @Override
   public Item handleItemToServer(UserConnection connection, @Nullable Item item) {
      if (item == null) {
         return null;
      } else {
         item = super.handleItemToServer(connection, item);
         if (item.tag() != null) {
            Tag originalId = item.tag().remove(this.nbtTagName("id"));
            if (originalId instanceof IntTag) {
               item.setIdentifier(((NumberTag)originalId).asInt());
            }

            Tag originalData = item.tag().remove(this.nbtTagName("data"));
            if (originalData instanceof ShortTag) {
               item.setData(((NumberTag)originalData).asShort());
            }
         }

         return item;
      }
   }

   public PacketHandler getFallingBlockHandler() {
      return wrapper -> {
         int objectData = (Integer)wrapper.get(Types.INT, 0);
         ObjectType type = ObjectType.findById((Byte)wrapper.get(Types.BYTE, 0), objectData);
         if (type == ObjectType.FALLING_BLOCK) {
            IdAndData block = this.handleBlock(objectData & 4095, objectData >> 12 & 15);
            if (block == null) {
               return;
            }

            wrapper.set(Types.INT, 0, block.getId() | block.getData() << 12);
         }
      };
   }

   @Nullable
   public IdAndData handleBlock(int blockId, int data) {
      MappedLegacyBlockItem settings = this.getMappedBlock(blockId, data);
      if (settings == null) {
         return null;
      } else {
         IdAndData block = settings.getBlock();
         return block.getData() == -1 ? block.withData(data) : block;
      }
   }

   public int handleBlockId(int rawId) {
      int id = IdAndData.getId(rawId);
      int data = IdAndData.getData(rawId);
      IdAndData mappedBlock = this.handleBlock(id, data);
      return mappedBlock == null ? rawId : IdAndData.toRawData(mappedBlock.getId(), mappedBlock.getData());
   }

   public void handleChunk(Chunk chunk) {
      Map<LegacyBlockItemRewriter.Pos, CompoundTag> tags = new HashMap<>();

      for (CompoundTag tag : chunk.getBlockEntities()) {
         NumberTag xTag;
         NumberTag yTag;
         NumberTag zTag;
         if ((xTag = tag.getNumberTag("x")) != null && (yTag = tag.getNumberTag("y")) != null && (zTag = tag.getNumberTag("z")) != null) {
            LegacyBlockItemRewriter.Pos pos = new LegacyBlockItemRewriter.Pos(xTag.asInt() & 15, yTag.asInt(), zTag.asInt() & 15);
            tags.put(pos, tag);
            if (pos.y() >= 0 && pos.y() <= 255) {
               ChunkSection section = chunk.getSections()[pos.y() >> 4];
               if (section != null) {
                  int block = section.palette(PaletteType.BLOCKS).idAt(pos.x(), pos.y() & 15, pos.z());
                  MappedLegacyBlockItem settings = this.getMappedBlock(block);
                  if (settings != null && settings.hasBlockEntityHandler()) {
                     settings.getBlockEntityHandler().handleCompoundTag(block, tag);
                  }
               }
            }
         }
      }

      for (int i = 0; i < chunk.getSections().length; i++) {
         ChunkSection section = chunk.getSections()[i];
         if (section != null) {
            boolean hasBlockEntityHandler = false;
            DataPalette palette = section.palette(PaletteType.BLOCKS);

            for (int j = 0; j < palette.size(); j++) {
               int block = palette.idByIndex(j);
               int btype = block >> 4;
               int meta = block & 15;
               IdAndData b = this.handleBlock(btype, meta);
               if (b != null) {
                  palette.setIdByIndex(j, IdAndData.toRawData(b.getId(), b.getData()));
               }

               if (!hasBlockEntityHandler) {
                  MappedLegacyBlockItem settings = this.getMappedBlock(block);
                  if (settings != null && settings.hasBlockEntityHandler()) {
                     hasBlockEntityHandler = true;
                  }
               }
            }

            if (hasBlockEntityHandler) {
               for (int x = 0; x < 16; x++) {
                  for (int y = 0; y < 16; y++) {
                     for (int z = 0; z < 16; z++) {
                        int blockx = palette.idAt(x, y, z);
                        MappedLegacyBlockItem settings = this.getMappedBlock(blockx);
                        if (settings != null && settings.hasBlockEntityHandler()) {
                           LegacyBlockItemRewriter.Pos pos = new LegacyBlockItemRewriter.Pos(x, y + (i << 4), z);
                           if (!tags.containsKey(pos)) {
                              CompoundTag tagx = new CompoundTag();
                              tagx.putInt("x", x + (chunk.getX() << 4));
                              tagx.putInt("y", y + (i << 4));
                              tagx.putInt("z", z + (chunk.getZ() << 4));
                              settings.getBlockEntityHandler().handleCompoundTag(blockx, tagx);
                              chunk.getBlockEntities().add(tagx);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   protected CompoundTag getNamedTag(String text) {
      CompoundTag tag = new CompoundTag();
      CompoundTag displayTag = new CompoundTag();
      tag.put("display", displayTag);
      text = "§r" + text;
      displayTag.putString("Name", this.jsonNameFormat ? ComponentUtil.legacyToJsonString(text) : text);
      return tag;
   }

   @Nullable
   private MappedLegacyBlockItem getMappedBlock(int id, int data) {
      MappedLegacyBlockItem mapping = (MappedLegacyBlockItem)this.blockReplacements.get(this.compress(id, data));
      return mapping != null ? mapping : (MappedLegacyBlockItem)this.blockReplacements.get(this.compress(id, -1));
   }

   @Nullable
   private MappedLegacyBlockItem getMappedItem(int id, int data) {
      MappedLegacyBlockItem mapping = (MappedLegacyBlockItem)this.itemReplacements.get(this.compress(id, data));
      return mapping != null ? mapping : (MappedLegacyBlockItem)this.itemReplacements.get(this.compress(id, -1));
   }

   @Nullable
   private MappedLegacyBlockItem getMappedBlock(int rawId) {
      int id = IdAndData.getId(rawId);
      int data = IdAndData.getData(rawId);
      return this.getMappedBlock(id, data);
   }

   protected JsonObject readMappingsFile(String name) {
      return BackwardsMappingDataLoader.INSTANCE.loadFromDataDir(name);
   }

   protected int compress(int id, int data) {
      return id << 16 | data & 65535;
   }

   private record Pos(int x, short y, int z) {
      public Pos(int x, int y, int z) {
         this(x, (short)y, z);
      }
   }
}
