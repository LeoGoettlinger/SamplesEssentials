package de.theredend2000.advancedhunt.lib.cloud.help.result;

import de.theredend2000.advancedhunt.lib.cloud.help.HelpQuery;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = API.Status.STABLE
)
@Immutable
public interface MultipleCommandResult<C> extends HelpQueryResult<C> {
   @NonNull
   static <C> MultipleCommandResult<C> of(@NonNull final HelpQuery<C> query, @NonNull final String longestPath, @NonNull final List<String> childSuggestions) {
      return MultipleCommandResultImpl.of(query, longestPath, childSuggestions);
   }

   @NonNull
   @Override
   HelpQuery<C> query();

   @NonNull
   String longestPath();

   @NonNull
   List<String> childSuggestions();
}
