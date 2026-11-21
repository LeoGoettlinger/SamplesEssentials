package vrx.economy.vRXClans.libs.cloud.key;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(
   status = API.Status.STABLE
)
public interface CloudKeyHolder<T> {
   @NonNull
   CloudKey<T> key();
}
