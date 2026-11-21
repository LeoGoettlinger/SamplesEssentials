package dev.vatten.baserad;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.TextComponent;

public interface Renderable {
   Component asComponent();

   default int getLength() {
      return this.getLength(this.asComponent());
   }

   default int estimateWidth() {
      return this.estimateWidth(this.asComponent());
   }

   private int getLength(Component component) {
      int length = 0;
      if (component instanceof TextComponent textComponent) {
         length += textComponent.content().length();
      } else {
         length++;
      }

      for (Component child : component.children()) {
         length += this.getLength(child);
      }

      return length;
   }

   private int estimateWidth(Component component) {
      int width = 0;
      if (component instanceof TextComponent textComponent) {
         width += textComponent.content().length() * 5;
      } else if (component instanceof ObjectComponent) {
         width += 8;
      }

      for (Component child : component.children()) {
         width += this.estimateWidth(child);
      }

      return width;
   }
}
