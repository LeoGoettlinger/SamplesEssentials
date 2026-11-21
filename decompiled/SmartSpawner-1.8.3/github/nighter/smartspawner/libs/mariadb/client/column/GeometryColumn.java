package github.nighter.smartspawner.libs.mariadb.client.column;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.type.Geometry;
import github.nighter.smartspawner.libs.mariadb.type.GeometryCollection;
import github.nighter.smartspawner.libs.mariadb.type.LineString;
import github.nighter.smartspawner.libs.mariadb.type.MultiLineString;
import github.nighter.smartspawner.libs.mariadb.type.MultiPoint;
import github.nighter.smartspawner.libs.mariadb.type.MultiPolygon;
import github.nighter.smartspawner.libs.mariadb.type.Point;
import github.nighter.smartspawner.libs.mariadb.type.Polygon;
import java.nio.charset.StandardCharsets;
import java.sql.SQLDataException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;

public class GeometryColumn extends BlobColumn {
   public GeometryColumn(
      ReadableByteBuf buf, int charset, long length, DataType dataType, byte decimals, int flags, int[] stringPos, byte[] extTypeName, byte[] extTypeFormat
   ) {
      super(buf, charset, length, dataType, decimals, flags, stringPos, extTypeName, extTypeFormat);
   }

   protected GeometryColumn(GeometryColumn prev) {
      super(prev);
   }

   public GeometryColumn useAliasAsName() {
      return new GeometryColumn(this);
   }

   @Override
   public String defaultClassname(Configuration conf) {
      if (conf.geometryDefaultType() == null || !"default".equals(conf.geometryDefaultType())) {
         return "byte[]";
      } else {
         if (this.extTypeName != null) {
            String typeName = new String(this.extTypeName, StandardCharsets.US_ASCII);
            switch (typeName) {
               case "point":
                  return Point.class.getName();
               case "linestring":
                  return LineString.class.getName();
               case "polygon":
                  return Polygon.class.getName();
               case "multipoint":
                  return MultiPoint.class.getName();
               case "multilinestring":
                  return MultiLineString.class.getName();
               case "multipolygon":
                  return MultiPolygon.class.getName();
               case "geometrycollection":
                  return GeometryCollection.class.getName();
            }
         }

         return GeometryCollection.class.getName();
      }
   }

   @Override
   public int getColumnType(Configuration conf) {
      return -3;
   }

   @Override
   public String getColumnTypeName(Configuration conf) {
      return this.extTypeName != null ? new String(this.extTypeName, StandardCharsets.US_ASCII).toUpperCase(Locale.ROOT) : "GEOMETRY";
   }

   @Override
   public Object getDefaultText(ReadableByteBuf buf, MutableInt length, Context context) throws SQLDataException {
      if (context.getConf().geometryDefaultType() != null && "default".equals(context.getConf().geometryDefaultType())) {
         buf.skip(4);
         return Geometry.getGeometry(buf, length.get() - 4, this);
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
