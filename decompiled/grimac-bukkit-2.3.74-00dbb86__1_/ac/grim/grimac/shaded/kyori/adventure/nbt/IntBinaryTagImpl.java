package ac.grim.grimac.shaded.kyori.adventure.nbt;

import ac.grim.grimac.shaded.jetbrains.annotations.Debug;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.examination.ExaminableProperty;
import java.util.stream.Stream;

@Debug.Renderer(
   text = "String.valueOf(this.value) + \"i\"",
   hasChildren = "false"
)
final class IntBinaryTagImpl extends AbstractBinaryTag implements IntBinaryTag {
   private final int value;

   IntBinaryTagImpl(final int value) {
      this.value = value;
   }

   @Override
   public int value() {
      return this.value;
   }

   @Override
   public byte byteValue() {
      return (byte)(this.value & 0xFF);
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
      return (short)(this.value & 65535);
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
         IntBinaryTagImpl that = (IntBinaryTagImpl)other;
         return this.value == that.value;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Integer.hashCode(this.value);
   }

   @NotNull
   @Override
   public Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(ExaminableProperty.of("value", this.value));
   }
}
