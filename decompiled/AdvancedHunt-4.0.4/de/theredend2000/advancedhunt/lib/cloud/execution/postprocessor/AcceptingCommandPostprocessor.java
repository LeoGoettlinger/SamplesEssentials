package de.theredend2000.advancedhunt.lib.cloud.execution.postprocessor;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.INTERNAL,
   consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
)
public final class AcceptingCommandPostprocessor<C> implements CommandPostprocessor<C> {
   public static final String PROCESSED_INDICATOR_KEY = "__COMMAND_POST_PROCESSED__";

   public void accept(@NonNull final CommandPostprocessingContext<C> context) {
      context.commandContext().store("__COMMAND_POST_PROCESSED__", "true");
   }
}
