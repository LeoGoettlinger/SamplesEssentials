package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Vectors - Create Location from Vector")
@Description({"Creates a location from a vector in a world."})
@Example.Examples({@Example("set {_loc} to {_v} to location in world \"world\""), @Example("set {_loc} to {_v} to location in world \"world\" with yaw 45 and pitch 90"), @Example("set {_loc} to location of {_v} in \"world\" with yaw 45 and pitch 90")})
@Since({"2.2-dev28"})
public class ExprLocationFromVector extends SimpleExpression<Location> {
   private Expression<Vector> vector;
   private Expression<World> world;
   @Nullable
   private Expression<Number> yaw;
   @Nullable
   private Expression<Number> pitch;
   private boolean hasDirection;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (exprs.length > 3) {
         this.hasDirection = true;
      }

      this.vector = (Expression<Vector>)exprs[0];
      this.world = (Expression<World>)exprs[1];
      if (this.hasDirection) {
         this.yaw = (Expression<Number>)exprs[2];
         this.pitch = (Expression<Number>)exprs[3];
      }

      return true;
   }

   protected Location[] get(Event event) {
      Vector vector = this.vector.getSingle(event);
      World world = this.world.getSingle(event);
      if (vector != null && world != null) {
         if (this.hasDirection) {
            assert this.yaw != null && this.pitch != null;

            Number yaw = this.yaw.getSingle(event);
            Number pitch = this.pitch.getSingle(event);
            if (yaw != null || pitch != null) {
               return CollectionUtils.array(vector.toLocation(world, yaw == null ? 0.0F : yaw.floatValue(), pitch == null ? 0.0F : pitch.floatValue()));
            }
         }

         return CollectionUtils.array(vector.toLocation(world));
      } else {
         return null;
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Location> getReturnType() {
      return Location.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.hasDirection
         ? "location of "
            + this.vector.toString(event, debug)
            + " in "
            + this.world.toString(event, debug)
            + " with yaw "
            + this.yaw.toString(event, debug)
            + " and pitch "
            + this.pitch.toString(event, debug)
         : "location of " + this.vector.toString(event, debug) + " in " + this.world.toString(event, debug);
   }

   static {
      Skript.registerExpression(
         ExprLocationFromVector.class,
         Location.class,
         ExpressionType.COMBINED,
         "%vector% to location in %world%",
         "location (from|of) %vector% in %world%",
         "%vector% [to location] in %world% with yaw %number% and pitch %number%",
         "location (from|of) %vector% in %world% with yaw %number% and pitch %number%"
      );
   }
}
