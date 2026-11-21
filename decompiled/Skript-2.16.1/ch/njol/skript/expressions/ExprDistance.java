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
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Distance")
@Description({"The distance between two points."})
@Example("if the distance between the player and {home::%uuid of player%} is smaller than 20:\n\tmessage \"You're very close to your home!\"\n")
@Since({"1.0"})
public class ExprDistance extends SimpleExpression<Number> {
   private Expression<Location> loc1;
   private Expression<Location> loc2;

   @Override
   public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.loc1 = (Expression<Location>)vars[0];
      this.loc2 = (Expression<Location>)vars[1];
      return true;
   }

   @Nullable
   protected Number[] get(Event event) {
      Location l1 = this.loc1.getSingle(event);
      Location l2 = this.loc2.getSingle(event);
      if (l1 != null && l2 != null) {
         if (l1.getWorld() != l2.getWorld()) {
            this.error(
               "Cannot calculate the distance between locations from two different worlds! ("
                  + Classes.toString(l1.getWorld())
                  + " and "
                  + Classes.toString(l2.getWorld())
                  + ")"
            );
            return new Number[0];
         } else {
            return new Number[]{l1.distance(l2)};
         }
      } else {
         return new Number[0];
      }
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
      return (Expression<? extends Number>)(this.loc1 instanceof Literal && this.loc2 instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "distance between " + this.loc1.toString(event, debug) + " and " + this.loc2.toString(event, debug);
   }

   static {
      Skript.registerExpression(ExprDistance.class, Number.class, ExpressionType.COMBINED, "[the] distance between %location% and %location%");
   }
}
