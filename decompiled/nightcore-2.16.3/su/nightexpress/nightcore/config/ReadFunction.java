package su.nightexpress.nightcore.config;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface ReadFunction<V> {
   @Nullable
   V read(@NonNull FileConfig var1, @NonNull String var2, @NonNull String var3);
}
