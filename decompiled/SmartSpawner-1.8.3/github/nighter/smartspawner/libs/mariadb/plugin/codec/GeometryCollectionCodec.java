package github.nighter.smartspawner.libs.mariadb.plugin.codec;

import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.socket.Writer;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.plugin.Codec;
import github.nighter.smartspawner.libs.mariadb.type.Geometry;
import github.nighter.smartspawner.libs.mariadb.type.GeometryCollection;
import github.nighter.smartspawner.libs.mariadb.type.LineString;
import github.nighter.smartspawner.libs.mariadb.type.MultiLineString;
import github.nighter.smartspawner.libs.mariadb.type.MultiPoint;
import github.nighter.smartspawner.libs.mariadb.type.MultiPolygon;
import github.nighter.smartspawner.libs.mariadb.type.Point;
import github.nighter.smartspawner.libs.mariadb.type.Polygon;
import java.io.IOException;
import java.sql.SQLDataException;
import java.util.Calendar;

public class GeometryCollectionCodec implements Codec<GeometryCollection> {
   public static final GeometryCollectionCodec INSTANCE = new GeometryCollectionCodec();

   @Override
   public String className() {
      return GeometryCollection.class.getName();
   }

   @Override
   public boolean canDecode(ColumnDecoder column, Class<?> type) {
      return column.getType() == DataType.GEOMETRY && type.isAssignableFrom(GeometryCollection.class);
   }

   @Override
   public boolean canEncode(Object value) {
      return value instanceof GeometryCollection;
   }

   public GeometryCollection decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      return this.decodeBinary(buf, length, column, cal, context);
   }

   public GeometryCollection decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      if (column.getType() == DataType.GEOMETRY) {
         buf.skip(4);
         Geometry geo = Geometry.getGeometry(buf, length.get() - 4, column);
         if (geo instanceof GeometryCollection) {
            return (GeometryCollection)geo;
         } else {
            throw new SQLDataException(
               String.format("Geometric type %s cannot be decoded as GeometryCollection", geo == null ? "null" : geo.getClass().getName())
            );
         }
      } else {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as GeometryCollection", column.getType()));
      }
   }

   public void encodeText(Writer encoder, Context context, GeometryCollection value, Calendar cal, Long maxLength) throws IOException {
      encoder.writeBytes(("ST_GeomCollFromText('" + value.toString() + "')").getBytes());
   }

   public int getApproximateTextProtocolLength(GeometryCollection value, Long length) {
      return -1;
   }

   public void encodeBinary(Writer encoder, Context context, GeometryCollection value, Calendar cal, Long maxLength) throws IOException {
      int length = 13;

      for (Geometry geo : value.getGeometries()) {
         if (geo instanceof Point) {
            length += 21;
         } else if (geo instanceof LineString) {
            length += 9 + ((LineString)geo).getPoints().length * 16;
         } else if (geo instanceof Polygon) {
            length += 9;

            for (LineString ls : ((Polygon)geo).getLines()) {
               length += 4 + ls.getPoints().length * 16;
            }
         } else if (geo instanceof MultiPoint) {
            length += 9 + ((MultiPoint)geo).getPoints().length * 21;
         } else if (geo instanceof MultiLineString) {
            length += 9;

            for (LineString ls : ((MultiLineString)geo).getLines()) {
               length += 9 + ls.getPoints().length * 16;
            }
         } else if (geo instanceof MultiPolygon) {
            length += 9;

            for (Polygon poly : ((MultiPolygon)geo).getPolygons()) {
               length += 9;

               for (LineString ls : poly.getLines()) {
                  length += 4 + ls.getPoints().length * 16;
               }
            }
         }
      }

      encoder.writeLength(length);
      encoder.writeInt(0);
      encoder.writeByte(1);
      encoder.writeInt(7);
      encoder.writeInt(value.getGeometries().length);

      for (Geometry geox : value.getGeometries()) {
         if (geox instanceof Point) {
            Point pt = (Point)geox;
            encoder.writeByte(1);
            encoder.writeInt(1);
            encoder.writeDouble(pt.getX());
            encoder.writeDouble(pt.getY());
         } else if (geox instanceof LineString) {
            LineString ls = (LineString)geox;
            encoder.writeByte(1);
            encoder.writeInt(2);
            encoder.writeInt(ls.getPoints().length);

            for (Point pt : ls.getPoints()) {
               encoder.writeDouble(pt.getX());
               encoder.writeDouble(pt.getY());
            }
         } else if (geox instanceof Polygon) {
            Polygon poly = (Polygon)geox;
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
         } else if (geox instanceof MultiPoint) {
            MultiPoint mp = (MultiPoint)geox;
            encoder.writeByte(1);
            encoder.writeInt(4);
            encoder.writeInt(mp.getPoints().length);

            for (Point pt : mp.getPoints()) {
               encoder.writeByte(1);
               encoder.writeInt(1);
               encoder.writeDouble(pt.getX());
               encoder.writeDouble(pt.getY());
            }
         } else if (geox instanceof MultiLineString) {
            MultiLineString mlines = (MultiLineString)geox;
            encoder.writeByte(1);
            encoder.writeInt(5);
            encoder.writeInt(mlines.getLines().length);

            for (LineString ls : mlines.getLines()) {
               encoder.writeByte(1);
               encoder.writeInt(2);
               encoder.writeInt(ls.getPoints().length);

               for (Point pt : ls.getPoints()) {
                  encoder.writeDouble(pt.getX());
                  encoder.writeDouble(pt.getY());
               }
            }
         } else if (geox instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon)geox;
            encoder.writeByte(1);
            encoder.writeInt(6);
            encoder.writeInt(multiPolygon.getPolygons().length);

            for (Polygon poly : multiPolygon.getPolygons()) {
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
      }
   }

   @Override
   public int getBinaryEncodeType() {
      return DataType.BLOB.get();
   }
}
