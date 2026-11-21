package dev.vatten.baserad.interfaces;

import dev.vatten.baserad.Renderable;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

public class RenderableComponent implements Renderable {
   private final Component component;

   protected RenderableComponent(Component component) {
      this.component = component;
   }

   public static RenderableComponent of(Component component) {
      return new RenderableComponent(component);
   }

   public static RenderableComponent of(String text) {
      return of(Component.text(text));
   }

   public static RenderableComponent of(Renderable renderable) {
      return of(renderable.asComponent());
   }

   public static Renderable emptyLine() {
      return of(Component.empty());
   }

   public static RenderableComponent.MultiLineBuilder multiLine() {
      return new RenderableComponent.MultiLineBuilder();
   }

   public static RenderableComponent.FieldBuilder field() {
      return new RenderableComponent.FieldBuilder();
   }

   public static RenderableComponent.CenteredBuilder centered() {
      return new RenderableComponent.CenteredBuilder();
   }

   @Override
   public Component asComponent() {
      return this.component;
   }

   public static class CenteredBuilder {
      private Renderable renderable;

      public RenderableComponent.CenteredBuilder component(Renderable renderable) {
         this.renderable = renderable;
         return this;
      }

      public RenderableComponent.CenteredBuilder component(Component component) {
         return this.component(RenderableComponent.of(component));
      }

      public RenderableComponent build() {
         int padding = Math.max(0, 80 - this.renderable.estimateWidth() / 4) / 2;
         Component component = this.renderable.asComponent();
         if (padding > 0) {
            component = Component.text(" ".repeat(padding)).append(component);
         }

         return RenderableComponent.of(component);
      }
   }

   public static class FieldBuilder {
      private Component title = Component.empty();
      private List<Component> components = new ArrayList<>();
      private int spacing = 1;
      private int padding = 0;

      public RenderableComponent.FieldBuilder title(Component title) {
         this.title = title.applyFallbackStyle(Style.style(NamedTextColor.GRAY));
         return this;
      }

      public RenderableComponent.FieldBuilder addComponents(Component... components) {
         for (Component component : components) {
            this.addComponent(component);
         }

         return this;
      }

      public RenderableComponent.FieldBuilder addComponent(Component component) {
         this.components.add(component);
         return this;
      }

      public int size() {
         return this.components.size();
      }

      public RenderableComponent.FieldBuilder spacing(int spacing) {
         this.spacing = spacing;
         return this;
      }

      public RenderableComponent.FieldBuilder padding(int padding) {
         this.padding = padding;
         return this;
      }

      public RenderableComponent build() {
         Builder componentBuilder = (Builder)((Builder)Component.text().append(Component.text(" ".repeat(this.padding)))).append(this.title);

         for (int i = 0; i < this.components.size(); i++) {
            if (i > 0) {
               componentBuilder.append(Component.text(" ".repeat(this.spacing)));
            }

            componentBuilder.append(this.components.get(i).asComponent());
         }

         return RenderableComponent.of(componentBuilder.build());
      }
   }

   public static class MultiLineBuilder {
      private final List<Component> lines = new ArrayList<>();

      public RenderableComponent.MultiLineBuilder addLine(Component component) {
         this.lines.add(component);
         return this;
      }

      public RenderableComponent.MultiLineBuilder addLines(Component... components) {
         for (Component component : components) {
            this.addLine(component);
         }

         return this;
      }

      public RenderableComponent build() {
         Builder componentBuilder = Component.text();

         for (int i = 0; i < this.lines.size(); i++) {
            componentBuilder.append(this.lines.get(i));
            if (i + 1 < this.lines.size()) {
               componentBuilder = (Builder)componentBuilder.appendNewline();
            }
         }

         return RenderableComponent.of(componentBuilder.build());
      }
   }
}
