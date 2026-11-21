package de.theredend2000.advancedhunt.lib.cloud.meta;

import de.theredend2000.advancedhunt.lib.cloud.key.CloudKeyContainer;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

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
