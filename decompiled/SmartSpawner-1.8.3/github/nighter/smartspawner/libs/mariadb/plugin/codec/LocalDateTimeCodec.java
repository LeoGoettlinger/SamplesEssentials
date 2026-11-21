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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Locale;
import java.util.TimeZone;

public class LocalDateTimeCodec implements Codec<LocalDateTime> {
   public static final LocalDateTimeCodec INSTANCE = new LocalDateTimeCodec();
   public static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.ROOT);
   public static final DateTimeFormatter TIMESTAMP_FORMAT_NO_FRACTIONAL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
   public static final DateTimeFormatter MARIADB_LOCAL_DATE_TIME = new DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .append(DateTimeFormatter.ISO_LOCAL_DATE)
      .appendLiteral(' ')
      .append(DateTimeFormatter.ISO_LOCAL_TIME)
      .toFormatter();
   private static final EnumSet<DataType> COMPATIBLE_TYPES = EnumSet.of(
      DataType.DATETIME,
      DataType.TIMESTAMP,
      DataType.VARSTRING,
      DataType.VARCHAR,
      DataType.STRING,
      DataType.TIME,
      DataType.YEAR,
      DataType.DATE,
      DataType.BLOB,
      DataType.TINYBLOB,
      DataType.MEDIUMBLOB,
      DataType.LONGBLOB
   );

   public static int[] parseTextTimestamp(ReadableByteBuf buf, MutableInt length) {
      int pos = buf.pos();
      int nanoBegin = -1;
      int[] parts = new int[7];
      int partIdx = 0;

      for (int begin = 0; begin < length.get(); begin++) {
         byte b = buf.readByte();
         if (isDelimiter(b)) {
            partIdx++;
            if (b == 46) {
               nanoBegin = begin;
            }
         } else {
            if (!isDigit(b)) {
               buf.pos(pos);
               throw new IllegalArgumentException("Invalid character in timestamp");
            }

            parts[partIdx] = parts[partIdx] * 10 + (b - 48);
         }
      }

      if (nanoBegin > 0) {
         adjustNanoPrecision(parts, length.get() - nanoBegin - 1);
      }

      if (partIdx < 2) {
         buf.pos(pos);
         throw new IllegalArgumentException("Wrong timestamp format");
      } else {
         return parts;
      }
   }

   private static boolean isDelimiter(byte b) {
      return b == 45 || b == 32 || b == 58 || b == 46;
   }

   private static boolean isDigit(byte b) {
      return b >= 48 && b <= 57;
   }

   private static void adjustNanoPrecision(int[] parts, int nanoLength) {
      for (int i = 0; i < 9 - nanoLength; i++) {
         parts[6] *= 10;
      }
   }

   public static boolean isZeroTimestamp(int[] parts) {
      for (int part : parts) {
         if (part != 0) {
            return false;
         }
      }

      return true;
   }

   @Override
   public String className() {
      return LocalDateTime.class.getName();
   }

   @Override
   public boolean canDecode(ColumnDecoder column, Class<?> type) {
      return COMPATIBLE_TYPES.contains(column.getType()) && type.isAssignableFrom(LocalDateTime.class);
   }

   @Override
   public boolean canEncode(Object value) {
      return value instanceof LocalDateTime;
   }

   public LocalDateTime decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      switch (column.getType()) {
         case TIMESTAMP:
         case DATETIME:
            int[] parts = parseTextTimestamp(buf, length);
            if (isZeroTimestamp(parts)) {
               length.set(-1);
               return null;
            }

            return fromConnectionTimeZone(LocalDateTime.of(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]), cal, context);
         default:
            ZonedDateTime zdt = ZonedDateTimeCodec.INSTANCE.decodeText(buf, length, column, cal, context);
            return zdt == null ? null : zdt.toLocalDateTime();
      }
   }

   public LocalDateTime decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      switch (column.getType()) {
         case TIMESTAMP:
         case DATETIME:
            if (length.get() == 0) {
               length.set(-1);
               return null;
            } else {
               int year = buf.readUnsignedShort();
               int month = buf.readByte();
               int dayOfMonth = buf.readByte();
               int hour = 0;
               int minutes = 0;
               int seconds = 0;
               long microseconds = 0L;
               if (length.get() > 4) {
                  hour = buf.readByte();
                  minutes = buf.readByte();
                  seconds = buf.readByte();
                  if (length.get() > 7) {
                     microseconds = buf.readUnsignedInt();
                  }
               }

               if (year == 0 && month == 0 && dayOfMonth == 0 && hour == 0 && minutes == 0 && seconds == 0) {
                  length.set(-1);
                  return null;
               }

               return fromConnectionTimeZone(LocalDateTime.of(year, month, dayOfMonth, hour, minutes, seconds, (int)(microseconds * 1000L)), cal, context);
            }
         default:
            ZonedDateTime zdt = ZonedDateTimeCodec.INSTANCE.decodeBinary(buf, length, column, cal, context);
            return zdt == null ? null : zdt.toLocalDateTime();
      }
   }

   private static LocalDateTime fromConnectionTimeZone(LocalDateTime ldt, Calendar cal, Context context) {
      return cal == null && context.getConf().preserveInstants() && !context.getConf().pureLocalDateTime()
         ? ldt.atZone(context.getConnectionTimeZone().toZoneId()).withZoneSameInstant(TimeZone.getDefault().toZoneId()).toLocalDateTime()
         : ldt;
   }

   private static LocalDateTime toConnectionTimeZone(LocalDateTime ldt, Calendar cal, Context context) {
      return cal == null && context.getConf().preserveInstants() && !context.getConf().pureLocalDateTime()
         ? ldt.atZone(TimeZone.getDefault().toZoneId()).withZoneSameInstant(context.getConnectionTimeZone().toZoneId()).toLocalDateTime()
         : ldt;
   }

   public void encodeText(Writer encoder, Context context, LocalDateTime value, Calendar cal, Long maxLen) throws IOException {
      LocalDateTime val = toConnectionTimeZone(value, cal, context);
      encoder.writeByte(39);
      encoder.writeAscii(val.format(val.getNano() != 0 ? TIMESTAMP_FORMAT : TIMESTAMP_FORMAT_NO_FRACTIONAL));
      encoder.writeByte(39);
   }

   public int getApproximateTextProtocolLength(LocalDateTime value, Long length) {
      return value.getNano() > 0 ? 28 : 21;
   }

   public void encodeBinary(Writer encoder, Context context, LocalDateTime value, Calendar cal, Long maxLength) throws IOException {
      LocalDateTime val = toConnectionTimeZone(value, cal, context);
      int nano = val.getNano();
      if (nano > 0) {
         encoder.writeByte(11);
         encoder.writeShort((short)val.getYear());
         encoder.writeByte(val.getMonthValue());
         encoder.writeByte(val.getDayOfMonth());
         encoder.writeByte(val.getHour());
         encoder.writeByte(val.getMinute());
         encoder.writeByte(val.getSecond());
         encoder.writeInt(nano / 1000);
      } else {
         encoder.writeByte(7);
         encoder.writeShort((short)val.getYear());
         encoder.writeByte(val.getMonthValue());
         encoder.writeByte(val.getDayOfMonth());
         encoder.writeByte(val.getHour());
         encoder.writeByte(val.getMinute());
         encoder.writeByte(val.getSecond());
      }
   }

   @Override
   public int getBinaryEncodeType() {
      return DataType.DATETIME.get();
   }
}
