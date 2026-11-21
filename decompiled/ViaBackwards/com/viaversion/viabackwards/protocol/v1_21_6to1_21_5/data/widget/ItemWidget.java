package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.widget;

import com.viaversion.nbt.tag.CompoundTag;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ItemWidget implements Widget {
   private final CompoundTag item;
   @Nullable
   private final TextWidget description;
   private final boolean showTooltip;
   private final int width;
   private final int height;

   public ItemWidget(CompoundTag tag) {
      this.item = tag.getCompoundTag("item");
      if (this.item == null) {
         throw new IllegalArgumentException("Item tag is missing in ItemWidget tag: " + tag);
      } else {
         this.description = tag.contains("description") ? new TextWidget(tag.getCompoundTag("description")) : null;
         this.showTooltip = tag.getBoolean("show_tooltip", true);
         int width = tag.getInt("width", 16);
         int height = tag.getInt("height", 16);
         if (width < 1 || width > 256) {
            throw new IllegalArgumentException("Width must be between 1 and 256, got: " + width);
         } else if (height >= 1 && height <= 256) {
            this.width = width;
            this.height = height;
         } else {
            throw new IllegalArgumentException("Height must be between 1 and 256, got: " + height);
         }
      }
   }

   public CompoundTag item() {
      return this.item;
   }

   @Nullable
   public TextWidget description() {
      return this.description;
   }

   public boolean showTooltip() {
      return this.showTooltip;
   }

   public int width() {
      return this.width;
   }

   public int height() {
      return this.height;
   }
}
