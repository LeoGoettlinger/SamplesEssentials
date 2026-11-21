package org.skriptlang.skript.bukkit.breeding.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Love Time")
@Description({"The amount of time the animals have been in love for. Using a value of 30 seconds is equivalent to using an item to breed them.", "Only works on animals that can be bred and returns '0 seconds' for animals that can't be bred."})
@Example("on right click:\n\tsend \"%event-entity% has been in love for %love time of event-entity% more than you!\" to player\n")
@Since({"2.10"})
public class ExprLoveTime extends SimplePropertyExpression<LivingEntity, Timespan> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprLoveTime.class, Timespan.class, "love[d] time", "livingentities", false)
               .supplier(ExprLoveTime::new))
            .build()
      );
   }

   @Nullable
   public Timespan convert(LivingEntity entity) {
      return entity instanceof Animals animal ? new Timespan(Timespan.TimePeriod.TICK, animal.getLoveModeTicks()) : new Timespan(0L);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET -> (Class[])CollectionUtils.array(Timespan.class);
         case ADD, REMOVE -> (Class[])CollectionUtils.array(Timespan[].class);
         case RESET -> (Class[])CollectionUtils.array();
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int changeTicks = 0;
      if (delta != null) {
         for (Object object : delta) {
            changeTicks += (int)((Timespan)object).getAs(Timespan.TimePeriod.TICK);
         }
      }

      for (LivingEntity livingEntity : this.getExpr().getArray(event)) {
         if (livingEntity instanceof Animals animal) {
            int loveTicks = animal.getLoveModeTicks();
            switch (mode) {
               case SET:
                  loveTicks = changeTicks;
                  break;
               case ADD:
                  loveTicks += changeTicks;
                  break;
               case REMOVE:
                  loveTicks -= changeTicks;
                  break;
               case RESET:
                  loveTicks = 0;
            }

            animal.setLoveModeTicks(Math.max(loveTicks, 0));
         }
      }
   }

   @Override
   public Class<? extends Timespan> getReturnType() {
      return Timespan.class;
   }

   @Override
   protected String getPropertyName() {
      return "love time";
   }
}
