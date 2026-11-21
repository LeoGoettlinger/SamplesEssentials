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
import github.nighter.smartspawner.libs.mariadb.type.Point;
import java.io.IOException;
import java.sql.SQLDataException;
import java.util.Calendar;

public class LineStringCodec implements Codec<LineString> {
   public static final LineStringCodec INSTANCE = new LineStringCodec();

   @Override
   public String className() {
      return LineString.class.getName();
   }

   @Override
   public boolean canDecode(ColumnDecoder column, Class<?> type) {
      return column.getType() == DataType.GEOMETRY && type.isAssignableFrom(LineString.class);
   }

   @Override
   public boolean canEncode(Object value) {
      return value instanceof LineString;
   }

   public LineString decodeText(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      return this.decodeBinary(buf, length, column, cal, context);
   }

   public LineString decodeBinary(ReadableByteBuf buf, MutableInt length, ColumnDecoder column, Calendar cal, Context context) throws SQLDataException {
      if (column.getType() == DataType.GEOMETRY) {
         buf.skip(4);
         Geometry geo = Geometry.getGeometry(buf, length.get() - 4, column);
         if (geo instanceof LineString) {
            return (LineString)geo;
         } else {
            throw new SQLDataException(String.format("Geometric type %s cannot be decoded as LineString", geo == null ? "null" : geo.getClass().getName()));
         }
      } else {
         buf.skip(length.get());
         throw new SQLDataException(String.format("Data type %s cannot be decoded as LineString", column.getType()));
      }
   }

   public void encodeText(Writer encoder, Context context, LineString value, Calendar cal, Long maxLength) throws IOException {
      encoder.writeBytes(("ST_LineFromText('" + value.toString() + "')").getBytes());
   }

   public int getApproximateTextProtocolLength(LineString value, Long length) {
      return -1;
   }

   public void encodeBinary(Writer encoder, Context context, LineString value, Calendar cal, Long maxLength) throws IOException {
      encoder.writeLength(13L + value.getPoints().length * 16L);
      encoder.writeInt(0);
      encoder.writeByte(1);
      encoder.writeInt(2);
      encoder.writeInt(value.getPoints().length);

      for (Point pt : value.getPoints()) {
         encoder.writeDouble(pt.getX());
         encoder.writeDouble(pt.getY());
      }
   }

   @Override
   public int getBinaryEncodeType() {
      return DataType.BLOB.get();
   }
}
