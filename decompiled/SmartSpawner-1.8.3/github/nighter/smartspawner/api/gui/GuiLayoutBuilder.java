package github.nighter.smartspawner.api.gui;

import java.util.HashMap;
import java.util.Map;

public class GuiLayoutBuilder {
   private GuiLayoutType type = GuiLayoutType.MAIN_GUI;
   private final Map<String, GuiButtonData> buttons = new HashMap<>();

   public GuiLayoutBuilder type(GuiLayoutType type) {
      this.type = type;
      return this;
   }

   public GuiLayoutBuilder addButton(GuiButtonData button) {
      if (button == null) {
         throw new IllegalArgumentException("button cannot be null");
      } else {
         String key = button.getButtonType() != null ? button.getButtonType() : "slot_" + button.getSlot();
         this.buttons.put(key, button);
         return this;
      }
   }

   public GuiLayoutBuilder addButton(String key, GuiButtonData button) {
      if (key == null || key.isBlank()) {
         throw new IllegalArgumentException("key cannot be blank");
      } else if (button == null) {
         throw new IllegalArgumentException("button cannot be null");
      } else {
         this.buttons.put(key, button);
         return this;
      }
   }

   public GuiLayoutData build() {
      return new GuiLayoutData(this.type, new HashMap<>(this.buttons));
   }
}
