package me.byteful.plugin.leveltools.libs.xseries.reflection.proxy.processors;

import java.lang.reflect.Method;
import me.byteful.plugin.leveltools.libs.xseries.reflection.ReflectiveHandle;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class ProxyMethodInfo {
   public final ReflectiveHandle<?> handle;
   public final Method interfaceMethod;
   public final MappedType rType;
   public final MappedType[] pTypes;

   public ProxyMethodInfo(ReflectiveHandle<?> handle, Method interfaceMethod, MappedType rType, MappedType[] pTypes) {
      this.handle = handle;
      this.interfaceMethod = interfaceMethod;
      this.rType = rType;
      this.pTypes = pTypes;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + '(' + this.interfaceMethod + ')';
   }
}
