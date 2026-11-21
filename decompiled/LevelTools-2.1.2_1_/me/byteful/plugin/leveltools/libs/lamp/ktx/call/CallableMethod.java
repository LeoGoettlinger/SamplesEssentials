package me.byteful.plugin.leveltools.libs.lamp.ktx.call;

import java.lang.reflect.Method;
import me.byteful.plugin.leveltools.libs.lamp.core.reflect.MethodCaller;
import me.byteful.plugin.leveltools.libs.lamp.util.Preconditions;
import org.jetbrains.annotations.NotNull;

public final class CallableMethod {
   private final Method method;
   private final MethodCaller caller;

   CallableMethod(@NotNull Method method, @NotNull MethodCaller caller) {
      this.method = method;
      this.caller = caller;
   }

   @NotNull
   public Method getMethod() {
      return this.method;
   }

   @NotNull
   public static CallableMethod of(@NotNull Method method, @NotNull MethodCaller caller) {
      Preconditions.notNull(method, "method");
      Preconditions.notNull(caller, "caller");
      return new CallableMethod(method, caller);
   }

   public MethodCaller getCaller() {
      return this.caller;
   }
}
