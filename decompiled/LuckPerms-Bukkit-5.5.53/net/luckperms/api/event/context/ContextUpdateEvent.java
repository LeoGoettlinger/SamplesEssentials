package net.luckperms.api.event.context;

import java.util.Optional;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ContextUpdateEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   Object getSubject();

   @NonNull
   default <T> Optional<T> getSubject(@NonNull Class<T> subjectClass) {
      Object subject = this.getSubject();
      return subjectClass.isInstance(subject) ? Optional.of(subjectClass.cast(subject)) : Optional.empty();
   }
}
