package com.viaversion.nbt.conversion;

import com.viaversion.nbt.tag.Tag;

public interface TagConverter<T extends Tag, V> {
   V convert(T var1);

   T convert(V var1);
}
