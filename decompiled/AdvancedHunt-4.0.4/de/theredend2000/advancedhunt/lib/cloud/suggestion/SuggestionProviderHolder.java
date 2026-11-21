package de.theredend2000.advancedhunt.lib.cloud.suggestion;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public interface SuggestionProviderHolder<C> {
   @NonNull
   SuggestionProvider<C> suggestionProvider();
}
