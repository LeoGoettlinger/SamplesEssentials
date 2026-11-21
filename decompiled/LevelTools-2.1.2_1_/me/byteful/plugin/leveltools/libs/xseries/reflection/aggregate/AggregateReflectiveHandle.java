package me.byteful.plugin.leveltools.libs.xseries.reflection.aggregate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import me.byteful.plugin.leveltools.libs.xseries.reflection.ReflectiveHandle;
import me.byteful.plugin.leveltools.libs.xseries.reflection.XReflection;
import me.byteful.plugin.leveltools.libs.xseries.reflection.jvm.objects.ReflectedObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class AggregateReflectiveHandle<T, H extends ReflectiveHandle<T>> implements ReflectiveHandle<T> {
   private final List<Callable<H>> handles;
   private Consumer<H> handleModifier;

   @Internal
   public AggregateReflectiveHandle(Collection<Callable<H>> handles) {
      this.handles = new ArrayList<>(handles.size());
      this.handles.addAll(handles);
   }

   public AggregateReflectiveHandle<T, H> or(@NotNull H handle) {
      return this.or(() -> handle);
   }

   public AggregateReflectiveHandle<T, H> or(@NotNull Callable<H> handle) {
      this.handles.add(handle);
      return this;
   }

   public AggregateReflectiveHandle<T, H> modify(@Nullable Consumer<H> handleModifier) {
      this.handleModifier = handleModifier;
      return this;
   }

   public H getHandle() {
      ClassNotFoundException errors = null;

      for (Callable<H> handle : this.handles) {
         try {
            H handled = handle.call();
            if (this.handleModifier != null) {
               this.handleModifier.accept(handled);
            }

            if (!handled.exists()) {
               handled.reflect();
            }

            return handled;
         } catch (Throwable var6) {
            if (errors == null) {
               errors = new ClassNotFoundException("None of the aggregate handles were successful");
            }

            errors.addSuppressed(var6);
         }
      }

      throw XReflection.throwCheckedException(XReflection.relativizeSuppressedExceptions(errors));
   }

   public AggregateReflectiveHandle<T, H> copy() {
      AggregateReflectiveHandle<T, H> handle = new AggregateReflectiveHandle<>(new ArrayList<>(this.handles));
      handle.handleModifier = this.handleModifier;
      return handle;
   }

   @NotNull
   @Override
   public ReflectiveHandle<ReflectedObject> jvm() {
      return this.getHandle().jvm();
   }

   @Override
   public T reflect() throws ReflectiveOperationException {
      ClassNotFoundException errors = null;

      for (Callable<H> handle : this.handles) {
         H handled;
         try {
            handled = handle.call();
            if (this.handleModifier != null) {
               this.handleModifier.accept(handled);
            }
         } catch (Throwable var7) {
            if (errors == null) {
               errors = new ClassNotFoundException("None of the aggregate handles were successful");
            }

            errors.addSuppressed(var7);
            continue;
         }

         try {
            return handled.reflect();
         } catch (Throwable var6) {
            if (errors == null) {
               errors = new ClassNotFoundException("None of the aggregate handles were successful");
            }

            errors.addSuppressed(var6);
         }
      }

      throw (ClassNotFoundException)XReflection.relativizeSuppressedExceptions(errors);
   }
}
