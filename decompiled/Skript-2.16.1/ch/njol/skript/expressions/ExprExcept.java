package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

@Name("Except")
@Description({"Filter a list by providing objects to be excluded."})
@Example.Examples({@Example("spawn zombie at location(0, 0, 0):\n\thide entity from all players except {_player}\n"), @Example("set {_items::*} to a copper ingot, an iron ingot and a gold ingot\nset {_except::*} to {_items::*} excluding copper ingot\n")})
@Since({"2.12"})
public class ExprExcept extends WrapperExpression<Object> {
   private Expression<?> exclude;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      Expression<?> source = LiteralUtils.defendExpression(exprs[0]);
      this.setExpr((Expression<? extends Object>)source);
      if (source.isSingle() && !(source instanceof ExpressionList)) {
         Skript.error("Must provide a list containing more than one object to exclude objects from.");
         return false;
      } else {
         this.exclude = LiteralUtils.defendExpression(exprs[1]);
         return LiteralUtils.canInitSafely(source, this.exclude);
      }
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      Object[] exclude = this.exclude.getArray(event);
      return exclude.length == 0 ? this.getExpr().getArray(event) : this.getExpr().streamAll(event).filter(sourceObject -> {
         for (Object excludeObject : exclude) {
            if (sourceObject.equals(excludeObject) || Comparators.compare(sourceObject, excludeObject) == Relation.EQUAL) {
               return false;
            }
         }

         return true;
      }).toArray(o -> (Object[])Array.newInstance(this.getReturnType(), o));
   }

   @Override
   public Expression<?> simplify() {
      this.setExpr((Expression<? extends Object>)this.getExpr().simplify());
      return (Expression<?>)(this.getExpr() instanceof Literal && this.exclude instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return new SyntaxStringBuilder(event, debug).append(this.getExpr(), "except", this.exclude).toString();
   }

   static {
      Skript.registerExpression(ExprExcept.class, Object.class, ExpressionType.COMBINED, "%objects% (except|excluding|not including) %objects%");
   }
}
