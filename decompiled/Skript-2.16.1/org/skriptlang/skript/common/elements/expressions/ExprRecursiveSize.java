package org.skriptlang.skript.common.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Recursive Size")
@Description({"The recursive size of list.", "Returns the recursive size of the list with sublists included, e.g.", "", "<pre>", "{list::*} Structure<br>", "  ├──── {list::1}: 1<br>", "  ├──── {list::2}: 2<br>", "  │     ├──── {list::2::1}: 3<br>", "  │     │    └──── {list::2::1::1}: 4<br>", "  │     └──── {list::2::2}: 5<br>", "  └──── {list::3}: 6", "</pre>", "", "Where using %size of {list::*}% will only return 3 (the first layer of indices only), while %recursive size of {list::*}% will return 6 (the entire list)", "Please note that getting a list's recursive size can cause lag if the list is large, so only use this expression if you need to!"})
@Example("if recursive size of {player-data::*} > 1000:")
@Since({"1.0"})
public class ExprRecursiveSize extends SimpleExpression<Long> {
   private ExpressionList<?> exprs;

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprRecursiveSize.class, Long.class
                     )
                     .supplier(ExprRecursiveSize::new))
                  .priority(PropertyExpression.DEFAULT_PRIORITY))
               .addPattern("[the] recursive (amount|number|size) of %objects%"))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.exprs = expressions[0] instanceof ExpressionList<?> exprList
         ? exprList
         : new ExpressionList<>(new Expression[]{expressions[0]}, Object.class, false);
      this.exprs = (ExpressionList<?>)LiteralUtils.defendExpression(this.exprs);
      if (!LiteralUtils.canInitSafely(this.exprs)) {
         return false;
      } else if (this.exprs.isSingle()) {
         Skript.error(
            "'"
               + this.exprs.toString(null, Skript.debug())
               + "' can only ever have one value at most, thus the 'recursive size of ...' expression is useless. Use '... exists' instead to find out whether the expression has a value."
         );
         return false;
      } else {
         for (Expression<?> expr : this.exprs.getExpressions()) {
            if (!(expr instanceof Variable)) {
               Skript.error(
                  "Getting the recursive size of a list only applies to variables, thus the '"
                     + expr.toString(null, Skript.debug())
                     + "' expression is useless."
               );
               return false;
            }
         }

         return true;
      }
   }

   @Nullable
   protected Long[] get(Event event) {
      int currentSize = 0;

      for (Expression<?> expr : this.exprs.getExpressions()) {
         Object var = ((Variable)expr).getRaw(event);
         if (var != null) {
            currentSize += getRecursiveSize((Map<?, ?>)var);
         }
      }

      return new Long[]{(long)currentSize};
   }

   private static int getRecursiveSize(Map<?, ?> map) {
      return getRecursiveSize(map, true);
   }

   private static int getRecursiveSize(Map<?, ?> map, boolean skipNull) {
      int count = 0;

      for (Entry<?, ?> entry : map.entrySet()) {
         if (!skipNull || entry.getKey() != null) {
            if (entry.getValue() instanceof Map<?, ?> nestedMap) {
               count += getRecursiveSize(nestedMap, false);
            } else {
               count++;
            }
         }
      }

      return count;
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Long> getReturnType() {
      return Long.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "recursive size of " + this.exprs.toString(event, debug);
   }
}
