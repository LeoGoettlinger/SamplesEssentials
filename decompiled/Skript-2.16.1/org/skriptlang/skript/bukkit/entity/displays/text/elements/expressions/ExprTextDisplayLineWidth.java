package org.skriptlang.skript.bukkit.entity.displays.text.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Text Display Line Width")
@Description({"Returns or changes the line width of <a href='#display'>text displays</a>. Default is 200."})
@Example("set the line width of the last spawned text display to 300")
@Since({"2.10"})
public class ExprTextDisplayLineWidth extends SimplePropertyExpression<Display, Integer> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprTextDisplayLineWidth.class, Integer.class, "line width", "displays", true)
               .supplier(ExprTextDisplayLineWidth::new))
            .build()
      );
   }

   @Nullable
   public Integer convert(Display display) {
      return display instanceof TextDisplay textDisplay ? textDisplay.getLineWidth() : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, REMOVE, SET -> (Class[])CollectionUtils.array(Number.class);
         case RESET -> (Class[])CollectionUtils.array();
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Display[] displays = this.getExpr().getArray(event);
      int change = delta == null ? 200 : ((Number)delta[0]).intValue();
      switch (mode) {
         case REMOVE:
            change = -change;
         case ADD:
            for (Display displayx : displays) {
               if (displayx instanceof TextDisplay textDisplay) {
                  int value = Math.max(0, textDisplay.getLineWidth() + change);
                  textDisplay.setLineWidth(value);
               }
            }
            break;
         case SET:
         case RESET:
            change = Math.max(0, change);

            for (Display display : displays) {
               if (display instanceof TextDisplay textDisplay) {
                  textDisplay.setLineWidth(change);
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
      return "line width";
   }
}
