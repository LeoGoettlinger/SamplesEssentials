package de.theredend2000.advancedhunt.lib.cloud.type.tuple;

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
