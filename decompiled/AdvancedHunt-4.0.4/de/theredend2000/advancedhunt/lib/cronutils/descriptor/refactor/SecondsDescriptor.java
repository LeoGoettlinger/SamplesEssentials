package de.theredend2000.advancedhunt.lib.cronutils.descriptor.refactor;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Always;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.And;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Between;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.QuestionMark;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor.FieldExpressionVisitor;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.FieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.IntegerFieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import de.theredend2000.advancedhunt.lib.cronutils.utils.VisibleForTesting;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

class SecondsDescriptor implements FieldExpressionVisitor {
   private static final String EVERY = "every";
   protected ResourceBundle bundle;

   SecondsDescriptor(ResourceBundle bundle) {
      this.bundle = bundle;
   }

   public String describe(FieldExpression fieldExpression) {
      return this.describe(fieldExpression, false);
   }

   protected String describe(FieldExpression fieldExpression, boolean and) {
      Preconditions.checkNotNull(fieldExpression, "CronFieldExpression should not be null!");
      if (fieldExpression instanceof Always) {
         return this.describe(fieldExpression, and);
      } else if (fieldExpression instanceof And) {
         return this.describe((And)fieldExpression);
      } else if (fieldExpression instanceof Between) {
         return this.describe(fieldExpression, and);
      } else if (fieldExpression instanceof Every) {
         return this.describe((Every)fieldExpression, and);
      } else {
         return fieldExpression instanceof On ? this.describe((On)fieldExpression, and) : "";
      }
   }

   protected String describe(Always always, boolean and) {
      return this.bundle.getString("every");
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
         builder.append(this.bundle.getString("at"));
         this.createAndDescription(builder, onExpressions);
      }

      if (!expressions.isEmpty()) {
         this.createAndDescription(builder, expressions);
      }

      return builder.toString();
   }

   protected String describe(Between between, boolean and) {
      return MessageFormat.format(this.bundle.getString("between_x_and_y"), this.nominalValue(between.getFrom()), this.nominalValue(between.getTo())) + " ";
   }

   protected String describe(Every every, boolean and) {
      return null;
   }

   protected String describe(On on, boolean and) {
      return and ? this.nominalValue(on.getTime()) : String.format("%s %s ", this.bundle.getString("at"), this.nominalValue(on.getTime())) + "%s";
   }

   protected String nominalValue(FieldValue<?> fieldValue) {
      Preconditions.checkNotNull(fieldValue, "FieldValue must not be null");
      return fieldValue instanceof IntegerFieldValue ? "" + ((IntegerFieldValue)fieldValue).getValue() : fieldValue.toString();
   }

   @VisibleForTesting
   StringBuilder createAndDescription(StringBuilder builder, List<FieldExpression> expressions) {
      if (expressions.size() - 2 >= 0) {
         for (int j = 0; j < expressions.size() - 2; j++) {
            builder.append(String.format(" %s, ", this.describe(expressions.get(j), true)));
         }

         builder.append(String.format(" %s ", this.describe(expressions.get(expressions.size() - 2), true)));
      }

      builder.append(String.format(" %s ", this.bundle.getString("and")));
      builder.append(this.describe(expressions.get(expressions.size() - 1), true));
      return builder;
   }

   public Always visit(Always always) {
      return null;
   }

   public And visit(And and) {
      return null;
   }

   public Between visit(Between between) {
      return null;
   }

   public Every visit(Every every) {
      if (every.getPeriod().getValue() > 1) {
         String description = String.format("%s %s ", this.bundle.getString("every"), this.nominalValue(every.getPeriod())) + " replace_plural ";
      } else {
         String description = this.bundle.getString("every") + " %s ";
      }

      return every;
   }

   public On visit(On on) {
      return null;
   }

   public QuestionMark visit(QuestionMark questionMark) {
      return null;
   }
}
