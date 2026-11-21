package github.nighter.smartspawner.libs.mariadb.client.column;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.BigDecimalCodec;
import github.nighter.smartspawner.libs.mariadb.util.CharsetEncodingLength;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Date;
import java.sql.SQLDataException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;

public class BlobColumn extends StringColumn implements ColumnDecoder {
   public BlobColumn(
      ReadableByteBuf buf, int charset, long length, DataType dataType, byte decimals, int flags, int[] stringPos, byte[] extTypeName, byte[] extTypeFormat
   ) {
      super(buf, charset, length, dataType, decimals, flags, stringPos, extTypeName, extTypeFormat);
   }

   protected BlobColumn(BlobColumn prev) {
      super(prev);
   }

   @Override
   public int getDisplaySize() {
      if (this.charset != 63) {
         Integer maxWidth = CharsetEncodingLength.maxCharlen.get(this.charset);
         return maxWidth != null ? (int)(this.columnLength / maxWidth.intValue()) : (int)(this.columnLength / 4L);
      } else {
         return (int)this.columnLength;
      }
   }

   public BlobColumn useAliasAsName() {
      return new BlobColumn(this);
   }

   @Override
   public String defaultClassname(Configuration conf) {
      return this.isBinary() ? Blob.class.getName() : String.class.getName();
   }

   @Override
   public int getColumnType(Configuration conf) {
      if (this.columnLength <= 0L || this.getDisplaySize() > 16777215) {
         return this.isBinary() ? -4 : -1;
      } else if (this.dataType != DataType.TINYBLOB && this.dataType != DataType.BLOB) {
         return this.isBinary() ? -4 : -1;
      } else {
         return this.isBinary() ? -3 : 12;
      }
   }

   @Override
   public String getColumnTypeName(Configuration conf) {
      if (this.extTypeFormat != null) {
         return new String(this.extTypeFormat, StandardCharsets.US_ASCII).toUpperCase(Locale.ROOT);
      } else if (this.isBinary()) {
         if (this.columnLength < 0L) {
            return "LONGBLOB";
         } else if (this.columnLength <= 255L) {
            return "TINYBLOB";
         } else if (this.columnLength <= 65535L) {
            return "BLOB";
         } else {
            return this.columnLength <= 16777215L ? "MEDIUMBLOB" : "LONGBLOB";
         }
      } else if (this.columnLength < 0L) {
         return "LONGTEXT";
      } else if (this.getDisplaySize() <= 255) {
         return "TINYTEXT";
      } else if (this.getDisplaySize() <= 65535) {
         return "TEXT";
      } else {
         return this.getDisplaySize() <= 16777215 ? "MEDIUMTEXT" : "LONGTEXT";
      }
   }

   @Override
   public int getPrecision() {
      if (!this.isBinary()) {
         Integer maxWidth2 = CharsetEncodingLength.maxCharlen.get(this.charset);
         return maxWidth2 != null ? (int)(this.columnLength / maxWidth2.intValue()) : (int)this.columnLength / 4;
      } else {
         return (int)this.columnLength;
      }
   }

   @Override
   public Object getDefaultText(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      if (this.isBinary()) {
         byte[] bytes = new byte[length.get()];
         buf.readBytes(bytes);
         return bytes;
      } else {
         return buf.readString(length.get());
      }
   }

   @Override
   public Object getDefaultBinary(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      return this.getDefaultText(buf, length, context);
   }

   @Override
   public boolean decodeBooleanText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Boolean", this.dataType));
      } else {
         String s = buf.readAscii(length.get());
         return !"0".equals(s);
      }
   }

   @Override
   public boolean decodeBooleanBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return this.decodeBooleanText(buf, length);
   }

   @Override
   public byte decodeByteText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      if (!this.isBinary()) {
         String str2 = buf.readString(length.get());

         long result;
         try {
            result = BigDecimalCodec.parseBigDecimal(str2).setScale(0, RoundingMode.DOWN).longValue();
         } catch (NumberFormatException var7) {
            throw new SQLDataException(String.format("value '%s' (%s) cannot be decoded as Byte", str2, this.dataType));
         }

         if ((byte)result != result) {
            throw new SQLDataException("byte overflow");
         } else {
            return (byte)result;
         }
      } else if (length.get() > 0) {
         byte b = buf.readByte();
         buf.skip(length.get() - 1);
         return b;
      } else {
         throw new SQLDataException("empty String value cannot be decoded as Byte");
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
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Short", this.dataType));
      } else {
         return super.decodeShortText(buf, length);
      }
   }

   @Override
   public short decodeShortBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Short", this.dataType));
      } else {
         return super.decodeShortBinary(buf, length);
      }
   }

   @Override
   public int decodeIntText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Integer", this.dataType));
      } else {
         return super.decodeIntText(buf, length);
      }
   }

   @Override
   public int decodeIntBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Integer", this.dataType));
      } else {
         return super.decodeIntBinary(buf, length);
      }
   }

   @Override
   public long decodeLongText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Long", this.dataType));
      } else {
         return super.decodeLongText(buf, length);
      }
   }

   @Override
   public long decodeLongBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Long", this.dataType));
      } else {
         return super.decodeLongBinary(buf, length);
      }
   }

   @Override
   public float decodeFloatText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Float", this.dataType));
      } else {
         return super.decodeFloatText(buf, length);
      }
   }

   @Override
   public float decodeFloatBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      return this.decodeFloatText(buf, length);
   }

   @Override
   public double decodeDoubleText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Double", this.dataType));
      } else {
         return super.decodeDoubleText(buf, length);
      }
   }

   @Override
   public double decodeDoubleBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Double", this.dataType));
      } else {
         return super.decodeDoubleBinary(buf, length);
      }
   }

   @Override
   public Date decodeDateText(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Date", this.dataType));
      } else {
         return super.decodeDateText(buf, length, cal, context);
      }
   }

   @Override
   public Date decodeDateBinary(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Date", this.dataType));
      } else {
         return super.decodeDateBinary(buf, length, cal, context);
      }
   }

   @Override
   public Time decodeTimeText(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Time", this.dataType));
      } else {
         return super.decodeTimeText(buf, length, cal, context);
      }
   }

   @Override
   public Time decodeTimeBinary(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Time", this.dataType));
      } else {
         return super.decodeTimeBinary(buf, length, cal, context);
      }
   }

   @Override
   public Timestamp decodeTimestampText(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Timestamp", this.dataType));
      } else {
         return super.decodeTimestampText(buf, length, cal, context);
      }
   }

   @Override
   public Timestamp decodeTimestampBinary(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      if (this.isBinary()) {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as Timestamp", this.dataType));
      } else {
         return super.decodeTimestampBinary(buf, length, cal, context);
      }
   }
}
