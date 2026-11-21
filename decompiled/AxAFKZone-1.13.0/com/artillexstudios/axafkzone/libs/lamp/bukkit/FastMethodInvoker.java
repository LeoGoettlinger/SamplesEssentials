package com.artillexstudios.axafkzone.libs.lamp.bukkit;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class FastMethodInvoker {
   public static FastMethodInvoker create(String clazz, String method, Class<?>... parameters) {
      try {
         return createSilently(clazz, method, parameters);
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   public static FastMethodInvoker createSilently(String clazz, String method, Class<?>... parameters) throws Exception {
      return create(Class.forName(clazz).getDeclaredMethod(method, parameters));
   }

   public static FastMethodInvoker create(Method method) {
      FastMethodInvoker invoker = null;
      method.setAccessible(true);
      if (method.getParameters().length == 0) {
         Class<?> returnType = method.getReturnType();
         if (returnType == void.class) {
            if (Modifier.isStatic(method.getModifiers())) {
               invoker = new FastMethodInvoker.FastStaticRunnableInvoker(method);
            } else {
               invoker = new FastMethodInvoker.FastInstanceRunnableInvoker(method);
            }
         } else if (Modifier.isStatic(method.getModifiers())) {
            invoker = new FastMethodInvoker.FastStaticSupplierInvoker(method);
         } else {
            invoker = new FastMethodInvoker.FastInstanceSupplierInvoker(method);
         }
      } else if (method.getParameters().length == 1) {
         if (method.getReturnType() != void.class) {
            if (Modifier.isStatic(method.getModifiers())) {
               invoker = new FastMethodInvoker.FastStaticFunctionInvoker(method);
            } else {
               invoker = new FastMethodInvoker.FastInstanceFunctionInvoker(method);
            }
         } else if (Modifier.isStatic(method.getModifiers())) {
            invoker = new FastMethodInvoker.FastStaticBiSupplierInvoker(method);
         } else {
            invoker = new FastMethodInvoker.FastInstanceBiSupplierInvoker(method);
         }
      } else if (method.getParameters().length == 2 && method.getReturnType() != void.class) {
         if (Modifier.isStatic(method.getModifiers())) {
            invoker = new FastMethodInvoker.FastStaticBiFunctionInvoker(method);
         } else {
            invoker = new FastMethodInvoker.FastInstanceBiFunctionInvoker(method);
         }
      }

      if (invoker == null) {
         invoker = new FastMethodInvoker.MethodHandleFallbackInvoker(method);
      }

      return invoker;
   }

   public abstract <T> T invoke(Object instance, Object... a);

   private static final class FastInstanceBiFunctionInvoker extends FastMethodInvoker {
      FastMethodInvoker.TriFunction<Object, Object, Object, Object> supplier;

      private FastInstanceBiFunctionInvoker(Method method) {
         try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method);
            CallSite callSite = LambdaMetafactory.metafactory(
               MethodHandles.lookup(),
               "apply",
               MethodType.methodType(FastMethodInvoker.TriFunction.class, MethodHandle.class),
               MethodType.methodType(Object.class, Object.class, Object.class, Object.class),
               MethodHandles.exactInvoker(handle.type()),
               handle.type()
            );
            this.supplier = (FastMethodInvoker.TriFunction)callSite.getTarget().invokeExact((MethodHandle)handle);
         } catch (Throwable var4) {
            throw new RuntimeException(var4);
         }
      }

      @Override
      public <T> T invoke(Object instance, Object... a) {
         return (T)this.supplier.apply(instance, a[0], a[1]);
      }
   }

   private static final class FastInstanceBiSupplierInvoker extends FastMethodInvoker {
      BiConsumer<Object, Object> supplier;

      private FastInstanceBiSupplierInvoker(Method method) {
         try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method);
            CallSite callSite = LambdaMetafactory.metafactory(
               MethodHandles.lookup(),
               "accept",
               MethodType.methodType(BiConsumer.class, MethodHandle.class),
               MethodType.methodType(void.class, Object.class, Object.class),
               MethodHandles.exactInvoker(handle.type()),
               handle.type()
            );
            this.supplier = (BiConsumer)callSite.getTarget().invokeExact((MethodHandle)handle);
         } catch (Throwable var4) {
            throw new RuntimeException(var4);
         }
      }

      @Override
      public <T> T invoke(Object instance, Object... a) {
         this.supplier.accept(instance, a[0]);
         return null;
      }
   }

   private static final class FastInstanceFunctionInvoker extends FastMethodInvoker {
      BiFunction<Object, Object, Object> supplier;

      private FastInstanceFunctionInvoker(Method method) {
         try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method);
            CallSite callSite = LambdaMetafactory.metafactory(
               MethodHandles.lookup(),
               "apply",
               MethodType.methodType(BiFunction.class, MethodHandle.class),
               MethodType.methodType(Object.class, Object.class, Object.class),
               MethodHandles.exactInvoker(handle.type()),
               handle.type()
            );
            this.supplier = (BiFunction)callSite.getTarget().invokeExact((MethodHandle)handle);
         } catch (Throwable var4) {
            throw new RuntimeException(var4);
         }
      }

      @Override
      public <T> T invoke(Object instance, Object... a) {
         return (T)this.supplier.apply(instance, a[0]);
      }
   }

   private static final class FastInstanceRunnableInvoker extends FastMethodInvoker {
      Consumer<Object> consumer;

      private FastInstanceRunnableInvoker(Method method) {
         try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method);
            CallSite callSite = LambdaMetafactory.metafactory(
               MethodHandles.lookup(),
               "accept",
               MethodType.methodType(Consumer.class, MethodHandle.class),
               MethodType.methodType(void.class, Object.class),
               MethodHandles.exactInvoker(handle.type()),
               handle.type()
            );
            this.consumer = (Consumer)callSite.getTarget().invokeExact((MethodHandle)handle);
         } catch (Throwable var4) {
            throw new RuntimeException(var4);
         }
      }

      @Override
      public <T> T invoke(Object instance, Object... a) {
         this.consumer.accept(instance);
         return null;
      }
   }

   private static final class FastInstanceSupplierInvoker extends FastMethodInvoker {
      Function<Object, Object> supplier;

      private FastInstanceSupplierInvoker(Method method) {
         try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method);
            CallSite callSite = LambdaMetafactory.metafactory(
               MethodHandles.lookup(),
               "apply",
               MethodType.methodType(Function.class, MethodHandle.class),
               MethodType.methodType(Object.class, Object.class),
               MethodHandles.exactInvoker(handle.type()),
               handle.type()
            );
            this.supplier = (Function)callSite.getTarget().invokeExact((MethodHandle)handle);
         } catch (Throwable var4) {
            throw new RuntimeException(var4);
         }
      }

      @Override
      public <T> T invoke(Object instance, Object... a) {
         return (T)this.supplier.apply(instance);
      }
   }

   private static final class FastStaticBiFunctionInvoker extends FastMethodInvoker {
      BiFunction<Object, Object, Object> supplier;

      private FastStaticBiFunctionInvoker(Method method) {
         try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method);
            CallSite callSite = LambdaMetafactory.metafactory(
               MethodHandles.lookup(),
               "apply",
               MethodType.methodType(BiFunction.class, MethodHandle.class),
               MethodType.methodType(Object.class, Object.class, Object.class),
               MethodHandles.exactInvoker(handle.type()),
               handle.type()
            );
            this.supplier = (BiFunction)callSite.getTarget().invokeExact((MethodHandle)handle);
         } catch (Throwable var4) {
            throw new RuntimeException(var4);
         }
      }

      @Override
      public <T> T invoke(Object instance, Object... a) {
         return (T)this.supplier.apply(a[0], a[1]);
      }
   }

   private static final class FastStaticBiSupplierInvoker extends FastMethodInvoker {
      Consumer<Object> supplier;

      private FastStaticBiSupplierInvoker(Method method) {
         try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method);
            CallSite callSite = LambdaMetafactory.metafactory(
               MethodHandles.lookup(),
               "accept",
               MethodType.methodType(Consumer.class, MethodHandle.class),
               MethodType.methodType(void.class, Object.class),
               MethodHandles.exactInvoker(handle.type()),
               handle.type()
            );
            this.supplier = (Consumer)callSite.getTarget().invokeExact((MethodHandle)handle);
         } catch (Throwable var4) {
            throw new RuntimeException(var4);
         }
      }

      @Override
      public <T> T invoke(Object instance, Object... a) {
         this.supplier.accept(a[0]);
         return null;
      }
   }

   private static final class FastStaticFunctionInvoker extends FastMethodInvoker {
      Function<Object, Object> supplier;

      private FastStaticFunctionInvoker(Method method) {
         try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method);
            CallSite callSite = LambdaMetafactory.metafactory(
               MethodHandles.lookup(),
               "apply",
               MethodType.methodType(Function.class, MethodHandle.class),
               MethodType.methodType(Object.class, Object.class),
               MethodHandles.exactInvoker(handle.type()),
               handle.type()
            );
            this.supplier = (Function)callSite.getTarget().invokeExact((MethodHandle)handle);
         } catch (Throwable var4) {
            throw new RuntimeException(var4);
         }
      }

      @Override
      public <T> T invoke(Object instance, Object... a) {
         return (T)this.supplier.apply(a[0]);
      }
   }

   private static final class FastStaticRunnableInvoker extends FastMethodInvoker {
      Runnable runnable;

      private FastStaticRunnableInvoker(Method method) {
         try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method);
            CallSite callSite = LambdaMetafactory.metafactory(
               MethodHandles.lookup(),
               "run",
               MethodType.methodType(Runnable.class, MethodHandle.class),
               MethodType.methodType(void.class),
               MethodHandles.exactInvoker(handle.type()),
               MethodType.methodType(void.class)
            );
            this.runnable = (Runnable)callSite.getTarget().invokeExact((MethodHandle)handle);
         } catch (Throwable var4) {
            throw new RuntimeException(var4);
         }
      }

      @Override
      public <T> T invoke(Object instance, Object... a) {
         this.runnable.run();
         return null;
      }
   }

   private static final class FastStaticSupplierInvoker extends FastMethodInvoker {
      Supplier<?> supplier;

      private FastStaticSupplierInvoker(Method method) {
         try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method);
            CallSite callSite = LambdaMetafactory.metafactory(
               MethodHandles.lookup(),
               "get",
               MethodType.methodType(Supplier.class, MethodHandle.class),
               MethodType.methodType(Object.class),
               MethodHandles.exactInvoker(handle.type()),
               handle.type()
            );
            this.supplier = (Supplier)callSite.getTarget().invokeExact((MethodHandle)handle);
         } catch (Throwable var4) {
            throw new RuntimeException(var4);
         }
      }

      @Override
      public <T> T invoke(Object instance, Object... a) {
         return (T)this.supplier.get();
      }
   }

   private static final class MethodHandleFallbackInvoker extends FastMethodInvoker {
      MethodHandle handle;

      private MethodHandleFallbackInvoker(Method method) {
         try {
            this.handle = MethodHandles.lookup().unreflect(method);
         } catch (Throwable var3) {
            throw new RuntimeException(var3);
         }
      }

      @Override
      public <T> T invoke(Object instance, Object... a) {
         try {
            return (T)(Object)this.handle.invoke((Object)instance, (Object[])a);
         } catch (Throwable var4) {
            throw new RuntimeException(var4);
         }
      }
   }

   public interface TriFunction<A, B, C, D> {
      A apply(B b, C c, D d);
   }
}
