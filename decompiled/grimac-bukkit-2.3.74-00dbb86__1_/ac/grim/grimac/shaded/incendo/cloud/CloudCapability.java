package ac.grim.grimac.shaded.incendo.cloud;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface CloudCapability {
   @NonNull
   @Override
   String toString();

   @API(
      status = Status.STABLE
   )
   public static final class CloudCapabilityMissingException extends RuntimeException {
      public CloudCapabilityMissingException(@NonNull final CloudCapability capability) {
         super(String.format("Missing capability '%s'", capability));
      }
   }

   @API(
      status = Status.STABLE
   )
   public static enum StandardCapabilities implements CloudCapability {
      ROOT_COMMAND_DELETION;

      @NonNull
      @Override
      public String toString() {
         return this.name();
      }
   }
}
