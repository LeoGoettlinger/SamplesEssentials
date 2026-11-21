package com.viaversion.viaversion.api.minecraft.item;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Item extends ItemBase {
   default short data() {
      return 0;
   }

   default void setData(short data) {
      throw new UnsupportedOperationException();
   }

   @Nullable
   CompoundTag tag();

   void setTag(@Nullable CompoundTag var1);

   StructuredDataContainer dataContainer();

   Item copy();

   default boolean isTemplate() {
      return false;
   }

   static boolean isEmpty(@Nullable Item item) {
      return item == null || item.isEmpty();
   }
}
