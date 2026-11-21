package github.nighter.smartspawner.libs.mariadb;

public enum TransactionIsolation {
   REPEATABLE_READ("REPEATABLE-READ", 4),
   READ_COMMITTED("READ-COMMITTED", 2),
   READ_UNCOMMITTED("READ-UNCOMMITTED", 1),
   SERIALIZABLE("SERIALIZABLE", 8);

   private final String value;
   private final int level;

   private TransactionIsolation(String value, int level) {
      this.value = value;
      this.level = level;
   }

   public static TransactionIsolation from(String value) {
      for (TransactionIsolation transactionIsolation : values()) {
         if (transactionIsolation.value.replaceAll("[ \\-_]", "").equalsIgnoreCase(value.replaceAll("[ \\-_]", ""))) {
            return transactionIsolation;
         }
      }

      throw new IllegalArgumentException(String.format("Wrong argument value '%s' for TransactionIsolation", value));
   }

   public String getValue() {
      return this.value;
   }

   public int getLevel() {
      return this.level;
   }
}
