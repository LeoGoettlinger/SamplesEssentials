package vrx.economy.vRXClans.libs.cloud.caption;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = API.Status.STABLE
)
@Immutable
public interface Caption {
   @NonNull
   static Caption of(@NonNull final String key) {
      return CaptionImpl.of(key);
   }

   @NonNull
   String key();
}
