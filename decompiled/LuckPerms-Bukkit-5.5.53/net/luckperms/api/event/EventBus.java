package net.luckperms.api.event;

import java.util.Set;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

public interface EventBus {
   @NonNull
   <T extends LuckPermsEvent> EventSubscription<T> subscribe(@NonNull Class<T> var1, @NonNull Consumer<? super T> var2);

   @NonNull
   <T extends LuckPermsEvent> EventSubscription<T> subscribe(Object var1, @NonNull Class<T> var2, @NonNull Consumer<? super T> var3);

   @NonNull
   @Unmodifiable
   <T extends LuckPermsEvent> Set<EventSubscription<T>> getSubscriptions(@NonNull Class<T> var1);
}
