package me.clip.placeholderapi.libs.kyori.adventure.text;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import me.clip.placeholderapi.libs.kyori.adventure.key.Key;
import me.clip.placeholderapi.libs.kyori.adventure.text.event.ClickEvent;
import me.clip.placeholderapi.libs.kyori.adventure.text.event.HoverEventSource;
import me.clip.placeholderapi.libs.kyori.adventure.text.format.Style;
import me.clip.placeholderapi.libs.kyori.adventure.text.format.StyleBuilderApplicable;
import me.clip.placeholderapi.libs.kyori.adventure.text.format.TextColor;
import me.clip.placeholderapi.libs.kyori.adventure.text.format.TextDecoration;
import me.clip.placeholderapi.libs.kyori.adventure.util.ARGBLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ScopedComponent<C extends Component> extends Component {
   @NotNull
   @Override
   default C asComponent() {
      return (C)Component.super.asComponent();
   }

   @NotNull
   @Override
   C children(@NotNull final List<? extends ComponentLike> children);

   @NotNull
   @Override
   C style(@NotNull final Style style);

   @NotNull
   @Override
   default C style(@NotNull final Consumer<Style.Builder> style) {
      return (C)Component.super.style(style);
   }

   @NotNull
   @Override
   default C style(@NotNull final Style.Builder style) {
      return (C)Component.super.style(style);
   }

   @NotNull
   @Override
   default C style(@NotNull final Consumer<Style.Builder> consumer, @NotNull final Style.Merge.Strategy strategy) {
      return (C)Component.super.style(consumer, strategy);
   }

   @NotNull
   @Override
   default C mergeStyle(@NotNull final Component that) {
      return (C)Component.super.mergeStyle(that);
   }

   @NotNull
   @Override
   default C mergeStyle(@NotNull final Component that, @NotNull final Style.Merge... merges) {
      return (C)Component.super.mergeStyle(that, merges);
   }

   @NotNull
   @Override
   default C append(@NotNull final Component component) {
      return (C)Component.super.append(component);
   }

   @NotNull
   @Override
   default C append(@NotNull final ComponentLike like) {
      return (C)Component.super.append(like);
   }

   @NotNull
   @Override
   default C append(@NotNull final ComponentBuilder<?, ?> builder) {
      return (C)Component.super.append(builder);
   }

   @NotNull
   @Override
   default C append(@NotNull final List<? extends ComponentLike> components) {
      return (C)Component.super.append(components);
   }

   @NotNull
   @Override
   default C append(@NotNull final ComponentLike... components) {
      return (C)Component.super.append(components);
   }

   @NotNull
   @Override
   default C appendNewline() {
      return (C)Component.super.appendNewline();
   }

   @NotNull
   @Override
   default C appendSpace() {
      return (C)Component.super.appendSpace();
   }

   @NotNull
   @Override
   default C applyFallbackStyle(@NotNull final StyleBuilderApplicable... style) {
      return (C)Component.super.applyFallbackStyle(style);
   }

   @NotNull
   @Override
   default C applyFallbackStyle(@NotNull final Style style) {
      return (C)Component.super.applyFallbackStyle(style);
   }

   @NotNull
   @Override
   default C mergeStyle(@NotNull final Component that, @NotNull final Set<Style.Merge> merges) {
      return (C)Component.super.mergeStyle(that, merges);
   }

   @NotNull
   @Override
   default C color(@Nullable final TextColor color) {
      return (C)Component.super.color(color);
   }

   @NotNull
   @Override
   default C colorIfAbsent(@Nullable final TextColor color) {
      return (C)Component.super.colorIfAbsent(color);
   }

   @NotNull
   @Override
   default C shadowColor(@Nullable final ARGBLike argb) {
      return (C)Component.super.shadowColor(argb);
   }

   @NotNull
   @Override
   default C shadowColorIfAbsent(@Nullable final ARGBLike argb) {
      return (C)Component.super.shadowColorIfAbsent(argb);
   }

   @NotNull
   @Override
   default C decorate(@NotNull final TextDecoration decoration) {
      return (C)Component.super.decorate(decoration);
   }

   @NotNull
   @Override
   default C decoration(@NotNull final TextDecoration decoration, final boolean flag) {
      return (C)Component.super.decoration(decoration, flag);
   }

   @NotNull
   @Override
   default C decoration(@NotNull final TextDecoration decoration, @NotNull final TextDecoration.State state) {
      return (C)Component.super.decoration(decoration, state);
   }

   @NotNull
   @Override
   default C decorationIfAbsent(@NotNull final TextDecoration decoration, @NotNull final TextDecoration.State state) {
      return (C)Component.super.decorationIfAbsent(decoration, state);
   }

   @NotNull
   @Override
   default C decorations(@NotNull final Map<TextDecoration, TextDecoration.State> decorations) {
      return (C)Component.super.decorations(decorations);
   }

   @NotNull
   @Override
   default C clickEvent(@Nullable final ClickEvent event) {
      return (C)Component.super.clickEvent(event);
   }

   @NotNull
   @Override
   default C hoverEvent(@Nullable final HoverEventSource<?> event) {
      return (C)Component.super.hoverEvent(event);
   }

   @NotNull
   @Override
   default C insertion(@Nullable final String insertion) {
      return (C)Component.super.insertion(insertion);
   }

   @NotNull
   @Override
   default C font(@Nullable final Key key) {
      return (C)Component.super.font(key);
   }
}
