package ac.grim.grimac.internal.storage.backend.sql.v2.dialect;

import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.codec.MergeMode;
import ac.grim.grimac.api.storage.kind.IndexSpec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.UUID;

@ApiStatus.Internal
public final class SqliteDialect implements SqlDialect {
   private final boolean legacyUpsert;
   private final boolean supportsReturning;

   public SqliteDialect() {
      this(false, true);
   }

   private SqliteDialect(boolean legacyUpsert, boolean supportsReturning) {
      this.legacyUpsert = legacyUpsert;
      this.supportsReturning = supportsReturning;
   }

   @ApiStatus.Internal
   @NotNull
   public static SqliteDialect legacyForTest() {
      return new SqliteDialect(true, false);
   }

   @NotNull
   public static SqliteDialect forEngineVersion(@NotNull String version) {
      boolean legacy = compareVersionTriple(version, 3, 24, 0) < 0;
      boolean returning = compareVersionTriple(version, 3, 35, 0) >= 0;
      return new SqliteDialect(legacy, returning);
   }

   @NotNull
   @Override
   public String name() {
      return "sqlite";
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

      sb.append("\n)");
      return sb.toString();
   }

   @NotNull
   @Override
   public String createIndexSql(@NotNull String tableName, @NotNull IndexSpec spec) {
      StringBuilder sb = new StringBuilder("CREATE ");
      if (spec.unique()) {
         sb.append("UNIQUE ");
      }

      sb.append("INDEX IF NOT EXISTS ").append(q(tableName + "_" + spec.name()));
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
         base = "INTEGER";
      } else if (t == int.class || t == Integer.class) {
         base = "INTEGER";
      } else if (t == double.class || t == Double.class) {
         base = "REAL";
      } else if (t == float.class || t == Float.class) {
         base = "REAL";
      } else if (t == boolean.class || t == Boolean.class) {
         base = "INTEGER";
      } else if (t == String.class) {
         base = "TEXT";
      } else if (t == UUID.class) {
         base = "BLOB";
      } else if (t == byte[].class) {
         base = "BLOB";
      } else {
         if (!t.isEnum()) {
            throw new IllegalArgumentException("SqliteDialect: unsupported type " + t.getName());
         }

         base = "INTEGER";
      }

      boolean primitive = t.isPrimitive();
      return !primitive && field.nullable() ? base : base + " NOT NULL";
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

      sb.append(") ON CONFLICT (").append(q(shape.idField())).append(") DO UPDATE SET ");
      first = true;
      String tableQuoted = q(tableName);

      for (EncodeShape.FieldDef f : shape.fields()) {
         if (!f.name().equals(shape.idField()) && f.mergeMode() != MergeMode.INSERT_ONLY) {
            if (!first) {
               sb.append(", ");
            }

            String col = q(f.name());
            String existing = tableQuoted + "." + col;
            String incoming = "excluded." + col;
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
                  sb.append(col).append(" = MAX(").append(existing).append(", ").append(incoming).append(')');
                  break;
               case MIN:
                  sb.append(col).append(" = MIN(").append(existing).append(", ").append(incoming).append(')');
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
   public boolean usesLegacySqliteUpsert() {
      return this.legacyUpsert;
   }

   @Override
   public boolean supportsReturning() {
      return this.supportsReturning;
   }

   @NotNull
   @Override
   public String greatestFn(@NotNull String a, @NotNull String b) {
      return "MAX(" + a + ", " + b + ")";
   }

   @NotNull
   private static String q(@NotNull String name) {
      return PostgresDialect.quoteId(name);
   }

   private static int compareVersionTriple(String version, int majorFloor, int minorFloor, int patchFloor) {
      int[] triple = new int[]{0, 0, 0};
      int idx = 0;

      for (int i = 0; i < version.length() && idx < 3; i++) {
         int start = i;

         while (i < version.length() && Character.isDigit(version.charAt(i))) {
            i++;
         }

         if (i == start) {
            break;
         }

         triple[idx++] = Integer.parseInt(version.substring(start, i));
         if (i >= version.length() || version.charAt(i) != '.') {
            break;
         }
      }

      if (triple[0] != majorFloor) {
         return Integer.compare(triple[0], majorFloor);
      } else {
         return triple[1] != minorFloor ? Integer.compare(triple[1], minorFloor) : Integer.compare(triple[2], patchFloor);
      }
   }
}
