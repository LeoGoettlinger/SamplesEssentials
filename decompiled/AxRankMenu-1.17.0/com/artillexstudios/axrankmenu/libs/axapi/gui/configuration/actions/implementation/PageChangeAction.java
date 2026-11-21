package com.artillexstudios.axrankmenu.libs.axapi.gui.configuration.actions.implementation;

import com.artillexstudios.axrankmenu.libs.axapi.context.HashMapContext;
import com.artillexstudios.axrankmenu.libs.axapi.gui.configuration.actions.Action;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.Gui;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.GuiKeys;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.implementation.PaginatedGui;
import com.artillexstudios.axrankmenu.libs.axapi.utils.NumberUtils;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axrankmenu.libs.axapi.utils.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;

public final class PageChangeAction extends Action<PageChangeAction.Direction> {
   public PageChangeAction(String data) {
      super(data);
   }

   public PageChangeAction.Direction transform(String data) {
      return PageChangeAction.Direction.fetch(data);
   }

   @Override
   public void run(Player player, HashMapContext context) {
      Gui gui = context.get(GuiKeys.GUI);
      if (gui instanceof PaginatedGui paginatedGui) {
         if (this.value().page == -1) {
            if (!paginatedGui.hasPreviousPage()) {
               if (FeatureFlags.DEBUG.get()) {
                  LogUtils.debug("Gui has no previous page!");
               }

               return;
            }

            paginatedGui.page(paginatedGui.page() - 1);
         } else if (this.value().page == -2) {
            if (!paginatedGui.hasNextPage()) {
               if (FeatureFlags.DEBUG.get()) {
                  LogUtils.debug("Gui has no next page!");
               }

               return;
            }

            paginatedGui.page(paginatedGui.page() + 1);
         } else {
            if (!paginatedGui.hasPage(this.value().page)) {
               if (FeatureFlags.DEBUG.get()) {
                  LogUtils.debug("Gui has no page with index {}!", this.value().page);
               }

               return;
            }

            paginatedGui.page(this.value().page);
         }

         paginatedGui.open();
      } else {
         if (FeatureFlags.DEBUG.get()) {
            LogUtils.debug("Gui is not a paginated gui! Gui: {}", gui);
         }
      }
   }

   public record Direction(int page, String... aliases) {
      private static final List<PageChangeAction.Direction> directions = new ArrayList<>();
      public static final PageChangeAction.Direction BACKWARDS = register(new PageChangeAction.Direction(-1, "back", "backwards", "prev", "previous"));
      public static final PageChangeAction.Direction FORWARDS = register(new PageChangeAction.Direction(-2, "forwards", "forward", "next"));

      public static PageChangeAction.Direction register(PageChangeAction.Direction direction) {
         directions.add(direction);
         return direction;
      }

      public static PageChangeAction.Direction fetch(String directionName) {
         if (NumberUtils.isInt(directionName)) {
            return new PageChangeAction.Direction(Integer.parseInt(directionName));
         } else {
            for (PageChangeAction.Direction direction : directions) {
               if (ArrayUtils.contains(direction.aliases, directionName)) {
                  return direction;
               }
            }

            return null;
         }
      }
   }
}
