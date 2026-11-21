package github.nighter.smartspawner.libs.mariadb.plugin.codec;

import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.socket.Writer;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.plugin.Codec;
import github.nighter.smartspawner.libs.mariadb.type.Geometry;
import github.nighter.smartspawner.libs.mariadb.type.MultiPoint;
import github.nighter.smartspawner.libs.mariadb.type.Point;
import java.io.IOException;
import java.sql.SQLDataException;
import java.util.Calendar;

public class MultiPointCodec implements Codec<MultiPoint> {
   public static final MultiPointCodec INSTANCE = new MultiPointCodec();

   @Override
   public String className() {
      return MultiPoint.class.getName();
   }

   @Override
   public boolean canDecode(ColumnDecoder column, Class<?> type) {
      return column.getType() == DataType.GEOMETRY && type.isAssignableFrom(MultiPoint.class);
   }

   @Override
   public boolean canEncode(Object value) {
      return value instanceof MultiPoint;
   }

   public MultiPoint decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      return this.decodeBinary(buf, length, column, cal, context);
   }

   public MultiPoint decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      if (column.getType() == DataType.GEOMETRY) {
         buf.skip(4);
         Geometry geo = Geometry.getGeometry(buf, length.get() - 4, column);
         if (geo instanceof MultiPoint) {
            return (MultiPoint)geo;
         } else {
            throw new SQLDataException(String.format("Geometric type %s cannot be decoded as MultiPoint", geo == null ? "null" : geo.getClass().getName()));
         }
      } else {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as MultiPoint", column.getType()));
      }
   }

   public void encodeText(Writer encoder, Context context, MultiPoint value, Calendar cal, Long maxLength) throws IOException {
      encoder.writeBytes(("ST_MPointFromText('" + value.toString() + "')").getBytes());
   }

   public int getApproximateTextProtocolLength(MultiPoint value, Long length) {
      return -1;
   }

   public void encodeBinary(Writer encoder, Context context, MultiPoint value, Calendar cal, Long maxLength) throws IOException {
      encoder.writeLength(13L + value.getPoints().length * 21L);
      encoder.writeInt(0);
      encoder.writeByte(1);
      encoder.writeInt(4);
      encoder.writeInt(value.getPoints().length);

      for (Point pt : value.getPoints()) {
         encoder.writeByte(1);
         encoder.writeInt(1);
         encoder.writeDouble(pt.getX());
         encoder.writeDouble(pt.getY());
      }
   }

   @Override
   public int getBinaryEncodeType() {
      return DataType.BLOB.get();
   }
}
