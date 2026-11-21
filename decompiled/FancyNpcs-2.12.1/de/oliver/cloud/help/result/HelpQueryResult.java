package de.oliver.cloud.help.result;

import de.oliver.cloud.help.HelpQuery;
import de.oliver.cloud.help.HelpRenderer;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface HelpQueryResult<C> {
   @NonNull
   HelpQuery<C> query();

   default void render(@NonNull final HelpRenderer<C> renderer) {
      renderer.render(this);
   }
}
