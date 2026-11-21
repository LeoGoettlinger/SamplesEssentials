package github.nighter.smartspawner.api.gui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;

public class GuiLayoutData {
   private final GuiLayoutType type;
   private final Map<String, GuiButtonData> buttons;

   public GuiLayoutData(GuiLayoutType type, Map<String, GuiButtonData> buttons) {
      this.type = type;
      this.buttons = buttons != null ? Collections.unmodifiableMap(new HashMap<>(buttons)) : Collections.emptyMap();
   }

   @Generated
   public GuiLayoutType getType() {
      return this.type;
   }

   @Generated
   public Map<String, GuiButtonData> getButtons() {
      return this.buttons;
   }
}
