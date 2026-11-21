package vrx.economy.vRXClans.libs.cloud.type.tuple;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public interface Tuple {
   int size();

   @NonNull
   Object[] toArray();
}
