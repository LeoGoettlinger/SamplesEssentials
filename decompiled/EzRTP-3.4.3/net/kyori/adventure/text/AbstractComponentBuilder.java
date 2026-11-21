package net.kyori.adventure.text;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.ARGBLike;
import org.jspecify.annotations.Nullable;

abstract sealed class AbstractComponentBuilder<C extends Component, B extends ComponentBuilder<C, B>>
   implements ComponentBuilder<C, B>
   permits AbstractNBTComponentBuilder,
   KeybindComponentImpl.BuilderImpl,
   ObjectComponentImpl.BuilderImpl,
   ScoreComponentImpl.BuilderImpl,
   SelectorComponentImpl.BuilderImpl,
   TextComponentImpl.BuilderImpl,
   TranslatableComponentImpl.BuilderImpl {
   protected List<Component> children = List.of();
   @Nullable
   private Style style;
   @Nullable
   private Style.Builder styleBuilder;

   protected AbstractComponentBuilder() {
   }

   protected AbstractComponentBuilder(final C component) {
      List<Component> children = component.children();
      if (!children.isEmpty()) {
         this.children = new ArrayList<>(children);
      }

      if (component.hasStyling()) {
         this.style = component.style();
      }
   }

   @Override
   public B append(final Component component) {
      if (component == Component.empty()) {
         return (B)this;
      } else {
         this.prepareChildren();
         this.children.add(Objects.requireNonNull(component, "component"));
         return (B)this;
      }
   }

   @Override
   public B append(final Component... components) {
      return this.append(components);
   }

   @Override
   public B append(final ComponentLike... components) {
      Objects.requireNonNull(components, "components");
      boolean prepared = false;

      for (ComponentLike componentLike : components) {
         Component component = Objects.requireNonNull(componentLike, "components[?]").asComponent();
         if (component != Component.empty()) {
            if (!prepared) {
               this.prepareChildren();
               prepared = true;
            }

            this.children.add(Objects.requireNonNull(component, "components[?]"));
         }
      }

      return (B)this;
   }

   @Override
   public B append(final Iterable<? extends ComponentLike> components) {
      Objects.requireNonNull(components, "components");
      boolean prepared = false;

      for (ComponentLike like : components) {
         Component component = Objects.requireNonNull(like, "components[?]").asComponent();
         if (component != Component.empty()) {
            if (!prepared) {
               this.prepareChildren();
               prepared = true;
            }

            this.children.add(Objects.requireNonNull(component, "components[?]"));
         }
      }

      return (B)this;
   }

   private void prepareChildren() {
      if (this.children.isEmpty()) {
         this.children = new ArrayList<>();
      }
   }

   @Override
   public B applyDeep(final Consumer<? super ComponentBuilder<?, ?>> consumer) {
      this.apply(consumer);
      if (this.children.isEmpty()) {
         return (B)this;
      } else {
         ListIterator<Component> it = this.children.listIterator();

         while (it.hasNext()) {
            ComponentBuilder<?, ?> childBuilder = it.next().toBuilder();
            childBuilder.applyDeep(consumer);
            it.set(childBuilder.build());
         }

         return (B)this;
      }
   }

   @Override
   public B mapChildren(final Function<Component, ? extends Component> function) {
      if (this.children.isEmpty()) {
         return (B)this;
      } else {
         ListIterator<Component> it = this.children.listIterator();

         while (it.hasNext()) {
            Component child = it.next();
            Component mappedChild = Objects.requireNonNull(function.apply(child), "mappedChild");
            if (child != mappedChild) {
               it.set(mappedChild);
            }
         }

         return (B)this;
      }
   }

   @Override
   public B mapChildrenDeep(final Function<Component, ? extends Component> function) {
      if (this.children.isEmpty()) {
         return (B)this;
      } else {
         ListIterator<Component> it = this.children.listIterator();

         while (it.hasNext()) {
            Component child = it.next();
            Component mappedChild = Objects.requireNonNull(function.apply(child), "mappedChild");
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

         return (B)this;
      }
   }

   @Override
   public List<Component> children() {
      return List.copyOf(this.children);
   }

   @Override
   public B style(final Style style) {
      this.style = style;
      this.styleBuilder = null;
      return (B)this;
   }

   @Override
   public B style(final Consumer<Style.Builder> consumer) {
      consumer.accept(this.styleBuilder());
      return (B)this;
   }

   @Override
   public B font(@Nullable final Key font) {
      this.styleBuilder().font(font);
      return (B)this;
   }

   @Override
   public B color(@Nullable final TextColor color) {
      this.styleBuilder().color(color);
      return (B)this;
   }

   @Override
   public B colorIfAbsent(@Nullable final TextColor color) {
      this.styleBuilder().colorIfAbsent(color);
      return (B)this;
   }

   public B shadowColor(@Nullable final ARGBLike argb) {
      this.styleBuilder().shadowColor(argb);
      return (B)this;
   }

   public B shadowColorIfAbsent(@Nullable final ARGBLike argb) {
      this.styleBuilder().shadowColorIfAbsent(argb);
      return (B)this;
   }

   @Override
   public B decoration(final TextDecoration decoration, final TextDecoration.State state) {
      this.styleBuilder().decoration(decoration, state);
      return (B)this;
   }

   @Override
   public B decorationIfAbsent(final TextDecoration decoration, final TextDecoration.State state) {
      this.styleBuilder().decorationIfAbsent(decoration, state);
      return (B)this;
   }

   @Override
   public B clickEvent(@Nullable final ClickEvent<?> event) {
      this.styleBuilder().clickEvent(event);
      return (B)this;
   }

   @Override
   public B hoverEvent(@Nullable final HoverEventSource<?> source) {
      this.styleBuilder().hoverEvent(source);
      return (B)this;
   }

   @Override
   public B insertion(@Nullable final String insertion) {
      this.styleBuilder().insertion(insertion);
      return (B)this;
   }

   @Override
   public B mergeStyle(final Component that, final Set<Style.Merge> merges) {
      Style thatStyle = Objects.requireNonNull(that, "that").style();
      if (thatStyle.isEmpty() && merges.isEmpty()) {
         return (B)this;
      } else {
         this.styleBuilder().merge(thatStyle, merges);
         return (B)this;
      }
   }

   @Override
   public B resetStyle() {
      this.style = null;
      this.styleBuilder = null;
      return (B)this;
   }

   private Style.Builder styleBuilder() {
      if (this.styleBuilder == null) {
         if (this.style != null) {
            this.styleBuilder = this.style.toBuilder();
            this.style = null;
         } else {
            this.styleBuilder = Style.style();
         }
      }

      return this.styleBuilder;
   }

   protected final boolean hasStyle() {
      return this.styleBuilder != null || this.style != null;
   }

   protected Style buildStyle() {
      if (this.styleBuilder != null) {
         return this.styleBuilder.build();
      } else {
         return this.style != null ? this.style : Style.empty();
      }
   }
}
