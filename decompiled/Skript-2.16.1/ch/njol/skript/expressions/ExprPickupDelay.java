package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Pickup Delay")
@Description({"The amount of time before a dropped item can be picked up by an entity."})
@Example.Examples({@Example("drop diamond sword at {_location} without velocity"), @Example("set pickup delay of last dropped item to 5 seconds")})
@Since({"2.7"})
public class ExprPickupDelay extends SimplePropertyExpression<Entity, Timespan> {
   @Nullable
   public Timespan convert(Entity entity) {
      return !(entity instanceof Item) ? null : new Timespan(Timespan.TimePeriod.TICK, ((Item)entity).getPickupDelay());
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      switch (mode) {
         case SET:
         case ADD:
         case RESET:
         case DELETE:
         case REMOVE:
            return CollectionUtils.array(Timespan.class);
         default:
            return null;
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Entity[] entities = this.getExpr().getArray(event);
      int change = delta == null ? 0 : (int)((Timespan)delta[0]).getAs(Timespan.TimePeriod.TICK);
      switch (mode) {
         case SET:
         case RESET:
         case DELETE:
            for (Entity entity : entities) {
               if (entity instanceof Item) {
                  ((Item)entity).setPickupDelay(change);
               }
            }
            break;
         case REMOVE:
            change = -change;
         case ADD:
            for (Entity entityx : entities) {
               if (entityx instanceof Item item) {
                  item.setPickupDelay(item.getPickupDelay() + change);
               }
            }
            break;
         default:
            assert false;
      }
   }

   @Override
   public Class<? extends Timespan> getReturnType() {
      return Timespan.class;
   }

   @Override
   protected String getPropertyName() {
      return "pickup delay";
   }

   static {
      register(ExprPickupDelay.class, Timespan.class, "pick[ ]up delay", "entities");
   }
}
