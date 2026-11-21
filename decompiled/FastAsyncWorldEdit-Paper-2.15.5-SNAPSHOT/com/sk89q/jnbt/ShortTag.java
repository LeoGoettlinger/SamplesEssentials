package com.sk89q.jnbt;

import com.fastasyncworldedit.core.jnbt.NumberTag;
import org.enginehub.linbus.tree.LinShortTag;

@Deprecated
public final class ShortTag extends NumberTag<LinShortTag> {
   public ShortTag(short value) {
      super(LinShortTag.of(value));
   }

   public ShortTag(LinShortTag tag) {
      super(tag);
   }

   public Short getValue() {
      return this.linTag.value();
   }

   @Override
   public int getTypeCode() {
      return 2;
   }
}
