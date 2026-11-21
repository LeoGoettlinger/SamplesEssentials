package com.viaversion.viaversion.protocols.v1_17to1_17_1;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.StringType;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.packet.ClientboundPackets1_17_1;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.rewriter.ItemPacketRewriter1_17_1;

public final class Protocol1_17To1_17_1
   extends AbstractProtocol<ClientboundPackets1_17, ClientboundPackets1_17_1, ServerboundPackets1_17, ServerboundPackets1_17> {
   private static final StringType PAGE_STRING_TYPE = new StringType(8192);
   private static final StringType TITLE_STRING_TYPE = new StringType(128);
   private final ItemPacketRewriter1_17_1 itemRewriter = new ItemPacketRewriter1_17_1(this);

   public Protocol1_17To1_17_1() {
      super(ClientboundPackets1_17.class, ClientboundPackets1_17_1.class, ServerboundPackets1_17.class, ServerboundPackets1_17.class);
   }

   @Override
   protected void registerPackets() {
      super.registerPackets();
      this.registerClientbound(ClientboundPackets1_17.REMOVE_ENTITY, ClientboundPackets1_17_1.REMOVE_ENTITIES, wrapper -> {
         int entityId = wrapper.read(Types.VAR_INT);
         wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[]{entityId});
      });
      this.registerServerbound(ServerboundPackets1_17.EDIT_BOOK, wrapper -> {
         CompoundTag tag = new CompoundTag();
         Item item = new DataItem(942, (byte)1, tag);
         wrapper.write(Types.ITEM1_13_2, item);
         int slot = wrapper.read(Types.VAR_INT);
         int pages = wrapper.read(Types.VAR_INT);
         ListTag<StringTag> pagesTag = new ListTag<>(StringTag.class);

         for (int i = 0; i < pages; i++) {
            String page = wrapper.read(PAGE_STRING_TYPE);
            if (i < 200) {
               pagesTag.add(new StringTag(page));
            }
         }

         if (pagesTag.isEmpty()) {
            pagesTag.add(new StringTag(""));
         }

         tag.put("pages", pagesTag);
         if (wrapper.read(Types.BOOLEAN)) {
            String title = wrapper.read(TITLE_STRING_TYPE);
            tag.put("title", new StringTag(title));
            tag.put("author", new StringTag(wrapper.user().getProtocolInfo().getUsername()));
            wrapper.write(Types.BOOLEAN, true);
         } else {
            wrapper.write(Types.BOOLEAN, false);
         }

         wrapper.write(Types.VAR_INT, slot);
      });
   }

   public ItemPacketRewriter1_17_1 getItemRewriter() {
      return this.itemRewriter;
   }
}
