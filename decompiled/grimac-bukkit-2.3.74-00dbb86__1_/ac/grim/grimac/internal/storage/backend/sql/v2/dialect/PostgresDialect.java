package ac.grim.grimac.internal.storage.backend.sql.v2.dialect;

import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.codec.MergeMode;
import ac.grim.grimac.api.storage.kind.IndexSpec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.UUID;

@ApiStatus.Internal
public final class PostgresDialect implements SqlDialect {
   @NotNull
   @Override
   public String name() {
      return "postgres";
   }

   @NotNull
   @Override
   public String kvValueColumnType() {
      return "BYTEA";
   }

   @NotNull
   @Override
   public String createTableSql(@NotNull String tableName, @NotNull EncodeShape shape) {
      StringBuilder sb = new StringBuilder();
      sb.append("CREATE TABLE IF NOT EXISTS ").append(quoteId(tableName)).append(" (\n");
      boolean first = true;

      for (EncodeShape.FieldDef f : shape.fields()) {
         if (!first) {
            sb.append(",\n");
         }

         sb.append("    ").append(quoteId(f.name())).append(' ').append(this.columnTypeSql(f));
         if (f.name().equals(shape.idField())) {
            sb.append(" PRIMARY KEY");
         }

         first = false;
      }

      sb.append("\n)");
      return sb.toString();
   }

   @NotNull
   @Override
   public String createIndexSql(@NotNull String tableName, @NotNull IndexSpec spec) {
      StringBuilder sb = new StringBuilder();
      sb.append("CREATE ");
      if (spec.unique()) {
         sb.append("UNIQUE ");
      }

      String physical = qualifiedIndexName(tableName, spec.name());
      sb.append("INDEX IF NOT EXISTS ").append(quoteId(physical));
      sb.append(" ON ").append(quoteId(tableName)).append(" (");
      boolean first = true;

      for (int i = 0; i < spec.fields().size(); i++) {
         String f = spec.fields().get(i);
         if (!first) {
            sb.append(", ");
         }

         boolean desc = f.startsWith("-");
         String name = desc ? f.substring(1) : f;
         if (i == 0 && spec.caseInsensitivePrefix()) {
            sb.append("LOWER(").append(quoteId(name)).append(')');
         } else {
            sb.append(quoteId(name));
         }

         if (desc) {
            sb.append(" DESC");
         }

         first = false;
      }

      sb.append(')');
      return sb.toString();
   }

   @NotNull
   static String qualifiedIndexName(@NotNull String tableName, @NotNull String indexName) {
      String combined = tableName + "_" + indexName;
      return combined.length() <= 63 ? combined : combined.substring(0, 63);
   }

   @NotNull
   @Override
   public String columnTypeSql(@NotNull EncodeShape.FieldDef field) {
      Class<?> t = field.javaType();
      String base;
      if (t == long.class || t == Long.class) {
         base = "BIGINT";
      } else if (t == int.class || t == Integer.class) {
         base = "INTEGER";
      } else if (t == double.class || t == Double.class) {
         base = "DOUBLE PRECISION";
      } else if (t == float.class || t == Float.class) {
         base = "REAL";
      } else if (t == boolean.class || t == Boolean.class) {
         base = "BOOLEAN";
      } else if (t == String.class) {
         base = "TEXT";
      } else if (t == UUID.class) {
         base = "UUID";
      } else if (t == byte[].class) {
         base = "BYTEA";
      } else {
         if (!t.isEnum()) {
            throw new IllegalArgumentException("PostgresDialect: unsupported field type " + t.getName() + " on " + field.name());
         }

         base = "INTEGER";
      }

      boolean primitive = t.isPrimitive();
      return !primitive && field.nullable() ? base : base + " NOT NULL";
   }

   @NotNull
   @Override
   public List<String> convertColumnToBinarySql(@NotNull String tableName, @NotNull EncodeShape.FieldDef field, @NotNull String actualTypeName) {
      if (field.javaType() == byte[].class && !this.isBinaryColumnType(actualTypeName)) {
         String column = quoteId(field.name());
         return List.of("ALTER TABLE " + quoteId(tableName) + " ALTER COLUMN " + column + " TYPE BYTEA USING convert_to(" + column + ", 'UTF8')");
      } else {
         return List.of();
      }
   }

   @NotNull
   @Override
   public String quoteIdentifier(@NotNull String name) {
      return quoteId(name);
   }

   @NotNull
   @Override
   public String upsertSql(@NotNull String tableName, @NotNull EncodeShape shape) {
      StringBuilder sb = new StringBuilder();
      sb.append("INSERT INTO ").append(quoteId(tableName)).append(" (");
      boolean first = true;

      for (EncodeShape.FieldDef f : shape.fields()) {
         if (!first) {
            sb.append(", ");
         }

         sb.append(quoteId(f.name()));
         first = false;
      }

      sb.append(") VALUES (");

      for (int i = 0; i < shape.fields().size(); i++) {
         if (i > 0) {
            sb.append(", ");
         }

         sb.append('?');
      }

      sb.append(") ON CONFLICT (").append(quoteId(shape.idField())).append(") DO UPDATE SET ");
      first = true;
      String tableQuoted = quoteId(tableName);

      for (EncodeShape.FieldDef f : shape.fields()) {
         if (!f.name().equals(shape.idField()) && f.mergeMode() != MergeMode.INSERT_ONLY) {
            if (!first) {
               sb.append(", ");
            }

            String col = quoteId(f.name());
            String existing = tableQuoted + "." + col;
            String incoming = "EXCLUDED." + col;
            switch (f.mergeMode()) {
               case OVERWRITE:
                  sb.append(col).append(" = ").append(incoming);
                  break;
               case PRESERVE_ON_NON_NULL:
                  sb.append(col).append(" = COALESCE(").append(existing).append(", ").append(incoming).append(')');
                  break;
               case PRESERVE_ON_NON_SENTINEL:
                  sb.append(col)
                     .append(" = CASE WHEN ")
                     .append(existing)
                     .append(" IS NULL OR ")
                     .append(existing)
                     .append(" = ")
                     .append(f.sentinelValue())
                     .append(" THEN ")
                     .append(incoming)
                     .append(" ELSE ")
                     .append(existing)
                     .append(" END");
                  break;
               case MAX:
                  sb.append(col).append(" = GREATEST(").append(existing).append(", ").append(incoming).append(')');
                  break;
               case MIN:
                  sb.append(col).append(" = LEAST(").append(existing).append(", ").append(incoming).append(')');
               case INSERT_ONLY:
            }

            first = false;
         }
      }

      if (first) {
         int set = sb.lastIndexOf(" DO UPDATE SET ");
         sb.setLength(set);
         sb.append(" DO NOTHING");
      }

      return sb.toString();
   }

   @Override
   public boolean supportsReturning() {
      return true;
   }

   @NotNull
   static String quoteId(@NotNull String name) {
      StringBuilder sb = new StringBuilder(name.length() + 2);
      sb.append('"');

      for (int i = 0; i < name.length(); i++) {
         char c = name.charAt(i);
         if (c == '"') {
            sb.append('"').append('"');
         } else {
            sb.append(c);
         }
      }

      sb.append('"');
      return sb.toString();
   }
}
