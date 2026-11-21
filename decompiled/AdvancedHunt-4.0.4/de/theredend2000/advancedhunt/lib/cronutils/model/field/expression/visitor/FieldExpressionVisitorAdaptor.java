package de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Always;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.And;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Between;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.QuestionMark;

public class FieldExpressionVisitorAdaptor implements FieldExpressionVisitor {
   @Override
   public FieldExpression visit(Always always) {
      return this.caseDefault(always);
   }

   @Override
   public FieldExpression visit(And and) {
      return this.caseDefault(and);
   }

   @Override
   public FieldExpression visit(Between between) {
      return this.caseDefault(between);
   }

   @Override
   public FieldExpression visit(Every every) {
      return this.caseDefault(every);
   }

   @Override
   public FieldExpression visit(On on) {
      return this.caseDefault(on);
   }

   @Override
   public FieldExpression visit(QuestionMark questionMark) {
      return this.caseDefault(questionMark);
   }

   protected <T extends FieldExpression> T caseDefault(T expression) {
      return expression;
   }
}
