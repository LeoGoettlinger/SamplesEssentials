package org.jdbi.v3.core.extension;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.internal.exceptions.Sneaky;
import org.jdbi.v3.meta.Alpha;

@FunctionalInterface
@Alpha
public interface ExtensionHandler {
   AttachedExtensionHandler attachTo(ConfigRegistry config, Object target);

   static ExtensionHandler missingExtensionHandler(Method method) {
      return (ExtensionHandler.Simple)(handleSupplier, args) -> {
         throw new IllegalStateException(
            String.format("Method %s.%s has no registered extension handler!", method.getDeclaringClass().getSimpleName(), method.getName())
         );
      };
   }

   static ExtensionHandler createForMethod(Method method) throws IllegalAccessException {
      Class<?> declaringClass = method.getDeclaringClass();
      MethodHandle methodHandle = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup()).unreflect(method);
      return createForMethodHandle(methodHandle);
   }

   static ExtensionHandler createForSpecialMethod(Method method) throws IllegalAccessException {
      Class<?> declaringClass = method.getDeclaringClass();
      MethodHandle methodHandle = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup()).unreflectSpecial(method, declaringClass);
      return createForMethodHandle(methodHandle);
   }

   static ExtensionHandler createForMethodHandle(MethodHandle methodHandle) {
      return new ExtensionHandler() {
         @Override
         public AttachedExtensionHandler attachTo(ConfigRegistry config, Object target) {
            if (target == null) {
               throw new IllegalStateException("no target object present, called from a proxy factory?");
            } else {
               return new AttachedExtensionHandler() {
                  final MethodHandle boundMh = methodHandle.bindTo(target);

                  @Override
                  public Object invoke(HandleSupplier handleSupplier, Object... args) throws Exception {
                     try {
                        return this.boundMh.invokeWithArguments(args);
                     } catch (Throwable var4) {
                        throw Sneaky.throwAnyway(var4);
                     }
                  }
               };
            }
         }
      };
   }

   @FunctionalInterface
   public interface Simple extends ExtensionHandler, AttachedExtensionHandler {
      @Override
      default AttachedExtensionHandler attachTo(ConfigRegistry config, Object target) {
         return this;
      }
   }
}
