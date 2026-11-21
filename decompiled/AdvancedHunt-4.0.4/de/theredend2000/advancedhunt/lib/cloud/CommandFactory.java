package de.theredend2000.advancedhunt.lib.cloud;

import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
@FunctionalInterface
public interface CommandFactory<C> {
   @NonNull
   List<Command<? extends C>> createCommands(@NonNull CommandManager<C> commandManager);
}
