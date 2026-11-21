package org.skriptlang.skript.bukkit.fishing.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Fishing Wait Time")
@Description({"Returns the minimum and/or maximum waiting time of the fishing hook. ", "Default minimum value is 5 seconds and maximum is 30 seconds, before lure is applied."})
@Example("on fishing line cast:\n\tset min fish waiting time to 10 seconds\n\tset max fishing waiting time to 20 seconds\n")
@Events({"Fishing"})
@Since({"2.10"})
public class ExprFishingWaitTime extends SimpleExpression<Timespan> {
   private static final int DEFAULT_MINIMUM_TICKS = 100;
   private static final int DEFAULT_MAXIMUM_TICKS = 600;
   private boolean isMin;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprFishingWaitTime.class, Timespan.class
                     )
                     .addPatterns(new String[]{"(min:min[imum]|max[imum]) fish[ing] wait[ing] time"}))
                  .supplier(ExprFishingWaitTime::new))
               .priority(EventValueExpression.DEFAULT_PRIORITY))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (!this.getParser().isCurrentEvent(PlayerFishEvent.class)) {
         Skript.error("The 'fishing wait time' expression can only be used in a fishing event.");
         return false;
      } else {
         this.isMin = parseResult.hasTag("min");
         return true;
      }
   }

   @Nullable
   protected Timespan[] get(Event event) {
      if (event instanceof PlayerFishEvent fishEvent) {
         return this.isMin
            ? new Timespan[]{new Timespan(Timespan.TimePeriod.TICK, fishEvent.getHook().getMinWaitTime())}
            : new Timespan[]{new Timespan(Timespan.TimePeriod.TICK, fishEvent.getHook().getMaxWaitTime())};
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, REMOVE, SET, RESET -> new Class[]{Timespan.class};
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof PlayerFishEvent fishEvent) {
         FishHook hook = fishEvent.getHook();
         int ticks = mode == Changer.ChangeMode.RESET ? (this.isMin ? 100 : 600) : (int)((Timespan)delta[0]).getAs(Timespan.TimePeriod.TICK);
         switch (mode) {
            case ADD:
               if (this.isMin) {
                  hook.setMinWaitTime(Math.max(0, hook.getMinWaitTime() + ticks));
               } else {
                  hook.setMaxWaitTime(Math.max(0, hook.getMaxWaitTime() + ticks));
               }
               break;
            case REMOVE:
               if (this.isMin) {
                  hook.setMinWaitTime(Math.max(0, hook.getMinWaitTime() - ticks));
               } else {
                  hook.setMaxWaitTime(Math.max(0, hook.getMaxWaitTime() - ticks));
               }
               break;
            case SET:
            case RESET:
               if (this.isMin) {
                  hook.setMinWaitTime(Math.max(0, ticks));
               } else {
                  hook.setMaxWaitTime(Math.max(0, ticks));
               }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Timespan> getReturnType() {
      return Timespan.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return (this.isMin ? "minimum" : "maximum") + " fishing waiting time";
   }
}
