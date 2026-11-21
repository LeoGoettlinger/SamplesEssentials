package github.nighter.smartspawner.libs.mariadb.type;

import github.nighter.smartspawner.libs.mariadb.client.Column;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import java.sql.SQLDataException;

public interface Geometry {
   static Point parsePoint(boolean littleEndian, ReadableByteBuf buf) {
      double x = littleEndian ? buf.readDouble() : buf.readDoubleBE();
      double y = littleEndian ? buf.readDouble() : buf.readDoubleBE();
      return new Point(x, y);
   }

   static Geometry getGeometry(ReadableByteBuf buf, int length, Column column) throws SQLDataException {
      if (length == 0) {
         return null;
      } else {
         boolean littleEndian = buf.readByte() == 1;
         int dataType = littleEndian ? buf.readInt() : buf.readIntBE();
         switch (dataType) {
            case 1:
               return parsePoint(littleEndian, buf);
            case 2:
               int pointNumber = littleEndian ? buf.readInt() : buf.readIntBE();
               Point[] points = new Point[pointNumber];

               for (int i = 0; i < pointNumber; i++) {
                  points[i] = parsePoint(littleEndian, buf);
               }

               return new LineString(points, true);
            case 3:
               int numRings = littleEndian ? buf.readInt() : buf.readIntBE();
               LineString[] lines = new LineString[numRings];

               for (int i = 0; i < numRings; i++) {
                  int pointNb = littleEndian ? buf.readInt() : buf.readIntBE();
                  Point[] lsPoints = new Point[pointNb];

                  for (int j = 0; j < pointNb; j++) {
                     lsPoints[j] = parsePoint(littleEndian, buf);
                  }

                  lines[i] = new LineString(lsPoints, false);
               }

               return new Polygon(lines);
            case 4:
               int pointNb = littleEndian ? buf.readInt() : buf.readIntBE();
               Point[] pointArr = new Point[pointNb];

               for (int i = 0; i < pointNb; i++) {
                  pointArr[i] = (Point)getGeometry(buf, length, column);
               }

               return new MultiPoint(pointArr);
            case 5:
               int multiNb = littleEndian ? buf.readInt() : buf.readIntBE();
               LineString[] multiLines = new LineString[multiNb];

               for (int i = 0; i < multiNb; i++) {
                  multiLines[i] = (LineString)getGeometry(buf, length, column);
               }

               return new MultiLineString(multiLines);
            case 6:
               int multiPolyNb = littleEndian ? buf.readInt() : buf.readIntBE();
               Polygon[] multiPolygons = new Polygon[multiPolyNb];

               for (int i = 0; i < multiPolyNb; i++) {
                  multiPolygons[i] = (Polygon)getGeometry(buf, length, column);
               }

               return new MultiPolygon(multiPolygons);
            case 7:
               int multiCollNb = littleEndian ? buf.readInt() : buf.readIntBE();
               Geometry[] multiGeos = new Geometry[multiCollNb];

               for (int i = 0; i < multiCollNb; i++) {
                  multiGeos[i] = getGeometry(buf, length, column);
               }

               return new GeometryCollection(multiGeos);
            default:
               buf.skip(length - 5);
               throw new SQLDataException(String.format("Data type %s cannot be decoded as Geometry", column.getType()));
         }
      }
   }
}
