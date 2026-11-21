package com.github.retrooper.packetevents.protocol.nbt;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class NBTEnd extends NBT {
   public static final NBTEnd INSTANCE = new NBTEnd();

   @Override
   public NBTType<NBTEnd> getType() {
      return NBTType.END;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj == null ? false : this.getClass() == obj.getClass();
      }
   }

   @Override
   public int hashCode() {
      return 0;
   }

   public NBTEnd copy() {
      return this;
   }

   @Override
   public String toString() {
      return "END";
   }
}
