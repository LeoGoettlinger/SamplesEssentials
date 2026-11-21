package github.nighter.smartspawner.libs.mariadb.client.column;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.message.server.ColumnDefinitionPacket;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.ByteCodec;
import java.sql.Date;
import java.sql.SQLDataException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class BitColumn extends ColumnDefinitionPacket implements ColumnDecoder {
   public BitColumn(
      ReadableByteBuf buf, int charset, long length, DataType dataType, byte decimals, int flags, int[] stringPos, byte[] extTypeName, byte[] extTypeFormat
   ) {
      super(buf, charset, length, dataType, decimals, flags, stringPos, extTypeName, extTypeFormat, false);
   }

   protected BitColumn(BitColumn prev) {
      super(prev, true);
   }

   public BitColumn useAliasAsName() {
      return new BitColumn(this);
   }

   @Override
   public String defaultClassname(Configuration conf) {
      return this.columnLength == 1L && conf.transformedBitIsBoolean() ? Boolean.class.getName() : "byte[]";
   }

   @Override
   public int getColumnType(Configuration conf) {
      return this.columnLength == 1L && conf.transformedBitIsBoolean() ? 16 : -7;
   }

   @Override
   public String getColumnTypeName(Configuration conf) {
      return "BIT";
   }

   @Override
   public int getPrecision() {
      return (int)this.columnLength;
   }

   @Override
   public Object getDefaultText(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      if (this.columnLength == 1L && context.getConf().transformedBitIsBoolean()) {
         return ByteCodec.parseBit(buf, length) != 0L;
      } else {
         byte[] arr = new byte[length.get()];
         buf.readBytes(arr);
         return arr;
      }
   }

   @Override
   public Object getDefaultBinary(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      return this.getDefaultText(buf, length, context);
   }

   @Override
   public boolean decodeBooleanText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return ByteCodec.parseBit(buf, length) != 0L;
   }

   @Override
   public boolean decodeBooleanBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return ByteCodec.parseBit(buf, length) != 0L;
   }

   @Override
   public byte decodeByteText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      byte val = buf.readByte();
      if (length.get() > 1) {
         buf.skip(length.get() - 1);
      }

      return val;
   }

   @Override
   public byte decodeByteBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return this.decodeByteText(buf, length);
   }

   @Override
   public String decodeStringText(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      if (this.columnLength == 1L && context.getConf().transformedBitIsBoolean()) {
         return String.valueOf(ByteCodec.parseBit(buf, length) != 0L);
      } else {
         byte[] bytes = new byte[length.get()];
         buf.readBytes(bytes);
         StringBuilder sb = new StringBuilder(bytes.length * 8 + 3);
         sb.append("b'");
         boolean firstByteNonZero = false;

         for (int i = 0; i < 8 * bytes.length; i++) {
            boolean b = (bytes[i / 8] & 1 << 7 - i % 8) > 0;
            if (b) {
               sb.append('1');
               firstByteNonZero = true;
            } else if (firstByteNonZero) {
               sb.append('0');
            }
         }

         sb.append("'");
         return sb.toString();
      }
   }

   @Override
   public String decodeStringBinary(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      if (this.columnLength == 1L && context.getConf().transformedBitIsBoolean()) {
         return String.valueOf(ByteCodec.parseBit(buf, length) != 0L);
      } else {
         byte[] bytes = new byte[length.get()];
         buf.readBytes(bytes);
         StringBuilder sb = new StringBuilder(bytes.length * 8 + 3);
         sb.append("b'");
         boolean firstByteNonZero = false;

         for (int i = 0; i < 8 * bytes.length; i++) {
            boolean b = (bytes[i / 8] & 1 << 7 - i % 8) > 0;
            if (b) {
               sb.append('1');
               firstByteNonZero = true;
            } else if (firstByteNonZero) {
               sb.append('0');
            }
         }

         sb.append("'");
         return sb.toString();
      }
   }

   @Override
   public short decodeShortText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      long result = 0L;

      for (int i = 0; i < length.get(); i++) {
         byte b = buf.readByte();
         result = (result << 8) + (b & 255);
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
      long result = 0L;

      for (int i = 0; i < length.get(); i++) {
         byte b = buf.readByte();
         result = (result << 8) + (b & 255);
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
      long result = 0L;

      for (int i = 0; i < length.get(); i++) {
         byte b = buf.readByte();
         result = (result << 8) + (b & 255);
      }

      int res = (int)result;
      if (res != result) {
         throw new SQLDataException("integer overflow");
      } else {
         return res;
      }
   }

   @Override
   public long decodeLongText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      long result = 0L;

      for (int i = 0; i < length.get(); i++) {
         byte b = buf.readByte();
         result = (result << 8) + (b & 255);
      }

      return result;
   }

   @Override
   public long decodeLongBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return this.decodeLongText(buf, length);
   }

   @Override
   public float decodeFloatText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException(String.format("Data type %s cannot be decoded as Float", this.dataType));
   }

   @Override
   public float decodeFloatBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException(String.format("Data type %s cannot be decoded as Float", this.dataType));
   }

   @Override
   public double decodeDoubleText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException(String.format("Data type %s cannot be decoded as Double", this.dataType));
   }

   @Override
   public double decodeDoubleBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException(String.format("Data type %s cannot be decoded as Double", this.dataType));
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
