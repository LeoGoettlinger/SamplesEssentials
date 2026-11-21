package github.nighter.smartspawner.libs.mariadb.client.column;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.message.server.ColumnDefinitionPacket;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.BigDecimalCodec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.SQLDataException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class BigDecimalColumn extends ColumnDefinitionPacket implements ColumnDecoder {
   public BigDecimalColumn(
      ReadableByteBuf buf, int charset, long length, DataType dataType, byte decimals, int flags, int[] stringPos, byte[] extTypeName, byte[] extTypeFormat
   ) {
      super(buf, charset, length, dataType, decimals, flags, stringPos, extTypeName, extTypeFormat, false);
   }

   protected BigDecimalColumn(BigDecimalColumn prev) {
      super(prev, true);
   }

   public BigDecimalColumn useAliasAsName() {
      return new BigDecimalColumn(this);
   }

   @Override
   public String defaultClassname(Configuration conf) {
      return BigDecimal.class.getName();
   }

   @Override
   public int getColumnType(Configuration conf) {
      return 3;
   }

   @Override
   public String getColumnTypeName(Configuration conf) {
      return this.isSigned() ? "DECIMAL" : "DECIMAL UNSIGNED";
   }

   @Override
   public int getPrecision() {
      return this.isSigned() ? (int)(this.columnLength - (this.decimals > 0 ? 2 : 1)) : (int)(this.columnLength - (this.decimals > 0 ? 1 : 0));
   }

   @Override
   public Object getDefaultText(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      return BigDecimalCodec.parseBigDecimal(buf.readAscii(length.get()));
   }

   @Override
   public Object getDefaultBinary(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      return BigDecimalCodec.parseBigDecimal(buf.readAscii(length.get()));
   }

   @Override
   public boolean decodeBooleanText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return BigDecimalCodec.parseBigDecimal(buf.readAscii(length.get())).intValue() != 0;
   }

   @Override
   public boolean decodeBooleanBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return this.decodeBooleanText(buf, length);
   }

   @Override
   public byte decodeByteText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      String str = buf.readString(length.get());

      try {
         return BigDecimalCodec.parseBigDecimal(str).setScale(0, RoundingMode.DOWN).byteValueExact();
      } catch (ArithmeticException | NumberFormatException var5) {
         throw new SQLDataException(String.format("value '%s' (%s) cannot be decoded as Byte", str, this.dataType));
      }
   }

   @Override
   public byte decodeByteBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return this.decodeByteText(buf, length);
   }

   @Override
   public String decodeStringText(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      return buf.readString(length.get());
   }

   @Override
   public String decodeStringBinary(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      return buf.readString(length.get());
   }

   @Override
   public short decodeShortText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      String str = buf.readString(length.get());

      long result;
      try {
         result = BigDecimalCodec.parseBigDecimal(str).setScale(0, RoundingMode.DOWN).longValueExact();
      } catch (ArithmeticException | NumberFormatException var7) {
         throw new SQLDataException(String.format("value '%s' cannot be decoded as Short", str));
      }

      if ((short)result == result && (result >= 0L || this.isSigned())) {
         return (short)result;
      } else {
         throw new SQLDataException("Short overflow");
      }
   }

   @Override
   public short decodeShortBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return this.decodeShortText(buf, length);
   }

   @Override
   public int decodeIntText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      String str = buf.readString(length.get());

      long result;
      try {
         result = BigDecimalCodec.parseBigDecimal(str).setScale(0, RoundingMode.DOWN).longValueExact();
      } catch (ArithmeticException | NumberFormatException var7) {
         throw new SQLDataException(String.format("value '%s' cannot be decoded as Integer", str));
      }

      int res = (int)result;
      if (res == result && (result >= 0L || this.isSigned())) {
         return res;
      } else {
         throw new SQLDataException("integer overflow");
      }
   }

   @Override
   public int decodeIntBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return this.decodeIntText(buf, length);
   }

   @Override
   public long decodeLongText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      String str2 = buf.readAscii(length.get());

      try {
         return BigDecimalCodec.parseBigDecimal(str2).setScale(0, RoundingMode.DOWN).longValueExact();
      } catch (ArithmeticException | NumberFormatException var5) {
         throw new SQLDataException(String.format("value '%s' cannot be decoded as Long", str2));
      }
   }

   @Override
   public long decodeLongBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      String str = buf.readString(length.get());

      try {
         return BigDecimalCodec.parseBigDecimal(str).setScale(0, RoundingMode.DOWN).longValueExact();
      } catch (ArithmeticException | NumberFormatException var5) {
         throw new SQLDataException(String.format("value '%s' cannot be decoded as Long", str));
      }
   }

   @Override
   public float decodeFloatText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return Float.parseFloat(buf.readAscii(length.get()));
   }

   @Override
   public float decodeFloatBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return BigDecimalCodec.parseBigDecimal(buf.readAscii(length.get())).floatValue();
   }

   @Override
   public double decodeDoubleText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return Double.parseDouble(buf.readAscii(length.get()));
   }

   @Override
   public double decodeDoubleBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return BigDecimalCodec.parseBigDecimal(buf.readAscii(length.get())).doubleValue();
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
