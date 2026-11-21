package vrx.economy.vRXClans.libs.cloud.parser.aggregate;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.key.MutableCloudKeyContainer;

@API(
   status = API.Status.STABLE
)
public interface AggregateParsingContext<C> extends MutableCloudKeyContainer {
   @NonNull
   static <C> AggregateParsingContext<C> argumentContext(@NonNull final AggregateParser<C, ?> parser) {
      return new AggregateParsingContextImpl<>(parser);
   }
}
