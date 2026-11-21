package vrx.economy.vRXClans.libs.cloud.caption;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = API.Status.STABLE
)
@Immutable
public interface CaptionVariable {
   @NonNull
   static CaptionVariable of(@NonNull final String key, @NonNull final String value) {
      return CaptionVariableImpl.of(key, value);
   }

   @NonNull
   String key();

   @NonNull
   String value();
}
