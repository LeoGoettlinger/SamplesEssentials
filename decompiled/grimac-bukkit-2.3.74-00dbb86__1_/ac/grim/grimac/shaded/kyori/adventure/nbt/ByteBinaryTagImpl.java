package ac.grim.grimac.shaded.kyori.adventure.nbt;

import ac.grim.grimac.shaded.jetbrains.annotations.Debug;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.examination.ExaminableProperty;
import java.util.stream.Stream;

@Debug.Renderer(
   text = "\"0x\" + Integer.toString(this.value, 16)",
   hasChildren = "false"
)
final class ByteBinaryTagImpl extends AbstractBinaryTag implements ByteBinaryTag {
   private final byte value;

   ByteBinaryTagImpl(final byte value) {
      this.value = value;
   }

   @Override
   public byte value() {
      return this.value;
   }

   @Override
   public byte byteValue() {
      return this.value;
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
         ByteBinaryTagImpl that = (ByteBinaryTagImpl)other;
         return this.value == that.value;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Byte.hashCode(this.value);
   }

   @NotNull
   @Override
   public Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(ExaminableProperty.of("value", this.value));
   }
}
