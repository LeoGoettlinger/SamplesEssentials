package github.nighter.smartspawner.libs.mariadb.plugin.codec;

import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.socket.Writer;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.plugin.Codec;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLDataException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Locale;

public class DateCodec implements Codec<Date> {
   public static final DateCodec INSTANCE = new DateCodec();
   private static final EnumSet<DataType> COMPATIBLE_TYPES = EnumSet.of(
      DataType.DATE,
      DataType.NEWDATE,
      DataType.DATETIME,
      DataType.TIMESTAMP,
      DataType.YEAR,
      DataType.VARSTRING,
      DataType.VARCHAR,
      DataType.STRING,
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
      return value instanceof Date;
   }

   public Date decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      return column.decodeDateText(buf, length, cal, context);
   }

   public Date decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      return column.decodeDateBinary(buf, length, cal, context);
   }

   public void encodeText(Writer encoder, Context context, Date val, Calendar providedCal, Long maxLen) throws IOException {
      Calendar cal = providedCal == null ? context.getDefaultCalendar() : providedCal;
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
      sdf.setTimeZone(cal.getTimeZone());
      String dateString = sdf.format(val);
      encoder.writeByte(39);
      encoder.writeAscii(dateString);
      encoder.writeByte(39);
   }

   public int getApproximateTextProtocolLength(Date value, Long length) {
      return 12;
   }

   public void encodeBinary(Writer encoder, Context context, Date value, Calendar providedCal, Long maxLength) throws IOException {
      if (providedCal == null) {
         Calendar cal = Calendar.getInstance();
         cal.clear();
         cal.setTimeInMillis(value.getTime());
         encoder.writeByte(4);
         encoder.writeShort((short)cal.get(1));
         encoder.writeByte(cal.get(2) + 1 & 0xFF);
         encoder.writeByte(cal.get(5) & 0xFF);
      } else {
         synchronized (providedCal) {
            providedCal.clear();
            providedCal.setTimeInMillis(value.getTime());
            encoder.writeByte(4);
            encoder.writeShort((short)providedCal.get(1));
            encoder.writeByte(providedCal.get(2) + 1 & 0xFF);
            encoder.writeByte(providedCal.get(5) & 0xFF);
         }
      }
   }

   @Override
   public int getBinaryEncodeType() {
      return DataType.DATE.get();
   }
}
