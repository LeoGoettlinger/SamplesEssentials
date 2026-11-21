package com.viaversion.viaversion.libs.mcstructs.converter.model;

import java.util.Collection;
import java.util.function.Function;
import lombok.Generated;

public interface Result<T> {
   static <T> Result<T> success(T result) {
      return new Result.Success<>(result);
   }

   static <T> Result<T> error(String error) {
      return new Result.Error<>(new CodecException(error));
   }

   static <T> Result<T> error(Throwable cause) {
      return new Result.Error<>(new CodecException(cause));
   }

   static <T> Result<T> mergeErrors(String error, Collection<Result<?>> errors) {
      return new Result.Error<>(new MergedCodecException(error, errors));
   }

   static <T> Result<T> unexpected(Object actual, Class<?>... expected) {
      String[] names = new String[expected.length];

      for (int i = 0; i < expected.length; i++) {
         names[i] = expected[i].getSimpleName();
      }

      return unexpected(actual, names);
   }

   static <T> Result<T> unexpected(Object actual, String... expected) {
      return error("Expected " + String.join("/", expected) + " but got " + (actual == null ? "null" : actual.getClass().getSimpleName()));
   }

   T get();

   CodecException getError();

   T getOrThrow(Function<Throwable, ? extends Throwable> var1);

   T orElse(T var1);

   T orElseThrow(Function<Throwable, ? extends Throwable> var1);

   <N> Result<N> map(Function<T, N> var1);

   <N> Result<N> mapResult(Function<T, Result<N>> var1);

   <N> Result<N> mapError();

   boolean isSuccessful();

   boolean isError();

   @Override
   String toString();

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();

   public static class Error<T> implements Result<T> {
      private final CodecException error;

      private Error(CodecException error) {
         this.error = error;
      }

      @Override
      public T get() {
         throw this.error;
      }

      @Override
      public CodecException getError() {
         return this.error;
      }

      @Override
      public T getOrThrow(Function<Throwable, ? extends Throwable> exceptionSupplier) {
         try {
            throw (Throwable)exceptionSupplier.apply(this.error);
         } catch (Throwable var3) {
            throw var3;
         }
      }

      @Override
      public T orElse(T other) {
         return other;
      }

      @Override
      public T orElseThrow(Function<Throwable, ? extends Throwable> exceptionSupplier) {
         try {
            throw (Throwable)exceptionSupplier.apply(this.error);
         } catch (Throwable var3) {
            throw var3;
         }
      }

      @Override
      public <N> Result<N> map(Function<T, N> mapper) {
         return this.mapError();
      }

      @Override
      public <N> Result<N> mapResult(Function<T, Result<N>> mapper) {
         return this.mapError();
      }

      @Override
      public <N> Result<N> mapError() {
         return (Result<N>)this;
      }

      @Override
      public boolean isSuccessful() {
         return false;
      }

      @Override
      public boolean isError() {
         return true;
      }

      @Override
      public String toString() {
         return "Error{" + this.error.getMessage() + "}";
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof Result.Error)) {
            return false;
         } else {
            Result.Error<?> other = (Result.Error<?>)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$error = this.getError();
               Object other$error = other.getError();
               return this$error == null ? other$error == null : this$error.equals(other$error);
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof Result.Error;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $error = this.getError();
         return result * 59 + ($error == null ? 43 : $error.hashCode());
      }
   }

   public static class Success<T> implements Result<T> {
      private final T result;

      private Success(T result) {
         this.result = result;
      }

      @Override
      public T get() {
         return this.result;
      }

      @Override
      public CodecException getError() {
         return null;
      }

      @Override
      public T getOrThrow(Function<Throwable, ? extends Throwable> exceptionSupplier) {
         return this.result;
      }

      @Override
      public T orElse(T other) {
         return this.result;
      }

      @Override
      public T orElseThrow(Function<Throwable, ? extends Throwable> exceptionSupplier) {
         return this.result;
      }

      @Override
      public <N> Result<N> map(Function<T, N> mapper) {
         return Result.success(mapper.apply(this.result));
      }

      @Override
      public <N> Result<N> mapResult(Function<T, Result<N>> mapper) {
         return mapper.apply(this.result);
      }

      @Override
      public <N> Result<N> mapError() {
         return Result.error("No error");
      }

      @Override
      public boolean isSuccessful() {
         return true;
      }

      @Override
      public boolean isError() {
         return false;
      }

      @Override
      public String toString() {
         return "Success{" + this.result + "}";
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof Result.Success)) {
            return false;
         } else {
            Result.Success<?> other = (Result.Success<?>)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$result = this.result;
               Object other$result = other.result;
               return this$result == null ? other$result == null : this$result.equals(other$result);
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof Result.Success;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $result = this.result;
         return result * 59 + ($result == null ? 43 : $result.hashCode());
      }
   }
}
