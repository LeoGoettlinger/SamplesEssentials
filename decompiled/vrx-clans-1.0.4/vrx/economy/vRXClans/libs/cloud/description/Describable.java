package vrx.economy.vRXClans.libs.cloud.description;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public interface Describable {
   @NonNull
   Description description();
}
