package com.fastasyncworldedit.core.jnbt;

import com.sk89q.jnbt.Tag;
import org.enginehub.linbus.tree.LinTag;

public abstract class NumberTag<LT extends LinTag<? extends Number>> extends Tag<Number, LT> {
   protected NumberTag(LT linTag) {
      super(linTag);
   }

   public abstract Number getValue();
}
