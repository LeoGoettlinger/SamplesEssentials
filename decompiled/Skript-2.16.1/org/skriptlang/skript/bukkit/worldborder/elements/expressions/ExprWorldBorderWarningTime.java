package org.skriptlang.skript.bukkit.worldborder.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Warning Time of World Border")
@Description({"The warning time of a world border. If the border is shrinking, the player's screen will be tinted red once the border will catch the player within this time period."})
@Example("set world border warning time of {_worldborder} to 1 second")
@Since({"2.11"})
public class ExprWorldBorderWarningTime extends SimplePropertyExpression<WorldBorder, Timespan> {
   private static final boolean USE_DEPRECATED = !Skript.methodExists(WorldBorder.class, "getWarningTimeTicks");

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprWorldBorderWarningTime.class, Timespan.class, "world[ ]border warning time", "worldborders", true
               )
               .supplier(ExprWorldBorderWarningTime::new))
            .build()
      );
   }

   @Nullable
   public Timespan convert(WorldBorder worldBorder) {
      return USE_DEPRECATED
         ? new Timespan(Timespan.TimePeriod.SECOND, worldBorder.getWarningTime())
         : new Timespan(Timespan.TimePeriod.TICK, worldBorder.getWarningTimeTicks());
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, ADD, REMOVE, RESET -> (Class[])CollectionUtils.array(Timespan.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      long input;
      if (USE_DEPRECATED) {
         input = delta == null ? 15L : ((Timespan)delta[0]).getAs(Timespan.TimePeriod.SECOND);
      } else {
         input = delta == null ? 300L : ((Timespan)delta[0]).getAs(Timespan.TimePeriod.TICK);
      }

      for (WorldBorder worldBorder : this.getExpr().getArray(event)) {
         long warningTime = switch (mode) {
            case SET, RESET -> input;
            case ADD -> USE_DEPRECATED ? Math2.addClamped(worldBorder.getWarningTime(), input) : Math2.addClamped(worldBorder.getWarningTimeTicks(), input);
            case REMOVE -> USE_DEPRECATED
               ? Math2.addClamped(worldBorder.getWarningTime(), -input)
               : Math2.addClamped(worldBorder.getWarningTimeTicks(), -input);
            default -> throw new IllegalStateException();
         };
         setWarningTime(worldBorder, warningTime);
      }
   }

   private static void setWarningTime(WorldBorder worldBorder, long inputTime) {
      if (USE_DEPRECATED) {
         long time = Math2.multiplyClamped(inputTime, 20L);
         int warningTime = (int)Math2.fit(0L, time, 2147483647L) / 20;
         worldBorder.setWarningTime(warningTime);
      } else {
         int warningTime = (int)Math2.fit(0L, inputTime, 2147483647L);
         worldBorder.setWarningTimeTicks(warningTime);
      }
   }

   @Override
   public Class<? extends Timespan> getReturnType() {
      return Timespan.class;
   }

   @Override
   protected String getPropertyName() {
      return "world border warning time";
   }
}
