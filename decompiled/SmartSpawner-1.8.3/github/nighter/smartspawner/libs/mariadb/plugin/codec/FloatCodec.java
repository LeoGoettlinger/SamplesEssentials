package github.nighter.smartspawner.libs.mariadb.plugin.codec;

import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.socket.Writer;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.plugin.Codec;
import java.io.IOException;
import java.sql.SQLDataException;
import java.util.Calendar;
import java.util.EnumSet;

public class FloatCodec implements Codec<Float> {
   public static final FloatCodec INSTANCE = new FloatCodec();
   private static final EnumSet<DataType> COMPATIBLE_TYPES = EnumSet.of(
      DataType.TINYINT,
      DataType.SMALLINT,
      DataType.MEDIUMINT,
      DataType.INTEGER,
      DataType.FLOAT,
      DataType.BIGINT,
      DataType.OLDDECIMAL,
      DataType.DECIMAL,
      DataType.YEAR,
      DataType.DOUBLE,
      DataType.VARCHAR,
      DataType.VARSTRING,
      DataType.STRING,
      DataType.BLOB,
      DataType.TINYBLOB,
      DataType.MEDIUMBLOB,
      DataType.LONGBLOB
   );

   @Override
   public String className() {
      return Float.class.getName();
   }

   @Override
   public boolean canDecode(ColumnDecoder column, Class<?> type) {
      return COMPATIBLE_TYPES.contains(column.getType()) && (type.isPrimitive() && type == float.class || type.isAssignableFrom(Float.class));
   }

   @Override
   public boolean canEncode(Object value) {
      return value instanceof Float;
   }

   public Float decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      return column.decodeFloatText(buf, length);
   }

   public Float decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      return column.decodeFloatBinary(buf, length);
   }

   public void encodeText(Writer encoder, Context context, Float value, Calendar cal, Long maxLen) throws IOException {
      encoder.writeAscii(value.toString());
   }

   public int getApproximateTextProtocolLength(Float value, Long length) {
      return value.toString().length();
   }

   public void encodeBinary(Writer encoder, Context context, Float value, Calendar cal, Long maxLength) throws IOException {
      encoder.writeFloat(value);
   }

   @Override
   public int getBinaryEncodeType() {
      return DataType.FLOAT.get();
   }
}
