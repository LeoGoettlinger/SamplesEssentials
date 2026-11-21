package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R1.items.datacomponents.impl;

import com.artillexstudios.axrankmenu.libs.axapi.items.components.data.CustomModelData;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;

public final class CustomModelDataDataComponent implements DataComponentHandler<CustomModelData> {
   public void setData(ItemStack from, CustomModelData data) {
      NBTTagCompound tag = this.getOrCreateTag(from);
      tag.a("CustomModelData", data.floats().getFirst().intValue());
   }

   public CustomModelData getData(ItemStack stack) {
      NBTTagCompound tag = this.getTag(stack);
      if (tag != null && tag.e("CustomModelData")) {
         int customModelData = tag.h("CustomModelData");
         return new CustomModelData(List.of((float)customModelData), List.of(), List.of(), List.of());
      } else {
         return null;
      }
   }

   @Override
   public void removeData(ItemStack itemStack) {
      NBTTagCompound tag = this.getTag(itemStack);
      if (tag != null) {
         tag.r("CustomModelData");
      }
   }
}
