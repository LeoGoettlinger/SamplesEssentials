package ac.grim.grimac.internal.plugin.resolver;

import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@ApiStatus.Internal
public final class GrimExtensionManager {
   private final List<GrimExtensionResolver> resolvers = new CopyOnWriteArrayList<>();
   private ResolutionFailureHandler failureHandler = failedContext -> new IllegalArgumentException(
      "Unable to resolve plugin context for type: "
         + failedContext.getClass().getName()
         + ". Ensure you are passing a valid platform plugin instance or a pre-existing GrimPlugin."
   );

   public void setFailureHandler(@NotNull ResolutionFailureHandler failureHandler) {
      this.failureHandler = Objects.requireNonNull(failureHandler, "failureHandler cannot be null");
   }

   public void registerResolver(@NotNull GrimExtensionResolver resolver) {
      Objects.requireNonNull(resolver, "resolver cannot be null");
      this.resolvers.add(resolver);
   }

   @NotNull
   public GrimPlugin getPlugin(@NotNull Object context) {
      Objects.requireNonNull(context, "context cannot be null");
      if (context instanceof GrimPlugin) {
         return (GrimPlugin)context;
      } else {
         for (GrimExtensionResolver resolver : this.resolvers) {
            GrimPlugin resolved = resolver.resolve(context);
            if (resolved != null) {
               return resolved;
            }
         }

         throw this.failureHandler.createExceptionFor(context);
      }
   }
}
