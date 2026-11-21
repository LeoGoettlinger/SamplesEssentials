package de.oliver.cloud.annotations.assembler;

import de.oliver.cloud.annotation.specifier.Completions;
import de.oliver.cloud.annotations.AnnotationParser;
import de.oliver.cloud.annotations.ArgumentMode;
import de.oliver.cloud.annotations.PreprocessorMapper;
import de.oliver.cloud.annotations.SyntaxFragment;
import de.oliver.cloud.annotations.descriptor.ArgumentDescriptor;
import de.oliver.cloud.component.CommandComponent;
import de.oliver.cloud.component.preprocessor.ComponentPreprocessor;
import de.oliver.cloud.parser.ArgumentParser;
import de.oliver.cloud.parser.ParserDescriptor;
import de.oliver.cloud.parser.ParserParameters;
import de.oliver.cloud.parser.standard.EitherParser;
import de.oliver.cloud.suggestion.Suggestion;
import de.oliver.cloud.suggestion.SuggestionProvider;
import de.oliver.cloud.type.Either;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.INTERNAL,
   consumers = {"de.oliver.cloud.annotations.*"}
)
public final class ArgumentAssemblerImpl<C> implements ArgumentAssembler<C> {
   private final AnnotationParser<C> annotationParser;

   public ArgumentAssemblerImpl(@NonNull final AnnotationParser<C> annotationParser) {
      this.annotationParser = annotationParser;
   }

   @NonNull
   @Override
   public CommandComponent<C> assembleArgument(@NonNull final SyntaxFragment syntaxFragment, @NonNull final ArgumentDescriptor descriptor) {
      Parameter parameter = descriptor.parameter();
      Collection<Annotation> annotations = Arrays.asList(parameter.getAnnotations());
      TypeToken<?> token = TypeToken.get(parameter.getParameterizedType());
      ParserParameters parameters = this.annotationParser.manager().parserRegistry().parseAnnotations(token, annotations);
      ArgumentParser<C, ?> parser;
      if (GenericTypeReflector.isSuperType(Either.class, token.getType())) {
         TypeToken<?> primaryType = TypeToken.get(GenericTypeReflector.getTypeParameter(parameter.getParameterizedType(), Either.class.getTypeParameters()[0]));
         TypeToken<?> fallbackType = TypeToken.get(GenericTypeReflector.getTypeParameter(parameter.getParameterizedType(), Either.class.getTypeParameters()[1]));
         ParserDescriptor<C, ?> primary = this.annotationParser
            .manager()
            .parserRegistry()
            .createParser(primaryType, parameters)
            .map(primaryParser -> ParserDescriptor.of((ArgumentParser<C, ?>)primaryParser, primaryType))
            .orElseThrow(
               () -> new IllegalArgumentException(
                  String.format(
                     "Parameter '%s' has parser 'Either<%s, ?>' but no parser exists for that type", parameter.getName(), token.getType().getTypeName()
                  )
               )
            );
         ParserDescriptor<C, ?> fallback = this.annotationParser
            .manager()
            .parserRegistry()
            .createParser(fallbackType, parameters)
            .map(fallbackParser -> ParserDescriptor.of((ArgumentParser<C, ?>)fallbackParser, fallbackType))
            .orElseThrow(
               () -> new IllegalArgumentException(
                  String.format(
                     "Parameter '%s' has parser 'Either<?, %s>' but no parser exists for that type", parameter.getName(), token.getType().getTypeName()
                  )
               )
            );
         parser = EitherParser.eitherParser(primary, fallback).parser();
      } else if (descriptor.parserName() == null) {
         parser = this.annotationParser
            .manager()
            .parserRegistry()
            .createParser(token, parameters)
            .orElseThrow(
               () -> new IllegalArgumentException(
                  String.format("Parameter '%s' has parser '%s' but no parser exists for that type", parameter.getName(), token.getType().getTypeName())
               )
            );
      } else {
         parser = (ArgumentParser<C, ?>)this.annotationParser
            .manager()
            .parserRegistry()
            .createParser(this.annotationParser.processString(descriptor.parserName()), parameters)
            .orElseThrow(
               () -> new IllegalArgumentException(
                  String.format("Parameter '%s' has parser '%s' but no parser exists for that type", parameter.getName(), token.getType().getTypeName())
               )
            );
      }

      String argumentName = this.annotationParser.processString(descriptor.name());
      if (syntaxFragment.argumentMode() == ArgumentMode.LITERAL) {
         throw new IllegalArgumentException(String.format("Invalid command argument '%s': Missing syntax mapping", argumentName));
      } else {
         CommandComponent.Builder componentBuilder = CommandComponent.builder();
         componentBuilder.commandManager(this.annotationParser.manager())
            .valueType(parameter.getType())
            .name(argumentName)
            .parser(parser)
            .required(syntaxFragment.argumentMode() == ArgumentMode.REQUIRED);
         Completions completions = parameter.getDeclaredAnnotation(Completions.class);
         if (completions != null) {
            List<Suggestion> suggestions = Arrays.stream(completions.value().replace(" ", "").split(","))
               .map(Suggestion::suggestion)
               .collect(Collectors.toList());
            componentBuilder.suggestionProvider(SuggestionProvider.suggesting(suggestions));
         } else if (descriptor.suggestions() != null) {
            String suggestionProviderName = this.annotationParser.processString(descriptor.suggestions());
            Optional<SuggestionProvider<C>> suggestionsFunction = this.annotationParser
               .manager()
               .parserRegistry()
               .getSuggestionProvider(suggestionProviderName);
            componentBuilder.suggestionProvider(
               suggestionsFunction.orElseThrow(
                  () -> new IllegalArgumentException(
                     String.format("There is no suggestion provider with name '%s'. Did you forget to register it?", suggestionProviderName)
                  )
               )
            );
         }

         if (descriptor.description() != null) {
            componentBuilder.description(descriptor.description());
         }

         if (syntaxFragment.argumentMode() == ArgumentMode.OPTIONAL && descriptor.defaultValue() != null) {
            componentBuilder.defaultValue(descriptor.defaultValue());
         }

         for (Annotation annotation : annotations) {
            PreprocessorMapper preprocessorMapper = this.annotationParser.preprocessorMappers().get(annotation.annotationType());
            if (preprocessorMapper != null) {
               ComponentPreprocessor<C> preprocessor = preprocessorMapper.mapAnnotation(annotation);
               componentBuilder.preprocessor(preprocessor);
            }
         }

         return componentBuilder.build();
      }
   }
}
