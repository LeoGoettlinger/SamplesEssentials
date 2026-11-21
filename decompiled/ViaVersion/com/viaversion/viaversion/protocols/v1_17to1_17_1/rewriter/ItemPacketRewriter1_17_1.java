package com.viaversion.viaversion.protocols.v1_17to1_17_1.rewriter;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.Protocol1_17To1_17_1;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Limit;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ItemPacketRewriter1_17_1 extends ItemRewriter<ClientboundPackets1_17, ServerboundPackets1_17, Protocol1_17To1_17_1> {
   public ItemPacketRewriter1_17_1(Protocol1_17To1_17_1 protocol) {
      super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_SHORT_ARRAY);
   }

   @Override
   public void registerPackets() {
      new RecipeRewriter<>(this.protocol).register(ClientboundPackets1_17.UPDATE_RECIPES);
      this.protocol.replaceClientbound(ClientboundPackets1_17.CONTAINER_SET_SLOT, wrapper -> {
         wrapper.passthrough(Types.BYTE);
         wrapper.write(Types.VAR_INT, 0);
         wrapper.passthrough(Types.SHORT);
         this.passthroughClientboundItem(wrapper);
      });
      this.protocol.replaceClientbound(ClientboundPackets1_17.CONTAINER_SET_CONTENT, wrapper -> {
         wrapper.passthrough(Types.UNSIGNED_BYTE);
         wrapper.write(Types.VAR_INT, 0);
         Item[] items = wrapper.passthroughAndMap(Types.ITEM1_13_2_SHORT_ARRAY, Types.ITEM1_13_2_ARRAY);

         for (int i = 0; i < items.length; i++) {
            items[i] = this.handleItemToClient(wrapper.user(), items[i]);
         }

         wrapper.write(Types.ITEM1_13_2, null);
      });
      this.protocol.replaceServerbound(ServerboundPackets1_17.CONTAINER_CLICK, wrapper -> {
         wrapper.passthrough(Types.BYTE);
         wrapper.read(Types.VAR_INT);
         wrapper.passthrough(Types.SHORT);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.VAR_INT);
         int length = Limit.max(wrapper.passthrough(Types.VAR_INT), 128);

         for (int i = 0; i < length; i++) {
            wrapper.passthrough(Types.SHORT);
            wrapper.write(Types.ITEM1_13_2, this.handleItemToServer(wrapper.user(), wrapper.read(Types.ITEM1_13_2)));
         }

         wrapper.write(Types.ITEM1_13_2, this.handleItemToServer(wrapper.user(), wrapper.read(Types.ITEM1_13_2)));
      });
   }

   @Override
   public Item handleItemToClient(UserConnection connection, Item item) {
      if (item == null) {
         return null;
      } else {
         CompoundTag tag = item.tag();
         if (tag == null) {
            return item;
         } else {
            int hideFlags = tag.getInt("HideFlags");
            if ((hideFlags & 1) == 0) {
               this.replaceInvalidEnchantments(tag, "Enchantments");
            }

            if ((hideFlags & 32) == 0) {
               this.replaceInvalidEnchantments(tag, "StoredEnchantments");
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
         CompoundTag tag = item.tag();
         if (tag == null) {
            return item;
         } else {
            this.restoreInvalidEnchantments(tag, "Enchantments");
            this.restoreInvalidEnchantments(tag, "StoredEnchantments");
            return item;
         }
      }
   }

   private void replaceInvalidEnchantments(CompoundTag tag, String tagName) {
      ListTag<CompoundTag> enchantments = tag.getListTag(tagName, CompoundTag.class);
      if (enchantments != null) {
         boolean hasInvalidLevel = false;

         for (CompoundTag enchantment : enchantments.getValue()) {
            short lvl = enchantment.getShort("lvl");
            if (lvl < 0 || lvl > 255) {
               hasInvalidLevel = true;
               break;
            }
         }

         if (hasInvalidLevel) {
            tag.put(this.nbtTagName(tagName), new ByteTag(true));
            int originalHideFlags = tag.getInt("HideFlags");
            tag.put(this.nbtTagName("HideFlags"), new ByteTag((byte)originalHideFlags));
            int hideBit = tagName.equals("Enchantments") ? 1 : 32;
            tag.put("HideFlags", new ByteTag((byte)(originalHideFlags | hideBit)));
            CompoundTag display = tag.getCompoundTag("display");
            if (display != null) {
               tag.put(this.nbtTagName("display"), display.copy());
            } else {
               tag.put("display", display = new CompoundTag());
            }

            ListTag<StringTag> lore = display.getListTag("Lore", StringTag.class);
            if (lore == null) {
               display.put("Lore", lore = new ListTag<>(StringTag.class));
            }

            for (int i = enchantments.size() - 1; i >= 0; i--) {
               CompoundTag enchantmentx = enchantments.get(i);
               String id = enchantmentx.getString("id");
               if (id != null) {
                  short lvl = enchantmentx.getShort("lvl");
                  Key key = Key.of(id);
                  lore.getValue()
                     .add(
                        0,
                        new StringTag(
                           "{\"italic\":false,\"color\":\"gray\",\"translate\":\"enchantment."
                              + key.namespace()
                              + "."
                              + key.path()
                              + "\",\"extra\":[\" \",{\"translate\":\"enchantment.level."
                              + lvl
                              + "\"}]}"
                        )
                     );
               }
            }
         }
      }
   }

   private void restoreInvalidEnchantments(CompoundTag tag, String tagName) {
      Tag marker = tag.remove(this.nbtTagName(tagName));
      if (marker != null) {
         Tag hideFlags = tag.remove(this.nbtTagName("HideFlags"));
         if (hideFlags != null) {
            if (((ByteTag)hideFlags).asByte() == 0) {
               tag.remove("HideFlags");
            } else {
               tag.put("HideFlags", hideFlags);
            }
         }

         Tag display = tag.remove(this.nbtTagName("display"));
         if (display != null) {
            tag.put("display", display);
         } else {
            tag.remove("display");
         }
      }
   }
}
