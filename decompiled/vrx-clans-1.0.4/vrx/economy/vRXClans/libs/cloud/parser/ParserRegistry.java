package vrx.economy.vRXClans.libs.cloud.parser;

import io.leangen.geantyref.TypeToken;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import vrx.economy.vRXClans.libs.cloud.suggestion.SuggestionProvider;

@API(
   status = API.Status.STABLE
)
public interface ParserRegistry<C> {
   @This
   <T> ParserRegistry<C> registerParserSupplier(@NonNull TypeToken<T> type, @NonNull Function<ParserParameters, ArgumentParser<C, ?>> supplier);

   @API(
      status = API.Status.STABLE
   )
   @This
   default <T> ParserRegistry<C> registerParser(@NonNull final ParserDescriptor<C, T> descriptor) {
      return this.registerParserSupplier(descriptor.valueType(), parameters -> descriptor.parser());
   }

   @This
   ParserRegistry<C> registerNamedParserSupplier(@NonNull String name, @NonNull Function<ParserParameters, ArgumentParser<C, ?>> supplier);

   @This
   default ParserRegistry<C> registerNamedParser(@NonNull String name, @NonNull ParserDescriptor<C, ?> descriptor) {
      return this.registerNamedParserSupplier(name, parameters -> descriptor.parser());
   }

   @This
   <A extends Annotation> ParserRegistry<C> registerAnnotationMapper(@NonNull Class<A> annotation, @NonNull ParserRegistry.AnnotationMapper<A> mapper);

   @NonNull
   ParserParameters parseAnnotations(@NonNull TypeToken<?> parsingType, @NonNull Collection<? extends Annotation> annotations);

   @NonNull
   <T> Optional<ArgumentParser<C, T>> createParser(@NonNull TypeToken<T> type, @NonNull ParserParameters parserParameters);

   @NonNull
   <T> Optional<ArgumentParser<C, T>> createParser(@NonNull String name, @NonNull ParserParameters parserParameters);

   @API(
      status = API.Status.STABLE
   )
   void registerSuggestionProvider(@NonNull String name, @NonNull SuggestionProvider<C> suggestionProvider);

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   Optional<SuggestionProvider<C>> getSuggestionProvider(@NonNull String name);

   @FunctionalInterface
   @API(
      status = API.Status.STABLE
   )
   public interface AnnotationMapper<A extends Annotation> {
      @NonNull
      ParserParameters mapAnnotation(@NonNull A annotation, @NonNull TypeToken<?> parsedType);
   }
}
