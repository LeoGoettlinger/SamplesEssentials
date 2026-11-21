package github.nighter.smartspawner.libs.mariadb.plugin.codec;

import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.socket.Writer;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.plugin.Codec;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.SQLDataException;
import java.util.Calendar;
import java.util.EnumSet;

public class BigDecimalCodec implements Codec<BigDecimal> {
   public static final BigDecimalCodec INSTANCE = new BigDecimalCodec();
   public static final int MAX_BIG_DECIMAL_STRING_LENGTH = 1024;
   private static final EnumSet<DataType> COMPATIBLE_TYPES = EnumSet.of(
      DataType.TINYINT,
      DataType.SMALLINT,
      DataType.MEDIUMINT,
      DataType.INTEGER,
      DataType.FLOAT,
      DataType.DOUBLE,
      DataType.BIGINT,
      DataType.BIT,
      DataType.DECIMAL,
      DataType.OLDDECIMAL,
      DataType.YEAR,
      DataType.VARCHAR,
      DataType.VARSTRING,
      DataType.STRING,
      DataType.BLOB,
      DataType.TINYBLOB,
      DataType.MEDIUMBLOB,
      DataType.LONGBLOB
   );

   public static BigDecimal parseBigDecimal(String str) throws SQLDataException {
      if (str.length() > 1024) {
         throw new SQLDataException("value cannot be decoded as BigDecimal: length " + str.length() + " exceeds maximum of " + 1024 + " characters");
      } else {
         return new BigDecimal(str);
      }
   }

   static BigInteger getBigInteger(ReadableByteBuf buf, ColumnDecoder column) {
      BigInteger val;
      if (column.isSigned()) {
         val = BigInteger.valueOf(buf.readLong());
      } else {
         byte[] bb = new byte[8];

         for (int i = 7; i >= 0; i--) {
            bb[i] = buf.readByte();
         }

         val = new BigInteger(1, bb);
      }

      return val;
   }

   @Override
   public String className() {
      return BigDecimal.class.getName();
   }

   @Override
   public boolean canDecode(ColumnDecoder column, Class<?> type) {
      return COMPATIBLE_TYPES.contains(column.getType()) && type.isAssignableFrom(BigDecimal.class);
   }

   @Override
   public boolean canEncode(Object value) {
      return value instanceof BigDecimal;
   }

   public BigDecimal decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      switch (column.getType()) {
         case TINYINT:
         case SMALLINT:
         case MEDIUMINT:
         case INTEGER:
         case BIGINT:
         case FLOAT:
         case DOUBLE:
         case DECIMAL:
         case OLDDECIMAL:
         case YEAR:
            return parseBigDecimal(buf.readAscii(length.get()));
         case BLOB:
         case TINYBLOB:
         case MEDIUMBLOB:
         case LONGBLOB:
            if (column.isBinary()) {
               buf.skip(length.get());
               throw new SQLDataException(String.format("Data type %s cannot be decoded as BigDecimal", column.getType()));
            }
         case VARCHAR:
         case VARSTRING:
         case STRING:
            String str = buf.readString(length.get());

            try {
               return parseBigDecimal(str);
            } catch (NumberFormatException var11) {
               throw new SQLDataException(String.format("value '%s' cannot be decoded as BigDecimal", str));
            }
         case BIT:
            long result = 0L;

            for (int i = 0; i < length.get(); i++) {
               byte b = buf.readByte();
               result = (result << 8) + (b & 255);
            }

            return BigDecimal.valueOf(result);
         default:
            buf.skip(length.get());
            throw new SQLDataException(String.format("Data type %s cannot be decoded as BigDecimal", column.getType()));
      }
   }

   public BigDecimal decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      switch (column.getType()) {
         case TINYINT:
            if (!column.isSigned()) {
               return BigDecimal.valueOf((long)buf.readUnsignedByte());
            }

            return BigDecimal.valueOf((long)buf.readByte());
         case SMALLINT:
         case YEAR:
            if (!column.isSigned()) {
               return BigDecimal.valueOf((long)buf.readUnsignedShort());
            }

            return BigDecimal.valueOf((long)buf.readShort());
         case MEDIUMINT:
            if (!column.isSigned()) {
               int valx = buf.readUnsignedMedium();
               buf.skip();
               return BigDecimal.valueOf((long)valx);
            }

            return BigDecimal.valueOf((long)buf.readInt());
         case INTEGER:
            if (!column.isSigned()) {
               return BigDecimal.valueOf(buf.readUnsignedInt());
            }

            return BigDecimal.valueOf((long)buf.readInt());
         case BIGINT:
            BigInteger val = getBigInteger(buf, column);
            return new BigDecimal(String.valueOf(val)).setScale(column.getDecimals(), RoundingMode.CEILING);
         case FLOAT:
            return BigDecimal.valueOf((double)buf.readFloat());
         case DOUBLE:
            return BigDecimal.valueOf(buf.readDouble());
         case BLOB:
         case TINYBLOB:
         case MEDIUMBLOB:
         case LONGBLOB:
            if (column.isBinary()) {
               buf.skip(length.get());
               throw new SQLDataException(String.format("Data type %s cannot be decoded as BigDecimal", column.getType()));
            }
         case DECIMAL:
         case OLDDECIMAL:
         case VARCHAR:
         case VARSTRING:
         case STRING:
            String str = buf.readString(length.get());

            try {
               return parseBigDecimal(str);
            } catch (NumberFormatException var11) {
               throw new SQLDataException(String.format("value '%s' cannot be decoded as BigDecimal", str));
            }
         case BIT:
            long result = 0L;

            for (int i = 0; i < length.get(); i++) {
               byte b = buf.readByte();
               result = (result << 8) + (b & 255);
            }

            return BigDecimal.valueOf(result);
         default:
            buf.skip(length.get());
            throw new SQLDataException(String.format("Data type %s cannot be decoded as BigDecimal", column.getType()));
      }
   }

   public void encodeText(Writer encoder, Context context, BigDecimal value, Calendar cal, Long length) throws IOException {
      encoder.writeAscii(value.toPlainString());
   }

   public int getApproximateTextProtocolLength(BigDecimal value, Long length) {
      return value.toPlainString().length();
   }

   public void encodeBinary(Writer encoder, Context context, BigDecimal value, Calendar cal, Long maxLength) throws IOException {
      String asciiFormat = value.toPlainString();
      encoder.writeLength(asciiFormat.length());
      encoder.writeAscii(asciiFormat);
   }

   @Override
   public int getBinaryEncodeType() {
      return DataType.DECIMAL.get();
   }
}
