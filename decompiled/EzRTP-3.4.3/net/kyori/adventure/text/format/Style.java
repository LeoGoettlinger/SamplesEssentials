package net.kyori.adventure.text.format;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.util.Buildable;
import net.kyori.adventure.util.MonkeyBars;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

public sealed interface Style extends StyleGetter, StyleSetter<Style>, Buildable<Style.Builder> permits StyleImpl {
   Key DEFAULT_FONT = Key.key("default");

   static Style empty() {
      return StyleImpl.EMPTY;
   }

   static Style.Builder style() {
      return new StyleImpl.BuilderImpl();
   }

   static Style style(final Consumer<Style.Builder> consumer) {
      return AbstractBuilder.configureAndBuild(style(), consumer);
   }

   static Style style(@Nullable final TextColor color) {
      return empty().color(color);
   }

   static Style style(final TextDecoration decoration) {
      return style().decoration(decoration, true).build();
   }

   static Style style(@Nullable final TextColor color, final TextDecoration... decorations) {
      Style.Builder builder = style();
      builder.color(color);
      builder.decorate(decorations);
      return builder.build();
   }

   static Style style(@Nullable final TextColor color, final Set<TextDecoration> decorations) {
      Style.Builder builder = style();
      builder.color(color);
      if (!decorations.isEmpty()) {
         for (TextDecoration decoration : decorations) {
            builder.decoration(decoration, true);
         }
      }

      return builder.build();
   }

   static Style style(final StyleBuilderApplicable... applicables) {
      int length = applicables.length;
      if (length == 0) {
         return empty();
      } else {
         Style.Builder builder = style();

         for (StyleBuilderApplicable applicable : applicables) {
            if (applicable != null) {
               applicable.styleApply(builder);
            }
         }

         return builder.build();
      }
   }

   static Style style(final Iterable<? extends StyleBuilderApplicable> applicables) {
      Style.Builder builder = style();

      for (StyleBuilderApplicable applicable : applicables) {
         applicable.styleApply(builder);
      }

      return builder.build();
   }

   default Style edit(final Consumer<Style.Builder> consumer) {
      return this.edit(consumer, Style.Merge.Strategy.ALWAYS);
   }

   default Style edit(final Consumer<Style.Builder> consumer, final Style.Merge.Strategy strategy) {
      return style(style -> {
         if (strategy == Style.Merge.Strategy.ALWAYS) {
            style.merge(this, strategy);
         }

         consumer.accept(style);
         if (strategy == Style.Merge.Strategy.IF_ABSENT_ON_TARGET) {
            style.merge(this, strategy);
         }
      });
   }

   @Nullable
   @Override
   Key font();

   Style font(@Nullable final Key font);

   @Nullable
   @Override
   TextColor color();

   Style color(@Nullable final TextColor color);

   Style colorIfAbsent(@Nullable final TextColor color);

   @Override
   default boolean hasDecoration(final TextDecoration decoration) {
      return StyleGetter.super.hasDecoration(decoration);
   }

   @Override
   TextDecoration.State decoration(final TextDecoration decoration);

   default Style decorate(final TextDecoration decoration) {
      return (Style)StyleSetter.super.decorate(decoration);
   }

   default Style decoration(final TextDecoration decoration, final boolean flag) {
      return (Style)StyleSetter.super.decoration(decoration, flag);
   }

   Style decoration(final TextDecoration decoration, final TextDecoration.State state);

   Style decorationIfAbsent(final TextDecoration decoration, final TextDecoration.State state);

   @Unmodifiable
   @Override
   default Map<TextDecoration, TextDecoration.State> decorations() {
      return StyleGetter.super.decorations();
   }

   Style decorations(final Map<TextDecoration, TextDecoration.State> decorations);

   @Nullable
   @Override
   ClickEvent<?> clickEvent();

   Style clickEvent(@Nullable final ClickEvent<?> event);

   @Nullable
   @Override
   HoverEvent<?> hoverEvent();

   Style hoverEvent(@Nullable final HoverEventSource<?> source);

   @Nullable
   @Override
   String insertion();

   Style insertion(@Nullable final String insertion);

   default Style merge(final Style that) {
      return this.merge(that, Style.Merge.all());
   }

   default Style merge(final Style that, final Style.Merge.Strategy strategy) {
      return this.merge(that, strategy, Style.Merge.all());
   }

   default Style merge(final Style that, final Style.Merge merge) {
      return this.merge(that, Collections.singleton(merge));
   }

   default Style merge(final Style that, final Style.Merge.Strategy strategy, final Style.Merge merge) {
      return this.merge(that, strategy, Collections.singleton(merge));
   }

   default Style merge(final Style that, final Style.Merge... merges) {
      return this.merge(that, Style.Merge.merges(merges));
   }

