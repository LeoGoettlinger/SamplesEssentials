package ac.grim.grimac.shaded.kyori.adventure.nbt;

import ac.grim.grimac.shaded.jetbrains.annotations.Debug;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.examination.ExaminableProperty;
import java.util.stream.Stream;

@Debug.Renderer(
   text = "String.valueOf(this.value) + \"s\"",
   hasChildren = "false"
)
final class ShortBinaryTagImpl extends AbstractBinaryTag implements ShortBinaryTag {
   private final short value;

   ShortBinaryTagImpl(final short value) {
      this.value = value;
   }

   @Override
   public short value() {
      return this.value;
   }

   @Override
   public byte byteValue() {
      return (byte)(this.value & 255);
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
      return this.value;
   }

   @Override
   public long longValue() {
      return this.value;
   }

   @Override
   public short shortValue() {
      return this.value;
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
         ShortBinaryTagImpl that = (ShortBinaryTagImpl)other;
         return this.value == that.value;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Short.hashCode(this.value);
   }

   @NotNull
   @Override
   public Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(ExaminableProperty.of("value", this.value));
   }
}
