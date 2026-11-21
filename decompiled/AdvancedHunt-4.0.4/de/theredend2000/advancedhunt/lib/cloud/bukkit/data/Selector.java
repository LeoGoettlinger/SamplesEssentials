package de.theredend2000.advancedhunt.lib.cloud.bukkit.data;

import java.util.Collection;
import java.util.Collections;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE,
   since = "2.0.0"
)
public interface Selector<V> {
   @NonNull
   String inputString();

   @NonNull
   Collection<V> values();

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public interface Single<V> extends Selector<V> {
      @NonNull
      @Override
      default Collection<V> values() {
         return Collections.singletonList(this.single());
      }

      @NonNull
      V single();
   }
}
