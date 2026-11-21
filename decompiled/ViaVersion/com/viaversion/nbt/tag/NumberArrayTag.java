package com.viaversion.nbt.tag;

public interface NumberArrayTag extends Tag {
   int length();

   ListTag<? extends NumberTag> toListTag();

   NumberArrayTag copy();
}
