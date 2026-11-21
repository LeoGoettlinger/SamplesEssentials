package de.theredend2000.advancedhunt.lib.kyori.adventure.text.format;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.ClickEvent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.HoverEvent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.HoverEventSource;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.ARGBLike;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

final class StyleImpl implements Style {
   static final StyleImpl EMPTY = new StyleImpl(null, null, null, DecorationMap.EMPTY, null, null, null);
   @Nullable
   final Key font;
   @Nullable
   final TextColor color;
   @Nullable
   final ShadowColor shadowColor;
   final DecorationMap decorations;
   @Nullable
   final ClickEvent<?> clickEvent;
   @Nullable
   final HoverEvent<?> hoverEvent;
   @Nullable
   final String insertion;

   StyleImpl(
      @Nullable final Key font,
      @Nullable final TextColor color,
      @Nullable final ShadowColor shadowColor,
      final Map<TextDecoration, TextDecoration.State> decorations,
      @Nullable final ClickEvent<?> clickEvent,
      @Nullable final HoverEvent<?> hoverEvent,
      @Nullable final String insertion
   ) {
      this.font = font;
      this.color = color;
      this.shadowColor = shadowColor;
      this.decorations = DecorationMap.fromMap(decorations);
      this.clickEvent = clickEvent;
      this.hoverEvent = hoverEvent;
      this.insertion = insertion;
   }

   @Nullable
   @Override
   public Key font() {
      return this.font;
   }

   @Override
   public Style font(@Nullable final Key font) {
      return Objects.equals(this.font, font)
         ? this
         : new StyleImpl(font, this.color, this.shadowColor, this.decorations, this.clickEvent, this.hoverEvent, this.insertion);
   }

   @Nullable
   @Override
   public TextColor color() {
      return this.color;
   }

   @Override
   public Style color(@Nullable final TextColor color) {
      return Objects.equals(this.color, color)
         ? this
         : new StyleImpl(this.font, color, this.shadowColor, this.decorations, this.clickEvent, this.hoverEvent, this.insertion);
   }

   @Override
   public Style colorIfAbsent(@Nullable final TextColor color) {
      return (Style)(this.color == null ? this.color(color) : this);
   }

   @Nullable
   @Override
   public ShadowColor shadowColor() {
      return this.shadowColor;
   }

   public Style shadowColor(@Nullable final ARGBLike argb) {
      return Objects.equals(this.shadowColor, argb)
         ? this
         : new StyleImpl(
            this.font, this.color, argb == null ? null : ShadowColor.shadowColor(argb), this.decorations, this.clickEvent, this.hoverEvent, this.insertion
         );
   }

   public Style shadowColorIfAbsent(@Nullable final ARGBLike argb) {
      return (Style)(this.shadowColor == null ? this.shadowColor(argb) : this);
   }

   @Override
   public TextDecoration.State decoration(final TextDecoration decoration) {
      TextDecoration.State state = this.decorations.get(decoration);
      if (state != null) {
         return state;
      } else {
         throw new IllegalArgumentException(String.format("unknown decoration '%s'", decoration));
      }
   }

   @Override
   public Style decoration(final TextDecoration decoration, final TextDecoration.State state) {
      Objects.requireNonNull(state, "state");
      return this.decoration(decoration) == state
         ? this
         : new StyleImpl(this.font, this.color, this.shadowColor, this.decorations.with(decoration, state), this.clickEvent, this.hoverEvent, this.insertion);
   }

   @Override
   public Style decorationIfAbsent(final TextDecoration decoration, final TextDecoration.State state) {
      Objects.requireNonNull(state, "state");
      TextDecoration.State oldState = this.decorations.get(decoration);
      if (oldState == TextDecoration.State.NOT_SET) {
         return new StyleImpl(
            this.font, this.color, this.shadowColor, this.decorations.with(decoration, state), this.clickEvent, this.hoverEvent, this.insertion
         );
      } else if (oldState != null) {
         return this;
      } else {
         throw new IllegalArgumentException(String.format("unknown decoration '%s'", decoration));
      }
   }

   @Override
   public Map<TextDecoration, TextDecoration.State> decorations() {
      return this.decorations;
   }

   @Override
   public Style decorations(final Map<TextDecoration, TextDecoration.State> decorations) {
      return new StyleImpl(
         this.font, this.color, this.shadowColor, DecorationMap.merge(decorations, this.decorations), this.clickEvent, this.hoverEvent, this.insertion
      );
   }

   @Nullable
   @Override
   public ClickEvent<?> clickEvent() {
      return this.clickEvent;
   }

   @Override
   public Style clickEvent(@Nullable final ClickEvent<?> event) {
      return new StyleImpl(this.font, this.color, this.shadowColor, this.decorations, event, this.hoverEvent, this.insertion);
   }

   @Nullable
   @Override
   public HoverEvent<?> hoverEvent() {
      return this.hoverEvent;
   }

   @Override
   public Style hoverEvent(@Nullable final HoverEventSource<?> source) {
      return new StyleImpl(this.font, this.color, this.shadowColor, this.decorations, this.clickEvent, HoverEventSource.unbox(source), this.insertion);
   }

