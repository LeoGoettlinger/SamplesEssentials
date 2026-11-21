package org.skriptlang.skript.bukkit.item.book.elements.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Book Pages")
@Description({"The pages of a book (Supports Skript's chat format)", "Note: In order to modify the pages of a new written book, you must have the title and author", "of the book set. Skript will do this for you, but if you want your own, please set those values."})
@Example.Examples({@Example("on book sign:\n\tif the number of pages of event-item is greater than 1:\n\t\tmessage \"The second page of the authored book is: %page 2 of event-item%\"\n"), @Example("set page 1 of the player's held item to \"This page was written with Skript!\"")})
@Since({"2.2-dev31, 2.7 (changers)"})
public class ExprBookPages extends SimpleExpression<Component> {
   private static final boolean EXTENDS_ADVENTURE_BOOK = Book.class.isAssignableFrom(BookMeta.class);
   private Expression<ItemType> books;
   @Nullable
   private Expression<Integer> pageNumber;

   @Unmodifiable
   public static List<Component> getPages(BookMeta bookMeta) {
      return EXTENDS_ADVENTURE_BOOK ? ((Book)bookMeta).pages() : bookMeta.pages();
   }

   public static void setPages(BookMeta bookMeta, List<Component> pages) {
      if (EXTENDS_ADVENTURE_BOOK) {
         ((Book)bookMeta).pages(pages);
      } else {
         bookMeta.pages(pages);
      }
   }

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprBookPages.class, Component.class
                     )
                     .supplier(ExprBookPages::new))
                  .priority(PropertyExpression.DEFAULT_PRIORITY))
               .addPatterns(
                  new String[]{
                     "[all [[of] the]|the] [book] (pages|content) of %itemtypes%",
                     "%itemtypes%'[s] [book] (pages|content)",
                     "[book] page %integer% of %itemtypes%",
                     "%itemtypes%'[s] [book] page %integer%"
                  }
               ))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (matchedPattern == 0 || matchedPattern == 1) {
         this.books = (Expression<ItemType>)expressions[0];
      } else if (matchedPattern == 2) {
         this.pageNumber = (Expression<Integer>)expressions[0];
         this.books = (Expression<ItemType>)expressions[1];
      } else {
         this.books = (Expression<ItemType>)expressions[0];
         this.pageNumber = (Expression<Integer>)expressions[1];
      }

      return true;
   }

   protected Component[] get(Event event) {
      List<Component> pages = new ArrayList<>();

      for (ItemType book : this.books.getArray(event)) {
         if (book.getMaterial() != Material.WRITTEN_BOOK || !(book.getItemMeta() instanceof BookMeta bookMeta)) {
            return new Component[0];
         }

         if (this.isAllPages()) {
            pages.addAll(getPages(bookMeta));
         } else {
            Integer pageNumber = this.pageNumber.getSingle(event);
            if (pageNumber != null && pageNumber > 0 && pageNumber <= bookMeta.getPageCount()) {
               pages.add(bookMeta.page(pageNumber));
            }
         }
      }

      return pages.toArray(new Component[0]);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET -> (Class[])CollectionUtils.array(this.isAllPages() ? Component[].class : Component.class);
         case ADD -> this.isAllPages() ? (Class[])CollectionUtils.array(Component[].class) : null;
         case DELETE, RESET -> (Class[])CollectionUtils.array();
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int pageNumber = -1;
      if (!this.isAllPages()) {
         pageNumber = this.pageNumber.getOptionalSingle(event).orElse(-1);
         if (pageNumber <= 0) {
            return;
         }
      }

      List<Component> newPages = (List<Component>)(delta == null ? List.of() : new ArrayList<>(delta.length));
      if (delta != null) {
         for (Object page : delta) {
            newPages.add((Component)page);
         }
      }

      for (ItemType book : this.books.getArray(event)) {
         if (book.getMaterial() == Material.WRITTEN_BOOK && book.getItemMeta() instanceof BookMeta bookMeta) {
            if (this.isAllPages()) {
               switch (mode) {
                  case SET:
                  case DELETE:
                  case RESET:
                     setPages(bookMeta, newPages);
                     break;
                  case ADD:
                     bookMeta.addPages(newPages.toArray(new Component[0]));
                     break;
                  default:
                     throw new IllegalStateException();
               }
            } else {
               switch (mode) {
                  case SET:
                     if (pageNumber > bookMeta.getPageCount()) {
                        Component[] pages = new Component[pageNumber - bookMeta.getPageCount()];
                        Arrays.fill(pages, Component.empty());
                        bookMeta.addPages(pages);
                     }

                     bookMeta.page(pageNumber, newPages.getFirst());
                     break;
                  case ADD:
                  default:
                     throw new IllegalStateException();
                  case DELETE:
                     if (pageNumber <= bookMeta.getPageCount()) {
                        List<Component> pages = new ArrayList<>(getPages(bookMeta));
                        pages.remove(pageNumber - 1);
                        setPages(bookMeta, pages);
                     }
                     break;
                  case RESET:
                     if (pageNumber > bookMeta.getPageCount()) {
                        continue;
                     }

                     bookMeta.page(pageNumber, Component.empty());
               }
            }

            if (!bookMeta.hasTitle()) {
               Component title = (Component)(bookMeta.hasDisplayName() ? bookMeta.displayName() : Component.text("Written Book"));
               bookMeta.title(title);
            }

            if (!bookMeta.hasAuthor()) {
               bookMeta.author(Component.text("Unknown"));
            }

            book.setItemMeta(bookMeta);
         }
      }
   }

   private boolean isAllPages() {
      return this.pageNumber == null;
   }

   @Override
   public boolean isSingle() {
      return this.books.isSingle() && !this.isAllPages();
   }

   @Override
   public Class<? extends Component> getReturnType() {
      return Component.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      if (this.isAllPages()) {
         builder.append("all of the pages");
      } else {
         builder.append("page", this.pageNumber);
      }

      builder.append("of", this.books);
      return builder.toString();
   }
}
