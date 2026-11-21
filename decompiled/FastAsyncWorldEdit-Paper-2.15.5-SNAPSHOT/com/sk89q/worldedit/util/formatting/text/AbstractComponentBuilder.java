package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.Style;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class AbstractComponentBuilder<C extends BuildableComponent<C, B>, B extends ComponentBuilder<C, B>> implements ComponentBuilder<C, B> {
   protected List<Component> children = AbstractComponent.EMPTY_COMPONENT_LIST;
   @Nullable
   private Style style;
   @Nullable
   private Style.Builder styleBuilder;

   static <C extends BuildableComponent<C, B>, B extends ComponentBuilder<C, B>> C configureAndBuild(final B builder, final Consumer<? super B> consumer) {
      consumer.accept(builder);
      return builder.build();
   }

   protected AbstractComponentBuilder() {
   }

   protected AbstractComponentBuilder(@NonNull final C component) {
      List<Component> children = component.children();
      if (!children.isEmpty()) {
         this.children = new ArrayList<>(children);
      }

      if (component.hasStyling()) {
         this.style = component.style();
      }
   }

   @NonNull
   @Override
   public B append(@NonNull final Component component) {
      this.prepareChildren();
      this.children.add(component);
      return (B)this;
   }

   @NonNull
   @Override
   public B append(@NonNull final Component... components) {
      this.prepareChildren();
      Collections.addAll(this.children, components);
      return (B)this;
   }

   @NonNull
   @Override
   public B append(@NonNull final Iterable<? extends Component> components) {
      this.prepareChildren();
      components.forEach(this.children::add);
      return (B)this;
   }

   private void prepareChildren() {
      if (this.children == AbstractComponent.EMPTY_COMPONENT_LIST) {
         this.children = new ArrayList<>();
      }
   }

   @NonNull
   @Override
   public B applyDeep(@NonNull final Consumer<? super ComponentBuilder<?, ?>> consumer) {
      this.apply(consumer);
      if (this.children == AbstractComponent.EMPTY_COMPONENT_LIST) {
         return (B)this;
      } else {
         ListIterator<Component> it = this.children.listIterator();

         while (it.hasNext()) {
            Component child = it.next();
            if (child instanceof BuildableComponent) {
               ComponentBuilder<?, ?> childBuilder = ((BuildableComponent)child).toBuilder();
               childBuilder.applyDeep(consumer);
               it.set(childBuilder.build());
            }
         }

         return (B)this;
      }
   }

   @NonNull
   @Override
   public B mapChildren(@NonNull final Function<BuildableComponent<?, ?>, ? extends BuildableComponent<?, ?>> function) {
      if (this.children == AbstractComponent.EMPTY_COMPONENT_LIST) {
         return (B)this;
      } else {
         ListIterator<Component> it = this.children.listIterator();

         while (it.hasNext()) {
            Component child = it.next();
            if (child instanceof BuildableComponent) {
               BuildableComponent<?, ?> mappedChild = (BuildableComponent<?, ?>)function.apply((BuildableComponent<?, ?>)child);
               if (child != mappedChild) {
                  it.set(mappedChild);
               }
            }
         }

         return (B)this;
      }
   }

   @NonNull
   @Override
   public B mapChildrenDeep(@NonNull final Function<BuildableComponent<?, ?>, ? extends BuildableComponent<?, ?>> function) {
      if (this.children == AbstractComponent.EMPTY_COMPONENT_LIST) {
         return (B)this;
      } else {
         ListIterator<Component> it = this.children.listIterator();

         while (it.hasNext()) {
            Component child = it.next();
            if (child instanceof BuildableComponent) {
               BuildableComponent<?, ?> mappedChild = (BuildableComponent<?, ?>)function.apply((BuildableComponent<?, ?>)child);
               if (mappedChild.children().isEmpty()) {
                  if (child != mappedChild) {
                     it.set(mappedChild);
                  }
               } else {
                  ComponentBuilder<?, ?> builder = mappedChild.toBuilder();
                  builder.mapChildrenDeep(function);
                  it.set(builder.build());
               }
            }
         }

         return (B)this;
      }
   }

   @NonNull
   @Override
   public B style(@NonNull final Style style) {
      this.style = style;
      this.styleBuilder = null;
      return (B)this;
   }

   @NonNull
   @Override
   public B style(@NonNull final Consumer<Style.Builder> consumer) {
      consumer.accept(this.styleBuilder());
      return (B)this;
   }

   @NonNull
   @Override
   public B color(@Nullable final TextColor color) {
      this.styleBuilder().color(color);
      return (B)this;
   }

   @NonNull
   @Override
   public B colorIfAbsent(@Nullable final TextColor color) {
      this.styleBuilder().colorIfAbsent(color);
      return (B)this;
   }

   @NonNull
   @Override
   public B decoration(@NonNull final TextDecoration decoration, @NonNull final TextDecoration.State state) {
      this.styleBuilder().decoration(decoration, state);
      return (B)this;
   }

   @NonNull
   @Override
   public B clickEvent(@Nullable final ClickEvent event) {
      this.styleBuilder().clickEvent(event);
      return (B)this;
   }

   @NonNull
   @Override
   public B hoverEvent(@Nullable final HoverEvent event) {
      this.styleBuilder().hoverEvent(event);
      return (B)this;
   }

   @NonNull
   @Override
   public B insertion(@Nullable final String insertion) {
      this.styleBuilder().insertion(insertion);
      return (B)this;
   }

   @NonNull
   @Override
   public B mergeStyle(@NonNull final Component that, @NonNull final Set<Style.Merge> merges) {
      this.styleBuilder().merge(that.style(), merges);
      return (B)this;
   }

   @NonNull
   @Override
   public B resetStyle() {
      this.style = null;
      this.styleBuilder = null;
      return (B)this;
   }

   @NonNull
   private Style.Builder styleBuilder() {
      if (this.styleBuilder == null) {
         if (this.style != null) {
            this.styleBuilder = this.style.toBuilder();
            this.style = null;
         } else {
            this.styleBuilder = Style.builder();
         }
      }

      return this.styleBuilder;
   }

   protected final boolean hasStyle() {
      return this.styleBuilder != null || this.style != null;
   }

   @NonNull
   protected Style buildStyle() {
      if (this.styleBuilder != null) {
         return this.styleBuilder.build();
      } else {
         return this.style != null ? this.style : Style.empty();
      }
   }
}
