package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.format.Style;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import com.sk89q.worldedit.util.formatting.text.util.ShadyPines;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface TextComponent extends BuildableComponent<TextComponent, TextComponent.Builder>, ScopedComponent<TextComponent> {
   @NonNull
   static TextComponent empty() {
      return TextComponentImpl.EMPTY;
   }

   @NonNull
   static TextComponent newline() {
      return TextComponentImpl.NEWLINE;
   }

   @NonNull
   static TextComponent space() {
      return TextComponentImpl.SPACE;
   }

   @NonNull
   static TextComponent join(@NonNull final Component separator, final Component... components) {
      return join(separator, Arrays.asList(components));
   }

   @NonNull
   static TextComponent join(@NonNull final Component separator, final Iterable<? extends Component> components) {
      Iterator<? extends Component> it = components.iterator();
      if (!it.hasNext()) {
         return empty();
      } else {
         TextComponent.Builder builder = builder();

         while (it.hasNext()) {
            builder.append(it.next());
            if (it.hasNext()) {
               builder.append(separator);
            }
         }

         return builder.build();
      }
   }

   @NonNull
   static TextComponent of(@NonNull final String content) {
      return content.isEmpty() ? empty() : builder(content).build();
   }

   @NonNull
   static TextComponent of(@NonNull final String content, @Nullable final TextColor color) {
      return builder(content).color(color).build();
   }

   @NonNull
   static TextComponent of(@NonNull final String content, @Nullable final TextColor color, @NonNull final TextDecoration... decorations) {
      return of(content, color, ShadyPines.enumSet(TextDecoration.class, decorations));
   }

   @NonNull
   static TextComponent of(@NonNull final String content, @Nullable final TextColor color, @NonNull final Set<TextDecoration> decorations) {
      return builder(content).color(color).decorations(decorations, true).build();
   }

   @NonNull
   static TextComponent of(@NonNull final String content, @NonNull final Style style) {
      return builder(content).style(style).build();
   }

   @NonNull
   static TextComponent of(final boolean value) {
      return of(String.valueOf(value));
   }

   @NonNull
   static TextComponent of(final boolean value, @Nullable final TextColor color) {
      return builder(String.valueOf(value)).color(color).build();
   }

   @NonNull
   static TextComponent of(final boolean value, @Nullable final TextColor color, @NonNull final TextDecoration... decorations) {
      return of(String.valueOf(value), color, ShadyPines.enumSet(TextDecoration.class, decorations));
   }

   @NonNull
   static TextComponent of(final boolean value, @Nullable final TextColor color, @NonNull final Set<TextDecoration> decorations) {
      return builder(String.valueOf(value)).color(color).decorations(decorations, true).build();
   }

   @NonNull
   static TextComponent of(final boolean value, @NonNull final Style style) {
      return builder(String.valueOf(value)).style(style).build();
   }

   @NonNull
   static TextComponent of(final char value) {
      if (value == '\n') {
         return newline();
      } else {
         return value == ' ' ? space() : of(String.valueOf(value));
      }
   }

   @NonNull
   static TextComponent of(final char value, @Nullable final TextColor color) {
      return builder(String.valueOf(value)).color(color).build();
   }

   @NonNull
   static TextComponent of(final char value, @Nullable final TextColor color, @NonNull final TextDecoration... decorations) {
      return of(String.valueOf(value), color, ShadyPines.enumSet(TextDecoration.class, decorations));
   }

   @NonNull
   static TextComponent of(final char value, @Nullable final TextColor color, @NonNull final Set<TextDecoration> decorations) {
      return builder(String.valueOf(value)).color(color).decorations(decorations, true).build();
   }

   @NonNull
   static TextComponent of(final char value, @NonNull final Style style) {
      return builder(String.valueOf(value)).style(style).build();
   }

   @NonNull
   static TextComponent of(final double value) {
      return of(String.valueOf(value));
   }

   @NonNull
   static TextComponent of(final double value, @Nullable final TextColor color) {
      return builder(String.valueOf(value)).color(color).build();
   }

   @NonNull
   static TextComponent of(final double value, @Nullable final TextColor color, @NonNull final TextDecoration... decorations) {
      return of(String.valueOf(value), color, ShadyPines.enumSet(TextDecoration.class, decorations));
   }

   @NonNull
   static TextComponent of(final double value, @Nullable final TextColor color, @NonNull final Set<TextDecoration> decorations) {
      return builder(String.valueOf(value)).color(color).decorations(decorations, true).build();
   }

   @NonNull
   static TextComponent of(final double value, @NonNull final Style style) {
      return builder(String.valueOf(value)).style(style).build();
   }

   @NonNull
   static TextComponent of(final float value) {
      return of(String.valueOf(value));
   }

   @NonNull
   static TextComponent of(final float value, @Nullable final TextColor color) {
      return builder(String.valueOf(value)).color(color).build();
   }

   @NonNull
   static TextComponent of(final float value, @Nullable final TextColor color, @NonNull final TextDecoration... decorations) {
      return of(String.valueOf(value), color, ShadyPines.enumSet(TextDecoration.class, decorations));
   }

   @NonNull
   static TextComponent of(final float value, @Nullable final TextColor color, @NonNull final Set<TextDecoration> decorations) {
      return builder(String.valueOf(value)).color(color).decorations(decorations, true).build();
   }

   @NonNull
   static TextComponent of(final float value, @NonNull final Style style) {
      return builder(String.valueOf(value)).style(style).build();
   }

   @NonNull
   static TextComponent of(final int value) {
      return of(String.valueOf(value));
   }

   @NonNull
   static TextComponent of(final int value, @Nullable final TextColor color) {
      return builder(String.valueOf(value)).color(color).build();
   }

   @NonNull
   static TextComponent of(final int value, @Nullable final TextColor color, @NonNull final TextDecoration... decorations) {
      return of(String.valueOf(value), color, ShadyPines.enumSet(TextDecoration.class, decorations));
   }

   @NonNull
   static TextComponent of(final int value, @Nullable final TextColor color, @NonNull final Set<TextDecoration> decorations) {
      return builder(String.valueOf(value)).color(color).decorations(decorations, true).build();
   }

   @NonNull
   static TextComponent of(final int value, @NonNull final Style style) {
      return builder(String.valueOf(value)).style(style).build();
   }

   @NonNull
   static TextComponent of(final long value) {
      return of(String.valueOf(value));
   }

   @NonNull
   static TextComponent of(final long value, @Nullable final TextColor color) {
      return builder(String.valueOf(value)).color(color).build();
   }

   @NonNull
   static TextComponent of(final long value, @Nullable final TextColor color, @NonNull final TextDecoration... decorations) {
      return of(String.valueOf(value), color, ShadyPines.enumSet(TextDecoration.class, decorations));
   }

   @NonNull
   static TextComponent of(final long value, @Nullable final TextColor color, @NonNull final Set<TextDecoration> decorations) {
      return builder(String.valueOf(value)).color(color).decorations(decorations, true).build();
   }

   @NonNull
   static TextComponent of(final long value, @NonNull final Style style) {
      return builder(String.valueOf(value)).style(style).build();
   }

   @NonNull
   static TextComponent.Builder builder() {
      return new TextComponentImpl.BuilderImpl();
   }

   @NonNull
   static TextComponent.Builder builder(@NonNull final String content) {
      return builder().content(content);
   }

   @NonNull
   static TextComponent.Builder builder(@NonNull final String content, @Nullable final TextColor color) {
      return builder(content).color(color);
   }

   @NonNull
   static TextComponent make(@NonNull final Consumer<? super TextComponent.Builder> consumer) {
      TextComponent.Builder builder = builder();
      return AbstractComponentBuilder.configureAndBuild(builder, consumer);
   }

   @NonNull
   static TextComponent make(@NonNull final String content, @NonNull final Consumer<? super TextComponent.Builder> consumer) {
      TextComponent.Builder builder = builder(content);
      return AbstractComponentBuilder.configureAndBuild(builder, consumer);
   }

   @NonNull
   String content();

   @NonNull
   TextComponent content(@NonNull final String content);

   boolean isEmpty();

   public interface Builder extends ComponentBuilder<TextComponent, TextComponent.Builder> {
      @NonNull
      TextComponent.Builder content(@NonNull final String content);
   }
}
