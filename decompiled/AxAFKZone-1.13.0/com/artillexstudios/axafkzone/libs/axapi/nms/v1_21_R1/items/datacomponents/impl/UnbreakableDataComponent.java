package com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R1.items.datacomponents.impl;

import com.artillexstudios.axafkzone.libs.axapi.items.component.type.Unit;
import net.minecraft.world.item.component.Unbreakable;

public final class UnbreakableDataComponent implements DataComponentHandler<Unit, Unbreakable> {
   public Unbreakable toNMS(Unit from) {
      return new Unbreakable(false);
   }

   public Unit fromNMS(Unbreakable data) {
      return Unit.INSTANCE;
   }
}
