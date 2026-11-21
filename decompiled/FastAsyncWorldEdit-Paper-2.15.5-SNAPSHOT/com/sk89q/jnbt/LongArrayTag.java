package com.sk89q.jnbt;

import com.google.common.base.Preconditions;
import org.enginehub.linbus.tree.LinLongArrayTag;

@Deprecated
public class LongArrayTag extends Tag<long[], LinLongArrayTag> {
   public LongArrayTag(long[] value) {
      this(LinLongArrayTag.of((long[])Preconditions.checkNotNull(value)));
   }

   public LongArrayTag(LinLongArrayTag tag) {
      super(tag);
   }

   public long[] getValue() {
      return this.linTag.value();
   }

   @Override
   public int getTypeCode() {
      return 12;
   }
}
