package de.theredend2000.advancedhunt.lib.cloud.execution.preprocessor;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.INTERNAL,
   consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
)
public final class AcceptingCommandPreprocessor<C> implements CommandPreprocessor<C> {
   public static final String PROCESSED_INDICATOR_KEY = "__COMMAND_PRE_PROCESSED__";

   public void accept(@NonNull final CommandPreprocessingContext<C> context) {
      context.commandContext().store("__COMMAND_PRE_PROCESSED__", "true");
   }
}
