package com.sk89q.jnbt;

import com.fastasyncworldedit.core.jnbt.NumberTag;
import org.enginehub.linbus.tree.LinIntTag;

@Deprecated
public final class IntTag extends NumberTag<LinIntTag> {
   public IntTag(int value) {
      this(LinIntTag.of(value));
   }

   public IntTag(LinIntTag tag) {
      super(tag);
   }

   public Integer getValue() {
      return this.linTag.value();
   }

   @Override
   public int getTypeCode() {
      return 3;
   }
}
