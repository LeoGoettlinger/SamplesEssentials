package org.jdbi.v3.core.statement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.jdbi.v3.core.result.ResultBearing;
import org.jdbi.v3.core.result.internal.ResultSetSupplier;

public class OutParameters {
   private final StatementContext ctx;
   private final ResultBearing resultSet;
   private final OutParameters.ParameterValueMap map = new OutParameters.ParameterValueMap();

   OutParameters(ResultBearing resultSet, StatementContext ctx) {
      this.resultSet = resultSet;
      this.ctx = ctx;
   }

   public ResultBearing getResultSet() {
      return this.resultSet;
   }

   @Nullable
   public <T> T getObject(String name, Class<T> type) {
      return type.cast(this.getObject(name));
   }

   @Nullable
   public <T> T getObject(int pos, Class<T> type) {
      return type.cast(this.getObject(pos));
   }

   @Nullable
   public Object getObject(String name) {
      return this.map.getValue(name);
   }

   @Nullable
   public Object getObject(int pos) {
      return this.map.getValue(pos);
   }

   @Nullable
   public String getString(String name) {
      Object obj = this.getObject(name);
      return obj == null ? null : obj.toString();
   }

   @Nullable
   public String getString(int pos) {
      Object obj = this.map.getValue(pos);
      return obj == null ? null : obj.toString();
   }

   @Nullable
   public byte[] getBytes(String name) {
      try {
         return this.getObject(name, byte[].class);
      } catch (ClassCastException var3) {
         throw new IllegalArgumentException(String.format("Parameter '%s' is not a byte[]", name), var3);
      }
   }

   @Nullable
   public byte[] getBytes(int pos) {
      try {
         return this.getObject(pos, byte[].class);
      } catch (ClassCastException var3) {
         throw new IllegalArgumentException(String.format("Parameter #%d is not a byte[]", pos), var3);
      }
   }

   @Nullable
   public Integer getInt(String name) {
      Number n = this.getNumber(name);
      return n == null ? null : n.intValue();
   }

   @Nullable
   public Integer getInt(int pos) {
      Number n = this.getNumber(pos);
      return n == null ? null : n.intValue();
   }

   public int getIntValue(String name) {
      Number n = this.getNumber(name);
      return n == null ? 0 : n.intValue();
   }

   public int getIntValue(int pos) {
      Number n = this.getNumber(pos);
      return n == null ? 0 : n.intValue();
   }

   @Nullable
   public Long getLong(String name) {
      Number n = this.getNumber(name);
      return n == null ? null : n.longValue();
   }

   @Nullable
   public Long getLong(int pos) {
      Number n = this.getNumber(pos);
      return n == null ? null : n.longValue();
   }

   public long getLongValue(String name) {
      Number n = this.getNumber(name);
      return n == null ? 0L : n.longValue();
   }

   public long getLongValue(int pos) {
      Number n = this.getNumber(pos);
      return n == null ? 0L : n.longValue();
   }

   @Nullable
   public Short getShort(String name) {
      Number n = this.getNumber(name);
      return n == null ? null : n.shortValue();
   }

   @Nullable
   public Short getShort(int pos) {
      Number n = this.getNumber(pos);
      return n == null ? null : n.shortValue();
   }

   public short getShortValue(String name) {
      Number n = this.getNumber(name);
      return n == null ? 0 : n.shortValue();
   }

   public short getShortValue(int pos) {
      Number n = this.getNumber(pos);
      return n == null ? 0 : n.shortValue();
   }

   @Nullable
   public Date getDate(String name) {
      try {
         return this.getObject(name, Date.class);
      } catch (ClassCastException var4) {
         Long epoch = this.getEpoch(name);
         return epoch == null ? null : new Date(epoch);
      }
   }

   @Nullable
   public Date getDate(int pos) {
      try {
         return this.getObject(pos, Date.class);
      } catch (ClassCastException var4) {
         Long epoch = this.getEpoch(pos);
         return epoch == null ? null : new Date(epoch);
      }
   }

