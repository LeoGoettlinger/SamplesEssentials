package org.skriptlang.skript.lang.comparator;

public enum Relation {
   EQUAL("equal to"),
   NOT_EQUAL("not equal to"),
   GREATER("greater than"),
   GREATER_OR_EQUAL("greater than or equal to"),
   SMALLER("smaller than"),
   SMALLER_OR_EQUAL("smaller than or equal to");

   private final String toString;

   private Relation(String toString) {
      this.toString = toString;
   }

   public static Relation get(boolean b) {
      return b ? EQUAL : NOT_EQUAL;
   }

   public static Relation get(int i) {
      return i == 0 ? EQUAL : (i > 0 ? GREATER : SMALLER);
   }

   public static Relation get(double d) {
      return d == 0.0 ? EQUAL : (d > 0.0 ? GREATER : SMALLER);
   }

   public boolean isImpliedBy(Relation other) {
      if (other == this) {
         return true;
      } else {
         switch (this) {
            case EQUAL:
            case GREATER:
            case SMALLER:
               return false;
            case NOT_EQUAL:
               return other == SMALLER || other == GREATER;
            case GREATER_OR_EQUAL:
               return other == GREATER || other == EQUAL;
            case SMALLER_OR_EQUAL:
               return other == SMALLER || other == EQUAL;
            default:
               throw new IllegalStateException("Unexpected value: " + this);
         }
      }
   }

   public boolean isImpliedBy(Relation... others) {
      for (Relation other : others) {
         if (this.isImpliedBy(other)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public String toString() {
      return this.toString;
   }

   public Relation getInverse() {
      switch (this) {
         case EQUAL:
            return NOT_EQUAL;
         case NOT_EQUAL:
            return EQUAL;
         case GREATER:
            return SMALLER_OR_EQUAL;
         case GREATER_OR_EQUAL:
            return SMALLER;
         case SMALLER:
            return GREATER_OR_EQUAL;
         case SMALLER_OR_EQUAL:
            return GREATER;
         default:
            throw new IllegalStateException("Unexpected value: " + this);
      }
   }

   public Relation getSwitched() {
      switch (this) {
         case EQUAL:
            return EQUAL;
         case NOT_EQUAL:
            return NOT_EQUAL;
         case GREATER:
            return SMALLER;
         case GREATER_OR_EQUAL:
            return SMALLER_OR_EQUAL;
         case SMALLER:
            return GREATER;
         case SMALLER_OR_EQUAL:
            return GREATER_OR_EQUAL;
         default:
            throw new IllegalStateException("Unexpected value: " + this);
      }
   }

   public int getRelation() {
      switch (this) {
         case EQUAL:
         case NOT_EQUAL:
            return 0;
         case GREATER:
         case GREATER_OR_EQUAL:
            return 1;
         case SMALLER:
         case SMALLER_OR_EQUAL:
            return -1;
         default:
            throw new IllegalStateException("Unexpected value: " + this);
      }
   }
}
