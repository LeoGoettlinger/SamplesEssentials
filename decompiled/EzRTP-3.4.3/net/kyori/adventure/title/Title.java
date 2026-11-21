package net.kyori.adventure.title;

import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Ticks;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public sealed interface Title permits TitleImpl {
   Title.Times DEFAULT_TIMES = Title.Times.times(Ticks.duration(10L), Ticks.duration(70L), Ticks.duration(20L));

   static Title title(final Component title, final Component subtitle) {
      return title(title, subtitle, DEFAULT_TIMES);
   }

   static Title title(final Component title, final Component subtitle, @Nullable final Title.Times times) {
      return new TitleImpl(title, subtitle, times);
   }

   static Title title(final Component title, final Component subtitle, final int fadeInTicks, final int stayTicks, final int fadeOutTicks) {
      return new TitleImpl(title, subtitle, Title.Times.times(Ticks.duration(fadeInTicks), Ticks.duration(stayTicks), Ticks.duration(fadeOutTicks)));
   }

   Component title();

   Component subtitle();

   @Nullable
   Title.Times times();

   @UnknownNullability
   <T> T part(final TitlePart<T> part);

   public sealed interface Times permits TitleImpl.TimesImpl {
      static Title.Times times(final Duration fadeIn, final Duration stay, final Duration fadeOut) {
         return new TitleImpl.TimesImpl(fadeIn, stay, fadeOut);
      }

      Duration fadeIn();

      Duration stay();

      Duration fadeOut();
   }
}
