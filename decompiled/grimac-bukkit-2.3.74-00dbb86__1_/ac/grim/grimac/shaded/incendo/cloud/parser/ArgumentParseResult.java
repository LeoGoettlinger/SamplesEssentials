package ac.grim.grimac.shaded.incendo.cloud.parser;

import ac.grim.grimac.shaded.incendo.cloud.exception.handling.ExceptionController;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public abstract class ArgumentParseResult<T> {
   private ArgumentParseResult() {
   }

   @NonNull
   public static <T> ArgumentParseResult<T> failure(@NonNull final Throwable failure) {
      return new ArgumentParseResult.ParseFailure<>(failure);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <T> CompletableFuture<ArgumentParseResult<T>> failureFuture(@NonNull final Throwable failure) {
      return new ArgumentParseResult.ParseFailure<T>(failure).asFuture();
   }

   @NonNull
   public static <T> ArgumentParseResult<T> success(@NonNull final T value) {
      return new ArgumentParseResult.ParseSuccess<>(value);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <T> CompletableFuture<ArgumentParseResult<T>> successFuture(@NonNull final T value) {
      return success(value).asFuture();
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public abstract Optional<T> parsedValue();

   @API(
      status = Status.STABLE
   )
   @NonNull
   public abstract Optional<Throwable> failure();

   @API(
      status = Status.STABLE
   )
   @NonNull
   public final CompletableFuture<ArgumentParseResult<T>> asFuture() {
      return CompletableFuture.completedFuture(this);
   }

   @NonNull
   public abstract <O> CompletableFuture<ArgumentParseResult<O>> flatMapSuccessFuture(@NonNull Function<T, CompletableFuture<ArgumentParseResult<O>>> mapper);

   @NonNull
   public abstract <O> CompletableFuture<ArgumentParseResult<O>> mapSuccessFuture(@NonNull Function<T, CompletableFuture<O>> mapper);

   @NonNull
   public abstract <O> ArgumentParseResult<O> flatMapSuccess(@NonNull Function<T, ArgumentParseResult<O>> mapper);

   @NonNull
   public abstract <O> ArgumentParseResult<O> mapSuccess(@NonNull Function<T, O> mapper);

   private static final class ParseFailure<T> extends ArgumentParseResult<T> {
      private final Throwable failure;

      private ParseFailure(@NonNull final Throwable failure) {
         this.failure = ExceptionController.unwrapCompletionException(failure);
      }

      @NonNull
      @Override
      public Optional<T> parsedValue() {
         return Optional.empty();
      }

      @NonNull
      @Override
      public Optional<Throwable> failure() {
         return Optional.of(this.failure);
      }

      @NonNull
      @Override
      public <O> CompletableFuture<ArgumentParseResult<O>> flatMapSuccessFuture(@NonNull final Function<T, CompletableFuture<ArgumentParseResult<O>>> mapper) {
         return CompletableFuture.completedFuture(this.self());
      }

      @NonNull
      @Override
      public <O> CompletableFuture<ArgumentParseResult<O>> mapSuccessFuture(@NonNull final Function<T, CompletableFuture<O>> mapper) {
         return CompletableFuture.completedFuture(this.self());
      }

      @NonNull
      @Override
      public <O> ArgumentParseResult<O> flatMapSuccess(@NonNull final Function<T, ArgumentParseResult<O>> mapper) {
         return this.self();
      }

      @NonNull
      @Override
      public <O> ArgumentParseResult<O> mapSuccess(@NonNull final Function<T, O> mapper) {
         return this.self();
      }

      @NonNull
      private <O> ArgumentParseResult<O> self() {
         return (ArgumentParseResult<O>)this;
      }
   }

   private static final class ParseSuccess<T> extends ArgumentParseResult<T> {
      private final T value;

      private ParseSuccess(@NonNull final T value) {
         this.value = value;
      }

      @NonNull
      @Override
      public Optional<T> parsedValue() {
         return Optional.of(this.value);
      }

      @NonNull
      @Override
      public Optional<Throwable> failure() {
         return Optional.empty();
      }

      @NonNull
      @Override
      public <O> CompletableFuture<ArgumentParseResult<O>> flatMapSuccessFuture(@NonNull final Function<T, CompletableFuture<ArgumentParseResult<O>>> mapper) {
         return mapper.apply(this.value);
      }

      @NonNull
      @Override
      public <O> CompletableFuture<ArgumentParseResult<O>> mapSuccessFuture(@NonNull final Function<T, CompletableFuture<O>> mapper) {
         return mapper.apply(this.value).thenApply(ArgumentParseResult::success);
      }

      @NonNull
      @Override
      public <O> ArgumentParseResult<O> flatMapSuccess(@NonNull final Function<T, ArgumentParseResult<O>> mapper) {
         return mapper.apply(this.value);
      }

      @NonNull
      @Override
      public <O> ArgumentParseResult<O> mapSuccess(@NonNull final Function<T, O> mapper) {
         return ArgumentParseResult.success(mapper.apply(this.value));
      }
   }
}
