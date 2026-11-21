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

@Name("Display Height/Width")
@Description({"Returns or changes the height or width of <a href='#display'>displays</a>.", "The rendering culling bounding box spans horizontally width/2 from entity position, which determines the point at which the display will be frustum culled (no longer rendered because the game determines you are no longer able to see it).", "If set to 0, no culling will occur on both the vertical and horizontal directions. Default is 0.0."})
@Example("set display height of the last spawned text display to 2.5")
@Since({"2.10"})
public class ExprDisplayHeightWidth extends SimplePropertyExpression<Display, Float> {
   private boolean height;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprDisplayHeightWidth.class, Float.class, "display (:height|width)", "displays", true)
               .supplier(ExprDisplayHeightWidth::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.height = parseResult.hasTag("height");
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Float convert(Display display) {
      return this.height ? display.getDisplayHeight() : display.getDisplayWidth();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, REMOVE, RESET, SET -> (Class[])CollectionUtils.array(Number.class);
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
                  if (this.height) {
                     float value = Math.max(0.0F, displayx.getDisplayHeight() + change);
                     if (!Float.isInfinite(value)) {
                        displayx.setDisplayHeight(value);
                     }
                  } else {
                     float value = Math.max(0.0F, displayx.getDisplayWidth() + change);
                     if (!Float.isInfinite(value)) {
                        displayx.setDisplayWidth(value);
                     }
                  }
               }
               break;
            case RESET:
            case SET:
               change = Math.max(0.0F, change);

               for (Display display : displays) {
                  if (this.height) {
                     display.setDisplayHeight(change);
                  } else {
                     display.setDisplayWidth(change);
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
      return this.height ? "height" : "width";
   }
}
