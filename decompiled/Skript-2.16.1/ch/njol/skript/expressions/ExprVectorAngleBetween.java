package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Vectors - Angle Between")
@Description({"Gets the angle between two vectors."})
@Example("send \"%the angle between vector 1, 0, 0 and vector 0, 1, 1%\"")
@Since({"2.2-dev28"})
public class ExprVectorAngleBetween extends SimpleExpression<Number> {
   private static final float RAD_TO_DEG = (float) (180.0 / Math.PI);
   private Expression<Vector> first;
   private Expression<Vector> second;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.first = (Expression<Vector>)exprs[0];
      this.second = (Expression<Vector>)exprs[1];
      return true;
   }

   protected Number[] get(Event event) {
      Vector first = this.first.getSingle(event);
      Vector second = this.second.getSingle(event);
      return first != null && second != null ? CollectionUtils.array(first.angle(second) * (float) (180.0 / Math.PI)) : null;
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Number> getReturnType() {
      return Number.class;
   }

   @Override
   public Expression<? extends Number> simplify() {
      return (Expression<? extends Number>)(this.first instanceof Literal && this.second instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the angle between " + this.first.toString(event, debug) + " and " + this.second.toString(event, debug);
   }

   static {
      Skript.registerExpression(
         ExprVectorAngleBetween.class, Number.class, ExpressionType.COMBINED, "[the] angle between [[the] vectors] %vector% and %vector%"
      );
   }
}
