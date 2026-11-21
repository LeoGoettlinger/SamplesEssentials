package de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Always;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.And;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Between;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.QuestionMark;

public interface FieldExpressionVisitor {
   FieldExpression visit(Always var1);

   FieldExpression visit(And var1);

   FieldExpression visit(Between var1);

   FieldExpression visit(Every var1);

   FieldExpression visit(On var1);

   FieldExpression visit(QuestionMark var1);
}
