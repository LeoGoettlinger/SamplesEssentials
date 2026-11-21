package de.oliver.cloud.annotations;

import de.oliver.cloud.component.DefaultValue;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.INTERNAL
)
final class MethodDefaultValueFactory<C, T> implements DefaultValueFactory<C, T> {
   private final MethodHandle methodHandle;

   MethodDefaultValueFactory(@NonNull final Method method, @NonNull final Object instance) {
      if (!method.isAccessible()) {
         method.setAccessible(true);
      }

      try {
         this.methodHandle = MethodHandles.lookup().unreflect(method).bindTo(instance);
      } catch (Exception var4) {
         throw new RuntimeException(
            String.format("Failed to create the default value factory using method %s in class %s", method.getName(), method.getDeclaringClass().getName()),
            var4
         );
      }
   }

   @NonNull
   @Override
   public DefaultValue<C, T> create(@NonNull final Parameter parameter) {
      try {
         return (DefaultValue)this.methodHandle.invoke((Parameter)parameter);
      } catch (Throwable var3) {
         throw new RuntimeException("Failed to create default value instance", var3);
      }
   }
}
