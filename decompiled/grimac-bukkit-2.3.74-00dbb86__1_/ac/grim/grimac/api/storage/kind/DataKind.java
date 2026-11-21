package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.codec.Codec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.EnumSet;

@ApiStatus.Experimental
public sealed interface DataKind<E, R> permits EventStream, Entity, KeyValueScoped, Counter {
   @NotNull
   String name();

   @NotNull
   Class<E> eventType();

   @NotNull
   Class<R> recordType();

   @NotNull
   Codec<R> codec();

   @NotNull
   EnumSet<Capability> requiredCapabilities();

   @NotNull
   EnumSet<Capability> optionalCapabilities();
}
