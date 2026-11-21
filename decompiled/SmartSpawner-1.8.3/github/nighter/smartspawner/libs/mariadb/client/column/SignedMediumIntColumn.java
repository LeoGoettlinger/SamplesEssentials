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

public class SignedMediumIntColumn extends ColumnDefinitionPacket implements ColumnDecoder {
   public SignedMediumIntColumn(
      ReadableByteBuf buf, int charset, long length, DataType dataType, byte decimals, int flags, int[] stringPos, byte[] extTypeName, byte[] extTypeFormat
   ) {
      super(buf, charset, length, dataType, decimals, flags, stringPos, extTypeName, extTypeFormat, false);
   }

   protected SignedMediumIntColumn(SignedMediumIntColumn prev) {
      super(prev, true);
   }

   @Override
   public int getPrecision() {
      return Math.min(7, (int)this.columnLength);
   }

   @Override
   public int getDisplaySize() {
      return Math.min(8, (int)this.columnLength);
   }

   public SignedMediumIntColumn useAliasAsName() {
      return new SignedMediumIntColumn(this);
   }

   @Override
   public String defaultClassname(Configuration conf) {
      return Integer.class.getName();
   }

   @Override
   public int getColumnType(Configuration conf) {
      return 4;
   }

   @Override
   public String getColumnTypeName(Configuration conf) {
      return "MEDIUMINT";
   }

   @Override
   public Object getDefaultText(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      return this.decodeIntText(buf, length);
   }

   @Override
   public Object getDefaultBinary(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      return this.decodeIntBinary(buf, length);
   }

   @Override
   public boolean decodeBooleanText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      String s = buf.readAscii(length.get());
      return !"0".equals(s);
   }

   @Override
   public boolean decodeBooleanBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return buf.readInt() != 0;
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
      long result = buf.readMedium();
      buf.skip();
      if ((byte)result != result) {
         throw new SQLDataException("byte overflow");
      } else {
         return (byte)result;
      }
   }

   @Override
   public String decodeStringText(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      return buf.readString(length.get());
   }

   @Override
   public String decodeStringBinary(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      String mediumStr = String.valueOf(this.isSigned() ? buf.readMedium() : buf.readUnsignedMedium());
      buf.skip();
      return mediumStr;
   }

   @Override
   public short decodeShortText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      long result = buf.atoll(length.get());
      if ((short)result != result) {
         throw new SQLDataException("Short overflow");
      } else {
         return (short)result;
      }
   }

   @Override
   public short decodeShortBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      long result = buf.readMedium();
      buf.skip();
      if ((short)result != result) {
         throw new SQLDataException("Short overflow");
      } else {
         return (short)result;
      }
   }

   @Override
   public int decodeIntText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return (int)buf.atoll(length.get());
   }

   @Override
   public int decodeIntBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      int res = buf.readMedium();
      buf.skip();
      return res;
   }

   @Override
   public long decodeLongText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return buf.atoll(length.get());
   }

   @Override
   public long decodeLongBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      long l = buf.readMedium();
      buf.skip();
      return l;
   }

   @Override
   public float decodeFloatText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return Float.parseFloat(buf.readAscii(length.get()));
   }

   @Override
   public float decodeFloatBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      float f = buf.readMedium();
      buf.skip();
      return f;
   }

   @Override
   public double decodeDoubleText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return Double.parseDouble(buf.readAscii(length.get()));
   }

   @Override
   public double decodeDoubleBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      double f = buf.readMedium();
      buf.skip();
      return f;
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
