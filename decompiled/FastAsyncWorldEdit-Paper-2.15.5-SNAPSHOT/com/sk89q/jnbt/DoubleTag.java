package com.sk89q.jnbt;

import com.fastasyncworldedit.core.jnbt.NumberTag;
import org.enginehub.linbus.tree.LinDoubleTag;

@Deprecated
public final class DoubleTag extends NumberTag<LinDoubleTag> {
   public DoubleTag(double value) {
      this(LinDoubleTag.of(value));
   }

   public DoubleTag(LinDoubleTag tag) {
      super(tag);
   }

   public Double getValue() {
      return this.linTag.value();
   }

   @Override
   public int getTypeCode() {
      return 6;
   }
}
