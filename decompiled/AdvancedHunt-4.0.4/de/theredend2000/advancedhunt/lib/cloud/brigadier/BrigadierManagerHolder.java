package de.theredend2000.advancedhunt.lib.cloud.brigadier;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE,
   since = "2.0.0"
)
public interface BrigadierManagerHolder<C, S> {
   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   boolean hasBrigadierManager();

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   CloudBrigadierManager<C, ? extends S> brigadierManager();

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public static final class BrigadierManagerNotPresent extends RuntimeException {
      public BrigadierManagerNotPresent(final String message) {
         super(message);
      }
   }
}
