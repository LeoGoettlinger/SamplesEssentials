package org.skriptlang.skript.bukkit.item.book.elements.expressions;

import ch.njol.skript.aliases.ItemType;
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
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.ScriptWarning;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Book Title")
@Description({"The title of a book."})
@Example("on book sign:\n\tmessage \"You finished your book titled %title of event-item%\"\n")
@Since({"2.2-dev31"})
@Deprecated(
   since = "2.16",
   forRemoval = true
)
public class ExprBookTitle extends SimplePropertyExpression<ItemType, Component> {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprBookTitle.class, Component.class, "book (name|title)", "itemtypes", false)
               .supplier(ExprBookTitle::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      ScriptWarning.printDeprecationWarning(
         "This element is deprecated and scheduled for removal in a future release. Consider using 'title of '" + exprs[0].toString(null, false) + "' instead."
      );
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   public Component convert(ItemType item) {
      return item.getItemMeta() instanceof BookMeta bookMeta && bookMeta.hasTitle() ? bookMeta.title() : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, DELETE, RESET -> (Class[])CollectionUtils.array(Component.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Component title = delta == null ? null : (Component)delta[0];

      for (ItemType item : this.getExpr().getArray(event)) {
         if (item.getItemMeta() instanceof BookMeta bookMeta) {
            bookMeta.title(title);
            item.setItemMeta(bookMeta);
         }
      }
   }

   @Override
   public Class<? extends Component> getReturnType() {
      return Component.class;
   }

   @Override
   protected String getPropertyName() {
      return "book title";
   }
}
