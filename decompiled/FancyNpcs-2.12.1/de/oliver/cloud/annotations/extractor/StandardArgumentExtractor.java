package de.oliver.cloud.annotations.extractor;

import de.oliver.cloud.annotations.AnnotationParser;
import de.oliver.cloud.annotations.Argument;
import de.oliver.cloud.annotations.ArgumentMode;
import de.oliver.cloud.annotations.DefaultValueFactory;
import de.oliver.cloud.annotations.DescriptionMapper;
import de.oliver.cloud.annotations.SyntaxFragment;
import de.oliver.cloud.annotations.descriptor.ArgumentDescriptor;
import de.oliver.cloud.component.DefaultValue;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

@Immutable
public abstract class StandardArgumentExtractor implements ArgumentExtractor {
   @Nullable
   private static String nullIfEmpty(@NonNull final String string) {
      return string.isEmpty() ? null : string;
   }

   @NonNull
   public static StandardArgumentExtractor create(@NonNull final AnnotationParser<?> annotationParser) {
      return builder(annotationParser).build();
   }

   @NonNull
   public static ImmutableStandardArgumentExtractor.Builder builder(@NonNull final AnnotationParser<?> annotationParser) {
      return ImmutableStandardArgumentExtractor.builder().annotationParser(annotationParser);
   }

   @NonNull
   public static ImmutableStandardArgumentExtractor.Builder builder(@NonNull final StandardArgumentExtractor extractor) {
      return ImmutableStandardArgumentExtractor.builder().from(extractor);
   }

   @NonNull
   public abstract AnnotationParser<?> annotationParser();

   @Default
   @NonNull
   public ParameterNameExtractor parameterNameExtractor() {
      return ParameterNameExtractor.simple();
   }

   @Default
   @NonNull
   public DescriptionMapper descriptionMapper() {
      return this.annotationParser()::mapDescription;
   }

   @NonNull
   @Override
   public final Collection<ArgumentDescriptor> extractArguments(@NonNull final List<SyntaxFragment> syntax, @NonNull final Method method) {
      Map<String, SyntaxFragment> variableFragments = new HashMap<>();
      syntax.stream()
         .filter(fragment -> fragment.argumentMode() != ArgumentMode.LITERAL)
         .forEach(fragment -> variableFragments.put(fragment.major(), fragment));
      Collection<ArgumentDescriptor> arguments = new ArrayList<>();

      for (Parameter parameter : method.getParameters()) {
         String parameterName = this.parameterNameExtractor().extract(parameter);
         DefaultValue<?, ?> defaultValue = null;
         if (parameter.isAnnotationPresent(de.oliver.cloud.annotations.Default.class)) {
            de.oliver.cloud.annotations.Default defaultAnnotation = parameter.getAnnotation(de.oliver.cloud.annotations.Default.class);
            if (defaultAnnotation.name().isEmpty()) {
               defaultValue = DefaultValue.parsed(
                  this.annotationParser().processString(parameter.getAnnotation(de.oliver.cloud.annotations.Default.class).value())
               );
            } else {
               DefaultValueFactory<?, ?> factory = this.annotationParser()
                  .defaultValueRegistry()
                  .named(this.annotationParser().processString(defaultAnnotation.name()))
                  .orElseThrow(
                     () -> new IllegalArgumentException(String.format("No default value factory named '%s' has been registered", defaultAnnotation.name()))
                  );
               defaultValue = factory.create(parameter);
            }
         }

         if (!parameter.isAnnotationPresent(Argument.class)) {
            SyntaxFragment fragment = variableFragments.get(parameterName);
            if (fragment != null) {
               arguments.add(ArgumentDescriptor.builder().parameter(parameter).defaultValue(defaultValue).name(parameterName).build());
            }
         } else {
            Argument argument = parameter.getAnnotation(Argument.class);
            String name;
            if (argument.value().equals("__INFERRED_ARGUMENT_NAME__")) {
               name = parameterName;
            } else {
               name = this.annotationParser().processString(argument.value());
            }

            ArgumentDescriptor argumentDescriptor = ArgumentDescriptor.builder()
               .parameter(parameter)
               .name(name)
               .parserName(nullIfEmpty(this.annotationParser().processString(argument.parserName())))
               .defaultValue(defaultValue)
               .description(this.descriptionMapper().map(argument.description()))
               .suggestions(nullIfEmpty(this.annotationParser().processString(argument.suggestions())))
               .build();
            arguments.add(argumentDescriptor);
         }
      }

      return arguments;
   }
}
