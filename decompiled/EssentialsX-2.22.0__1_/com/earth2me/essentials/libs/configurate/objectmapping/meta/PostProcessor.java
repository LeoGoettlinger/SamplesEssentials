package com.earth2me.essentials.libs.configurate.objectmapping.meta;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.util.Types;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public interface PostProcessor {
   void postProcess(Object instance) throws SerializationException;

   static PostProcessor.Factory methodsAnnotated(final Class<? extends Annotation> annotation) {
      return type -> {
         List<Method> methods = null;

         for (Method method : Types.allDeclaredMethods(GenericTypeReflector.erase(type))) {
            if (method.isAnnotationPresent(annotation)) {
               int modifiers = method.getModifiers();
               if (!Modifier.isAbstract(modifiers)) {
                  if (Modifier.isStatic(modifiers)) {
                     throw new SerializationException(
                        type, "Post-processor method " + method.getName() + "() annotated @" + annotation.getSimpleName() + " must not be static."
                     );
                  }

                  if (method.getParameterCount() != 0) {
                     throw new SerializationException(
                        type, "Post-processor method " + method.getName() + "() annotated @" + annotation.getSimpleName() + " must not take any parameters."
                     );
                  }

                  for (Class<?> exception : method.getExceptionTypes()) {
                     if (!SerializationException.class.isAssignableFrom(exception)) {
                        throw new SerializationException(
                           type,
                           "Post-processor method "
                              + method.getName()
                              + "() annotated @"
                              + annotation.getSimpleName()
                              + " must only throw SerializationException or its subtypes, but is declared to throw "
                              + exception.getSimpleName()
                              + "."
                        );
                     }
                  }

                  method.setAccessible(true);
                  if (methods == null) {
                     methods = new ArrayList<>();
                  }

                  methods.add(method);
               }
            }
         }

         if (methods != null) {
            List<Method> finalMethods = methods;
            return instance -> {
               SerializationException aggregateException = null;

               for (Method postProcessorMethod : finalMethods) {
                  SerializationException exc = null;

                  try {
                     postProcessorMethod.invoke(instance);
                  } catch (InvocationTargetException var8) {
                     if (var8.getCause() instanceof SerializationException) {
                        exc = (SerializationException)var8.getCause();
                        exc.initType(type);
                     } else if (var8.getCause() != null) {
                        exc = new SerializationException(
                           type, "Failure occurred in post-processor method " + postProcessorMethod.getName() + "()", var8.getCause()
                        );
                     } else {
                        exc = new SerializationException(
                           type, "Unknown error occurred attempting to invoke post-processor method " + postProcessorMethod.getName() + "()", var8
                        );
                     }
                  } catch (IllegalArgumentException | IllegalAccessException var9x) {
                     exc = new SerializationException(type, "Failed to invoke post-processor method " + postProcessorMethod.getName() + "()", var9x);
                  }

                  if (exc != null) {
                     if (aggregateException == null) {
                        aggregateException = exc;
                     } else {
                        aggregateException.addSuppressed(exc);
                     }
                  }
               }

               if (aggregateException != null) {
                  throw aggregateException;
               }
            };
         } else {
            return null;
         }
      };
   }

   static PostProcessor.Factory methodsAnnotatedPostProcess() {
      return methodsAnnotated(PostProcess.class);
   }

   public interface Factory {
      @Nullable
      PostProcessor createProcessor(Type type) throws SerializationException;
   }
}
