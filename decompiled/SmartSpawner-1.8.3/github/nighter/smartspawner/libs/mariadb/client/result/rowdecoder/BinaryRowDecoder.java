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

public class BinaryRowDecoder implements RowDecoder {
   @Override
   public <T> T decode(
      Codec<T> codec, Calendar cal, ReadableByteBuf rowBuf, MutableInt fieldLength, ColumnDecoder[] metadataList, int fieldIndex, Context context
   ) throws SQLException {
      return codec.decodeBinary(rowBuf, fieldLength, metadataList[fieldIndex], cal, context);
   }

   @Override
   public Object defaultDecode(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength, Context context) throws SQLException {
      return metadataList[fieldIndex].getDefaultBinary(rowBuf, fieldLength, context);
   }

   @Override
   public String decodeString(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength, Context context) throws SQLException {
      return metadataList[fieldIndex].decodeStringBinary(rowBuf, fieldLength, null, context);
   }

   @Override
   public byte decodeByte(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength) throws SQLException {
      return metadataList[fieldIndex].decodeByteBinary(rowBuf, fieldLength);
   }

   @Override
   public boolean decodeBoolean(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength) throws SQLException {
      return metadataList[fieldIndex].decodeBooleanBinary(rowBuf, fieldLength);
   }

   @Override
   public Date decodeDate(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength, Calendar cal, Context context) throws SQLException {
      return metadataList[fieldIndex].decodeDateBinary(rowBuf, fieldLength, cal, context);
   }

   @Override
   public Time decodeTime(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength, Calendar cal, Context context) throws SQLException {
      return metadataList[fieldIndex].decodeTimeBinary(rowBuf, fieldLength, cal, context);
   }

   @Override
   public Timestamp decodeTimestamp(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength, Calendar cal, Context context) throws SQLException {
      return metadataList[fieldIndex].decodeTimestampBinary(rowBuf, fieldLength, cal, context);
   }

   @Override
   public short decodeShort(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength) throws SQLException {
      return metadataList[fieldIndex].decodeShortBinary(rowBuf, fieldLength);
   }

   @Override
   public int decodeInt(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength) throws SQLException {
      return metadataList[fieldIndex].decodeIntBinary(rowBuf, fieldLength);
   }

   @Override
   public long decodeLong(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength) throws SQLException {
      return metadataList[fieldIndex].decodeLongBinary(rowBuf, fieldLength);
   }

   @Override
   public float decodeFloat(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength) throws SQLException {
      return metadataList[fieldIndex].decodeFloatBinary(rowBuf, fieldLength);
   }

   @Override
   public double decodeDouble(ColumnDecoder[] metadataList, int fieldIndex, ReadableByteBuf rowBuf, MutableInt fieldLength) throws SQLException {
      return metadataList[fieldIndex].decodeDoubleBinary(rowBuf, fieldLength);
   }

   @Override
   public boolean wasNull(byte[] nullBitmap, MutableInt fieldIndex, MutableInt fieldLength) {
      int idx = fieldIndex.get() + 2;
      return (nullBitmap[idx / 8] & 1 << idx % 8) > 0 || fieldLength.get() == -1;
   }

   @Override
   public int setPosition(int newIndex, MutableInt fieldIndex, int maxIndex, ReadableByteBuf rowBuf, byte[] nullBitmap, ColumnDecoder[] metadataList) throws SQLException {
      if (fieldIndex.get() >= newIndex) {
         fieldIndex.set(0);
         rowBuf.pos(1);
         rowBuf.readBytes(nullBitmap);
      } else {
         fieldIndex.incrementAndGet();
         if (fieldIndex.get() == 0) {
            rowBuf.pos(1);
            rowBuf.readBytes(nullBitmap);
         }
      }

      for (; fieldIndex.get() < newIndex; fieldIndex.incrementAndGet()) {
         int fi = fieldIndex.get();
         int idx = fi + 2;
         if ((nullBitmap[idx / 8] & 1 << idx % 8) == 0) {
            switch (metadataList[fi].getType()) {
               case BIGINT:
               case DOUBLE:
                  rowBuf.skip(8);
                  break;
               case INTEGER:
               case MEDIUMINT:
               case FLOAT:
                  rowBuf.skip(4);
                  break;
               case SMALLINT:
               case YEAR:
                  rowBuf.skip(2);
                  break;
               case TINYINT:
                  rowBuf.skip(1);
                  break;
               default:
                  rowBuf.skipLengthEncoded();
            }
         }
      }

      int fi = fieldIndex.get();
      int idx = fi + 2;
      if ((nullBitmap[idx / 8] & 1 << idx % 8) > 0) {
         return -1;
      } else {
         switch (metadataList[fi].getType()) {
            case BIGINT:
            case DOUBLE:
               return 8;
            case INTEGER:
            case MEDIUMINT:
            case FLOAT:
               return 4;
            case SMALLINT:
            case YEAR:
               return 2;
            case TINYINT:
               return 1;
            default:
               byte len = rowBuf.readByte();
               switch (len) {
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
   }
}
