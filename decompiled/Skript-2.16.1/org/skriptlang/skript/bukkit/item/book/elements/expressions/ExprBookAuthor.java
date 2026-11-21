package org.skriptlang.skript.bukkit.item.book.elements.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Book Author")
@Description({"The author of a book."})
@Example("on book sign:\n\tbroadcast \"A new book has been created by %author of event-item%\"\n")
@Since({"2.2-dev31"})
public class ExprBookAuthor extends SimplePropertyExpression<ItemType, Component> {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprBookAuthor.class, Component.class, "[book] (author|writer|publisher)", "itemtypes", false)
               .supplier(ExprBookAuthor::new))
            .build()
      );
   }

   @Nullable
   public Component convert(ItemType item) {
      return item.getItemMeta() instanceof BookMeta bookMeta && bookMeta.hasAuthor() ? bookMeta.author() : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, RESET, DELETE -> (Class[])CollectionUtils.array(Component.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Component author = delta == null ? null : (Component)delta[0];

      for (ItemType item : this.getExpr().getArray(event)) {
         if (item.getItemMeta() instanceof BookMeta bookMeta) {
            bookMeta.author(author);
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
      return "book author";
   }
}
