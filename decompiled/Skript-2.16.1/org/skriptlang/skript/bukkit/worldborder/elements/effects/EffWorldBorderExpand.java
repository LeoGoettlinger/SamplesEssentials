package org.skriptlang.skript.bukkit.worldborder.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Expand/Shrink World Border")
@Description({"Expand or shrink the size of a world border.", "Using `by` adds/subtracts from the current size of the world border.", "Using `to` sets to the specified size."})
@Example.Examples({@Example("expand world border of player by 100 over 5 seconds"), @Example("shrink world border of world \"world\" to 100 over 10 seconds")})
@Since({"2.11"})
public class EffWorldBorderExpand extends Effect {
   private boolean shrink;
   private boolean radius;
   private boolean to;
   private Expression<WorldBorder> worldBorders;
   private Expression<Number> numberExpr;
   @Nullable
   private Expression<Timespan> timespan;
   private static final double MAX_WORLDBORDER_SIZE = 5.999997E7F;
   private static final boolean USE_DEPRECATED = !Skript.methodExists(WorldBorder.class, "changeSize");

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EFFECT,
         SyntaxInfo.builder(EffWorldBorderExpand.class)
            .supplier(EffWorldBorderExpand::new)
            .addPatterns(
               "(expand|grow) [[the] (diameter|:radius) of] %worldborders% (by|:to) %number% [over [a period of] %-timespan%]",
               "(expand|grow) %worldborders%['s (diameter|:radius)] (by|:to) %number% [over [a period of] %-timespan%]",
               "(contract|shrink) [[the] (diameter|:radius) of] %worldborders% (by|:to) %number% [over [a period of] %-timespan%]",
               "(contract|shrink) %worldborders%['s (diameter|:radius)] (by|:to) %number% [over [a period of] %-timespan%]"
            )
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.worldBorders = (Expression<WorldBorder>)exprs[0];
      this.numberExpr = (Expression<Number>)exprs[1];
      this.timespan = (Expression<Timespan>)exprs[2];
      this.shrink = matchedPattern > 1;
      this.radius = parseResult.hasTag("radius");
      this.to = parseResult.hasTag("to");
      return true;
   }

   @Override
   protected void execute(Event event) {
      Number number = this.numberExpr.getSingle(event);
      if (number != null) {
         double input = number.doubleValue();
         if (Double.isNaN(input)) {
            this.error("You can't " + (this.shrink ? "shrink" : "grow") + " a world border " + (this.to ? "to" : "by") + " NaN.");
         } else {
            if (this.radius) {
               input *= 2.0;
            }

            long speed = 0L;
            if (this.timespan != null) {
               Timespan timespan = this.timespan.getSingle(event);
               if (timespan != null) {
                  if (USE_DEPRECATED) {
                     speed = timespan.getAs(Timespan.TimePeriod.SECOND);
                  } else {
                     speed = timespan.getAs(Timespan.TimePeriod.TICK);
                  }
               }
            }

            WorldBorder[] worldBorders = this.worldBorders.getArray(event);
            if (this.to) {
               input = Math2.fit(1.0, input, 5.999997E7F);
               if (USE_DEPRECATED) {
                  for (WorldBorder worldBorder : worldBorders) {
                     worldBorder.setSize(input, speed);
                  }
               } else {
                  for (WorldBorder worldBorder : worldBorders) {
                     worldBorder.changeSize(input, speed);
                  }
               }
            } else {
               if (this.shrink) {
                  input = -input;
               }

               for (WorldBorder worldBorder : worldBorders) {
                  double size = worldBorder.getSize();
                  size = Math2.fit(1.0, size + input, 5.999997E7F);
                  if (USE_DEPRECATED) {
                     worldBorder.setSize(size, speed);
                  } else {
                     worldBorder.changeSize(size, speed);
                  }
               }
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append(this.shrink ? "shrink" : "expand");
      builder.append(this.radius ? "radius" : "diameter");
      builder.append("of", this.worldBorders);
      builder.append(this.to ? "to" : "by");
      builder.append(this.numberExpr);
      if (this.timespan != null) {
         builder.append("over", this.timespan);
      }

      return builder.toString();
   }
}
