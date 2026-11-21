package de.theredend2000.advancedhunt.lib.cloud.help.result;

import de.theredend2000.advancedhunt.lib.cloud.help.HelpQuery;
import de.theredend2000.advancedhunt.lib.cloud.help.HelpRenderer;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public interface HelpQueryResult<C> {
   @NonNull
   HelpQuery<C> query();

   default void render(@NonNull final HelpRenderer<C> renderer) {
      renderer.render(this);
   }
}
