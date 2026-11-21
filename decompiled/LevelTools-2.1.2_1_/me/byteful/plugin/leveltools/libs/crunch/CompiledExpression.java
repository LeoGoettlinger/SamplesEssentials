package me.byteful.plugin.leveltools.libs.crunch;

import me.byteful.plugin.leveltools.libs.crunch.exceptions.ExpressionEvaluationException;
import me.byteful.plugin.leveltools.libs.crunch.token.Value;

public class CompiledExpression {
   protected double[] variableValues;
   private int variableCount;
   private Value value;

   protected CompiledExpression() {
   }

   public CompiledExpression(Value value, int variableCount) {
      this.initialize(value, variableCount);
   }

   protected void initialize(Value value, int variableCount) {
      this.value = value;
      this.variableCount = variableCount;
   }

   protected void setVariableValues(double[] values) {
      this.checkArgCount(values.length);
      this.variableValues = values;
   }

   public Value getValue() {
      return this.value;
   }

   public int getVariableCount() {
      return this.variableCount;
   }

   public double evaluate(double... values) {
      this.setVariableValues(values);
      return this.value.getValue(this.variableValues);
   }

   public double evaluate() {
      this.checkArgCount(0);
      return this.value.getValue(this.variableValues);
   }

   public double evaluate(double first) {
      this.checkArgCount(1);
      if (this.variableValues == null) {
         this.variableValues = new double[1];
      }

      this.variableValues[0] = first;
      return this.value.getValue(this.variableValues);
   }

   public double evaluate(double first, double second) {
      this.checkArgCount(2);
      if (this.variableValues == null) {
         this.variableValues = new double[2];
      }

      this.variableValues[0] = first;
      this.variableValues[1] = second;
      return this.value.getValue(this.variableValues);
   }

   private void checkArgCount(int args) {
      if (this.variableCount > args) {
         throw new ExpressionEvaluationException("Too few variable values - expected " + this.variableCount + ", got " + args);
      }
   }

   public CompiledExpression clone() {
      return new CompiledExpression(this.value, this.variableCount);
   }

   @Override
   public String toString() {
      return this.value.toString();
   }
}
