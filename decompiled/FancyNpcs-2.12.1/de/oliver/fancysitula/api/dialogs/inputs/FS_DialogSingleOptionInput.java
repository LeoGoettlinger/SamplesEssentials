package de.oliver.fancysitula.api.dialogs.inputs;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public class FS_DialogSingleOptionInput implements FS_DialogInputControl {
   private int width;
   private List<FS_DialogSingleOptionInput.Entry> entries;
   private String label;
   private boolean isLabelVisible;

   public FS_DialogSingleOptionInput(int width, List<FS_DialogSingleOptionInput.Entry> entries, String label, boolean isLabelVisible) {
      this.width = width;
      this.entries = entries;
      this.label = label;
      this.isLabelVisible = isLabelVisible;
   }

   public int getWidth() {
      return this.width;
   }

   public void setWidth(int width) {
      this.width = width;
   }

   public List<FS_DialogSingleOptionInput.Entry> getEntries() {
      return this.entries;
   }

   public void setEntries(List<FS_DialogSingleOptionInput.Entry> entries) {
      this.entries = entries;
   }

   public String getLabel() {
      return this.label;
   }

   public void setLabel(String label) {
      this.label = label;
   }

   public boolean isLabelVisible() {
      return this.isLabelVisible;
   }

   public void setLabelVisible(boolean labelVisible) {
      this.isLabelVisible = labelVisible;
   }

   public static class Entry {
      private String id;
      @Nullable
      private String display;
      private boolean initial;

      public Entry(String id, @Nullable String display, boolean initial) {
         this.id = id;
         this.display = display;
         this.initial = initial;
      }

      public String getId() {
         return this.id;
      }

      public void setId(String id) {
         this.id = id;
      }

      @Nullable
      public String getDisplay() {
         return this.display;
      }

      public void setDisplay(@Nullable String display) {
         this.display = display;
      }

      public boolean isInitial() {
         return this.initial;
      }

      public void setInitial(boolean initial) {
         this.initial = initial;
      }
   }
}
