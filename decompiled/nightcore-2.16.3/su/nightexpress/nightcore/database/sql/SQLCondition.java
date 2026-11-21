package su.nightexpress.nightcore.database.sql;

import org.jspecify.annotations.NonNull;

@Deprecated
public class SQLCondition {
   private final SQLValue value;
   private final SQLCondition.Type type;

   public SQLCondition(@NonNull SQLValue value, @NonNull SQLCondition.Type type) {
      this.value = value;
      this.type = type;
   }

   @NonNull
   public static SQLCondition of(@NonNull SQLValue value, @NonNull SQLCondition.Type type) {
      return new SQLCondition(value, type);
   }

   @NonNull
   public static SQLCondition equal(@NonNull SQLValue value) {
      return of(value, SQLCondition.Type.EQUAL);
   }

   @NonNull
   public static SQLCondition not(@NonNull SQLValue value) {
      return of(value, SQLCondition.Type.NOT_EQUAL);
   }

   @NonNull
   public static SQLCondition smaller(@NonNull SQLValue value) {
      return of(value, SQLCondition.Type.SMALLER);
   }

   @NonNull
   public static SQLCondition greater(@NonNull SQLValue value) {
      return of(value, SQLCondition.Type.GREATER);
   }

   @NonNull
   public SQLCondition.Type getType() {
      return this.type;
   }

   @NonNull
   public SQLValue getValue() {
      return this.value;
   }

   @NonNull
   public SQLColumn getColumn() {
      return this.getValue().getColumn();
   }

   public static enum Type {
      GREATER(">"),
      SMALLER("<"),
      EQUAL("="),
      NOT_EQUAL("!=");

      private final String operator;

      private Type(@NonNull String operator) {
         this.operator = operator;
      }

      @NonNull
      public String getOperator() {
         return this.operator;
      }
   }
}
