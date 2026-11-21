package net.kyori.adventure.text;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.MutableStyleSetter;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jspecify.annotations.Nullable;

@NonExtendable
public sealed interface ComponentBuilder<C extends Component, B extends ComponentBuilder<C, B>>
   extends AbstractBuilder<C>,
   ComponentBuilderApplicable,
   ComponentLike,
   MutableStyleSetter<B>
   permits AbstractComponentBuilder,
   KeybindComponent.Builder,
   NBTComponentBuilder,
   ObjectComponent.Builder,
   ScoreComponent.Builder,
   SelectorComponent.Builder,
   TextComponent.Builder,
   TranslatableComponent.Builder {
   @Contract("_ -> this")
   B append(final Component component);

   @Contract("_ -> this")
   default B append(final ComponentLike component) {
      return this.append(component.asComponent());
   }

   @Contract("_ -> this")
   default B append(final ComponentBuilder<?, ?> builder) {
      return this.append(builder.build());
   }

   @Contract("_ -> this")
   B append(final Component... components);

   @Contract("_ -> this")
   B append(final ComponentLike... components);

   @Contract("_ -> this")
   B append(final Iterable<? extends ComponentLike> components);

   default B appendNewline() {
      return this.append((Component)Component.newline());
   }

   default B appendSpace() {
      return this.append((Component)Component.space());
   }

   @Contract("_ -> this")
   default B apply(final Consumer<? super ComponentBuilder<?, ?>> consumer) {
      consumer.accept(this);
      return (B)this;
   }

   @Contract("_ -> this")
   B applyDeep(final Consumer<? super ComponentBuilder<?, ?>> action);

   @Contract("_ -> this")
   B mapChildren(final Function<Component, ? extends Component> function);

   @Contract("_ -> this")
   B mapChildrenDeep(final Function<Component, ? extends Component> function);

   List<Component> children();

   @Contract("_ -> this")
   B style(final Style style);

   @Contract("_ -> this")
   B style(final Consumer<Style.Builder> consumer);

   @Contract("_ -> this")
   B font(@Nullable final Key font);

   @Contract("_ -> this")
   B color(@Nullable final TextColor color);

   @Contract("_ -> this")
   B colorIfAbsent(@Nullable final TextColor color);

   @Contract("_, _ -> this")
   default B decorations(final Set<TextDecoration> decorations, final boolean flag) {
      return (B)MutableStyleSetter.super.decorations(decorations, flag);
   }

   @Contract("_ -> this")
   default B decorate(final TextDecoration decoration) {
      return this.decoration(decoration, TextDecoration.State.TRUE);
   }

   @Contract("_ -> this")
   default B decorate(final TextDecoration... decorations) {
      return (B)MutableStyleSetter.super.decorate(decorations);
   }

   @Contract("_, _ -> this")
   default B decoration(final TextDecoration decoration, final boolean flag) {
      return this.decoration(decoration, TextDecoration.State.byBoolean(flag));
   }

   @Contract("_ -> this")
   default B decorations(final Map<TextDecoration, TextDecoration.State> decorations) {
      return (B)MutableStyleSetter.super.decorations(decorations);
   }

   @Contract("_, _ -> this")
   B decoration(final TextDecoration decoration, final TextDecoration.State state);

   @Contract("_, _ -> this")
   B decorationIfAbsent(final TextDecoration decoration, final TextDecoration.State state);

   @Contract("_ -> this")
   B clickEvent(@Nullable final ClickEvent<?> event);

   @Contract("_ -> this")
   B hoverEvent(@Nullable final HoverEventSource<?> source);

   @Contract("_ -> this")
   B insertion(@Nullable final String insertion);

   @Contract("_ -> this")
   default B mergeStyle(final Component that) {
      return this.mergeStyle(that, Style.Merge.all());
   }

   @Contract("_, _ -> this")
   default B mergeStyle(final Component that, final Style.Merge... merges) {
      return this.mergeStyle(that, Style.Merge.merges(merges));
   }

   @Contract("_, _ -> this")
   B mergeStyle(final Component that, final Set<Style.Merge> merges);

   B resetStyle();

   C build();

   @Contract("_ -> this")
   default B applicableApply(final ComponentBuilderApplicable applicable) {
      applicable.componentBuilderApply(this);
      return (B)this;
   }

   @Override
   default void componentBuilderApply(final ComponentBuilder<?, ?> component) {
      component.append(this);
   }

   @Override
   default Component asComponent() {
      return this.build();
   }
}
