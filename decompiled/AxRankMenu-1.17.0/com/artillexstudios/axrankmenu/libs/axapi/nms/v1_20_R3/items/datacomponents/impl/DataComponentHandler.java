package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R3.items.datacomponents.impl;

import com.artillexstudios.axrankmenu.libs.axapi.items.WrappedItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface DataComponentHandler<T> {
   String DISPLAY_TAG = "display";

   default void apply(WrappedItemStack stack, T data) {
      ItemStack wrapped = ((com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R3.items.WrappedItemStack)stack).itemStack;
      if (data == null) {
         this.removeData(wrapped);
      } else {
         this.setData(wrapped, data);
      }
   }

   default T getData(WrappedItemStack stack) {
      ItemStack wrapped = ((com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R3.items.WrappedItemStack)stack).itemStack;
      return this.getData(wrapped);
   }

   void setData(ItemStack from, T data);

   T getData(ItemStack stack);

   void removeData(ItemStack itemStack);

   @Nullable
   default NBTTagCompound getTag(ItemStack stack) {
      return stack.v();
   }

   default NBTTagCompound getOrCreateTag(ItemStack stack) {
      return stack.w();
   }

   @Nullable
   default NBTTagCompound getDisplayTag(ItemStack stack) {
      NBTTagCompound compoundTag = this.getTag(stack);
      if (compoundTag == null) {
         return null;
      } else {
         return !compoundTag.e("display") ? null : compoundTag.p("display");
      }
   }

   default void setDisplayTag(NBTTagCompound compoundTag, String key, NBTBase value) {
      NBTTagCompound display = compoundTag.p("display");
      if (!compoundTag.e("display")) {
         compoundTag.a("display", display);
      }

      display.r(key);
      if (value != null) {
         display.a(key, value);
      }
   }
}
