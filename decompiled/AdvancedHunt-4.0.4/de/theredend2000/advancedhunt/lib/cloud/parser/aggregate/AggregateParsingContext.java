package de.theredend2000.advancedhunt.lib.cloud.parser.aggregate;

import de.theredend2000.advancedhunt.lib.cloud.key.MutableCloudKeyContainer;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public interface AggregateParsingContext<C> extends MutableCloudKeyContainer {
   @NonNull
   static <C> AggregateParsingContext<C> argumentContext(@NonNull final AggregateParser<C, ?> parser) {
      return new AggregateParsingContextImpl<>(parser);
   }
}
