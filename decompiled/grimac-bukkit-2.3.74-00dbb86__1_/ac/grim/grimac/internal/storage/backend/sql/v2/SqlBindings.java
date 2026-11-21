package ac.grim.grimac.internal.storage.backend.sql.v2;

import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

@ApiStatus.Internal
public final class SqlBindings {
   private SqlBindings() {
   }

   public static void bind(@NotNull PreparedStatement ps, int parameterIndex, @NotNull EncodeShape.FieldDef field, @Nullable Object value) throws SQLException {
      Class<?> t = field.javaType();
      if (value == null) {
         if (t.isPrimitive()) {
            throw new SQLException("null value for primitive field " + field.name());
         } else {
            ps.setNull(parameterIndex, sqlTypeFor(t));
         }
      } else {
         if (t == long.class || t == Long.class) {
            ps.setLong(parameterIndex, ((Number)value).longValue());
         } else if (t == int.class || t == Integer.class) {
            ps.setInt(parameterIndex, ((Number)value).intValue());
         } else if (t == double.class || t == Double.class) {
            ps.setDouble(parameterIndex, ((Number)value).doubleValue());
         } else if (t == float.class || t == Float.class) {
            ps.setFloat(parameterIndex, ((Number)value).floatValue());
         } else if (t == boolean.class || t == Boolean.class) {
            ps.setBoolean(parameterIndex, (Boolean)value);
         } else if (t == String.class) {
            ps.setString(parameterIndex, (String)value);
         } else if (t == UUID.class) {
            UUID uuid = (UUID)value;
            String driverName = "";

            try {
               driverName = ps.getConnection().getMetaData().getDriverName();
            } catch (SQLException var8) {
            }

            if (driverName.toLowerCase(Locale.ROOT).contains("postgresql")) {
               ps.setObject(parameterIndex, uuid);
            } else {
               ByteBuffer buf = ByteBuffer.allocate(16);
               buf.putLong(uuid.getMostSignificantBits());
               buf.putLong(uuid.getLeastSignificantBits());
               ps.setBytes(parameterIndex, buf.array());
            }
         } else if (t == byte[].class) {
            ps.setBytes(parameterIndex, (byte[])value);
         } else {
            if (!t.isEnum()) {
               throw new SQLException("SqlBindings: unsupported field type " + t.getName() + " for field " + field.name());
            }

            ps.setInt(parameterIndex, ((Enum)value).ordinal());
         }
      }
   }

   @Nullable
   public static Object extract(@NotNull ResultSet rs, @NotNull EncodeShape.FieldDef field) throws SQLException {
      Class<?> t = field.javaType();
      if (t == long.class || t == Long.class) {
         long v = rs.getLong(field.name());
         return rs.wasNull() ? null : v;
      } else if (t == int.class || t == Integer.class) {
         int v = rs.getInt(field.name());
         return rs.wasNull() ? null : v;
      } else if (t == double.class || t == Double.class) {
         double v = rs.getDouble(field.name());
         return rs.wasNull() ? null : v;
      } else if (t == float.class || t == Float.class) {
         float v = rs.getFloat(field.name());
         return rs.wasNull() ? null : (double)v;
      } else if (t == boolean.class || t == Boolean.class) {
         boolean v = rs.getBoolean(field.name());
         return rs.wasNull() ? null : v;
      } else if (t == String.class) {
         return rs.getString(field.name());
      } else if (t == UUID.class) {
         Object v = rs.getObject(field.name());
         if (v == null) {
            return null;
         } else if (v instanceof UUID u) {
            return u;
         } else if (v instanceof byte[] b && b.length == 16) {
            ByteBuffer buf = ByteBuffer.wrap(b);
            return new UUID(buf.getLong(), buf.getLong());
         } else if (v instanceof String s) {
            return UUID.fromString(s);
         } else {
            throw new SQLException("can't decode UUID from " + v.getClass().getName() + " on field " + field.name());
         }
      } else if (t == byte[].class) {
         return rs.getBytes(field.name());
      } else if (t.isEnum()) {
         int ordinal = rs.getInt(field.name());
         if (rs.wasNull()) {
            return null;
         } else {
            Object[] constants = t.getEnumConstants();
            if (ordinal >= 0 && ordinal < constants.length) {
               return constants[ordinal];
            } else {
               throw new SQLException("enum ordinal " + ordinal + " out of range for " + t.getName() + " on field " + field.name());
            }
         }
      } else {
         throw new SQLException("SqlBindings: unsupported field type " + t.getName() + " for field " + field.name());
      }
   }

   private static int sqlTypeFor(@NotNull Class<?> t) {
      if (t == Long.class) {
         return -5;
      } else if (t == Integer.class) {
         return 4;
      } else if (t == Double.class) {
         return 8;
      } else if (t == Float.class) {
         return 7;
      } else if (t == Boolean.class) {
         return 16;
      } else if (t == String.class) {
         return 12;
      } else if (t == UUID.class) {
         return 1111;
      } else if (t == byte[].class) {
         return -2;
      } else {
         return t.isEnum() ? 4 : 1111;
      }
   }

   @NotNull
   public static byte[] idToBytes(@NotNull EncodeShape.FieldDef field, @NotNull Object value) {
      Class<?> t = field.javaType();
      if (t == UUID.class) {
         UUID u = (UUID)value;
         ByteBuffer buf = ByteBuffer.allocate(16);
         buf.putLong(u.getMostSignificantBits());
         buf.putLong(u.getLeastSignificantBits());
         return buf.array();
      } else if (t == byte[].class) {
         return (byte[])value;
      } else if (t == String.class) {
         return ((String)value).getBytes(StandardCharsets.UTF_8);
      } else if (t == Long.class || t == long.class) {
         ByteBuffer buf = ByteBuffer.allocate(8);
         buf.putLong(((Number)value).longValue());
         return buf.array();
      } else if (t != Integer.class && t != int.class) {
         throw new IllegalArgumentException("SqlBindings.idToBytes: unsupported id type " + t.getName() + " for field " + field.name());
      } else {
         ByteBuffer buf = ByteBuffer.allocate(4);
         buf.putInt(((Number)value).intValue());
         return buf.array();
      }
   }

   @NotNull
   public static Object bytesToId(@NotNull EncodeShape.FieldDef field, @NotNull byte[] bytes) {
      Class<?> t = field.javaType();
      if (t == UUID.class) {
         if (bytes.length != 16) {
            throw new IllegalArgumentException("UUID id payload must be 16 bytes, got " + bytes.length);
         } else {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            return new UUID(buf.getLong(), buf.getLong());
         }
      } else if (t == byte[].class) {
         return bytes;
      } else if (t == String.class) {
         return new String(bytes, StandardCharsets.UTF_8);
      } else if (t == Long.class || t == long.class) {
         if (bytes.length != 8) {
            throw new IllegalArgumentException("Long id payload must be 8 bytes, got " + bytes.length);
         } else {
            return ByteBuffer.wrap(bytes).getLong();
         }
      } else if (t != Integer.class && t != int.class) {
         throw new IllegalArgumentException("SqlBindings.bytesToId: unsupported id type " + t.getName() + " for field " + field.name());
      } else if (bytes.length != 4) {
         throw new IllegalArgumentException("Integer id payload must be 4 bytes, got " + bytes.length);
      } else {
         return ByteBuffer.wrap(bytes).getInt();
      }
   }
}
