package vrx.economy.vRXClans.libs.cloud.services.type;

import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import vrx.economy.vRXClans.libs.cloud.services.ChunkedRequestContext;

public interface PartialResultService<Context, Result, Chunked extends ChunkedRequestContext<Context, Result>> extends Service<Chunked, Map<Context, Result>> {
   @Nullable
   default Map<Context, Result> handle(@NonNull final Chunked context) {
      if (!context.isCompleted()) {
         this.handleRequests(context.remaining()).forEach(context::storeResult);
      }

      return context.isCompleted() ? context.availableResults() : null;
   }

   @NonNull
   Map<Context, Result> handleRequests(@NonNull List<Context> requests);
}
