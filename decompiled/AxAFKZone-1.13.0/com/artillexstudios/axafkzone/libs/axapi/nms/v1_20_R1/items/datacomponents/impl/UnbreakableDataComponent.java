package com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R1.items.datacomponents.impl;

import com.artillexstudios.axafkzone.libs.axapi.items.component.type.Unit;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;

public final class UnbreakableDataComponent implements DataComponentHandler<Unit> {
   public void setData(ItemStack from, Unit data) {
      NBTTagCompound tag = this.getOrCreateTag(from);
      tag.a("Unbreakable", true);
   }

   public Unit getData(ItemStack stack) {
      NBTTagCompound tag = this.getTag(stack);
      if (tag != null && tag.e("Unbreakable")) {
         return tag.q("Unbreakable") ? Unit.INSTANCE : null;
      } else {
         return null;
      }
   }

   @Override
   public void removeData(ItemStack itemStack) {
      NBTTagCompound tag = this.getTag(itemStack);
      if (tag != null && tag.e("Unbreakable")) {
         tag.r("Unbreakable");
      }
   }
}
