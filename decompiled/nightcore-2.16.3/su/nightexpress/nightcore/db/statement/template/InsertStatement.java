package su.nightexpress.nightcore.db.statement.template;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.db.config.DatabaseType;
import su.nightexpress.nightcore.db.statement.ColumnMapping;
import su.nightexpress.nightcore.db.statement.condition.Wheres;

public final class InsertStatement<T> extends UpsertStatement<T> {
   private final boolean ignoreDuplications;
   private final boolean updateOnConflict;
   private final List<String> conflictKeys;
   private String sqlTemplate;

   private InsertStatement(
      @NonNull List<String> columns,
      @NonNull List<ColumnMapping<T, ?>> columnMappings,
      boolean ignoreDuplications,
      boolean updateOnConflict,
      @NonNull List<String> conflictKeys
   ) {
      super(columns, columnMappings);
      this.ignoreDuplications = ignoreDuplications;
      this.updateOnConflict = updateOnConflict;
      this.conflictKeys = conflictKeys;
   }

   @NonNull
   public static <T> InsertStatement.Builder<T> builder(@NonNull Class<T> type) {
      return new InsertStatement.Builder<>();
   }

   @NonNull
   public static <T> InsertStatement.Builder<T> builder() {
      return new InsertStatement.Builder<>();
   }

   @NonNull
   @Override
   public String toSql(@NonNull DatabaseType type, @NonNull String table, @Nullable Wheres<T> where) {
      if (this.sqlTemplate == null) {
         String columns = String.join(", ", this.columns);
         String values = this.columns.stream().map(value -> "?").collect(Collectors.joining(", "));
         String prefixModifier = "";
         String postfixModifier = "";
         if (this.ignoreDuplications) {
            prefixModifier = switch (type) {
               case MYSQL -> " IGNORE";
               case SQLITE -> " OR IGNORE";
            };
         } else if (this.updateOnConflict) {
            List<String> columnsToUpdate = this.columns.stream().filter(col -> !this.conflictKeys.contains(col)).toList();
            if (columnsToUpdate.isEmpty()) {
               throw new IllegalStateException("No columns left to update after filtering out conflict keys!");
            }

            if (type == DatabaseType.MYSQL) {
               String assignments = columnsToUpdate.stream().map(col -> col + " = VALUES(" + col + ")").collect(Collectors.joining(", "));
               postfixModifier = " ON DUPLICATE KEY UPDATE " + assignments;
            } else if (type == DatabaseType.SQLITE) {
               if (this.conflictKeys.isEmpty()) {
                  throw new IllegalStateException("SQLite requires conflict keys to be specified for UPSERT operations.");
               }

               String conflictTargets = String.join(", ", this.conflictKeys);
               String assignments = columnsToUpdate.stream().map(col -> col + " = EXCLUDED." + col).collect(Collectors.joining(", "));
               postfixModifier = " ON CONFLICT(" + conflictTargets + ") DO UPDATE SET " + assignments;
            }
         }

         this.sqlTemplate = "INSERT" + prefixModifier + " INTO %s (" + columns + ") VALUES (" + values + ")" + postfixModifier;
      }

      return this.sqlTemplate.formatted(table);
   }

   public static class Builder<T> extends UpsertStatement.AbstractBuilder<InsertStatement.Builder<T>, InsertStatement<T>, T> {
      private boolean ignoreDuplications = false;
      private boolean updateOnConflict = false;

      Builder() {
      }

      @NonNull
      public InsertStatement<T> build() {
         if (this.ignoreDuplications && this.updateOnConflict) {
            throw new IllegalStateException("Cannot use both ignoreDuplications and updateOnConflict simultaneously.");
         } else {
            List<String> finalConflictKeys = new ArrayList<>();
            if (this.updateOnConflict) {
               if (this.autoDetectedConflictKeys.isEmpty()) {
                  throw new IllegalStateException(
                     "SQLite requires conflict keys. None were provided explicitly, and no Primary Key or Unique columns were detected."
                  );
               }

               finalConflictKeys.addAll(this.autoDetectedConflictKeys);
            }

            return new InsertStatement<>(this.columns, this.columnMappings, this.ignoreDuplications, this.updateOnConflict, finalConflictKeys);
         }
      }

      @NonNull
      protected InsertStatement.Builder<T> getThis() {
         return this;
      }

      @NonNull
      public InsertStatement.Builder<T> ignoreDuplications() {
         this.ignoreDuplications = true;
         return this;
      }

      @NonNull
      public InsertStatement.Builder<T> updateOnConflict() {
         this.updateOnConflict = true;
         return this;
      }
   }
}
