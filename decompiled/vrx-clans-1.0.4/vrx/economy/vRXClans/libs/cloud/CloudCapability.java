package vrx.economy.vRXClans.libs.cloud;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public interface CloudCapability {
   @NonNull
   @Override
   String toString();

   @API(
      status = API.Status.STABLE
   )
   public static final class CloudCapabilityMissingException extends RuntimeException {
      public CloudCapabilityMissingException(@NonNull final CloudCapability capability) {
         super(String.format("Missing capability '%s'", capability));
      }
   }

   @API(
      status = API.Status.STABLE
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
