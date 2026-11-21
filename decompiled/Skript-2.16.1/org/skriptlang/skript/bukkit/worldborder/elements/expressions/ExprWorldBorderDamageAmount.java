package org.skriptlang.skript.bukkit.worldborder.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Damage Amount of World Border")
@Description({"The amount of damage a player takes per second for each block they are outside the border plus the border buffer.", "Players only take damage when outside of the world's world border, and the damage value cannot be less than 0."})
@Example("set world border damage amount of {_worldborder} to 1")
@Since({"2.11"})
public class ExprWorldBorderDamageAmount extends SimplePropertyExpression<WorldBorder, Double> {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprWorldBorderDamageAmount.class, Double.class, "world[ ]border damage amount", "worldborders", true
               )
               .supplier(ExprWorldBorderDamageAmount::new))
            .build()
      );
   }

   @Nullable
   public Double convert(WorldBorder worldBorder) {
      return worldBorder.getDamageAmount();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, ADD, REMOVE, RESET -> (Class[])CollectionUtils.array(Number.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      double input = mode == Changer.ChangeMode.RESET ? 0.2 : ((Number)delta[0]).doubleValue();
      if (Double.isNaN(input)) {
         this.error("NaN is not a valid world border damage amount");
      } else if (Double.isInfinite(input)) {
         this.error("World border damage amount cannot be infinite");
      } else {
         for (WorldBorder worldBorder : this.getExpr().getArray(event)) {
            switch (mode) {
               case SET:
               case RESET:
                  worldBorder.setDamageAmount(Math.max(input, 0.0));
                  break;
               case ADD:
                  worldBorder.setDamageAmount(Math.max(worldBorder.getDamageAmount() + input, 0.0));
                  break;
               case REMOVE:
                  worldBorder.setDamageAmount(Math.max(worldBorder.getDamageAmount() - input, 0.0));
            }
         }
      }
   }

   @Override
   public Class<? extends Double> getReturnType() {
      return Double.class;
   }

   @Override
   protected String getPropertyName() {
      return "world border damage amount";
   }
}
