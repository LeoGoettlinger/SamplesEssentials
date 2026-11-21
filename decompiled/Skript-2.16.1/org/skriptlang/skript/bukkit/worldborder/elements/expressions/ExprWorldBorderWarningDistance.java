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

@Name("Warning Distance of World Border")
@Description({"The warning distance of a world border. The player's screen will be tinted red when they are within this distance of the border.", "Players only see a red tint when approaching a world's worldborder and the warning distance has to be an integer greater than or equal to 0."})
@Example("set world border warning distance of {_worldborder} to 1")
@Since({"2.11"})
public class ExprWorldBorderWarningDistance extends SimplePropertyExpression<WorldBorder, Integer> {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprWorldBorderWarningDistance.class, Integer.class, "world[ ]border warning distance", "worldborders", true
               )
               .supplier(ExprWorldBorderWarningDistance::new))
            .build()
      );
   }

   @Nullable
   public Integer convert(WorldBorder worldBorder) {
      return worldBorder.getWarningDistance();
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
      int input = mode == Changer.ChangeMode.RESET ? 5 : ((Number)delta[0]).intValue();
      if (mode != Changer.ChangeMode.RESET && Double.isNaN(((Number)delta[0]).doubleValue())) {
         this.error("NaN is not a valid world border warning distance");
      } else {
         for (WorldBorder worldBorder : this.getExpr().getArray(event)) {
            switch (mode) {
               case SET:
               case RESET:
                  worldBorder.setWarningDistance(Math.max(input, 0));
                  break;
               case ADD:
                  if ((long)worldBorder.getWarningDistance() + input > 2147483647L) {
                     worldBorder.setWarningDistance(Integer.MAX_VALUE);
                  } else if ((long)worldBorder.getWarningDistance() + input < -2147483648L) {
                     worldBorder.setWarningDistance(0);
                  } else {
                     worldBorder.setWarningDistance(Math.max(worldBorder.getWarningDistance() + input, 0));
                  }
                  break;
               case REMOVE:
                  if ((long)worldBorder.getWarningDistance() - input > 2147483647L) {
                     worldBorder.setWarningDistance(Integer.MAX_VALUE);
                  } else if ((long)worldBorder.getWarningDistance() - input < -2147483648L) {
                     worldBorder.setWarningDistance(0);
                  } else {
                     worldBorder.setWarningDistance(Math.max(worldBorder.getWarningDistance() - input, 0));
                  }
            }
         }
      }
   }

   @Override
   public Class<? extends Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   protected String getPropertyName() {
      return "world border warning distance";
   }
}