   @Nullable
   @Override
   public String insertion() {
      return this.insertion;
   }

   @Override
   public Style insertion(@Nullable final String insertion) {
      return Objects.equals(this.insertion, insertion)
         ? this
         : new StyleImpl(this.font, this.color, this.shadowColor, this.decorations, this.clickEvent, this.hoverEvent, insertion);
   }

   @Override
   public Style merge(final Style that, final Style.Merge.Strategy strategy, final Set<Style.Merge> merges) {
      if (nothingToMerge(that, strategy, merges)) {
         return this;
      } else if (this.isEmpty() && Style.Merge.hasAll(merges)) {
         return that;
      } else {
         Style.Builder builder = this.toBuilder();
         builder.merge(that, strategy, merges);
         return builder.build();
      }
   }

   @Override
   public Style unmerge(final Style that) {
      if (this.isEmpty()) {
         return this;
      } else {
         Style.Builder builder = new StyleImpl.BuilderImpl(this);
         if (Objects.equals(this.font(), that.font())) {
            builder.font(null);
         }

         if (Objects.equals(this.color(), that.color())) {
            builder.color(null);
         }

         if (Objects.equals(this.shadowColor(), that.shadowColor())) {
            builder.shadowColor(null);
         }

         int i = 0;

         for (int length = DecorationMap.DECORATIONS.length; i < length; i++) {
            TextDecoration decoration = DecorationMap.DECORATIONS[i];
            if (this.decoration(decoration) == that.decoration(decoration)) {
               builder.decoration(decoration, TextDecoration.State.NOT_SET);
            }
         }

         if (Objects.equals(this.clickEvent(), that.clickEvent())) {
            builder.clickEvent(null);
         }

         if (Objects.equals(this.hoverEvent(), that.hoverEvent())) {
            builder.hoverEvent(null);
         }

         if (Objects.equals(this.insertion(), that.insertion())) {
            builder.insertion(null);
         }

         return builder.build();
      }
   }

   static boolean nothingToMerge(final Style mergeFrom, final Style.Merge.Strategy strategy, final Set<Style.Merge> merges) {
      if (strategy == Style.Merge.Strategy.NEVER) {
         return true;
      } else {
         return mergeFrom.isEmpty() ? true : merges.isEmpty();
      }
   }

   @Override
   public boolean isEmpty() {
      return this == EMPTY;
   }

   @Override
   public Style.Builder toBuilder() {
      return new StyleImpl.BuilderImpl(this);
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else {
         return !(other instanceof StyleImpl that)
            ? false
            : Objects.equals(this.color, that.color)
               && this.decorations.equals(that.decorations)
               && Objects.equals(this.shadowColor, that.shadowColor)
               && Objects.equals(this.clickEvent, that.clickEvent)
               && Objects.equals(this.hoverEvent, that.hoverEvent)
               && Objects.equals(this.insertion, that.insertion)
               && Objects.equals(this.font, that.font);
      }
   }

   @Override
   public int hashCode() {
      int result = Objects.hashCode(this.color);
      result = 31 * result + Objects.hashCode(this.shadowColor);
      result = 31 * result + this.decorations.hashCode();
      result = 31 * result + Objects.hashCode(this.clickEvent);
      result = 31 * result + Objects.hashCode(this.hoverEvent);
      result = 31 * result + Objects.hashCode(this.insertion);
      return 31 * result + Objects.hashCode(this.font);
   }

   @Override
   public String toString() {
      return "StyleImpl{font=%s, color=%s, shadowColor=%s, decorations=%s, clickEvent=%s, hoverEvent=%s, insertion=%s}"
         .formatted(this.font, this.color, this.shadowColor, this.decorations, this.clickEvent, this.hoverEvent, this.insertion);
   }

   static final class BuilderImpl implements Style.Builder {
      @Nullable
      Key font;
      @Nullable
      TextColor color;
      @Nullable
      ShadowColor shadowColor;
      DecorationMap decorations;
      @Nullable
      ClickEvent<?> clickEvent;
      @Nullable
      HoverEvent<?> hoverEvent;
      @Nullable
      String insertion;

      BuilderImpl() {
         this.decorations = DecorationMap.EMPTY;
      }

      BuilderImpl(final StyleImpl style) {
         this.color = style.color;
         this.shadowColor = style.shadowColor;
         this.decorations = style.decorations;
         this.clickEvent = style.clickEvent;
         this.hoverEvent = style.hoverEvent;
         this.insertion = style.insertion;
         this.font = style.font;
      }

      @Override
      public Style.Builder font(@Nullable final Key font) {
         this.font = font;
         return this;
      }

      @Override
      public Style.Builder color(@Nullable final TextColor color) {
         this.color = color;
         return this;
      }

      @Override
      public Style.Builder colorIfAbsent(@Nullable final TextColor color) {
         if (this.color == null) {
            this.color = color;
         }

         return this;
      }

      public Style.Builder shadowColor(@Nullable final ARGBLike argb) {
         this.shadowColor = argb == null ? null : ShadowColor.shadowColor(argb);
         return this;
      }

