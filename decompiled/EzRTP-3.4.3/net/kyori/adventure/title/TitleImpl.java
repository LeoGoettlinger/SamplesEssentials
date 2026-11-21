package net.kyori.adventure.title;

import java.time.Duration;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

record TitleImpl(Component title, Component subtitle, @Nullable Title.Times times) implements Title {
   TitleImpl(final Component title, final Component subtitle, @Nullable final Title.Times times) {
      this.title = Objects.requireNonNull(title, "title");
      this.subtitle = Objects.requireNonNull(subtitle, "subtitle");
      this.times = times;
   }

   @UnknownNullability
   @Override
   public <T> T part(final TitlePart<T> part) {
      Objects.requireNonNull(part, "part");
      if (part == TitlePart.TITLE) {
         return (T)this.title;
      } else if (part == TitlePart.SUBTITLE) {
         return (T)this.subtitle;
      } else if (part == TitlePart.TIMES) {
         return (T)this.times;
      } else {
         throw new IllegalArgumentException("Don't know what " + part + " is.");
      }
   }

   record TimesImpl(Duration fadeIn, Duration stay, Duration fadeOut) implements Title.Times {
      TimesImpl(final Duration fadeIn, final Duration stay, final Duration fadeOut) {
         this.fadeIn = Objects.requireNonNull(fadeIn, "fadeIn");
         this.stay = Objects.requireNonNull(stay, "stay");
         this.fadeOut = Objects.requireNonNull(fadeOut, "fadeOut");
      }
   }
}
