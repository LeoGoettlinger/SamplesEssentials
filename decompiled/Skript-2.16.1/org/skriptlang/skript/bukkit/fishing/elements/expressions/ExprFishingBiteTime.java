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

@Name("Fishing Bite Time")
@Description({"Returns the time it takes a fish to bite the fishing hook, after it started approaching the hook.", "May return a timespan of 0 seconds. If modifying the value, it should be at least 1 tick."})
@Example("on fish approach:\n\tset fishing bite time to 5 seconds\n")
@Events({"Fishing"})
@Since({"2.10"})
public class ExprFishingBiteTime extends SimpleExpression<Timespan> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprFishingBiteTime.class, Timespan.class
                     )
                     .addPatterns(new String[]{"fish[ing] bit(e|ing) [wait] time"}))
                  .supplier(ExprFishingBiteTime::new))
               .priority(EventValueExpression.DEFAULT_PRIORITY))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (!this.getParser().isCurrentEvent(PlayerFishEvent.class)) {
         Skript.error("The 'fishing bite time' expression can only be used in a fishing event.");
         return false;
      } else {
         return true;
      }
   }

   @Nullable
   protected Timespan[] get(Event event) {
      return event instanceof PlayerFishEvent fishEvent ? new Timespan[]{new Timespan(Timespan.TimePeriod.TICK, fishEvent.getHook().getTimeUntilBite())} : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, REMOVE, SET -> new Class[]{Timespan.class};
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof PlayerFishEvent fishEvent) {
         FishHook hook = fishEvent.getHook();

         assert delta != null;

         int ticks = (int)((Timespan)delta[0]).getAs(Timespan.TimePeriod.TICK);
         switch (mode) {
            case ADD:
               hook.setTimeUntilBite(Math.max(1, hook.getTimeUntilBite() + ticks));
               break;
            case REMOVE:
               hook.setTimeUntilBite(Math.max(1, hook.getTimeUntilBite() - ticks));
               break;
            case SET:
               hook.setTimeUntilBite(Math.max(1, ticks));
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
      return "fishing bite time";
   }
}
