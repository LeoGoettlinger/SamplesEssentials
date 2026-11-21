package com.viaversion.viaversion.api.minecraft;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntFunction;
import java.util.function.ToIntFunction;

public interface HolderSet {
   static HolderSet of(String tagKey) {
      Preconditions.checkNotNull(tagKey);
      return new HolderSetImpl.Tag(tagKey);
   }

   static HolderSet of(int[] ids) {
      Preconditions.checkNotNull(ids);
      return new HolderSetImpl.Ids(ids);
   }

   static HolderSet fromTag(Tag tag, ToIntFunction<String> mappingFunction) {
      if (tag instanceof StringTag stringTag) {
         if (stringTag.getValue().startsWith("#")) {
            return of(stringTag.getValue().substring(1));
         } else {
            int id = mappingFunction.applyAsInt(stringTag.getValue());
            return of(new int[]{id});
         }
      } else if (!(tag instanceof ListTag<?> listTag)) {
         throw new IllegalArgumentException();
      } else {
         int[] ids = new int[listTag.size()];

         for (int i = 0; i < listTag.size(); i++) {
            String value = ((StringTag)listTag.get(i)).getValue();
            ids[i] = mappingFunction.applyAsInt(value);
         }

         return of(ids);
      }
   }

   String tagKey();

   boolean hasTagKey();

   int[] ids();

   boolean hasIds();

   HolderSet rewrite(Int2IntFunction var1);
}
