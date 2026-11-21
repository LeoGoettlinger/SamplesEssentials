package me.clip.placeholderapi.libs.kyori.adventure.nbt;

import java.util.Collections;

final class ListBinaryTag0 {
   private static final String WRAPPER_KEY = "";

   private ListBinaryTag0() {
   }

   static BinaryTag unbox(final CompoundBinaryTag compound) {
      if (compound.size() == 1) {
         BinaryTag potentialValue = compound.get("");
         if (potentialValue != null) {
            return potentialValue;
         }
      }

      return compound;
   }

   static CompoundBinaryTag box(final BinaryTag tag) {
      return (CompoundBinaryTag)(tag instanceof CompoundBinaryTag ? (CompoundBinaryTag)tag : new CompoundBinaryTagImpl(Collections.singletonMap("", tag)));
   }
}
