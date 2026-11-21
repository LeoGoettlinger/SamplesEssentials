package ch.njol.skript.lang;

import ch.njol.util.StringUtils;
import java.util.List;
import org.jetbrains.annotations.Nullable;

final class DefaultExpressionUtils {
   @Nullable
   static DefaultExpressionUtils.DefaultExpressionError isValid(DefaultExpression<?> expr, SkriptParser.ExprInfo exprInfo, int index) {
      if (expr == null) {
         return DefaultExpressionUtils.DefaultExpressionError.NOT_FOUND;
      } else if (!(expr instanceof Literal) && (exprInfo.flagMask & 1) == 0) {
         return DefaultExpressionUtils.DefaultExpressionError.NOT_LITERAL;
      } else if (expr instanceof Literal && (exprInfo.flagMask & 2) == 0) {
         return DefaultExpressionUtils.DefaultExpressionError.LITERAL;
      } else if (!exprInfo.isPlural[index] && !expr.isSingle()) {
         return DefaultExpressionUtils.DefaultExpressionError.NOT_SINGLE;
      } else {
         return exprInfo.time != 0 && !expr.setTime(exprInfo.time) ? DefaultExpressionUtils.DefaultExpressionError.TIME_STATE : null;
      }
   }

   static enum DefaultExpressionError {
      NOT_FOUND {
         @Override
         public String getError(List<String> codeNames, String pattern) {
            StringBuilder builder = new StringBuilder();
            String combinedComma = DefaultExpressionUtils.DefaultExpressionError.getCombinedComma(codeNames);
            String combinedSlash = StringUtils.join(codeNames, "/");
            builder.append(DefaultExpressionUtils.DefaultExpressionError.plurality(codeNames, "The class '", "The classes '"));
            builder.append(combinedComma)
               .append("'")
               .append(DefaultExpressionUtils.DefaultExpressionError.plurality(codeNames, " does ", " do "))
               .append("not provide a default expression. Either allow null (with %-")
               .append(combinedSlash)
               .append("%) or make it mandatory [pattern: ")
               .append(pattern)
               .append("]");
            return builder.toString();
         }
      },
      NOT_LITERAL {
         @Override
         public String getError(List<String> codeNames, String pattern) {
            StringBuilder builder = new StringBuilder();
            builder.append(DefaultExpressionUtils.DefaultExpressionError.defaultExpression(codeNames, " is not a literal. ", " are not literals. "))
               .append("Either allow null (with %-*")
               .append(StringUtils.join(codeNames, "/"))
               .append("%) or make it mandatory [pattern: ")
               .append(pattern)
               .append("]");
            return builder.toString();
         }
      },
      LITERAL {
         @Override
         public String getError(List<String> codeNames, String pattern) {
            StringBuilder builder = new StringBuilder();
            builder.append(DefaultExpressionUtils.DefaultExpressionError.defaultExpression(codeNames, " is a literal. ", " are literals. "))
               .append("Either allow null (with %-~")
               .append(StringUtils.join(codeNames, "/"))
               .append("%) or make it mandatory [pattern: ")
               .append(pattern)
               .append("]");
            return builder.toString();
         }
      },
      NOT_SINGLE {
         @Override
         public String getError(List<String> codeNames, String pattern) {
            StringBuilder builder = new StringBuilder();
            builder.append(
                  DefaultExpressionUtils.DefaultExpressionError.defaultExpression(
                     codeNames, " is not a single-element expression. ", " are not single-element expressions. "
                  )
               )
               .append("Change your pattern to allow multiple elements or make the expression mandatory [pattern: ")
               .append(pattern)
               .append("]");
            return builder.toString();
         }
      },
      TIME_STATE {
         @Override
         public String getError(List<String> codeNames, String pattern) {
            StringBuilder builder = new StringBuilder(DefaultExpressionUtils.DefaultExpressionError.defaultExpression(codeNames, " does ", " do "));
            builder.append("not have distinct time states. [pattern: ").append(pattern).append("]");
            return builder.toString();
         }
      };

      public abstract String getError(List<String> var1, String var2);

      private static String defaultExpression(List<String> codeNames, String single, String plural) {
         StringBuilder builder = new StringBuilder();
         String combinedComma = getCombinedComma(codeNames);
         builder.append("The default ")
            .append(plurality(codeNames, "expression ", "expressions "))
            .append("of '")
            .append(combinedComma)
            .append("'")
            .append(plurality(codeNames, single, plural));
         return builder.toString();
      }

      private static String plurality(List<String> codeNames, String single, String plural) {
         return codeNames.size() > 1 ? plural : single;
      }

      private static String getCombinedComma(List<String> codeNames) {
         assert !codeNames.isEmpty();

         if (codeNames.size() == 1) {
            return codeNames.get(0);
         } else {
            return codeNames.size() == 2 ? StringUtils.join(codeNames, " and ") : StringUtils.join(codeNames, ", ", ", and ");
         }
      }
   }
}
