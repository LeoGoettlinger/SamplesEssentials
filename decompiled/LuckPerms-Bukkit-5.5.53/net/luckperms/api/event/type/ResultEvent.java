package net.luckperms.api.event.type;

import java.util.concurrent.atomic.AtomicReference;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ResultEvent<T> {
   @Param(-1)
   @NonNull
   AtomicReference<T> result();

   default boolean hasResult() {
      return this.result().get() != null;
   }
}
