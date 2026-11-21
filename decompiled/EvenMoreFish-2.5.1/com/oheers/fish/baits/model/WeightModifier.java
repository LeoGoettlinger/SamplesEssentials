package com.oheers.fish.baits.model;

import java.util.Locale;
import org.jspecify.annotations.NonNull;

public record WeightModifier(@NonNull WeightModifier.Operation operation, double value, @NonNull String expression) {
   public static final WeightModifier IDENTITY = new WeightModifier(WeightModifier.Operation.ADD, 0.0, "+0");

   @NonNull
   public static WeightModifier parse(Object rawValue) {
      if (rawValue == null) {
         return IDENTITY;
      } else if (rawValue instanceof Number number) {
         return add(number.doubleValue());
      } else {
         String input = rawValue.toString().trim();
         if (input.isEmpty()) {
            return IDENTITY;
         } else {
            char prefix = input.charAt(0);
            if (prefix != '+' && prefix != '-' && prefix != '*' && prefix != '/') {
               return add(parseDouble(input, input));
            } else {
               double value = parseDouble(input.substring(1).trim(), input);

               return switch (prefix) {
                  case '*' -> new WeightModifier(WeightModifier.Operation.MULTIPLY, value, input);
                  case '+' -> new WeightModifier(WeightModifier.Operation.ADD, value, input);
                  default -> throw new IllegalStateException("Unexpected value: " + prefix);
                  case '-' -> new WeightModifier(WeightModifier.Operation.SUBTRACT, value, input);
                  case '/' -> new WeightModifier(WeightModifier.Operation.DIVIDE, value, input);
               };
            }
         }
      }
   }

   @NonNull
   public static WeightModifier add(double value) {
      return new WeightModifier(WeightModifier.Operation.ADD, value, formatExpression('+', value));
   }

   @NonNull
   public static WeightModifier multiply(double value) {
      return new WeightModifier(WeightModifier.Operation.MULTIPLY, value, formatExpression('*', value));
   }

   public double apply(double baseWeight) {
      double adjustedWeight = switch (this.operation) {
         case ADD -> baseWeight + this.value;
         case SUBTRACT -> baseWeight - this.value;
         case MULTIPLY -> baseWeight * this.value;
         case DIVIDE -> this.value == 0.0 ? 0.0 : baseWeight / this.value;
      };
      return Math.max(0.0, adjustedWeight);
   }

   public boolean isIdentity() {
      return this.operation == WeightModifier.Operation.ADD && this.value == 0.0;
   }

   @NonNull
   public String describe() {
      return this.expression;
   }

   private static double parseDouble(@NonNull String value, @NonNull String originalInput) {
      try {
         return Double.parseDouble(value);
      } catch (NumberFormatException var3) {
         throw new IllegalArgumentException("Invalid weight modifier: " + originalInput, var3);
      }
   }

   @NonNull
   private static String formatExpression(char operator, double value) {
      return "%c%s".formatted(operator, formatNumber(value));
   }

   @NonNull
   private static String formatNumber(double value) {
      return value == Math.rint(value)
         ? Long.toString(Math.round(value))
         : String.format(Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
   }

   public static enum Operation {
      ADD,
      SUBTRACT,
      MULTIPLY,
      DIVIDE;
   }
}
