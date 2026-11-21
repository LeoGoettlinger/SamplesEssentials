package com.artillexstudios.axafkzone.libs.axapi.utils;

import org.jspecify.annotations.NonNull;

public class SimplePattern {
   private final String expression;
   private final boolean endsWith;
   private final boolean startsWith;

   public static SimplePattern of(@NonNull String expression) {
      return new SimplePattern(expression.isBlank() ? "%%" : expression);
   }

   private SimplePattern(@NonNull String expression) {
      String modifiableExpression = expression;
      if (expression.charAt(0) == '%') {
         this.endsWith = true;
         modifiableExpression = expression.substring(1);
      } else {
         this.endsWith = false;
      }

      if (expression.charAt(expression.length() - 1) == '%') {
         this.startsWith = true;
         modifiableExpression = modifiableExpression.substring(0, modifiableExpression.length() - 1);
      } else {
         this.startsWith = false;
      }

      this.expression = modifiableExpression;
   }

   public boolean matches(@NonNull String other) {
      if (this.endsWith && this.startsWith) {
         return other.contains(this.expression);
      } else if (this.endsWith) {
         return other.endsWith(this.expression);
      } else {
         return this.startsWith ? other.startsWith(this.expression) : other.equals(this.expression);
      }
   }
}
