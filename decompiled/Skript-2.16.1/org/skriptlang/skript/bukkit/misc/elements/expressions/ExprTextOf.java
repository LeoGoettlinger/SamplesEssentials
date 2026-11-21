package org.skriptlang.skript.bukkit.misc.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import java.util.Arrays;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentUtils;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Text Of")
@Description({"Returns or changes the <a href='#string'>text/string</a> of <a href='#display'>displays</a>.", "Note that currently you can only use Skript chat codes when running Paper."})
@Example("set text of the last spawned text display to \"example\"")
@Since({"2.10"})
public class ExprTextOf extends SimplePropertyExpression<Object, Component> {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprTextOf.class, Component.class, "text[s]", "displays", false).supplier(ExprTextOf::new))
            .build()
      );
   }

   @Nullable
   public Component convert(Object object) {
      return object instanceof TextDisplay textDisplay ? textDisplay.text() : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case RESET -> (Class[])CollectionUtils.array();
         case SET -> (Class[])CollectionUtils.array(Component[].class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Component component = delta == null ? null : TextComponentUtils.joinByNewLine(Arrays.copyOf(delta, delta.length, Component[].class));

      for (Object object : this.getExpr().getArray(event)) {
         if (object instanceof TextDisplay textDisplay) {
            textDisplay.text(component);
         }
      }
   }

   @Override
   public Class<? extends Component> getReturnType() {
      return Component.class;
   }

   @Override
   protected String getPropertyName() {
      return "text";
   }
}
