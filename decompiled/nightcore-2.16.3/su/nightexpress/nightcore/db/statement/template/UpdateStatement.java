package su.nightexpress.nightcore.db.statement.template;

import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.db.config.DatabaseType;
import su.nightexpress.nightcore.db.statement.ColumnMapping;
import su.nightexpress.nightcore.db.statement.condition.Wheres;

public final class UpdateStatement<T> extends UpsertStatement<T> {
   private UpdateStatement(@NonNull List<String> columns, @NonNull List<ColumnMapping<T, ?>> columnMappings) {
      super(columns, columnMappings);
   }

   @NonNull
   public static <T> UpdateStatement.Builder<T> builder(@NonNull Class<T> type) {
      return new UpdateStatement.Builder<>();
   }

   @NonNull
   public static <T> UpdateStatement.Builder<T> builder() {
      return new UpdateStatement.Builder<>();
   }

   @NonNull
   @Override
   public String toSql(@NonNull DatabaseType type, @NonNull String table, @Nullable Wheres<T> where) {
      String columns = this.columns.stream().map(col -> col + " = ?").collect(Collectors.joining(","));
      StringBuilder builder = new StringBuilder();
      builder.append("UPDATE ").append(table).append(" SET ").append(columns);
      if (where != null && !where.isEmpty()) {
         builder.append(" WHERE ").append(where.toSql());
      }

      return builder.toString();
   }

   public static class Builder<T> extends UpsertStatement.AbstractBuilder<UpdateStatement.Builder<T>, UpdateStatement<T>, T> {
      Builder() {
      }

      @NonNull
      public UpdateStatement<T> build() {
         return new UpdateStatement<>(this.columns, this.columnMappings);
      }

      @NonNull
      protected UpdateStatement.Builder<T> getThis() {
         return this;
      }
   }
}
