package com.viaversion.viaversion.protocols.v1_13_2to1_14.rewriter;

import com.google.common.collect.Sets;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.DoubleTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.Protocol1_13_2To1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.storage.EntityTracker1_14;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.rewriter.text.ComponentRewriterBase;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class ItemPacketRewriter1_14 extends ItemRewriter<ClientboundPackets1_13, ServerboundPackets1_14, Protocol1_13_2To1_14> {
   private static final Set<String> REMOVED_RECIPE_TYPES = Sets.newHashSet(new String[]{"crafting_special_banneraddpattern", "crafting_special_repairitem"});
   private static final JsonNBTComponentRewriter<ClientboundPackets1_13> COMPONENT_REWRITER = new JsonNBTComponentRewriter<ClientboundPackets1_13>(
      null, ComponentRewriterBase.ReadType.JSON
   ) {
      @Override
      protected void handleTranslate(JsonObject object, String translate) {
         super.handleTranslate(object, translate);
         if (translate.startsWith("block.") && translate.endsWith(".name")) {
            object.addProperty("translate", translate.substring(0, translate.length() - 5));
         }
      }
   };

   public ItemPacketRewriter1_14(Protocol1_13_2To1_14 protocol) {
      super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_SHORT_ARRAY);
   }

   @Override
   public void registerPackets() {
      this.protocol.registerClientbound(ClientboundPackets1_13.OPEN_SCREEN, null, wrapper -> {
         Short windowId = wrapper.read(Types.UNSIGNED_BYTE);
         String type = wrapper.read(Types.STRING);
         JsonElement title = wrapper.read(Types.COMPONENT);
         COMPONENT_REWRITER.processText(wrapper.user(), title);
         Short slots = wrapper.read(Types.UNSIGNED_BYTE);
         if (type.equals("EntityHorse")) {
            wrapper.setPacketType(ClientboundPackets1_14.HORSE_SCREEN_OPEN);
            int entityId = wrapper.read(Types.INT);
            wrapper.write(Types.UNSIGNED_BYTE, windowId);
            wrapper.write(Types.VAR_INT, slots.intValue());
            wrapper.write(Types.INT, entityId);
         } else {
            wrapper.setPacketType(ClientboundPackets1_14.OPEN_SCREEN);
            wrapper.write(Types.VAR_INT, windowId.intValue());
            int typeId = -1;
            byte var8 = -1;
            switch (type.hashCode()) {
               case -1879003021:
                  if (type.equals("minecraft:villager")) {
                     var8 = 6;
                  }
                  break;
               case -1719356277:
                  if (type.equals("minecraft:furnace")) {
                     var8 = 1;
                  }
                  break;
               case -1293651279:
                  if (type.equals("minecraft:beacon")) {
                     var8 = 7;
                  }
                  break;
               case -1150744385:
                  if (type.equals("minecraft:anvil")) {
                     var8 = 8;
                  }
                  break;
               case -1124126594:
                  if (type.equals("minecraft:crafting_table")) {
                     var8 = 0;
                  }
                  break;
               case -1112182111:
                  if (type.equals("minecraft:hopper")) {
                     var8 = 9;
                  }
                  break;
               case 319164197:
                  if (type.equals("minecraft:enchanting_table")) {
                     var8 = 4;
                  }
                  break;
               case 712019713:
                  if (type.equals("minecraft:dropper")) {
                     var8 = 2;
                  }
                  break;
               case 1374330859:
                  if (type.equals("minecraft:shulker_box")) {
                     var8 = 10;
                  }
                  break;
               case 1649065834:
                  if (type.equals("minecraft:brewing_stand")) {
                     var8 = 5;
                  }
                  break;
               case 2090881320:
                  if (type.equals("minecraft:dispenser")) {
                     var8 = 3;
                  }
            }

            typeId = switch (var8) {
               case 0 -> 11;
               case 1 -> 13;
               case 2, 3 -> 6;
               case 4 -> 12;
               case 5 -> 10;
               case 6 -> 18;
               case 7 -> 8;
               case 8 -> 7;
               case 9 -> 15;
               case 10 -> 19;
               default -> {
                  if (slots > 0 && slots <= 54) {
                     yield slots / 9 - 1;
                  }
               }
            };

            if (typeId == -1) {
               this.protocol.getLogger().warning("Can't open inventory for player! Type: " + type + " Size: " + slots);
            }

            wrapper.write(Types.VAR_INT, typeId);
            wrapper.write(Types.COMPONENT, title);
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_13.CUSTOM_PAYLOAD, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.STRING);
            this.handlerSoftFail(wrapper -> {
               String channel = Key.namespaced(wrapper.get(Types.STRING, 0));
               if (channel.equals("minecraft:trader_list")) {
                  wrapper.setPacketType(ClientboundPackets1_14.MERCHANT_OFFERS);
                  wrapper.resetReader();
                  wrapper.read(Types.STRING);
                  int windowId = wrapper.read(Types.INT);
                  EntityTracker1_14 tracker = wrapper.user().getEntityTracker(ItemPacketRewriter1_14.this.protocol);
                  tracker.setLatestTradeWindowId(windowId);
                  wrapper.write(Types.VAR_INT, windowId);
                  int size = wrapper.passthrough(Types.UNSIGNED_BYTE);

                  for (int i = 0; i < size; i++) {
                     ItemPacketRewriter1_14.this.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
                     ItemPacketRewriter1_14.this.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
                     boolean secondItem = wrapper.passthrough(Types.BOOLEAN);
                     if (secondItem) {
                        ItemPacketRewriter1_14.this.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
                     }

                     wrapper.passthrough(Types.BOOLEAN);
                     wrapper.passthrough(Types.INT);
                     wrapper.passthrough(Types.INT);
                     wrapper.write(Types.INT, 0);
                     wrapper.write(Types.INT, 0);
                     wrapper.write(Types.FLOAT, 0.0F);
                  }

                  wrapper.write(Types.VAR_INT, 0);
                  wrapper.write(Types.VAR_INT, 0);
                  wrapper.write(Types.BOOLEAN, false);
                  wrapper.clearInputBuffer();
               } else if (channel.equals("minecraft:book_open")) {
                  int hand = wrapper.read(Types.VAR_INT);
                  wrapper.clearPacket();
                  wrapper.setPacketType(ClientboundPackets1_14.OPEN_BOOK);
                  wrapper.write(Types.VAR_INT, hand);
               }
            });
         }
      });
      RecipeRewriter<ClientboundPackets1_13> recipeRewriter = new RecipeRewriter<>(this.protocol);
      this.protocol.registerClientbound(ClientboundPackets1_13.UPDATE_RECIPES, wrapper -> {
         int size = wrapper.passthrough(Types.VAR_INT);
         int deleted = 0;

         for (int i = 0; i < size; i++) {
            String id = wrapper.read(Types.STRING);
            String type = wrapper.read(Types.STRING);
            if (REMOVED_RECIPE_TYPES.contains(type)) {
               deleted++;
            } else {
               wrapper.write(Types.STRING, type);
               wrapper.write(Types.STRING, id);
               recipeRewriter.handleRecipeType(wrapper, type);
            }
         }

         wrapper.set(Types.VAR_INT, 0, size - deleted);
      });
      this.protocol.registerServerbound(ServerboundPackets1_14.SELECT_TRADE, wrapper -> {
         PacketWrapper resyncPacket = wrapper.create(ServerboundPackets1_13.CONTAINER_CLICK);
         EntityTracker1_14 tracker = wrapper.user().getEntityTracker(this.protocol);
         resyncPacket.write(Types.BYTE, (byte)tracker.getLatestTradeWindowId());
         resyncPacket.write(Types.SHORT, (short)-999);
         resyncPacket.write(Types.BYTE, (byte)2);
         resyncPacket.write(Types.SHORT, (short)ThreadLocalRandom.current().nextInt());
         resyncPacket.write(Types.VAR_INT, 5);
         CompoundTag tag = new CompoundTag();
         tag.put("force_resync", new DoubleTag(Double.NaN));
         resyncPacket.write(Types.ITEM1_13_2, new DataItem(1, (byte)1, tag));
         resyncPacket.scheduleSendToServer(Protocol1_13_2To1_14.class);
      });
   }

   @Override
   public Item handleItemToClient(UserConnection connection, Item item) {
      if (item == null) {
         return null;
      } else {
         item.setIdentifier(Protocol1_13_2To1_14.MAPPINGS.getNewItemId(item.identifier()));
         if (item.tag() == null) {
            return item;
         } else {
            CompoundTag display = item.tag().getCompoundTag("display");
            if (display != null) {
               ListTag<StringTag> lore = display.getListTag("Lore", StringTag.class);
               if (lore != null) {
                  display.put(this.nbtTagName("Lore"), lore.copy());

                  for (StringTag loreEntry : lore) {
                     String jsonText = ComponentUtil.legacyToJsonString(loreEntry.getValue(), true);
                     loreEntry.setValue(jsonText);
                  }
               }
            }

            return item;
         }
      }
   }

   @Override
   public Item handleItemToServer(UserConnection connection, Item item) {
      if (item == null) {
         return null;
      } else {
         item.setIdentifier(Protocol1_13_2To1_14.MAPPINGS.getOldItemId(item.identifier()));
         if (item.tag() == null) {
            return item;
         } else {
            CompoundTag display = item.tag().getCompoundTag("display");
            if (display != null) {
               ListTag<StringTag> lore = display.getListTag("Lore", StringTag.class);
               if (lore != null) {
                  Tag savedLore = display.remove(this.nbtTagName("Lore"));
                  if (savedLore instanceof ListTag) {
                     display.put("Lore", savedLore.copy());
                  } else {
                     for (StringTag loreEntry : lore) {
                        loreEntry.setValue(ComponentUtil.jsonToLegacy(loreEntry.getValue()));
                     }
                  }
               }
            }

            return item;
         }
      }
   }
}