      public Style.Builder shadowColorIfAbsent(@Nullable final ARGBLike argb) {
         if (this.shadowColor == null) {
            this.shadowColor = argb == null ? null : ShadowColor.shadowColor(argb);
         }

         return this;
      }

      @Override
      public Style.Builder decoration(final TextDecoration decoration, final TextDecoration.State state) {
         Objects.requireNonNull(state, "state");
         Objects.requireNonNull(decoration, "decoration");
         this.decorations = this.decorations.with(decoration, state);
         return this;
      }

      @Override
      public Style.Builder decorationIfAbsent(final TextDecoration decoration, final TextDecoration.State state) {
         Objects.requireNonNull(state, "state");
         TextDecoration.State oldState = this.decorations.get(decoration);
         if (oldState == TextDecoration.State.NOT_SET) {
            this.decorations = this.decorations.with(decoration, state);
         }

         if (oldState != null) {
            return this;
         } else {
            throw new IllegalArgumentException(String.format("unknown decoration '%s'", decoration));
         }
      }

      @Override
      public Style.Builder clickEvent(@Nullable final ClickEvent<?> event) {
         this.clickEvent = event;
         return this;
      }

      @Override
      public Style.Builder hoverEvent(@Nullable final HoverEventSource<?> source) {
         this.hoverEvent = HoverEventSource.unbox(source);
         return this;
      }

      @Override
      public Style.Builder insertion(@Nullable final String insertion) {
         this.insertion = insertion;
         return this;
      }

      @Override
      public Style.Builder merge(final Style that, final Style.Merge.Strategy strategy, final Set<Style.Merge> merges) {
         Objects.requireNonNull(that, "style");
         Objects.requireNonNull(strategy, "strategy");
         Objects.requireNonNull(merges, "merges");
         if (StyleImpl.nothingToMerge(that, strategy, merges)) {
            return this;
         } else {
            if (merges.contains(Style.Merge.COLOR)) {
               TextColor color = that.color();
               if (color != null && (strategy == Style.Merge.Strategy.ALWAYS || strategy == Style.Merge.Strategy.IF_ABSENT_ON_TARGET && this.color == null)) {
                  this.color(color);
               }
            }

            if (merges.contains(Style.Merge.SHADOW_COLOR)) {
               ShadowColor shadowColor = that.shadowColor();
               if (shadowColor != null
                  && (strategy == Style.Merge.Strategy.ALWAYS || strategy == Style.Merge.Strategy.IF_ABSENT_ON_TARGET && this.shadowColor == null)) {
                  this.shadowColor(shadowColor);
               }
            }

            if (merges.contains(Style.Merge.DECORATIONS)) {
               int i = 0;

               for (int length = DecorationMap.DECORATIONS.length; i < length; i++) {
                  TextDecoration decoration = DecorationMap.DECORATIONS[i];
                  TextDecoration.State state = that.decoration(decoration);
                  if (state != TextDecoration.State.NOT_SET) {
                     if (strategy == Style.Merge.Strategy.ALWAYS) {
                        this.decoration(decoration, state);
                     } else if (strategy == Style.Merge.Strategy.IF_ABSENT_ON_TARGET) {
                        this.decorationIfAbsent(decoration, state);
                     }
                  }
               }
            }

            if (merges.contains(Style.Merge.EVENTS)) {
               ClickEvent<?> clickEvent = that.clickEvent();
               if (clickEvent != null
                  && (strategy == Style.Merge.Strategy.ALWAYS || strategy == Style.Merge.Strategy.IF_ABSENT_ON_TARGET && this.clickEvent == null)) {
                  this.clickEvent(clickEvent);
               }

               HoverEvent<?> hoverEvent = that.hoverEvent();
               if (hoverEvent != null
                  && (strategy == Style.Merge.Strategy.ALWAYS || strategy == Style.Merge.Strategy.IF_ABSENT_ON_TARGET && this.hoverEvent == null)) {
                  this.hoverEvent(hoverEvent);
               }
            }

            if (merges.contains(Style.Merge.INSERTION)) {
               String insertion = that.insertion();
               if (insertion != null
                  && (strategy == Style.Merge.Strategy.ALWAYS || strategy == Style.Merge.Strategy.IF_ABSENT_ON_TARGET && this.insertion == null)) {
                  this.insertion(insertion);
               }
            }

            if (merges.contains(Style.Merge.FONT)) {
               Key font = that.font();
               if (font != null && (strategy == Style.Merge.Strategy.ALWAYS || strategy == Style.Merge.Strategy.IF_ABSENT_ON_TARGET && this.font == null)) {
                  this.font(font);
               }
            }

            return this;
         }
      }

      public StyleImpl build() {
         return this.isEmpty()
            ? StyleImpl.EMPTY
            : new StyleImpl(this.font, this.color, this.shadowColor, this.decorations, this.clickEvent, this.hoverEvent, this.insertion);
      }

      private boolean isEmpty() {
         return this.color == null
            && this.shadowColor == null
            && this.decorations == DecorationMap.EMPTY
            && this.clickEvent == null
            && this.hoverEvent == null
            && this.insertion == null
            && this.font == null;
      }
   }
}
