package org.jdbi.v3.core.interceptor;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import org.jdbi.v3.meta.Alpha;

@Alpha
public final class JdbiInterceptionChainHolder<S, T> {
   private final List<JdbiInterceptor<S, T>> interceptors;
   private final Function<S, T> defaultTransformer;

   public JdbiInterceptionChainHolder(Function<S, T> defaultTransformer) {
      this.interceptors = new CopyOnWriteArrayList<>();
      this.defaultTransformer = Objects.requireNonNull(defaultTransformer, "defaultTransformer is null");
   }

   public JdbiInterceptionChainHolder() {
      this.interceptors = new CopyOnWriteArrayList<>();
      this.defaultTransformer = JdbiInterceptionChainHolder::defaultTransformer;
   }

   public JdbiInterceptionChainHolder(JdbiInterceptionChainHolder<S, T> that) {
      this.interceptors = new CopyOnWriteArrayList<>(that.interceptors);
      this.defaultTransformer = that.defaultTransformer;
   }

   @Nonnull
   public T process(@Nullable S source) {
      JdbiInterceptionChainHolder<S, T>.ChainInstance instance = new JdbiInterceptionChainHolder.ChainInstance(source);
      T result = instance.next();
      Objects.requireNonNull(result, "chain returned null value for '" + source + "'");
      return result;
   }

   public void addFirst(JdbiInterceptor<S, T> interceptor) {
      Objects.requireNonNull(interceptor, "interceptor is null");
      this.interceptors.add(0, interceptor);
   }

   public void addLast(JdbiInterceptor<S, T> interceptor) {
      Objects.requireNonNull(interceptor, "interceptor is null");
      this.interceptors.add(interceptor);
   }

   private static <S, T> T defaultTransformer(S source) {
      if (source == null) {
         throw new UnsupportedOperationException("null value is not supported");
      } else {
         throw new UnsupportedOperationException("object type '" + source.getClass().getSimpleName() + "' is not supported");
      }
   }

   final class ChainInstance implements JdbiInterceptionChain<T> {
      private final Iterator<JdbiInterceptor<S, T>> iterator;
      private final S source;

      ChainInstance(S source) {
         this.source = source;
         this.iterator = JdbiInterceptionChainHolder.this.interceptors.iterator();
      }

      @Override
      public T next() {
         if (this.iterator.hasNext()) {
            JdbiInterceptor<S, T> interceptor = this.iterator.next();
            return interceptor.intercept(this.source, this);
         } else {
            return JdbiInterceptionChainHolder.this.defaultTransformer.apply(this.source);
         }
      }
   }
}
