package com.viaversion.viaversion.protocols.v1_16_4to1_17.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.Protocol1_16_4To1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

public final class ItemPacketRewriter1_17 extends ItemRewriter<ClientboundPackets1_16_2, ServerboundPackets1_17, Protocol1_16_4To1_17> {
   public ItemPacketRewriter1_17(Protocol1_16_4To1_17 protocol) {
      super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_SHORT_ARRAY);
   }

   @Override
   public void registerPackets() {
      new RecipeRewriter<>(this.protocol).register(ClientboundPackets1_16_2.UPDATE_RECIPES);
      this.protocol
         .registerServerbound(ServerboundPackets1_17.EDIT_BOOK, wrapper -> this.handleItemToServer(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2)));
      this.protocol.replaceServerbound(ServerboundPackets1_17.CONTAINER_CLICK, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.BYTE);
            this.map(Types.SHORT);
            this.map(Types.BYTE);
            this.handler(wrapper -> wrapper.write(Types.SHORT, (short)0));
            this.map(Types.VAR_INT);
            this.handler(wrapper -> {
               int length = wrapper.read(Types.VAR_INT);

               for (int i = 0; i < length; i++) {
                  wrapper.read(Types.SHORT);
                  wrapper.read(Types.ITEM1_13_2);
               }

               Item item = wrapper.read(Types.ITEM1_13_2);
               int action = wrapper.get(Types.VAR_INT, 0);
               if (action != 5 && action != 1) {
                  ItemPacketRewriter1_17.this.handleItemToServer(wrapper.user(), item);
               } else {
                  item = null;
               }

               wrapper.write(Types.ITEM1_13_2, item);
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_16_2.CONTAINER_ACK, null, wrapper -> {
         short inventoryId = wrapper.read(Types.UNSIGNED_BYTE);
         short confirmationId = wrapper.read(Types.SHORT);
         boolean accepted = wrapper.read(Types.BOOLEAN);
         if (!accepted) {
            int id = 1073741824 | inventoryId << 16 | confirmationId & '\uffff';
            PacketWrapper pingPacket = wrapper.create(ClientboundPackets1_17.PING);
            pingPacket.write(Types.INT, id);
            pingPacket.send(Protocol1_16_4To1_17.class);
         }

         wrapper.cancel();
      });
      this.protocol.registerServerbound(ServerboundPackets1_17.PONG, null, wrapper -> {
         int id = wrapper.read(Types.INT);
         if ((id & 1073741824) != 0) {
            byte inventoryId = (byte)(id >> 16 & 0xFF);
            short confirmationId = (short)(id & 65535);
            PacketWrapper packet = wrapper.create(ServerboundPackets1_16_2.CONTAINER_ACK);
            packet.write(Types.BYTE, inventoryId);
            packet.write(Types.SHORT, confirmationId);
            packet.write(Types.BOOLEAN, true);
            packet.sendToServer(Protocol1_16_4To1_17.class);
         }

         wrapper.cancel();
      });
   }

   @Override
   public Item handleItemToClient(UserConnection connection, Item item) {
      if (item == null) {
         return null;
      } else {
         CompoundTag tag = item.tag();
         if (item.identifier() == 733) {
            if (tag == null) {
               item.setTag(tag = new CompoundTag());
            }

            if (tag.getNumberTag("map") == null) {
               tag.put("map", new IntTag(0));
               tag.putBoolean(this.nbtTagName("map"), true);
            }
         }

         item.setIdentifier(this.protocol.getMappingData().getNewItemId(item.identifier()));
         return item;
      }
   }

   @Override
   public Item handleItemToServer(UserConnection connection, Item item) {
      if (item == null) {
         return null;
      } else {
         super.handleItemToServer(connection, item);
         CompoundTag tag = item.tag();
         if (item.identifier() == 733 && tag != null && tag.remove(this.nbtTagName("map")) != null) {
            tag.remove("map");
            if (tag.isEmpty()) {
               item.setTag(null);
            }
         }

         return item;
      }
   }
}
