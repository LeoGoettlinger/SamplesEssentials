package de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor;

import de.theredend2000.advancedhunt.lib.cronutils.Function;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Always;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.And;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Between;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.QuestionMark;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.FieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.IntegerFieldValue;

public class ValueMappingFieldExpressionVisitor implements FieldExpressionVisitor {
   private final Function<FieldValue<?>, FieldValue<?>> transform;

   public ValueMappingFieldExpressionVisitor(Function<FieldValue<?>, FieldValue<?>> transform) {
      this.transform = transform;
   }

   @Override
   public FieldExpression visit(Always always) {
      return always;
   }

   @Override
   public FieldExpression visit(And and) {
      And clone = new And();

      for (FieldExpression expression : and.getExpressions()) {
         clone.and(expression.accept(this));
      }

      return clone;
   }

   @Override
   public FieldExpression visit(Between between) {
      FieldValue<?> from = this.transform.apply(between.getFrom());
      FieldValue<?> to = this.transform.apply(between.getTo());
      return new Between(from, to);
   }

   @Override
   public FieldExpression visit(Every every) {
      return new Every((IntegerFieldValue)this.transform.apply(every.getPeriod()));
   }

   @Override
   public FieldExpression visit(On on) {
      return new On((IntegerFieldValue)this.transform.apply(on.getTime()), on.getSpecialChar(), on.getNth());
   }

   @Override
   public FieldExpression visit(QuestionMark questionMark) {
      return FieldExpression.questionMark();
   }
}
