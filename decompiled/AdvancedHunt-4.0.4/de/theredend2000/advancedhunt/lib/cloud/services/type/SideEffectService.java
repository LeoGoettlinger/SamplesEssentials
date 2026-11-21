package de.theredend2000.advancedhunt.lib.cloud.services.type;

import de.theredend2000.advancedhunt.lib.cloud.services.State;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface SideEffectService<Context> extends Service<Context, State> {
   @NonNull
   State handle(@NonNull Context context) throws Exception;
}
