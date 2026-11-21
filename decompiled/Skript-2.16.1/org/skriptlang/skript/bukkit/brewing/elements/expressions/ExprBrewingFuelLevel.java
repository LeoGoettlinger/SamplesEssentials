package org.skriptlang.skript.bukkit.brewing.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Brewing Stand Fuel Level")
@Description({"The fuel level of a brewing stand. The fuel level is decreased by one at the start of brewing each potion."})
@Example("set the brewing stand fuel level of {_block} to 10\nclear the brewing stand fuel level of {_block}\n")
@Since({"2.13"})
public class ExprBrewingFuelLevel extends SimplePropertyExpression<Block, Integer> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprBrewingFuelLevel.class, Integer.class, "brewing [stand] fuel (level|amount)", "blocks", true)
               .supplier(ExprBrewingFuelLevel::new))
            .build()
      );
   }

   @Nullable
   public Integer convert(Block block) {
      return block.getState() instanceof BrewingStand brewingStand ? brewingStand.getFuelLevel() : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, REMOVE, SET, DELETE, RESET -> (Class[])CollectionUtils.array(Number.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int providedValue = delta != null ? ((Number)delta[0]).intValue() : 0;
      if (mode == Changer.ChangeMode.SET) {
         providedValue = Math2.fit(0, providedValue, Integer.MAX_VALUE);
      }

      for (Block block : this.getExpr().getArray(event)) {
         if (block.getState() instanceof BrewingStand brewingStand) {
            int newValue = providedValue;
            int current = brewingStand.getFuelLevel();
            if (mode == Changer.ChangeMode.ADD) {
               newValue = Math2.fit(0, current + providedValue, Integer.MAX_VALUE);
            } else if (mode == Changer.ChangeMode.REMOVE) {
               newValue = Math2.fit(0, current - providedValue, Integer.MAX_VALUE);
            }

            brewingStand.setFuelLevel(newValue);
            brewingStand.update(true, false);
         }
      }
   }

   @Override
   public Class<Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   protected String getPropertyName() {
      return "brewing stand fuel level";
   }
}
