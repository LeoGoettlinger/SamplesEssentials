package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.gui.ConfigGui;
import com.oheers.fish.gui.GuiUtils;
import com.oheers.fish.items.ItemFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.inventorygui.GuiElement;
import org.evenmorefish.fish.libs.inventorygui.StaticGuiElement;
import org.jspecify.annotations.NonNull;

public class GuiFillerConfig extends ConfigBase {
   private static GuiFillerConfig instance;

   public GuiFillerConfig() {
      super("gui-fillers.yml", "gui-fillers.yml", EvenMoreFish.getInstance(), true);
      instance = this;
   }

   public static GuiFillerConfig getInstance() {
      return instance;
   }

   public List<GuiElement> getDefaultFillerItems(@NonNull ConfigGui gui) {
      List<GuiElement> elements = new ArrayList<>();
      this.getConfig().getRoutesAsStrings(false).forEach(key -> {
         Section itemSection = this.getConfig().getSection(key);
         if (itemSection != null && itemSection.contains("item")) {
            StaticGuiElement element = this.getGuiItem(gui, itemSection);
            if (element != null) {
               elements.add(element);
            }
         }
      });
      return elements;
   }

   private StaticGuiElement getGuiItem(@NonNull ConfigGui gui, @NonNull Section itemSection) {
      char character = FishUtils.getCharFromString(itemSection.getString("character", "#"), '#');
      if (character == '#') {
         return null;
      } else {
         ItemFactory factory = ItemFactory.itemFactory(itemSection);
         ItemStack item = factory.createItem();
         if (item.getType() == Material.AIR) {
            return null;
         } else {
            Section actionSection = itemSection.getSection("click-action");
            return actionSection != null ? new StaticGuiElement(character, item, click -> {
               BiConsumer<ConfigGui, GuiElement.Click> action = switch (click.getType()) {
                  case LEFT -> (BiConsumer)GuiUtils.getActionMap().get(actionSection.getString("left", ""));
                  case RIGHT -> (BiConsumer)GuiUtils.getActionMap().get(actionSection.getString("right", ""));
                  case MIDDLE -> (BiConsumer)GuiUtils.getActionMap().get(actionSection.getString("middle", ""));
                  case DROP -> (BiConsumer)GuiUtils.getActionMap().get(actionSection.getString("drop", ""));
                  default -> null;
               };
               if (action != null) {
                  action.accept(null, click);
               }

               return true;
            }) : new StaticGuiElement(character, item, click -> {
               BiConsumer<ConfigGui, GuiElement.Click> action = GuiUtils.getActionMap().get(itemSection.getString("click-action", ""));
               if (action != null) {
                  action.accept(gui, click);
               }

               return true;
            });
         }
      }
   }
}
