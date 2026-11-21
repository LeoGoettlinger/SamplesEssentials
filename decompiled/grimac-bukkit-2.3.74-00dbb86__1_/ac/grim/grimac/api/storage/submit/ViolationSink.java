package ac.grim.grimac.api.storage.submit;

import ac.grim.grimac.api.storage.event.ViolationEvent;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

@ApiStatus.Experimental
public interface ViolationSink {
   @NotNull
   SubmitResult record(@NotNull Consumer<ViolationEvent> var1);
}
