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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.EnumSet;

public class InstantCodec implements Codec<Instant> {
   public static final InstantCodec INSTANCE = new InstantCodec();
   private static final EnumSet<DataType> COMPATIBLE_TYPES = EnumSet.of(
      DataType.DATETIME,
      DataType.DATE,
      DataType.YEAR,
      DataType.TIMESTAMP,
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
      return Instant.class.getName();
   }

   @Override
   public boolean canDecode(ColumnDecoder column, Class<?> type) {
      return COMPATIBLE_TYPES.contains(column.getType()) && type.isAssignableFrom(Instant.class);
   }

   @Override
   public boolean canEncode(Object value) {
      return value instanceof Instant;
   }

   public Instant decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar calParam, Context context) throws SQLDataException {
      LocalDateTime localDateTime = LocalDateTimeCodec.INSTANCE.decodeText(buf, length, column, calParam, context);
      return localDateTime == null ? null : localDateTime.atZone(ZoneId.systemDefault()).toInstant();
   }

   public Instant decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar calParam, Context context) throws SQLDataException {
      LocalDateTime localDateTime = LocalDateTimeCodec.INSTANCE.decodeBinary(buf, length, column, calParam, context);
      return localDateTime == null ? null : localDateTime.atZone(ZoneId.systemDefault()).toInstant();
   }

   public void encodeText(Writer encoder, Context context, Instant val, Calendar calParam, Long maxLen) throws IOException {
      encoder.writeByte(39);
      if (calParam == null && "UTC".equals(ZoneId.systemDefault().getId())) {
         encoder.writeAscii(val.toString().replace('T', ' '));
         encoder.pos(encoder.pos() - 1);
      } else {
         ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(val, ZoneId.systemDefault());
         if (calParam != null) {
            zonedDateTime = zonedDateTime.withZoneSameInstant(calParam.getTimeZone().toZoneId());
         }

         encoder.writeAscii(zonedDateTime.format(val.getNano() != 0 ? LocalDateTimeCodec.TIMESTAMP_FORMAT : LocalDateTimeCodec.TIMESTAMP_FORMAT_NO_FRACTIONAL));
      }

      encoder.writeByte(39);
   }

   public int getApproximateTextProtocolLength(Instant value, Long length) {
      return value.getNano() != 0 ? 28 : 21;
   }

   public void encodeBinary(Writer encoder, Context context, Instant value, Calendar calParam, Long maxLength) throws IOException {
      ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(value, ZoneId.systemDefault());
      if (calParam != null) {
         zonedDateTime = zonedDateTime.withZoneSameInstant(calParam.getTimeZone().toZoneId());
      }

      int nano = zonedDateTime.getNano();
      if (nano > 0) {
         encoder.writeByte(11);
         encoder.writeShort((short)zonedDateTime.getYear());
         encoder.writeByte(zonedDateTime.getMonth().getValue());
         encoder.writeByte(zonedDateTime.getDayOfMonth());
         encoder.writeByte(zonedDateTime.getHour());
         encoder.writeByte(zonedDateTime.getMinute());
         encoder.writeByte(zonedDateTime.getSecond());
         encoder.writeInt(nano / 1000);
      } else {
         encoder.writeByte(7);
         encoder.writeShort((short)zonedDateTime.getYear());
         encoder.writeByte(zonedDateTime.getMonthValue());
         encoder.writeByte(zonedDateTime.getDayOfMonth());
         encoder.writeByte(zonedDateTime.getHour());
         encoder.writeByte(zonedDateTime.getMinute());
         encoder.writeByte(zonedDateTime.getSecond());
      }
   }

   @Override
   public int getBinaryEncodeType() {
      return DataType.DATETIME.get();
   }
}
