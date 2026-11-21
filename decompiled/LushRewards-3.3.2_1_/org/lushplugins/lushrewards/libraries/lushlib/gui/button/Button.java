package org.lushplugins.lushrewards.libraries.lushlib.gui.button;

import java.util.function.Consumer;
import org.bukkit.event.inventory.InventoryClickEvent;

public class Button {
   private Consumer<InventoryClickEvent> onClick;

   public Button(Consumer<InventoryClickEvent> onClick) {
      this.onClick = onClick;
   }

   public void click(InventoryClickEvent event) {
      this.onClick.accept(event);
   }

   public void onClick(Consumer<InventoryClickEvent> onClick) {
      this.onClick = onClick;
   }
}
