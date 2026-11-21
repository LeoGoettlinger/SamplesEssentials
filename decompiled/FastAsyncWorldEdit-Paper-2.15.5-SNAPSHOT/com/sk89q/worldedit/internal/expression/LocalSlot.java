package com.sk89q.worldedit.internal.expression;

public interface LocalSlot {
   double value();

   public record Constant(double value) implements LocalSlot {
      @Override
      public String toString() {
         return String.valueOf(this.value);
      }
   }

   public static final class Variable implements LocalSlot {
      private double value;

      public Variable(double value) {
         this.value = value;
      }

      public void setValue(double value) {
         this.value = value;
      }

      @Override
      public double value() {
         return this.value;
      }

      @Override
      public String toString() {
         return String.valueOf(this.value);
      }
   }
}
