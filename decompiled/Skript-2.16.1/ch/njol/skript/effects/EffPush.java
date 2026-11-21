package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import java.util.function.Function;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Push")
@Description({"Push entities in a given direction or towards a specific location."})
@Example.Examples({@Example("push the player upwards"), @Example("push the victim downwards at speed 0.5"), @Example("push player towards player's target at speed 2"), @Example("pull player along vector(1,1,1) at speed 1.5")})
@Since({"1.4.6", "2.12 (push towards)"})
public class EffPush extends Effect {
   private Expression<Entity> entities;
   @Nullable
   private Expression<Direction> direction;
   @Nullable
   private Expression<Location> target;
   private boolean awayFrom = false;
   @Nullable
   private Expression<Number> speed = null;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.entities = (Expression<Entity>)exprs[0];
      if (matchedPattern == 0) {
         this.direction = (Expression<Direction>)exprs[1];
      } else {
         this.target = (Expression<Location>)exprs[1];
         this.awayFrom = parseResult.hasTag("away");
      }

      this.speed = (Expression<Number>)exprs[2];
      return true;
   }

   @Override
   protected void execute(Event event) {
      Number speed = this.speed != null ? this.speed.getSingle(event) : null;
      if (this.speed == null || speed != null) {
         Function<Entity, Vector> getDirection;
         if (this.direction != null) {
            Direction direction = this.direction.getSingle(event);
            if (direction == null) {
               return;
            }

            getDirection = direction::getDirection;
         } else {
            assert this.target != null;

            Location target = this.target.getSingle(event);
            if (target == null) {
               return;
            }

            Vector targetVector = target.toVector();
            getDirection = entityx -> {
               Vector direction = targetVector.subtract(entityx.getLocation().toVector());
               if (this.awayFrom) {
                  direction.multiply(-1);
               }

               return direction;
            };
         }

         Entity[] entities = this.entities.getArray(event);

         for (Entity entity : entities) {
            Vector pushDirection = getDirection.apply(entity);
            if (speed != null) {
               pushDirection.normalize().multiply(speed.doubleValue());
            }

            if (!Double.isFinite(pushDirection.getX()) || !Double.isFinite(pushDirection.getY()) || !Double.isFinite(pushDirection.getZ())) {
               return;
            }

            entity.setVelocity(entity.getVelocity().add(pushDirection));
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder ssb = new SyntaxStringBuilder(event, debug).append("push", this.entities);
      if (this.direction != null) {
         ssb.append(this.direction);
      } else {
         assert this.target != null;

         if (this.awayFrom) {
            ssb.append("away from", this.target);
         } else {
            ssb.append("towards", this.target);
         }
      }

      if (this.speed != null) {
         ssb.append("at a speed of", this.speed);
      }

      return ssb.toString();
   }

   static {
      Skript.registerEffect(
         EffPush.class,
         "(push|thrust|pull) %entities% [along] %direction% [(at|with) [a] (speed|velocity|force) [of] %-number%]",
         "(push|thrust|pull) %entities% (towards|away:away from) %location% [(at|with) [a] (speed|velocity|force) [of] %-number%]"
      );
   }
}
