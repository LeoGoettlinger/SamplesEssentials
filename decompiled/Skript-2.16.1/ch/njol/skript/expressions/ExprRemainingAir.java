package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Remaining Air")
@Description({"How much time a player has left underwater before starting to drown."})
@Example("if the player's remaining air is less than 3 seconds:\n\tsend \"hurry, get to the surface!\" to the player\n")
@Since({"2.0"})
public class ExprRemainingAir extends SimplePropertyExpression<LivingEntity, Timespan> {
   public Timespan convert(LivingEntity entity) {
      return new Timespan(Timespan.TimePeriod.TICK, Math.max(0, entity.getRemainingAir()));
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      switch (mode) {
         case ADD:
         case SET:
         case REMOVE:
         case DELETE:
         case RESET:
            return CollectionUtils.array(Timespan.class);
         default:
            return null;
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      long changeValue = delta != null ? ((Timespan)delta[0]).getAs(Timespan.TimePeriod.TICK) : 300L;
      if (mode == Changer.ChangeMode.REMOVE) {
         changeValue *= -1L;
      }

      for (LivingEntity entity : this.getExpr().getArray(event)) {
         long newRemainingAir = 0L;
         if (mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE) {
            newRemainingAir = entity.getRemainingAir();
         }

         newRemainingAir = Math.max(Math.min(newRemainingAir + changeValue, 2147483647L), 0L);
         entity.setRemainingAir((int)newRemainingAir);
      }
   }

   @Override
   public Class<Timespan> getReturnType() {
      return Timespan.class;
   }

   @Override
   protected String getPropertyName() {
      return "remaining air";
   }

   static {
      register(ExprRemainingAir.class, Timespan.class, "remaining air", "livingentities");
   }
}
