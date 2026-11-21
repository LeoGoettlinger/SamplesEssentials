package vrx.economy.vRXClans.libs.cloud.key;

import io.leangen.geantyref.TypeToken;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = API.Status.STABLE
)
@Immutable
public abstract class CloudKey<T> {
   @API(
      status = API.Status.STABLE
   )
   public static <T> CloudKey<T> of(@NonNull final String name, @NonNull final TypeToken<T> type) {
      return CloudKeyImpl.of(name, type);
   }

   @API(
      status = API.Status.STABLE
   )
   public static <T> CloudKey<T> of(@NonNull final String name, @NonNull final Class<T> type) {
      return CloudKeyImpl.of(name, TypeToken.get(type));
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static CloudKey<Void> of(@NonNull final String name) {
      return CloudKeyImpl.of(name, TypeToken.get(void.class));
   }

   @API(
      status = API.Status.STABLE
   )
   public static <T> CloudKey<T> cloudKey(@NonNull final String name, @NonNull final TypeToken<T> type) {
      return CloudKeyImpl.of(name, type);
   }

   @API(
      status = API.Status.STABLE
   )
   public static <T> CloudKey<T> cloudKey(@NonNull final String name, @NonNull final Class<T> type) {
      return CloudKeyImpl.of(name, TypeToken.get(type));
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static CloudKey<Void> cloudKey(@NonNull final String name) {
      return CloudKeyImpl.of(name, TypeToken.get(void.class));
   }

   @NonNull
   public abstract String name();

   @NonNull
   public abstract TypeToken<T> type();

   @Override
   public final boolean equals(final Object other) {
      if (this == other) {
         return true;
      } else if (other != null && this.getClass() == other.getClass()) {
         CloudKey<?> that = (CloudKey<?>)other;
         return Objects.equals(this.name(), that.name());
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hashCode(this.name());
   }
}
