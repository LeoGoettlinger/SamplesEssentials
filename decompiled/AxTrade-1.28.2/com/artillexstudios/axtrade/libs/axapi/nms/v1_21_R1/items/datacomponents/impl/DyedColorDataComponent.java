package com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R1.items.datacomponents.impl;

import com.artillexstudios.axtrade.libs.axapi.items.components.data.DyedItemColor;

public final class DyedColorDataComponent implements DataComponentHandler<DyedItemColor, net.minecraft.world.item.component.DyedItemColor> {
   public net.minecraft.world.item.component.DyedItemColor toNMS(DyedItemColor from) {
      return new net.minecraft.world.item.component.DyedItemColor(from.rgb(), false);
   }

   public DyedItemColor fromNMS(net.minecraft.world.item.component.DyedItemColor data) {
      return new DyedItemColor(data.a());
   }
}
