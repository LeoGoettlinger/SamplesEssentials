package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R5.items.datacomponents.impl;

import com.artillexstudios.axrankmenu.libs.axapi.items.component.type.Unit;

public final class UnitDataComponent implements DataComponentHandler<Unit, net.minecraft.util.Unit> {
   public net.minecraft.util.Unit toNMS(Unit from) {
      return from == null ? null : net.minecraft.util.Unit.a;
   }

   public Unit fromNMS(net.minecraft.util.Unit data) {
      return data == null ? null : Unit.INSTANCE;
   }
}
