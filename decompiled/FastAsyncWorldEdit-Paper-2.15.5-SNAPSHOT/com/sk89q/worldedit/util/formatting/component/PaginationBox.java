package com.sk89q.worldedit.util.formatting.component;

import com.fastasyncworldedit.core.configuration.Caption;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public abstract class PaginationBox extends MessageBox {
   private static final int IDEAL_ROWS_FOR_PLAYER = 8;
   private String pageCommand;
   private int componentsPerPage = 8;
   private int currentPage = -1;

   protected PaginationBox(String title) {
      this(title, null);
   }

   public abstract Component getComponent(int number);

   public abstract int getComponentsSize();

   public void setComponentsPerPage(int componentsPerPage) {
      this.componentsPerPage = componentsPerPage;
   }

   public void formatForConsole() {
      this.pageCommand = null;
      this.componentsPerPage = 20;
   }

   protected final int getCurrentPage() {
      return this.currentPage;
   }

   protected PaginationBox(String title, @Nullable String pageCommand) {
      super(title, new TextComponentProducer());
      if (pageCommand != null && !pageCommand.contains("%page%")) {
         if (pageCommand.contains("-p ")) {
            pageCommand = pageCommand.replaceAll("-p [0-9]+", "-p %page%");
         } else {
            pageCommand = pageCommand + " -p %page%";
         }
      }

      this.pageCommand = pageCommand;
   }

   public Component create(int page) throws InvalidComponentException {
      if (page == 1 && this.getComponentsSize() == 0) {
         return this.getContents().reset().append("No results found.").create();
      } else {
         int pageCount = (int)Math.ceil((double)this.getComponentsSize() / this.componentsPerPage);
         if (page >= 1 && page <= pageCount) {
            this.currentPage = page;
            int lastComp = Math.min(page * this.componentsPerPage, this.getComponentsSize());

            for (int i = (page - 1) * this.componentsPerPage; i < lastComp; i++) {
               this.getContents().append(this.getComponent(i));
               if (i + 1 != lastComp) {
                  this.getContents().newline();
               }
            }

            if (pageCount == 1) {
               return super.create();
            } else {
               this.getContents().newline();
               TextComponent pageNumberComponent = (TextComponent)((TextComponent)((TextComponent)TextComponent.of("Page ", TextColor.YELLOW)
                        .append(TextComponent.of(String.valueOf(page), TextColor.GOLD)))
                     .append(TextComponent.of(" of ")))
                  .append(TextComponent.of(String.valueOf(pageCount), TextColor.GOLD));
               if (this.pageCommand != null) {
                  TextComponentProducer navProducer = new TextComponentProducer();
                  if (page > 1) {
                     TextComponent prevComponent = (TextComponent)((TextComponent)TextComponent.of("<<< ", TextColor.GOLD)
                           .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, this.pageCommand.replace("%page%", String.valueOf(page - 1)))))
                        .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to navigate")));
                     navProducer.append(prevComponent);
                  }

                  navProducer.append(pageNumberComponent);
                  if (page < pageCount) {
                     TextComponent nextComponent = (TextComponent)((TextComponent)TextComponent.of(" >>>", TextColor.GOLD)
                           .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, this.pageCommand.replace("%page%", String.valueOf(page + 1)))))
                        .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to navigate")));
                     navProducer.append(nextComponent);
                  }

                  this.getContents().append(this.centerAndBorder(navProducer.create()));
               } else {
                  this.getContents().append(this.centerAndBorder(pageNumberComponent));
               }

               return super.create();
            }
         } else {
            throw new InvalidComponentException(Caption.of("worldedit.error.invalid-page"));
         }
      }
   }

   @Override
   public TextComponent create() {
      throw new IllegalStateException("Pagination components must be created with a page");
   }

   public static <T> PaginationBox fromStrings(String header, @Nullable String pageCommand, Collection<T> lines, Function<T, Component> adapt) {
      return fromComponents(header, pageCommand, Collections2.transform(lines, adapt));
   }

   public static PaginationBox fromStrings(String header, @Nullable String pageCommand, Collection<String> lines) {
      return fromComponents(header, pageCommand, lines.stream().map(TextComponent::of).collect(Collectors.toList()));
   }

   public static PaginationBox fromComponents(String header, @Nullable String pageCommand, Collection<Component> lines) {
      return new PaginationBox.ListPaginationBox(header, pageCommand, lines);
   }

   private static class ListPaginationBox extends PaginationBox {
      private final List<Component> lines;

      ListPaginationBox(String header, String pageCommand, Collection<Component> lines) {
         super(header, pageCommand);
         this.lines = ImmutableList.copyOf(lines);
      }

      @Override
      public Component getComponent(int number) {
         return this.lines.get(number);
      }

      @Override
      public int getComponentsSize() {
         return this.lines.size();
      }
   }

   public static class MergedPaginationBox extends PaginationBox {
      private final PaginationBox[] values;

      public MergedPaginationBox(String header, String pageCommand, PaginationBox... values) {
         super(header, pageCommand);
         this.values = values;
      }

      @Override
      public Component getComponent(int number) {
         for (PaginationBox box : this.values) {
            if (box != null) {
               int size = box.getComponentsSize();
               if (size > number) {
                  return box.getComponent(number);
               }

               number -= size;
            }
         }

         return null;
      }

      @Override
      public int getComponentsSize() {
         int size = 0;

         for (PaginationBox box : this.values) {
            if (box != null) {
               size += box.getComponentsSize();
            }
         }

         return size;
      }
   }
}
