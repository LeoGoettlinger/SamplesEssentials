package org.jdbi.v3.core.array;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

class ArrayColumnMapper implements ColumnMapper<Object> {
   private static final CopyOnWriteArraySet<Integer> UNSUPPORTED_TYPES = new CopyOnWriteArraySet<>();
   private final ColumnMapper<?> elementMapper;
   private final Class<?> componentType;

   ArrayColumnMapper(ColumnMapper<?> elementMapper, Class<?> componentType) {
      this.elementMapper = elementMapper;
      this.componentType = componentType;
   }

   @Override
   public Object map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
      Array array = r.getArray(columnNumber);
      if (array == null) {
         return null;
      } else {
         Object var5;
         try {
            var5 = this.buildArray(array, ctx);
         } finally {
            array.free();
         }

         return var5;
      }
   }

   private Object buildArray(Array array, StatementContext ctx) throws SQLException {
      if (!UNSUPPORTED_TYPES.contains(array.getBaseType())) {
         try {
            Object ary = array.getArray();
            if (this.componentType.equals(ary.getClass().getComponentType())) {
               return ary;
            }
         } catch (SQLException var4) {
         }
      }

      UNSUPPORTED_TYPES.add(array.getBaseType());
      return this.buildFromResultSet(array, ctx);
   }

   private Object buildFromResultSet(Array array, StatementContext ctx) throws SQLException {
      List<Object> list = new ArrayList<>();

      try (ResultSet rs = array.getResultSet()) {
         while (rs.next()) {
            list.add(this.elementMapper.map(rs, 2, ctx));
         }
      }

      Object ary = java.lang.reflect.Array.newInstance(this.componentType, list.size());
      if (!this.componentType.isPrimitive()) {
         return list.toArray((Object[])ary);
      } else {
         for (int i = 0; i < list.size(); i++) {
            java.lang.reflect.Array.set(ary, i, list.get(i));
         }

         return ary;
      }
   }
}
