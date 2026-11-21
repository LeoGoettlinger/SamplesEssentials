package org.skriptlang.skript.bukkit.entity.displays.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Display View Range")
@Description({"Returns or changes the view range of <a href='#display'>displays</a>.", "Default value is 1.0. This value is then multiplied by 64 and the player's entity view distance setting to determine the actual range.", "For example, a player with 150% entity view distance will see a block display with a view range of 1.2 at 1.2 * 64 * 150% = 115.2 blocks away."})
@Example("set view range of the last spawned text display to 2.9")
@Since({"2.10"})
public class ExprDisplayViewRange extends SimplePropertyExpression<Display, Float> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprDisplayViewRange.class, Float.class, "[display] view (range|radius)", "displays", true)
               .supplier(ExprDisplayViewRange::new))
            .build()
      );
   }

   @Nullable
   public Float convert(Display display) {
      return display.getViewRange();
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
      float change = delta == null ? 1.0F : ((Number)delta[0]).floatValue();
      if (!Float.isNaN(change) && !Float.isInfinite(change)) {
         switch (mode) {
            case SET:
            case RESET:
               change = Math.max(0.0F, change);

               for (Display display : displays) {
                  display.setViewRange(change);
               }
               break;
            case REMOVE:
               change = -change;
            case ADD:
               for (Display display : displays) {
                  float value = Math.max(0.0F, display.getViewRange() + change);
                  if (!Float.isInfinite(value)) {
                     display.setViewRange(value);
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
      return "view range";
   }
}
