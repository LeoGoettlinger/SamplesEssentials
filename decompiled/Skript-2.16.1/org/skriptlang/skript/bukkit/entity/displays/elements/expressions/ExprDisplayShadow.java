package org.skriptlang.skript.bukkit.entity.displays.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Display Shadow Radius/Strength")
@Description({"Returns or changes the shadow radius/strength of <a href='#display'>displays</a>."})
@Example("set shadow radius of the last spawned text display to 1.75")
@Since({"2.10"})
public class ExprDisplayShadow extends SimplePropertyExpression<Display, Float> {
   private boolean radius;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprDisplayShadow.class, Float.class, "shadow (:radius|strength)", "displays", true)
               .supplier(ExprDisplayShadow::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.radius = parseResult.hasTag("radius");
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Float convert(Display display) {
      return this.radius ? display.getShadowRadius() : display.getShadowStrength();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, SET, REMOVE -> (Class[])CollectionUtils.array(Number.class);
         case RESET -> (Class[])CollectionUtils.array();
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Display[] displays = this.getExpr().getArray(event);
      float change = delta == null ? 0.0F : ((Number)delta[0]).floatValue();
      if (!Float.isInfinite(change) && !Float.isNaN(change)) {
         switch (mode) {
            case REMOVE:
               change = -change;
            case ADD:
               for (Display displayx : displays) {
                  if (this.radius) {
                     float value = Math.max(0.0F, displayx.getShadowRadius() + change);
                     if (!Float.isInfinite(value)) {
                        displayx.setShadowRadius(value);
                     }
                  } else {
                     float value = Math.max(0.0F, displayx.getShadowStrength() + change);
                     if (!Float.isInfinite(value)) {
                        displayx.setShadowStrength(value);
                     }
                  }
               }
               break;
            case RESET:
               if (!this.radius) {
                  change = 1.0F;
               }
            case SET:
               change = Math.max(0.0F, change);

               for (Display display : displays) {
                  if (this.radius) {
                     display.setShadowRadius(change);
                  } else {
                     display.setShadowStrength(change);
                  }
               }
         }
      }
   }

   @Override
   public Class<? extends Float> getReturnType() {
      return Float.class;
   }

   @Override
   protected String getPropertyName() {
      return this.radius ? "radius" : "strength";
   }
}
