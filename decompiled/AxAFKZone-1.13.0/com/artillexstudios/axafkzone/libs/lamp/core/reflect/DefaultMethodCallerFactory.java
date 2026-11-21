package com.artillexstudios.axafkzone.libs.lamp.core.reflect;

import com.artillexstudios.axafkzone.libs.lamp.ktx.call.KotlinConstants;
import java.lang.reflect.Method;
import org.jetbrains.annotations.NotNull;

final class DefaultMethodCallerFactory implements MethodCallerFactory {
   public static final DefaultMethodCallerFactory INSTANCE = new DefaultMethodCallerFactory();

   @NotNull
   @Override
   public MethodCaller createFor(@NotNull Method method) throws Throwable {
      return KotlinConstants.isKotlinClass(method.getDeclaringClass())
         ? MethodCallerFactory.kotlinFunctions().createFor(method)
         : MethodCallerFactory.methodHandles().createFor(method);
   }
}
