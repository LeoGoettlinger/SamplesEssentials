package ac.grim.grimac.internal.storage.codec.bson;

import ac.grim.grimac.internal.storage.codec.MethodHandleCodec;
import ac.grim.grimac.internal.storage.codec.TypeTag;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import org.bson.BsonBinary;
import org.bson.types.Binary;

@ApiStatus.Internal
public final class BsonDecodeHelper {
   private BsonDecodeHelper() {
   }

   @Nullable
   public static Object decodeField(@NotNull MethodHandleCodec<?> codec, int i, @Nullable Object raw) {
      TypeTag tag = codec.typeTagAt(i);
      if (raw == null) {
         return codec.requireFieldOrDefault(i, null);
      } else {
         return switch (tag) {
            case INT -> toInt(raw);
            case LONG -> raw instanceof Date d ? d.getTime() : toLong(raw);
            case DOUBLE -> toDouble(raw);
            case FLOAT -> (float)toDouble(raw);
            case BOOLEAN -> raw;
            case STRING -> raw.toString();
            case BYTES -> bytesFrom(raw);
            case UUID -> BsonBinaries.toUuid(raw);
            case ENUM -> codec.enumFromOrdinal(i, toInt(raw));
            case FLOAT_ARRAY -> floatArrayFrom(raw);
            case NESTED_SEALED -> raw;
         };
      }
   }

   private static int toInt(@NotNull Object o) {
      if (o instanceof Number n) {
         return n.intValue();
      } else if (o instanceof Boolean b) {
         return b ? 1 : 0;
      } else {
         throw new IllegalArgumentException("expected int-like BSON value, got " + o.getClass().getName());
      }
   }

   private static long toLong(@NotNull Object o) {
      if (o instanceof Number n) {
         return n.longValue();
      } else if (o instanceof Date d) {
         return d.getTime();
      } else {
         throw new IllegalArgumentException("expected long-like BSON value, got " + o.getClass().getName());
      }
   }

   private static double toDouble(@NotNull Object o) {
      if (o instanceof Number n) {
         return n.doubleValue();
      } else {
         throw new IllegalArgumentException("expected double-like BSON value, got " + o.getClass().getName());
      }
   }

   @NotNull
   private static byte[] bytesFrom(@NotNull Object o) {
      if (o instanceof byte[] b) {
         return b;
      } else if (o instanceof Binary b) {
         return b.getData();
      } else if (o instanceof BsonBinary b) {
         return b.getData();
      } else {
         throw new IllegalArgumentException("expected byte[]-like BSON value, got " + o.getClass().getName());
      }
   }

   @NotNull
   private static float[] floatArrayFrom(@NotNull Object o) {
      if (o instanceof float[] fa) {
         return fa;
      } else {
         byte[] bytes;
         if (o instanceof byte[] b) {
            bytes = b;
         } else if (o instanceof Binary b) {
            bytes = b.getData();
         } else {
            if (!(o instanceof BsonBinary b)) {
               throw new IllegalArgumentException("expected float[]-shaped BSON value, got " + o.getClass().getName());
            }

            bytes = b.getData();
         }

         if ((bytes.length & 3) != 0) {
            throw new IllegalArgumentException("float[] binary length must be a multiple of 4, got " + bytes.length);
         } else {
            float[] out = new float[bytes.length / 4];
            ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < out.length; i++) {
               out[i] = buf.getFloat();
            }

            return out;
         }
      }
   }
}
