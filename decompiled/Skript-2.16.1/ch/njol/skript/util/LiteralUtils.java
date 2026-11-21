package ch.njol.skript.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.registrations.Classes;
import java.util.stream.Stream;

public class LiteralUtils {
   public static <T> Expression<T> defendExpression(Expression<?> expr) {
      if (!(expr instanceof ExpressionList)) {
         if (expr instanceof UnparsedLiteral) {
            Literal<?> parsedLiteral = ((UnparsedLiteral)expr).getConvertedExpression(Object.class);
            return (Expression<T>)(parsedLiteral == null ? expr : parsedLiteral);
         } else {
            return (Expression<T>)expr;
         }
      } else {
         Expression<?>[] oldExpressions = ((ExpressionList)expr).getExpressions();
         Expression<? extends T>[] newExpressions = new Expression[oldExpressions.length];
         Class<?>[] returnTypes = new Class[oldExpressions.length];

         for (int i = 0; i < oldExpressions.length; i++) {
            newExpressions[i] = defendExpression(oldExpressions[i]);
            returnTypes[i] = newExpressions[i].getReturnType();
         }

         return new ExpressionList<>(newExpressions, (Class<T>)Classes.getSuperClassInfo(returnTypes).getC(), returnTypes, expr.getAnd());
      }
   }

   public static boolean hasUnparsedLiteral(Expression<?> expr) {
      if (expr instanceof UnparsedLiteral) {
         return true;
      } else {
         return expr instanceof ExpressionList exprList ? Stream.of(exprList.getExpressions()).anyMatch(LiteralUtils::hasUnparsedLiteral) : false;
      }
   }

   public static boolean canInitSafely(Expression<?>... expressions) {
      for (Expression<?> expression : expressions) {
         if (expression == null || hasUnparsedLiteral(expression)) {
            return false;
         }
      }

      return true;
   }
}
