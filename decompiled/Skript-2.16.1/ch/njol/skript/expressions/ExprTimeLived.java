package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Time Lived of Entity")
@Description({"Returns the total amount of time the entity has lived.\nNote: This does not reset when a player dies.\n"})
@Example.Examples({@Example("clear all entities where [input's time lived > 1 hour]"), @Example("on right click on entity:\n\tsend \"%entity% has lived for %time lived of entity%\" to player\n")})
@Since({"2.13"})
public class ExprTimeLived extends SimplePropertyExpression<Entity, Timespan> {
   @Nullable
   public Timespan convert(Entity entity) {
      return new Timespan(Timespan.TimePeriod.TICK, entity.getTicksLived());
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, REMOVE, SET, RESET -> (Class[])CollectionUtils.array(Timespan.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      long newTicks = 1L;
      if (delta != null && delta[0] instanceof Timespan timespan) {
         newTicks = (int)timespan.get(Timespan.TimePeriod.TICK);
      }

      for (Entity entity : this.getExpr().getArray(event)) {
         int currentTicks = entity.getTicksLived();

         long valueToSet = switch (mode) {
            case ADD -> currentTicks + newTicks;
            case REMOVE -> currentTicks - newTicks;
            case SET, RESET -> newTicks;
            default -> currentTicks;
         };
         entity.setTicksLived((int)Math2.fit(1L, valueToSet, 2147483647L));
      }
   }

   @Override
   public Class<? extends Timespan> getReturnType() {
      return Timespan.class;
   }

   @Override
   protected String getPropertyName() {
      return "time lived";
   }

   static {
      register(ExprTimeLived.class, Timespan.class, "time (alive|lived)", "entities");
   }
}
