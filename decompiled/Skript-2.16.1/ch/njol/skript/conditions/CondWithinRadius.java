package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Within Radius")
@Description({"Checks whether a location is within a certain radius of another location."})
@Example("on damage:\n\tif attacker's location is within 10 blocks around {_spawn}:\n\t\tcancel event\n\t\tsend \"You can't PVP in spawn.\"\n")
@Since({"2.7"})
public class CondWithinRadius extends Condition {
   private Expression<Location> locations;
   private Expression<Number> radius;
   private Expression<Location> points;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.locations = (Expression<Location>)exprs[0];
      this.radius = (Expression<Number>)exprs[1];
      this.points = (Expression<Location>)exprs[2];
      this.setNegated(matchedPattern == 1);
      return true;
   }

   @Override
   public boolean check(Event event) {
      double radius = this.radius.getOptionalSingle(event).orElse(0).doubleValue();
      double radiusSquared = radius * radius * 1.00001;
      return this.locations
         .check(
            event,
            location -> this.points
               .check(event, center -> !location.getWorld().equals(center.getWorld()) ? false : location.distanceSquared(center) <= radiusSquared),
            this.isNegated()
         );
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.locations.toString(event, debug)
         + (this.locations.isSingle() ? " is " : " are ")
         + (this.isNegated() ? " not " : "")
         + "within "
         + this.radius.toString(event, debug)
         + " blocks around "
         + this.points.toString(event, debug);
   }

   static {
      PropertyCondition.register(CondWithinRadius.class, "within %number% (block|metre|meter)[s] (around|of) %locations%", "locations");
   }
}
