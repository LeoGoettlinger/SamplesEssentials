package de.theredend2000.advancedhunt.lib.cronutils.descriptor;

import de.theredend2000.advancedhunt.lib.cronutils.Function;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

class NominalDescriptionStrategy extends DescriptionStrategy {
   private FieldExpression expression;
   private final Set<Function<FieldExpression, String>> descriptions = new HashSet<>();

   public NominalDescriptionStrategy(ResourceBundle bundle, Function<Integer, String> nominalValueFunction, FieldExpression expression) {
      super(bundle);
      if (nominalValueFunction != null) {
         this.nominalValueFunction = nominalValueFunction;
      }

      if (expression != null) {
         this.expression = expression;
      } else {
         this.expression = FieldExpression.always();
      }
   }

   @Override
   public String describe() {
      for (Function<FieldExpression, String> function : this.descriptions) {
         if (!"".equals(function.apply(this.expression))) {
            return function.apply(this.expression);
         }
      }

      return this.describe(this.expression);
   }

   public NominalDescriptionStrategy addDescription(Function<FieldExpression, String> desc) {
      this.descriptions.add(desc);
      return this;
   }
}
