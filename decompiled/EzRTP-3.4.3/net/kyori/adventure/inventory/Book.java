package net.kyori.adventure.inventory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Buildable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

public sealed interface Book extends Buildable<Book.Builder>, BookLike permits BookImpl {
   static Book book(final Component title, final Component author, final Collection<Component> pages) {
      return new BookImpl(Objects.requireNonNull(title, "title"), Objects.requireNonNull(author, "author"), List.copyOf(Objects.requireNonNull(pages, "pages")));
   }

   static Book book(final Component title, final Component author, final Component... pages) {
      return book(title, author, List.of(Objects.requireNonNull(pages)));
   }

   static Book book(final Collection<Component> pages) {
      return book(Component.empty(), Component.empty(), pages);
   }

   static Book book(final Component... pages) {
      return book(Component.empty(), Component.empty(), pages);
   }

   static Book.Builder builder() {
      return new BookImpl.BuilderImpl();
   }

   Component title();

   @Contract(
      value = "_ -> new",
      pure = true
   )
   Book title(final Component title);

   Component author();

   @Contract(
      value = "_ -> new",
      pure = true
   )
   Book author(final Component author);

   @Unmodifiable
   List<Component> pages();

   @Contract(
      value = "_ -> new",
      pure = true
   )
   default Book pages(final Component... pages) {
      return this.pages(List.of(pages));
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   Book pages(final List<Component> pages);

   public sealed interface Builder extends AbstractBuilder<Book> permits BookImpl.BuilderImpl {
      @Contract("_ -> this")
      Book.Builder title(final Component title);

      @Contract("_ -> this")
      Book.Builder author(final Component author);

      @Contract("_ -> this")
      Book.Builder addPage(final Component page);

      @Contract("_ -> this")
      Book.Builder pages(final Component... pages);

      @Contract("_ -> this")
      Book.Builder pages(final Collection<Component> pages);
   }
}
