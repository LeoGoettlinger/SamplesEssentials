package ac.grim.grimac.internal.storage.backend.sql.v2.dialect;

import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.codec.MergeMode;
import ac.grim.grimac.api.storage.kind.IndexSpec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.UUID;

@ApiStatus.Internal
public final class MysqlDialect implements SqlDialect {
   @NotNull
   @Override
   public String name() {
      return "mysql";
   }

   @NotNull
   @Override
   public String createTableSql(@NotNull String tableName, @NotNull EncodeShape shape) {
      StringBuilder sb = new StringBuilder();
      sb.append("CREATE TABLE IF NOT EXISTS ").append(q(tableName)).append(" (\n");
      boolean first = true;

      for (EncodeShape.FieldDef f : shape.fields()) {
         if (!first) {
            sb.append(",\n");
         }

         sb.append("    ").append(q(f.name())).append(' ').append(this.columnTypeSql(f));
         if (f.name().equals(shape.idField())) {
            sb.append(" PRIMARY KEY");
         }

         first = false;
      }

      sb.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
      return sb.toString();
   }

   @NotNull
   @Override
   public String createIndexSql(@NotNull String tableName, @NotNull IndexSpec spec) {
      StringBuilder sb = new StringBuilder("CREATE ");
      if (spec.unique()) {
         sb.append("UNIQUE ");
      }

      sb.append("INDEX ").append(q(spec.name()));
      sb.append(" ON ").append(q(tableName)).append(" (");
      boolean first = true;

      for (String f : spec.fields()) {
         if (!first) {
            sb.append(", ");
         }

         boolean desc = f.startsWith("-");
         String name = desc ? f.substring(1) : f;
         sb.append(q(name));
         if (desc) {
            sb.append(" DESC");
         }

         first = false;
      }

      sb.append(')');
      return sb.toString();
   }

   @NotNull
   @Override
   public String columnTypeSql(@NotNull EncodeShape.FieldDef field) {
      Class<?> t = field.javaType();
      String base;
      if (t == long.class || t == Long.class) {
         base = "BIGINT";
      } else if (t == int.class || t == Integer.class) {
         base = "INT";
      } else if (t == double.class || t == Double.class) {
         base = "DOUBLE";
      } else if (t == float.class || t == Float.class) {
         base = "FLOAT";
      } else if (t == boolean.class || t == Boolean.class) {
         base = "TINYINT(1)";
      } else if (t == String.class) {
         base = "VARCHAR(512)";
      } else if (t == UUID.class) {
         base = "BINARY(16)";
      } else if (t == byte[].class) {
         base = "BLOB";
      } else {
         if (!t.isEnum()) {
            throw new IllegalArgumentException("MysqlDialect: unsupported type " + t.getName());
         }

         base = "INT";
      }

      boolean primitive = t.isPrimitive();
      return !primitive && field.nullable() ? base : base + " NOT NULL";
   }

   @NotNull
   @Override
   public List<String> convertColumnToBinarySql(@NotNull String tableName, @NotNull EncodeShape.FieldDef field, @NotNull String actualTypeName) {
      return field.javaType() == byte[].class && !this.isBinaryColumnType(actualTypeName)
         ? List.of("ALTER TABLE " + q(tableName) + " MODIFY COLUMN " + q(field.name()) + " " + this.columnTypeSql(field).replace(" NOT NULL", ""))
         : List.of();
   }

   @NotNull
   @Override
   public String quoteIdentifier(@NotNull String name) {
      return q(name);
   }

   @NotNull
   @Override
   public String upsertSql(@NotNull String tableName, @NotNull EncodeShape shape) {
      StringBuilder sb = new StringBuilder();
      sb.append("INSERT INTO ").append(q(tableName)).append(" (");
      boolean first = true;

      for (EncodeShape.FieldDef f : shape.fields()) {
         if (!first) {
            sb.append(", ");
         }

         sb.append(q(f.name()));
         first = false;
      }

      sb.append(") VALUES (");

      for (int i = 0; i < shape.fields().size(); i++) {
         if (i > 0) {
            sb.append(", ");
         }

         sb.append('?');
      }

      sb.append(") ON DUPLICATE KEY UPDATE ");
      first = true;

      for (EncodeShape.FieldDef f : shape.fields()) {
         if (!f.name().equals(shape.idField()) && f.mergeMode() != MergeMode.INSERT_ONLY) {
            if (!first) {
               sb.append(", ");
            }

            String col = q(f.name());
            String incoming = "VALUES(" + col + ")";
            switch (f.mergeMode()) {
               case OVERWRITE:
                  sb.append(col).append(" = ").append(incoming);
                  break;
               case PRESERVE_ON_NON_NULL:
                  sb.append(col).append(" = COALESCE(").append(col).append(", ").append(incoming).append(')');
                  break;
               case PRESERVE_ON_NON_SENTINEL:
                  sb.append(col)
                     .append(" = CASE WHEN ")
                     .append(col)
                     .append(" IS NULL OR ")
                     .append(col)
                     .append(" = ")
                     .append(f.sentinelValue())
                     .append(" THEN ")
                     .append(incoming)
                     .append(" ELSE ")
                     .append(col)
                     .append(" END");
                  break;
               case MAX:
                  sb.append(col).append(" = GREATEST(").append(col).append(", ").append(incoming).append(')');
                  break;
               case MIN:
                  sb.append(col).append(" = LEAST(").append(col).append(", ").append(incoming).append(')');
               case INSERT_ONLY:
            }

            first = false;
         }
      }

      if (first) {
         int upd = sb.lastIndexOf(" ON DUPLICATE KEY UPDATE ");
         sb.setLength(upd);
         sb.append(" ON DUPLICATE KEY UPDATE ").append(q(shape.idField())).append(" = ").append(q(shape.idField()));
      }

      return sb.toString();
   }

   @Override
   public boolean supportsReturning() {
      return false;
   }

   @NotNull
   @Override
   public String excludedRef(@NotNull String quotedCol) {
      return "VALUES(" + quotedCol + ")";
   }

   @NotNull
   @Override
   public String counterIncrementSql(@NotNull String quotedTable) {
      return "INSERT INTO " + quotedTable + " (`id`, `value`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `value` = `value` + VALUES(`value`)";
   }

   @NotNull
   @Override
   public String kvUpsertSql(@NotNull String quotedTable) {
      return "INSERT INTO "
         + quotedTable
         + " (`scope`, `scope_key`, `key`, `value`, `updated_at`) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `value` = VALUES(`value`), `updated_at` = VALUES(`updated_at`)";
   }

   @NotNull
   private static String q(@NotNull String name) {
      StringBuilder sb = new StringBuilder(name.length() + 2);
      sb.append('`');

      for (int i = 0; i < name.length(); i++) {
         char c = name.charAt(i);
         if (c == '`') {
            sb.append('`').append('`');
         } else {
            sb.append(c);
         }
      }

      sb.append('`');
      return sb.toString();
   }
}
