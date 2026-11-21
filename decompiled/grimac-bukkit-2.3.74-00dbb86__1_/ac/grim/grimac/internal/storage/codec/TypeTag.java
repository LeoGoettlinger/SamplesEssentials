package ac.grim.grimac.internal.storage.codec;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.UUID;

@ApiStatus.Internal
public enum TypeTag {
   INT,
   LONG,
   DOUBLE,
   FLOAT,
   BOOLEAN,
   STRING,
   BYTES,
   UUID,
   ENUM,
   FLOAT_ARRAY,
   NESTED_SEALED;

   @NotNull
   public static TypeTag of(@NotNull Class<?> type) {
      if (type == int.class || type == Integer.class) {
         return INT;
      } else if (type == long.class || type == Long.class) {
         return LONG;
      } else if (type == double.class || type == Double.class) {
         return DOUBLE;
      } else if (type == float.class || type == Float.class) {
         return FLOAT;
      } else if (type == boolean.class || type == Boolean.class) {
         return BOOLEAN;
      } else if (type == String.class) {
         return STRING;
      } else if (type == byte[].class) {
         return BYTES;
      } else if (type == UUID.class) {
         return UUID;
      } else if (type == float[].class) {
         return FLOAT_ARRAY;
      } else if (type.isEnum()) {
         return ENUM;
      } else if (type.isSealed()) {
         return NESTED_SEALED;
      } else {
         throw new IllegalArgumentException(
            "unsupported persistent field type: " + type.getName() + " (supported: int/long/double/float/boolean/String/byte[]/UUID/enum/float[]/sealed)"
         );
      }
   }
}
