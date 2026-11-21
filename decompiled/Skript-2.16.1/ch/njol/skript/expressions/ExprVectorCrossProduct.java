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

@Name("Vectors - Cross Product")
@Description({"Gets the cross product between two vectors."})
@Example("send \"%vector 1, 0, 0 cross vector 0, 1, 0%\"")
@Since({"2.2-dev28"})
public class ExprVectorCrossProduct extends SimpleExpression<Vector> {
   private Expression<Vector> first;
   private Expression<Vector> second;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.first = (Expression<Vector>)exprs[0];
      this.second = (Expression<Vector>)exprs[1];
      return true;
   }

   protected Vector[] get(Event event) {
      Vector first = this.first.getSingle(event);
      Vector second = this.second.getSingle(event);
      return first != null && second != null ? CollectionUtils.array(first.clone().crossProduct(second)) : null;
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Vector> getReturnType() {
      return Vector.class;
   }

   @Override
   public Expression<? extends Vector> simplify() {
      return (Expression<? extends Vector>)(this.first instanceof Literal && this.second instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.first.toString(event, debug) + " cross " + this.second.toString(event, debug);
   }

   static {
      Skript.registerExpression(ExprVectorCrossProduct.class, Vector.class, ExpressionType.COMBINED, "%vector% cross %vector%");
   }
}
