package com.artillexstudios.axafkzone.libs.axapi.utils.position;

public record ImmutableBlockPosition(int x, int y, int z) implements BlockPosition {
   public ImmutableBlockPosition(long packedPos) {
      this((int)(packedPos >> 38), (int)(packedPos << 52 >> 52), (int)(packedPos << 26 >> 38));
   }

   public ImmutableBlockPosition() {
      this(0, 0, 0);
   }

   @Override
   public long asLong() {
      return (this.x & 67108863L) << 38 | this.y & 4095L | (this.z & 67108863L) << 12;
   }

   @Override
   public ImmutableBlockPosition immutable() {
      return this;
   }

   @Override
   public MutableBlockPosition mutable() {
      return new MutableBlockPosition(this.x, this.y, this.z);
   }

   public static ImmutableBlockPosition of(long packedPos) {
      return new ImmutableBlockPosition((int)(packedPos >> 38), (int)(packedPos << 52 >> 52), (int)(packedPos << 26 >> 38));
   }
}
