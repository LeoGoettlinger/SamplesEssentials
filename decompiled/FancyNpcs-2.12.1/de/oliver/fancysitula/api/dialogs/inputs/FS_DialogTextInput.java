package de.oliver.fancysitula.api.dialogs.inputs;

import org.jetbrains.annotations.Nullable;

public class FS_DialogTextInput implements FS_DialogInputControl {
   private int width;
   private String label;
   private boolean isLabelVisible;
   private String initial;
   private int maxLength;
   @Nullable
   private FS_DialogTextInput.MultilineOptions multilineOptions;

   public FS_DialogTextInput(
      int width, String label, boolean isLabelVisible, String initial, int maxLength, @Nullable FS_DialogTextInput.MultilineOptions multilineOptions
   ) {
      this.width = width;
      this.label = label;
      this.isLabelVisible = isLabelVisible;
      this.initial = initial;
      this.maxLength = maxLength;
      this.multilineOptions = multilineOptions;
   }

   public int getWidth() {
      return this.width;
   }

   public void setWidth(int width) {
      this.width = width;
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

   public String getInitial() {
      return this.initial;
   }

   public void setInitial(String initial) {
      this.initial = initial;
   }

   public int getMaxLength() {
      return this.maxLength;
   }

   public void setMaxLength(int maxLength) {
      this.maxLength = maxLength;
   }

   @Nullable
   public FS_DialogTextInput.MultilineOptions getMultilineOptions() {
      return this.multilineOptions;
   }

   public void setMultilineOptions(@Nullable FS_DialogTextInput.MultilineOptions multilineOptions) {
      this.multilineOptions = multilineOptions;
   }

   public static class MultilineOptions {
      @Nullable
      private Integer maxLines;
      @Nullable
      private Integer height;

      public MultilineOptions(@Nullable Integer maxLines, @Nullable Integer height) {
         this.maxLines = maxLines;
         this.height = height;
      }

      @Nullable
      public Integer getMaxLines() {
         return this.maxLines;
      }

      public void setMaxLines(@Nullable Integer maxLines) {
         this.maxLines = maxLines;
      }

      @Nullable
      public Integer getHeight() {
         return this.height;
      }

      public void setHeight(@Nullable Integer height) {
         this.height = height;
      }
   }
}
