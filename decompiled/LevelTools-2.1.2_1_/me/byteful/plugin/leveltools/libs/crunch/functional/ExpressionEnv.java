package me.byteful.plugin.leveltools.libs.crunch.functional;

import java.util.Locale;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;
import me.byteful.plugin.leveltools.libs.crunch.Variable;
import me.byteful.plugin.leveltools.libs.crunch.data.CharTree;
import me.byteful.plugin.leveltools.libs.crunch.token.BinaryOperator;
import me.byteful.plugin.leveltools.libs.crunch.token.Constant;
import me.byteful.plugin.leveltools.libs.crunch.token.LazyVariable;
import me.byteful.plugin.leveltools.libs.crunch.token.Token;
import me.byteful.plugin.leveltools.libs.crunch.token.UnaryOperator;
import me.byteful.plugin.leveltools.libs.crunch.token.Value;

public class ExpressionEnv {
   private final CharTree<BinaryOperator> binaryOperators = new CharTree<>();
   private final CharTree<Token> leadingOperators = new CharTree<>();
   private final CharTree<Value> values = new CharTree<>();
   private int varCount = 0;

   public ExpressionEnv() {
      for (BinaryOperator operator : BinaryOperator.values()) {
         this.binaryOperators.set(operator.getSymbol(), operator);
      }

      for (UnaryOperator operator : UnaryOperator.values()) {
         this.leadingOperators.set(operator.getSymbol(), operator);
      }

      for (Constant constant : Constant.values()) {
         this.values.set(constant.toString().toLowerCase(Locale.ROOT), constant);
      }
   }

   private void checkName(String name) {
      if (name == null || name.isEmpty()) {
         throw new IllegalArgumentException("Identifier cannot be empty or null");
      } else if (!Character.isAlphabetic(name.charAt(0))) {
         throw new IllegalArgumentException("Identifier must begin with an alphabetic character");
      }
   }

   public ExpressionEnv addFunction(Function function) {
      if (function == null) {
         throw new IllegalArgumentException("Function cannot be null");
      } else {
         String name = function.getName();
         this.checkName(name);
         this.leadingOperators.set(name, function);
         return this;
      }
   }

   public ExpressionEnv addFunctions(Function... functions) {
      if (functions == null) {
         throw new IllegalArgumentException("Functions cannot be null");
      } else {
         for (Function function : functions) {
            this.addFunction(function);
         }

         return this;
      }
   }

   public ExpressionEnv addLazyVariable(String name, DoubleSupplier supply) {
      if (supply == null) {
         throw new IllegalArgumentException("Supply cannot be null");
      } else {
         this.checkName(name);
         this.values.set(name, new LazyVariable(name, supply));
         return this;
      }
   }

   public ExpressionEnv setVariableNames(String... names) {
      if (names == null) {
         throw new IllegalArgumentException("Names cannot be null");
      } else {
         this.varCount = names.length;

         for (int i = 0; i < names.length; i++) {
            this.checkName(names[i]);
            this.values.set(names[i], new Variable(i));
         }

         return this;
      }
   }

   public ExpressionEnv addFunction(String name, int argCount, ToDoubleFunction<double[]> func) {
      this.addFunction(new Function(name, argCount, func));
      return this;
   }

   public CharTree<Token> getLeadingOperators() {
      return this.leadingOperators;
   }

   public CharTree<BinaryOperator> getBinaryOperators() {
      return this.binaryOperators;
   }

   public CharTree<Value> getValues() {
      return this.values;
   }

   public int getVariableCount() {
      return this.varCount;
   }
}
