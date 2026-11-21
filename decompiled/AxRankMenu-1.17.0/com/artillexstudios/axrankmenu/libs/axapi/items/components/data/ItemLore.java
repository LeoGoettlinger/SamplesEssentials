package com.artillexstudios.axrankmenu.libs.axapi.items.components.data;

import java.util.List;
import net.kyori.adventure.text.Component;

public sealed interface ItemLore permits ItemLore.Unstyled, ItemLore.Styled {
   List<Component> lines();

   static ItemLore create(List<Component> lines) {
      return new ItemLore.Unstyled(lines);
   }

   static ItemLore create(List<Component> lines, List<Component> styled) {
      return new ItemLore.Styled(lines, styled);
   }

   public record Styled(List<Component> lines, List<Component> styledLines) implements ItemLore {
   }

   public record Unstyled(List<Component> lines) implements ItemLore {
   }
}
