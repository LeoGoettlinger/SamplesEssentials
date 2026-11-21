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

@Name("Vectors - Normalized")
@Description({"Returns the same vector but with length 1."})
@Example.Examples({@Example("set {_v} to normalized {_v}"), @Example("set {_v} to normalised {_u}")})
@Since({"2.2-dev28"})
public class ExprVectorNormalize extends SimpleExpression<Vector> {
   private Expression<Vector> vector;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.vector = (Expression<Vector>)exprs[0];
      return true;
   }

   protected Vector[] get(Event event) {
      Vector vector = this.vector.getSingle(event);
      if (vector == null) {
         return null;
      } else {
         vector = vector.clone();
         if (!vector.isZero() && !vector.isNormalized()) {
            vector.normalize();
         }

         return CollectionUtils.array(vector);
      }
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
      return (Expression<? extends Vector>)(this.vector instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "normalized " + this.vector.toString(event, debug);
   }

   static {
      Skript.registerExpression(ExprVectorNormalize.class, Vector.class, ExpressionType.COMBINED, "normali(z|s)e[d] %vector%", "%vector% normali(z|s)ed");
   }
}
