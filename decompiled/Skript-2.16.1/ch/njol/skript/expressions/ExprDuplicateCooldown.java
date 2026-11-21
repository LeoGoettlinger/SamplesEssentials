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
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Allay Duplication Cooldown")
@Description({"The cooldown time until an allay can duplicate again naturally.", "Resetting the cooldown time will set the cooldown time to the same amount of time after an allay has duplicated."})
@Example.Examples({@Example("set {_time} to the duplicate cooldown of last spawned allay"), @Example("add 5 seconds to the duplication cool down time of last spawned allay"), @Example("remove 3 seconds from the duplicating cooldown time of last spawned allay"), @Example("clear the clone cool down of last spawned allay"), @Example("reset the cloning cool down time of last spawned allay")})
@Since({"2.11"})
public class ExprDuplicateCooldown extends SimplePropertyExpression<LivingEntity, Timespan> {
   @Nullable
   public Timespan convert(LivingEntity entity) {
      return entity instanceof Allay allay ? new Timespan(Timespan.TimePeriod.TICK, allay.getDuplicationCooldown()) : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, ADD, REMOVE, DELETE, RESET -> (Class[])CollectionUtils.array(Timespan.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      long ticks = delta == null ? 0L : ((Timespan)delta[0]).getAs(Timespan.TimePeriod.TICK);
      ticks = Math2.fit(0L, ticks, Long.MAX_VALUE);

      for (LivingEntity entity : this.getExpr().getArray(event)) {
         if (entity instanceof Allay allay) {
            switch (mode) {
               case SET:
               case DELETE:
                  allay.setDuplicationCooldown(ticks);
                  break;
               case ADD: {
                  long current = allay.getDuplicationCooldown();
                  long value = Math2.fit(0L, current + ticks, Long.MAX_VALUE);
                  allay.setDuplicationCooldown(value);
                  break;
               }
               case REMOVE: {
                  long current = allay.getDuplicationCooldown();
                  long value = Math2.fit(0L, current - ticks, Long.MAX_VALUE);
                  allay.setDuplicationCooldown(value);
                  break;
               }
               case RESET:
                  allay.resetDuplicationCooldown();
            }
         }
      }
   }

   @Override
   public Class<Timespan> getReturnType() {
      return Timespan.class;
   }

   @Override
   protected String getPropertyName() {
      return "duplicate cooldown time";
   }

   static {
      registerDefault(ExprDuplicateCooldown.class, Timespan.class, "(duplicat(e|ing|ion)|clon(e|ing)) cool[ ]down [time]", "livingentities");
   }
}
