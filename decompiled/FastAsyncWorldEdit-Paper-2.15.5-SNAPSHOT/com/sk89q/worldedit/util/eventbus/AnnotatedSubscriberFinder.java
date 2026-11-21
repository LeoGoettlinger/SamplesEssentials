package com.sk89q.worldedit.util.eventbus;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

class AnnotatedSubscriberFinder implements SubscriberFindingStrategy {
   @Override
   public Multimap<Class<?>, EventHandler> findAllSubscribers(Object listener) {
      Multimap<Class<?>, EventHandler> methodsInListener = HashMultimap.create();

      for (Class<?> clazz = listener.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
         for (Method method : clazz.getMethods()) {
            Subscribe annotation = method.getAnnotation(Subscribe.class);
            method.setAccessible(true);
            if (annotation != null) {
               Class<?>[] parameterTypes = method.getParameterTypes();
               if (parameterTypes.length != 1) {
                  throw new IllegalArgumentException(
                     "Method "
                        + method
                        + " has @Subscribe annotation, but requires "
                        + parameterTypes.length
                        + " arguments. Event handler methods must require a single argument."
                  );
               }

               Class<?> eventType = parameterTypes[0];

               MethodHandle handle;
               try {
                  handle = MethodHandles.publicLookup().unreflect(method);
               } catch (IllegalAccessException var13) {
                  throw new IllegalArgumentException("Method " + method + " failed to unreflect.", var13);
               }

               EventHandler handler = new MethodHandleEventHandler(annotation.priority(), listener, handle, method.getName());
               methodsInListener.put(eventType, handler);
            }
         }
      }

      return methodsInListener;
   }
}
