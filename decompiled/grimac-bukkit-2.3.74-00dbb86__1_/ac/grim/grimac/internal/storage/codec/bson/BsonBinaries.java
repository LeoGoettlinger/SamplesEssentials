package ac.grim.grimac.internal.storage.codec.bson;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;
import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.BsonWriter;
import org.bson.types.Binary;

@ApiStatus.Internal
public final class BsonBinaries {
   private BsonBinaries() {
   }

   @NotNull
   public static BsonBinary uuidBinary(@NotNull UUID uuid) {
      byte[] bytes = new byte[16];
      ByteBuffer buf = ByteBuffer.wrap(bytes);
      buf.putLong(uuid.getMostSignificantBits());
      buf.putLong(uuid.getLeastSignificantBits());
      return new BsonBinary(BsonBinarySubType.UUID_STANDARD, bytes);
   }

   @Nullable
   public static UUID toUuid(@Nullable Object value) {
      if (value == null) {
         return null;
      } else if (value instanceof UUID u) {
         return u;
      } else if (value instanceof Binary b) {
         return decode(b.getData());
      } else if (value instanceof BsonBinary b) {
         return decode(b.getData());
      } else if (value instanceof byte[] bytes) {
         return decode(bytes);
      } else {
         throw new IllegalArgumentException("not a UUID-shaped BSON value: " + value.getClass().getName());
      }
   }

   @NotNull
   private static UUID decode(@NotNull byte[] bytes) {
      if (bytes.length != 16) {
         throw new IllegalArgumentException("expected 16-byte UUID binary, got " + bytes.length);
      } else {
         ByteBuffer buf = ByteBuffer.wrap(bytes);
         return new UUID(buf.getLong(), buf.getLong());
      }
   }

   public static void writeFloatArray(@NotNull String name, @NotNull float[] values, @NotNull BsonWriter w) {
      ByteBuffer buf = ByteBuffer.allocate(values.length * 4).order(ByteOrder.LITTLE_ENDIAN);

      for (float v : values) {
         buf.putFloat(v);
      }

      w.writeBinaryData(name, new BsonBinary(BsonBinarySubType.BINARY, buf.array()));
   }
}
