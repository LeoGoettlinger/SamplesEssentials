package de.oliver.cloud.annotations;

import de.oliver.cloud.parser.ParserParameters;
import java.lang.annotation.Annotation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
@FunctionalInterface
public interface AnnotationMapper<A extends Annotation> {
   @NonNull
   ParserParameters mapAnnotation(@NonNull A annotation);
}
