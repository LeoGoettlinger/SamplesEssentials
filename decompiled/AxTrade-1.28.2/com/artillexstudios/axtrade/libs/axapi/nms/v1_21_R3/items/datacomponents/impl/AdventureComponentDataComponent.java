package com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R3.items.datacomponents.impl;

import com.artillexstudios.axtrade.libs.axapi.utils.ComponentSerializer;
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
