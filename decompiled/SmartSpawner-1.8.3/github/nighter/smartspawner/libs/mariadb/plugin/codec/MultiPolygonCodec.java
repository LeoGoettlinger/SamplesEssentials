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
import github.nighter.smartspawner.libs.mariadb.type.MultiPolygon;
import github.nighter.smartspawner.libs.mariadb.type.Point;
import github.nighter.smartspawner.libs.mariadb.type.Polygon;
import java.io.IOException;
import java.sql.SQLDataException;
import java.util.Calendar;

public class MultiPolygonCodec implements Codec<MultiPolygon> {
   public static final MultiPolygonCodec INSTANCE = new MultiPolygonCodec();

   @Override
   public String className() {
      return MultiPolygon.class.getName();
   }

   @Override
   public boolean canDecode(ColumnDecoder column, Class<?> type) {
      return column.getType() == DataType.GEOMETRY && type.isAssignableFrom(MultiPolygon.class);
   }

   @Override
   public boolean canEncode(Object value) {
      return value instanceof MultiPolygon;
   }

   public MultiPolygon decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      return this.decodeBinary(buf, length, column, cal, context);
   }

   public MultiPolygon decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      if (column.getType() == DataType.GEOMETRY) {
         buf.skip(4);
         Geometry geo = Geometry.getGeometry(buf, length.get() - 4, column);
         if (geo instanceof MultiPolygon) {
            return (MultiPolygon)geo;
         } else {
            throw new SQLDataException(String.format("Geometric type %s cannot be decoded as MultiPolygon", geo == null ? "null" : geo.getClass().getName()));
         }
      } else {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as MultiPolygon", column.getType()));
      }
   }

   public void encodeText(Writer encoder, Context context, MultiPolygon value, Calendar cal, Long maxLength) throws IOException {
      encoder.writeBytes(("ST_MPolyFromText('" + value.toString() + "')").getBytes());
   }

   public int getApproximateTextProtocolLength(MultiPolygon value, Long length) {
      return -1;
   }

   public void encodeBinary(Writer encoder, Context context, MultiPolygon value, Calendar cal, Long maxLength) throws IOException {
      int length = 13;

      for (Polygon poly : value.getPolygons()) {
         length += 9;

         for (LineString ls : poly.getLines()) {
            length += 4 + ls.getPoints().length * 16;
         }
      }

      encoder.writeLength(length);
      encoder.writeInt(0);
      encoder.writeByte(1);
      encoder.writeInt(6);
      encoder.writeInt(value.getPolygons().length);

      for (Polygon poly : value.getPolygons()) {
         encoder.writeByte(1);
         encoder.writeInt(3);
         encoder.writeInt(poly.getLines().length);

         for (LineString ls : poly.getLines()) {
            encoder.writeInt(ls.getPoints().length);

            for (Point pt : ls.getPoints()) {
               encoder.writeDouble(pt.getX());
               encoder.writeDouble(pt.getY());
            }
         }
      }
   }

   @Override
   public int getBinaryEncodeType() {
      return DataType.BLOB.get();
   }
}
