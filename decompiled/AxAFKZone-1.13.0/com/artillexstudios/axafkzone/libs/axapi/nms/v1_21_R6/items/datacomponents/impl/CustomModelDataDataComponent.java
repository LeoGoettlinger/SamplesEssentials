package com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R6.items.datacomponents.impl;

import com.artillexstudios.axafkzone.libs.axapi.items.components.data.CustomModelData;

public final class CustomModelDataDataComponent implements DataComponentHandler<CustomModelData, net.minecraft.world.item.component.CustomModelData> {
   public net.minecraft.world.item.component.CustomModelData toNMS(CustomModelData from) {
      return new net.minecraft.world.item.component.CustomModelData(from.floats(), from.flags(), from.strings(), from.colors());
   }

   public CustomModelData fromNMS(net.minecraft.world.item.component.CustomModelData data) {
      return new CustomModelData(data.a(), data.b(), data.c(), data.d());
   }
}
