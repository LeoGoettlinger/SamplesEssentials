package com.sk89q.jnbt;

import javax.annotation.Nonnull;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.ToLinTag;

@Deprecated
public abstract class Tag<V, LT extends LinTag<? extends V>> implements ToLinTag<LT> {
   protected final LT linTag;

   protected Tag(LT linTag) {
      this.linTag = linTag;
   }

   public V getValue() {
      return (V)this.linTag.value();
   }

   @Override
   public String toString() {
      return this.toLinTag().toString();
   }

   @Nonnull
   @Override
   public LT toLinTag() {
      return this.linTag;
   }

   public Object toRaw() {
      return this.getValue();
   }

   public abstract int getTypeCode();

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return !this.getClass().equals(o.getClass()) ? false : this.linTag.equals(((Tag)o).linTag);
      }
   }
}
