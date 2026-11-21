package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Max Minecart Speed")
@Description({"The maximum speed of a minecart."})
@Example("on right click on minecart:\n\tset max minecart speed of event-entity to 1\n")
@Since({"2.5.1"})
public class ExprMaxMinecartSpeed extends SimplePropertyExpression<Entity, Number> {
   @Nullable
   public Number convert(Entity entity) {
      return entity instanceof Minecart ? ((Minecart)entity).getMaxSpeed() : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      switch (mode) {
         case ADD:
         case REMOVE:
         case RESET:
         case SET:
            return CollectionUtils.array(Number.class);
         default:
            return null;
      }
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (delta == null) {
         if (mode == Changer.ChangeMode.RESET) {
            for (Entity entity : this.getExpr().getArray(e)) {
               if (entity instanceof Minecart) {
                  ((Minecart)entity).setMaxSpeed(0.4);
               }
            }
         }
      } else {
         int mod = 1;
         switch (mode) {
            case REMOVE:
               mod = -1;
            case ADD:
               for (Entity entityx : this.getExpr().getArray(e)) {
                  if (entityx instanceof Minecart minecart) {
                     minecart.setMaxSpeed(minecart.getMaxSpeed() + ((Number)delta[0]).doubleValue() * mod);
                  }
               }
               break;
            case RESET:
            default:
               assert false;
               break;
            case SET:
               for (Entity entityxx : this.getExpr().getArray(e)) {
                  if (entityxx instanceof Minecart) {
                     ((Minecart)entityxx).setMaxSpeed(((Number)delta[0]).doubleValue());
                  }
               }
         }
      }
   }

   @Override
   public Class<? extends Number> getReturnType() {
      return Number.class;
   }

   @Override
   protected String getPropertyName() {
      return "max minecart speed";
   }

   static {
      register(ExprMaxMinecartSpeed.class, Number.class, "max[imum] minecart (speed|velocity)", "entities");
   }
}
