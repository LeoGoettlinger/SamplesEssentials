package github.nighter.smartspawner.libs.mariadb.plugin.codec;

import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.socket.Writer;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.plugin.Codec;
import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.SQLDataException;
import java.util.Calendar;
import java.util.EnumSet;

public class FloatObjectArrayCodec implements Codec<Float[]> {
   public static final FloatObjectArrayCodec INSTANCE = new FloatObjectArrayCodec();
   private static Class<?> floatArrayClass = Array.newInstance(Float.class, 0).getClass();
   private static final EnumSet<DataType> COMPATIBLE_TYPES = EnumSet.of(
      DataType.BLOB, DataType.TINYBLOB, DataType.MEDIUMBLOB, DataType.LONGBLOB, DataType.VARSTRING, DataType.VARCHAR, DataType.STRING
   );
   static final int BYTES_IN_FLOAT = 4;

   @Override
   public String className() {
      return float[].class.getName();
   }

   @Override
   public boolean canDecode(ColumnDecoder column, Class<?> type) {
      return COMPATIBLE_TYPES.contains(column.getType()) && !type.isPrimitive() && type == floatArrayClass && type.isArray();
   }

   @Override
   public boolean canEncode(Object value) {
      return value instanceof Float[];
   }

   public Float[] decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      return toFloatArray(this.getBytes(buf, length, column));
   }

   public Float[] decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      return toFloatArray(this.getBytes(buf, length, column));
   }

   public static byte[] toByteArray(Float[] floatArray) {
      byte[] buf = new byte[floatArray.length * 4];
      int pos = 0;

      for (Float f : floatArray) {
         int value = Float.floatToIntBits(f);
         buf[pos] = (byte)value;
         buf[pos + 1] = (byte)(value >> 8);
         buf[pos + 2] = (byte)(value >> 16);
         buf[pos + 3] = (byte)(value >> 24);
         pos += 4;
      }

      return buf;
   }

   public static Float[] toFloatArray(byte[] byteArray) {
      int len = (int)Math.ceil(byteArray.length / 4.0);
      Float[] res = new Float[len];
      int pos = 0;

      while (pos < len) {
         int value;
         if (pos + 1 <= len) {
            value = (byteArray[pos * 4] & 255)
               + ((byteArray[pos * 4 + 1] & 255) << 8)
               + ((byteArray[pos * 4 + 2] & 255) << 16)
               + ((byteArray[pos * 4 + 3] & 255) << 24);
         } else {
            value = byteArray[pos * 4] & 255;
            if (pos + 1 < byteArray.length) {
               value += (byteArray[pos * 4 + 1] & 255) << 8;
            }

            if (pos + 2 < byteArray.length) {
               value += (byteArray[pos * 4 + 2] & 255) << 16;
            }
         }

         res[pos++] = Float.intBitsToFloat(value);
      }

      return res;
   }

   private byte[] getBytes(ReadableByteBuf buf, MutableInt length, ColumnDecoder column) throws SQLDataException {
      switch (column.getType()) {
         case BLOB:
         case TINYBLOB:
         case MEDIUMBLOB:
         case LONGBLOB:
         case STRING:
         case VARSTRING:
         case VARCHAR:
         case GEOMETRY:
            byte[] arr = new byte[length.get()];
            buf.readBytes(arr);
            return arr;
         default:
            buf.skip(length.get());
            throw new SQLDataException(String.format("Data type %s cannot be decoded as float[]", column.getType()));
      }
   }

   public void encodeText(Writer encoder, Context context, Float[] value, Calendar cal, Long maxLen) throws IOException {
      byte[] encoded = toByteArray(value);
      encoder.writeBytes(ByteArrayCodec.BINARY_PREFIX);
      encoder.writeBytesEscaped(encoded, encoded.length, (context.getServerStatus() & 512) != 0);
      encoder.writeByte(39);
   }

   public int getApproximateTextProtocolLength(Float[] value, Long length) {
      return value.length * 4 + 10;
   }

   public void encodeBinary(Writer encoder, Context context, Float[] value, Calendar cal, Long maxLength) throws IOException {
      byte[] arr = toByteArray(value);
      encoder.writeLength(arr.length);
      encoder.writeBytes(arr);
   }

   @Override
   public int getBinaryEncodeType() {
      return DataType.BLOB.get();
   }
}
