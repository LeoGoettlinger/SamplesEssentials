package org.jdbi.v3.core.interceptor;

import com.google.errorprone.annotations.CheckReturnValue;
import jakarta.annotation.Nullable;
import org.jdbi.v3.meta.Alpha;

@FunctionalInterface
@Alpha
public interface JdbiInterceptor<S, T> {
   @Nullable
   @CheckReturnValue
   T intercept(@Nullable S source, JdbiInterceptionChain<T> chain);
}
