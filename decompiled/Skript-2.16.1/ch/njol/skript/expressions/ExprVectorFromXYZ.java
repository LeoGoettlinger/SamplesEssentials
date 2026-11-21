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

@Name("Vectors - Create from XYZ")
@Description({"Creates a vector from x, y and z values."})
@Example("set {_v} to vector 0, 1, 0")
@Since({"2.2-dev28"})
public class ExprVectorFromXYZ extends SimpleExpression<Vector> {
   private Expression<Number> x;
   private Expression<Number> y;
   private Expression<Number> z;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.x = (Expression<Number>)exprs[0];
      this.y = (Expression<Number>)exprs[1];
      this.z = (Expression<Number>)exprs[2];
      return true;
   }

   protected Vector[] get(Event event) {
      Number x = this.x.getSingle(event);
      Number y = this.y.getSingle(event);
      Number z = this.z.getSingle(event);
      return x != null && y != null && z != null ? CollectionUtils.array(new Vector(x.doubleValue(), y.doubleValue(), z.doubleValue())) : null;
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
      return (Expression<? extends Vector>)(this.x instanceof Literal && this.y instanceof Literal && this.z instanceof Literal
         ? SimplifiedLiteral.fromExpression(this)
         : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "vector from x " + this.x.toString(event, debug) + ", y " + this.y.toString(event, debug) + ", z " + this.z.toString(event, debug);
   }

   static {
      Skript.registerExpression(
         ExprVectorFromXYZ.class, Vector.class, ExpressionType.COMBINED, "[a] [new] vector [(from|at|to)] %number%,[ ]%number%(,[ ]| and )%number%"
      );
   }
}
