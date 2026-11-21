package de.oliver.fancysitula.api.dialogs.inputs;

import org.jetbrains.annotations.Nullable;

public class FS_DialogNumberRangeInput implements FS_DialogInputControl {
   private int width;
   private String label;
   private String labelFormat;
   private float start;
   private float end;
   @Nullable
   private Float initial;
   @Nullable
   private Float step;

   public FS_DialogNumberRangeInput(int width, String label, String labelFormat, float start, float end, @Nullable Float initial, @Nullable Float step) {
      this.width = width;
      this.label = label;
      this.labelFormat = labelFormat;
      this.start = start;
      this.end = end;
      this.initial = initial;
      this.step = step;
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

   public String getLabelFormat() {
      return this.labelFormat;
   }

   public void setLabelFormat(String labelFormat) {
      this.labelFormat = labelFormat;
   }

   public float getStart() {
      return this.start;
   }

   public void setStart(float start) {
      this.start = start;
   }

   public float getEnd() {
      return this.end;
   }

   public void setEnd(float end) {
      this.end = end;
   }

   @Nullable
   public Float getInitial() {
      return this.initial;
   }

   public void setInitial(@Nullable Float initial) {
      this.initial = initial;
   }

   @Nullable
   public Float getStep() {
      return this.step;
   }

   public void setStep(@Nullable Float step) {
      this.step = step;
   }
}
