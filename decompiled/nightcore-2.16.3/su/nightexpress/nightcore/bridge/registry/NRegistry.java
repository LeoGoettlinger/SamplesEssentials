package su.nightexpress.nightcore.bridge.registry;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface NRegistry<K, T> {
   void clear();

   int size();

   void register(@NonNull T var1);

   @Nullable
   T remove(@NonNull K var1);

   @Nullable
   T get(@NonNull K var1);

   Optional<T> lookup(@NonNull K var1);

   Set<T> values();

   Set<K> keys();

   Stream<T> stream();
}
