package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.input;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class TextInput implements Input {
   private final String key;
   @Nullable
   private final Tag label;
   private final String initial;
   private final int maxLength;
   private final TextInput.MultilineOptions[] options;
   private String value;

   public TextInput(CompoundTag tag) {
      this.key = tag.getString("key");
      this.label = tag.getBoolean("label_visible", true) ? tag.get("label") : null;
      this.initial = tag.getString("initial", "");
      this.maxLength = tag.getInt("max_length", 32);
      if (this.maxLength < 1) {
         throw new IllegalArgumentException("Max length must be at least 1, got: " + this.maxLength);
      } else {
         ListTag<CompoundTag> multilineList = tag.getListTag("multiline", CompoundTag.class);
         if (multilineList != null && !multilineList.isEmpty()) {
            this.options = multilineList.stream().map(TextInput.MultilineOptions::new).toArray(TextInput.MultilineOptions[]::new);
         } else {
            this.options = null;
         }

         this.value = this.initial;
      }
   }

   @Override
   public String key() {
      return this.key;
   }

   @Override
   public String asCommandSubstitution() {
      return this.value;
   }

   @Override
   public Tag asTag() {
      return new StringTag(this.value);
   }

   @Nullable
   public Tag label() {
      return this.label;
   }

   public String initial() {
      return this.initial;
   }

   public int maxLength() {
      return this.maxLength;
   }

   public TextInput.MultilineOptions[] options() {
      return this.options;
   }

   public String value() {
      return this.value;
   }

   public void setValue(String value) {
      if (value.length() > this.maxLength) {
         throw new IllegalArgumentException("Value exceeds max length of " + this.maxLength + ": " + value);
      } else {
         this.value = value;
      }
   }

   public void setClampedValue(String value) {
      if (value.length() > this.maxLength) {
         this.value = value.substring(0, this.maxLength);
      } else {
         this.value = value;
      }
   }

   public static class MultilineOptions {
      @Nullable
      private final Integer maxLines;

      public MultilineOptions(CompoundTag tag) {
         IntTag maxLinesTag = tag.getIntTag("max_lines");
         if (maxLinesTag != null && maxLinesTag.asInt() < 1) {
            throw new IllegalArgumentException("Max lines must be at least 1, got: " + maxLinesTag);
         } else {
            this.maxLines = maxLinesTag != null ? maxLinesTag.asInt() : null;
         }
      }

      @Nullable
      public Integer maxLines() {
         return this.maxLines;
      }
   }
}
