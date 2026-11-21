package de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor;

import de.theredend2000.advancedhunt.lib.cronutils.StringValidations;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraints;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Always;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.And;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Between;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.QuestionMark;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.FieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.IntegerFieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.SpecialChar;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.SpecialCharFieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.utils.VisibleForTesting;

public class ValidationFieldExpressionVisitor implements FieldExpressionVisitor {
   private static final String OORANGE = "Value %s not in range [%s, %s]";
   private final FieldConstraints constraints;
   private final StringValidations stringValidations;

   public ValidationFieldExpressionVisitor(FieldConstraints constraints) {
      this.constraints = constraints;
      this.stringValidations = new StringValidations(constraints);
   }

   protected ValidationFieldExpressionVisitor(FieldConstraints constraints, StringValidations stringValidation) {
      this.constraints = constraints;
      this.stringValidations = stringValidation;
   }

   private void checkUnsupportedChars(FieldExpression expression) {
      String unsupportedChars = this.stringValidations.removeValidChars(expression.asString());
      if (!unsupportedChars.isEmpty()) {
         throw new IllegalArgumentException(
            String.format("Invalid chars in expression! Expression: %s Invalid chars: %s", expression.asString(), unsupportedChars)
         );
      }
   }

   public Always visit(Always always) {
      this.checkUnsupportedChars(always);
      return always;
   }

   public And visit(And and) {
      this.checkUnsupportedChars(and);

      for (FieldExpression expression : and.getExpressions()) {
         expression.accept(this);
      }

      return and;
   }

   public Between visit(Between between) {
      this.checkUnsupportedChars(between);
      this.preConditions(between);
      if (this.constraints.isStrictRange() && between.getFrom() instanceof IntegerFieldValue && between.getTo() instanceof IntegerFieldValue) {
         int from = ((IntegerFieldValue)between.getFrom()).getValue();
         int to = ((IntegerFieldValue)between.getTo()).getValue();
         if (from > to) {
            throw new IllegalArgumentException(String.format("Invalid range! [%s,%s]", from, to));
         }
      }

      return between;
   }

   public Every visit(Every every) {
      this.checkUnsupportedChars(every);
      if (every.getExpression() != null) {
         every.getExpression().accept(this);
      }

      this.isPeriodInRange(every.getPeriod());
      return every;
   }

   public On visit(On on) {
      this.checkUnsupportedChars(on);
      if (!this.isDefault(on.getTime())) {
         this.isInRange(on.getTime());
      }

      if (!this.isDefault(on.getNth())) {
         this.isInRange(on.getNth());
      }

      return on;
   }

   public QuestionMark visit(QuestionMark questionMark) {
      this.checkUnsupportedChars(questionMark);
      return questionMark;
   }

   private void preConditions(Between between) {
      this.isInRange(between.getFrom());
      this.isInRange(between.getTo());
      if (this.isSpecialCharNotL(between.getFrom()) || this.isSpecialCharNotL(between.getTo())) {
         throw new IllegalArgumentException("No special characters allowed in range, except for 'L'");
      }
   }

   @VisibleForTesting
   protected void isInRange(FieldValue<?> fieldValue) {
      if (fieldValue instanceof IntegerFieldValue) {
         int value = ((IntegerFieldValue)fieldValue).getValue();
         if (!this.constraints.isInRange(value)) {
            throw new IllegalArgumentException(
               String.format("Value %s not in range [%s, %s]", value, this.constraints.getStartRange(), this.constraints.getEndRange())
            );
         }
      }
   }

   @VisibleForTesting
   protected void isPeriodInRange(FieldValue<?> fieldValue) {
      if (fieldValue instanceof IntegerFieldValue) {
         int value = ((IntegerFieldValue)fieldValue).getValue();
         if (!this.constraints.isPeriodInRange(value)) {
            throw new IllegalArgumentException(
               String.format("Period %s not in range [%s, %s]", value, this.constraints.getStartRange(), this.constraints.getEndRange())
            );
         }
      }
   }

   @VisibleForTesting
   protected boolean isDefault(FieldValue<?> fieldValue) {
      return fieldValue instanceof IntegerFieldValue && ((IntegerFieldValue)fieldValue).getValue() == -1;
   }

   protected boolean isSpecialCharNotL(FieldValue<?> fieldValue) {
      return fieldValue instanceof SpecialCharFieldValue && !SpecialChar.L.equals(fieldValue.getValue());
   }
}
