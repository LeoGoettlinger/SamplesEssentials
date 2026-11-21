package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.input;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.utils.ChatUtil;

public final class NumberRangeInput implements Input {
   private final String key;
   private final Tag label;
   private final String labelFormat;
   private final float start;
   private final float end;
   private final Float initial;
   private final Float step;
   private float value;

   public NumberRangeInput(CompoundTag tag) {
      this.key = tag.getString("key");
      this.label = tag.get("label");
      this.labelFormat = tag.getString("label_format", "options.generic_value");
      this.start = tag.getFloat("start");
      this.end = tag.getFloat("end");
      this.initial = tag.getFloat("initial", (this.start + this.end) / 2.0F);
      FloatTag stepTag = tag.getFloatTag("step");
      if (stepTag != null && stepTag.asFloat() < 0.0F) {
         throw new IllegalArgumentException("Step must be non-negative, got: " + stepTag.asFloat());
      } else {
         this.step = stepTag == null ? -1.0F : stepTag.asFloat();
         this.value = this.initial;
      }
   }

   @Override
   public String key() {
      return this.key;
   }

   @Override
   public String asCommandSubstitution() {
      return this.valueAsString();
   }

   @Override
   public Tag asTag() {
      return new FloatTag(this.value);
   }

   public Tag label() {
      return this.label;
   }

   public String labelFormat() {
      return this.labelFormat;
   }

   public float start() {
      return this.start;
   }

   public float end() {
      return this.end;
   }

   public float initial() {
      return this.initial;
   }

   public float step() {
      return this.step;
   }

   public float value() {
      return this.value;
   }

   public String valueAsString() {
      int asInt = (int)this.value;
      return asInt == this.value ? Integer.toString(asInt) : Float.toString(this.value);
   }

   public void setValue(float value) {
      this.value = value;
   }

   public void setClampedValue(float value) {
      this.value = value < this.start ? this.start : Math.min(value, this.end);
   }

   public Tag displayName() {
      return ChatUtil.translate(this.labelFormat, this.label, ChatUtil.translate(this.valueAsString()));
   }
}
