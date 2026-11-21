package de.oliver.cloud.annotations;

import de.oliver.cloud.description.Description;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface DescriptionMapper {
   @NonNull
   static DescriptionMapper simple() {
      return Description::of;
   }

   @NonNull
   Description map(@NonNull String string);
}
