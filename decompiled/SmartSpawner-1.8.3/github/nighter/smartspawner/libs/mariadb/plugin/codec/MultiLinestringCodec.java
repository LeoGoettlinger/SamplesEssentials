package github.nighter.smartspawner.libs.mariadb.plugin.codec;

import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.socket.Writer;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.plugin.Codec;
import github.nighter.smartspawner.libs.mariadb.type.Geometry;
import github.nighter.smartspawner.libs.mariadb.type.LineString;
import github.nighter.smartspawner.libs.mariadb.type.MultiLineString;
import github.nighter.smartspawner.libs.mariadb.type.Point;
import java.io.IOException;
import java.sql.SQLDataException;
import java.util.Calendar;

public class MultiLinestringCodec implements Codec<MultiLineString> {
   public static final MultiLinestringCodec INSTANCE = new MultiLinestringCodec();

   @Override
   public String className() {
      return MultiLineString.class.getName();
   }

   @Override
   public boolean canDecode(ColumnDecoder column, Class<?> type) {
      return column.getType() == DataType.GEOMETRY && type.isAssignableFrom(MultiLineString.class);
   }

   @Override
   public boolean canEncode(Object value) {
      return value instanceof MultiLineString;
   }

   public MultiLineString decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      return this.decodeBinary(buf, length, column, cal, context);
   }

   public MultiLineString decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      if (column.getType() == DataType.GEOMETRY) {
         buf.skip(4);
         Geometry geo = Geometry.getGeometry(buf, length.get() - 4, column);
         if (geo instanceof MultiLineString) {
            return (MultiLineString)geo;
         } else {
            throw new SQLDataException(String.format("Geometric type %s cannot be decoded as MultiLineString", geo == null ? "null" : geo.getClass().getName()));
         }
      } else {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as MultiLineString", column.getType()));
      }
   }

   public void encodeText(Writer encoder, Context context, MultiLineString value, Calendar cal, Long maxLength) throws IOException {
      encoder.writeBytes(("ST_MLineFromText('" + value.toString() + "')").getBytes());
   }

   public int getApproximateTextProtocolLength(MultiLineString value, Long length) {
      return -1;
   }

   public void encodeBinary(Writer encoder, Context context, MultiLineString value, Calendar cal, Long maxLength) throws IOException {
      int length = 13;

      for (LineString ls : value.getLines()) {
         length += 9 + ls.getPoints().length * 16;
      }

      encoder.writeLength(length);
      encoder.writeInt(0);
      encoder.writeByte(1);
      encoder.writeInt(5);
      encoder.writeInt(value.getLines().length);

      for (LineString ls : value.getLines()) {
         encoder.writeByte(1);
         encoder.writeInt(2);
         encoder.writeInt(ls.getPoints().length);

         for (Point pt : ls.getPoints()) {
            encoder.writeDouble(pt.getX());
            encoder.writeDouble(pt.getY());
         }
      }
   }

   @Override
   public int getBinaryEncodeType() {
      return DataType.BLOB.get();
   }
}
