package com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R2.items.datacomponents.impl;

import com.artillexstudios.axafkzone.libs.axapi.items.components.data.DyedItemColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class DyedColorDataComponent implements DataComponentHandler<DyedItemColor> {
   public void setData(ItemStack from, DyedItemColor data) {
      NBTTagCompound tag = this.getOrCreateTag(from);
      if (from.d() != Items.oN && from.d() != Items.oM && from.d() != Items.oL && from.d() != Items.oK) {
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
         return stack.d() != Items.oN && stack.d() != Items.oM && stack.d() != Items.oL && stack.d() != Items.oK
            ? new DyedItemColor(tag.h("CustomPotionColor"))
            : new DyedItemColor(tag.h("Color"));
      }
   }

   @Override
   public void removeData(ItemStack itemStack) {
      NBTTagCompound tag = this.getTag(itemStack);
      if (tag != null) {
         if (itemStack.d() != Items.oN && itemStack.d() != Items.oM && itemStack.d() != Items.oL && itemStack.d() != Items.oK) {
            tag.r("CustomPotionColor");
         } else {
            tag.r("Color");
         }
      }
   }
}
