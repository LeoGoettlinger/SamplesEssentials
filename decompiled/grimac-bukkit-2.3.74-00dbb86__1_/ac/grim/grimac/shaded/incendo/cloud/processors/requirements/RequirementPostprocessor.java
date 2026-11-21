package ac.grim.grimac.shaded.incendo.cloud.processors.requirements;

import ac.grim.grimac.shaded.incendo.cloud.execution.postprocessor.CommandPostprocessingContext;
import ac.grim.grimac.shaded.incendo.cloud.execution.postprocessor.CommandPostprocessor;
import ac.grim.grimac.shaded.incendo.cloud.key.CloudKey;
import ac.grim.grimac.shaded.incendo.cloud.services.type.ConsumerService;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE,
   since = "1.0.0"
)
public final class RequirementPostprocessor<C, R extends Requirement<C, R>> implements CommandPostprocessor<C> {
   private final CloudKey<Requirements<C, R>> requirementKey;
   private final RequirementFailureHandler<C, R> failureHandler;

   public static <C, R extends Requirement<C, R>> RequirementPostprocessor<C, R> of(
      @NonNull final CloudKey<Requirements<C, R>> requirementKey, @NonNull final RequirementFailureHandler<C, R> failureHandler
   ) {
      return new RequirementPostprocessor<>(requirementKey, failureHandler);
   }

   private RequirementPostprocessor(@NonNull final CloudKey<Requirements<C, R>> requirementKey, @NonNull final RequirementFailureHandler<C, R> failureHandler) {
      this.requirementKey = Objects.requireNonNull(requirementKey, "requirementKey");
      this.failureHandler = Objects.requireNonNull(failureHandler, "failureHandler");
   }

   public void accept(@NonNull final CommandPostprocessingContext<C> context) {
      Requirements<C, R> requirements = context.command().commandMeta().getOrDefault(this.requirementKey, null);
      if (requirements != null) {
         for (R requirement : requirements) {
            if (!requirement.evaluateRequirement(context.commandContext())) {
               this.failureHandler.handleFailure(context.commandContext(), requirement);
               ConsumerService.interrupt();
            }
         }
      }
   }
}
