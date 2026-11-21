package net.luckperms.api.event;

import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface EventSubscription<T extends LuckPermsEvent> extends AutoCloseable {
   @NonNull
   Class<T> getEventClass();

   boolean isActive();

   @Override
   void close();

   @NonNull
   Consumer<? super T> getHandler();
}
