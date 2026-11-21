package vrx.economy.vRXClans.libs.cloud.paper.suggestion.tooltips;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.brigadier.suggestion.TooltipSuggestion;

@API(
   status = API.Status.INTERNAL,
   since = "2.0.0"
)
public interface CompletionMapper {
   @NonNull
   Completion map(@NonNull TooltipSuggestion suggestion);
}
