package org.jdbi.v3.core.transaction;

public enum TransactionIsolationLevel {
   NONE(0),
   READ_UNCOMMITTED(1),
   READ_COMMITTED(2),
   REPEATABLE_READ(4),
   SERIALIZABLE(8),
   UNKNOWN(Integer.MIN_VALUE);

   private final int value;

   private TransactionIsolationLevel(int value) {
      this.value = value;
   }

   public int intValue() {
      return this.value;
   }

   public static TransactionIsolationLevel valueOf(int val) {
      switch (val) {
         case 0:
            return NONE;
         case 1:
            return READ_UNCOMMITTED;
         case 2:
            return READ_COMMITTED;
         case 3:
         case 5:
         case 6:
         case 7:
         default:
            return UNKNOWN;
         case 4:
            return REPEATABLE_READ;
         case 8:
            return SERIALIZABLE;
      }
   }
}
