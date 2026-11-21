package com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.datacomponents.impl;

import com.artillexstudios.axafkzone.libs.axapi.utils.ComponentSerializer;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.IChatBaseComponent;

public final class AdventureComponentDataComponent implements DataComponentHandler<Component, IChatBaseComponent> {
   public IChatBaseComponent toNMS(Component from) {
      return ComponentSerializer.INSTANCE.toVanilla(from);
   }

   public Component fromNMS(IChatBaseComponent data) {
      return ComponentSerializer.INSTANCE.fromVanilla(data);
   }
}
