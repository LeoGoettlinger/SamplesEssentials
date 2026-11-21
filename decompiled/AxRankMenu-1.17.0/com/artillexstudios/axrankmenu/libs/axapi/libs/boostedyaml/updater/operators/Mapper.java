package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.updater.operators;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.route.Route;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.settings.updater.ValueMapper;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class Mapper {
   public static void apply(@NotNull Section section, @NotNull Map<Route, ValueMapper> mappers) {
      mappers.forEach((route, mapper) -> section.getParent(route).ifPresent(parent -> {
         Route key = Route.fromSingleKey(route.get(route.length() - 1));
         if (parent.getStoredValue().containsKey(key.get(0))) {
            parent.set(key, mapper.map(parent, key));
         }
      }));
   }
}
