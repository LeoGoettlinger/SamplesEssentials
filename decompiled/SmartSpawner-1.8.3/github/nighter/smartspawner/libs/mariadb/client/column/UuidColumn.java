package github.nighter.smartspawner.libs.mariadb.client.column;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.message.server.ColumnDefinitionPacket;
import github.nighter.smartspawner.libs.mariadb.util.CharsetEncodingLength;
import java.sql.Date;
import java.sql.SQLDataException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

public class UuidColumn extends ColumnDefinitionPacket implements ColumnDecoder {
   public UuidColumn(
      ReadableByteBuf buf, int charset, long length, DataType dataType, byte decimals, int flags, int[] stringPos, byte[] extTypeName, byte[] extTypeFormat
   ) {
      super(buf, charset, length, dataType, decimals, flags, stringPos, extTypeName, extTypeFormat, false);
   }

   protected UuidColumn(UuidColumn prev) {
      super(prev, true);
   }

   public UuidColumn useAliasAsName() {
      return new UuidColumn(this);
   }

   @Override
   public String defaultClassname(Configuration conf) {
      return conf.uuidAsString() ? String.class.getName() : UUID.class.getName();
   }

   @Override
   public int getColumnType(Configuration conf) {
      return conf.uuidAsString() ? 1 : 1111;
   }

   @Override
   public String getColumnTypeName(Configuration conf) {
      return "uuid";
   }

   @Override
   public int getPrecision() {
      Integer maxWidth = CharsetEncodingLength.maxCharlen.get(this.charset);
      return maxWidth == null ? (int)this.columnLength : (int)(this.columnLength / maxWidth.intValue());
   }

   @Override
   public Object getDefaultText(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      return context.getConf().uuidAsString() ? buf.readString(length.get()) : UUID.fromString(buf.readAscii(length.get()));
   }

   @Override
   public Object getDefaultBinary(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      return context.getConf().uuidAsString() ? buf.readString(length.get()) : UUID.fromString(buf.readAscii(length.get()));
   }

   @Override
   public boolean decodeBooleanText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Boolean");
   }

   @Override
   public boolean decodeBooleanBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Boolean");
   }

   @Override
   public byte decodeByteText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as byte");
   }

   @Override
   public byte decodeByteBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as byte");
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
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Short");
   }

   @Override
   public short decodeShortBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Short");
   }

   @Override
   public int decodeIntText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Integer");
   }

   @Override
   public int decodeIntBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Integer");
   }

   @Override
   public long decodeLongText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Long");
   }

   @Override
   public long decodeLongBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Long");
   }

   @Override
   public float decodeFloatText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Float");
   }

   @Override
   public float decodeFloatBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Float");
   }

   @Override
   public double decodeDoubleText(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Double");
   }

   @Override
   public double decodeDoubleBinary(ReadableByteBuf buf, MutableInt length) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Double");
   }

   @Override
   public Date decodeDateText(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Date");
   }

   @Override
   public Date decodeDateBinary(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Date");
   }

   @Override
   public Time decodeTimeText(ReadableByteBuf buf, MutableInt length, Calendar cal, Context context) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Time");
   }

   @Override
   public Time decodeTimeBinary(ReadableByteBuf buf, MutableInt length, Calendar calParam, Context context) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Time");
   }

   @Override
   public Timestamp decodeTimestampText(ReadableByteBuf buf, MutableInt length, Calendar calParam, Context context) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Timestamp");
   }

   @Override
   public Timestamp decodeTimestampBinary(ReadableByteBuf buf, MutableInt length, Calendar calParam, Context context) throws SQLDataException {
      buf.skip(length.get());
      throw new SQLDataException("Data type UUID cannot be decoded as Timestamp");
   }
}
