package com.github.retrooper.packetevents.protocol.nbt;

import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class NBT {
   public abstract NBTType<?> getType();

   @Override
   public abstract boolean equals(@Nullable Object other);

   @Override
   public abstract int hashCode();

   @Override
   public String toString() {
      return "nbt";
   }

   public abstract NBT copy();

   public <T> T castOrThrow(Class<T> clazz) throws NbtCodecException {
      if (clazz.isInstance(this)) {
         return (T)this;
      } else {
         throw new NbtCodecException("expected: " + clazz.getName() + ", actual: " + this);
      }
   }
}
