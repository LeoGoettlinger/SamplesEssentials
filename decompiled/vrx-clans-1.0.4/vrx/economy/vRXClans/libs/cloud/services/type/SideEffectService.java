package vrx.economy.vRXClans.libs.cloud.services.type;

import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.services.State;

@FunctionalInterface
public interface SideEffectService<Context> extends Service<Context, State> {
   @NonNull
   State handle(@NonNull Context context) throws Exception;
}
