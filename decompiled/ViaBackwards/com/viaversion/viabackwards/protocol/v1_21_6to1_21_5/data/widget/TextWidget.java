package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.widget;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;

public final class TextWidget implements Widget {
   private final Tag label;
   private final int width;

   public TextWidget(CompoundTag tag) {
      this.label = tag.get("contents");
      this.width = tag.getInt("width", 200);
   }

   public Tag label() {
      return this.label;
   }

   public int width() {
      return this.width;
   }
}
