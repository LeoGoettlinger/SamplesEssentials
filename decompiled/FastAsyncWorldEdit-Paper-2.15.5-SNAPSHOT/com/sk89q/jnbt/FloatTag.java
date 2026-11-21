package com.sk89q.jnbt;

import com.fastasyncworldedit.core.jnbt.NumberTag;
import org.enginehub.linbus.tree.LinFloatTag;

/** @deprecated */
public final class FloatTag extends NumberTag<LinFloatTag> {
   public FloatTag(float value) {
      this(LinFloatTag.of(value));
   }

   public FloatTag(LinFloatTag tag) {
      super(tag);
   }

   public Float getValue() {
      return this.linTag.value();
   }

   @Override
   public int getTypeCode() {
      return 5;
   }
}
