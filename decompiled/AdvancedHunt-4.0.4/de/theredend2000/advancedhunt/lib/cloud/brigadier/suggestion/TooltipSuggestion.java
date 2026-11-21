package de.theredend2000.advancedhunt.lib.cloud.brigadier.suggestion;

import com.mojang.brigadier.Message;
import de.theredend2000.advancedhunt.lib.cloud.suggestion.Suggestion;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value.Immutable;

@API(
   status = API.Status.STABLE,
   since = "2.0.0"
)
@Immutable
public interface TooltipSuggestion extends Suggestion {
   @NonNull
   static TooltipSuggestion suggestion(@NonNull final String suggestion, @Nullable final Message tooltip) {
      return TooltipSuggestionImpl.of(suggestion, tooltip);
   }

   @NonNull
   static TooltipSuggestion tooltipSuggestion(@NonNull final Suggestion suggestion) {
      return suggestion instanceof TooltipSuggestion ? (TooltipSuggestion)suggestion : suggestion(suggestion.suggestion(), null);
   }

   @NonNull
   @Override
   String suggestion();

   @Nullable
   Message tooltip();

   @NonNull
   TooltipSuggestion withSuggestion(@NonNull String suggestion);
}
