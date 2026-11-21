package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

@Name("World")
@Description({"The world the event occurred in."})
@Example.Examples({@Example("world is \"world_nether\""), @Example("teleport the player to the world's spawn"), @Example("set the weather in the player's world to rain"), @Example("set {_world} to world of event-chunk")})
@Since({"1.0"})
public class ExprWorld extends PropertyExpression<Object, World> {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      this.setExpr((Expression<? extends Object>)exprs[0]);
      return true;
   }

   protected World[] get(Event event, Object[] source) {
      return this.get(source, objInWorld -> {
         if (event instanceof PlayerTeleportEvent playerTeleportEvent) {
            if (this.getTime() == EventValues.TIME_FUTURE) {
               return playerTeleportEvent.getTo().getWorld();
            }

            if (this.getTime() == EventValues.TIME_PAST) {
               return playerTeleportEvent.getFrom().getWorld();
            }
         }

         if (objInWorld instanceof Entity entity) {
            return entity.getWorld();
         } else if (objInWorld instanceof Location location) {
            return location.getWorld();
         } else if (objInWorld instanceof Chunk chunk) {
            return chunk.getWorld();
         } else {
            assert false : objInWorld;

            return null;
         }
      });
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET && this.getExpr().canReturn(Location.class) ? CollectionUtils.array(World.class) : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (delta != null) {
         for (Object object : this.getExpr().getArray(event)) {
            if (object instanceof Location location) {
               location.setWorld((World)delta[0]);
            }
         }
      }
   }

   @Override
   public boolean setTime(int time) {
      return super.setTime(time, this.getExpr(), new Class[]{PlayerTeleportEvent.class});
   }

   @Override
   public Class<World> getReturnType() {
      return World.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the world" + (this.getExpr().isDefault() ? "" : " of " + this.getExpr().toString(event, debug));
   }

   static {
      Skript.registerExpression(
         ExprWorld.class, World.class, ExpressionType.PROPERTY, "[the] world [of %locations/entities/chunk%]", "%locations/entities/chunk%'[s] world"
      );
   }
}
