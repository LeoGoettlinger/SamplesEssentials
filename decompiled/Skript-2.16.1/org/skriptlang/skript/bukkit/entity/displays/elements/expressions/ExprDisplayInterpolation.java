package org.skriptlang.skript.bukkit.entity.displays.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Display Interpolation Delay/Duration")
@Description({"Returns or changes the interpolation delay/duration of <a href='#display'>displays</a>.", "Interpolation duration is the amount of time a display will take to interpolate, or shift, between its current state and a new state.", "Interpolation delay is the amount of ticks before client-side interpolation will commence.Setting to 0 seconds will make it immediate.", "Resetting either value will return that value to 0."})
@Example("set interpolation delay of the last spawned text display to 2 ticks")
@Since({"2.10"})
public class ExprDisplayInterpolation extends SimplePropertyExpression<Display, Timespan> {
   private boolean delay;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprDisplayInterpolation.class, Timespan.class, "interpolation (:delay|duration)[s]", "displays", true
               )
               .supplier(ExprDisplayInterpolation::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.delay = parseResult.hasTag("delay");
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Timespan convert(Display display) {
      return new Timespan(Timespan.TimePeriod.TICK, this.delay ? display.getInterpolationDelay() : display.getInterpolationDuration());
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, REMOVE, SET -> (Class[])CollectionUtils.array(Timespan.class);
         case RESET -> (Class[])CollectionUtils.array();
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Display[] displays = this.getExpr().getArray(event);
      long ticks = 0L;
      if (delta != null) {
         ticks = Math2.fit(-2147483648L, ((Timespan)delta[0]).getAs(Timespan.TimePeriod.TICK), 2147483647L);
      }

      switch (mode) {
         case REMOVE:
            ticks = -ticks;
         case ADD:
            for (Display displayx : displays) {
               if (this.delay) {
                  int value = (int)Math2.fit(0L, displayx.getInterpolationDelay() + ticks, 2147483647L);
                  displayx.setInterpolationDelay(value);
               } else {
                  int value = (int)Math2.fit(0L, displayx.getInterpolationDuration() + ticks, 2147483647L);
                  displayx.setInterpolationDuration(value);
               }
            }
            break;
         case SET:
         case RESET:
            ticks = Math2.fit(0L, ticks, 2147483647L);

            for (Display display : displays) {
               if (this.delay) {
                  display.setInterpolationDelay((int)ticks);
               } else {
                  display.setInterpolationDuration((int)ticks);
               }
            }
      }
   }

   @Override
   public Class<? extends Timespan> getReturnType() {
      return Timespan.class;
   }

   @Override
   protected String getPropertyName() {
      return "interpolation " + (this.delay ? "delay" : "duration");
   }
}
