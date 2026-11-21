package com.viaversion.viabackwards.protocol.v1_14to1_13_2.rewriter;

import com.google.common.collect.ImmutableSet;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viabackwards.api.data.TranslatableMappings;
import com.viaversion.viabackwards.api.rewriters.BackwardsItemRewriter;
import com.viaversion.viabackwards.api.rewriters.EnchantmentRewriter;
import com.viaversion.viabackwards.item.DataItemWithExtras;
import com.viaversion.viabackwards.protocol.v1_14to1_13_2.Protocol1_14To1_13_2;
import com.viaversion.viabackwards.protocol.v1_14to1_13_2.storage.ChunkLightStorage;
import com.viaversion.viabackwards.protocol.v1_14to1_13_2.storage.ProtocolStorables1_14;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.Environment;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionLight;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionLightImpl;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.NibbleArray;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_13;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_14;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.api.type.types.version.Types1_13_2;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParseException;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.utils.TextUtils;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.Protocol1_13_2To1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ClientboundPackets1_14;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.SerializerVersion;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockItemPacketRewriter1_14 extends BackwardsItemRewriter<ClientboundPackets1_14, ServerboundPackets1_13, Protocol1_14To1_13_2> {
   private EnchantmentRewriter enchantmentRewriter;

   public BlockItemPacketRewriter1_14(Protocol1_14To1_13_2 protocol) {
      super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_SHORT_ARRAY);
   }

   protected void registerPackets() {
      ((Protocol1_14To1_13_2)this.protocol)
         .registerServerbound(ServerboundPackets1_13.EDIT_BOOK, wrapper -> this.handleItemToServer(wrapper.user(), (Item)wrapper.passthrough(Types.ITEM1_13_2)));
      ((Protocol1_14To1_13_2)this.protocol)
         .replaceClientbound(
            ClientboundPackets1_14.OPEN_SCREEN,
            wrapper -> {
               int windowId = (Integer)wrapper.read(Types.VAR_INT);
               wrapper.write(Types.UNSIGNED_BYTE, (short)windowId);
               int type = (Integer)wrapper.read(Types.VAR_INT);
               String stringType = null;
               String containerTitle = null;
               int slotSize = 0;
               if (type < 6) {
                  if (type == 2) {
                     containerTitle = "Barrel";
                  }

                  stringType = "minecraft:container";
                  slotSize = (type + 1) * 9;
               } else {
                  switch (type) {
                     case 6:
                        stringType = "minecraft:dropper";
                        slotSize = 9;
                        break;
                     case 7:
                     case 21:
                        if (type == 21) {
                           containerTitle = "Cartography Table";
                        }

                        stringType = "minecraft:anvil";
                        break;
                     case 8:
                        stringType = "minecraft:beacon";
                        slotSize = 1;
                        break;
                     case 9:
                     case 13:
                     case 14:
                     case 20:
                        if (type == 9) {
                           containerTitle = "Blast Furnace";
                        } else if (type == 20) {
                           containerTitle = "Smoker";
                        } else if (type == 14) {
                           containerTitle = "Grindstone";
                        }

                        stringType = "minecraft:furnace";
                        slotSize = 3;
                        break;
                     case 10:
                        stringType = "minecraft:brewing_stand";
                        slotSize = 5;
                        break;
                     case 11:
                        stringType = "minecraft:crafting_table";
                        break;
                     case 12:
                        stringType = "minecraft:enchanting_table";
                        break;
                     case 15:
                        stringType = "minecraft:hopper";
                        slotSize = 5;
                     case 16:
                     case 17:
                     default:
                        break;
                     case 18:
                        stringType = "minecraft:villager";
                        break;
                     case 19:
                        stringType = "minecraft:shulker_box";
                        slotSize = 27;
                  }
               }

               if (stringType == null) {
                  wrapper.cancel();
               } else {
                  wrapper.write(Types.STRING, stringType);
                  JsonElement title = (JsonElement)wrapper.read(Types.COMPONENT);
                  JsonObject object;
                  if (containerTitle != null
                     && title.isJsonObject()
                     && (object = title.getAsJsonObject()).has("translate")
                     && (type != 2 || object.getAsJsonPrimitive("translate").getAsString().equals("container.barrel"))) {
                     title = ComponentUtil.legacyToJson(containerTitle);
                  }

                  wrapper.write(Types.COMPONENT, title);
                  wrapper.write(Types.UNSIGNED_BYTE, (short)slotSize);
               }
            }
         );
      ((Protocol1_14To1_13_2)this.protocol).registerClientbound(ClientboundPackets1_14.HORSE_SCREEN_OPEN, ClientboundPackets1_13.OPEN_SCREEN, wrapper -> {
         wrapper.passthrough(Types.UNSIGNED_BYTE);
         wrapper.write(Types.STRING, "EntityHorse");
         JsonObject object = new JsonObject();
         object.addProperty("translate", "minecraft.horse");
         wrapper.write(Types.COMPONENT, object);
         wrapper.write(Types.UNSIGNED_BYTE, ((Integer)wrapper.read(Types.VAR_INT)).shortValue());
         wrapper.passthrough(Types.INT);
      });
      ((Protocol1_14To1_13_2)this.protocol).registerClientbound(ClientboundPackets1_14.MERCHANT_OFFERS, ClientboundPackets1_13.CUSTOM_PAYLOAD, wrapper -> {
         wrapper.write(Types.STRING, "minecraft:trader_list");
         int windowId = (Integer)wrapper.read(Types.VAR_INT);
         wrapper.write(Types.INT, windowId);
         int size = (Short)wrapper.passthrough(Types.UNSIGNED_BYTE);

         for (int i = 0; i < size; i++) {
            Item input = (Item)wrapper.read(Types.ITEM1_13_2);
            input = this.handleItemToClient(wrapper.user(), input);
            wrapper.write(Types.ITEM1_13_2, input);
            Item output = (Item)wrapper.read(Types.ITEM1_13_2);
            output = this.handleItemToClient(wrapper.user(), output);
            wrapper.write(Types.ITEM1_13_2, output);
            boolean secondItem = (Boolean)wrapper.passthrough(Types.BOOLEAN);
            if (secondItem) {
               Item second = (Item)wrapper.read(Types.ITEM1_13_2);
               second = this.handleItemToClient(wrapper.user(), second);
               wrapper.write(Types.ITEM1_13_2, second);
            }

            wrapper.passthrough(Types.BOOLEAN);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.INT);
            wrapper.read(Types.INT);
            wrapper.read(Types.INT);
            wrapper.read(Types.FLOAT);
         }

         wrapper.read(Types.VAR_INT);
         wrapper.read(Types.VAR_INT);
         wrapper.read(Types.BOOLEAN);
      }, true);
      ((Protocol1_14To1_13_2)this.protocol).registerClientbound(ClientboundPackets1_14.OPEN_BOOK, ClientboundPackets1_13.CUSTOM_PAYLOAD, wrapper -> {
         wrapper.write(Types.STRING, "minecraft:book_open");
         wrapper.passthrough(Types.VAR_INT);
      });
      ((Protocol1_14To1_13_2)this.protocol).replaceClientbound(ClientboundPackets1_14.SET_EQUIPPED_ITEM, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.handler(wrapper -> BlockItemPacketRewriter1_14.this.passthroughClientboundItem(wrapper));
            this.handler(wrapper -> {
               int entityId = (Integer)wrapper.get(Types.VAR_INT, 0);
               EntityType entityType = wrapper.user().getEntityTracker(BlockItemPacketRewriter1_14.this.protocol).entityType(entityId);
               if (entityType != null) {
                  if (entityType.isOrHasParent(EntityTypes1_14.ABSTRACT_HORSE)) {
                     wrapper.setPacketType(ClientboundPackets1_13.SET_ENTITY_DATA);
                     wrapper.resetReader();
                     wrapper.passthrough(Types.VAR_INT);
                     wrapper.read(Types.VAR_INT);
                     Item item = (Item)wrapper.read(Types.ITEM1_13_2);
                     int armorType = item != null && item.identifier() != 0 ? item.identifier() - 726 : 0;
                     if (armorType < 0 || armorType > 3) {
                        wrapper.cancel();
                        return;
                     }

                     List<EntityData> entityDataList = new ArrayList<>();
                     entityDataList.add(new EntityData(16, Types1_13_2.ENTITY_DATA_TYPES.varIntType, armorType));
                     wrapper.write(Types1_13.ENTITY_DATA_LIST, entityDataList);
                  }
               }
            });
         }
      });
      RecipeRewriter<ClientboundPackets1_14> recipeHandler = new RecipeRewriter(this.protocol);
      Set<String> removedTypes = ImmutableSet.of("crafting_special_suspiciousstew", "blasting", "smoking", "campfire_cooking", "stonecutting");
      ((Protocol1_14To1_13_2)this.protocol).registerClientbound(ClientboundPackets1_14.UPDATE_RECIPES, wrapper -> {
         int size = (Integer)wrapper.passthrough(Types.VAR_INT);
         int deleted = 0;

         for (int i = 0; i < size; i++) {
            String type = (String)wrapper.read(Types.STRING);
            String id = (String)wrapper.read(Types.STRING);
            type = Key.stripMinecraftNamespace(type);
            if (removedTypes.contains(type)) {
               switch (type) {
                  case "blasting":
                  case "smoking":
                  case "campfire_cooking":
                     wrapper.read(Types.STRING);
                     wrapper.read(Types.ITEM1_13_2_ARRAY);
                     wrapper.read(Types.ITEM1_13_2);
                     wrapper.read(Types.FLOAT);
                     wrapper.read(Types.VAR_INT);
                     break;
                  case "stonecutting":
                     wrapper.read(Types.STRING);
                     wrapper.read(Types.ITEM1_13_2_ARRAY);
                     wrapper.read(Types.ITEM1_13_2);
               }

               deleted++;
            } else {
               wrapper.write(Types.STRING, id);
               wrapper.write(Types.STRING, type);
               recipeHandler.handleRecipeType(wrapper, type);
            }
         }

         wrapper.set(Types.VAR_INT, 0, size - deleted);
      });
      ((Protocol1_14To1_13_2)this.protocol).registerClientbound(ClientboundPackets1_14.BLOCK_DESTRUCTION, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.BLOCK_POSITION1_14, Types.BLOCK_POSITION1_8);
            this.map(Types.BYTE);
         }
      });
      ((Protocol1_14To1_13_2)this.protocol).registerClientbound(ClientboundPackets1_14.BLOCK_ENTITY_DATA, new PacketHandlers() {
         public void register() {
            this.map(Types.BLOCK_POSITION1_14, Types.BLOCK_POSITION1_8);
         }
      });
      ((Protocol1_14To1_13_2)this.protocol)
         .replaceClientbound(
            ClientboundPackets1_14.BLOCK_EVENT,
            new PacketHandlers() {
               public void register() {
                  this.map(Types.BLOCK_POSITION1_14, Types.BLOCK_POSITION1_8);
                  this.map(Types.UNSIGNED_BYTE);
                  this.map(Types.UNSIGNED_BYTE);
                  this.map(Types.VAR_INT);
                  this.handler(
                     wrapper -> {
                        int mappedId = ((Protocol1_14To1_13_2)BlockItemPacketRewriter1_14.this.protocol)
                           .getMappingData()
                           .getNewBlockId((Integer)wrapper.get(Types.VAR_INT, 0));
                        if (mappedId == -1) {
                           wrapper.cancel();
                        } else {
                           wrapper.set(Types.VAR_INT, 0, mappedId);
                        }
                     }
                  );
               }
            }
         );
      ((Protocol1_14To1_13_2)this.protocol).replaceClientbound(ClientboundPackets1_14.BLOCK_UPDATE, new PacketHandlers() {
         public void register() {
            this.map(Types.BLOCK_POSITION1_14, Types.BLOCK_POSITION1_8);
            this.map(Types.VAR_INT);
            this.handler(wrapper -> {
               int id = (Integer)wrapper.get(Types.VAR_INT, 0);
               wrapper.set(Types.VAR_INT, 0, ((Protocol1_14To1_13_2)BlockItemPacketRewriter1_14.this.protocol).getMappingData().getNewBlockStateId(id));
            });
         }
      });
      ((Protocol1_14To1_13_2)this.protocol).registerClientbound(ClientboundPackets1_14.EXPLODE, new PacketHandlers() {
         public void register() {
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.handler(wrapper -> {
               for (int i = 0; i < 3; i++) {
                  float coord = (Float)wrapper.get(Types.FLOAT, i);
                  if (coord < 0.0F) {
                     coord = (float)Math.floor(coord);
                     wrapper.set(Types.FLOAT, i, coord);
                  }
               }
            });
         }
      });
      ((Protocol1_14To1_13_2)this.protocol)
         .registerClientbound(
            ClientboundPackets1_14.LEVEL_CHUNK,
            wrapper -> {
               ProtocolStorables1_14 storables = (ProtocolStorables1_14)wrapper.user().storables(this.protocol);
               ClientWorld clientWorld = storables.clientWorld();
               Chunk chunk = (Chunk)wrapper.read(ChunkType1_14.TYPE);
               wrapper.write(ChunkType1_13.forEnvironment(clientWorld.getEnvironment()), chunk);
               ChunkLightStorage.ChunkLight chunkLight = storables.chunkLightStorage().getStoredLight(chunk.getX(), chunk.getZ());

               for (int i = 0; i < chunk.getSections().length; i++) {
                  ChunkSection section = chunk.getSections()[i];
                  if (section != null) {
                     ChunkSectionLight sectionLight = ChunkSectionLightImpl.createWithBlockLight();
                     section.setLight(sectionLight);
                     if (chunkLight == null) {
                        sectionLight.setBlockLight(ChunkLightStorage.FULL_LIGHT);
                        if (clientWorld.getEnvironment() == Environment.NORMAL) {
                           sectionLight.setSkyLight(ChunkLightStorage.FULL_LIGHT);
                        }
                     } else {
                        byte[] blockLight = chunkLight.blockLight()[i];
                        sectionLight.setBlockLight(blockLight != null ? blockLight : ChunkLightStorage.FULL_LIGHT);
                        if (clientWorld.getEnvironment() == Environment.NORMAL) {
                           byte[] skyLight = chunkLight.skyLight()[i];
                           sectionLight.setSkyLight(skyLight != null ? skyLight : ChunkLightStorage.FULL_LIGHT);
                        }
                     }

                     DataPalette palette = section.palette(PaletteType.BLOCKS);
                     if (Via.getConfig().isNonFullBlockLightFix() && section.getNonAirBlocksCount() != 0 && sectionLight.hasBlockLight()) {
                        NibbleArray blockLightx = sectionLight.getBlockLightNibbleArray();
                        palette.forEachMatchingCoordinate(
                           id -> Protocol1_13_2To1_14.MAPPINGS.getNonFullBlocks().contains(id),
                           idx -> blockLight.set(ChunkSection.xFromIndex(idx), ChunkSection.yFromIndex(idx), ChunkSection.zFromIndex(idx), 0)
                        );
                     }

                     for (int j = 0; j < palette.size(); j++) {
                        int mappedBlockStateId = ((Protocol1_14To1_13_2)this.protocol).getMappingData().getNewBlockStateId(palette.idByIndex(j));
                        palette.setIdByIndex(j, mappedBlockStateId);
                     }
                  }
               }
            }
         );
      ((Protocol1_14To1_13_2)this.protocol).registerClientbound(ClientboundPackets1_14.FORGET_LEVEL_CHUNK, wrapper -> {
         int x = (Integer)wrapper.passthrough(Types.INT);
         int z = (Integer)wrapper.passthrough(Types.INT);
         ProtocolStorables1_14 storables = (ProtocolStorables1_14)wrapper.user().storables(this.protocol);
         storables.chunkLightStorage().unloadChunk(x, z);
      });
      ((Protocol1_14To1_13_2)this.protocol).replaceClientbound(ClientboundPackets1_14.LEVEL_EVENT, new PacketHandlers() {
         public void register() {
            this.map(Types.INT);
            this.map(Types.BLOCK_POSITION1_14, Types.BLOCK_POSITION1_8);
            this.map(Types.INT);
            this.handler(wrapper -> {
               int id = (Integer)wrapper.get(Types.INT, 0);
               int data = (Integer)wrapper.get(Types.INT, 1);
               if (id == 1010) {
                  wrapper.set(Types.INT, 1, ((Protocol1_14To1_13_2)BlockItemPacketRewriter1_14.this.protocol).getMappingData().getNewItemId(data));
               } else if (id == 2001) {
                  wrapper.set(Types.INT, 1, ((Protocol1_14To1_13_2)BlockItemPacketRewriter1_14.this.protocol).getMappingData().getNewBlockStateId(data));
               }
            });
         }
      });
      ((Protocol1_14To1_13_2)this.protocol).registerClientbound(ClientboundPackets1_14.MAP_ITEM_DATA, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.BYTE);
            this.map(Types.BOOLEAN);
            this.read(Types.BOOLEAN);
         }
      });
      ((Protocol1_14To1_13_2)this.protocol).registerClientbound(ClientboundPackets1_14.SET_DEFAULT_SPAWN_POSITION, new PacketHandlers() {
         public void register() {
            this.map(Types.BLOCK_POSITION1_14, Types.BLOCK_POSITION1_8);
         }
      });
   }

   protected void registerRewrites() {
      this.enchantmentRewriter = new EnchantmentRewriter(this, false);
      this.enchantmentRewriter.registerEnchantment("minecraft:multishot", "§7Multishot");
      this.enchantmentRewriter.registerEnchantment("minecraft:quick_charge", "§7Quick Charge");
      this.enchantmentRewriter.registerEnchantment("minecraft:piercing", "§7Piercing");
   }

   @Override
   public Item handleItemToClient(UserConnection connection, Item item) {
      if (item == null) {
         return null;
      } else {
         item = super.handleItemToClient(connection, item);
         CompoundTag display = item.tag() != null ? item.tag().getCompoundTag("display") : null;
         if (display != null && item instanceof DataItemWithExtras fullItem && fullItem.lore() != null) {
            List<JsonElement> lore = fullItem.lore();
            ListTag<StringTag> loreTag = fullItem.rawLore();
            this.saveListTag(display, loreTag, "Lore");

            try {
               for (int i = 0; i < lore.size(); i++) {
                  JsonElement loreEntry = lore.get(i);
                  TextComponent component = SerializerVersion.V1_12.toComponent(loreEntry);
                  if (component == null) {
                     lore.remove(i);
                     loreTag.remove(i);
                     i--;
                  } else {
                     TextUtils.setTranslator(
                        component,
                        s -> Protocol1_12_2To1_13.MAPPINGS.getMojangTranslation().getOrDefault(s, TranslatableMappings.getTranslatableMappings("1.14").get(s))
                     );
                     ((StringTag)loreTag.get(i)).setValue(component.asLegacyFormatString());
                  }
               }
            } catch (JsonParseException var10) {
               display.remove("Lore");
            }
         }

         if (item instanceof DataItemWithExtras) {
            item = new DataItem(item.identifier(), (byte)item.amount(), item.data(), item.tag());
         }

         this.enchantmentRewriter.handleToClient(item);
         return item;
      }
   }

   @Override
   public Item handleItemToServer(UserConnection connection, Item item) {
      if (item == null) {
         return null;
      } else {
         CompoundTag tag = item.tag();
         CompoundTag display;
         if (tag != null && (display = tag.getCompoundTag("display")) != null) {
            ListTag<StringTag> lore = display.getListTag("Lore", StringTag.class);
            if (lore != null && !this.hasBackupTag(display, "Lore")) {
               for (StringTag loreEntry : lore) {
                  loreEntry.setValue(ComponentUtil.legacyToJsonString(loreEntry.getValue()));
               }
            }
         }

         this.enchantmentRewriter.handleToServer(item);
         return super.handleItemToServer(connection, item);
      }
   }
}
