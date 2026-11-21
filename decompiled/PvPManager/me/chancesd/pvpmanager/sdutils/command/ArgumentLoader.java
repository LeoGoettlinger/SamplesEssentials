package me.chancesd.pvpmanager.sdutils.command;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ArgumentLoader<T> {
   private final Function<String, Optional<?>> contextValidator;
   private final BiFunction<String, Object, CompletableFuture<T>> contextLoader;
   private final Function<String, String> validationErrorMessage;

   private ArgumentLoader(
      Function<String, Optional<?>> contextValidator,
      BiFunction<String, Object, CompletableFuture<T>> contextLoader,
      Function<String, String> validationErrorMessage
   ) {
      this.contextValidator = contextValidator;
      this.contextLoader = contextLoader;
      this.validationErrorMessage = validationErrorMessage;
   }

   public static <T, C> ArgumentLoader<T> of(
      Function<String, Optional<C>> contextValidator, BiFunction<String, C, T> loader, Function<String, String> errorMessage
   ) {
      Function<String, Optional<?>> wrappedValidator = contextValidator::apply;
      BiFunction<String, Object, CompletableFuture<T>> wrappedLoader = (input, ctx) -> CompletableFuture.supplyAsync(() -> loader.apply(input, (C)ctx));
      return new ArgumentLoader<>(wrappedValidator, wrappedLoader, errorMessage);
   }

   public static <T, C> ArgumentLoader<T> of(Function<String, Optional<C>> contextValidator, BiFunction<String, C, T> loader, String errorMessage) {
      return of(contextValidator, loader, input -> errorMessage);
   }

   Optional<?> validateAndGetContext(String input) {
      return this.contextValidator.apply(input);
   }

   public boolean isValid(String input) {
      return this.contextValidator.apply(input).isPresent();
   }

   CompletableFuture<T> loadWithContext(String input, Object context) {
      return this.contextLoader.apply(input, context);
   }

   public String getValidationError(String input) {
      return this.validationErrorMessage.apply(input);
   }
}
