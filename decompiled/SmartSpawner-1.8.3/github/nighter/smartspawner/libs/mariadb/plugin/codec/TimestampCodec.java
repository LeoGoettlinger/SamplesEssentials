package github.nighter.smartspawner.libs.mariadb.plugin.codec;

import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.socket.Writer;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.plugin.Codec;
import java.io.IOException;
import java.sql.SQLDataException;
import java.sql.Timestamp;
import java.util.Calendar;

public class TimestampCodec implements Codec<Timestamp> {
   public static final TimestampCodec INSTANCE = new TimestampCodec();

   @Override
   public String className() {
      return Timestamp.class.getName();
   }

   @Override
   public boolean canDecode(ColumnDecoder column, Class<?> type) {
      return UtilDateCodec.COMPATIBLE_TYPES.contains(column.getType()) && type.isAssignableFrom(Timestamp.class);
   }

   @Override
   public boolean canEncode(Object value) {
      return value instanceof Timestamp;
   }

   public Timestamp decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      return column.decodeTimestampText(buf, length, cal, context);
   }

   public Timestamp decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      return column.decodeTimestampBinary(buf, length, cal, context);
   }

   public void encodeText(Writer encoder, Context context, Timestamp val, Calendar providedCal, Long maxLen) throws IOException {
      UtilDateCodec.encodeTextDate(encoder, context, val, providedCal, maxLen);
   }

   public int getApproximateTextProtocolLength(Timestamp value, Long length) {
      return UtilDateCodec.getMicroseconds(value) > 0 ? 28 : 21;
   }

   public void encodeBinary(Writer encoder, Context context, Timestamp value, Calendar providedCal, Long maxLength) throws IOException {
      UtilDateCodec.encodeBinaryDate(encoder, context, value, providedCal, maxLength);
   }

   @Override
   public int getBinaryEncodeType() {
      return UtilDateCodec.INSTANCE.getBinaryEncodeType();
   }
}
