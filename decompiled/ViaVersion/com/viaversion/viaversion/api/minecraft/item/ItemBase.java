package com.viaversion.viaversion.api.minecraft.item;

import com.viaversion.viaversion.util.Copyable;

public interface ItemBase extends Copyable {
   int identifier();

   void setIdentifier(int var1);

   int amount();

   void setAmount(int var1);

   default boolean isEmpty() {
      return this.identifier() == 0 || this.amount() <= 0;
   }

   ItemBase copy();
}
