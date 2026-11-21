package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R6.items.datacomponents.impl;

import com.artillexstudios.axrankmenu.libs.axapi.items.components.data.TooltipDisplay;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.stream.Collectors;
import net.minecraft.core.Holder.c;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;

public final class TooltipDisplayDataComponent implements DataComponentHandler<TooltipDisplay, net.minecraft.world.item.component.TooltipDisplay> {
   public net.minecraft.world.item.component.TooltipDisplay toNMS(TooltipDisplay from) {
      return new net.minecraft.world.item.component.TooltipDisplay(
         from.hideTooltip(),
         from.hiddenComponents()
            .stream()
            .map(component -> (DataComponentType)((c)BuiltInRegistries.an.c(MinecraftKey.a("minecraft", component)).orElseThrow()).a())
            .collect(Collectors.toCollection(ReferenceLinkedOpenHashSet::new))
      );
   }

   public TooltipDisplay fromNMS(net.minecraft.world.item.component.TooltipDisplay data) {
      return new TooltipDisplay(
         data.a(), data.b().stream().map(component -> BuiltInRegistries.an.b(component).a()).collect(Collectors.toCollection(ArrayList::new))
      );
   }
}
