package de.theredend2000.advancedhunt.lib.cloud.help;

import de.theredend2000.advancedhunt.lib.cloud.help.result.HelpQueryResult;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public interface HelpRenderer<C> {
   void render(@NonNull HelpQueryResult<C> result);
}
