package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.input;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class SingleOptionInput implements Input {
   private final String key;
   private final SingleOptionInput.Entry[] options;
   @Nullable
   private final Tag label;
   private int value;

   public SingleOptionInput(CompoundTag tag) {
      ListTag<CompoundTag> options = tag.getListTag("options", CompoundTag.class);
      if (options != null && !options.isEmpty()) {
         this.key = tag.getString("key");
         this.options = options.stream().map(SingleOptionInput.Entry::new).toArray(SingleOptionInput.Entry[]::new);
         this.label = tag.getBoolean("label_visible", true) ? tag.get("label") : null;

         for (int i = 0; i < this.options.length; i++) {
            if (this.options[i].initial()) {
               if (this.value != 0) {
                  throw new IllegalArgumentException("Multiple initial options found in tag: " + tag);
               }

               this.value = i;
            }
         }
      } else {
         throw new IllegalArgumentException("Options must not be empty in tag: " + tag);
      }
   }

   @Override
   public String key() {
      return this.key;
   }

   @Override
   public String asCommandSubstitution() {
      return this.options[this.value].id;
   }

   @Override
   public Tag asTag() {
      return new StringTag(this.asCommandSubstitution());
   }

   public SingleOptionInput.Entry[] options() {
      return this.options;
   }

   @Nullable
   public Tag label() {
      return this.label;
   }

   public int value() {
      return this.value;
   }

   public void setValue(int value) {
      if (value >= 0 && value < this.options.length) {
         this.value = value;
      } else {
         throw new IllegalArgumentException("Value must be between 0 and " + (this.options.length - 1));
      }
   }

   public void setClampedValue(int value) {
      if (value < 0) {
         this.value = 0;
      } else if (value >= this.options.length) {
         this.value = 0;
      } else {
         this.value = value;
      }
   }

   public static class Entry {
      private final String id;
      private final Tag display;
      private final boolean initial;

      public Entry(CompoundTag tag) {
         this.id = tag.getString("id");
         this.display = tag.get("display");
         this.initial = tag.getBoolean("initial", false);
      }

      public String id() {
         return this.id;
      }

      public Tag display() {
         return this.display;
      }

      public boolean initial() {
         return this.initial;
      }

      public Tag computeDisplay() {
         return Objects.requireNonNullElseGet(this.display, () -> new StringTag(this.id));
      }
   }
}
