package github.nighter.smartspawner.libs.mariadb.client.column;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.message.server.ColumnDefinitionPacket;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.SQLDataException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class UnsignedBigIntColumn extends ColumnDefinitionPacket implements ColumnDecoder {
   public UnsignedBigIntColumn(
      ReadableByteBuf buf, int charset, long length, DataType dataType, byte decimals, int flags, int[] stringPos, byte[] extTypeName, byte[] extTypeFormat
   ) {
      super(buf, charset, length, dataType, decimals, flags, stringPos, extTypeName, extTypeFormat, false);
   }

   protected UnsignedBigIntColumn(UnsignedBigIntColumn prev) {
      super(prev, true);
   }

   public UnsignedBigIntColumn useAliasAsName() {
      return new UnsignedBigIntColumn(this);
   }

   @Override
   public String defaultClassname(Configuration conf) {
      return BigInteger.class.getName();
   }

   @Override
   public int getColumnType(Configuration conf) {
      return -5;
   }

   @Override
   public String getColumnTypeName(Configuration conf) {
      return "BIGINT UNSIGNED";
   }

   @Override
   public Object getDefaultText(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      return new BigInteger(buf.readAscii(length.get()));
   }

   @Override
   public Object getDefaultBinary(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      byte[] bb = new byte[8];

      for (int i = 7; i >= 0; i--) {
         bb[i] = buf.readByte();
      }

      return new BigInteger(1, bb);
   }

   @Override
   public boolean decodeBooleanText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      String s = buf.readAscii(length.get());
      return !"0".equals(s);
   }

   @Override
   public boolean decodeBooleanBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return buf.readLong() != 0L;
   }

   @Override
   public byte decodeByteText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      long result = buf.atoull(length.get());
      if ((byte)result == result && result >= 0L) {
         return (byte)result;
      } else {
         throw new SQLDataException("byte overflow");
      }
   }

   @Override
   public byte decodeByteBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      byte[] bb = new byte[8];

      for (int i = 7; i >= 0; i--) {
         bb[i] = buf.readByte();
      }

      BigInteger val = new BigInteger(1, bb);

      try {
         return val.byteValueExact();
      } catch (ArithmeticException | NumberFormatException var6) {
         throw new SQLDataException(String.format("value '%s' (%s) cannot be decoded as Byte", val, this.dataType));
      }
   }

   @Override
   public String decodeStringText(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      return buf.readString(length.get());
   }

   @Override
   public String decodeStringBinary(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      byte[] bb = new byte[8];

      for (int i = 7; i >= 0; i--) {
         bb[i] = buf.readByte();
      }

      return new BigInteger(1, bb).toString();
   }

   @Override
   public short decodeShortText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      long result = buf.atoull(length.get());
      if ((short)result == result && result >= 0L) {
         return (short)result;
      } else {
         throw new SQLDataException("Short overflow");
      }
   }

   @Override
   public short decodeShortBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      long result = buf.readLong();
      if ((short)result == result && result >= 0L) {
         return (short)result;
      } else {
         throw new SQLDataException("Short overflow");
      }
   }

   @Override
   public int decodeIntText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      long result = buf.atoull(length.get());
      int res = (int)result;
      if (res == result && result >= 0L) {
         return res;
      } else {
         throw new SQLDataException("integer overflow");
      }
   }

   @Override
   public int decodeIntBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      byte[] bb = new byte[8];

      for (int i = 7; i >= 0; i--) {
         bb[i] = buf.readByte();
      }

      BigInteger val = new BigInteger(1, bb);

      try {
         return val.intValueExact();
      } catch (ArithmeticException var6) {
         throw new SQLDataException(String.format("value '%s' cannot be decoded as Integer", val));
      }
   }

   @Override
   public long decodeLongText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      if (length.get() < 10) {
         return buf.atoull(length.get());
      } else {
         BigInteger val = new BigInteger(buf.readAscii(length.get()));

         try {
            return val.longValueExact();
         } catch (ArithmeticException var5) {
            throw new SQLDataException(String.format("value '%s' cannot be decoded as Long", val));
         }
      }
   }

   @Override
   public long decodeLongBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      if ((buf.getByte(buf.pos() + 7) & 128) == 0) {
         return buf.readLong();
      } else {
         byte[] bb = new byte[8];

         for (int i = 7; i >= 0; i--) {
            bb[i] = buf.readByte();
         }

         BigInteger val = new BigInteger(1, bb);

         try {
            return val.longValueExact();
         } catch (ArithmeticException var6) {
            throw new SQLDataException(String.format("value '%s' cannot be decoded as Long", val));
         }
      }
   }

   @Override
   public float decodeFloatText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return Float.parseFloat(buf.readAscii(length.get()));
   }

   @Override
   public float decodeFloatBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      byte[] bb = new byte[8];

      for (int i = 7; i >= 0; i--) {
         bb[i] = buf.readByte();
      }

      return new BigInteger(1, bb).floatValue();
   }

   @Override
   public double decodeDoubleText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return Double.parseDouble(buf.readAscii(length.get()));
   }

   @Override
   public double decodeDoubleBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      byte[] bb = new byte[8];

      for (int i = 7; i >= 0; i--) {
         bb[i] = buf.readByte();
      }

      return new BigInteger(1, bb).doubleValue();
   }

   @Override
   public Date decodeDateText(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException(String.format("Data type %s cannot be decoded as Date", this.dataType));
   }

   @Override
   public Date decodeDateBinary(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException(String.format("Data type %s cannot be decoded as Date", this.dataType));
   }

   @Override
   public Time decodeTimeText(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException(String.format("Data type %s cannot be decoded as Time", this.dataType));
   }

   @Override
   public Time decodeTimeBinary(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException(String.format("Data type %s cannot be decoded as Time", this.dataType));
   }

   @Override
   public Timestamp decodeTimestampText(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException(String.format("Data type %s cannot be decoded as Timestamp", this.dataType));
   }

   @Override
   public Timestamp decodeTimestampBinary(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException(String.format("Data type %s cannot be decoded as Timestamp", this.dataType));
   }
}
