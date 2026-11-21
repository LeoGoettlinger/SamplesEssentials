package de.oliver.fancysitula.api.dialogs.actions;

import org.jetbrains.annotations.Nullable;

public class FS_CommonButtonData {
   private String label;
   @Nullable
   private String tooltip;
   private int width;

   public FS_CommonButtonData(String label, @Nullable String tooltip, int width) {
      this.label = label;
      this.tooltip = tooltip;
      this.width = width;
   }

   public String getLabel() {
      return this.label;
   }

   public void setLabel(String label) {
      this.label = label;
   }

   @Nullable
   public String getTooltip() {
      return this.tooltip;
   }

   public void setTooltip(@Nullable String tooltip) {
      this.tooltip = tooltip;
   }

   public int getWidth() {
      return this.width;
   }

   public void setWidth(int width) {
      this.width = width;
   }
}
