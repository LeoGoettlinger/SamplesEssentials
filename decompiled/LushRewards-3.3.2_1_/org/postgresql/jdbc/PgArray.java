package org.postgresql.jdbc;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.Driver;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.BaseStatement;
import org.postgresql.core.Field;
import org.postgresql.core.Tuple;
import org.postgresql.jdbc2.ArrayAssistantRegistry;
import org.postgresql.util.ByteConverter;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class PgArray implements Array {
   @Nullable
   protected BaseConnection connection;
   private final int oid;
   @Nullable
   protected String fieldString;
   @Nullable
   protected ArrayDecoding.PgArrayList arrayList;
   @Nullable
   protected byte[] fieldBytes;
   private final ResourceLock lock = new ResourceLock();

   private PgArray(BaseConnection connection, int oid) throws SQLException {
      this.connection = connection;
      this.oid = oid;
   }

   public PgArray(BaseConnection connection, int oid, @Nullable String fieldString) throws SQLException {
      this(connection, oid);
      this.fieldString = fieldString;
   }

   public PgArray(BaseConnection connection, int oid, @Nullable byte[] fieldBytes) throws SQLException {
      this(connection, oid);
      this.fieldBytes = fieldBytes;
   }

   private BaseConnection getConnection() {
      return Nullness.castNonNull(this.connection);
   }

   @Override
   public Object getArray() throws SQLException {
      return this.getArrayImpl(1L, 0, null);
   }

   @Override
   public Object getArray(long index, int count) throws SQLException {
      return this.getArrayImpl(index, count, null);
   }

   public Object getArrayImpl(Map<String, Class<?>> map) throws SQLException {
      return this.getArrayImpl(1L, 0, map);
   }

   @Override
   public Object getArray(Map<String, Class<?>> map) throws SQLException {
      return this.getArrayImpl(map);
   }

   @Override
   public Object getArray(long index, int count, @Nullable Map<String, Class<?>> map) throws SQLException {
      return this.getArrayImpl(index, count, map);
   }

   @Nullable
   public Object getArrayImpl(long index, int count, @Nullable Map<String, Class<?>> map) throws SQLException {
      if (map != null && !map.isEmpty()) {
         throw Driver.notImplemented(this.getClass(), "getArrayImpl(long,int,Map)");
      } else if (index < 1L) {
         throw new PSQLException(GT.tr("The array index is out of range: {0}", index), PSQLState.DATA_ERROR);
      } else if (this.fieldBytes != null) {
         return this.readBinaryArray(this.fieldBytes, (int)index, count);
      } else if (this.fieldString == null) {
         return null;
      } else {
         ArrayDecoding.PgArrayList arrayList = this.buildArrayList(this.fieldString);
         if (count == 0) {
            count = arrayList.size();
         }

         if (index - 1L + count > arrayList.size()) {
            throw new PSQLException(
               GT.tr("The array index is out of range: {0}, number of elements: {1}.", index + count, (long)arrayList.size()), PSQLState.DATA_ERROR
            );
         } else {
            return this.buildArray(arrayList, (int)index, count);
         }
      }
   }

   private Object readBinaryArray(byte[] fieldBytes, int index, int count) throws SQLException {
      return ArrayDecoding.readBinaryArray(index, count, fieldBytes, this.getConnection());
   }

   private ResultSet readBinaryResultSet(byte[] fieldBytes, int index, int count) throws SQLException {
      int dimensions = ByteConverter.int4(fieldBytes, 0);
      int elementOid = ByteConverter.int4(fieldBytes, 8);
      int pos = 12;
      int[] dims = new int[dimensions];

      for (int d = 0; d < dimensions; d++) {
         dims[d] = ByteConverter.int4(fieldBytes, pos);
         pos += 4;
         pos += 4;
      }

      if (count > 0 && dimensions > 0) {
         dims[0] = Math.min(count, dims[0]);
      }

      List<Tuple> rows = new ArrayList<>();
      Field[] fields = new Field[2];
      this.storeValues(fieldBytes, rows, fields, elementOid, dims, pos, 0, index);
      BaseStatement stat = (BaseStatement)this.getConnection().createStatement(1004, 1007);
      return stat.createDriverResultSet(fields, rows);
   }

   private int storeValues(byte[] fieldBytes, List<Tuple> rows, Field[] fields, int elementOid, int[] dims, int pos, int thisDimension, int index) throws SQLException {
      if (dims.length == 0) {
         fields[0] = new Field("INDEX", 23);
         fields[0].setFormat(1);
         fields[1] = new Field("VALUE", elementOid);
         fields[1].setFormat(1);

         for (int i = 1; i < index; i++) {
            int len = ByteConverter.int4(fieldBytes, pos);
            pos += 4;
            if (len != -1) {
               pos += len;
            }
         }
      } else if (thisDimension == dims.length - 1) {
         fields[0] = new Field("INDEX", 23);
         fields[0].setFormat(1);
         fields[1] = new Field("VALUE", elementOid);
         fields[1].setFormat(1);

         for (int ix = 1; ix < index; ix++) {
            int len = ByteConverter.int4(fieldBytes, pos);
            pos += 4;
            if (len != -1) {
               pos += len;
            }
         }

         for (int ixx = 0; ixx < dims[thisDimension]; ixx++) {
            byte[][] rowData = new byte[][]{new byte[4], null};
            ByteConverter.int4(rowData[0], 0, ixx + index);
            rows.add(new Tuple(rowData));
            int len = ByteConverter.int4(fieldBytes, pos);
            pos += 4;
            if (len != -1) {
               rowData[1] = new byte[len];
               System.arraycopy(fieldBytes, pos, rowData[1], 0, rowData[1].length);
               pos += len;
            }
         }
      } else {
         fields[0] = new Field("INDEX", 23);
         fields[0].setFormat(1);
         fields[1] = new Field("VALUE", this.oid);
         fields[1].setFormat(1);
         int nextDimension = thisDimension + 1;
         int dimensionsLeft = dims.length - nextDimension;

         for (int ixxx = 1; ixxx < index; ixxx++) {
            pos = this.calcRemainingDataLength(fieldBytes, dims, pos, elementOid, nextDimension);
         }

         for (int ixxx = 0; ixxx < dims[thisDimension]; ixxx++) {
            byte[][] rowData = new byte[][]{new byte[4], null};
            ByteConverter.int4(rowData[0], 0, ixxx + index);
            rows.add(new Tuple(rowData));
            int dataEndPos = this.calcRemainingDataLength(fieldBytes, dims, pos, elementOid, nextDimension);
            int dataLength = dataEndPos - pos;
            rowData[1] = new byte[12 + 8 * dimensionsLeft + dataLength];
            ByteConverter.int4(rowData[1], 0, dimensionsLeft);
            System.arraycopy(fieldBytes, 4, rowData[1], 4, 8);
            System.arraycopy(fieldBytes, 12 + nextDimension * 8, rowData[1], 12, dimensionsLeft * 8);
            System.arraycopy(fieldBytes, pos, rowData[1], 12 + dimensionsLeft * 8, dataLength);
            pos = dataEndPos;
         }
      }

      return pos;
   }

   private int calcRemainingDataLength(byte[] fieldBytes, int[] dims, int pos, int elementOid, int thisDimension) {
      if (thisDimension == dims.length - 1) {
         for (int i = 0; i < dims[thisDimension]; i++) {
            int len = ByteConverter.int4(fieldBytes, pos);
            pos += 4;
            if (len != -1) {
               pos += len;
            }
         }
      } else {
         pos = this.calcRemainingDataLength(fieldBytes, dims, elementOid, pos, thisDimension + 1);
      }

      return pos;
   }

   private ArrayDecoding.PgArrayList buildArrayList(String fieldString) throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      ArrayDecoding.PgArrayList var3;
      try {
         if (this.arrayList == null) {
            this.arrayList = ArrayDecoding.buildArrayList(fieldString, this.getConnection().getTypeInfo().getArrayDelimiter(this.oid));
         }

         var3 = this.arrayList;
      } catch (Throwable var6) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var3;
   }

   private Object buildArray(ArrayDecoding.PgArrayList input, int index, int count) throws SQLException {
      BaseConnection connection = this.getConnection();
      return ArrayDecoding.readStringArray(index, count, connection.getTypeInfo().getPGArrayElement(this.oid), input, connection);
   }

   @Override
   public int getBaseType() throws SQLException {
      return this.getConnection().getTypeInfo().getSQLType(this.getBaseTypeName());
   }

   @Override
   public String getBaseTypeName() throws SQLException {
      int elementOID = this.getConnection().getTypeInfo().getPGArrayElement(this.oid);
      return Nullness.castNonNull(this.getConnection().getTypeInfo().getPGType(elementOID));
   }

   @Override
   public ResultSet getResultSet() throws SQLException {
      return this.getResultSetImpl(1L, 0, null);
   }

   @Override
   public ResultSet getResultSet(long index, int count) throws SQLException {
      return this.getResultSetImpl(index, count, null);
   }

   @Override
   public ResultSet getResultSet(@Nullable Map<String, Class<?>> map) throws SQLException {
      return this.getResultSetImpl(map);
   }

   @Override
   public ResultSet getResultSet(long index, int count, @Nullable Map<String, Class<?>> map) throws SQLException {
      return this.getResultSetImpl(index, count, map);
   }

   public ResultSet getResultSetImpl(@Nullable Map<String, Class<?>> map) throws SQLException {
      return this.getResultSetImpl(1L, 0, map);
   }

   public ResultSet getResultSetImpl(long index, int count, Map<String, Class<?>> map) throws SQLException {
      if (map != null && !map.isEmpty()) {
         throw Driver.notImplemented(this.getClass(), "getResultSetImpl(long,int,Map)");
      } else if (index < 1L) {
         throw new PSQLException(GT.tr("The array index is out of range: {0}", index), PSQLState.DATA_ERROR);
      } else if (this.fieldBytes != null) {
         return this.readBinaryResultSet(this.fieldBytes, (int)index, count);
      } else {
         ArrayDecoding.PgArrayList arrayList = this.buildArrayList(Nullness.castNonNull(this.fieldString));
         if (count == 0) {
            count = arrayList.size();
         }

         if (--index + count > arrayList.size()) {
            throw new PSQLException(
               GT.tr("The array index is out of range: {0}, number of elements: {1}.", index + count, (long)arrayList.size()), PSQLState.DATA_ERROR
            );
         } else {
            List<Tuple> rows = new ArrayList<>();
            Field[] fields = new Field[2];
            if (arrayList.dimensionsCount <= 1) {
               int baseOid = this.getConnection().getTypeInfo().getPGArrayElement(this.oid);
               fields[0] = new Field("INDEX", 23);
               fields[1] = new Field("VALUE", baseOid);

               for (int i = 0; i < count; i++) {
                  int offset = (int)index + i;
                  byte[][] t = new byte[2][0];
                  String v = (String)arrayList.get(offset);
                  t[0] = this.getConnection().encodeString(Integer.toString(offset + 1));
                  t[1] = v == null ? null : this.getConnection().encodeString(v);
                  rows.add(new Tuple(t));
               }
            } else {
               fields[0] = new Field("INDEX", 23);
               fields[1] = new Field("VALUE", this.oid);

               for (int i = 0; i < count; i++) {
                  int offset = (int)index + i;
                  byte[][] t = new byte[2][0];
                  Object v = arrayList.get(offset);
                  t[0] = this.getConnection().encodeString(Integer.toString(offset + 1));
                  t[1] = v == null ? null : this.getConnection().encodeString(this.toString((ArrayDecoding.PgArrayList)v));
                  rows.add(new Tuple(t));
               }
            }

            BaseStatement stat = (BaseStatement)this.getConnection().createStatement(1004, 1007);
            return stat.createDriverResultSet(fields, rows);
         }
      }
   }

   @Nullable
   @Override
   public String toString() {
      if (this.fieldString == null && this.fieldBytes != null) {
         try {
            Object array = this.readBinaryArray(this.fieldBytes, 1, 0);
            ArrayEncoding.ArrayEncoder arraySupport = ArrayEncoding.getArrayEncoder(array);

            assert arraySupport != null;

            this.fieldString = arraySupport.toArrayString(this.connection.getTypeInfo().getArrayDelimiter(this.oid), array);
         } catch (SQLException var3) {
            this.fieldString = "NULL";
         }
      }

      return this.fieldString;
   }

   private String toString(ArrayDecoding.PgArrayList list) throws SQLException {
      if (list == null) {
         return "NULL";
      } else {
         StringBuilder b = new StringBuilder().append('{');
         char delim = this.getConnection().getTypeInfo().getArrayDelimiter(this.oid);

         for (int i = 0; i < list.size(); i++) {
            Object v = list.get(i);
            if (i > 0) {
               b.append(delim);
            }

            if (v == null) {
               b.append("NULL");
            } else if (v instanceof ArrayDecoding.PgArrayList) {
               b.append(this.toString((ArrayDecoding.PgArrayList)v));
            } else {
               escapeArrayElement(b, (String)v);
            }
         }

         b.append('}');
         return b.toString();
      }
   }

   public static void escapeArrayElement(StringBuilder b, String s) {
      b.append('"');

      for (int j = 0; j < s.length(); j++) {
         char c = s.charAt(j);
         if (c == '"' || c == '\\') {
            b.append('\\');
         }

         b.append(c);
      }

      b.append('"');
   }

   public boolean isBinary() {
      return this.fieldBytes != null;
   }

   @Nullable
   public byte[] toBytes() {
      return this.fieldBytes;
   }

   @Override
   public void free() throws SQLException {
      this.connection = null;
      this.fieldString = null;
      this.fieldBytes = null;
      this.arrayList = null;
   }

   static {
      ArrayAssistantRegistry.register(2950, new UUIDArrayAssistant());
      ArrayAssistantRegistry.register(2951, new UUIDArrayAssistant());
   }
}
