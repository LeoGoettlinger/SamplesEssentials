package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.TransformingType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;

public record LockCode(CompoundTag tag) implements Rewritable, Copyable {
   public static final Type<LockCode> TYPE = TransformingType.of(Types.COMPOUND_TAG, LockCode.class, LockCode::new, LockCode::tag);

   public LockCode rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      CompoundTag updatedTag = this.tag;
      if (clientbound && protocol.getComponentRewriter() != null) {
         updatedTag = this.tag.copy();
         updatedTag.putString("id", "air");
         protocol.getComponentRewriter().handleShowItem(connection, updatedTag);
         updatedTag.remove("id");
         FullMappings mappings = protocol.getMappingData().getFullItemMappings();
         Tag itemsTag = updatedTag.get("items");
         if (itemsTag != null) {
            HolderSet items = HolderSet.fromTag(itemsTag, key -> mappings.id(key));
            items.rewrite(mappings::getNewId);
            updatedTag.put("items", this.toTag(items, mappings));
         }
      }

      return new LockCode(updatedTag);
   }

   private Tag toTag(HolderSet holderSet, FullMappings mappings) {
      if (holderSet.hasTagKey()) {
         return new StringTag("#" + holderSet.tagKey());
      } else {
         int[] ids = holderSet.ids();
         if (ids.length == 1) {
            return new StringTag(mappings.mappedIdentifier(ids[0]));
         } else {
            ListTag<StringTag> listTag = new ListTag<>(StringTag.class);

            for (int id : ids) {
               listTag.add(new StringTag(mappings.mappedIdentifier(id)));
            }

            return listTag;
         }
      }
   }

   public LockCode copy() {
      return new LockCode(this.tag.copy());
   }
}
