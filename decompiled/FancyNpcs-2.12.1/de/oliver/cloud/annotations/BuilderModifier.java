package de.oliver.cloud.annotations;

import java.lang.annotation.Annotation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
@FunctionalInterface
public interface BuilderModifier<A extends Annotation, C> {
   @NonNull
   de.oliver.cloud.Command.Builder<? extends C> modifyBuilder(@NonNull A annotation, @NonNull de.oliver.cloud.Command.Builder<C> builder);
}
