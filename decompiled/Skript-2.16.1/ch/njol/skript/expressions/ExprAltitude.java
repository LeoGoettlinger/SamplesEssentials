package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import org.bukkit.Location;

@Name("Altitude")
@Description({"Effectively an alias of 'y-<a href='#ExprCoordinate'>coordinate</a> of …', it represents the height of some location within the world."})
@Example("on damage:\n\taltitude of the attacker is higher than the altitude of the victim\n\tset damage to damage * 1.2\n")
@Since({"1.4.3"})
public class ExprAltitude extends SimplePropertyExpression<Location, Number> {
   public Number convert(Location l) {
      return l.getY();
   }

   @Override
   protected String getPropertyName() {
      return "altitude";
   }

   @Override
   public Class<? extends Number> getReturnType() {
      return Number.class;
   }

   @Override
   public Expression<? extends Number> simplify() {
      return (Expression<? extends Number>)(this.getExpr() instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   static {
      register(ExprAltitude.class, Number.class, "altitude[s]", "locations");
   }
}
