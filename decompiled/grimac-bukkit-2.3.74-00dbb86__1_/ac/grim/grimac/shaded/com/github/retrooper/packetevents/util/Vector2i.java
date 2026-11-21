package ac.grim.grimac.shaded.com.github.retrooper.packetevents.util;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Vector2i {
   private final int x;
   private final int z;

   public Vector2i(int x, int z) {
      this.x = x;
      this.z = z;
   }

   public static Vector2i read(PacketWrapper<?> wrapper) {
      return fromLong(wrapper.readLong());
   }

   public static void write(PacketWrapper<?> wrapper, Vector2i vec) {
      wrapper.writeLong(vec.asLong());
   }

   public static Vector2i fromLong(long l) {
      return new Vector2i((int)l, (int)(l >> 32));
   }

   public long asLong() {
      return this.x & 4294967295L | (this.z & 4294967295L) << 32;
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof Vector2i)) {
         return false;
      } else {
         Vector2i vector2i = (Vector2i)obj;
         return this.x != vector2i.x ? false : this.z == vector2i.z;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.z);
   }

   @Override
   public String toString() {
      return "Vector2i[" + this.x + "," + this.z + "]";
   }
}
