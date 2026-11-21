package org.sethomegui.SetHomeGUI.libs.updater.operators;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.SetHomeGUI.libs.route.Route;
import org.sethomegui.SetHomeGUI.libs.settings.updater.ValueMapper;

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
