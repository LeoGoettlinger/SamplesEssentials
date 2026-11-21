package de.oliver.cloud.paper.suggestion.tooltips;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion;
import de.oliver.cloud.brigadier.suggestion.TooltipSuggestion;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.INTERNAL,
   since = "2.0.0"
)
public interface CompletionMapper {
   @NonNull
   Completion map(@NonNull TooltipSuggestion suggestion);
}
