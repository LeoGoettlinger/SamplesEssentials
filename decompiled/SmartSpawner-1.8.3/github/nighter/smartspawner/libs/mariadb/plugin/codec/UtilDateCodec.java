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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;

public class UtilDateCodec implements Codec<Date> {
   public static final UtilDateCodec INSTANCE = new UtilDateCodec();
   protected static final EnumSet<DataType> COMPATIBLE_TYPES = EnumSet.of(
      DataType.DATE,
      DataType.NEWDATE,
      DataType.DATETIME,
      DataType.TIMESTAMP,
      DataType.YEAR,
      DataType.VARSTRING,
      DataType.VARCHAR,
      DataType.STRING,
      DataType.TIME,
      DataType.BLOB,
      DataType.TINYBLOB,
      DataType.MEDIUMBLOB,
      DataType.LONGBLOB
   );

   @Override
   public String className() {
      return Date.class.getName();
   }

   @Override
   public boolean canDecode(ColumnDecoder column, Class<?> type) {
      return COMPATIBLE_TYPES.contains(column.getType()) && type.isAssignableFrom(Date.class);
   }

   @Override
   public boolean canEncode(Object value) {
      return Date.class.equals(value.getClass());
   }

   protected static int getMicroseconds(Date val) {
      return val instanceof Timestamp ? ((Timestamp)val).getNanos() / 1000 : (int)(val.getTime() % 1000L * 1000L);
   }

   public Date decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      Timestamp ts = column.decodeTimestampText(buf, length, cal, context);
      return ts == null ? null : new Date(ts.getTime());
   }

   public Date decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      Timestamp ts = column.decodeTimestampBinary(buf, length, cal, context);
      return ts == null ? null : new Date(ts.getTime());
   }

   protected static void encodeTextDate(Writer encoder, Context context, Date val, Calendar providedCal, Long maxLen) throws IOException {
      Calendar cal = providedCal == null ? context.getDefaultCalendar() : providedCal;
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
      sdf.setTimeZone(cal.getTimeZone());
      String dateString = sdf.format(val);
      encoder.writeByte(39);
      encoder.writeAscii(dateString);
      int microseconds = getMicroseconds(val);
      if (microseconds > 0) {
         if (microseconds % 1000 == 0) {
            encoder.writeAscii("." + Integer.toString(microseconds / 1000 + 1000).substring(1));
         } else {
            encoder.writeAscii("." + Integer.toString(microseconds + 1000000).substring(1));
         }
      }

      encoder.writeByte(39);
   }

   protected static void encodeBinaryDate(Writer encoder, Context context, Date value, Calendar providedCal, Long maxLength) throws IOException {
      long timeInMillis = value.getTime();
      int microseconds = getMicroseconds(value);
      if (providedCal == null) {
         Calendar cal = context.getDefaultCalendar();
         cal.clear();
         cal.setTimeInMillis(timeInMillis);
         if (microseconds == 0) {
            encoder.writeByte(7);
            encoder.writeShort((short)cal.get(1));
            encoder.writeByte(cal.get(2) + 1);
            encoder.writeByte(cal.get(5));
            encoder.writeByte(cal.get(11));
            encoder.writeByte(cal.get(12));
            encoder.writeByte(cal.get(13));
         } else {
            encoder.writeByte(11);
            encoder.writeShort((short)cal.get(1));
            encoder.writeByte(cal.get(2) + 1);
            encoder.writeByte(cal.get(5));
            encoder.writeByte(cal.get(11));
            encoder.writeByte(cal.get(12));
            encoder.writeByte(cal.get(13));
            encoder.writeInt(microseconds);
         }
      } else {
         synchronized (providedCal) {
            providedCal.clear();
            providedCal.setTimeInMillis(timeInMillis);
            if (microseconds == 0) {
               encoder.writeByte(7);
               encoder.writeShort((short)providedCal.get(1));
               encoder.writeByte(providedCal.get(2) + 1);
               encoder.writeByte(providedCal.get(5));
               encoder.writeByte(providedCal.get(11));
               encoder.writeByte(providedCal.get(12));
               encoder.writeByte(providedCal.get(13));
            } else {
               encoder.writeByte(11);
               encoder.writeShort((short)providedCal.get(1));
               encoder.writeByte(providedCal.get(2) + 1);
               encoder.writeByte(providedCal.get(5));
               encoder.writeByte(providedCal.get(11));
               encoder.writeByte(providedCal.get(12));
               encoder.writeByte(providedCal.get(13));
               encoder.writeInt(microseconds);
            }
         }
      }
   }

   public void encodeText(Writer encoder, Context context, Date val, Calendar providedCal, Long maxLen) throws IOException {
      encodeTextDate(encoder, context, val, providedCal, maxLen);
   }

   public int getApproximateTextProtocolLength(Date value, Long length) {
      return getMicroseconds(value) > 0 ? 28 : 21;
   }

   public void encodeBinary(Writer encoder, Context context, Date value, Calendar providedCal, Long maxLength) throws IOException {
      encodeBinaryDate(encoder, context, value, providedCal, maxLength);
   }

   @Override
   public int getBinaryEncodeType() {
      return DataType.DATETIME.get();
   }
}
