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
import java.sql.SQLDataException;
import java.util.Calendar;
import java.util.EnumSet;

public class BigIntegerCodec implements Codec<BigInteger> {
   public static final BigIntegerCodec INSTANCE = new BigIntegerCodec();
   private static final EnumSet<DataType> COMPATIBLE_TYPES = EnumSet.of(
      DataType.TINYINT,
      DataType.SMALLINT,
      DataType.MEDIUMINT,
      DataType.INTEGER,
      DataType.BIGINT,
      DataType.YEAR,
      DataType.DOUBLE,
      DataType.DECIMAL,
      DataType.OLDDECIMAL,
      DataType.FLOAT,
      DataType.BIT,
      DataType.VARCHAR,
      DataType.VARSTRING,
      DataType.STRING,
      DataType.BLOB,
      DataType.TINYBLOB,
      DataType.MEDIUMBLOB,
      DataType.LONGBLOB
   );

   public static BigInteger parseBigInteger(String str) throws SQLDataException {
      if (str.length() > 1024) {
         throw new SQLDataException("value cannot be decoded as BigInteger: length " + str.length() + " exceeds maximum of " + 1024 + " characters");
      } else {
         return new BigInteger(str);
      }
   }

   @Override
   public String className() {
      return BigInteger.class.getName();
   }

   @Override
   public boolean canDecode(ColumnDecoder column, Class<?> type) {
      return COMPATIBLE_TYPES.contains(column.getType()) && type.isAssignableFrom(BigInteger.class);
   }

   @Override
   public boolean canEncode(Object value) {
      return value instanceof BigInteger;
   }

   public BigInteger decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      switch (column.getType()) {
         case FLOAT:
         case DOUBLE:
         case DECIMAL:
         case OLDDECIMAL:
            return BigDecimalCodec.parseBigDecimal(buf.readAscii(length.get())).toBigInteger();
         case BLOB:
         case TINYBLOB:
         case MEDIUMBLOB:
         case LONGBLOB:
            if (column.isBinary()) {
               buf.skip(length.get());
               throw new SQLDataException(String.format("Data type %s cannot be decoded as BigInteger", column.getType()));
            }
         case VARCHAR:
         case VARSTRING:
         case STRING:
            String str2 = buf.readString(length.get());

            try {
               return BigDecimalCodec.parseBigDecimal(str2).toBigInteger();
            } catch (NumberFormatException var11) {
               throw new SQLDataException(String.format("value '%s' cannot be decoded as BigInteger", str2));
            }
         case BIT:
            long result = 0L;

            for (int i = 0; i < length.get(); i++) {
               byte b = buf.readByte();
               result = (result << 8) + (b & 255);
            }

            return BigInteger.valueOf(result);
         case TINYINT:
         case SMALLINT:
         case MEDIUMINT:
         case INTEGER:
         case BIGINT:
         case YEAR:
            return new BigInteger(buf.readAscii(length.get()));
         default:
            buf.skip(length.get());
            throw new SQLDataException(String.format("Data type %s cannot be decoded as BigInteger", column.getType()));
      }
   }

   public BigInteger decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      switch (column.getType()) {
         case FLOAT:
            return BigDecimal.valueOf((double)buf.readFloat()).toBigInteger();
         case DOUBLE:
            return BigDecimal.valueOf(buf.readDouble()).toBigInteger();
         case DECIMAL:
            return BigDecimalCodec.parseBigDecimal(buf.readAscii(length.get())).toBigInteger();
         case OLDDECIMAL:
         default:
            buf.skip(length.get());
            throw new SQLDataException(String.format("Data type %s cannot be decoded as BigInteger", column.getType()));
         case BLOB:
         case TINYBLOB:
         case MEDIUMBLOB:
         case LONGBLOB:
            if (column.isBinary()) {
               buf.skip(length.get());
               throw new SQLDataException(String.format("Data type %s cannot be decoded as BigInteger", column.getType()));
            }
         case VARCHAR:
         case VARSTRING:
         case STRING:
            String str = buf.readString(length.get());

            try {
               return parseBigInteger(str);
            } catch (NumberFormatException var11) {
               throw new SQLDataException(String.format("value '%s' cannot be decoded as BigInteger", str));
            }
         case BIT:
            long result = 0L;

            for (int i = 0; i < length.get(); i++) {
               byte b = buf.readByte();
               result = (result << 8) + (b & 255);
            }

            return BigInteger.valueOf(result);
         case TINYINT:
            if (!column.isSigned()) {
               return BigInteger.valueOf(buf.readUnsignedByte());
            }

            return BigInteger.valueOf(buf.readByte());
         case SMALLINT:
         case YEAR:
            if (!column.isSigned()) {
               return BigInteger.valueOf(buf.readUnsignedShort());
            }

            return BigInteger.valueOf(buf.readShort());
         case MEDIUMINT:
            if (!column.isSigned()) {
               int val = buf.readUnsignedMedium();
               buf.skip();
               return BigInteger.valueOf(val);
            }

            return BigInteger.valueOf(buf.readInt());
         case INTEGER:
            if (!column.isSigned()) {
               return BigInteger.valueOf(buf.readUnsignedInt());
            }

            return BigInteger.valueOf(buf.readInt());
         case BIGINT:
            if (column.isSigned()) {
               return BigInteger.valueOf(buf.readLong());
            } else {
               byte[] bb = new byte[8];

               for (int i = 7; i >= 0; i--) {
                  bb[i] = buf.readByte();
               }

               return new BigInteger(1, bb);
            }
      }
   }

   public void encodeText(Writer encoder, Context context, BigInteger value, Calendar cal, Long length) throws IOException {
      encoder.writeAscii(value.toString());
   }

   public int getApproximateTextProtocolLength(BigInteger value, Long length) {
      return value.toString().length();
   }

   public void encodeBinary(Writer encoder, Context context, BigInteger value, Calendar cal, Long maxLength) throws IOException {
      String asciiFormat = value.toString();
      encoder.writeLength(asciiFormat.length());
      encoder.writeAscii(asciiFormat);
   }

   @Override
   public int getBinaryEncodeType() {
      return DataType.DECIMAL.get();
   }
}
