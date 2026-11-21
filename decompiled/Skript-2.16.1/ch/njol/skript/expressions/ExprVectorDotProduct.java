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

@Name("Vectors - Dot Product")
@Description({"Gets the dot product between two vectors."})
@Example("set {_dot} to {_v1} dot {_v2}")
@Since({"2.2-dev28"})
public class ExprVectorDotProduct extends SimpleExpression<Number> {
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
      return first != null && second != null
         ? CollectionUtils.array(first.getX() * second.getX() + first.getY() * second.getY() + first.getZ() * second.getZ())
         : null;
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
      return this.first.toString(event, debug) + " dot " + this.second.toString(event, debug);
   }

   static {
      Skript.registerExpression(ExprVectorDotProduct.class, Number.class, ExpressionType.COMBINED, "%vector% dot %vector%");
   }
}
