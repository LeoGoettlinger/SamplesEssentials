package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.Style;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Component {
   @NonNull
   List<Component> children();

   @NonNull
   Component children(@NonNull final List<Component> children);

   default boolean contains(@NonNull final Component that) {
      if (this == that) {
         return true;
      } else {
         for (Component child : this.children()) {
            if (child.contains(that)) {
               return true;
            }
         }

         HoverEvent hoverEvent = this.hoverEvent();
         if (hoverEvent != null) {
            Component hover = hoverEvent.value();
            if (that == hover) {
               return true;
            }

            for (Component childx : hover.children()) {
               if (childx.contains(that)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   default void detectCycle(@NonNull final Component that) {
      if (that.contains(this)) {
         throw new IllegalStateException("Component cycle detected between " + this + " and " + that);
      }
   }

   @NonNull
   Component append(@NonNull final Component component);

   @NonNull
   default Component append(@NonNull final ComponentBuilder<?, ?> builder) {
      return this.append(builder.build());
   }

   @NonNull
   Style style();

   @NonNull
   Component style(@NonNull final Style style);

   @NonNull
   default Component style(@NonNull final Consumer<Style.Builder> consumer) {
      return this.style(this.style().edit(consumer));
   }

   @NonNull
   default Component style(@NonNull final Consumer<Style.Builder> consumer, @NonNull final Style.Merge.Strategy strategy) {
      return this.style(this.style().edit(consumer, strategy));
   }

   @NonNull
   default Component style(@NonNull final Style.Builder style) {
      return this.style(style.build());
   }

   @NonNull
   default Component mergeStyle(@NonNull final Component that) {
      return this.mergeStyle(that, Style.Merge.all());
   }

   @NonNull
   default Component mergeStyle(@NonNull final Component that, @NonNull final Style.Merge... merges) {
      return this.mergeStyle(that, Style.Merge.of(merges));
   }

   @NonNull
   default Component mergeStyle(@NonNull final Component that, @NonNull final Set<Style.Merge> merges) {
      return this.style(this.style().merge(that.style(), merges));
   }

   @Nullable
   default TextColor color() {
      return this.style().color();
   }

   @NonNull
   default Component color(@Nullable final TextColor color) {
      return this.style(this.style().color(color));
   }

   @NonNull
   default Component colorIfAbsent(@Nullable final TextColor color) {
      return this.color() == null ? this.color(color) : this;
   }

   default boolean hasDecoration(@NonNull final TextDecoration decoration) {
      return this.decoration(decoration) == TextDecoration.State.TRUE;
   }

   @NonNull
   default TextDecoration.State decoration(@NonNull final TextDecoration decoration) {
      return this.style().decoration(decoration);
   }

   @NonNull
   default Component decoration(@NonNull final TextDecoration decoration, final boolean flag) {
      return this.decoration(decoration, TextDecoration.State.byBoolean(flag));
   }

   @NonNull
   default Component decoration(@NonNull final TextDecoration decoration, @NonNull final TextDecoration.State state) {
      return this.style(this.style().decoration(decoration, state));
   }

   @NonNull
   default Set<TextDecoration> decorations() {
      return this.decorations(Collections.emptySet());
   }

   @NonNull
   default Set<TextDecoration> decorations(@NonNull final Set<TextDecoration> defaultValues) {
      return this.style().decorations(defaultValues);
   }

   @Nullable
   default ClickEvent clickEvent() {
      return this.style().clickEvent();
   }

   @NonNull
   default Component clickEvent(@Nullable final ClickEvent event) {
      return this.style(this.style().clickEvent(event));
   }

   @Nullable
   default HoverEvent hoverEvent() {
      return this.style().hoverEvent();
   }

   @NonNull
   default Component hoverEvent(@Nullable final HoverEvent event) {
      if (event != null) {
         this.detectCycle(event.value());
      }

      return this.style(this.style().hoverEvent(event));
   }

   @Nullable
   default String insertion() {
      return this.style().insertion();
   }

   @NonNull
   default Component insertion(@Nullable final String insertion) {
      return this.style(this.style().insertion(insertion));
   }

   @Deprecated
   @NonNull
   default Component mergeColor(@NonNull final Component that) {
      return this.style(this.style().mergeColor(that.style()));
   }

   @Deprecated
   @NonNull
   default Component mergeDecorations(@NonNull final Component that) {
      return this.style(this.style().mergeDecorations(that.style()));
   }

   @Deprecated
   @NonNull
   default Component mergeEvents(@NonNull final Component that) {
      return this.style(this.style().mergeEvents(that.style()));
   }

   default boolean hasStyling() {
      return !this.style().isEmpty();
   }
}
