package com.sk89q.worldedit.util.eventbus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Objects;

public class MethodHandleEventHandler extends EventHandler {
   private final MethodHandle methodHandle;
   private final String methodName;
   private final Object object;

   protected MethodHandleEventHandler(EventHandler.Priority priority, Object object, MethodHandle methodHandle, String methodName) {
      super(priority);
      this.object = object;
      this.methodHandle = methodHandle.bindTo(object).asType(MethodType.methodType(void.class, Object.class));
      this.methodName = methodName;
   }

   @Override
   public void dispatch(Object event) throws Exception {
      try {
         this.methodHandle.invokeExact((Object)event);
      } catch (Error | Exception var3) {
         throw var3;
      } catch (Throwable var4) {
         throw new Exception(var4);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.methodName, this.object);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         MethodHandleEventHandler that = (MethodHandleEventHandler)o;
         return !this.methodName.equals(that.methodName) ? false : Objects.equals(this.object, that.object);
      } else {
         return false;
      }
   }
}
