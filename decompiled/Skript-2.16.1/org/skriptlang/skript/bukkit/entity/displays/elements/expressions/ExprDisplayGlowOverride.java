package org.skriptlang.skript.bukkit.entity.displays.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Display Glow Color Override")
@Description({"Returns or changes the glowing color override of <a href='#display'>displays</a>.", "This overrides whatever color is already set for the scoreboard team of the displays."})
@Example("set glow color override of the last spawned text display to blue")
@Since({"2.10"})
public class ExprDisplayGlowOverride extends SimplePropertyExpression<Display, Color> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprDisplayGlowOverride.class, Color.class, "glow[ing] colo[u]r[s] override[s]", "displays", true)
               .supplier(ExprDisplayGlowOverride::new))
            .build()
      );
   }

   @Nullable
   public Color convert(Display display) {
      org.bukkit.Color color = display.getGlowColorOverride();
      return color != null ? ColorRGB.fromBukkitColor(color) : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, RESET, DELETE -> (Class[])CollectionUtils.array(Color.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      org.bukkit.Color color = delta != null ? ((Color)delta[0]).asBukkitColor() : null;

      for (Display display : this.getExpr().getArray(event)) {
         display.setGlowColorOverride(color);
      }
   }

   @Override
   public Class<? extends Color> getReturnType() {
      return Color.class;
   }

   @Override
   protected String getPropertyName() {
      return "glow color override";
   }
}
