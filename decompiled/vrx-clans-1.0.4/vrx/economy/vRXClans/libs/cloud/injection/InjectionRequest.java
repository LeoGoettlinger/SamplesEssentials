package vrx.economy.vRXClans.libs.cloud.injection;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.util.annotation.AnnotationAccessor;

@API(
   status = API.Status.STABLE
)
@Immutable
public interface InjectionRequest<C> {
   @NonNull
   static <C> InjectionRequest<C> of(
      @NonNull final CommandContext<C> context, @NonNull final TypeToken<?> injectedType, @NonNull final AnnotationAccessor annotationAccessor
   ) {
      return InjectionRequestImpl.of(context, injectedType, annotationAccessor);
   }

   @NonNull
   static <C> InjectionRequest<C> of(@NonNull final CommandContext<C> context, @NonNull final TypeToken<?> injectedType) {
      return InjectionRequestImpl.of(context, injectedType, AnnotationAccessor.empty());
   }

   @NonNull
   CommandContext<C> commandContext();

   @NonNull
   TypeToken<?> injectedType();

   @Derived
   @NonNull
   default Class<?> injectedClass() {
      return GenericTypeReflector.erase(this.injectedType().getType());
   }

   @NonNull
   AnnotationAccessor annotationAccessor();
}
