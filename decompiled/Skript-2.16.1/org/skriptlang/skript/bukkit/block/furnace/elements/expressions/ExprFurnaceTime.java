package org.skriptlang.skript.bukkit.block.furnace.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Furnace Times")
@Description({"The cook time, total cook time, and burn time of a furnace. Can be changed.", "<ul>", "<li>cook time: The amount of time an item has been smelting for.</li>", "<li>total cook time: The amount of time required to finish smelting an item.</li>", "<li>burn time: The amount of time left for the current fuel until consumption of another fuel item.</li>", "</ul>"})
@Example.Examples({@Example("set the cooking time of {_block} to 10"), @Example("set the total cooking time of {_block} to 50"), @Example("set the fuel burning time of {_block} to 100"), @Example("on smelt:\n\tif the fuel slot is charcoal:\n\t\tadd 5 seconds to the fuel burn time\n")})
@Since({"2.10"})
public class ExprFurnaceTime extends PropertyExpression<Block, Timespan> {
   private static final ExprFurnaceTime.FurnaceExpressions[] furnaceExprs = ExprFurnaceTime.FurnaceExpressions.values();
   private ExprFurnaceTime.FurnaceExpressions type;
   private boolean explicitlyBlock = false;

   public static void register(SyntaxRegistry registry) {
      int size = furnaceExprs.length;
      String[] patterns = new String[size * 2];

      for (ExprFurnaceTime.FurnaceExpressions value : furnaceExprs) {
         patterns[2 * value.ordinal()] = "[the] [furnace] " + value.pattern + " [of %blocks%]";
         patterns[2 * value.ordinal() + 1] = "%blocks%'[s]" + value.pattern;
      }

      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprFurnaceTime.class, Timespan.class
                     )
                     .addPatterns(patterns))
                  .supplier(ExprFurnaceTime::new))
               .priority(PropertyExpression.DEFAULT_PRIORITY))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.type = furnaceExprs[matchedPattern / 2];
      if (exprs[0] != null) {
         this.explicitlyBlock = true;
         this.setExpr((Expression<? extends Block>)exprs[0]);
      } else {
         if (!this.getParser().isCurrentEvent(FurnaceBurnEvent.class, FurnaceStartSmeltEvent.class, FurnaceExtractEvent.class, FurnaceSmeltEvent.class)) {
            Skript.error("There's no furnace in a '" + this.getParser().getCurrentEventName() + "' event.");
            return false;
         }

         this.explicitlyBlock = false;
         this.setExpr(new EventValueExpression<>(Block.class));
      }

      return true;
   }

   @Nullable
   protected Timespan[] get(Event event, Block[] source) {
      return this.get(source, block -> {
         if (block != null && block.getState() instanceof Furnace furnace) {
            switch (this.type) {
               case COOKTIME:
                  return new Timespan(Timespan.TimePeriod.TICK, furnace.getCookTime());
               case TOTALCOOKTIME:
                  if (event instanceof FurnaceStartSmeltEvent startEvent && block.equals(startEvent.getBlock())) {
                     return new Timespan(Timespan.TimePeriod.TICK, startEvent.getTotalCookTime());
                  }

                  return new Timespan(Timespan.TimePeriod.TICK, furnace.getCookTimeTotal());
               case BURNTIME:
                  if (event instanceof FurnaceBurnEvent burnEvent && block.equals(burnEvent.getBlock())) {
                     return new Timespan(Timespan.TimePeriod.TICK, burnEvent.getBurnTime());
                  }

                  return new Timespan(Timespan.TimePeriod.TICK, furnace.getBurnTime());
               default:
                  return null;
            }
         } else {
            return null;
         }
      });
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.REMOVE_ALL && mode != Changer.ChangeMode.RESET ? CollectionUtils.array(Timespan.class) : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int providedTime = 0;
      if (delta != null && delta[0] instanceof Timespan span) {
         providedTime = (int)span.getAs(Timespan.TimePeriod.TICK);
      }

      int finalTime = providedTime;

      Function<Integer, Integer> calculateTime = switch (mode) {
         case SET -> time -> finalTime;
         case ADD -> time -> Math2.fit(0, time + finalTime, Integer.MAX_VALUE);
         case REMOVE -> time -> Math2.fit(0, time - finalTime, Integer.MAX_VALUE);
         case DELETE -> time -> 0;
         default -> throw new IllegalStateException("Unexpected value: " + mode);
      };

      Function<Short, Short> calculateTimeShort;
      Function var12 = calculateTimeShort = switch (mode) {
         case SET -> time -> (short)finalTime;
         case ADD -> time -> (short)Math2.fit(0, time + finalTime, 32767);
         case REMOVE -> time -> (short)Math2.fit(0, time - finalTime, 32767);
         case DELETE -> time -> (short)0;
         default -> throw new IllegalStateException("Unexpected value: " + mode);
      };
      switch (this.type) {
         case COOKTIME:
            this.changeFurnaces(event, furnace -> change(furnace::setCookTime, furnace::getCookTime, calculateTimeShort));
            break;
         case TOTALCOOKTIME:
            if (!this.explicitlyBlock && event instanceof FurnaceStartSmeltEvent smeltEvent) {
               change(smeltEvent::setTotalCookTime, smeltEvent::getTotalCookTime, calculateTime);
            } else {
               this.changeFurnaces(event, furnace -> change(furnace::setCookTimeTotal, furnace::getCookTimeTotal, calculateTime));
            }
            break;
         case BURNTIME:
            if (!this.explicitlyBlock && event instanceof FurnaceBurnEvent burnEvent) {
               change(burnEvent::setBurnTime, burnEvent::getBurnTime, calculateTime);
            } else {
               this.changeFurnaces(event, furnace -> change(furnace::setBurnTime, furnace::getBurnTime, calculateTimeShort));
            }
      }
   }

   private void changeFurnaces(Event event, Consumer<Furnace> changer) {
      for (Block block : this.getExpr().getArray(event)) {
         if (block != null && block.getState() instanceof Furnace furnace) {
            changer.accept(furnace);
            furnace.update(true);
         }
      }
   }

   private static <T extends Number> void change(@NotNull Consumer<T> set, @NotNull Supplier<T> get, @NotNull Function<T, T> calculateTime) {
      set.accept(calculateTime.apply(get.get()));
   }

   @Override
   public boolean isSingle() {
      return this.getExpr().isSingle();
   }

   @Override
   public Class<Timespan> getReturnType() {
      return Timespan.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.type.toString + " of " + this.getExpr().toString(event, debug);
   }

   static enum FurnaceExpressions {
      COOKTIME("cook[ing] time", "cook time"),
      TOTALCOOKTIME("total cook[ing] time", "total cook time"),
      BURNTIME("fuel burn[ing] time", "fuel burn time");

      private String pattern;
      private String toString;

      private FurnaceExpressions(String pattern, String toString) {
         this.pattern = pattern;
         this.toString = toString;
      }
   }
}
