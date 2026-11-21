package com.fastasyncworldedit.core.extension.platform.binding;

import com.fastasyncworldedit.core.util.StringMan;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;
import org.apache.logging.log4j.Logger;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.exception.StopExecutionException;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.InjectedValueStore;
import org.enginehub.piston.inject.Key;

public class Bindings {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final WorldEdit worldEdit;

   public Bindings(WorldEdit worldEdit) {
      this.worldEdit = worldEdit;
   }

   public WorldEdit getWorldEdit() {
      return this.worldEdit;
   }

   public void register(InjectedValueStore store, CommandManager manager) {
      for (Method method : this.getClass().getDeclaredMethods()) {
         this.register(method, store, manager);
      }
   }

   private boolean register(Method method, InjectedValueStore store, CommandManager manager) {
      final Binding binding = method.getAnnotation(Binding.class);
      if (binding == null) {
         return false;
      } else {
         Annotation[] annotations = method.getAnnotations();
         Class<?> ret = method.getReturnType();
         Key key;
         if (annotations.length == 1) {
            key = Key.of(ret);
         } else {
            if (annotations.length != 2) {
               LOGGER.error("Cannot annotate: {} with {}", method, StringMan.getString(annotations));
               return false;
            }

            Annotation annotation = annotations[0] == binding ? annotations[1] : annotations[0];
            key = Key.of(ret, annotation);
         }

         Class<?>[] params = method.getParameterTypes();
         Annotation[][] paramAnns = method.getParameterAnnotations();
         final Function<InjectedValueAccess, Object>[] argsFunc = new Function[params.length];
         boolean provide = true;

         for (int i = 0; i < params.length; i++) {
            Class<?> param = params[i];
            if (param != String.class) {
               Annotation[] paramAnn = paramAnns[i];
               Key paramKey;
               if (paramAnn.length == 1) {
                  paramKey = Key.of(param, paramAnn[0]);
               } else {
                  if (paramAnn.length != 0) {
                     throw new UnsupportedOperationException("Only one annotation is permitted for " + method);
                  }

                  paramKey = Key.of(param);
               }

               argsFunc[i] = v -> v.injectedValue(paramKey);
            } else {
               if (!provide) {
                  throw new UnsupportedOperationException("Only one argument is allowed");
               }

               provide = false;
            }
         }

         if (provide) {
            store.injectValue(key, access -> Optional.ofNullable(this.invoke(null, argsFunc, access, method)));
         } else {
            manager.registerConverter(key, new ArgumentConverter<Object>() {
               @Override
               public Component describeAcceptableArguments() {
                  return TextComponent.of(binding.value());
               }

               @Override
               public ConversionResult<Object> convert(String s, InjectedValueAccess access) {
                  try {
                     Object o = Bindings.this.invoke(s, argsFunc, access, method);
                     return (ConversionResult<Object>)(o == null ? FailedConversion.from(new NullPointerException()) : SuccessfulConversion.fromSingle(o));
                  } catch (Throwable var4) {
                     return FailedConversion.from(var4);
                  }
               }
            });
         }

         return true;
      }
   }

   private Object invoke(String arg, Function<InjectedValueAccess, Object>[] argsFunc, InjectedValueAccess access, Method method) {
      try {
         Object[] args = new Object[argsFunc.length];

         for (int i = 0; i < argsFunc.length; i++) {
            Function<InjectedValueAccess, Object> func = argsFunc[i];
            if (func != null) {
               Optional optional = (Optional)func.apply(access);
               args[i] = optional.orElse(null);
            } else {
               args[i] = arg;
            }
         }

         return method.invoke(this, args);
      } catch (InvocationTargetException | IllegalAccessException var9) {
         if (!(var9.getCause() instanceof StopExecutionException)) {
            throw new RuntimeException(var9);
         } else {
            return null;
         }
      }
   }
}
