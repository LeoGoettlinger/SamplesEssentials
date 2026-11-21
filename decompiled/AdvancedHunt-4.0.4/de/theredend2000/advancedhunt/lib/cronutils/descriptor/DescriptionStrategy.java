package de.theredend2000.advancedhunt.lib.cronutils.descriptor;

import de.theredend2000.advancedhunt.lib.cronutils.Function;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Always;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.And;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Between;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.FieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.IntegerFieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

abstract class DescriptionStrategy {
   private static final String EVERY = "every";
   private static final String WHITE_SPACE = " ";
   protected Function<Integer, String> nominalValueFunction;
   protected ResourceBundle bundle;

   public DescriptionStrategy(ResourceBundle bundle) {
      this.bundle = bundle;
      this.nominalValueFunction = integer -> " " + integer;
   }

   public abstract String describe();

   protected String describe(FieldExpression fieldExpression) {
      return this.describe(fieldExpression, false);
   }

   protected String describe(FieldExpression fieldExpression, boolean and) {
      Preconditions.checkNotNull(fieldExpression, "CronFieldExpression should not be null!");
      if (fieldExpression instanceof Always) {
         return this.describe((Always)fieldExpression, and);
      } else if (fieldExpression instanceof And) {
         return this.describe((And)fieldExpression);
      } else if (fieldExpression instanceof Between) {
         return this.describe((Between)fieldExpression, and);
      } else if (fieldExpression instanceof Every) {
         return this.describe((Every)fieldExpression, and);
      } else {
         return fieldExpression instanceof On ? this.describe((On)fieldExpression, and) : "";
      }
   }

   protected String describe(Always always, boolean and) {
      return "";
   }

   protected String describe(And and) {
      List<FieldExpression> expressions = new ArrayList<>();
      List<FieldExpression> onExpressions = new ArrayList<>();

      for (FieldExpression fieldExpression : and.getExpressions()) {
         if (fieldExpression instanceof On) {
            onExpressions.add(fieldExpression);
         } else {
            expressions.add(fieldExpression);
         }
      }

      StringBuilder builder = new StringBuilder();
      if (!onExpressions.isEmpty()) {
         if (onExpressions.size() == 1) {
            builder.append(this.bundle.getString("at"));
            builder.append(" %s ");
            this.createAndDescription(builder, onExpressions);
         } else {
            builder.append(this.bundle.getString("at"));
            this.createAndDescription(builder, onExpressions).append(" replace_plural ");
         }
      }

      if (!expressions.isEmpty()) {
         if (!onExpressions.isEmpty()) {
            builder.append(String.format(" %s ", this.bundle.getString("and")));
         }

         this.createAndDescription(builder, expressions);
      }

      return builder.toString();
   }

   protected String describe(Between between, boolean and) {
      return this.bundle.getString("every")
         + " %s "
         + MessageFormat.format(this.bundle.getString("between_x_and_y"), this.nominalValue(between.getFrom()), this.nominalValue(between.getTo()))
         + " ";
   }

   protected String describe(Every every, boolean and) {
      String description;
      if (every.getPeriod().getValue() > 1) {
         description = String.format("%s %s ", this.bundle.getString("every"), this.nominalValue(every.getPeriod())) + " replace_plural ";
      } else {
         description = this.bundle.getString("every") + " %s ";
      }

      if (every.getExpression() instanceof Between) {
         Between between = (Between)every.getExpression();
         description = description
            + MessageFormat.format(this.bundle.getString("between_x_and_y"), this.nominalValue(between.getFrom()), this.nominalValue(between.getTo()))
            + " ";
      }

      return description;
   }

   protected String describe(On on, boolean and) {
      return and ? this.nominalValue(on.getTime()) : String.format("%s %s ", this.bundle.getString("at"), this.nominalValue(on.getTime())) + "%s";
   }

   protected String nominalValue(FieldValue<?> fieldValue) {
      Preconditions.checkNotNull(fieldValue, "FieldValue must not be null");
      return fieldValue instanceof IntegerFieldValue ? this.nominalValueFunction.apply(((IntegerFieldValue)fieldValue).getValue()) : fieldValue.toString();
   }

   private StringBuilder createAndDescription(StringBuilder builder, List<FieldExpression> expressions) {
      if (expressions.size() > 1) {
         for (int j = 0; j < expressions.size() - 2; j++) {
            builder.append(String.format(" %s, ", this.describe(expressions.get(j), true)));
         }

         builder.append(String.format(" %s ", this.describe(expressions.get(expressions.size() - 2), true)));
         builder.append(String.format(" %s ", this.bundle.getString("and")));
      }

      builder.append(String.format(" %s ", this.describe(expressions.get(expressions.size() - 1), true)));
      return builder;
   }
}
