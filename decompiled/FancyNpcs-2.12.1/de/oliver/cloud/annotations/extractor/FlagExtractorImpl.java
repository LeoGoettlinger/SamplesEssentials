package de.oliver.cloud.annotations.extractor;

import de.oliver.cloud.annotations.AnnotationParser;
import de.oliver.cloud.annotations.DescriptionMapper;
import de.oliver.cloud.annotations.Flag;
import de.oliver.cloud.annotations.descriptor.FlagDescriptor;
import de.oliver.cloud.permission.Permission;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = Status.INTERNAL,
   consumers = {"de.oliver.cloud.annotations.*"}
)
public final class FlagExtractorImpl implements FlagExtractor {
   private final AnnotationParser<?> annotationParser;
   private final DescriptionMapper descriptionMapper;

   @Nullable
   private static String nullIfEmpty(@NonNull final String string) {
      return string.isEmpty() ? null : string;
   }

   public FlagExtractorImpl(@NonNull final AnnotationParser<?> annotationParser) {
      this.annotationParser = annotationParser;
      this.descriptionMapper = this.annotationParser::mapDescription;
   }

   @NonNull
   @Override
   public Collection<FlagDescriptor> extractFlags(@NonNull final Method method) {
      Collection<FlagDescriptor> flags = new LinkedList<>();

      for (Parameter parameter : method.getParameters()) {
         if (parameter.isAnnotationPresent(Flag.class)) {
            Flag flag = parameter.getAnnotation(Flag.class);
            String flagName = this.annotationParser.processString(flag.value());
            String name;
            if (flagName.equals("__INFERRED_ARGUMENT_NAME__")) {
               name = parameter.getName();
            } else {
               name = flagName;
            }

            FlagDescriptor flagDescriptor = FlagDescriptor.builder()
               .parameter(parameter)
               .name(name)
               .description(this.descriptionMapper.map(flag.description()))
               .aliases(this.annotationParser.processStrings(Arrays.asList(flag.aliases())))
               .permission(Permission.of(this.annotationParser.processString(flag.permission())))
               .repeatable(flag.repeatable())
               .parserName(nullIfEmpty(this.annotationParser.processString(flag.parserName())))
               .suggestions(nullIfEmpty(this.annotationParser.processString(flag.suggestions())))
               .build();
            flags.add(flagDescriptor);
         }
      }

      return flags;
   }
}
