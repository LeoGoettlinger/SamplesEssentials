package de.theredend2000.advancedhunt.lib.kyori.adventure.inventory;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

record BookImpl(Component title, Component author, List<Component> pages) implements Book {
   @Override
   public Book title(final Component title) {
      return new BookImpl(Objects.requireNonNull(title, "title"), this.author, this.pages);
   }

   @Override
   public Book author(final Component author) {
      return new BookImpl(this.title, Objects.requireNonNull(author, "author"), this.pages);
   }

   @Override
   public Book pages(final List<Component> pages) {
      return new BookImpl(this.title, this.author, List.copyOf(Objects.requireNonNull(pages, "pages")));
   }

   public Book.Builder toBuilder() {
      return new BookImpl.BuilderImpl().title(this.title).author(this.author).pages(this.pages);
   }

   @Override
   public Book asBook() {
      return this;
   }

   static final class BuilderImpl implements Book.Builder {
      private Component title = Component.empty();
      private Component author = Component.empty();
      private final List<Component> pages = new ArrayList<>();

      @Override
      public Book.Builder title(final Component title) {
         this.title = Objects.requireNonNull(title, "title");
         return this;
      }

      @Override
      public Book.Builder author(final Component author) {
         this.author = Objects.requireNonNull(author, "author");
         return this;
      }

      @Override
      public Book.Builder addPage(final Component page) {
         this.pages.add(Objects.requireNonNull(page, "page"));
         return this;
      }

      @Override
      public Book.Builder pages(final Collection<Component> pages) {
         this.pages.addAll(Objects.requireNonNull(pages, "pages"));
         return this;
      }

      @Override
      public Book.Builder pages(final Component... pages) {
         Objects.requireNonNull(pages, "pages");
         Collections.addAll(this.pages, pages);
         return this;
      }

      public Book build() {
         return new BookImpl(this.title, this.author, this.pages);
      }
   }
}
