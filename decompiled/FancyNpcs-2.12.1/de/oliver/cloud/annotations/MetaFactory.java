package de.oliver.cloud.annotations;

import de.oliver.cloud.meta.CommandMeta;
import de.oliver.cloud.parser.ParserParameters;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

class MetaFactory implements Function<Method, CommandMeta> {
   private final AnnotationParser<?> annotationParser;
   private final Function<ParserParameters, CommandMeta> metaMapper;

   MetaFactory(@NonNull final AnnotationParser<?> annotationParser, @NonNull final Function<ParserParameters, CommandMeta> metaMapper) {
      this.annotationParser = annotationParser;
      this.metaMapper = metaMapper;
   }

   @NonNull
   public CommandMeta apply(@NonNull final Method method) {
      ParserParameters parameters = ParserParameters.empty();
      this.annotationParser.annotationMappers().forEach((annotationClass, mapper) -> {
         Annotation annotation = AnnotationParser.getMethodOrClassAnnotation(method, (Class<Annotation>)annotationClass);
         if (annotation != null) {
            parameters.merge(((AnnotationMapper<Annotation>)mapper).mapAnnotation(annotation));
         }
      });
      return this.metaMapper.apply(parameters);
   }
}
