package com.sk89q.worldedit.internal.expression;

import com.sk89q.worldedit.antlr.ExpressionParser;
import com.sk89q.worldedit.antlr4.runtime.ParserRuleContext;
import com.sk89q.worldedit.antlr4.runtime.Token;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ExpressionHelper {
   public static final String WRAPPED_CONSTANT = "<wrapped constant>";

   public static void check(boolean condition, ParserRuleContext ctx, String message) {
      if (!condition) {
         throw evalException(ctx, message);
      }
   }

   public static void check(boolean condition, int positionInLine, String message) {
      if (!condition) {
         throw evalException(positionInLine, message);
      }
   }

   public static int getErrorPosition(Token token) {
      return token.getCharPositionInLine();
   }

   public static EvaluationException evalException(ParserRuleContext ctx, String message) {
      return evalException(ctx.start, message);
   }

   public static EvaluationException evalException(Token token, String message) {
      return evalException(getErrorPosition(token), message);
   }

   public static EvaluationException evalException(int positionInLine, String message) {
      return new EvaluationException(positionInLine, message);
   }

   public static void checkIterations(int iterations, int positionInLine) {
      check(iterations <= 256, positionInLine, "Loop exceeded 256 iterations");
   }

   public static MethodHandle resolveFunction(Functions functions, ExpressionParser.FunctionCallContext ctx) {
      String fnName = ctx.name.getText();
      Set<MethodHandle> matchingFns = functions.getMap().get(fnName);
      check(!matchingFns.isEmpty(), ctx, "Unknown function '" + fnName + "'");

      for (MethodHandle function : matchingFns) {
         if (function.isVarargsCollector()) {
            int nParams = function.type().parameterCount();
            int keptParams = nParams - 1;
            function = function.asCollector(function.type().parameterType(nParams - 1), ctx.args.size() - keptParams);
            function = function.asType(function.type().wrap());
         }

         MethodType type = function.type();
         if (type.parameterCount() == ctx.args.size()) {
            for (int i = 0; i < ctx.args.size(); i++) {
               ExpressionParser.ExpressionContext arg = ctx.args.get(i);
               getArgumentHandleName(fnName, type, i, arg);
            }

            return function;
         }
      }

      String possibleCounts = matchingFns.stream()
         .map(mh -> mh.isVarargsCollector() ? mh.type().parameterCount() - 1 + "+" : String.valueOf(mh.type().parameterCount()))
         .collect(Collectors.joining("/"));
      throw evalException(ctx, "Incorrect number of arguments for function '" + fnName + "', expected " + possibleCounts + ", got " + ctx.args.size());
   }

   public static String getArgumentHandleName(String fnName, MethodType type, int i, ParserRuleContext arg) {
      Class<?> pType = type.parameterType(i);
      Optional<String> id = tryResolveId(arg);
      if (pType == LocalSlot.Variable.class) {
         check(id.isPresent(), arg, "Function '" + fnName + "' requires a variable in parameter " + i);
         return id.get();
      } else {
         return pType == LocalSlot.class ? id.orElse("<wrapped constant>") : null;
      }
   }

   private static Optional<String> tryResolveId(ParserRuleContext arg) {
      Optional<ExpressionParser.WrappedExprContext> wrappedExprContext = tryAs(arg, ExpressionParser.WrappedExprContext.class);
      if (wrappedExprContext.isPresent()) {
         return tryResolveId(wrappedExprContext.get().expression());
      } else {
         Token token = arg.start;
         int tokenType = token.getType();
         boolean isId = arg.start == arg.stop && tokenType == 49;
         return isId ? Optional.of(token.getText()) : Optional.empty();
      }
   }

   private static <T extends ParserRuleContext> Optional<T> tryAs(ParserRuleContext ctx, Class<T> rule) {
      if (rule.isInstance(ctx)) {
         return Optional.of(rule.cast(ctx));
      } else if (ctx.children.size() != 1) {
         return Optional.empty();
      } else {
         List<ParserRuleContext> ctxs = ctx.getRuleContexts(ParserRuleContext.class);
         return ctxs.size() != 1 ? Optional.empty() : tryAs(ctxs.get(0), rule);
      }
   }

   private ExpressionHelper() {
   }
}
