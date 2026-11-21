package net.thenextlvl.nbt.tag;

import java.io.IOException;
import net.thenextlvl.nbt.NBTOutputStream;
import org.jetbrains.annotations.Contract;

public interface Tag {
   @Contract(
      pure = true
   )
   byte getTypeId();

   @Contract(
      mutates = "param1"
   )
   void write(NBTOutputStream var1) throws IOException;

   @Contract(
      pure = true
   )
   default boolean isCompound() {
      return false;
   }

   @Contract(
      pure = true
   )
   default boolean isList() {
      return false;
   }

   @Contract(
      pure = true
   )
   default boolean isNumber() {
      return false;
   }

   @Contract(
      pure = true
   )
   default boolean isBoolean() {
      return this.isNumber();
   }

   @Contract(
      pure = true
   )
   default boolean isString() {
      return false;
   }

   @Contract(
      pure = true
   )
   default boolean isByteArray() {
      return false;
   }

   @Contract(
      pure = true
   )
   default boolean isLongArray() {
      return false;
   }

   @Contract(
      pure = true
   )
   default boolean isIntArray() {
      return false;
   }

   @Contract(
      value = " -> this",
      pure = true
   )
   default CompoundTag getAsCompound() throws UnsupportedOperationException {
      throw new UnsupportedOperationException(this.getClass().getSimpleName());
   }

   @Contract(
      value = " -> this",
      pure = true
   )
   default <V extends Tag> ListTag<V> getAsList() throws UnsupportedOperationException {
      throw new UnsupportedOperationException(this.getClass().getSimpleName());
   }

   @Contract(
      pure = true
   )
   default Number getAsNumber() throws UnsupportedOperationException {
      throw new UnsupportedOperationException(this.getClass().getSimpleName());
   }

   @Contract(
      pure = true
   )
   default String getAsString() throws UnsupportedOperationException {
      throw new UnsupportedOperationException(this.getClass().getSimpleName());
   }

   @Contract(
      pure = true
   )
   default boolean getAsBoolean() throws UnsupportedOperationException {
      throw new UnsupportedOperationException(this.getClass().getSimpleName());
   }

   @Contract(
      pure = true
   )
   default double getAsDouble() throws UnsupportedOperationException {
      return this.getAsNumber().doubleValue();
   }

   @Contract(
      pure = true
   )
   default float getAsFloat() throws UnsupportedOperationException {
      return this.getAsNumber().floatValue();
   }

   @Contract(
      pure = true
   )
   default long getAsLong() throws UnsupportedOperationException {
      return this.getAsNumber().longValue();
   }

   @Contract(
      pure = true
   )
   default int getAsInt() throws UnsupportedOperationException {
      return this.getAsNumber().intValue();
   }

   @Contract(
      pure = true
   )
   default byte getAsByte() throws UnsupportedOperationException {
      return this.getAsNumber().byteValue();
   }

   @Contract(
      pure = true
   )
   default short getAsShort() throws UnsupportedOperationException {
      return this.getAsNumber().shortValue();
   }

   @Contract(
      value = " -> new",
      pure = true
   )
   default byte[] getAsByteArray() throws UnsupportedOperationException {
      throw new UnsupportedOperationException(this.getClass().getSimpleName());
   }

   @Contract(
      value = " -> new",
      pure = true
   )
   default long[] getAsLongArray() throws UnsupportedOperationException {
      throw new UnsupportedOperationException(this.getClass().getSimpleName());
   }

   @Contract(
      value = " -> new",
      pure = true
   )
   default int[] getAsIntArray() throws UnsupportedOperationException {
      throw new UnsupportedOperationException(this.getClass().getSimpleName());
   }
}
