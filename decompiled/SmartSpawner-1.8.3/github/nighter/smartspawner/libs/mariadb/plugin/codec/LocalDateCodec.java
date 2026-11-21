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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.EnumSet;

public class LocalDateCodec implements Codec<LocalDate> {
   public static final LocalDateCodec INSTANCE = new LocalDateCodec();
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

   public static int[] parseDate(ReadableByteBuf buf, MutableInt length) {
      int[] datePart = new int[]{0, 0, 0};
      int partIdx = 0;
      int idx = 0;

      while (idx++ < length.get()) {
         byte b = buf.readByte();
         if (b == 45) {
            partIdx++;
         } else {
            datePart[partIdx] = datePart[partIdx] * 10 + b - 48;
         }
      }

      if (datePart[0] == 0 && datePart[1] == 0 && datePart[2] == 0) {
         length.set(-1);
         return null;
      } else {
         return datePart;
      }
   }

   @Override
   public String className() {
      return LocalDate.class.getName();
   }

   @Override
   public boolean canDecode(ColumnDecoder column, Class<?> type) {
      return COMPATIBLE_TYPES.contains(column.getType()) && type.isAssignableFrom(LocalDate.class);
   }

   @Override
   public boolean canEncode(Object value) {
      return value instanceof LocalDate;
   }

   public LocalDate decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      int[] parts;
      switch (column.getType()) {
         case YEAR:
            short y = (short)buf.atoull(length.get());
            if (length.get() == 2 && column.getColumnLength() == 2L) {
               if (y <= 69) {
                  y = (short)(y + 2000);
               } else {
                  y = (short)(y + 1900);
               }
            }

            return LocalDate.of(y, 1, 1);
         case NEWDATE:
         case DATE:
            parts = parseDate(buf, length);
            break;
         case TIMESTAMP:
         case DATETIME:
            parts = LocalDateTimeCodec.parseTextTimestamp(buf, length);
            if (LocalDateTimeCodec.isZeroTimestamp(parts)) {
               length.set(-1);
               return null;
            }
            break;
         case BLOB:
         case TINYBLOB:
         case MEDIUMBLOB:
         case LONGBLOB:
            if (column.isBinary()) {
               buf.skip(length.get());
               throw new SQLDataException(String.format("Data type %s cannot be decoded as Date", column.getType()));
            }
         case VARSTRING:
         case VARCHAR:
         case STRING:
            String val = buf.readString(length.get());
            String[] stDatePart = val.split("[- ]");
            if (stDatePart.length < 3) {
               throw new SQLDataException(String.format("value '%s' (%s) cannot be decoded as Date", val, column.getType()));
            }

            try {
               int year = Integer.parseInt(stDatePart[0]);
               int month = Integer.parseInt(stDatePart[1]);
               int dayOfMonth = Integer.parseInt(stDatePart[2]);
               if (year == 0 && month == 0 && dayOfMonth == 0) {
                  length.set(-1);
                  return null;
               }

               return LocalDate.of(year, month, dayOfMonth);
            } catch (NumberFormatException var13) {
               throw new SQLDataException(String.format("value '%s' (%s) cannot be decoded as Date", val, column.getType()));
            }
         default:
            buf.skip(length.get());
            throw new SQLDataException(String.format("Data type %s cannot be decoded as Date", column.getType()));
      }

      if (parts == null) {
         length.set(-1);
         return null;
      } else {
         return LocalDate.of(parts[0], parts[1], parts[2]);
      }
   }

   public LocalDate decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      int month = 1;
      int dayOfMonth = 1;
      switch (column.getType()) {
         case YEAR:
         case DATE:
            if (length.get() == 0) {
               length.set(-1);
               return null;
            } else {
               int yearx = buf.readUnsignedShort();
               if (column.getColumnLength() == 2L) {
                  if (yearx <= 69) {
                     yearx += 2000;
                  } else {
                     yearx += 1900;
                  }
               }

               if (length.get() >= 4) {
                  month = buf.readByte();
                  dayOfMonth = buf.readByte();
               }

               if (yearx == 0 && month == 0 && dayOfMonth == 0) {
                  length.set(-1);
                  return null;
               }

               return LocalDate.of(yearx, month, dayOfMonth);
            }
         case NEWDATE:
         default:
            buf.skip(length.get());
            throw new SQLDataException(String.format("Data type %s cannot be decoded as Date", column.getType()));
         case TIMESTAMP:
         case DATETIME:
            if (length.get() == 0) {
               length.set(-1);
               return null;
            } else {
               int year = buf.readUnsignedShort();
               int var16 = buf.readByte();
               int var18 = buf.readByte();
               if (length.get() > 4) {
                  buf.skip(length.get() - 4);
               }

               if (year == 0 && var16 == 0 && var18 == 0) {
                  length.set(-1);
                  return null;
               }

               return LocalDate.of(year, var16, var18);
            }
         case BLOB:
         case TINYBLOB:
         case MEDIUMBLOB:
         case LONGBLOB:
            if (column.isBinary()) {
               buf.skip(length.get());
               throw new SQLDataException(String.format("Data type %s cannot be decoded as Date", column.getType()));
            }
         case VARSTRING:
         case VARCHAR:
         case STRING:
            String val = buf.readString(length.get());
            String[] stDatePart = val.split("[- ]");
            if (stDatePart.length < 3) {
               throw new SQLDataException(String.format("value '%s' (%s) cannot be decoded as Date", val, column.getType()));
            } else {
               try {
                  int year = Integer.parseInt(stDatePart[0]);
                  month = Integer.parseInt(stDatePart[1]);
                  dayOfMonth = Integer.parseInt(stDatePart[2]);
                  if (year == 0 && month == 0 && dayOfMonth == 0) {
                     length.set(-1);
                     return null;
                  } else {
                     return LocalDate.of(year, month, dayOfMonth);
                  }
               } catch (NumberFormatException var12) {
                  throw new SQLDataException(String.format("value '%s' (%s) cannot be decoded as Date", val, column.getType()));
               }
            }
      }
   }

   public void encodeText(Writer encoder, Context context, LocalDate val, Calendar cal, Long maxLen) throws IOException {
      encoder.writeByte(39);
      encoder.writeAscii(val.format(DateTimeFormatter.ISO_LOCAL_DATE));
      encoder.writeByte(39);
   }

   public int getApproximateTextProtocolLength(LocalDate value, Long length) {
      return 12;
   }

   public void encodeBinary(Writer encoder, Context context, LocalDate value, Calendar providedCal, Long maxLength) throws IOException {
      encoder.writeByte(7);
      encoder.writeShort((short)value.getYear());
      encoder.writeByte(value.getMonthValue());
      encoder.writeByte(value.getDayOfMonth());
      encoder.writeBytes(new byte[]{0, 0, 0});
   }

   @Override
   public int getBinaryEncodeType() {
      return DataType.DATE.get();
   }
}
