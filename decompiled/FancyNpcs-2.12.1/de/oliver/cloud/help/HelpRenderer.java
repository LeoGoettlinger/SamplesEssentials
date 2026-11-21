package de.oliver.cloud.help;

import de.oliver.cloud.help.result.HelpQueryResult;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface HelpRenderer<C> {
   void render(@NonNull HelpQueryResult<C> result);
}
