package de.theredend2000.advancedhunt.lib.cloud.paper.suggestion.tooltips;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.suggestion.TooltipSuggestion;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.INTERNAL,
   since = "2.0.0"
)
public interface CompletionMapper {
   @NonNull
   Completion map(@NonNull TooltipSuggestion suggestion);
}
