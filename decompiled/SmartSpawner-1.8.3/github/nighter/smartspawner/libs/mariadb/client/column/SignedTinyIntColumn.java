package github.nighter.smartspawner.libs.mariadb.client.column;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.message.server.ColumnDefinitionPacket;
import java.sql.Date;
import java.sql.SQLDataException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class SignedTinyIntColumn extends ColumnDefinitionPacket implements ColumnDecoder {
   public SignedTinyIntColumn(
      ReadableByteBuf buf, int charset, long length, DataType dataType, byte decimals, int flags, int[] stringPos, byte[] extTypeName, byte[] extTypeFormat
   ) {
      super(buf, charset, length, dataType, decimals, flags, stringPos, extTypeName, extTypeFormat, false);
   }

   protected SignedTinyIntColumn(SignedTinyIntColumn prev) {
      super(prev, true);
   }

   @Override
   public int getPrecision() {
      return Math.min(3, (int)this.columnLength);
   }

   public SignedTinyIntColumn useAliasAsName() {
      return new SignedTinyIntColumn(this);
   }

   @Override
   public String defaultClassname(Configuration conf) {
      return conf.tinyInt1isBit() && this.columnLength == 1L ? Boolean.class.getName() : Integer.class.getName();
   }

   @Override
   public int getColumnType(Configuration conf) {
      if (conf.tinyInt1isBit() && this.columnLength == 1L) {
         return conf.transformedBitIsBoolean() ? 16 : -7;
      } else {
         return this.isSigned() ? -6 : 5;
      }
   }

   @Override
   public String getColumnTypeName(Configuration conf) {
      if (conf.tinyInt1isBit() && this.columnLength == 1L) {
         return conf.transformedBitIsBoolean() ? "BOOLEAN" : "BIT";
      } else {
         return this.isSigned() ? "TINYINT" : "TINYINT UNSIGNED";
      }
   }

   @Override
   public Object getDefaultText(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      return context.getConf().tinyInt1isBit() && this.columnLength == 1L ? this.decodeBooleanText(buf, length) : (int)buf.atoll(length.get());
   }

   @Override
   public Object getDefaultBinary(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      if (context.getConf().tinyInt1isBit() && this.columnLength == 1L) {
         return this.decodeBooleanBinary(buf, length);
      } else {
         return this.isSigned() ? Integer.valueOf(buf.readByte()) : Integer.valueOf(buf.readUnsignedByte());
      }
   }

   @Override
   public boolean decodeBooleanText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      String s = buf.readAscii(length.get());
      return !"0".equals(s);
   }

   @Override
   public boolean decodeBooleanBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return buf.readByte() != 0;
   }

   @Override
   public byte decodeByteText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      long result = buf.atoll(length.get());
      if ((byte)result != result) {
         throw new SQLDataException("byte overflow");
      } else {
         return (byte)result;
      }
   }

   @Override
   public byte decodeByteBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      if (this.isSigned()) {
         return buf.readByte();
      } else {
         long result = buf.readUnsignedByte();
         if ((byte)result != result) {
            throw new SQLDataException("byte overflow");
         } else {
            return (byte)result;
         }
      }
   }

   @Override
   public String decodeStringText(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      return buf.readString(length.get());
   }

   @Override
   public String decodeStringBinary(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      return !this.isSigned() ? String.valueOf(buf.readUnsignedByte()) : String.valueOf(buf.readByte());
   }

   @Override
   public short decodeShortText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return (short)buf.atoll(length.get());
   }

   @Override
   public short decodeShortBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return this.isSigned() ? buf.readByte() : buf.readUnsignedByte();
   }

   @Override
   public int decodeIntText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return (int)buf.atoll(length.get());
   }

   @Override
   public int decodeIntBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return this.isSigned() ? buf.readByte() : buf.readUnsignedByte();
   }

   @Override
   public long decodeLongText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return buf.atoll(length.get());
   }

   @Override
   public long decodeLongBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return !this.isSigned() ? buf.readUnsignedByte() : buf.readByte();
   }

   @Override
   public float decodeFloatText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return Float.parseFloat(buf.readAscii(length.get()));
   }

   @Override
   public float decodeFloatBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return !this.isSigned() ? buf.readUnsignedByte() : buf.readByte();
   }

   @Override
   public double decodeDoubleText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return Double.parseDouble(buf.readAscii(length.get()));
   }

   @Override
   public double decodeDoubleBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return !this.isSigned() ? buf.readUnsignedByte() : buf.readByte();
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
