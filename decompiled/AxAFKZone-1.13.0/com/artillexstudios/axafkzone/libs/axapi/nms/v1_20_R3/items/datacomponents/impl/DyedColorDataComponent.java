package com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R3.items.datacomponents.impl;

import com.artillexstudios.axafkzone.libs.axapi.items.components.data.DyedItemColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class DyedColorDataComponent implements DataComponentHandler<DyedItemColor> {
   public void setData(ItemStack from, DyedItemColor data) {
      NBTTagCompound tag = this.getOrCreateTag(from);
      if (from.d() != Items.py && from.d() != Items.px && from.d() != Items.pw && from.d() != Items.pv) {
         tag.a("CustomPotionColor", data.rgb());
      } else {
         this.setDisplayTag(tag, "Color", NBTTagInt.a(data.rgb()));
      }
   }

   public DyedItemColor getData(ItemStack stack) {
      NBTTagCompound tag = this.getTag(stack);
      if (tag == null) {
         return null;
      } else {
         return stack.d() != Items.py && stack.d() != Items.px && stack.d() != Items.pw && stack.d() != Items.pv
            ? new DyedItemColor(tag.h("CustomPotionColor"))
            : new DyedItemColor(tag.h("Color"));
      }
   }

   @Override
   public void removeData(ItemStack itemStack) {
      NBTTagCompound tag = this.getTag(itemStack);
      if (tag != null) {
         if (itemStack.d() != Items.py && itemStack.d() != Items.px && itemStack.d() != Items.pw && itemStack.d() != Items.pv) {
            tag.r("CustomPotionColor");
         } else {
            tag.r("Color");
         }
      }
   }
}
