package com.sk89q.jnbt;

import com.google.common.base.Preconditions;
import org.enginehub.linbus.tree.LinIntArrayTag;

@Deprecated
public final class IntArrayTag extends Tag<int[], LinIntArrayTag> {
   public IntArrayTag(int[] value) {
      this(LinIntArrayTag.of((int[])Preconditions.checkNotNull(value)));
   }

   public IntArrayTag(LinIntArrayTag tag) {
      super(tag);
   }

   public int[] getValue() {
      return this.linTag.value();
   }

   @Override
   public int getTypeCode() {
      return 11;
   }
}
