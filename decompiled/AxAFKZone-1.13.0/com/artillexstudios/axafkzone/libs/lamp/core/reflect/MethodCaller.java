package com.artillexstudios.axafkzone.libs.lamp.core.reflect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MethodCaller {
   Object call(@Nullable Object var1, Object... var2);

   default MethodCaller.BoundMethodCaller bindTo(@Nullable Object instance) {
      return arguments -> this.call(instance, arguments);
   }

   public interface BoundMethodCaller {
      Object call(@NotNull Object... var1);
   }
}
