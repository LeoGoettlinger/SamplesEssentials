package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R7.items.datacomponents.impl;

import com.artillexstudios.axrankmenu.libs.axapi.items.nbt.CompoundTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.component.CustomData;

public final class CustomDataDataComponent implements DataComponentHandler<CompoundTag, CustomData> {
   public CustomData toNMS(CompoundTag from) {
      return CustomData.a(((com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R7.items.nbt.CompoundTag)from).getParent());
   }

   public CompoundTag fromNMS(CustomData data) {
      return new com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R7.items.nbt.CompoundTag(data.b());
   }

   public CustomData getDefault() {
      return CustomData.a(new NBTTagCompound());
   }
}
