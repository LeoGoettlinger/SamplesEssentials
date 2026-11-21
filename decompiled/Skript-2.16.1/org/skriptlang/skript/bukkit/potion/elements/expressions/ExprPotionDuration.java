package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect - Duration")
@Description({"An expression to obtain the duration of a potion effect."})
@Example.Examples({@Example("set the duration of {_potion} to 10 seconds"), @Example("add 10 seconds to the duration of the player's speed effect")})
@Since({"2.14"})
public class ExprPotionDuration extends SimplePropertyExpression<SkriptPotionEffect, Timespan> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprPotionDuration.class, Timespan.class, "([potion] duration|potion length)[s]", "skriptpotioneffects", true
               )
               .supplier(ExprPotionDuration::new))
            .build()
      );
   }

   public Timespan convert(SkriptPotionEffect potionEffect) {
      return potionEffect.infinite() ? Timespan.infinite() : new Timespan(Timespan.TimePeriod.TICK, potionEffect.duration());
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (!SkriptPotionEffect.isChangeable(this.getExpr())) {
         return null;
      } else {
         return switch (mode) {
            case ADD, SET, REMOVE, RESET -> (Class[])CollectionUtils.array(Timespan.class);
            default -> null;
         };
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Timespan change = delta != null ? (Timespan)delta[0] : new Timespan(Timespan.TimePeriod.TICK, 600L);

      for (SkriptPotionEffect potionEffect : this.getExpr().getArray(event)) {
         changeSafe(potionEffect, change, mode);
      }
   }

   @Internal
   public static void changeSafe(SkriptPotionEffect potionEffect, Timespan change, Changer.ChangeMode mode) {
      Timespan duration;
      if (mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET) {
         if (potionEffect.infinite()) {
            return;
         }

         duration = new Timespan(Timespan.TimePeriod.TICK, potionEffect.duration());
         if (mode == Changer.ChangeMode.ADD) {
            duration = duration.add(change);
         } else {
            duration = duration.subtract(change);
         }
      } else {
         duration = change;
      }

      if (duration.isInfinite()) {
         potionEffect.infinite(true);
      } else {
         potionEffect.duration((int)Math2.fit(0L, duration.getAs(Timespan.TimePeriod.TICK), 2147483647L));
      }
   }

   @Override
   public Class<? extends Timespan> getReturnType() {
      return Timespan.class;
   }

   @Override
   protected String getPropertyName() {
      return "duration";
   }
}
