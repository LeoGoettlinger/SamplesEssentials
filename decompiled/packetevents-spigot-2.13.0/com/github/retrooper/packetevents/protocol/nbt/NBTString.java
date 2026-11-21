package com.github.retrooper.packetevents.protocol.nbt;

import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class NBTString extends NBT {
   protected final String string;

   public NBTString(String string) {
      this.string = string;
   }

   @Override
   public NBTType<NBTString> getType() {
      return NBTType.STRING;
   }

   public String getValue() {
      return this.string;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         NBTString other = (NBTString)obj;
         return Objects.equals(this.string, other.string);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.string);
   }

   public NBTString copy() {
      return this;
   }

   @Override
   public String toString() {
      return "String(" + this.string + ")";
   }
}
