package github.nighter.smartspawner.libs.mariadb.client.result.rowdecoder;

import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.plugin.Codec;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class TextRowDecoder implements RowDecoder {
   @Override
   public <T> T decode(
      Codec<T> codec, Calendar cal, ReadableByteBuf rowBuf, MutableInt fieldLength, ColumnDecoder[] metadataList, int fieldIndex, Context context
   ) throws SQLException {
      return codec.decodeText(rowBuf, fieldLength, metadataList[fieldIndex], cal, context);
   }

   @Override
   public Object defaultDecode(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength, Context context) throws SQLException {
      return metadataList[fieldIndex].getDefaultText(rowBuf, fieldLength, context);
   }

   @Override
   public String decodeString(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength, Context context) throws SQLException {
      return metadataList[fieldIndex].decodeStringText(rowBuf, fieldLength, null, context);
   }

   @Override
   public byte decodeByte(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength) throws SQLException {
      return metadataList[fieldIndex].decodeByteText(rowBuf, fieldLength);
   }

   @Override
   public boolean decodeBoolean(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength) throws SQLException {
      return metadataList[fieldIndex].decodeBooleanText(rowBuf, fieldLength);
   }

   @Override
   public Date decodeDate(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength, Calendar cal, Context context) throws SQLException {
      return metadataList[fieldIndex].decodeDateText(rowBuf, fieldLength, cal, context);
   }

   @Override
   public Time decodeTime(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength, Calendar cal, Context context) throws SQLException {
      return metadataList[fieldIndex].decodeTimeText(rowBuf, fieldLength, cal, context);
   }

   @Override
   public Timestamp decodeTimestamp(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength, Calendar cal, Context context) throws SQLException {
      return metadataList[fieldIndex].decodeTimestampText(rowBuf, fieldLength, cal, context);
   }

   @Override
   public short decodeShort(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength) throws SQLException {
      return metadataList[fieldIndex].decodeShortText(rowBuf, fieldLength);
   }

   @Override
   public int decodeInt(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength) throws SQLException {
      return metadataList[fieldIndex].decodeIntText(rowBuf, fieldLength);
   }

   @Override
   public long decodeLong(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength) throws SQLException {
      return metadataList[fieldIndex].decodeLongText(rowBuf, fieldLength);
   }

   @Override
   public float decodeFloat(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength) throws SQLException {
      return metadataList[fieldIndex].decodeFloatText(rowBuf, fieldLength);
   }

   @Override
   public double decodeDouble(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength) throws SQLException {
      return metadataList[fieldIndex].decodeDoubleText(rowBuf, fieldLength);
   }

   @Override
   public boolean wasNull(byte[] nullBitmap, MutableInt fieldIndex, MutableInt fieldLength) {
      return fieldLength.get() == -1;
   }

   @Override
   public int setPosition(int newIndex, MutableInt fieldIndex, int maxIndex, ReadableByteBuf rowBuf, byte[] nullBitmap, ColumnDecoder[] metadataList) throws SQLException {
      if (fieldIndex.get() >= newIndex) {
         fieldIndex.set(0);
         rowBuf.pos(0);
      } else {
         fieldIndex.incrementAndGet();
      }

      while (fieldIndex.get() < newIndex) {
         rowBuf.skipLengthEncoded();
         fieldIndex.incrementAndGet();
      }

      byte len = rowBuf.buf[rowBuf.pos++];
      switch (len) {
         case -5:
            return -1;
         case -4:
            return rowBuf.readUnsignedShort();
         case -3:
            return rowBuf.readUnsignedMedium();
         case -2:
            long fieldLength = rowBuf.readLong();
            if (fieldLength >= 0L && fieldLength <= 2147483647L) {
               return (int)fieldLength;
            }

            throw new SQLException("Invalid length-encoded field length " + fieldLength);
         default:
            return len & 0xFF;
      }
   }
}
