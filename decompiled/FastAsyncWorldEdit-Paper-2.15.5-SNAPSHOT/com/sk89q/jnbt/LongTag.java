package com.sk89q.jnbt;

import com.fastasyncworldedit.core.jnbt.NumberTag;
import org.enginehub.linbus.tree.LinLongTag;

@Deprecated
public final class LongTag extends NumberTag<LinLongTag> {
   public LongTag(long value) {
      this(LinLongTag.of(value));
   }

   public LongTag(LinLongTag tag) {
      super(tag);
   }

   public Long getValue() {
      return this.linTag.value();
   }

   @Override
   public int getTypeCode() {
      return 4;
   }
}
