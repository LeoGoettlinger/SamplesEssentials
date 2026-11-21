package vrx.economy.vRXClans.libs.cloud.meta;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.key.CloudKeyContainer;

@API(
   status = API.Status.STABLE
)
public abstract class CommandMeta implements CloudKeyContainer {
   @NonNull
   public static CommandMetaBuilder builder() {
      return new CommandMetaBuilder();
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static CommandMeta empty() {
      return builder().build();
   }

   @NonNull
   @Override
   public final String toString() {
      return "";
   }
}
