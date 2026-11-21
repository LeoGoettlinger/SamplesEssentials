package ac.grim.grimac.shaded.incendo.cloud.services;

import ac.grim.grimac.shaded.incendo.cloud.services.annotation.Order;
import ac.grim.grimac.shaded.incendo.cloud.services.type.Service;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

class AnnotatedMethodService<Context, Result> implements Service<Context, Result> {
   private final ExecutionOrder executionOrder;
   private final MethodHandle methodHandle;
   private final Method method;
   private final Object instance;

   AnnotatedMethodService(@NonNull final Object instance, @NonNull final Method method) throws Exception {
      ExecutionOrder executionOrder = ExecutionOrder.SOON;

      try {
         Order order = method.getAnnotation(Order.class);
         if (order != null) {
            executionOrder = order.value();
         }
      } catch (Exception var5) {
      }

      this.instance = instance;
      this.executionOrder = executionOrder;
      method.setAccessible(true);
      this.methodHandle = MethodHandles.lookup().unreflect(method);
      this.method = method;
   }

   @Nullable
   @Override
   public Result handle(@NonNull final Context context) {
      try {
         return (Result)(Object)this.methodHandle.invoke((Object)this.instance, (Object)context);
      } catch (Throwable var3) {
         new IllegalStateException(
               String.format(
                  "Failed to call method service implementation '%s' in class '%s'", this.method.getName(), this.instance.getClass().getCanonicalName()
               ),
               var3
            )
            .printStackTrace();
         return null;
      }
   }

   @NonNull
   @Override
   public ExecutionOrder order() {
      return this.executionOrder;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AnnotatedMethodService<?, ?> that = (AnnotatedMethodService<?, ?>)o;
         return Objects.equals(this.methodHandle, that.methodHandle);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.methodHandle);
   }
}
