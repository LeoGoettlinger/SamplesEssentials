package com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R5.items.datacomponents.impl;

import com.artillexstudios.axafkzone.libs.axapi.items.components.data.ItemLore;
import com.artillexstudios.axafkzone.libs.axapi.utils.ComponentSerializer;
import java.util.ArrayList;
import java.util.List;

public final class LoreDataComponent implements DataComponentHandler<ItemLore, net.minecraft.world.item.component.ItemLore> {
   public net.minecraft.world.item.component.ItemLore toNMS(ItemLore from) {
      return switch (from) {
         case ItemLore.Unstyled(List var13) -> new net.minecraft.world.item.component.ItemLore(ComponentSerializer.INSTANCE.toVanillaList(var13));
         case ItemLore.Styled(List var9, List var14) -> new net.minecraft.world.item.component.ItemLore(
            ComponentSerializer.INSTANCE.toVanillaList(var9), ComponentSerializer.INSTANCE.toVanillaList(var14)
         );
         default -> throw new MatchException(null, null);
      };
   }

   public ItemLore fromNMS(net.minecraft.world.item.component.ItemLore data) {
      return new ItemLore.Styled(
         ComponentSerializer.INSTANCE.fromVanillaList(new ArrayList<>(data.a())), ComponentSerializer.INSTANCE.fromVanillaList(new ArrayList<>(data.b()))
      );
   }

   public net.minecraft.world.item.component.ItemLore getDefault() {
      return net.minecraft.world.item.component.ItemLore.a;
   }
}