   default Style merge(final Style that, final Style.Merge.Strategy strategy, final Style.Merge... merges) {
      return this.merge(that, strategy, Style.Merge.merges(merges));
   }

   default Style merge(final Style that, final Set<Style.Merge> merges) {
      return this.merge(that, Style.Merge.Strategy.ALWAYS, merges);
   }

   Style merge(final Style that, final Style.Merge.Strategy strategy, final Set<Style.Merge> merges);

   Style unmerge(final Style that);

   boolean isEmpty();

   Style.Builder toBuilder();

   public sealed interface Builder extends AbstractBuilder<Style>, MutableStyleSetter<Style.Builder> permits StyleImpl.BuilderImpl {
      @Contract("_ -> this")
      Style.Builder font(@Nullable final Key font);

      @Contract("_ -> this")
      Style.Builder color(@Nullable final TextColor color);

      @Contract("_ -> this")
      Style.Builder colorIfAbsent(@Nullable final TextColor color);

      @Contract("_ -> this")
      default Style.Builder decorate(final TextDecoration decoration) {
         return (Style.Builder)MutableStyleSetter.super.decorate(decoration);
      }

      @Contract("_ -> this")
      default Style.Builder decorate(final TextDecoration... decorations) {
         return (Style.Builder)MutableStyleSetter.super.decorate(decorations);
      }

      @Contract("_, _ -> this")
      default Style.Builder decoration(final TextDecoration decoration, final boolean flag) {
         return (Style.Builder)MutableStyleSetter.super.decoration(decoration, flag);
      }

      @Contract("_ -> this")
      default Style.Builder decorations(final Map<TextDecoration, TextDecoration.State> decorations) {
         return (Style.Builder)MutableStyleSetter.super.decorations(decorations);
      }

      @Contract("_, _ -> this")
      Style.Builder decoration(final TextDecoration decoration, final TextDecoration.State state);

      @Contract("_, _ -> this")
      Style.Builder decorationIfAbsent(final TextDecoration decoration, final TextDecoration.State state);

      @Contract("_ -> this")
      Style.Builder clickEvent(@Nullable final ClickEvent<?> event);

      @Contract("_ -> this")
      Style.Builder hoverEvent(@Nullable final HoverEventSource<?> source);

      @Contract("_ -> this")
      Style.Builder insertion(@Nullable final String insertion);

      @Contract("_ -> this")
      default Style.Builder merge(final Style that) {
         return this.merge(that, Style.Merge.all());
      }

      @Contract("_, _ -> this")
      default Style.Builder merge(final Style that, final Style.Merge.Strategy strategy) {
         return this.merge(that, strategy, Style.Merge.all());
      }

      @Contract("_, _ -> this")
      default Style.Builder merge(final Style that, final Style.Merge... merges) {
         return merges.length == 0 ? this : this.merge(that, Style.Merge.merges(merges));
      }

      @Contract("_, _, _ -> this")
      default Style.Builder merge(final Style that, final Style.Merge.Strategy strategy, final Style.Merge... merges) {
         return merges.length == 0 ? this : this.merge(that, strategy, Style.Merge.merges(merges));
      }

      @Contract("_, _ -> this")
      default Style.Builder merge(final Style that, final Set<Style.Merge> merges) {
         return this.merge(that, Style.Merge.Strategy.ALWAYS, merges);
      }

      @Contract("_, _, _ -> this")
      Style.Builder merge(final Style that, final Style.Merge.Strategy strategy, final Set<Style.Merge> merges);

      @Contract("_ -> this")
      default Style.Builder apply(final StyleBuilderApplicable applicable) {
         applicable.styleApply(this);
         return this;
      }

      Style build();
   }

   public static enum Merge {
      COLOR,
      SHADOW_COLOR,
      DECORATIONS,
      EVENTS,
      INSERTION,
      FONT;

      static final Set<Style.Merge> ALL = merges(values());
      static final Set<Style.Merge> COLOR_AND_DECORATIONS = merges(COLOR, DECORATIONS);

      @Unmodifiable
      public static Set<Style.Merge> all() {
         return ALL;
      }

      @Unmodifiable
      public static Set<Style.Merge> colorAndDecorations() {
         return COLOR_AND_DECORATIONS;
      }

      @Unmodifiable
      public static Set<Style.Merge> merges(final Style.Merge... merges) {
         return MonkeyBars.enumSet(Style.Merge.class, merges);
      }

      static boolean hasAll(final Set<Style.Merge> merges) {
         return merges.size() == ALL.size();
      }

      public static enum Strategy {
         ALWAYS,
         NEVER,
         IF_ABSENT_ON_TARGET;
      }
   }
}
