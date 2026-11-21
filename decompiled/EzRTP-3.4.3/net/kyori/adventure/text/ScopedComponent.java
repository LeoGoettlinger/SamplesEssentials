package net.kyori.adventure.text;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.ARGBLike;
import org.jspecify.annotations.Nullable;

public sealed interface ScopedComponent<C extends Component>
   extends Component
   permits BlockNBTComponent,
   EntityNBTComponent,
   KeybindComponent,
   ObjectComponent,
   ScoreComponent,
   SelectorComponent,
   StorageNBTComponent,
   TextComponent,
   TranslatableComponent {
   @Override
   default C asComponent() {
      return (C)Component.super.asComponent();
   }

   @Override
   C children(final List<? extends ComponentLike> children);

   @Override
   C style(final Style style);

   @Override
   default C style(final Consumer<Style.Builder> style) {
      return (C)Component.super.style(style);
   }

   @Override
   default C style(final Style.Builder style) {
      return (C)Component.super.style(style);
   }

   @Override
   default C style(final Consumer<Style.Builder> consumer, final Style.Merge.Strategy strategy) {
      return (C)Component.super.style(consumer, strategy);
   }

   @Override
   default C mergeStyle(final Component that) {
      return (C)Component.super.mergeStyle(that);
   }

   @Override
   default C mergeStyle(final Component that, final Style.Merge... merges) {
      return (C)Component.super.mergeStyle(that, merges);
   }

   @Override
   default C append(final Component component) {
      return (C)Component.super.append(component);
   }

   @Override
   default C append(final ComponentLike like) {
      return (C)Component.super.append(like);
   }

   @Override
   default C append(final ComponentBuilder<?, ?> builder) {
      return (C)Component.super.append(builder);
   }

   @Override
   default C append(final List<? extends ComponentLike> components) {
      return (C)Component.super.append(components);
   }

   @Override
   default C append(final ComponentLike... components) {
      return (C)Component.super.append(components);
   }

   @Override
   default C appendNewline() {
      return (C)Component.super.appendNewline();
   }

   @Override
   default C appendSpace() {
      return (C)Component.super.appendSpace();
   }

   @Override
   default C applyFallbackStyle(final StyleBuilderApplicable... style) {
      return (C)Component.super.applyFallbackStyle(style);
   }

   @Override
   default C applyFallbackStyle(final Style style) {
      return (C)Component.super.applyFallbackStyle(style);
   }

   @Override
   default C mergeStyle(final Component that, final Set<Style.Merge> merges) {
      return (C)Component.super.mergeStyle(that, merges);
   }

   @Override
   default C color(@Nullable final TextColor color) {
      return (C)Component.super.color(color);
   }

   @Override
   default C colorIfAbsent(@Nullable final TextColor color) {
      return (C)Component.super.colorIfAbsent(color);
   }

   @Override
   default C shadowColor(@Nullable final ARGBLike argb) {
      return (C)Component.super.shadowColor(argb);
   }

   @Override
   default C shadowColorIfAbsent(@Nullable final ARGBLike argb) {
      return (C)Component.super.shadowColorIfAbsent(argb);
   }

   @Override
   default C decorate(final TextDecoration decoration) {
      return (C)Component.super.decorate(decoration);
   }

   @Override
   default C decoration(final TextDecoration decoration, final boolean flag) {
      return (C)Component.super.decoration(decoration, flag);
   }

   @Override
   default C decoration(final TextDecoration decoration, final TextDecoration.State state) {
      return (C)Component.super.decoration(decoration, state);
   }

   @Override
   default C decorationIfAbsent(final TextDecoration decoration, final TextDecoration.State state) {
      return (C)Component.super.decorationIfAbsent(decoration, state);
   }

   @Override
   default C decorations(final Map<TextDecoration, TextDecoration.State> decorations) {
      return (C)Component.super.decorations(decorations);
   }

   @Override
   default C clickEvent(@Nullable final ClickEvent<?> event) {
      return (C)Component.super.clickEvent(event);
   }

   @Override
   default C hoverEvent(@Nullable final HoverEventSource<?> event) {
      return (C)Component.super.hoverEvent(event);
   }

   @Override
   default C insertion(@Nullable final String insertion) {
      return (C)Component.super.insertion(insertion);
   }

   @Override
   default C font(@Nullable final Key key) {
      return (C)Component.super.font(key);
   }

   @Override
   ComponentBuilder<C, ?> toBuilder();
}
