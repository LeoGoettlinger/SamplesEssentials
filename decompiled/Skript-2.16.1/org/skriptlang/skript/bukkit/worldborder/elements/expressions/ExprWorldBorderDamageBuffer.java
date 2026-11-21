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

@Name("Damage Buffer of World Border")
@Description({"The amount of blocks a player may safely be outside the border before taking damage.", "Players only take damage when outside of the world's world border, and the damage buffer distance cannot be less than 0."})
@Example("set world border damage buffer of {_worldborder} to 10")
@Since({"2.11"})
public class ExprWorldBorderDamageBuffer extends SimplePropertyExpression<WorldBorder, Double> {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprWorldBorderDamageBuffer.class, Double.class, "world[ ]border damage buffer", "worldborders", true
               )
               .supplier(ExprWorldBorderDamageBuffer::new))
            .build()
      );
   }

   @Nullable
   public Double convert(WorldBorder worldBorder) {
      return worldBorder.getDamageBuffer();
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
      double input = mode == Changer.ChangeMode.RESET ? 5.0 : ((Number)delta[0]).doubleValue();
      if (Double.isNaN(input)) {
         this.error("NaN is not a valid world border damage buffer");
      } else {
         for (WorldBorder worldBorder : this.getExpr().getArray(event)) {
            switch (mode) {
               case SET:
               case RESET:
                  worldBorder.setDamageBuffer(Math.max(input, 0.0));
                  break;
               case ADD:
                  worldBorder.setDamageBuffer(Math.max(worldBorder.getDamageBuffer() + input, 0.0));
                  break;
               case REMOVE:
                  worldBorder.setDamageBuffer(Math.max(worldBorder.getDamageBuffer() - input, 0.0));
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
      return "world border damage buffer";
   }
}
