package com.artillexstudios.axtrade.libs.lamp.core.reflect;

import java.lang.reflect.Method;
import org.jetbrains.annotations.NotNull;

public interface MethodCallerFactory {
   @NotNull
   MethodCaller createFor(@NotNull Method var1) throws Throwable;

   @NotNull
   static MethodCallerFactory methodHandles() {
      return MethodHandlesCallerFactory.INSTANCE;
   }

   @NotNull
   static MethodCallerFactory kotlinFunctions() {
      return KotlinMethodCallerFactory.INSTANCE;
   }

   @NotNull
   static MethodCallerFactory defaultFactory() {
      return DefaultMethodCallerFactory.INSTANCE;
   }
}
