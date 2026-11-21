package de.theredend2000.advancedhunt.lib.xseries.reflection;

import de.theredend2000.advancedhunt.lib.xseries.reflection.jvm.objects.ReflectedObject;
import org.jetbrains.annotations.NotNull;

public class StaticReflectiveHandle<T> implements ReflectiveHandle<T> {
   private final T reflected;
   private final ReflectiveHandle<ReflectedObject> jvm;

   public StaticReflectiveHandle(T reflected, ReflectedObject jvm) {
      this.reflected = reflected;
      this.jvm = new StaticReflectiveHandle<>(jvm);
   }

   private StaticReflectiveHandle(T reflected) {
      this.reflected = reflected;
      this.jvm = null;
   }

   @Override
   public ReflectiveHandle<T> copy() {
      return this;
   }

   @NotNull
   @Override
   public T reflect() throws ReflectiveOperationException {
      return this.reflected;
   }

   @NotNull
   @Override
   public ReflectiveHandle<ReflectedObject> jvm() {
      return (ReflectiveHandle<ReflectedObject>)(this.jvm == null ? this : this.jvm);
   }
}
