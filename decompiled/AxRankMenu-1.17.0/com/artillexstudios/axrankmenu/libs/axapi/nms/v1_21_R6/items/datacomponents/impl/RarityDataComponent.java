package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R6.items.datacomponents.impl;

import com.artillexstudios.axrankmenu.libs.axapi.items.components.data.Rarity;
import net.minecraft.world.item.EnumItemRarity;

public final class RarityDataComponent implements DataComponentHandler<Rarity, EnumItemRarity> {
   public EnumItemRarity toNMS(Rarity from) {
      return EnumItemRarity.valueOf(from.name());
   }

   public Rarity fromNMS(EnumItemRarity data) {
      return Rarity.valueOf(data.name());
   }
}
