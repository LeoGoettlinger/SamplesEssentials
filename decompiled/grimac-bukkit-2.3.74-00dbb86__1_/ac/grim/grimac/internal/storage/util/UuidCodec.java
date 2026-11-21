package ac.grim.grimac.internal.storage.util;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.nio.ByteBuffer;
import java.util.UUID;

@ApiStatus.Internal
public final class UuidCodec {
   private UuidCodec() {
   }

   public static byte[] toBytes(UUID uuid) {
      ByteBuffer buf = ByteBuffer.allocate(16);
      buf.putLong(uuid.getMostSignificantBits());
      buf.putLong(uuid.getLeastSignificantBits());
      return buf.array();
   }

   public static UUID fromBytes(byte[] bytes) {
      if (bytes != null && bytes.length == 16) {
         ByteBuffer buf = ByteBuffer.wrap(bytes);
         return new UUID(buf.getLong(), buf.getLong());
      } else {
         throw new IllegalArgumentException("uuid bytes must be 16 bytes, got " + (bytes == null ? "null" : bytes.length));
      }
   }
}
