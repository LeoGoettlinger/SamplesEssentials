package xyz.lychee.lagfixer.libs.kyori.adventure.nbt;

import java.util.stream.Stream;
import org.jetbrains.annotations.Debug;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.examination.ExaminableProperty;

@Debug.Renderer(
   text = "String.valueOf(this.value) + \"f\"",
   hasChildren = "false"
)
final class FloatBinaryTagImpl extends AbstractBinaryTag implements FloatBinaryTag {
   private final float value;

   FloatBinaryTagImpl(final float value) {
      this.value = value;
   }

   @Override
   public float value() {
      return this.value;
   }

   @Override
   public byte byteValue() {
      return (byte)(ShadyPines.floor(this.value) & 0xFF);
   }

   @Override
   public double doubleValue() {
      return this.value;
   }

   @Override
   public float floatValue() {
      return this.value;
   }

   @Override
   public int intValue() {
      return ShadyPines.floor(this.value);
   }

   @Override
   public long longValue() {
      return (long)this.value;
   }

   @Override
   public short shortValue() {
      return (short)(ShadyPines.floor(this.value) & 65535);
   }

   @NotNull
   @Override
   public Number numberValue() {
      return this.value;
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (other != null && this.getClass() == other.getClass()) {
         FloatBinaryTagImpl that = (FloatBinaryTagImpl)other;
         return Float.floatToIntBits(this.value) == Float.floatToIntBits(that.value);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Float.hashCode(this.value);
   }

   @NotNull
   @Override
   public Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(ExaminableProperty.of("value", this.value));
   }
}
