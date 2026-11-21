package ac.grim.grimac.internal.storage.backend.sql.v2.dialect;

import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.kind.IndexSpec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Locale;

@ApiStatus.Internal
public interface SqlDialect {
   @NotNull
   String name();

   @NotNull
   String createTableSql(@NotNull String var1, @NotNull EncodeShape var2);

   @NotNull
   String createIndexSql(@NotNull String var1, @NotNull IndexSpec var2);

   @NotNull
   String columnTypeSql(@NotNull EncodeShape.FieldDef var1);

   default boolean isBinaryColumnType(@NotNull String typeName) {
      String t = typeName.toLowerCase(Locale.ROOT);
      return t.contains("blob") || t.contains("bytea") || t.contains("binary") || t.contains("varbinary");
   }

   @NotNull
   default List<String> convertColumnToBinarySql(@NotNull String tableName, @NotNull EncodeShape.FieldDef field, @NotNull String actualTypeName) {
      return List.of();
   }

   @NotNull
   String quoteIdentifier(@NotNull String var1);

   @NotNull
   String upsertSql(@NotNull String var1, @NotNull EncodeShape var2);

   default boolean usesLegacySqliteUpsert() {
      return false;
   }

   default boolean supportsReturning() {
      return false;
   }

   @NotNull
   default String excludedRef(@NotNull String quotedCol) {
      return "EXCLUDED." + quotedCol;
   }

   @NotNull
   default String greatestFn(@NotNull String a, @NotNull String b) {
      return "GREATEST(" + a + ", " + b + ")";
   }

   @NotNull
   default String counterIncrementSql(@NotNull String quotedTable) {
      return "INSERT INTO "
         + quotedTable
         + " (id, value) VALUES (?, ?) ON CONFLICT (id) DO UPDATE SET value = "
         + quotedTable
         + ".value + "
         + this.excludedRef(this.quoteIdentifier("value"));
   }

   @NotNull
   default String kvValueColumnType() {
      return "BLOB";
   }

   @NotNull
   default String kvUpsertSql(@NotNull String quotedTable) {
      String scope = this.quoteIdentifier("scope");
      String scopeKey = this.quoteIdentifier("scope_key");
      String key = this.quoteIdentifier("key");
      String value = this.quoteIdentifier("value");
      String updatedAt = this.quoteIdentifier("updated_at");
      return "INSERT INTO "
         + quotedTable
         + " ("
         + scope
         + ", "
         + scopeKey
         + ", "
         + key
         + ", "
         + value
         + ", "
         + updatedAt
         + ") VALUES (?, ?, ?, ?, ?) ON CONFLICT ("
         + scope
         + ", "
         + scopeKey
         + ", "
         + key
         + ") DO UPDATE SET "
         + value
         + " = "
         + this.excludedRef(value)
         + ", "
         + updatedAt
         + " = "
         + this.excludedRef(updatedAt);
   }
}
