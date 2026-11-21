package com.viaversion.viaversion.protocols.v1_15_2to1_16.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packet.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.Protocol1_15_2To1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.data.AttributeMappings1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ServerboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.storage.ProtocolStorables1_16;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.UUIDUtil;
import java.util.UUID;

public class ItemPacketRewriter1_16 extends ItemRewriter<ClientboundPackets1_15, ServerboundPackets1_16, Protocol1_15_2To1_16> {
   public ItemPacketRewriter1_16(Protocol1_15_2To1_16 protocol) {
      super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_SHORT_ARRAY);
   }

   @Override
   public void registerPackets() {
      final PacketHandler cursorRemapper = wrapper -> {
         PacketWrapper clearPacket = wrapper.create(ClientboundPackets1_16.CONTAINER_SET_SLOT);
         clearPacket.write(Types.BYTE, (byte)-1);
         clearPacket.write(Types.SHORT, Short.valueOf((short)-1));
         clearPacket.write(Types.ITEM1_13_2, null);
         clearPacket.send(Protocol1_15_2To1_16.class);
      };
      this.protocol.replaceClientbound(ClientboundPackets1_15.OPEN_SCREEN, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.COMPONENT);
            this.handler(cursorRemapper);
            this.handler(wrapper -> {
               ProtocolStorables1_16 storables = wrapper.user().storables(ItemPacketRewriter1_16.this.protocol);
               int windowType = wrapper.get(Types.VAR_INT, 1);
               if (windowType >= 20) {
                  wrapper.set(Types.VAR_INT, 1, ++windowType);
               }

               storables.inventoryTracker().setInventoryOpen(true);
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_15.CONTAINER_CLOSE, new PacketHandlers() {
         @Override
         public void register() {
            this.handler(cursorRemapper);
            this.handler(wrapper -> {
               ProtocolStorables1_16 storables = wrapper.user().storables(ItemPacketRewriter1_16.this.protocol);
               storables.inventoryTracker().setInventoryOpen(false);
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_15.CONTAINER_SET_DATA, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.UNSIGNED_BYTE);
            this.map(Types.SHORT);
            this.map(Types.SHORT);
            this.handler(wrapper -> {
               short property = wrapper.get(Types.SHORT, 0);
               if (property >= 4 && property <= 6) {
                  short enchantmentId = wrapper.get(Types.SHORT, 1);
                  if (enchantmentId >= 11) {
                     wrapper.set(Types.SHORT, 1, ++enchantmentId);
                  }
               }
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_15.SET_EQUIPPED_ITEM, ClientboundPackets1_16.SET_EQUIPMENT, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.handler(wrapper -> {
               int slot = wrapper.read(Types.VAR_INT);
               wrapper.write(Types.BYTE, (byte)slot);
               ItemPacketRewriter1_16.this.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
            });
         }
      });
      new RecipeRewriter<>(this.protocol).register(ClientboundPackets1_15.UPDATE_RECIPES);
      this.protocol.registerServerbound(ServerboundPackets1_16.CONTAINER_CLOSE, wrapper -> {
         ProtocolStorables1_16 storables = wrapper.user().storables(this.protocol);
         storables.inventoryTracker().setInventoryOpen(false);
      });
      this.protocol
         .registerServerbound(ServerboundPackets1_16.EDIT_BOOK, wrapper -> this.handleItemToServer(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2)));
   }

   @Override
   public Item handleItemToClient(UserConnection connection, Item item) {
      if (item == null) {
         return null;
      } else {
         CompoundTag tag = item.tag();
         if (item.identifier() == 771 && tag != null) {
            CompoundTag ownerTag = tag.getCompoundTag("SkullOwner");
            if (ownerTag != null) {
               StringTag idTag = ownerTag.getStringTag("Id");
               if (idTag != null) {
                  UUID id = UUID.fromString(idTag.getValue());
                  ownerTag.put("Id", new IntArrayTag(UUIDUtil.toIntArray(id)));
               }
            }
         } else if (item.identifier() == 759 && tag != null) {
            ListTag<StringTag> pages = tag.getListTag("pages", StringTag.class);
            if (pages != null) {
               for (StringTag pageTag : pages) {
                  pageTag.setValue(this.protocol.getComponentRewriter().processText(connection, pageTag.getValue()).toString());
               }
            }
         }

         oldToNewAttributes(item);
         item.setIdentifier(Protocol1_15_2To1_16.MAPPINGS.getNewItemId(item.identifier()));
         return item;
      }
   }

   @Override
   public Item handleItemToServer(UserConnection connection, Item item) {
      if (item == null) {
         return null;
      } else {
         item.setIdentifier(Protocol1_15_2To1_16.MAPPINGS.getOldItemId(item.identifier()));
         if (item.identifier() == 771 && item.tag() != null) {
            CompoundTag tag = item.tag();
            CompoundTag ownerTag = tag.getCompoundTag("SkullOwner");
            if (ownerTag != null) {
               IntArrayTag idTag = ownerTag.getIntArrayTag("Id");
               if (idTag != null) {
                  UUID id = UUIDUtil.fromIntArray(idTag.getValue());
                  ownerTag.putString("Id", id.toString());
               }
            }
         }

         newToOldAttributes(item);
         return item;
      }
   }

   public static void oldToNewAttributes(Item item) {
      if (item.tag() != null) {
         ListTag<CompoundTag> attributes = item.tag().getListTag("AttributeModifiers", CompoundTag.class);
         if (attributes != null) {
            for (CompoundTag attribute : attributes) {
               rewriteAttributeName(attribute, "AttributeName", false);
               rewriteAttributeName(attribute, "Name", false);
               NumberTag leastTag = attribute.getNumberTag("UUIDLeast");
               NumberTag mostTag = attribute.getNumberTag("UUIDMost");
               if (leastTag != null && mostTag != null) {
                  int[] uuidIntArray = UUIDUtil.toIntArray(mostTag.asLong(), leastTag.asLong());
                  attribute.put("UUID", new IntArrayTag(uuidIntArray));
                  attribute.remove("UUIDLeast");
                  attribute.remove("UUIDMost");
               }
            }
         }
      }
   }

   public static void newToOldAttributes(Item item) {
      if (item.tag() != null) {
         ListTag<CompoundTag> attributes = item.tag().getListTag("AttributeModifiers", CompoundTag.class);
         if (attributes != null) {
            for (CompoundTag attribute : attributes) {
               rewriteAttributeName(attribute, "AttributeName", true);
               rewriteAttributeName(attribute, "Name", true);
               IntArrayTag uuidTag = attribute.getIntArrayTag("UUID");
               if (uuidTag != null && uuidTag.getValue().length == 4) {
                  UUID uuid = UUIDUtil.fromIntArray(uuidTag.getValue());
                  attribute.putLong("UUIDLeast", uuid.getLeastSignificantBits());
                  attribute.putLong("UUIDMost", uuid.getMostSignificantBits());
                  attribute.remove("UUID");
               }
            }
         }
      }
   }

   public static void rewriteAttributeName(CompoundTag compoundTag, String entryName, boolean inverse) {
      StringTag attributeNameTag = compoundTag.getStringTag(entryName);
      if (attributeNameTag != null) {
         String attributeName = attributeNameTag.getValue();
         if (inverse) {
            attributeName = Key.namespaced(attributeName);
         }

         String mappedAttribute = (String)(inverse
               ? AttributeMappings1_16.attributeIdentifierMappings().inverse()
               : AttributeMappings1_16.attributeIdentifierMappings())
            .get(attributeName);
         if (mappedAttribute != null) {
            attributeNameTag.setValue(mappedAttribute);
         }
      }
   }
}
