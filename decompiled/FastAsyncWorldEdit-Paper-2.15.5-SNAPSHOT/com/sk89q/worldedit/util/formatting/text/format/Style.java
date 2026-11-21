package com.sk89q.worldedit.util.formatting.text.format;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.util.ShadyPines;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Style {
   private static final Style EMPTY = new Style(
      null,
      TextDecoration.State.NOT_SET,
      TextDecoration.State.NOT_SET,
      TextDecoration.State.NOT_SET,
      TextDecoration.State.NOT_SET,
      TextDecoration.State.NOT_SET,
      null,
      null,
      null
   );
   private static final TextDecoration[] DECORATIONS = TextDecoration.values();
   @Nullable
   private final TextColor color;
   private final TextDecoration.State obfuscated;
   private final TextDecoration.State bold;
   private final TextDecoration.State strikethrough;
   private final TextDecoration.State underlined;
   private final TextDecoration.State italic;
   @Nullable
   private final ClickEvent clickEvent;
   @Nullable
   private final HoverEvent hoverEvent;
   @Nullable
   private final String insertion;

   @NonNull
   public static Style.Builder builder() {
      return new Style.Builder();
   }

   @NonNull
   public static Style empty() {
      return EMPTY;
   }

   @NonNull
   public static Style of(@Nullable final TextColor color) {
      return color == null ? empty() : builder().color(color).build();
   }

   @NonNull
   public static Style of(@NonNull final TextDecoration decoration) {
      return builder().decoration(decoration, true).build();
   }

   @NonNull
   public static Style of(@NonNull final TextDecoration... decorations) {
      if (decorations.length == 0) {
         return empty();
      } else {
         Style.Builder builder = builder();
         decorate(builder, decorations);
         return builder.build();
      }
   }

   @NonNull
   public static Style of(@Nullable final TextColor color, @NonNull final TextDecoration... decorations) {
      Style.Builder builder = builder();
      builder.color(color);
      decorate(builder, decorations);
      return builder.build();
   }

   private static void decorate(final Style.Builder builder, final TextDecoration[] decorations) {
      int i = 0;

      for (int length = decorations.length; i < length; i++) {
         TextDecoration decoration = decorations[i];
         builder.decoration(decoration, true);
      }
   }

   @NonNull
   public static Style of(@Nullable final TextColor color, final Set<TextDecoration> decorations) {
      Style.Builder builder = builder();
      builder.color(color);
      if (!decorations.isEmpty()) {
         for (TextDecoration decoration : decorations) {
            builder.decoration(decoration, true);
         }
      }

      return builder.build();
   }

   @NonNull
   public static Style make(@NonNull final Consumer<Style.Builder> consumer) {
      Style.Builder builder = builder();
      consumer.accept(builder);
      return builder.build();
   }

   @Deprecated
   @NonNull
   public static Style of(@NonNull final Consumer<Style.Builder> consumer) {
      return make(consumer);
   }

   private Style(
      @Nullable final TextColor color,
      final TextDecoration.State obfuscated,
      final TextDecoration.State bold,
      final TextDecoration.State strikethrough,
      final TextDecoration.State underlined,
      final TextDecoration.State italic,
      @Nullable final ClickEvent clickEvent,
      @Nullable final HoverEvent hoverEvent,
      @Nullable final String insertion
   ) {
      this.color = color;
      this.obfuscated = obfuscated;
      this.bold = bold;
      this.strikethrough = strikethrough;
      this.underlined = underlined;
      this.italic = italic;
      this.clickEvent = clickEvent;
      this.hoverEvent = hoverEvent;
      this.insertion = insertion;
   }

   @NonNull
   public Style edit(@NonNull final Consumer<Style.Builder> consumer) {
      return this.edit(consumer, Style.Merge.Strategy.ALWAYS);
   }

   @NonNull
   public Style edit(@NonNull final Consumer<Style.Builder> consumer, @NonNull final Style.Merge.Strategy strategy) {
      return make(style -> {
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
   public TextColor color() {
      return this.color;
   }

   @NonNull
   public Style colorIfAbsent(@Nullable final TextColor color) {
      return this.color == null ? this.color(color) : this;
   }

   @NonNull
   public Style color(@Nullable final TextColor color) {
      return Objects.equals(this.color, color)
         ? this
         : new Style(color, this.obfuscated, this.bold, this.strikethrough, this.underlined, this.italic, this.clickEvent, this.hoverEvent, this.insertion);
   }

   public boolean hasDecoration(@NonNull final TextDecoration decoration) {
      return this.decoration(decoration) == TextDecoration.State.TRUE;
   }

   @NonNull
   public TextDecoration.State decoration(@NonNull final TextDecoration decoration) {
      if (decoration == TextDecoration.BOLD) {
         return this.bold;
      } else if (decoration == TextDecoration.ITALIC) {
         return this.italic;
      } else if (decoration == TextDecoration.UNDERLINED) {
         return this.underlined;
      } else if (decoration == TextDecoration.STRIKETHROUGH) {
         return this.strikethrough;
      } else if (decoration == TextDecoration.OBFUSCATED) {
         return this.obfuscated;
      } else {
         throw new IllegalArgumentException(String.format("unknown decoration '%s'", decoration));
      }
   }

   @NonNull
   public Style decoration(@NonNull final TextDecoration decoration, final boolean flag) {
      return this.decoration(decoration, TextDecoration.State.byBoolean(flag));
   }

   @NonNull
   public Style decoration(@NonNull final TextDecoration decoration, @NonNull final TextDecoration.State state) {
      Objects.requireNonNull(state, "state");
      if (decoration == TextDecoration.BOLD) {
         return new Style(
            this.color, this.obfuscated, state, this.strikethrough, this.underlined, this.italic, this.clickEvent, this.hoverEvent, this.insertion
         );
      } else if (decoration == TextDecoration.ITALIC) {
         return new Style(this.color, this.obfuscated, this.bold, this.strikethrough, this.underlined, state, this.clickEvent, this.hoverEvent, this.insertion);
      } else if (decoration == TextDecoration.UNDERLINED) {
         return new Style(this.color, this.obfuscated, this.bold, this.strikethrough, state, this.italic, this.clickEvent, this.hoverEvent, this.insertion);
      } else if (decoration == TextDecoration.STRIKETHROUGH) {
         return new Style(this.color, this.obfuscated, this.bold, state, this.underlined, this.italic, this.clickEvent, this.hoverEvent, this.insertion);
      } else if (decoration == TextDecoration.OBFUSCATED) {
         return new Style(this.color, state, this.bold, this.strikethrough, this.underlined, this.italic, this.clickEvent, this.hoverEvent, this.insertion);
      } else {
         throw new IllegalArgumentException(String.format("unknown decoration '%s'", decoration));
      }
   }

   @NonNull
   public Set<TextDecoration> decorations() {
      return this.decorations(Collections.emptySet());
   }

   @NonNull
   public Set<TextDecoration> decorations(@NonNull final Set<TextDecoration> defaultValues) {
      Set<TextDecoration> decorations = EnumSet.noneOf(TextDecoration.class);
      int i = 0;

      for (int length = DECORATIONS.length; i < length; i++) {
         TextDecoration decoration = DECORATIONS[i];
         TextDecoration.State value = this.decoration(decoration);
         if (value == TextDecoration.State.TRUE || value == TextDecoration.State.NOT_SET && defaultValues.contains(decoration)) {
            decorations.add(decoration);
         }
      }

      return decorations;
   }

   @Nullable
   public ClickEvent clickEvent() {
      return this.clickEvent;
   }

   @NonNull
   public Style clickEvent(@Nullable final ClickEvent event) {
      return new Style(this.color, this.obfuscated, this.bold, this.strikethrough, this.underlined, this.italic, event, this.hoverEvent, this.insertion);
   }

   @Nullable
   public HoverEvent hoverEvent() {
      return this.hoverEvent;
   }

   @NonNull
   public Style hoverEvent(@Nullable final HoverEvent event) {
      return new Style(this.color, this.obfuscated, this.bold, this.strikethrough, this.underlined, this.italic, this.clickEvent, event, this.insertion);
   }

   @Nullable
   public String insertion() {
      return this.insertion;
   }

   @NonNull
   public Style insertion(@Nullable final String insertion) {
      return Objects.equals(this.insertion, insertion)
         ? this
         : new Style(this.color, this.obfuscated, this.bold, this.strikethrough, this.underlined, this.italic, this.clickEvent, this.hoverEvent, insertion);
   }

   @NonNull
   public Style merge(@NonNull final Style that) {
      return this.merge(that, Style.Merge.all());
   }

   @NonNull
   public Style merge(@NonNull final Style that, @NonNull final Style.Merge.Strategy strategy) {
      return this.merge(that, strategy, Style.Merge.all());
   }

   @NonNull
   public Style merge(@NonNull final Style that, @NonNull final Style.Merge merge) {
      return this.merge(that, Collections.singleton(merge));
   }

   @NonNull
   public Style merge(@NonNull final Style that, @NonNull final Style.Merge.Strategy strategy, @NonNull final Style.Merge merge) {
      return this.merge(that, strategy, Collections.singleton(merge));
   }

   @NonNull
   public Style merge(@NonNull final Style that, @NonNull final Style.Merge... merges) {
      return this.merge(that, Style.Merge.of(merges));
   }

   @NonNull
   public Style merge(@NonNull final Style that, @NonNull final Style.Merge.Strategy strategy, @NonNull final Style.Merge... merges) {
      return this.merge(that, strategy, Style.Merge.of(merges));
   }

   @NonNull
   public Style merge(@NonNull final Style that, @NonNull final Set<Style.Merge> merges) {
      return this.merge(that, Style.Merge.Strategy.ALWAYS, merges);
   }

   @NonNull
   public Style merge(@NonNull final Style that, @NonNull final Style.Merge.Strategy strategy, @NonNull final Set<Style.Merge> merges) {
      if (that.isEmpty() || strategy == Style.Merge.Strategy.NEVER || merges.isEmpty()) {
         return this;
      } else if (this.isEmpty() && Style.Merge.hasAll(merges)) {
         return that;
      } else {
         Style.Builder builder = this.toBuilder();
         builder.merge(that, strategy, merges);
         return builder.build();
      }
   }

   @Deprecated
   @NonNull
   public Style mergeColor(@NonNull final Style that) {
      return this.merge(that, Style.Merge.COLOR);
   }

   @Deprecated
   @NonNull
   public Style mergeDecorations(@NonNull final Style that) {
      return this.merge(that, Style.Merge.DECORATIONS);
   }

   @Deprecated
   @NonNull
   public Style mergeEvents(@NonNull final Style that) {
      return this.merge(that, Style.Merge.EVENTS);
   }

   public boolean isEmpty() {
      return this == EMPTY;
   }

   @NonNull
   public Style.Builder toBuilder() {
      return new Style.Builder(this);
   }

   @NonNull
   @Override
   public String toString() {
      return ShadyPines.toString(this, map -> {
         map.put("color", this.color);
         map.put("obfuscated", this.obfuscated);
         map.put("bold", this.bold);
         map.put("strikethrough", this.strikethrough);
         map.put("underlined", this.underlined);
         map.put("italic", this.italic);
         map.put("clickEvent", this.clickEvent);
         map.put("hoverEvent", this.hoverEvent);
         map.put("insertion", this.insertion);
      });
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof Style)) {
         return false;
      } else {
         Style that = (Style)other;
         return Objects.equals(this.color, that.color)
            && this.obfuscated == that.obfuscated
            && this.bold == that.bold
            && this.strikethrough == that.strikethrough
            && this.underlined == that.underlined
            && this.italic == that.italic
            && Objects.equals(this.clickEvent, that.clickEvent)
            && Objects.equals(this.hoverEvent, that.hoverEvent)
            && Objects.equals(this.insertion, that.insertion);
      }
   }

   @Override
   public int hashCode() {
      int result = Objects.hashCode(this.color);
      result = 31 * result + this.obfuscated.hashCode();
      result = 31 * result + this.bold.hashCode();
      result = 31 * result + this.strikethrough.hashCode();
      result = 31 * result + this.underlined.hashCode();
      result = 31 * result + this.italic.hashCode();
      result = 31 * result + Objects.hashCode(this.clickEvent);
      result = 31 * result + Objects.hashCode(this.hoverEvent);
      return 31 * result + Objects.hashCode(this.insertion);
   }

   public static class Builder {
      @Nullable
      private TextColor color;
      private TextDecoration.State obfuscated = TextDecoration.State.NOT_SET;
      private TextDecoration.State bold = TextDecoration.State.NOT_SET;
      private TextDecoration.State strikethrough = TextDecoration.State.NOT_SET;
      private TextDecoration.State underlined = TextDecoration.State.NOT_SET;
      private TextDecoration.State italic = TextDecoration.State.NOT_SET;
      @Nullable
      private ClickEvent clickEvent;
      @Nullable
      private HoverEvent hoverEvent;
      @Nullable
      private String insertion;

      protected Builder() {
      }

      protected Builder(@NonNull final Component component) {
         this(component.style());
      }

      protected Builder(@NonNull final Style style) {
         this.color = style.color;
         this.obfuscated = style.obfuscated;
         this.bold = style.bold;
         this.strikethrough = style.strikethrough;
         this.underlined = style.underlined;
         this.italic = style.italic;
         this.clickEvent = style.clickEvent;
         this.hoverEvent = style.hoverEvent;
         this.insertion = style.insertion;
      }

      @NonNull
      public Style.Builder color(@Nullable final TextColor color) {
         this.color = color;
         return this;
      }

      @NonNull
      public Style.Builder colorIfAbsent(@Nullable final TextColor color) {
         if (this.color == null) {
            this.color = color;
         }

         return this;
      }

      @NonNull
      public Style.Builder decoration(@NonNull final TextDecoration decoration, final boolean flag) {
         return this.decoration(decoration, TextDecoration.State.byBoolean(flag));
      }

      @NonNull
      public Style.Builder decoration(@NonNull final TextDecoration decoration, @NonNull final TextDecoration.State state) {
         Objects.requireNonNull(state, "state");
         if (decoration == TextDecoration.BOLD) {
            this.bold = state;
            return this;
         } else if (decoration == TextDecoration.ITALIC) {
            this.italic = state;
            return this;
         } else if (decoration == TextDecoration.UNDERLINED) {
            this.underlined = state;
            return this;
         } else if (decoration == TextDecoration.STRIKETHROUGH) {
            this.strikethrough = state;
            return this;
         } else if (decoration == TextDecoration.OBFUSCATED) {
            this.obfuscated = state;
            return this;
         } else {
            throw new IllegalArgumentException(String.format("unknown decoration '%s'", decoration));
         }
      }

      @NonNull
      public Style.Builder clickEvent(@Nullable final ClickEvent event) {
         this.clickEvent = event;
         return this;
      }

      @NonNull
      public Style.Builder hoverEvent(@Nullable final HoverEvent event) {
         this.hoverEvent = event;
         return this;
      }

      @NonNull
      public Style.Builder insertion(@Nullable final String insertion) {
         this.insertion = insertion;
         return this;
      }

      @NonNull
      public Style.Builder merge(@NonNull final Style that) {
         return this.merge(that, Style.Merge.all());
      }

      @NonNull
      public Style.Builder merge(@NonNull final Style that, @NonNull final Style.Merge.Strategy strategy) {
         return this.merge(that, strategy, Style.Merge.all());
      }

      @NonNull
      public Style.Builder merge(@NonNull final Style that, @NonNull final Style.Merge... merges) {
         return merges.length == 0 ? this : this.merge(that, Style.Merge.of(merges));
      }

      @NonNull
      public Style.Builder merge(@NonNull final Style that, @NonNull final Style.Merge.Strategy strategy, @NonNull final Style.Merge... merges) {
         return merges.length == 0 ? this : this.merge(that, strategy, Style.Merge.of(merges));
      }

      @NonNull
      public Style.Builder merge(@NonNull final Style that, @NonNull final Set<Style.Merge> merges) {
         return this.merge(that, Style.Merge.Strategy.ALWAYS, merges);
      }

      @NonNull
      public Style.Builder merge(@NonNull final Style that, @NonNull final Style.Merge.Strategy strategy, @NonNull final Set<Style.Merge> merges) {
         if (!that.isEmpty() && strategy != Style.Merge.Strategy.NEVER && !merges.isEmpty()) {
            if (merges.contains(Style.Merge.COLOR)) {
               TextColor color = that.color();
               if (color != null && strategy.mergeColor(this, color)) {
                  this.color(color);
               }
            }

            if (merges.contains(Style.Merge.DECORATIONS)) {
               int i = 0;

               for (int length = Style.DECORATIONS.length; i < length; i++) {
                  TextDecoration decoration = Style.DECORATIONS[i];
                  TextDecoration.State state = that.decoration(decoration);
                  if (state != TextDecoration.State.NOT_SET && strategy.mergeDecoration(this, decoration)) {
                     this.decoration(decoration, state);
                  }
               }
            }

            if (merges.contains(Style.Merge.EVENTS)) {
               ClickEvent clickEvent = that.clickEvent();
               if (clickEvent != null && strategy.mergeClickEvent(this, clickEvent)) {
                  this.clickEvent(clickEvent);
               }

               HoverEvent hoverEvent = that.hoverEvent();
               if (hoverEvent != null && strategy.mergeHoverEvent(this, hoverEvent)) {
                  this.hoverEvent(hoverEvent);
               }
            }

            if (merges.contains(Style.Merge.INSERTION)) {
               String insertion = that.insertion();
               if (insertion != null && strategy.mergeInsertion(this, insertion)) {
                  this.insertion(insertion);
               }
            }

            return this;
         } else {
            return this;
         }
      }

      @NonNull
      public Style build() {
         return this.isEmpty()
            ? Style.EMPTY
            : new Style(
               this.color, this.obfuscated, this.bold, this.strikethrough, this.underlined, this.italic, this.clickEvent, this.hoverEvent, this.insertion
            );
      }

      private boolean isEmpty() {
         return this.color == null
            && this.obfuscated == TextDecoration.State.NOT_SET
            && this.bold == TextDecoration.State.NOT_SET
            && this.strikethrough == TextDecoration.State.NOT_SET
            && this.underlined == TextDecoration.State.NOT_SET
            && this.italic == TextDecoration.State.NOT_SET
            && this.clickEvent == null
            && this.hoverEvent == null
            && this.insertion == null;
      }
   }

   public static enum Merge {
      COLOR,
      DECORATIONS,
      EVENTS,
      INSERTION;

      static final Set<Style.Merge> ALL = of(values());
      static final Set<Style.Merge> COLOR_AND_DECORATIONS = of(COLOR, DECORATIONS);

      @NonNull
      public static Set<Style.Merge> all() {
         return ALL;
      }

      @NonNull
      public static Set<Style.Merge> colorAndDecorations() {
         return COLOR_AND_DECORATIONS;
      }

      @NonNull
      public static Set<Style.Merge> of(@NonNull final Style.Merge... merges) {
         return ShadyPines.enumSet(Style.Merge.class, merges);
      }

      static boolean hasAll(@NonNull final Set<Style.Merge> merges) {
         return merges.size() == ALL.size();
      }

      public static enum Strategy {
         ALWAYS {
            @Override
            boolean mergeColor(@NonNull final Style.Builder target, @Nullable final TextColor color) {
               return true;
            }

            @Override
            boolean mergeDecoration(@NonNull final Style.Builder target, @NonNull final TextDecoration decoration) {
               return true;
            }

            @Override
            boolean mergeClickEvent(@NonNull final Style.Builder target, @Nullable final ClickEvent event) {
               return true;
            }

            @Override
            boolean mergeHoverEvent(@NonNull final Style.Builder target, @Nullable final HoverEvent event) {
               return true;
            }

            @Override
            boolean mergeInsertion(@NonNull final Style.Builder target, @Nullable final String insertion) {
               return true;
            }
         },
         NEVER {
            @Override
            boolean mergeColor(@NonNull final Style.Builder target, @Nullable final TextColor color) {
               return false;
            }

            @Override
            boolean mergeDecoration(@NonNull final Style.Builder target, @NonNull final TextDecoration decoration) {
               return false;
            }

            @Override
            boolean mergeClickEvent(@NonNull final Style.Builder target, @Nullable final ClickEvent event) {
               return false;
            }

            @Override
            boolean mergeHoverEvent(@NonNull final Style.Builder target, @Nullable final HoverEvent event) {
               return false;
            }

            @Override
            boolean mergeInsertion(@NonNull final Style.Builder target, @Nullable final String insertion) {
               return false;
            }
         },
         IF_ABSENT_ON_TARGET {
            @Override
            boolean mergeColor(@NonNull final Style.Builder target, @Nullable final TextColor color) {
               return target.color == null;
            }

            @Override
            boolean mergeDecoration(@NonNull final Style.Builder target, @NonNull final TextDecoration decoration) {
               if (decoration == TextDecoration.OBFUSCATED) {
                  return target.obfuscated == TextDecoration.State.NOT_SET;
               } else if (decoration == TextDecoration.BOLD) {
                  return target.bold == TextDecoration.State.NOT_SET;
               } else if (decoration == TextDecoration.STRIKETHROUGH) {
                  return target.strikethrough == TextDecoration.State.NOT_SET;
               } else if (decoration == TextDecoration.UNDERLINED) {
                  return target.underlined == TextDecoration.State.NOT_SET;
               } else if (decoration == TextDecoration.ITALIC) {
                  return target.italic == TextDecoration.State.NOT_SET;
               } else {
                  throw new IllegalArgumentException();
               }
            }

            @Override
            boolean mergeClickEvent(@NonNull final Style.Builder target, @Nullable final ClickEvent event) {
               return target.clickEvent == null;
            }

            @Override
            boolean mergeHoverEvent(@NonNull final Style.Builder target, @Nullable final HoverEvent event) {
               return target.hoverEvent == null;
            }

            @Override
            boolean mergeInsertion(@NonNull final Style.Builder target, @Nullable final String insertion) {
               return target.insertion == null;
            }
         };

         private Strategy() {
         }

         abstract boolean mergeColor(@NonNull final Style.Builder target, @Nullable final TextColor color);

         abstract boolean mergeDecoration(@NonNull final Style.Builder target, @NonNull final TextDecoration decoration);

         abstract boolean mergeClickEvent(@NonNull final Style.Builder target, @Nullable final ClickEvent event);

         abstract boolean mergeHoverEvent(@NonNull final Style.Builder target, @Nullable final HoverEvent event);

         abstract boolean mergeInsertion(@NonNull final Style.Builder target, @Nullable final String insertion);
      }
   }
}
