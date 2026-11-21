package xyz.lychee.lagfixer.libs.kyori.adventure.title;

import java.time.Duration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.util.Ticks;
import xyz.lychee.lagfixer.libs.kyori.examination.Examinable;

@ApiStatus.NonExtendable
public interface Title extends Examinable {
   Title.Times DEFAULT_TIMES = Title.Times.times(Ticks.duration(10L), Ticks.duration(70L), Ticks.duration(20L));

   @NotNull
   static Title title(@NotNull final Component title, @NotNull final Component subtitle) {
      return title(title, subtitle, DEFAULT_TIMES);
   }

   @NotNull
   static Title title(@NotNull final Component title, @NotNull final Component subtitle, @Nullable final Title.Times times) {
      return new TitleImpl(title, subtitle, times);
   }

   @NotNull
   static Title title(@NotNull final Component title, @NotNull final Component subtitle, final int fadeInTicks, final int stayTicks, final int fadeOutTicks) {
      return new TitleImpl(title, subtitle, Title.Times.times(Ticks.duration(fadeInTicks), Ticks.duration(stayTicks), Ticks.duration(fadeOutTicks)));
   }

   @NotNull
   Component title();

   @NotNull
   Component subtitle();

   @Nullable
   Title.Times times();

   @UnknownNullability
   <T> T part(@NotNull final TitlePart<T> part);

   @ApiStatus.NonExtendable
   public interface Times extends Examinable {
      @Deprecated
      @ApiStatus.ScheduledForRemoval(
         inVersion = "5.0.0"
      )
      @NotNull
      static Title.Times of(@NotNull final Duration fadeIn, @NotNull final Duration stay, @NotNull final Duration fadeOut) {
         return times(fadeIn, stay, fadeOut);
      }

      @NotNull
      static Title.Times times(@NotNull final Duration fadeIn, @NotNull final Duration stay, @NotNull final Duration fadeOut) {
         return new TitleImpl.TimesImpl(fadeIn, stay, fadeOut);
      }

      @NotNull
      Duration fadeIn();

      @NotNull
      Duration stay();

      @NotNull
      Duration fadeOut();
   }
}