   @Nullable
   public Timestamp getTimestamp(String name) {
      try {
         return this.getObject(name, Timestamp.class);
      } catch (ClassCastException var4) {
         Long epoch = this.getEpoch(name);
         return epoch == null ? null : new Timestamp(epoch);
      }
   }

   @Nullable
   public Timestamp getTimestamp(int pos) {
      try {
         return this.getObject(pos, Timestamp.class);
      } catch (ClassCastException var4) {
         Long epoch = this.getEpoch(pos);
         return epoch == null ? null : new Timestamp(epoch);
      }
   }

   @Nullable
   public Double getDouble(String name) {
      Number n = this.getNumber(name);
      return n == null ? null : n.doubleValue();
   }

   @Nullable
   public Double getDouble(int pos) {
      Number n = this.getNumber(pos);
      return n == null ? null : n.doubleValue();
   }

   public double getDoubleValue(String name) {
      Number n = this.getNumber(name);
      return n == null ? 0.0 : n.doubleValue();
   }

   public double getDoubleValue(int pos) {
      Number n = this.getNumber(pos);
      return n == null ? 0.0 : n.doubleValue();
   }

   @Nullable
   public Float getFloat(String name) {
      Number n = this.getNumber(name);
      return n == null ? null : n.floatValue();
   }

   @Nullable
   public Float getFloat(int pos) {
      Number n = this.getNumber(pos);
      return n == null ? null : n.floatValue();
   }

   public float getFloatValue(String name) {
      Number n = this.getNumber(name);
      return n == null ? 0.0F : n.floatValue();
   }

   public float getFloatValue(int pos) {
      Number n = this.getNumber(pos);
      return n == null ? 0.0F : n.floatValue();
   }

   @Nonnull
   public ResultBearing getRowSet(String name) {
      return ResultBearing.of(ResultSetSupplier.notClosingContext(() -> this.getObject(name, ResultSet.class)), this.ctx);
   }

   @Nonnull
   public ResultBearing getRowSet(int pos) {
      return ResultBearing.of(ResultSetSupplier.notClosingContext(() -> this.getObject(pos, ResultSet.class)), this.ctx);
   }

   @Nullable
   private Number getNumber(String name) {
      try {
         return this.getObject(name, Number.class);
      } catch (ClassCastException var3) {
         throw new IllegalArgumentException(String.format("Parameter '%s' is not a number", name), var3);
      }
   }

   @Nullable
   private Number getNumber(int pos) {
      try {
         return this.getObject(pos, Number.class);
      } catch (ClassCastException var3) {
         throw new IllegalArgumentException(String.format("Parameter #%d is not a number", pos), var3);
      }
   }

   @Nullable
   private Long getEpoch(String name) {
      try {
         java.util.Date date = this.getObject(name, java.util.Date.class);
         return date == null ? null : date.getTime();
      } catch (ClassCastException var3) {
         throw new IllegalArgumentException(String.format("Parameter '%s' is not a java.util.Date", name), var3);
      }
   }

   @Nullable
   private Long getEpoch(int pos) {
      try {
         java.util.Date date = this.getObject(pos, java.util.Date.class);
         return date == null ? null : date.getTime();
      } catch (ClassCastException var3) {
         throw new IllegalArgumentException(String.format("Parameter #%d is not a java.util.Date", pos), var3);
      }
   }

   void putValueSupplier(int index, String key, Supplier<Object> value) {
      this.map.putValueSupplier(index, key, value);
   }

   private static final class ParameterValueMap {
      private final Map<Object, Supplier<Object>> map = new HashMap<>();

      void putValueSupplier(int index, String key, Supplier<Object> value) {
         this.map.put(index, value);
         if (key != null) {
            this.map.put(key, value);
         }
      }

      Object getValue(int index) {
         Supplier<Object> supplier = this.map.get(index);
         if (supplier != null) {
            return supplier.get();
         } else {
            throw new IllegalArgumentException(String.format("Parameter #%d does not exist", index));
         }
      }

      Object getValue(String name) {
         Supplier<Object> supplier = this.map.get(name);
         if (supplier != null) {
            return supplier.get();
         } else {
            throw new IllegalArgumentException(String.format("Parameter '%s' does not exist", name));
         }
      }
   }
}
