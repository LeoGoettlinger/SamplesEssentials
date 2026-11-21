package com.artillexstudios.axtrade.libs.lamp.core.reflect;

import com.artillexstudios.axtrade.libs.lamp.annotation.Optional;
import com.artillexstudios.axtrade.libs.lamp.ktx.call.KotlinConstants;
import com.artillexstudios.axtrade.libs.lamp.ktx.call.KotlinFunction;
import com.artillexstudios.axtrade.libs.lamp.util.Collections;
import java.lang.reflect.Method;
import java.util.List;
import org.jetbrains.annotations.NotNull;

final class KotlinMethodCallerFactory implements MethodCallerFactory {
   public static final KotlinMethodCallerFactory INSTANCE = new KotlinMethodCallerFactory();

   @NotNull
   @Override
   public MethodCaller createFor(@NotNull Method method) {
      KotlinFunction function = KotlinFunction.wrap(method);
      return (instance, arguments) -> {
         List<Object> list = Collections.listOf(arguments);
         list.replaceAll(v -> v == KotlinConstants.ABSENT_VALUE ? null : v);
         return function.call(instance, list, parameter -> parameter.isAnnotationPresent(Optional.class));
      };
   }
}
