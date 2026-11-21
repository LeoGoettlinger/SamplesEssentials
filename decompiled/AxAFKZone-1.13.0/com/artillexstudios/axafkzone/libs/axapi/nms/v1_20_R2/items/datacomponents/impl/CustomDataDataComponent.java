package com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R2.items.datacomponents.impl;

import com.artillexstudios.axafkzone.libs.axapi.items.nbt.CompoundTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;

public final class CustomDataDataComponent implements DataComponentHandler<CompoundTag> {
   public void setData(ItemStack from, CompoundTag data) {
      from.c((NBTTagCompound)data.getParent());
   }

   public CompoundTag getData(ItemStack stack) {
      NBTTagCompound tag = this.getTag(stack);
      return new com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R2.items.nbt.CompoundTag(tag == null ? new NBTTagCompound() : tag);
   }

   @Override
   public void removeData(ItemStack itemStack) {
      itemStack.c(null);
   }
}
