package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.suggestion;

import de.theredend2000.advancedhunt.lib.cloud.suggestion.Suggestion;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value.Immutable;

@Immutable
public interface ComponentTooltipSuggestion extends Suggestion {
   @NonNull
   @Override
   String suggestion();

   @Nullable
   Component tooltip();

   @NonNull
   ComponentTooltipSuggestion withSuggestion(@NonNull String suggestion);

   @NonNull
   static ComponentTooltipSuggestion suggestion(@NonNull final String suggestion) {
      return ComponentTooltipSuggestionImpl.of(suggestion, null);
   }

   @NonNull
   static ComponentTooltipSuggestion suggestion(@NonNull final String suggestion, @Nullable final Component tooltip) {
      return ComponentTooltipSuggestionImpl.of(suggestion, tooltip);
   }
}
