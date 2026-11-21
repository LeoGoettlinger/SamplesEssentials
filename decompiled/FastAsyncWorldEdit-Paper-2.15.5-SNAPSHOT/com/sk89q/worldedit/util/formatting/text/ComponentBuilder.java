package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.Style;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ComponentBuilder<C extends BuildableComponent<C, B>, B extends ComponentBuilder<C, B>> {
   @NonNull
   default B append(@NonNull final String content) {
      return this.append(TextComponent.of(content));
   }

   @NonNull
   default B append(@NonNull final String content, @NonNull final TextColor color) {
      return this.append(TextComponent.of(content, color));
   }

   @NonNull
   default B append(@NonNull final String content, @NonNull final TextColor color, @NonNull final TextDecoration... decorations) {
      return this.append(TextComponent.of(content, color, decorations));
   }

   @NonNull
   default B append(@NonNull final String content, @NonNull final Consumer<? super TextComponent.Builder> builder) {
      return this.append(TextComponent.make(content, builder));
   }

   @NonNull
   B append(@NonNull final Component component);

   @NonNull
   default B append(@NonNull final ComponentBuilder<?, ?> builder) {
      return this.append(builder.build());
   }

   @NonNull
   B append(@NonNull final Component... components);

   @NonNull
   B append(@NonNull final Iterable<? extends Component> components);

   @NonNull
   default B apply(@NonNull final Consumer<? super ComponentBuilder<?, ?>> consumer) {
      consumer.accept(this);
      return (B)this;
   }

   @NonNull
   B applyDeep(@NonNull final Consumer<? super ComponentBuilder<?, ?>> action);

   @NonNull
   B mapChildren(@NonNull final Function<BuildableComponent<?, ?>, ? extends BuildableComponent<?, ?>> function);

   @NonNull
   B mapChildrenDeep(@NonNull final Function<BuildableComponent<?, ?>, ? extends BuildableComponent<?, ?>> function);

   @NonNull
   B style(@NonNull final Style style);

   @NonNull
   B style(@NonNull final Consumer<Style.Builder> consumer);

   @NonNull
   B color(@Nullable final TextColor color);

   @NonNull
   B colorIfAbsent(@Nullable final TextColor color);

   @NonNull
   default B decorations(@NonNull final Set<TextDecoration> decorations, final boolean flag) {
      TextDecoration.State state = TextDecoration.State.byBoolean(flag);
      decorations.forEach(decoration -> this.decoration(decoration, state));
      return (B)this;
   }

   @NonNull
   default B decoration(@NonNull final TextDecoration decoration, final boolean flag) {
      return this.decoration(decoration, TextDecoration.State.byBoolean(flag));
   }

   @NonNull
   B decoration(@NonNull final TextDecoration decoration, @NonNull final TextDecoration.State state);

   @NonNull
   B clickEvent(@Nullable final ClickEvent event);

   @NonNull
   B hoverEvent(@Nullable final HoverEvent event);

   @NonNull
   B insertion(@Nullable final String insertion);

   @NonNull
   default B mergeStyle(@NonNull final Component that) {
      return this.mergeStyle(that, Style.Merge.all());
   }

   @NonNull
   default B mergeStyle(@NonNull final Component that, @NonNull final Style.Merge... merges) {
      return this.mergeStyle(that, Style.Merge.of(merges));
   }

   @NonNull
   B mergeStyle(@NonNull final Component that, @NonNull final Set<Style.Merge> merges);

   @Deprecated
   @NonNull
   default B mergeColor(@NonNull final Component that) {
      return this.mergeStyle(that, Collections.singleton(Style.Merge.COLOR));
   }

   @Deprecated
   @NonNull
   default B mergeDecorations(@NonNull final Component that) {
      return this.mergeStyle(that, Collections.singleton(Style.Merge.DECORATIONS));
   }

   @Deprecated
   @NonNull
   default B mergeEvents(@NonNull final Component that) {
      return this.mergeStyle(that, Collections.singleton(Style.Merge.EVENTS));
   }

   @NonNull
   B resetStyle();

   @NonNull
   C build();
}
