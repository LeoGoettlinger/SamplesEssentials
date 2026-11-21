package de.oliver.cloud.annotations;

import de.oliver.cloud.component.preprocessor.ComponentPreprocessor;
import java.lang.annotation.Annotation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface PreprocessorMapper<A extends Annotation, C> {
   @NonNull
   ComponentPreprocessor<C> mapAnnotation(@NonNull A annotation);
}
