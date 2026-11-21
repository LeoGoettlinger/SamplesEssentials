package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.Pathfinder.PathResult;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Pathfinding")
@Description({"Checks whether living entities are pathfinding.", "Can only be a living entity that is a Mob."})
@Example("make {_entity} pathfind to {_location} at speed 2\nwhile {_entity} is pathfinding\n\twait a second\nlaunch flickering trailing burst firework colored red at location of {_entity}\nsubtract 10 from {defence::tower::health}\nclear entity within {_entity}\n")
@Since({"2.9.0"})
public class CondIsPathfinding extends Condition {
   private Expression<LivingEntity> entities;
   private Expression<?> target;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.entities = (Expression<LivingEntity>)expressions[0];
      this.target = expressions[1];
      this.setNegated(matchedPattern == 1);
      return true;
   }

   @Override
   public boolean check(Event event) {
      return this.entities.check(event, entity -> {
         if (!(entity instanceof Mob)) {
            return false;
         } else {
            Pathfinder pathfind = ((Mob)entity).getPathfinder();
            if (this.target == null) {
               return pathfind.hasPath();
            } else {
               PathResult current = pathfind.getCurrentPath();
               Object target = this.target.getSingle(event);
               if (target != null && current != null) {
                  Location location = current.getFinalPoint();
                  if (target instanceof Location) {
                     return location.equals(target);
                  } else {
                     assert target instanceof LivingEntity;

                     LivingEntity entityTarget = (LivingEntity)target;
                     return location.distance(((Mob)entityTarget).getLocation()) < 1.0;
                  }
               } else {
                  return false;
               }
            }
         }
      }, this.isNegated());
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return PropertyCondition.toString(
         this,
         PropertyCondition.PropertyType.BE,
         event,
         debug,
         this.entities,
         "pathfinding" + this.target == null ? "" : " to " + this.target.toString(event, debug)
      );
   }

   static {
      if (Skript.classExists("org.bukkit.entity.Mob") && Skript.methodExists(Mob.class, "getPathfinder")) {
         PropertyCondition.register(CondIsPathfinding.class, "pathfinding [to[wards] %-livingentity/location%]", "livingentities");
      }
   }
}
