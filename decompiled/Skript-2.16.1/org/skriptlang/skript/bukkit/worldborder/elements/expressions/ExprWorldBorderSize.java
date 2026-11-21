package org.skriptlang.skript.bukkit.worldborder.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Size of World Border")
@Description({"The size of a world border.", "The size can not be smaller than 1."})
@Example("set world border radius of {_worldborder} to 10")
@Since({"2.11"})
public class ExprWorldBorderSize extends SimplePropertyExpression<WorldBorder, Double> {
   private boolean radius;
   private static final double MAX_WORLDBORDER_SIZE = 5.999997E7F;

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprWorldBorderSize.class, Double.class, "world[ ]border (size|diameter|:radius)", "worldborders", true
               )
               .supplier(ExprWorldBorderSize::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.radius = parseResult.hasTag("radius");
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Double convert(WorldBorder worldBorder) {
      return worldBorder.getSize() * (this.radius ? 0.5 : 1.0);
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
      double input = mode == Changer.ChangeMode.RESET ? 5.999997E7F : ((Number)delta[0]).doubleValue() * (this.radius ? 2 : 1);
      if (Double.isNaN(input)) {
         this.error("NaN is not a valid world border size");
      } else {
         for (WorldBorder worldBorder : this.getExpr().getArray(event)) {
            switch (mode) {
               case SET:
               case RESET:
                  worldBorder.setSize(Math2.fit(1.0, input, 5.999997E7F));
                  break;
               case ADD:
                  worldBorder.setSize(Math2.fit(1.0, worldBorder.getSize() + input, 5.999997E7F));
                  break;
               case REMOVE:
                  worldBorder.setSize(Math2.fit(1.0, worldBorder.getSize() - input, 5.999997E7F));
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
      return "world border size";
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "world border " + (this.radius ? "radius" : "diameter") + " of " + this.getExpr().toString(event, debug);
   }
}
