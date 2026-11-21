package ac.grim.grimac.internal.storage.submit;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.event.ViolationEvent;
import ac.grim.grimac.api.storage.submit.SubmitResult;
import ac.grim.grimac.api.storage.submit.ViolationSink;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

@ApiStatus.Internal
public final class ViolationSinkImpl implements ViolationSink {
   private final DataStore store;
   private volatile boolean closed;

   public ViolationSinkImpl(DataStore store) {
      this.store = store;
   }

   @NotNull
   @Override
   public SubmitResult record(@NotNull Consumer<ViolationEvent> configurer) {
      if (this.closed) {
         return SubmitResult.DROPPED_SHUTTING_DOWN;
      } else {
         this.store.submit(Categories.VIOLATION, configurer);
         return SubmitResult.QUEUED;
      }
   }

   public void shutDown() {
      this.closed = true;
   }
}
